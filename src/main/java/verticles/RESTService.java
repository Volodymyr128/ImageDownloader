package verticles;

import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import services.HtmlService;
import utils.FileUtils;
import utils.VertxUtils;

import java.io.IOException;
import java.util.List;

import static constants.Events.SUBMIT_JOB;
import static constants.Events.GET_JOB_STATUS;
import static constants.Events.GET_JOB_RESULTS;


//TODO: more deep directory tree
/**
 * Entry point of this app. It maybe clustered to avoid single failure point
 *
 * REST service handles user requests. At the moment it handles three requests:
 * GET /job/:jobId/status - returns status of job by id
 * GET /job/:jobId/results - returns results of job by id (means metainfo, without downloading all images)
 * PUT /job - submit job for downloading all images from pageUrl pased in the body of request
 *
 * Request examples:
 * localhost:8080/job where body { "jobId": "aHR0cDovL2thbHVzaGNpdHkuaWYudWEv" }
 */
public class RESTService extends AbstractVerticle {

    public static void main(String[] args) {
        VertxOptions vertxOpts = new VertxOptions().setHAEnabled(true).setClustered(true);
        VertxUtils.deploy(RESTService.class.getName(), vertxOpts);
    }

    /**
     * Automatically deploy JobManager on startup.
     *
     * Launch HHTP server on localhost with port 8080
     */
    @Override
    public void start(Future<Void> startFuture) {
        VertxUtils.deployAsync(JobManager.class.getName(), new VertxOptions().setClustered(true), res -> {
            if (res.succeeded()) {
                Router router = Router.router(vertx);
                router.route().handler(BodyHandler.create());
                router.get("/job/:jobId/status").handler(this::handleGetJobStatus);
                router.get("/job/:jobId/results").handler(this::handleGetJobResults);
                router.put("/job").handler(this::handleSubmitJob);
                vertx.createHttpServer().requestHandler(router::accept).listen(8080);

                startFuture.complete();
            } else {
                startFuture.fail(res.cause());
            }
        });
    }

    /**
     * Accepts {@code jobId} param from {@param routingContext} and retrieve job status from JobManager
     * @param routingContext
     */
    private void handleGetJobStatus(RoutingContext routingContext) {
        String jobId = routingContext.request().getParam("jobId");
        HttpServerResponse response = routingContext.response();
        if (jobId == null) {
            sendError(400, response);
        } else {
            JsonObject payload = new JsonObject().put("jobId", jobId);
            vertx.eventBus().send(GET_JOB_STATUS.toString(), payload, reply ->{
                if (reply.succeeded()) {
                    JsonObject jobStatus = (JsonObject) reply.result().body();
                    response.putHeader(HttpHeaders.CONTENT_TYPE, "application/json");
                    response.end(jobStatus.encode());
                } else {
                    //TODO: add error handling
                }
            });
        }
    }

    /**
     * Send info about job: like pageUrl, totalImages, downloadedImages, jobStatus; and info about each image: like with, height, path in system
     * @param routingContext
     */
    private void handleGetJobResults(RoutingContext routingContext) {
        String jobId = routingContext.request().getParam("jobId");
        HttpServerResponse response = routingContext.response();
        if (jobId == null) {
            sendError(400, response);
        } else {
            JsonObject payload = new JsonObject().put("jobId", jobId);
            vertx.eventBus().send(GET_JOB_RESULTS.toString(), payload, reply -> {
                if (reply.succeeded()) {
                    JsonObject jobStatus = (JsonObject) reply.result().body();
                    response.putHeader(HttpHeaders.CONTENT_TYPE, "application/json");
                    response.end(jobStatus.encode());
                } else {
                    //TODO: add error handling
                }
            });
        }
    }

    /**
     * Sent DOWNLOAD_IMAGE tasks to DownloadTask verticles via Vert.x's EventBus.
     * EventBus get out-of-the-box message (task) queueing and round-robin algorithm which
     * distribute queued tasks between subscribers
     * @param routingContext
     */
    private void handleSubmitJob(RoutingContext routingContext) {
        EventBus eb = vertx.eventBus();
        String pageUrl = routingContext.request().getParam("pageUrl");
        HttpServerResponse response = routingContext.response();
        try {
            List<String> imagesUrls = HtmlService.parseUrl(pageUrl);
            /** TODO: filter supported by ImageIO formats
             imagesUrls.stream()
                 .filter(url -> FileUtils.isValidFormat(FileUtils.getFormatFromUrl(url)))
                 .collect(Collectors.toList());
             */

            JsonObject payload = new JsonObject()
                    .put("pageUrl", pageUrl)
                    .put("images",  new JsonArray(imagesUrls))
                    .put("totalImageCount", imagesUrls.size());

            String jobId = FileUtils.base64(pageUrl);
            response.putHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            response.end(new JsonObject().put("jobId", jobId).encode());

            eb.send(SUBMIT_JOB.toString(), payload);
        } catch(IOException e) {
            sendError(500, response);
        }
    }

    private void sendError(int statusCode, HttpServerResponse response) {
        response.setStatusCode(statusCode).end();
    }
}
