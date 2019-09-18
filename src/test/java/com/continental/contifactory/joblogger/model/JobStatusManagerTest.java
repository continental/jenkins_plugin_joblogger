package com.continental.contifactory.joblogger.model;

import com.continental.contifactory.joblogger.JobStatusManager;
import hudson.model.Run;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;

public class JobStatusManagerTest {

    @Test
    public void testAddAndRemove_OneJobStatus() {
        JobStatusManager mgr = JobStatusManager.getInstance();
        Run run = mock(Run.class);
        assertThat(mgr.get(run), is(nullValue()));

        JobStatus jobStatus = new JobStatus();
        mgr.put(run, jobStatus);
        assertThat(mgr.get(run), is(jobStatus));

        mgr.remove(run);
        assertThat(mgr.get(run), is(nullValue()));
    }

    @Test
    public void testAddAndRemove_MultipleJobStatus() {
        JobStatusManager mgr = JobStatusManager.getInstance();
        Run run1 = mock(Run.class);
        Run run2 = mock(Run.class);
        assertThat(mgr.get(run1), is(nullValue()));
        assertThat(mgr.get(run2), is(nullValue()));

        JobStatus jobStatus1 = new JobStatus();
        mgr.put(run1, jobStatus1);
        assertThat(mgr.get(run1), is(jobStatus1));
        assertThat(mgr.get(run2), is(nullValue()));

        JobStatus jobStatus2 = new JobStatus();
        mgr.put(run2, jobStatus2);
        assertThat(mgr.get(run1), is(jobStatus1));
        assertThat(mgr.get(run2), is(jobStatus2));

        mgr.remove(run1);
        assertThat(mgr.get(run1), is(nullValue()));
        assertThat(mgr.get(run2), is(jobStatus2));

        mgr.remove(run2);
        assertThat(mgr.get(run1), is(nullValue()));
        assertThat(mgr.get(run2), is(nullValue()));
    }

    @Test
    public void testClear() {
        JobStatusManager mgr = JobStatusManager.getInstance();
        Run run1 = mock(Run.class);
        JobStatus jobStatus1 = new JobStatus();
        Run run2 = mock(Run.class);
        JobStatus jobStatus2 = new JobStatus();
        mgr.put(run1, jobStatus1);
        mgr.put(run2, jobStatus2);
        assertThat(mgr.get(run1), is(jobStatus1));
        assertThat(mgr.get(run2), is(jobStatus2));

        mgr.clear();

        assertThat(mgr.get(run1), is(nullValue()));
        assertThat(mgr.get(run2), is(nullValue()));
    }
}
