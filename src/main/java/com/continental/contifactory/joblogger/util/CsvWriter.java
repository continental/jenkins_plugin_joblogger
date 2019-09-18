package com.continental.contifactory.joblogger.util;

import com.continental.contifactory.joblogger.JobLoggerPluginConfiguration;
import com.continental.contifactory.joblogger.model.JobStatus;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Logger;

/**
 * A utility class to write a job status to a CSV log file.
 */
public class CsvWriter {

    private CsvWriter() {
    };

    private static final Logger LOGGER = Logger.getLogger(CsvWriter.class.getName());

    private static final char SEPARATOR = ';';

    /**
     * Writes the given {@link JobStatus} to the given log file.<p>
     * If the given log file's size exceed the maximum size, the content of the log file
     * will be archived in a new archive log file: {@code job_logger_[currentTime].csv},
     * and the log file content will be first cleared before it is written again.
     * <p>
     * The format of the CSV file is:<br>
     * {@code JobName;JobNumber;Result;StartDate;EndDate;Cause;Node;ConsoleUrl}
     *
     * @param csvFile   the CSV log file to be appended with job status information
     * @param jobStatus the {@link JobStatus} to be logged
     */
    public static synchronized void logJobStatus(File csvFile, JobStatus jobStatus) {
        if (csvFile == null) {
            LOGGER.fine("CSV log file cannot be null!");
            return;
        }

        if (csvFile.exists() && FileUtil.exceedMaximumSize(csvFile)) {
            LOGGER.fine("Log file exceed maximum size, archiving log file: " + csvFile.getAbsolutePath());
            archiveLogFile(csvFile);
        }

        try {
            if (!csvFile.exists()) {
                LOGGER.fine("Creating new log file: " + csvFile.getAbsolutePath());
                writeHeader(csvFile);
            }

            writeJobStatus(csvFile, jobStatus);
            LOGGER.fine("Updated log file: " + csvFile.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.severe("IOException during writing csv log file: " + e.getMessage());
        }
    }

    private static void writeJobStatus(File csvFile, JobStatus jobStatus) throws IOException {
        StringBuilder sb = new StringBuilder();
        write(sb, jobStatus.getJobName());
        write(sb, SEPARATOR);
        write(sb, jobStatus.getJobNumber());
        write(sb, SEPARATOR);
        write(sb, jobStatus.getBuildResult());
        write(sb, SEPARATOR);
        write(sb, jobStatus.getStartDate());
        write(sb, SEPARATOR);
        write(sb, jobStatus.getEndDate());
        write(sb, SEPARATOR);
        write(sb, jobStatus.getBuildCause());
        write(sb, SEPARATOR);
        write(sb, jobStatus.getNodeNames());
        write(sb, SEPARATOR);
        write(sb, jobStatus.getConsoleUrl());
        sb.append("\n");
        FileUtils.writeStringToFile(csvFile, sb.toString(), true);
    }

    private static void writeHeader(File csvFile) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("JobName");
        sb.append(SEPARATOR);
        sb.append("JobNumber");
        sb.append(SEPARATOR);
        sb.append("Result");
        sb.append(SEPARATOR);
        sb.append("StartDate");
        sb.append(SEPARATOR);
        sb.append("EndDate");
        sb.append(SEPARATOR);
        sb.append("Cause");
        sb.append(SEPARATOR);
        sb.append("Node");
        sb.append(SEPARATOR);
        sb.append("ConsoleUrl");
        sb.append("\n");
        FileUtils.writeStringToFile(csvFile, sb.toString(), true);
    }

    private static void archiveLogFile(File csvLogFile) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentTime = dateFormat.format(Calendar.getInstance().getTime());
        String logFileName = JobLoggerPluginConfiguration.getLogFileShortname();
        String ext = JobLoggerPluginConfiguration.getLogFileExtension();
        File archiveFile = new File(csvLogFile.getParent() + File.separator + logFileName + "_" + currentTime
                + ext);
        try {
            LOGGER.fine("Copying log file to archive file...");
            FileUtils.copyFile(csvLogFile, archiveFile);
            csvLogFile.delete();
            LOGGER.info("Log file archived in: " + archiveFile.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.warning("IOException: Could not archive log file: " + e.getMessage());
        }
    }

    private static void write(StringBuilder writer, Object object) throws IOException {
        if (object != null) {
            writer.append(String.valueOf(object));
        } else {
            writer.append("");
        }
    }
}
