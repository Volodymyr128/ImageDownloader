package pojo;

import constants.JobStatus;
import io.vertx.core.json.JsonObject;
import utils.FileUtils;

import java.io.UnsupportedEncodingException;

import static constants.JobStatus.PENDING;

public class JobInfo {

    private String id;
    private String pageUrl;
    private final int totalImageCount;
    private int readyCount;
    private JobStatus jobStatus;

    public JobInfo(String pageUrl, int totalImageCount) {
        this.jobStatus = PENDING;
        this.pageUrl = pageUrl;
        this.totalImageCount = totalImageCount;
        this.readyCount = 0;
        this.id = FileUtils.base64(pageUrl);
    }

    public JobInfo(String pageUrl, int totalImageCount, int readyCount) {
        this.pageUrl = pageUrl;
        this.totalImageCount = totalImageCount;
        this.readyCount = readyCount;
        this.id = FileUtils.base64(pageUrl);
        this.jobStatus = (isCompleted()) ? JobStatus.OK : JobStatus.PENDING;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public int getTotalImageCount() {
        return totalImageCount;
    }

    public JobStatus getJobStatus() {
        return jobStatus;
    }

    public int getReadyCount() {
        return readyCount;
    }

    public void incrementReadyCount() {
        readyCount++;
    }

    public String getId() {
        return id;
    }

    public boolean isCompleted() {
        return totalImageCount == readyCount;
    }

    public JsonObject toJson() {
        return new JsonObject()
                .put("_id", id)
                .put("pageUrl", pageUrl)
                .put("status", jobStatus.toString())
                .put("total", totalImageCount)
                .put("ready", readyCount);
    }

    public static JobInfo parseJobInfo(JsonObject o) {
        String pageUrl = o.getString("pageUrl");
        int totalImageCount = o.getInteger("totalImageCount");
        Integer readyCount = o.getInteger("readyCount");
        return (readyCount == null) ? new JobInfo(pageUrl, totalImageCount) : new JobInfo(pageUrl, totalImageCount, readyCount);
    }

    @Override
    public int hashCode() {
        return pageUrl.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof JobInfo)) {
            return false;
        }
        //There shouldn't be two jobs with the same url
        return ((JobInfo) o).getPageUrl().equals(pageUrl);
    }
}
