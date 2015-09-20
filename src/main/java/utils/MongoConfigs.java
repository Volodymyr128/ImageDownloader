package utils;

import io.vertx.core.json.JsonObject;

//TODO: move to config file or pass as comand line args
public class MongoConfigs {

    private final static String DB_NAME = "DEMO";
    private final static String HOST_NAME = "DESKTOP-C9DUDQV";
    private final static int PORT = 27017;

    public static JsonObject getConfigs() {
        return new JsonObject()
                .put("host", HOST_NAME)
                .put("port", PORT)
                .put("db_name", DB_NAME);
    }
}
