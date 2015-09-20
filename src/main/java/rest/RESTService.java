package rest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import services.HtmlService;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

//TODO: more deep directory tree
//TODO: test pub/sub queuing
//TODO: add logging
//TODO: add clustering
public class RESTService extends AbstractVerticle {

    private Vertx vertx;

    public static void main(String[] args) {
        Consumer<Vertx> runner = vertx -> {
            try {
                vertx.deployVerticle(RESTService.class.getName());
            } catch (Throwable t) {
                t.printStackTrace();
            }
        };
        Vertx vertx = Vertx.vertx(new VertxOptions().setHAEnabled(true));
        runner.accept(vertx);
    }

    @Override
    public void start() {
        VertxOptions options = new VertxOptions();
        vertx = Vertx.vertx(options);

        int poolSize = options.getInternalBlockingPoolSize();
        DeploymentOptions deploymentOptions = new DeploymentOptions().setWorker(true).setInstances(poolSize);
        vertx.deployVerticle("verticles.DownloadImageVerticle", deploymentOptions);

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
        EventBus eb = vertx.eventBus();
        String pageUrl = routingContext.request().getParam("pageUrl");
        HttpServerResponse response = routingContext.response();
        try {
            List<String> imagesUrls = HtmlService.parseUrl(pageUrl);

            JsonObject body = new JsonObject().put("pageUrl", pageUrl).put("imageUrl", imagesUrls);

            imagesUrls.stream().forEach(imageUrl -> {
                eb.send("download-image", body, reply -> {
                    if (reply.succeeded()) {
                        reply.result().body();
                    } else {
                        System.out.println("No reply");
                    }
                });
            });
        } catch(IOException e) {
            sendError(500, response);
        }
    }

    private void sendError(int statusCode, HttpServerResponse response) {
        response.setStatusCode(statusCode).end();
    }
}
