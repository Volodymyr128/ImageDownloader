package verticles;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import constants.JobStatus;
import dao.ImageInfoDAO;
import dao.JobInfoDAO;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import pojo.JobInfo;
import utils.FileUtils;
import utils.VertxUtils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Consumer;

import static constants.Events.*;

public class JobManager extends AbstractVerticle {

    private ImageInfoDAO imageDAO;
    private JobInfoDAO jobDAO;

    //TODO: add fail-over on JobManager failure
    private Map<String, JobInfo> pendingJobs = Maps.newHashMap();
    private HashSet<String> completedJobs = Sets.newHashSet();
    private HashSet<String> failedJobs = Sets.newHashSet();

    @Override
    public void start(Future<Void> startFuture) {
        int poolSize = new VertxOptions().getInternalBlockingPoolSize();
        VertxOptions vertxOpts = new VertxOptions().setHAEnabled(true).setClustered(true);
        DeploymentOptions deployOpts = new DeploymentOptions().setWorker(true).setInstances(poolSize);
        VertxUtils.deployAsync(DownloadTask.class.getName(), vertxOpts, deployOpts, res -> {
            if (res.succeeded()) {
                imageDAO = new ImageInfoDAO(vertx);
                jobDAO = new JobInfoDAO(vertx);

                initializeSubmitJobHandler();
                initializeGetJobStatusHandler();
                initializeGetJobResultsHandler();

                try {
                    FileUtils.createDirectory(System.getProperty("user.dir") + File.separator + "file-uploads");
                } catch (UnsupportedEncodingException e) {
                    startFuture.fail("Can't create folder to download images");
                }
                startFuture.complete();
            } else {
                startFuture.fail(res.cause());
            }
        });
    }

    private void initializeGetJobResultsHandler() {
        EventBus eb = vertx.eventBus();
        eb.consumer(GET_JOB_RESULTS.toString(), message -> {

            final Consumer<Throwable> errorHandler = new Consumer<Throwable>() {
                @Override
                public void accept(Throwable throwable) {
                    message.reply(new JsonObject().put("error", "can't fetch images from db"));
                }
            };

            JsonObject payload = (JsonObject) message.body();
            String jobId = payload.getString("jobId");

            jobDAO.getJob(jobId, jobResult -> {
                final JsonObject jobInfo = jobResult.get(0);
                imageDAO.getImages(jobId, imagesResult -> {
                    JsonArray images = new JsonArray(imagesResult);
                    JsonObject response = new JsonObject().put("jobInfo", jobInfo).put("images", images);
                    message.reply(response);
                }, errorHandler);
            }, errorHandler);
        });
    }

    private void initializeGetJobStatusHandler() {
        EventBus eb = vertx.eventBus();
        eb.consumer(GET_JOB_STATUS.toString(), message -> {
            JsonObject payload = (JsonObject) message.body();
            JobStatus status = getJobStatus(payload.getString("jobId"));
            message.reply(new JsonObject(status.toString()));
        });
    }

    private void initializeSubmitJobHandler() {
        EventBus eb = vertx.eventBus();
        eb.consumer(SUBMIT_JOB.toString(), message -> {

            JsonObject body = (JsonObject) message.body();
            final JobInfo job = JobInfo.parseJobInfo(body);

            jobDAO.insertJob(job, results -> {
                pendingJobs.put(job.getId(), job);
            }, error -> {
                failedJobs.add(job.getId());
            });

            body.getJsonArray("images").stream().forEach(imageUrl -> {
                JsonObject downloadImagePayload = new JsonObject()
                        .put("pageUrl", job.getPageUrl())
                        .put("imageUrl", imageUrl);
                eb.send(DOWNLOAD_IMAGE.toString(), downloadImagePayload, reply -> {
                    if (reply.succeeded()) {
                        imageDAO.insertImage((JsonObject) reply.result().body(), result -> {
                            updateJobStatus(job);
                        }, error -> {
                            error.printStackTrace();
                        });
                    } else {
                        //TODO: add some threashold to determine if job is failed
                        failedJobs.add(job.getId()); //job can't succeed if we fail to load some image
                    }
                });
            });
        });
    }

    //TODO: pass response handler
    private JobStatus getJobStatus(String jobId) {
        if (pendingJobs.containsKey(jobId)) {
            return JobStatus.PENDING;
        } else if(completedJobs.contains(jobId)) {
            return JobStatus.OK;
        } else if(failedJobs.contains(jobId)) {
            return JobStatus.ERROR;
        } else {
            /**
             * JobManager crashed. IThanks to high-availability Vert.x deployment option
             * JobManager was recovers but it had loosed all in-memory caches.
             * TODO: we may recover that info on startup (load job statuses in mempry)
             */
        return JobStatus.ERROR; //lack of time
        }
    }

    private void updateJobStatus(JobInfo job) {
        job.incrementReadyCount();
        if (job.isCompleted()) {
            jobDAO.updateJob(job, result -> {
                pendingJobs.remove(job.getId());
                completedJobs.add(job.getId());
            }, error -> {
                //TODO: error handling
            });
        }
    }
}