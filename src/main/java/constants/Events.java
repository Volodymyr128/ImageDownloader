package constants;

public enum Events {
    /**
     * Is triggered when RESTService send download job to worker job
     */
    DOWNLOAD_IMAGE,
    /**
     * This is triggered on parse html page. Pass list of image urls as event payload
     */
    SUBMIT_JOB
}
