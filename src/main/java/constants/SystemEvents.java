package constants;

public enum SystemEvents {
    /**
     * Is triggered when RESTService send download job to worker job
     */
    DOWNLOAD_IMAGE,
    /**
     * Is triggered by worker verticle when image download is finished
     */
    IMAGE_DOWNLOADED
}
