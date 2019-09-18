package com.continental.contifactory.joblogger.util;

import com.continental.contifactory.joblogger.model.JobStatus;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CsvWriter.class, FileUtil.class, Calendar.class})
public class CsvWriterTest {
    private final String HEADER = "JobName;JobNumber;Result;StartDate;EndDate;Cause;Node;ConsoleUrl";

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Before
    public void setUp() {
        PowerMockito.mockStatic(FileUtil.class);
        PowerMockito.mockStatic(Calendar.class);
    }

    private void mockFileUtil(boolean exceedSize) {
        PowerMockito.when(FileUtil.exceedMaximumSize(any())).thenReturn(exceedSize);
    }

    @Test
    public void logJobStatus_EmptyJobStatus_NotExceedMaxSize() throws Exception {
        mockFileUtil(false);

        File tmpFile = new File(temp.getRoot().getAbsolutePath(), "test.log");
        assertThat(tmpFile.length(), is(0L));

        JobStatus jobStatus = new JobStatus();
        CsvWriter.logJobStatus(tmpFile, jobStatus);

        List<String> lines = FileUtils.readLines(tmpFile);
        assertThat(lines, hasItem(HEADER));
        assertThat(lines, hasItem(";0;;;;;;"));
    }

    @Test
    public void logJobStatus_FilledJobStatus_NotExceedMaxSize() throws Exception {
        mockFileUtil(false);
        File tmpLogFile = new File(temp.getRoot().getAbsolutePath(), "test.log");
        assertThat(tmpLogFile.length(), is(0L));

        JobStatus jobStatus = getJobStatus("testJob", 42, "testCause", "testUrl",
                "Mon Mar 12 16:55:42 CET 2018", "Mon Mar 12 17:42:55 CET 2018", "SUCCESS",
                "node 1", "node 2");

        CsvWriter.logJobStatus(tmpLogFile, jobStatus);

        List<String> logFileContent = FileUtils.readLines(tmpLogFile);
        assertThat(logFileContent, hasItem(HEADER));
        String expectedFirstLog = "testJob;42;SUCCESS;Mon Mar 12 16:55:42 CET 2018;Mon Mar 12 17:42:55 CET 2018;testCause;[node 1, node 2];testUrl";
        assertThat(logFileContent, hasItem(expectedFirstLog));

        // log a second job status, check if both are available in the log file
        JobStatus jobStatus2 = getJobStatus("testJob2", 88, "testCause2", "testUrl2",
                "Tue Mar 13 15:54:41 CET 2018", "Tue Mar 13 16:41:54 CET 2018", "FAILURE",
                "node 3");
        CsvWriter.logJobStatus(tmpLogFile, jobStatus2);

        logFileContent = FileUtils.readLines(tmpLogFile);
        assertThat(logFileContent, hasItem(HEADER));
        String expectedSecondLog = "testJob2;88;FAILURE;Tue Mar 13 15:54:41 CET 2018;Tue Mar 13 16:41:54 CET 2018;testCause2;[node 3];testUrl2";
        assertThat(logFileContent, hasItem(expectedFirstLog));
        assertThat(logFileContent, hasItem(expectedSecondLog));
        assertThat(logFileContent.indexOf(expectedFirstLog), is(1));
        assertThat(logFileContent.indexOf(expectedSecondLog), is(2));
    }

    @Test
    public void logJobStatus_FilledJobStatus_ExceedMaxSize() throws Exception {
        // first create and fill a test log file
        File tmpLogFile = new File(temp.getRoot().getAbsolutePath(), "test.log");

        String expectedFirstLog = "testJob;42;SUCCESS;Mon Mar 12 16:55:42 CET 2018;Mon Mar 12 17:42:55 CET 2018;testCause;[node 1, node 2];testUrl";
        String expectedSecondLog = "testJob2;88;FAILURE;Tue Mar 13 15:54:41 CET 2018;Tue Mar 13 16:41:54 CET 2018;testCause2;[node 3];testUrl2";

        FileUtils.writeStringToFile(tmpLogFile, HEADER + "\n", true);
        FileUtils.writeStringToFile(tmpLogFile, expectedFirstLog + "\n", true);
        FileUtils.writeStringToFile(tmpLogFile, expectedSecondLog + "\n", true);

        // mock FileUtil so that the log file will exceed the maximum file size
        mockFileUtil(true);

        // this date will be appended to the archive file's name
        Calendar cal = PowerMockito.mock(Calendar.class);
        PowerMockito.when(Calendar.getInstance()).thenReturn(cal);
        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
        Date currentDate = format.parse("Tue Mar 06 14:55:55 CET 2018");
        PowerMockito.when(cal.getTime()).thenReturn(currentDate);

        // now write a new job status
        JobStatus jobStatus3 = getJobStatus("testJob3", 55, "testCause3", "testUrl3",
                "Wed Mar 14 14:52:39 CET 2018", "Wed Mar 14 15:40:53 CET 2018", "SUCCESS",
                "node 4");
        String expectedNewLog = "testJob3;55;SUCCESS;Wed Mar 14 14:52:39 CET 2018;Wed Mar 14 15:40:53 CET 2018;testCause3;[node 4];testUrl3";

        // archive file should be created AFTER logJobStatus is completed
        File archiveFile = new File(tmpLogFile.getParent() + File.separator + "job_logger_2018-03-06_14-55-55.csv");
        assertThat(archiveFile.exists(), is(false));

        CsvWriter.logJobStatus(tmpLogFile, jobStatus3);

        assertThat(archiveFile.exists(), is(true));

        // check that archive file contains old logs, and not the new log
        List<String> archivedLogFileContent = FileUtils.readLines(archiveFile);
        assertThat(archivedLogFileContent, hasItem(HEADER));
        assertThat(archivedLogFileContent, hasItem(expectedFirstLog));
        assertThat(archivedLogFileContent, hasItem(expectedSecondLog));
        assertThat(archivedLogFileContent, not(hasItem(expectedNewLog)));
        assertThat(archivedLogFileContent.indexOf(expectedFirstLog), is(1));
        assertThat(archivedLogFileContent.indexOf(expectedSecondLog), is(2));

        // check that new log file contains new logs, but not the old logs
        List<String> logFileContent = FileUtils.readLines(tmpLogFile);
        assertThat(logFileContent, hasItem(HEADER));
        assertThat(logFileContent, hasItem(expectedNewLog));
        assertThat(logFileContent, not(hasItem(expectedFirstLog)));
        assertThat(logFileContent, not(hasItem(expectedSecondLog)));
    }

    private JobStatus getJobStatus(String jobName, int buildNr, String cause, String url, String start, String end,
                                   String result, String... nodes) throws ParseException {
        JobStatus jobStatus = new JobStatus();
        jobStatus.setJobName(jobName);
        jobStatus.setJobNumber(buildNr);
        jobStatus.setBuildCause(cause);
        jobStatus.setConsoleUrl(url);
        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
        Date startDate = format.parse(start);
        jobStatus.setStartDate(startDate);
        Date endDate = format.parse(end);
        jobStatus.setEndDate(endDate);
        jobStatus.setBuildResult(result);
        jobStatus.setNodeNames(Arrays.asList(nodes));
        return jobStatus;
    }
}
