package com.continental.contifactory.joblogger;

import com.continental.contifactory.joblogger.util.JenkinsUtil;
import hudson.Extension;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.logging.Logger;

/**
 * Global configuration for this plugin.
 */
@Extension
public class JobLoggerPluginConfiguration extends GlobalConfiguration {
    private static final Logger LOGGER = Logger.getLogger(JobLoggerPluginConfiguration.class.getName());
    private static final String LOG_FILE_SHORTNAME = "job_logger";
    private static final String LOG_FILE_EXT = ".csv";
    private static final String LOG_FILE_FULLNAME = LOG_FILE_SHORTNAME + LOG_FILE_EXT;
    private boolean logEnabled;
    private double maxLogFileSize = 1.0; // in MB

    public JobLoggerPluginConfiguration() {
        load();
    }

    public static JobLoggerPluginConfiguration get() {
        return GlobalConfiguration.all().get(JobLoggerPluginConfiguration.class);
    }

    /**
     * Sets enable/disable logging for this plugin.
     *
     * @param logEnabled enable/disable logging
     */
    public void setLogEnabled(boolean logEnabled) {
        this.logEnabled = logEnabled;
    }

    /**
     * Sets the maximum log size limit for this plugin.
     *
     * @param maxLogFileSize the maximum log size limit
     */
    public void setMaxLogFileSize(double maxLogFileSize) {
        this.maxLogFileSize = maxLogFileSize;
    }

    /**
     * @return the plugin's settings if logging is enabled
     */
    public boolean isLogEnabled() {
        return logEnabled;
    }

    /**
     * @return the plugin's settings for the maximum log size limit
     */
    public double getMaxLogFileSize() {
        return maxLogFileSize;
    }

    /**
     * @return the CSV log file for this plugin, which is located in the user content directory of Jenkins.
     * @throws FileNotFoundException if Jenkins home/root path cannot be found
     */
    public File getCsvLogFile() throws FileNotFoundException {
        File userContentDir = new File(JenkinsUtil.getUserContentDirectory());
        return new File(userContentDir.getAbsolutePath() + File.separator + LOG_FILE_FULLNAME);
    }

    /**
     * @return the log file short name (without file extension).
     */
    public static String getLogFileShortname() {
        return LOG_FILE_SHORTNAME;
    }

    /**
     * @return the log file extension.
     */
    public static String getLogFileExtension() {
        return LOG_FILE_EXT;
    }

    @Override
    public boolean configure(StaplerRequest request, JSONObject json) throws FormException {
        logEnabled = json.getBoolean("logEnabled");
        if (!logEnabled) {
            JobStatusManager.getInstance().clear();
        }
        maxLogFileSize = json.getDouble("maxLogFileSize");
        LOGGER.info("Saving configuration: log enabled: " + logEnabled + ", file size limit: " + maxLogFileSize);
        save();
        return true;
    }

    /**
     * Validates the entered maximum log size limit by a user.
     *
     * @param maxLogFileLength max Size of Log File
     * @return {@link FormValidation#ok()} if the entered number is valid; otherwise it will throw a validation error
     */
    public FormValidation doCheckMaxLogFileSize(@QueryParameter("maxLogFileSize") final double maxLogFileLength) {
        if (maxLogFileLength <= 0.0) {
            return FormValidation.error("Max log file size must be a decimal number, greater than zero!");
        }
        return FormValidation.ok();
    }
}
