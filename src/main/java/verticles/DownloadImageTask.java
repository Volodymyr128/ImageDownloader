package verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import pojo.ImageInfo;
import utils.FileUtils;
import utils.VertxUtils;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.Iterator;

import static constants.SystemEvents.DOWNLOAD_IMAGE;
import static constants.SystemEvents.IMAGE_DOWNLOADED;

public class DownloadImageTask extends AbstractVerticle {

    private final static String ROOT = System.getProperty("user.dir") + File.separator + "file-uploads" + File.separator;


    public static void main(String[] args) {
        int poolSize = new VertxOptions().getInternalBlockingPoolSize();
        VertxOptions vertxOpts = new VertxOptions().setHAEnabled(true);
        DeploymentOptions deployOpts = new DeploymentOptions().setWorker(true).setInstances(poolSize);
        VertxUtils.deploy(DownloadImageTask.class.getName(), vertxOpts, deployOpts);
    }

    @Override
    public void start() {
        //TODO: deploy PageDao
        EventBus eb = vertx.eventBus();

        eb.consumer(DOWNLOAD_IMAGE.toString(), message -> {

            try {
                JsonObject body = (JsonObject) message.body();
                ImageInfo image = saveImage(body.getString("pageUrl"), body.getString("imageUrl"));
                message.reply(image.toJson());
            } catch (IOException e) {
                message.reply("Error" + e);
            }
        });
    }

    public ImageInfo saveImage(String pageUrl, String imageUrl) throws IOException {
        InputStream is = new URL(imageUrl).openStream();
        ImageInputStream iis = ImageIO.createImageInputStream(is);

        BufferedImage image = ImageIO.read(iis);
        String format = getImageFormat(iis, imageUrl);

        ImageInfo daoImage = parseImage(image, format, imageUrl, pageUrl);

        String imagePath = ROOT + daoImage.getLocalImagePath();
        ImageIO.write(image, format, new File(imagePath));

        return daoImage;
    }

    private ImageInfo parseImage(BufferedImage image, String formatName, String imageUrl, String pageUrl) throws IOException {
        int width = image.getWidth(null);
        int height = image.getHeight(null);
        return new ImageInfo(imageUrl,
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
