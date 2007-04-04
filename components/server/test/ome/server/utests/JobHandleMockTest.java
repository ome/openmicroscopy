/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.utests;

// Java imports
import java.util.ArrayList;
import java.util.List;

// Third-party libraries
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.stub.DefaultResultStub;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.testng.annotations.*;

// Application-internal dependencies
import ome.api.IQuery;
import ome.api.ITypes;
import ome.api.IUpdate;
import ome.api.JobHandle;
import ome.api.local.LocalQuery;
import ome.conditions.ApiUsageException;
import ome.logic.QueryImpl;
import ome.model.IObject;
import ome.model.jobs.Job;
import ome.model.jobs.JobStatus;
import ome.model.jobs.ImportJob;
import ome.parameters.Filter;
import ome.security.SecuritySystem;
import ome.services.JobBean;
import ome.services.procs.Process;
import ome.services.procs.Processor;
import ome.services.procs.ProcessManager;
import ome.services.util.ServiceHandler;
import ome.system.EventContext;
import ome.tools.hibernate.SessionHandler;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
@Test( groups = "jobs" )
public class JobHandleMockTest extends MockObjectTestCase {

    protected JobHandle jh;

    protected LocalQuery iQuery;
    protected IUpdate iUpdate;
    protected ITypes iTypes;
    protected SecuritySystem sec;
    protected EventContext ec;
    protected Processor processor;
    protected Process process;

    protected Mock mockQuery, mockUpdate, mockTypes, mockSec, mockEc, mockProcessor, mockProcess;

    protected ProcessManager pm;

    @Override
    @Configuration(beforeTestMethod = true)
    protected void setUp() throws Exception {
        super.setUp();
	mockQuery = mock(LocalQuery.class);
	mockUpdate = mock(IUpdate.class);
	mockTypes = mock(ITypes.class);
	mockSec = mock(SecuritySystem.class);
	mockEc = mock(EventContext.class);
	mockProcessor = mock(Processor.class);
	mockProcess = mock(Process.class);
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

	pm = new ProcessManager(processor);

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
	    mockProcessor = null;
	    tearDown();
	}
    }

    @Test
    public void testSubmit() throws Exception {
	mockSec.expects(atLeastOnce()).method("getEventContext").will(returnValue(ec));
        mockUpdate.expects(once()).method("saveAndReturnObject").will(returnValue(new ImportJob(1L,true)));
	jh.submit(new ImportJob());
	cleanup();
    }

    @Test
    public void testAttachUnknown() throws Exception {
        mockQuery.expects(once()).method("get").will(returnValue(null));
	assertNull( jh.attach(1L) );
	cleanup();
    }

    @Test
    public void testAttachKnown() throws Exception {
	ImportJob job = jobWithStatus();
        mockQuery.expects(once()).method("get").will(returnValue(job));
	JobStatus rv = jh.attach(1L);
	assertTrue(rv == job.getStatus());
	cleanup();
    }

    @Test(groups = "broken")
    public void testRegisterCallback() throws Exception {
	ImportJob job = jobWithStatus();
        mockQuery.expects(once()).method("get").will(returnValue(job));
	mockProcessor.expects(once()).method("process").will(returnValue(process));
	mockProcess.expects(once()).method("registerCallback");
	jh.attach(1L);
	cleanup();
    }

    ImportJob jobWithStatus() {
	JobStatus status = new JobStatus("mock");
	ImportJob job = new ImportJob();
	job.setStatus(status);
	return job;
    }
}
