package utils;

import io.vertx.core.*;

import java.util.Optional;
import java.util.function.Consumer;

public class VertxUtils {

    public static void deploy(String verticleID, VertxOptions vertxOpts, DeploymentOptions deploymentOptions, Handler<AsyncResult<String>> completionHandler) {
        Consumer<Vertx> runner = vertx -> {
            try {
                if (deploymentOptions != null) {
                    if (completionHandler != null) {
                        vertx.deployVerticle(verticleID, deploymentOptions, completionHandler);
                    } else {
                        vertx.deployVerticle(verticleID, deploymentOptions);
                    }
                } else {
                    if (completionHandler != null) {
                        vertx.deployVerticle(verticleID, completionHandler);
                    } else {
                        vertx.deployVerticle(verticleID);
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        };
        if (vertxOpts.isClustered()) {
            Vertx.clusteredVertx(vertxOpts, res -> {
                if (res.succeeded()) {
                    Vertx vertx = res.result();
                    runner.accept(vertx);
                } else {
                    res.cause().printStackTrace();
                }
            });
        } else {
            Vertx vertx = Vertx.vertx(vertxOpts);
            runner.accept(vertx);
        }
    }

    public static void deploy(String verticleID, VertxOptions vertxOpts, DeploymentOptions deployOpts) {
        deploy(verticleID, vertxOpts, deployOpts, null);
    }

    public static void deploy(String verticleID, VertxOptions options) {
        deploy(verticleID, options, null, null);
    }

    public static void deployAsync(String verticleID, VertxOptions vertxOpts, DeploymentOptions deployOpts, Handler<AsyncResult<String>> completionHandler) {
        deploy(verticleID, vertxOpts, deployOpts, completionHandler);
    }

    public static void deployAsync(String verticleID, Handler<AsyncResult<String>> completionHandler) {
        deploy(verticleID, new VertxOptions(), null, completionHandler);
    }
}
