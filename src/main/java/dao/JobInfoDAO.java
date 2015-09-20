package dao;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import pojo.JobInfo;
import configs.MongoConfigs;

import java.util.List;
import java.util.function.Consumer;

//TODO: add error handling
public class JobInfoDAO extends AbstractDao {

    private MongoClient mongo;

    public JobInfoDAO(Vertx vertx) {
        mongo = MongoClient.createShared(vertx, MongoConfigs.getConfigs());
    }

    public void getJob(final String jobId, Consumer<List<JsonObject>> successHandler, Consumer<Throwable> errorHandler) {
        mongo.find("jobs", new JsonObject().put("_id", jobId), lookup -> {
            processListResult(lookup, successHandler, errorHandler);
        });
    }

    public void insertJob(JobInfo jobInfo, Consumer<String> successHandler, Consumer<Throwable> errorHandler) {
        mongo.insert("jobs", jobInfo.toJson(), lookup -> {
            processResult(lookup, successHandler, errorHandler);
        });
    }

    public void updateJob(JobInfo jobInfo, Consumer<String> successHandler, Consumer<Throwable> errorHandler) {
        mongo.save("jobs", jobInfo.toJson(), lookup -> {
            processResult(lookup, successHandler, errorHandler);
        });
    }
}
