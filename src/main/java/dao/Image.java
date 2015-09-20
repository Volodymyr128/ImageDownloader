package dao;

import io.vertx.core.json.JsonObject;

import java.io.File;

public class Image {

    private final String url;
    private final String imgName;
    private final String dirName;
    private final int width;
    private final int height;
    private final String formatName;

    public Image(String url, String ingName, String dirName, int width, int height, String formatName) {
        this.url = url;
        this.imgName = ingName;
        this.dirName = dirName;
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

    public String getDirName() {
        return dirName;
    }

    public String getLocalImagePath() {
        return new StringBuilder(dirName).append(File.separator).append(imgName).toString();
    }

    public JsonObject toJson() {
        return new JsonObject().put("url", url).put("width", width).put("height", height);
    }
}
