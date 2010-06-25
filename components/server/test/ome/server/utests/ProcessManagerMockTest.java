/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.utests;

import java.util.Arrays;
import java.util.List;

import ome.api.ITypes;
import ome.api.IUpdate;
import ome.api.local.LocalQuery;
import ome.model.jobs.ImportJob;
import ome.model.jobs.Job;
import ome.model.jobs.JobStatus;
import ome.security.SecuritySystem;
import ome.services.procs.Process;
import ome.services.procs.ProcessCallback;
import ome.services.procs.ProcessManager;
import ome.services.procs.Processor;
import ome.services.sessions.SessionManager;
import ome.services.util.Executor;
import ome.system.EventContext;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
@Test(groups = { "jobs", "ignore" })
public class ProcessManagerMockTest extends MockObjectTestCase {

    private LocalQuery iQuery;
    private IUpdate iUpdate;
    private ITypes iTypes;
    private SecuritySystem sec;
    private EventContext ec;
    private Processor processor;
    private Process process;
    private ProcessCallback callback;
    private Executor executor;
    private SessionManager manager;

    protected Mock mockQuery, mockUpdate, mockTypes, mockSec, mockEc,
            mockProcessor, mockProcess, mockCallback, mockExecutor,
            mockManager;

    protected ProcessManager pm;

    @Override
    @BeforeMethod
    protected void setUp() throws Exception {
        super.setUp();

        mockManager = mock(SessionManager.class);
        mockExecutor = mock(Executor.class);
        mockQuery = mock(LocalQuery.class);
        mockUpdate = mock(IUpdate.class);
        mockTypes = mock(ITypes.class);
        mockSec = mock(SecuritySystem.class);
        mockEc = mock(EventContext.class);
        mockProcessor = mock(Processor.class);
        mockProcess = mock(Process.class);
        mockCallback = mock(ProcessCallback.class);
        manager = (SessionManager) mockManager.proxy();
        executor = (Executor) mockExecutor.proxy();
        iQuery = (LocalQuery) mockQuery.proxy();
        iTypes = (ITypes) mockTypes.proxy();
        iUpdate = (IUpdate) mockUpdate.proxy();
        sec = (SecuritySystem) mockSec.proxy();
        processor = (Processor) mockProcessor.proxy();
        process = (Process) mockProcess.proxy();
        ec = (EventContext) mockEc.proxy();
        org.jmock.core.Stub stub = new org.jmock.core.stub.DefaultResultStub();
        mockEc.setDefaultStub(stub);
        mockProcessor.setDefaultStub(stub);

        pm = new ProcessManager(manager, sec, executor, processor);

    }

    void cleanup() throws Exception {
        try {
            verify();
        } finally {
            mockProcessor = null;
            tearDown();
        }
    }

    @Test
    public void testProcess() throws Exception {

        // This method should simply give each processor the chance to
        // consume a process
        willGetSubmitted();
        List<? extends Job> list = Arrays.asList(new ImportJob(1L, true));
        mockQuery.expects(once()).method("findAllByQuery").will(
                returnValue(list));
        mockProcessor.expects(once()).method("process").will(
                returnValue(process));
        pm.run();
    }

    @Test
    public void testStartProcessSuccess() throws Exception {

        long id = 1L;

        // on start process the process id should be passed to each processor
        mockProcessor.expects(once()).method("process").with(eq(id)).will(
                returnValue(process));

        // After run, startProcess will register a Process a turn over the
        // lifecycle to the processor
        pm.run();
        Process p = pm.runningProcess(id);
        assertEquals(p, process);
    }

    @Test
    public void testStartProcessFailure() throws Exception {

        // if no processor accepts the job, then it will be marked by the mgr
        mockProcessor.expects(once()).method("process").will(returnValue(null));

        // Then the manager will load the job to set it's state
        ImportJob job = new ImportJob();
        mockQuery.expects(once()).method("find").will(returnValue(job));
        mockUpdate.expects(once()).method("saveObject");

    }

    @Test
    public void testRunningProcess() throws Exception {

        List<? extends Job> list = Arrays.asList(new ImportJob(1L, true));
        mockQuery.expects(once()).method("findAllByQuery").will(
                returnValue(list));
        mockProcessor.expects(once()).method("process").will(
                returnValue(process));
        willGetSubmitted();
        pm.run();

        Process p = pm.runningProcess(1L);
        assertTrue(p != null);
    }

    // =========================================================================

    private void willGetSubmitted() {
        mockTypes.expects(once()).method("getEnumeration").will(
                returnValue(new JobStatus("Submitted")));
    }

}
