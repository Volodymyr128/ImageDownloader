package verticles;

import constants.JobStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import pojo.JobInfo;

import java.util.Map;

import static constants.SystemEvents.SUBMIT_JOB;
import static constants.SystemEvents.DOWNLOAD_IMAGE;

//TODO: not worker, single instance. How it should be deployed?
public class JobManager extends AbstractVerticle {

    //TODO: how to track job status? Maybe persist it?
    //TODO: how to track image download progress?
    private Map<String, JobInfo>

    @Override
    public void start() {
        EventBus eb = vertx.eventBus();

        eb.consumer(SUBMIT_JOB.toString(), message -> {
            JsonObject body = (JsonObject) message.body();
            String pageUrl = body.getString("pageUrl");
            body.getJsonArray("imageUrl").stream().forEach(imageUrl -> {
                JsonObject downloadImagePayload =
                        new JsonObject().put("pageUrl", pageUrl).put("imageUrl", imageUrl);
                eb.send(DOWNLOAD_IMAGE.toString(), downloadImagePayload, reply -> {
                    if (reply.succeeded()) {
                        //TODO: update job status if needed / persist job if needed
                    } else {
                        //TODO: do something, maybe resend task
                    }
                });
            });
        });
    }

    public JobStatus getJobStatus(String pageUrl) {

    }
}