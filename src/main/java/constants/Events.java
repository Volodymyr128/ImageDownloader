package constants;

public enum Events {
    /**
     * Is triggered when RESTService send download job to worker job
     */
    DOWNLOAD_IMAGE,
    /**
     * This is triggered on parse html page. Pass list of image urls as event payload
     */
    SUBMIT_JOB,
    /**
     * This event is send to JobManager to get job status. Pass job_id as event payload
     */
    GET_JOB_STATUS,
    /**
     * This event is send to get job results. Pass job_id as event payload.
     */
    GET_JOB_RESULTS
}
