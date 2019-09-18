package com.continental.contifactory.joblogger.util;

import hudson.FilePath;
import hudson.model.Cause;
import hudson.model.Job;
import hudson.model.Run;
import jenkins.model.Jenkins;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Jenkins.class, FilePath.class})
public class JenkinsUtilTest {
    private Jenkins jenkins;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        mockStatic(Jenkins.class);
        jenkins = mock(Jenkins.class);
        when(Jenkins.getInstance()).thenReturn(jenkins);
    }

    private void mockRootPath(FilePath rootPath, String remotePath) {
        when(jenkins.getRootPath()).thenReturn(rootPath);
        if (rootPath != null) {
            when(rootPath.getRemote()).thenReturn(remotePath);
        }
    }

    @Test
    public void getJenkinsHomeDirectory_NullJenkins() throws Exception {
        exception.expect(FileNotFoundException.class);
        when(Jenkins.getInstance()).thenReturn(null);
        JenkinsUtil.getJenkinsHomeDirectory();
    }

    @Test
    public void getJenkinsHomeDirectory_NullJenkinsRootPath() throws Exception {
        exception.expect(FileNotFoundException.class);
        mockRootPath(null, null);
        JenkinsUtil.getJenkinsHomeDirectory();
    }

    @Test
    public void getJenkinsHomeDirectory_NullRootPathRemote() throws Exception {
        exception.expect(FileNotFoundException.class);
        mockRootPath(mock(FilePath.class), null);
        JenkinsUtil.getJenkinsHomeDirectory();
    }

    @Test
    public void getJenkinsHomeDirectory_EmptyRootPathRemote() throws Exception {
        exception.expect(FileNotFoundException.class);
        mockRootPath(mock(FilePath.class), "");
        JenkinsUtil.getJenkinsHomeDirectory();
    }

    @Test
    public void getJenkinsHomeDirectory_AllValid() throws Exception {
        mockRootPath(mock(FilePath.class), "testRemoteRootPath");
        assertThat(JenkinsUtil.getJenkinsHomeDirectory(), is("testRemoteRootPath"));
    }

    @Test
    public void getUserContentDirectory_AllValid() throws Exception {
        mockRootPath(mock(FilePath.class), "testRemoteRootPath");
        String expectedDirectory = "testRemoteRootPath" + File.separator + "userContent";
        assertThat(JenkinsUtil.getUserContentDirectory(), is(expectedDirectory));
    }

    @Test
    public void getJobConsoleUrl_NullJenkins() {
        when(Jenkins.getInstance()).thenReturn(null);
        Run run = mock(Run.class);
        assertThat(JenkinsUtil.getJobConsoleUrl(run), isEmptyString());
    }

    @Test
    public void getJobConsoleUrl_NullRootPath() {
        when(jenkins.getRootUrl()).thenReturn(null);
        Run run = mock(Run.class);
        assertThat(JenkinsUtil.getJobConsoleUrl(run), isEmptyString());
    }

    @Test
    public void getJobConsoleUrl_ValidRootPath() {
        when(jenkins.getRootUrl()).thenReturn("testRootPath/");
        Run run = mock(Run.class);
        when(run.getUrl()).thenReturn("job/foo/32");
        String expectedConsoleUrl = "testRootPath/job/foo/32/console";
        assertThat(JenkinsUtil.getJobConsoleUrl(run), is(expectedConsoleUrl));
    }

    @Test
    public void getFirstCause_EmptyCause() {
        Run run = mock(Run.class);
        when(run.getCauses()).thenReturn(new ArrayList());
        assertThat(JenkinsUtil.getFirstCause(run), isEmptyString());
    }

    @Test
    public void getFirstCause_NotEmptyCause() {
        Run run = mock(Run.class);
        List<Cause> causes = new ArrayList<>();
        Cause firstCause = mock(Cause.class);
        Cause secondCause = mock(Cause.class);
        causes.add(firstCause);
        causes.add(secondCause);
        when(firstCause.getShortDescription()).thenReturn("test first cause");
        when(secondCause.getShortDescription()).thenReturn("test second cause");
        when(run.getCauses()).thenReturn(causes);
        assertThat(JenkinsUtil.getFirstCause(run), is("test first cause"));
    }

    @Test
    public void getBuildName_NullRunParent() {
        Run run = mock(Run.class);
        when(run.getParent()).thenReturn(null);
        assertThat(JenkinsUtil.getBuildName(run), isEmptyString());
    }

    @Test
    public void getBuildName_ValidRunParent() {
        Run run = mock(Run.class);
        Job job = mock(Job.class);
        when(job.getName()).thenReturn("testJobName");
        when(run.getParent()).thenReturn(job);
        assertThat(JenkinsUtil.getBuildName(run), is("testJobName"));
    }

    @Test
    public void getNodeNames_NoSlave() throws Exception {
        Run run = mock(Run.class);

        when(run.getLogFile()).thenReturn(getTestLog("testLog_NoSlave"));
        when(run.getLogReader()).thenCallRealMethod();
        when(run.getLogInputStream()).thenCallRealMethod();

        assertThat(JenkinsUtil.getNodeNames(run), is(empty()));
    }

    @Test
    public void getNodeNames_OneSlave() throws Exception {
        Run run = mock(Run.class);

        when(run.getLogFile()).thenReturn(getTestLog("testLog_OneSlave"));
        when(run.getLogReader()).thenCallRealMethod();
        when(run.getLogInputStream()).thenCallRealMethod();

        List<String> nodeNames = JenkinsUtil.getNodeNames(run);
        assertThat(nodeNames, hasSize(1));
        assertThat(nodeNames, contains("Test Node 1"));
    }

    @Test
    public void getNodeNames_MultipleSlave() throws Exception {
        Run run = mock(Run.class);

        when(run.getLogFile()).thenReturn(getTestLog("testLog_MultipleSlaves"));
        when(run.getLogReader()).thenCallRealMethod();
        when(run.getLogInputStream()).thenCallRealMethod();

        List<String> nodeNames = JenkinsUtil.getNodeNames(run);
        assertThat(nodeNames, hasSize(2));
        assertThat(nodeNames, hasItem("Test Node 1"));
        assertThat(nodeNames, hasItem("Test Node 2"));
    }

    private File getTestLog(final String name) throws FileNotFoundException {
        File testLog = null;

        ClassLoader loader = getClass().getClassLoader();
        URL url = loader.getResource(name);
        if (url != null) {
            testLog = new File(url.getFile());
        }

        if (testLog == null || !testLog.exists()) {
            throw new FileNotFoundException("Cannot find the test log file in test/resources/: " + name);
        }

        return testLog;
    }
}
