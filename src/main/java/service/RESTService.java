package service;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import utils.Utils;

public class RESTService extends AbstractVerticle {

    static final String DIR = "image-downloader" + RESTService.class.getPackage().getName().replace(".", "/");

    public static void main(String[] args) {
        Utils.deployVerticle(DIR, RESTService.class.getName());
    }

    @Override
    public void start() {
        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());
        router.get("/job/:jobId/status").handler(this::handleGetJobStatus);
        router.get("/job/:jobId/results").handler(this::handleGetJobResults);
        router.put("/job/:url").handler(this::handleSubmitJob);

        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    }

    private void handleGetJobStatus(RoutingContext routingContext) {
        String jobId = routingContext.request().getParam("jobId");
        HttpServerResponse response = routingContext.response();
        if (jobId == null) {
            sendError(400, response);
        } else {
            //TODO: retrieve {@code jobStatus} from redis
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
            //TODO: retrieve {@code jobResults} from redis
            JsonObject jobResults = new JsonObject("{\"message\": \"Job results for jobId: " + jobId + "\" }");
            if (jobResults == null) {
                sendError(404, response);
            } else {
                response.putHeader("content-type", "application/json").end(jobResults.encodePrettily());
            }
        }
    }

    private void handleSubmitJob(RoutingContext routingContext) {
        String url = routingContext.request().getParam("url");
        //TODO: submit job into separate thread - executeBlocking (managed by Vert.x thread pool)
    }

    private void sendError(int statusCode, HttpServerResponse response) {
        response.setStatusCode(statusCode).end();
    }
}
