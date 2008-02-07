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

import ome.api.ITypes;
import ome.api.JobHandle;
import ome.api.ServiceInterface;
import ome.api.local.LocalUpdate;
import ome.conditions.ApiUsageException;
import ome.model.IObject;
import ome.model.jobs.Job;
import ome.model.jobs.JobStatus;
import ome.security.SecureAction;
import ome.services.procs.IProcessManager;
import ome.services.procs.Process;
import ome.services.procs.ProcessCallback;
import ome.services.util.OmeroAroundInvoke;
import ome.system.EventContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.springframework.transaction.annotation.Transactional;

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
public class JobBean extends AbstractStatefulBean implements JobHandle,
        ProcessCallback {
    /**
     * 
     */
    private static final long serialVersionUID = 49809384038000069L;

    /** The logger for this class. */
    private transient static Log log = LogFactory.getLog(JobBean.class);

    private Long jobId, resetId;
    private transient ITypes iTypes;
    private transient LocalUpdate iUpdate;
    private transient IProcessManager pm;
    private transient JobNotification notification;

    /** default constructor */
    public JobBean() {
    }

    public Class<? extends ServiceInterface> getServiceInterface() {
        return JobHandle.class;
    }

    // Lifecycle methods
    // ===================================================

    /**
     * Configures a new or re-activated {@link JobBean}. If the {@link #jobId}
     * is non-null, then this instance will need to handle re-loading on first
     * access. (It cannot be done here, because the security system is not
     * configured.)
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
    public long submit(Job newJob) {
        reset(); // TODO or do we want to just checkState
        // and throw an exception if this is a stale handle.

        EventContext ec = getCurrentEventContext();
        long ms = System.currentTimeMillis();
        Timestamp now = new Timestamp(ms);

        // Values that can't be set by the user
        newJob.setUsername(ec.getCurrentUserName());
        newJob.setGroupname(ec.getCurrentGroupName());
        newJob.setType(ec.getCurrentEventType());
        newJob.setMessage(""); // TODO Should the user be able to set a message
        // as
        // a
        // check??
        newJob.setStatus(new JobStatus(JobHandle.SUBMITTED));
        newJob.setStarted(null);
        newJob.setFinished(null);
        newJob.setSubmitted(now);

        // Values that the user can optionally set
        Timestamp t = newJob.getScheduledFor();
        if (t == null || t.getTime() < now.getTime()) {
            newJob.setScheduledFor(now);
        }

        // Here it is necessary to perform a {@link SecureAction} since
        // SecuritySystem#isSystemType() returns true for all Jobs
        newJob.getDetails().copy(sec.newTransientDetails(newJob));
        newJob = secureSave(newJob);

        jobId = newJob.getId();
        notification.notice(jobId);

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
        }
        checkAndRegister();
        return getJob().getStatus();
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
    public void setUpdateService(LocalUpdate updateService) {
        getBeanHelper().throwIfAlreadySet(this.iUpdate, updateService);
        this.iUpdate = updateService;
    }

    /**
     * Process Manager Bean injector.
     * 
     * @param processManager
     *            a <code>ProcessManager</code>.
     */
    public void setProcessManager(IProcessManager procMgr) {
        getBeanHelper().throwIfAlreadySet(this.pm, procMgr);
        this.pm = procMgr;
    }

    /**
     * job notification injector.
     */
    public void setJobNotification(JobNotification notify) {
        getBeanHelper().throwIfAlreadySet(this.notification, notify);
        this.notification = notify;
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
        Process p = pm.runningProcess(jobId);
        if (p != null && p.isActive()) {
            p.registerCallback(this);
        }
    }

    @RolesAllowed("user")
    public Job getJob() {
        errorIfInvalidState();
        checkAndRegister();
        Job job = iQuery.find(Job.class, jobId);
        if (job == null) {
            throw new ApiUsageException("Unknown job:" + jobId);
        }
        return job;
    }

    @RolesAllowed("user")
    public Timestamp jobFinished() {
        return getJob().getFinished();
    }

    @RolesAllowed("user")
    public JobStatus jobStatus() {
        return getJob().getStatus();
    }

    @RolesAllowed("user")
    public String jobMessage() {
        return getJob().getMessage();
    }

    @RolesAllowed("user")
    public boolean jobRunning() {
        return getJob().getStatus().getValue().equals("Running"); // FIXME
    }

    @RolesAllowed("user")
    public boolean jobError() {
        return getJob().getStatus().getValue().equals("Error"); // FIXME
    }

    @Transactional(readOnly = false)
    @RolesAllowed("user")
    public void cancelJob() {
        errorIfInvalidState();
        Job job = getJob();
        job.setStatus(new JobStatus(JobHandle.CANCELLED));
        secureSave(job);
        Process p = pm.runningProcess(jobId);
        if (p != null) {
            p.cancel();
        }
    }

    // ProcessCallback ~
    // =========================================================================

    public void processCancelled(Process proc) {
        throw new UnsupportedOperationException("NYI");
    }

    public void processFinished(Process proc) {
        throw new UnsupportedOperationException("NYI");
    }

    // Helpers ~
    // =========================================================================

    private Job secureSave(Job job) {
        job = sec.doAction(new SecureAction() {
            public <T extends IObject> T updateObject(T... objs) {
                T result = iUpdate.saveAndReturnObject(objs[0]);
                iUpdate.commit();
                return result;
            }
        }, job);
        return job;
    }

}
