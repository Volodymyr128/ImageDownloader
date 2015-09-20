package dao;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import pojo.ImageInfo;
import pojo.JobInfo;
import utils.VertxUtils;

//TODO: CapedCollection http://docs.mongodb.org/manual/core/capped-collections/
//TODO: use embeded document scheme
//TODO: do we need indexes?

//!!! TODO: make not verticle
public class PageDao extends AbstractVerticle {

    private final static String DB_NAME = "DEMO";
    private MongoClient mongo;

    public static void main(String[] args) {
        VertxUtils.deploy(PageDao.class.getName(), new VertxOptions(), new DeploymentOptions().setWorker(true));
    }

    public void testMongo() { //TODO: remove
        JobInfo page = new JobInfo("some_url");
        page.addImage(createImage("1"));
        page.addImage(createImage("2"));
        page.addImage(createImage("3"));
        createJob(page);
        getJobs();
    }

    public void getJobs() { //TODO: remove
        mongo.find("jobs", new JsonObject(), lookup -> {
            if (lookup.failed()) {

            } else {
                lookup.result();
            }
        });
    }

    private static ImageInfo createImage(String id) {
        return new ImageInfo("some_url", id, "dir_name", 200, 100, "jpg");
    }

    @Override
    public void start() throws Exception {
        //TODO: pass port, host and db_name as command line arguments
        JsonObject configs = new JsonObject()
                .put("host", "DESKTOP-C9DUDQV")
                .put("port", 27017)
                .put("db_name", DB_NAME);
        mongo = MongoClient.createShared(vertx, configs);
        testMongo();
    }

    public void getJob(final String pageUrl) {
        mongo.find("jobs", new JsonObject().put("_id", pageUrl), lookup -> {
            // error handling
            if (lookup.failed()) {
                System.out.println("Failed to find the page with id " + pageUrl);
                //TODO: notify about failure
            }
            lookup.result();
        });
    }

    public void createJob(JobInfo jobInfo) {
        mongo.insert("jobs", jobInfo.toJson(), lookup -> {
            // error handling
            if (lookup.failed()) {
                return;
            }
            //TODO: inform everybody and change job status
        });
    }

    public void deleteJob(String pageUrl) {
        mongo.removeOne("jobs", new JsonObject().put("_id", pageUrl), lookup -> {
            // error handling
            if (lookup.failed()) {
                return;
            }
            //TODO: inform everybody and change job status
        });
    }
}