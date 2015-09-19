package verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class DownloadImageVerticle extends AbstractVerticle {

    private final static String ROOT = System.getProperty("user.dir") + File.separator + "file-uploads" + File.separator;
    //TODO: replace with the most compact
    private final static String DEFAULT_FORMAT = "jpg";
    //TODO: fullfill with allowed format list
    private final static List imageFormats = Arrays.asList("jpg", "png", "gif", "tiff");

    @Override
    public void start() {
        EventBus eb = vertx.eventBus();

        //TODO: doc for round-robin algorithm
        eb.consumer("download-image", message -> {

            try {
                System.out.println(message.body().toString());
                dao.Image image = saveImage(message.body().toString());
                message.reply(image.toJson());
            } catch (IOException e) {
                message.reply("Error" + e);
            }
        });
    }

    //TODO: remove
    public static void saveImage_tmp(String imageUrl) throws IOException {
//        dao.Image image = new dao.Image(imageUrl);
//        OutputStream output = new FileOutputStream(ROOT + image.getName());
//        InputStream input = null;
//
//        try {
//            input = new URL(imageUrl).openStream();
//            ByteStreams.copy(input, output);
//        } finally {
//            output.close();
//            if (input != null) {
//                input.close();
//            }
//        }
    }

    public static dao.Image saveImage(String imageUrl) throws IOException {
        InputStream is = new URL(imageUrl).openStream();
        ImageInputStream iis = ImageIO.createImageInputStream(is);

        BufferedImage image = ImageIO.read(iis);
        String format = getImageFormat(iis, imageUrl);

        dao.Image daoImage = parseImage(image, format, imageUrl);

        //TODO: create dir if not exists
        ImageIO.write(image, format, new File(ROOT + daoImage.getName()));

        return daoImage;
    }

    private static dao.Image parseImage(BufferedImage image, String formatName, String imageUrl) throws IOException {
        int width = image.getWidth(null);
        int height = image.getHeight(null);
        String[] paths = imageUrl.split("/");
        String imageName = paths[paths.length - 1];
        return new dao.Image(imageUrl, imageName, width, height, formatName);
    }

    private static String getFormatFromUrl(String imageUrl) {
        String[] chunks = imageUrl.split("\\.");
        if (chunks.length == 0) {
            return DEFAULT_FORMAT;
        }
        String format = chunks[chunks.length - 1];
        return (isValidFormat(format)) ? format : DEFAULT_FORMAT;
    }

    private static boolean isValidFormat(String format) {
        return imageFormats.contains(format.toLowerCase());
    }

    private static String getImageFormat(ImageInputStream input, String imageUrl) throws IOException {
        Iterator<ImageReader> iter = ImageIO.getImageReaders(input);
        return (iter.hasNext()) ? iter.next().getFormatName() : getFormatFromUrl(imageUrl);
    }
}
