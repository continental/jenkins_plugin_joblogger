package com.continental.contifactory.joblogger.util;

import com.continental.contifactory.joblogger.JobLoggerPluginConfiguration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({JobLoggerPluginConfiguration.class})
public class FileUtilTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();
    private JobLoggerPluginConfiguration mockConfig;

    @Before
    public void setUp() {
        mockStatic(JobLoggerPluginConfiguration.class);
        mockConfig = PowerMockito.mock(JobLoggerPluginConfiguration.class);
        PowerMockito.when(JobLoggerPluginConfiguration.get()).thenReturn(mockConfig);
    }

    @Test
    public void getFileSizeInMB_NullFile() {
        exception.expect(NullPointerException.class);
        FileUtil.getFileSizeInMB(null);
    }

    @Test
    public void getFileSizeInMB_ValidFile() {
        checkFileSize(1048576L, 1.0);
        checkFileSize(2097152L, 2.0);
        checkFileSize(1024L, 0.0009765625);
        checkFileSize(0L, 0.0);
    }

    private void checkFileSize(long mockedFileLength, double expectedFileSize) {
        File file = mock(File.class);
        when(file.length()).thenReturn(mockedFileLength);
        double actualSize = FileUtil.getFileSizeInMB(file);
        assertThat(actualSize, is(expectedFileSize));
    }

    @Test
    public void exceedMaximumSize_NullFile() {
        assertThat(FileUtil.exceedMaximumSize(null), is(false));
    }

    @Test
    public void exceedMaximumSize_FileNotExist() {
        File file = mock(File.class);
        when(file.exists()).thenReturn(false);
        assertThat(FileUtil.exceedMaximumSize(file), is(false));
    }

    @Test
    public void exceedMaximumSize_BelowMaxSize() {
        PowerMockito.when(mockConfig.getMaxLogFileSize()).thenReturn(1.0);

        File file = mock(File.class);
        when(file.exists()).thenReturn(true);
        when(file.length()).thenReturn(1048576L);
        assertThat(FileUtil.exceedMaximumSize(file), is(false));

        when(file.length()).thenReturn(1024L);
        assertThat(FileUtil.exceedMaximumSize(file), is(false));

        when(file.length()).thenReturn(0L);
        assertThat(FileUtil.exceedMaximumSize(file), is(false));
    }

    @Test
    public void exceedMaximumSize_ExceedMaxSize() {
        PowerMockito.when(mockConfig.getMaxLogFileSize()).thenReturn(1.0);

        File file = mock(File.class);
        when(file.exists()).thenReturn(true);
        when(file.length()).thenReturn(2097152L);
        assertThat(FileUtil.exceedMaximumSize(file), is(true));
    }
}
