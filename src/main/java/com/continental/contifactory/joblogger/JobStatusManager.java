package com.continental.contifactory.joblogger;

import com.continental.contifactory.joblogger.model.JobStatus;
import hudson.model.Run;
import hudson.model.TaskListener;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class caches job status in between job runs.
 * If a job started, the job status will be cached here in it will be removed after the job is completed.
 * The cache is cleared completely if logging is disabled for this plugin.
 *
 * @see com.continental.contifactory.joblogger.listeners.JobRunListener#onStarted(Run, TaskListener)
 * @see com.continental.contifactory.joblogger.listeners.JobRunListener#onCompleted(Run, TaskListener)
 * @see JobLoggerPluginConfiguration#configure(StaplerRequest, JSONObject)
 */
public class JobStatusManager {
    private static JobStatusManager instance = new JobStatusManager();

    private Map<Run, JobStatus> jobStatusMap = Collections.synchronizedMap(new HashMap<>());

    public static JobStatusManager getInstance() {
        return instance;
    }

    public void put(Run run, JobStatus jobStatus) {
        jobStatusMap.put(run, jobStatus);
    }

    public JobStatus get(Run run) {
        return jobStatusMap.get(run);
    }

    public void remove(Run run) {
        jobStatusMap.remove(run);
    }

    public void clear() {
        jobStatusMap.clear();
    }
}
