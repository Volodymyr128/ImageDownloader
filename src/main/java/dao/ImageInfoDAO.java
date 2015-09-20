package dao;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import utils.MongoConfigs;

import java.util.List;
import java.util.function.Consumer;

//TODO: add error handling
//TODO: add indexes on image#pageUrl field
public class ImageInfoDAO {

    private MongoClient mongo;

    public ImageInfoDAO(Vertx vertx) {
        mongo = MongoClient.createShared(vertx, MongoConfigs.getConfigs());
    }

    public void insertImage(JsonObject imagenfo, Consumer<String> handler) {
        mongo.insert("images", imagenfo, lookup -> {
            if (lookup.failed()) {
                return;
            }
            handler.accept(lookup.result());
        });
    }

    public void getImages(String pageUrl, Consumer<List<JsonObject>> handler) {
        mongo.find("images", new JsonObject().put("pageUrl", pageUrl), lookup -> {
            if(lookup.failed()) {
                return;
            }
            handler.accept(lookup.result());
        });
    }
}
