package dao;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import pojo.ImageInfo;
import pojo.PageInfo;

//TODO: CapedCollection http://docs.mongodb.org/manual/core/capped-collections/
//TODO: use embeded document scheme
//TODO: do we need indexes?
public class PageDao extends AbstractVerticle {

    private final static String DB_NAME = "DEMO";
    private MongoClient mongo = MongoClient.createShared(vertx, new JsonObject().put("db_name", DB_NAME));

    public static void main(String[] args) {
        PageInfo page = new PageInfo("some_url");
        page.addImage(createImage("1"));
        page.addImage(createImage("2"));
        page.addImage(createImage("3"));
        new PageDao().createPage(page);
        new PageDao().getPage("some_url");
    }

    private static ImageInfo createImage(String id) {
        return new ImageInfo("some_url", id, "dir_name", 200, 100, "jpg");
    }

    @Override
    public void start() throws Exception {
        mongo.find("users", new JsonObject(), lookup -> {
            // error handling
            if (lookup.failed()) {
            }

            // now convert the list to a JsonArray because it will be easier to encode the final object as the response.
            final JsonArray json = new JsonArray();
            for (JsonObject o : lookup.result()) {
                json.add(o);
            }
        });
    }

    public void getPage(String pageUrl) {
        mongo.find("pages", new JsonObject().put("_id", pageUrl), lookup -> {
            // error handling
            if (lookup.failed()) {
            }
            lookup.result();
        });
    }

    public void createPage(PageInfo pageInfo) {
        mongo.insert("pages", pageInfo.toJson(), lookup -> {
            // error handling
            if (lookup.failed()) {
                return;
            }
            //TODO: inform everybody and change job status
        });
    }

    public void deletePage(String pageUrl) {
        mongo.removeOne("pages", new JsonObject().put("_id", pageUrl), lookup -> {
            // error handling
            if (lookup.failed()) {
                return;
            }
            //TODO: inform everybody and change job status
        });
    }
}