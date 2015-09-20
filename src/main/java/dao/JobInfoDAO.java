package dao;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import pojo.JobInfo;
import utils.MongoConfigs;

import java.util.List;
import java.util.function.Consumer;

//TODO: add error handling
public class JobInfoDAO {

    private MongoClient mongo;

    public JobInfoDAO(Vertx vertx) {
        mongo = MongoClient.createShared(vertx, MongoConfigs.getConfigs());
    }

    public void getJob(final String pageUrl, Consumer<List<JsonObject>> handler) {
        mongo.find("jobs", new JsonObject().put("_id", pageUrl), lookup -> {
            if (lookup.failed()) {
                return;
            }
            handler.accept(lookup.result());
        });
    }

    public void insertJob(JobInfo jobInfo, Consumer<String> handler) {
        mongo.insert("jobs", jobInfo.toJson(), lookup -> {
            if (lookup.failed()) {
                return;
            }
            handler.accept(lookup.result());
        });
    }

    public void updateJob(JobInfo jobInfo, Consumer<String> handler) {
        mongo.save("jobs", jobInfo.toJson(), lookup -> {
            if (lookup.failed()) {
                return;
            }
            handler.accept(lookup.result());
        });
    }
}
