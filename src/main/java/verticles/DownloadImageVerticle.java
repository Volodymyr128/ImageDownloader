package verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import utils.FileUtils;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.Iterator;

public class DownloadImageVerticle extends AbstractVerticle {

    private final static String ROOT = System.getProperty("user.dir") + File.separator + "file-uploads" + File.separator;


    @Override
    public void start() {
        EventBus eb = vertx.eventBus();

        //TODO: doc for round-robin algorithm
        eb.consumer("download-image", message -> {

            try {
                JsonObject body = new JsonObject(message.body().toString());
                dao.Image image = saveImage(body.getString("pageUrl"), body.getString("imageUrl"));
                message.reply(image.toJson());
            } catch (IOException e) {
                message.reply("Error" + e);
            }
        });
    }

    public dao.Image saveImage(String pageUrl, String imageUrl) throws IOException {
        InputStream is = new URL(imageUrl).openStream();
        ImageInputStream iis = ImageIO.createImageInputStream(is);

        BufferedImage image = ImageIO.read(iis);
        String format = getImageFormat(iis, imageUrl);

        dao.Image daoImage = parseImage(image, format, imageUrl, pageUrl);

        String imagePath = ROOT + daoImage.getLocalImagePath();
        ImageIO.write(image, format, new File(imagePath));

        return daoImage;
    }

    private dao.Image parseImage(BufferedImage image, String formatName, String imageUrl, String pageUrl) throws IOException {
        int width = image.getWidth(null);
        int height = image.getHeight(null);
        return new dao.Image(imageUrl,
                FileUtils.genValidFileName(formatName),
                FileUtils.createDirectory(pageUrl),
                width,
                height,
                formatName);
    }



    private String getImageFormat(ImageInputStream input, String imageUrl) throws IOException {
        Iterator<ImageReader> iter = ImageIO.getImageReaders(input);
        return (iter.hasNext()) ? iter.next().getFormatName() : FileUtils.getFormatFromUrl(imageUrl);
    }
}
