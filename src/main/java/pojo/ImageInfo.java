package pojo;

import io.vertx.core.json.JsonObject;

import java.io.File;

//TODO: rename to ImageInfo
public class ImageInfo {

    //TODO: url and dir name isn't needed here, move it to PageInfo
    private final String url;
    private final String imgName;
    private final String dirName;
    private final int width;
    private final int height;
    private final String formatName;

    public ImageInfo(String url, String imgName, String dirName, int width, int height, String formatName) {
        this.url = url;
        this.imgName = imgName;
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
        return new JsonObject()
                .put("_id", imgName)
                .put("url", url)
                .put("width", width)
                .put("height", height)
                .put("formatName", formatName)
                .put("dirName", dirName);
    }
}
