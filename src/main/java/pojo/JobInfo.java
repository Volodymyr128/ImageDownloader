package pojo;

import com.google.common.collect.Lists;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

public class JobInfo {

    private List<ImageInfo> images;
    private String pageUrl;
    private int totalImageCount;

    public JobInfo(String pageUrl, int totalImageCount) {
        this.pageUrl = pageUrl;
        this.totalImageCount = totalImageCount;
        images = Lists.newLinkedList();
    }

    public JobInfo(ImageInfo imageInfo) {
        images.add(imageInfo);
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public void addImage(ImageInfo image) {
        images.add(image);
    }

    public JsonObject toJson() {
        JsonArray array = new JsonArray();
        images.stream().forEach(img -> array.add(img.toJson()));
        return new JsonObject()
                .put("_id", pageUrl)
                .put("images", array)
                .put("totalImageCount", totalImageCount);
    }

    public boolean isCompleted() {
        return images.size() == totalImageCount;
    }
}
