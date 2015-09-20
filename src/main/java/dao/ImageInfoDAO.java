package dao;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import configs.MongoConfigs;

import java.util.List;
import java.util.function.Consumer;

public class ImageInfoDAO extends AbstractDao {

    private MongoClient mongo;

    public ImageInfoDAO(Vertx vertx) {
        mongo = MongoClient.createShared(vertx, MongoConfigs.getConfigs());
    }

    public void insertImage(JsonObject imagenfo, Consumer<String> successHandler, Consumer<Throwable> errorHandler) {
        mongo.insert("images", imagenfo, lookup -> {
            processResult(lookup, successHandler, errorHandler);
        });
    }

    public void getImages(String jobId, Consumer<List<JsonObject>> successHandler, Consumer<Throwable> errorHandler) {
        mongo.find("images", new JsonObject().put("jobId", jobId), lookup -> {
            processListResult(lookup, successHandler, errorHandler);
        });
    }
}
