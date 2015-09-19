package dao;

import io.vertx.core.json.JsonObject;

import java.util.concurrent.atomic.AtomicInteger;

public class Image {

    private final String url;
    private final int width;
    private final int height;
    private final String formatName;
    private final int id;

    private static AtomicInteger uuid = new AtomicInteger(0);

    public Image(String url, String name, int width, int height, String formatName) {
        this.url = url;
        id = uuid.incrementAndGet();
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

    public String getName() {
        String[] urlChunks = url.split("/");
        //TODO: replace with StringBuffer
        return id + urlChunks[urlChunks.length - 1].replaceAll("\\W+", "") + "." + formatName;
    }

    public JsonObject toJson() {
        return new JsonObject().put("url", url).put("width", width).put("height", height);
    }
}
