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

import static constants.Events.DOWNLOAD_IMAGE;

public class DownloadTask extends AbstractVerticle {

    private final static String ROOT = System.getProperty("user.dir") + File.separator + "file-uploads" + File.separator;


    public static void main(String[] args) {
        int poolSize = new VertxOptions().getInternalBlockingPoolSize();
        VertxOptions vertxOpts = new VertxOptions().setHAEnabled(true);
        DeploymentOptions deployOpts = new DeploymentOptions().setWorker(true).setInstances(poolSize);
        VertxUtils.deploy(DownloadTask.class.getName(), vertxOpts, deployOpts);
    }

    @Override
    public void start() {
        EventBus eb = vertx.eventBus();

        eb.consumer(DOWNLOAD_IMAGE.toString(), message -> {

            try {
                JsonObject body = (JsonObject) message.body();
                ImageInfo image = saveImage(body.getString("pageUrl"), body.getString("imageUrl"));
                if (image == null) {
                    message.fail(501, "Can't download image of such format");
                } else {
                    message.reply(image.toJson());
                }
            } catch (IOException e) {
                message.reply("Error" + e);
            }
        });
    }

    /**
     * Save image to local file system
     * @param pageUrl
     * @param imageUrl
     * @return
     * @throws IOException
     */
    public ImageInfo saveImage(String pageUrl, String imageUrl) throws IOException {
        InputStream is = new URL(imageUrl).openStream();
        ImageInputStream iis = ImageIO.createImageInputStream(is);

        BufferedImage image = ImageIO.read(iis);
        if (image == null) {
            return null;
        }

        String format = getImageFormat(iis, imageUrl);

        ImageInfo daoImage = parseImage(image, format, imageUrl, pageUrl);
        String dirName = FileUtils.createDirectory(ROOT, pageUrl);
        String imagePath = new StringBuilder(ROOT)
                .append(dirName)
                .append(File.separator)
                .append(daoImage.getImgName())
                .append(".")
                .append(daoImage.getFormatName())
                .toString();
        ImageIO.write(image, format, new File(imagePath));

        return daoImage;
    }

    private ImageInfo parseImage(BufferedImage image, String formatName, String imageUrl, String pageUrl) throws IOException {
        int width = image.getWidth(null);
        int height = image.getHeight(null);
        return new ImageInfo(imageUrl,
                FileUtils.genValidFileName(formatName),
                pageUrl,
                width,
                height,
                formatName);
    }

    private String getImageFormat(ImageInputStream input, String imageUrl) throws IOException {
        Iterator<ImageReader> iter = ImageIO.getImageReaders(input);
        return (iter.hasNext()) ? iter.next().getFormatName() : FileUtils.getFormatFromUrl(imageUrl);
    }
}
