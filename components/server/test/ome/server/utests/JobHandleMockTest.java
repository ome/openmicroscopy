/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.utests;

import java.sql.Timestamp;

import ome.api.ITypes;
import ome.api.JobHandle;
import ome.api.local.LocalQuery;
import ome.api.local.LocalUpdate;
import ome.conditions.ApiUsageException;
import ome.model.jobs.ImportJob;
import ome.model.jobs.JobStatus;
import ome.security.SecuritySystem;
import ome.services.JobBean;
import ome.services.procs.IProcessManager;
import ome.services.procs.Process;
import ome.system.EventContext;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
@Test(groups = { "jobs", "broken" })
public class JobHandleMockTest extends MockObjectTestCase {

    protected JobHandle jh;

    protected LocalQuery iQuery;
    protected LocalUpdate iUpdate;
    protected ITypes iTypes;
    protected SecuritySystem sec;
    protected EventContext ec;
    protected IProcessManager pm;
    protected Process process;

    protected Mock mockQuery, mockUpdate, mockTypes, mockSec, mockEc, mockPm,
            mockProcess;

    @Override
    @BeforeMethod
    protected void setUp() throws Exception {
        super.setUp();
        mockQuery = mock(LocalQuery.class);
        mockUpdate = mock(LocalUpdate.class);
        mockTypes = mock(ITypes.class);
        mockSec = mock(SecuritySystem.class);
        mockEc = mock(EventContext.class);
        mockPm = mock(IProcessManager.class);
        mockProcess = mock(Process.class);
        iQuery = (LocalQuery) mockQuery.proxy();
        iTypes = (ITypes) mockTypes.proxy();
        iUpdate = (LocalUpdate) mockUpdate.proxy();
        sec = (SecuritySystem) mockSec.proxy();
        pm = (IProcessManager) mockPm.proxy();
        process = (Process) mockProcess.proxy();
        ec = (EventContext) mockEc.proxy();
        org.jmock.core.Stub stub = new org.jmock.core.stub.DefaultResultStub();
        mockEc.setDefaultStub(stub);

        JobBean jb = new JobBean();
        jb.setUpdateService(iUpdate);
        jb.setQueryService(iQuery);
        jb.setTypesService(iTypes);
        jb.setSecuritySystem(sec);
        jb.setProcessManager(pm);
        jh = jb;

    }

    void cleanup() throws Exception {
        try {
            verify();
        } finally {
            tearDown();
        }
    }

    @Test
    public void testSubmit() throws Exception {
        mockSec.expects(atLeastOnce()).method("getEventContext").will(
                returnValue(ec));
        mockUpdate.expects(once()).method("saveAndReturnObject").will(
                returnValue(new ImportJob(1L, true)));
        mockPm.expects(once()).method("process");
        jh.submit(new ImportJob());
        cleanup();
    }

    @Test(expectedExceptions = ApiUsageException.class)
    public void testAttachUnknown() throws Exception {
        mockQuery.expects(once()).method("get").will(returnValue(null));
        jh.attach(1L);
        cleanup();
    }

    @Test
    public void testAttachKnown() throws Exception {
        ImportJob job = jobWithStatus("Submitted");
        mockQuery.expects(once()).method("get").will(returnValue(job));
        register();
        JobStatus rv = jh.attach(1L);
        assertTrue(rv == job.getStatus());
        cleanup();
    }

    @Test
    public void testJobFinished() throws Exception {
        ImportJob job = jobWithStatus("Finished");
        job.setFinished(new Timestamp(0L));
        willGetJob(job);
        register();
        jh.attach(1L);
        assertTrue(jh.jobFinished() != null);
        cleanup();
    }

    @Test
    public void testJobNotFinished() throws Exception {
        ImportJob job = jobWithStatus("NotFinished");
        willGetJob(job);
        register();
        jh.attach(1L);
        assertTrue(jh.jobFinished() == null);
        cleanup();

    }

    @Test
    public void testJobDies() throws Exception {
        fail("NYI");
    }

    @Test
    public void testCallbackGetsUnregistered() throws Exception {
        fail("NYI");
    }

    @Test
    public void testNotificationReturnsAlmostImmediately() throws Exception {
        fail("NYI");
    }

    @Test(expectedExceptions = ApiUsageException.class)
    public void testNoAttachOrSubmit() throws Exception {
        jh.jobFinished();
    }

    @Test
    public void testConstantsHaventChanged() {
        assertEquals(JobHandle.SUBMITTED, "Submitted");
        assertEquals(JobHandle.RESUBMITTED, "Resubmitted");
        assertEquals(JobHandle.QUEUED, "Queued");
        assertEquals(JobHandle.REQUEUED, "Requeued");
        assertEquals(JobHandle.WAITING, "Waiting");
        assertEquals(JobHandle.FINISHED, "Finished");
        assertEquals(JobHandle.ERROR, "Error");
        assertEquals(JobHandle.RUNNING, "Running");

    }

    // Helpers ~
    // =========================================================================

    ImportJob jobWithStatus(String statusStr) {
        JobStatus status = new JobStatus(statusStr);
        ImportJob job = new ImportJob();
        job.setStatus(status);
        return job;
    }

    private void willGetJob(ImportJob job) {
        mockQuery.expects(once()).method("get").will(returnValue(job));
    }

    private void register() {
        mockPm.expects(once()).method("runningProcess").will(
                returnValue(process));
        mockProcess.expects(once()).method("isActive").will(returnValue(true));
        mockProcess.expects(once()).method("registerCallback");
    }

}
