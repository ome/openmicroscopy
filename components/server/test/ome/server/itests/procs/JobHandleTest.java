/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.procs;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.UUID;

import junit.framework.TestCase;
import ome.api.IQuery;
import ome.api.JobHandle;
import ome.conditions.SecurityViolation;
import ome.model.jobs.Job;
import ome.model.jobs.JobStatus;
import ome.model.jobs.ScriptJob;
import ome.security.SecuritySystem;
import ome.server.itests.ManagedContextFixture;
import ome.services.JobBean;
import ome.services.procs.ProcessManager;
import ome.services.procs.ProcessorSkeleton;
import ome.services.sessions.SessionManager;
import ome.services.util.Executor;
import ome.system.ServiceFactory;
import ome.util.ContextFilter;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
@Test(groups = { "jobs", "integration" })
public class JobHandleTest extends TestCase {

    protected ManagedContextFixture fixture;
    protected ServiceFactory sf;
    protected long id;
    protected JobHandle jh;
    protected PManager mgr;

    @Override
    @Configuration(beforeTestMethod = true)
    protected void setUp() throws Exception {
        super.setUp();
        fixture = new ManagedContextFixture();
        sf = fixture.sf;
        mgr = new PManager(new P(sf.getQueryService()), fixture.mgr,
                fixture.sec, fixture.ex);
        JobBean bean = (JobBean) fixture.ctx
                .getBean("internal:ome.api.JobHandle");
        Field pm = bean.getClass().getDeclaredField("pm");
        pm.setAccessible(true);
        pm.set(bean, mgr);
        // Fixing notifications
        Scheduler sched = (Scheduler) fixture.ctx.getBean("scheduler");
        JobDetail manual = (JobDetail) fixture.ctx.getBean("process-jobs-run");
        manual.getJobDataMap().put("processManager", mgr);
        sched.addJob(manual, true);
    }

    @Override
    @Configuration(afterTestMethod = true)
    protected void tearDown() throws Exception {
        super.tearDown();

    }

    @Test
    public void testJobIsSavedToDatabase() throws Exception {
        ScriptJob job = new ScriptJob();
        String uuid = UUID.randomUUID().toString();
        job.setMessage(uuid);
        jh = sf.createJobHandle();
        id = jh.submit(job);
        assertTrue(id > 0L);
    }

    @Test(expectedExceptions = SecurityViolation.class)
    public void testJobCannotBeCreatedViaIUpdate() throws Exception {
        ScriptJob job = new ScriptJob();
        job.setSubmitted(new Timestamp(System.currentTimeMillis()));
        job.setType("user");
        job.setGroupname("default");
        job.setMessage("test of override via iupdate");
        job.setStatus(new JobStatus("Submitted"));
        job.setUsername("root");
        job.setScheduledFor(new Timestamp(System.currentTimeMillis() + 100L));
        sf.getUpdateService().saveObject(job);
    }

    @Test
    public void testUserCanReattach() throws Exception {
        testJobIsSavedToDatabase();
        JobHandle attach = sf.createJobHandle();
        JobStatus status = attach.attach(id);
        assertTrue(status != null && status.getValue() != null);
    }

    @Test
    public void testUserCanRetrieveJob() throws Exception {
        testJobIsSavedToDatabase();
        Job job = jh.getJob();
        assertTrue(job != null && job.isLoaded());
    }

    @Test
    public void testMultipleUsersCanAttach() throws Exception {
        testJobIsSavedToDatabase();
        JobHandle attach1 = sf.createJobHandle();
        JobHandle attach2 = sf.createJobHandle();

        String user1 = fixture.getCurrentUser();
        String user2 = fixture.newUser();

        attach1.attach(id);

        fixture.setCurrentUser(user2);
        attach2.attach(id);
        attach2.cancelJob();

        fixture.setCurrentUser(user1);
        JobStatus cancelled = attach1.jobStatus();
        assertEquals(JobHandle.CANCELLED, cancelled.getValue());
    }

    @Test
    public void testProcessManagerIsNotified() throws Exception {
        testJobIsSavedToDatabase();
        Thread.sleep(1000L);
        assertTrue(mgr.called);
    }

    @Test
    public void testJobSwitchedToWaitingIfNoProcessorTakesIt() throws Exception {
        testJobIsSavedToDatabase();
        mgr.run();
        assertEquals(JobHandle.WAITING, jh.jobStatus().getValue());
    }

    @Test
    public void testPassiviationWorksProperly() throws Exception {
        fail("NYI");
    }

    @Test
    public void testCancelJobWorks() throws Exception {
        fail("NYI");
    }

    @Test
    public void testProcessCancelsJobWorks() throws Exception {
        fail("NYI");
    }

    @Test
    public void testServiceNotifiedOfServiceCompletionByProcess()
            throws Exception {
        fail("NYI");
    }

    @Test
    public void testUserForgetsToUploadScript() throws Exception {
        fail("NYI");
    }

    @Test
    public void testCheckAndRegisterNoticesFinishedProcesses() throws Exception {
        fail("NYI");
    }

    @Test
    public void testJobRetrievalCanBeSpecifiedViaQuery() throws Exception {
        fail("NYI");
        // Job job = iQuery.findByQuery("job.query."+job.getClass(),idParams);
    }

    @Test
    public void testWalkingThroughReturnedJob() throws Exception {
        testJobIsSavedToDatabase();
        Job job = jh.getJob();
        job.acceptFilter(new ContextFilter() {
        });
    }
}

class P extends ProcessorSkeleton {
    public P(IQuery query) {
        this.setQueryService(query);
    }

}

class PManager extends ProcessManager {
    public PManager(P p, SessionManager mgr, SecuritySystem sec, Executor ex) {
        super(mgr, sec, ex, p);
        if (sec == null || ex == null) {
            throw new RuntimeException("Null argument");
        }
    }

    public volatile boolean called = false;

    @Override
    public void doRun() {
        called = true;
        super.doRun();
    }

    public void waitFor(long timeout) {
        long stop = System.currentTimeMillis() + timeout;
        while (System.currentTimeMillis() < stop) {
            try {
                Thread.sleep(500L);
            } catch (InterruptedException ie) {
                // ok
            }
        }
    }
}
