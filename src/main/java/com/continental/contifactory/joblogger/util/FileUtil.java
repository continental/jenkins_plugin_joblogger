package com.continental.contifactory.joblogger.util;

import com.continental.contifactory.joblogger.JobLoggerPluginConfiguration;

import java.io.File;
import java.util.logging.Logger;

/**
 * This class provides file utility methods which is used by this plugin.
 */
public final class FileUtil {
    private static final Logger LOGGER = Logger.getLogger(FileUtil.class.getName());

    private FileUtil() {
    }

    /**
     * @param csvFile a file to be checked if its size exceed the maximum file size limit
     * @return true, if the given file size exceed the maximum file size limit; false otherwise
     * @see JobLoggerPluginConfiguration#getMaxLogFileSize()
     */
    public static boolean exceedMaximumSize(File csvFile) {
        boolean exceeded = false;

        if (csvFile != null && csvFile.exists()) {
            double csvFileSize = getFileSizeInMB(csvFile);
            if (csvFileSize > JobLoggerPluginConfiguration.get().getMaxLogFileSize()) {
                LOGGER.fine("log file \"" + csvFile.getAbsolutePath() + "\" exceed max size ("
                        + JobLoggerPluginConfiguration.get().getMaxLogFileSize() + "): " +  csvFileSize);
                exceeded = true;
            }
        }

        return exceeded;
    }

    /**
     * @param csvFile a file
     * @return the given file size in megabyte(s)
     * @throws NullPointerException if the given file is null
     */
    public static double getFileSizeInMB(File csvFile) {
        if (csvFile == null) {
            throw new NullPointerException("CSV file cannot be null!");
        }
        // file.length() is in bytes, compute to megabyte
        return Long.valueOf(csvFile.length()).doubleValue() / (1024 * 1024);
    }
}
