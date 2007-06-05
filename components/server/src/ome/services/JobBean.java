/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services;

import java.sql.Timestamp;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.Remote;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;

import org.springframework.transaction.annotation.Transactional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.security.SecurityDomain;

import ome.api.ITypes;
import ome.api.IUpdate;
import ome.api.JobHandle;
import ome.api.ServiceInterface;
import ome.conditions.ApiUsageException;
import ome.logic.AbstractLevel2Service;
import ome.logic.SimpleLifecycle;
import ome.model.jobs.Job;
import ome.model.jobs.JobStatus;
import ome.services.procs.Process;
import ome.services.procs.ProcessManager;
import ome.services.util.OmeroAroundInvoke;
import ome.system.EventContext;
import ome.system.SimpleEventContext;

/**
 * Provides methods for submitting asynchronous tasks.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 * 
 */
@TransactionManagement(TransactionManagementType.BEAN)
@Transactional(readOnly = true)
@Stateful
@Remote(JobHandle.class)
@RemoteBinding(jndiBinding = "omero/remote/ome.api.JobHandle")
@Local(JobHandle.class)
@LocalBinding(jndiBinding = "omero/local/ome.api.JobHandle")
@Interceptors( { OmeroAroundInvoke.class })
@SecurityDomain("OmeroSecurity")
public class JobBean extends AbstractStatefulBean implements
        JobHandle {
    /**
     * 
     */
    private static final long serialVersionUID = 49809384038000069L;

    /** The logger for this class. */
    private transient static Log log = LogFactory.getLog(JobBean.class);

    private Long jobId, resetId;
    private Job job;
    private transient Boolean   finished;
    private transient String    message;
    private transient JobStatus status;
    private transient ITypes    iTypes;
    private transient IUpdate   iUpdate;
    private transient ProcessManager pm;

    /** default constructor */
    public JobBean() {}
    
    public Class<? extends ServiceInterface> getServiceInterface() {
        return JobHandle.class;
    }

    // Lifecycle methods
    // ===================================================

    /**
     * Configures a new or re-activated {@link JobBean}. If the 
     * {@link #jobId} is non-null, then this instance will need to handle
     * re-loading on first access. (It cannot be done here, because
     * the security system is not configured.)
     */
    @PostConstruct
    @PostActivate
    public void create() {
        selfConfigure();
        if (jobId != null) {
            resetId = jobId;
            jobId = null;
        }
    }

    @PrePassivate
    @PreDestroy
    public void destroy() {
        // id is the only thing passivated.
    }

    /*
     * (non-Javadoc)
     * 
     * @see ome.api.StatefulServiceInterface#close()
     */
    @Remove
    public void close() {
	// FIXME do we need to check on the process here?
	// or callbacks? probably.
    }

    /*
     * (non-Javadoc)
     * 
     * @see ome.api.JobHandle#submit(Job)
     */
    @Transactional(readOnly = false)
    @RolesAllowed("user")
    public long submit(Job job) {
	reset(); // or do we want to just checkState
	// and throw an exception if this is a stale handle.

	EventContext ec = getCurrentEventContext();
	long ms = System.currentTimeMillis();
	Timestamp now = new Timestamp(ms);

	// Values that can't be set by the user
	job.setUsername(ec.getCurrentUserName());
	job.setGroupname(ec.getCurrentGroupName());
	job.setType(ec.getCurrentEventType());
	job.setMessage(""); // TODO Should the user be able to set a message as a check??
	job.setStatus(new JobStatus("Submitted"));
	job.setStarted(null);
	job.setFinished(null);
	job.setSubmitted(now);

	// Values that the user can optionally set
	Timestamp t = job.getScheduledFor();
	if (t == null || t.getTime() < now.getTime()) {
	    job.setScheduledFor(now);
	}

	job = iUpdate.saveAndReturnObject(job);
	jobId = job.getId();

        return jobId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ome.api.JobHandle#attach(long)
     */
    @RolesAllowed("user")
    public JobStatus attach(long id) {
        if (jobId == null || jobId.longValue() != id) {
            reset();
	    jobId = Long.valueOf(id);
	    job = iQuery.get(Job.class, id);

            if (job == null) {
		return null;
            }

        }
	checkAndRegister();
        return job.getStatus();
    }

    private void reset() {
	resetId = null;
	jobId = null;
    }
			  
    /**
     * Types service Bean injector.
     * 
     * @param typesService
     *            an <code>ITypes</code>.
     */
    public void setTypesService(ITypes typesService) {
        getBeanHelper().throwIfAlreadySet(this.iTypes, typesService);
	this.iTypes = typesService;
    }

    /**
     * Update service Bean injector.
     * 
     * @param updateService
     *            a <code>IUpdate</code>.
     */
    public void setUpdateService(IUpdate updateService) {
        getBeanHelper().throwIfAlreadySet(this.iUpdate, updateService);
	this.iUpdate = updateService;
    }

    /**
     * Process Manager Bean injector.
     * 
     * @param processManager
     *            a <code>ProcessManager</code>.
     */
    public void setProcessManager(ProcessManager procMgr) {
        getBeanHelper().throwIfAlreadySet(this.pm, procMgr);
	this.pm = procMgr;
    }

    // Usage methods
    // ===================================================


    protected void errorIfInvalidState() {
        if (resetId != null) {
            long reset = resetId.longValue();
            attach(reset);
        } else if (jobId == null) {
            throw new ApiUsageException(
                    "JobHandle not ready: Please submit() or attach() to a Job.");
        }
    }

    protected void checkAndRegister() {

	if (job == null) {
	    Job job = iQuery.get(Job.class, jobId);
	    if (job == null) {
		throw new ApiUsageException("Unknown job:"+jobId);
	    }
	}

	Process p = pm.runningProcess(jobId);
	if (p != null ) {
	    p.registerCallback(null);
	}
    }
    
    @RolesAllowed("user")
    public Timestamp jobFinished() {
	errorIfInvalidState();
	checkAndRegister();
	return job.getFinished();
    }

    @RolesAllowed("user")
    public JobStatus jobStatus() {
	errorIfInvalidState();
	checkAndRegister();
	return job.getStatus();
    }

    @RolesAllowed("user")
    public String jobMessage() {
	errorIfInvalidState();
	checkAndRegister();
	return job.getMessage();
    }

    @RolesAllowed("user")
    public boolean jobRunning() {
	errorIfInvalidState();
	checkAndRegister();
	return job.getStatus().equals("Running"); // FIXME
    }

    @RolesAllowed("user")
    public boolean jobError() {
	errorIfInvalidState();
	checkAndRegister();
	return job.getStatus().getValue().equals("Error"); // FIXME
    }

    @Transactional(readOnly = false)
    @RolesAllowed("user")
    public void cancelJob() {
	errorIfInvalidState();
	checkAndRegister();
	throw new UnsupportedOperationException();
    }

}
