package verticles;

import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import services.HtmlService;
import utils.VertxUtils;

import java.io.IOException;
import java.util.List;

import static constants.SystemEvents.SUBMIT_JOB;


//TODO: change configs - each cluster should contain RESTService+ImageDownloaderTasks+PageDAO+embedded mongo but also ability to each of elements separately
//TODO: more deep directory tree
//TODO: add logging
//TODO: add clustering
public class RESTService extends AbstractVerticle {

    private Vertx vertx;

    public static void main(String[] args) {
        VertxOptions vertxOpts = new VertxOptions().setHAEnabled(true);
        VertxUtils.deploy(RESTService.class.getName(), vertxOpts, null);
    }

    @Override
    public void start() {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.get("/job/:jobId/status").handler(this::handleGetJobStatus);
        router.get("/job/:jobId/results").handler(this::handleGetJobResults);
        router.put("/job").handler(this::handleSubmitJob);
        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    }

    private void handleGetJobStatus(RoutingContext routingContext) {
        String jobId = routingContext.request().getParam("jobId");
        HttpServerResponse response = routingContext.response();
        if (jobId == null) {
            sendError(400, response);
        } else {
            //TODO: remove it
            JsonObject jobStatus = new JsonObject("{\"message\": \"Job status for jobId: " + jobId + "\" }");
            if (jobStatus == null) {
                sendError(404, response);
            } else {
                response.putHeader("content-type", "application/json").end(jobStatus.encodePrettily());
            }
        }
    }

    private void handleGetJobResults(RoutingContext routingContext) {
        String jobId = routingContext.request().getParam("jobId");
        HttpServerResponse response = routingContext.response();
        if (jobId == null) {
            sendError(400, response);
        } else {
            //TODO: remove it
            JsonObject jobResults = new JsonObject("{\"message\": \"Job results for jobId: " + jobId + "\" }");
            if (jobResults == null) {
                sendError(404, response);
            } else {
                response.putHeader("content-type", "application/json").end(jobResults.encodePrettily());
            }
        }
    }

    /**
     * Sent DOWNLOAD_IMAGE tasks to DownloadImageTask verticles via Vert.x's EventBus.
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

            JsonObject submitJobPayload = new JsonObject()
                    .put("pageUrl", pageUrl)
                    .put("images",  new JsonArray(imagesUrls));
            eb.send(SUBMIT_JOB.toString(), submitJobPayload);
            //TODO: respose?
        } catch(IOException e) {
            sendError(500, response);
        }
    }

    private void sendError(int statusCode, HttpServerResponse response) {
        response.setStatusCode(statusCode).end();
    }
}
