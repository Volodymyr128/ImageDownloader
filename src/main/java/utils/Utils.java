package utils;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

import java.util.function.Consumer;

public class Utils {

    public static void deployVerticle(String dir, String verticleID) {
        System.setProperty("vertx.cwd", dir);
        Consumer<Vertx> runner = vertx -> {
            try {
                vertx.deployVerticle(verticleID);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        };
        Vertx vertx = Vertx.vertx(new VertxOptions().setHAEnabled(true));
        runner.accept(vertx);
    }
}
