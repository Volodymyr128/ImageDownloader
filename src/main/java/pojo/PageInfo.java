package pojo;

import com.google.common.collect.Lists;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

public class PageInfo {

    private List<ImageInfo> images;
    private String pageUrl;

    public PageInfo(String pageUrl) {
        this.pageUrl = pageUrl;
        images = Lists.newLinkedList();
    }

    public PageInfo(ImageInfo imageInfo) {
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
                .put("images", array);
    }
}
