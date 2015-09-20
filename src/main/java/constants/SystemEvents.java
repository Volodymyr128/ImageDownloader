package constants;

public enum SystemEvents {
    /**
     * Is triggered when RESTService send download job to worker job
     */
    DOWNLOAD_IMAGE,
    /**
     * This is triggered on parse html page. Pass list of image urls as event payload
     */
    SUBMIT_JOB,
    /**
     * Is triggered after image downloading completion and storing into db
     */
    IMAGE_DOWNLOADED
}
