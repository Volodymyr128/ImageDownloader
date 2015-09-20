package pojo;

import io.vertx.core.json.JsonObject;

import java.io.File;

//TODO: rename to ImageInfo
public class ImageInfo {

    private final String url;
    private final String imgName;
    private final String pageUrl;
    private final int width;
    private final int height;
    private final String formatName;

    public ImageInfo(String url, String imgName, String pageUrl, int width, int height, String formatName) {
        this.url = url;
        this.imgName = imgName;
        this.pageUrl = pageUrl;
        this.width = width;
        this.height = height;
        this.formatName = formatName;
    }

    public String getUrl() {
        return url;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getFormatName() {
        return formatName;
    }

    public String getImgName() {
        return imgName;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public JsonObject toJson() {
        return new JsonObject()
                .put("_id", imgName)
                .put("pageUrl", pageUrl)
                .put("width", width)
                .put("height", height)
                .put("formatName", formatName)
    }
}
