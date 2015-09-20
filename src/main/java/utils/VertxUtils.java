package utils;

import io.vertx.core.*;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Class which provides different verticles deployment options. In our application we use the next options:
 * DeploymentOptions:
 *  * setWorker - means thread will be executed outside event loop (as part of fixed thread pool)
 *  * setInstances - number of instances of specific verticle. That is used for DownloadTask in sake of parallel downloading
 *  *
 *  VertxOptions:
 *  * setCluster - specify if current verticle should be launched on separate node.
 *      Used for each verticle, if you look at console, you wil see that each verticle is launched on separate Hazelcast instance
 *  * setHAEnabled - out-of-the-box fail-over, means Vertx will relaunch failed verticle
 */
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

    public static void deployAsync(String verticleID, VertxOptions vertxOpts, Handler<AsyncResult<String>> completionHandler) {
        deploy(verticleID, vertxOpts, null, completionHandler);
    }

    public static void deployAsync(String verticleID, Handler<AsyncResult<String>> completionHandler) {
        deploy(verticleID, new VertxOptions(), null, completionHandler);
    }
}
