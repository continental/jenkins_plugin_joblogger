package com.continental.contifactory.joblogger;

import com.continental.contifactory.joblogger.model.JobStatus;
import com.continental.contifactory.joblogger.util.JenkinsUtil;
import hudson.model.Run;
import net.sf.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.kohsuke.stapler.StaplerRequest;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({JenkinsUtil.class})
public class JobLoggerPluginConfigurationTest {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testLogDisabled_ClearJobStatusCache() throws Exception {
        JobStatus testJobStatus = new JobStatus();
        Run run = mock(Run.class);
        JobStatusManager.getInstance().put(run, testJobStatus);
        assertThat(JobStatusManager.getInstance().get(run), is(notNullValue()));

        JobLoggerPluginConfiguration config = mock(JobLoggerPluginConfiguration.class);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("logEnabled", true);
        jsonObject.put("maxLogFileSize", 1L);
        when(config.configure(any(), eq(jsonObject))).thenCallRealMethod();

        config.configure(mock(StaplerRequest.class), jsonObject);
        assertThat(JobStatusManager.getInstance().get(run), is(testJobStatus));

        jsonObject.put("logEnabled", false);
        config.configure(mock(StaplerRequest.class), jsonObject);
        assertThat(JobStatusManager.getInstance().get(run), is(nullValue()));
    }

    @Test
    public void testGetCsvLogFile() throws Exception {
        PowerMockito.mockStatic(JenkinsUtil.class);
        PowerMockito.when(JenkinsUtil.getUserContentDirectory()).thenReturn(tempFolder.getRoot().getAbsolutePath());

        JobLoggerPluginConfiguration config = mock(JobLoggerPluginConfiguration.class);
        when(config.getCsvLogFile()).thenCallRealMethod();
        File csvLogFile = config.getCsvLogFile();
        assertThat(csvLogFile.getAbsolutePath(), is(tempFolder.getRoot().getAbsolutePath() + File.separator +
                "job_logger.csv"));
    }
}
