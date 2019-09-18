package com.continental.contifactory.joblogger.listeners;

import com.continental.contifactory.joblogger.JobLoggerPluginConfiguration;
import com.continental.contifactory.joblogger.JobStatusManager;
import com.continental.contifactory.joblogger.model.JobStatus;
import com.continental.contifactory.joblogger.util.CsvWriter;
import com.continental.contifactory.joblogger.util.JenkinsUtil;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;

import javax.annotation.Nonnull;
import java.io.FileNotFoundException;
import java.util.Calendar;
import java.util.logging.Logger;

/**
 * This {@link RunListener} logs job status to a CSV log file, if logging is enabled.<p>
 * When a job is started, job information will be partially cached in the plugin. When the job is finished,
 * the job information will be completed by adding the job status and the nodes used by the job,
 * and finally the job status is written into the log file.
 */
@Extension
public class JobRunListener extends RunListener<Run<?, ?>> {
    private static final Logger LOGGER = Logger.getLogger(JobRunListener.class.getName());

    @Override
    public void onStarted(Run<?, ?> run, TaskListener listener) {
        if (!JobLoggerPluginConfiguration.get().isLogEnabled()) {
            LOGGER.fine("Logging is disabled");
            return;
        }

        String jobName = JenkinsUtil.getBuildName(run);
        LOGGER.fine("Build started: " + jobName);

        JobStatus job = new JobStatus();
        job.setJobName(jobName);
        job.setJobNumber(run.getNumber());
        if (run.getTimestamp() != null) {
            job.setStartDate(run.getTimestamp().getTime());
        }
        job.setBuildCause(JenkinsUtil.getFirstCause(run));
        job.setConsoleUrl(JenkinsUtil.getJobConsoleUrl(run));

        JobStatusManager.getInstance().put(run, job);
    }

    @Override
    public void onCompleted(Run<?, ?> run, @Nonnull TaskListener listener) {
        if (!JobLoggerPluginConfiguration.get().isLogEnabled()) {
            return;
        }

        JobStatus job = JobStatusManager.getInstance().get(run);
        if (job == null) {
            LOGGER.warning("Could not find build: " + run.getParent().getName()
                    + " in plugin cache. Log entry not created!");
            return;
        }

        job.setEndDate(Calendar.getInstance().getTime());
        job.setBuildResult(run.getResult().toString());
        job.setNodeNames(JenkinsUtil.getNodeNames(run));

        LOGGER.fine("job: " + job);
        listener.getLogger().println(job);

        try {
            CsvWriter.logJobStatus(JobLoggerPluginConfiguration.get().getCsvLogFile(), job);
            LOGGER.fine("Build completed: " + run.getFullDisplayName());
        } catch (FileNotFoundException e) {
            LOGGER.severe("FileNotFoundException: " + e.getMessage());
        } finally {
            JobStatusManager.getInstance().remove(run);
        }
    }


}
