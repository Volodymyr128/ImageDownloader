package utils;


import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public class FileUtils {

    private final static String DEFAULT_FORMAT = "jpeg";
    private final static List knownImageFormats = Arrays.asList("jpg", "png", "jpeg");

    /**
     * Creates directory with name generated as base64(url)
     * @param pageUrl url of web page
     * @return name of created/existed directory
     * @throws UnsupportedEncodingException
     */
    public static String createDirectory(String root, String pageUrl) throws UnsupportedEncodingException {
        File dir = new File(root + genValidDirName(pageUrl));
        if (!dir.exists()) {
            dir.mkdir();
        }
        return dir.getName();
    }

    /**
     * @param pageUrl
     * @return returns unique valid directory name based on {@param pageUrl} argument
     * @throws UnsupportedEncodingException
     */
    public static String genValidDirName(String pageUrl) {
        return base64(pageUrl);
    }

    public static String base64(String srt) {
        try {
            return new String(Base64.getUrlEncoder().encode(srt.getBytes("UTF8")),"UTF-8");
        } catch (UnsupportedEncodingException e) { //just don't know what to do, when system can't generate dir/file names
            throw new RuntimeException(e);
        }
    }

    /**
     * Decodes encoded url based on {@param dirName} argument
     * @param dirName the name of the directory
     * @return valid web page url
     * @throws UnsupportedEncodingException
     */
    public static String getUrlFromDirName(String dirName) throws UnsupportedEncodingException {
        return new String(Base64.getUrlDecoder().decode(dirName.getBytes("UTF8")),"UTF-8");
    }

    /**
     * Generates random unique valid file name
     * @format file extension of image
     * @return unique valid file name
     */
    public static String genValidFileName(String format) {
        return UUID.randomUUID().toString() + "." + format;
    }

    /**
     * @param imageUrl
     * @return returns file format or default value if it is absent
     */
    public static String getFormatFromUrl(String imageUrl) {
        String[] chunks = imageUrl.split("\\.");
        if (chunks.length == 0) {
            return DEFAULT_FORMAT;
        }
        String format = chunks[chunks.length - 1];
        return (isValidFormat(format)) ? format : DEFAULT_FORMAT;
    }

    /**
     * @param format
     * @return return {@code true} if file format is know, {@code false} otherwise
     */
    private static boolean isValidFormat(String format) {
        return knownImageFormats.contains(format.toLowerCase());
    }
}
