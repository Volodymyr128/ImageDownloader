package verticles;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import constants.JobStatus;
import dao.ImageInfoDAO;
import dao.JobInfoDAO;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import pojo.JobInfo;

import java.util.HashSet;
import java.util.Map;
import static constants.Events.SUBMIT_JOB;
import static constants.Events.DOWNLOAD_IMAGE;

//TODO: not worker, single instance. Deploy as singleton + part of RESTService?
public class JobManager extends AbstractVerticle {

    private ImageInfoDAO imageDAO;
    private JobInfoDAO jobDAO;

    //TODO: add fail-over on JobManager failure - need persist. I.e. redis, RabitMQ?
    private Map<String, JobInfo> pendingJobs = Maps.newHashMap();
    private HashSet<String> completedJobs = Sets.newHashSet();
    private HashSet<String> failedJobs = Sets.newHashSet();

    @Override
    public void start() {
        imageDAO = new ImageInfoDAO(vertx);
        jobDAO = new JobInfoDAO(vertx);
        EventBus eb = vertx.eventBus();

        eb.consumer(SUBMIT_JOB.toString(), message -> {

            JsonObject body = (JsonObject) message.body();
            final JobInfo job = JobInfo.parseJobInfo(body);

            jobDAO.insertJob(job, results -> {
                pendingJobs.put(job.getId(), job);
                message.reply(job.toJson());
            });

            body.getJsonArray("images").stream().forEach(imageUrl -> {
                JsonObject downloadImagePayload =
                        new JsonObject().put("pageUrl", job.getPageUrl()).put("imageUrl", imageUrl);
                eb.send(DOWNLOAD_IMAGE.toString(), downloadImagePayload, reply -> {
                    if (reply.succeeded()) {
                        imageDAO.insertImage(new JsonObject(reply.result()), result -> {
                            updateJobStatus(job);
                        });
                    } else {
                        failedJobs.add(job.getId());
                    }
                });
            });
        });
    }

    //TODO: pass response handler
    private JobStatus getJobStatus(String pageUrl) {
        if (pendingJobs.containsKey(pageUrl)) {
            return JobStatus.PENDING;
        } else if(completedJobs.contains(pageUrl)) {
            return JobStatus.OK;
        } else if(failedJobs.contains(pageUrl)) {
            return JobStatus.ERROR;
        } else {
            /**
             * JobManager crashed. IThanks to high-availability Vert.x deployment option
             * JobManager was recovers but it had loosed all in-memory caches.
             * TODO: we may recover that info on startup (load job statuses in mempry)
             */
        return JobStatus.ERROR; //lack of time
//            jobDAO.getJob(pageUrl, result -> {
//                JobInfo job = JobInfo.parseJobInfo(result);
//                if (job.isCompleted()) {
//                    return JobStatus.OK; //how to return async?
//                } else {
//                    return JobStatus.ERROR; //how to return?
//                }
//            });
        }
    }

    private void updateJobStatus(JobInfo job) {
        job.incrementReadyCount();
        if (job.isCompleted()) {
            jobDAO.updateJob(job, result -> {
                pendingJobs.remove(job.getId());
                completedJobs.add(job.getId());
            });
        }
    }
}