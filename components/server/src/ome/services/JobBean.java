/*
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services;

import java.sql.Timestamp;

import ome.annotations.RolesAllowed;
import ome.api.ITypes;
import ome.api.JobHandle;
import ome.api.ServiceInterface;
import ome.conditions.ApiUsageException;
import ome.conditions.ValidationException;
import ome.model.IObject;
import ome.model.internal.Details;
import ome.model.jobs.Job;
import ome.model.jobs.JobStatus;
import ome.parameters.Parameters;
import ome.security.SecureAction;
import ome.services.procs.IProcessManager;
import ome.services.procs.Process;
import ome.services.procs.ProcessCallback;
import ome.system.EventContext;
import ome.util.ShallowCopy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * Provides methods for submitting asynchronous tasks.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 * 
 */
@Transactional(readOnly = true)
public class JobBean extends AbstractStatefulBean implements JobHandle,
        ProcessCallback {
    /**
     * 
     */
    private static final long serialVersionUID = 49809384038000069L;

    /** The logger for this class. */
    private transient static Logger log = LoggerFactory.getLogger(JobBean.class);

    private Long jobId, resetId;
    private transient ITypes iTypes;
    private transient IProcessManager pm;

    /** default constructor */
    public JobBean() {
    }

    public Class<? extends ServiceInterface> getServiceInterface() {
        return JobHandle.class;
    }

    // Lifecycle methods
    // ===================================================

    /**
     * Does nothing. The only non-shared state that this instance
     * holds on to is the jobId and resetId -- two longs -- making
     * passivation for the moment unimportant. This method should do
     * what errorIfInvalidState does and reattach the process if we've
     * been passivated. That will wait for larger changes later. At
     * which time, proper locking will be necessary!
     */
    @RolesAllowed("user")
    @Transactional
    public void passivate() {
	// Nothing necessary
    }

    /**
     * Does almost nothing. Since nothing is passivated, nothing needs
     * to be activated. However, since we are still using
     * errorIfInvalidState, if the {@link #jobId} is non-null, then
     * this instance will need to handle re-loading on first
     * access. (Previously it could not be done here, because the
     * security system was not configured for transactions during
     * JavaEE callbacks. This is no longer true.)
     */
    @RolesAllowed("user")
    @Transactional
    public void activate() {
        if (jobId != null) {
            resetId = jobId;
            jobId = null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see ome.api.StatefulServiceInterface#close()
     */
    @RolesAllowed("user")
    @Transactional(readOnly = true)
    public void close() {
        // id is the only thing passivated.
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
        newJob.setStarted(null);
        newJob.setFinished(null);
        newJob.setSubmitted(now);

        // Values that the user can optionally set
        Timestamp t = newJob.getScheduledFor();
        if (t == null || t.getTime() < now.getTime()) {
            newJob.setScheduledFor(now);
        }
        JobStatus s = newJob.getStatus();
        if (s == null) {
            newJob.setStatus(new JobStatus(JobHandle.SUBMITTED));
        } else {
            // Verifying the status
            if (s.getId() != null) {
                try {
                    s = iQuery.get(JobStatus.class, s.getId());
                } catch (Exception e) {
                    throw new ApiUsageException("Unknown job status: " + s);
                }
            }

            if (s.getValue() == null) {
                throw new ApiUsageException(
                        "JobStatus must have id or value set.");
            } else {
                if (!(s.getValue().equals(SUBMITTED) || s.getValue().equals(
                        WAITING))) {
                    throw new ApiUsageException(
                            "Currently only SUBMITTED and WAITING are accepted as JobStatus");
                }
            }
        }
        String m = newJob.getMessage();
        if (m == null) {
            newJob.setMessage("");
        }

        // Here it is necessary to perform a {@link SecureAction} since
        // SecuritySystem#isSystemType() returns true for all Jobs
        newJob.getDetails().copy(sec.newTransientDetails(newJob));
        newJob = secureSave(newJob);

        jobId = newJob.getId();

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
     * Process Manager Bean injector.
     * 
     * @param procMgr
     *            a <code>ProcessManager</code>.
     */
    public void setProcessManager(IProcessManager procMgr) {
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
        Process p = pm.runningProcess(jobId);
        if (p != null && p.isActive()) {
            p.registerCallback(this);
        }
    }

    @RolesAllowed("user")
    public Job getJob() {

        Job job = internalJobOnly();

        JobStatus status = job.getStatus();
        Details unloadedDetails = status.getDetails().shallowCopy();
        status.getDetails().shallowCopy(unloadedDetails);

        Job copy = new ShallowCopy().copy(job);
        copy.setStatus(job.getStatus());

        return copy;
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
        return JobHandle.RUNNING.equals(getJob().getStatus().getValue());
    }

    @RolesAllowed("user")
    public boolean jobError() {
        return JobHandle.ERROR.equals(getJob().getStatus().getValue());
    }

    @Transactional(readOnly = false)
    @RolesAllowed("user")
    public void cancelJob() {
        setStatus(JobHandle.CANCELLED);
    }

    @Transactional(readOnly = false)
    @RolesAllowed("user")
    public String setStatus(String status) {
        return setStatusAndMessage(status, null);
    }

    @Transactional(readOnly = false)
    @RolesAllowed("user")
    public String setMessage(String message) {
        return setStatusAndMessage(null, message);
    }

    @Transactional(readOnly = false)
    @RolesAllowed("user")
    public String setStatusAndMessage(String status, String message) {

        // status can only be null if this is invoked locally, otherwise
        // the @NotNull will prevent that. therefore the return value
        // will only be the message when called by setMessage()
        String rv = null;

        errorIfInvalidState();
        Job job = internalJobOnly();
        if (message != null) {
            rv = job.getMessage();
            job.setMessage(message);
        }
        if (status != null) {
            JobStatus s = iTypes.getEnumeration(JobStatus.class, status);
            rv = job.getStatus().getValue();
            job.setStatus(s);
            Timestamp t = new Timestamp(System.currentTimeMillis());
            if (status.equals(RUNNING)) {
                job.setStarted(t);
            } else if (status.equals(CANCELLED)) {
                job.setFinished(t);
                Process p = pm.runningProcess(jobId);
                if (p != null) {
                    p.cancel();
                }
            } else if (status.equals(FINISHED) || status.equals(ERROR)) {
                job.setFinished(t);
            } else {
                throw new ValidationException("Currently unsupported: " + status);
            }
        }
        secureSave(job);
        return rv;
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

    private Job internalJobOnly() {
        errorIfInvalidState();
        checkAndRegister();
        Job job = iQuery.findByQuery("select j from Job j "
                + "left outer join fetch j.status status "
                + "left outer join fetch j.originalFileLinks links "
                + "left outer join fetch links.child file "
                + "left outer join fetch j.details.owner owner "
                + "left outer join fetch owner.groupExperimenterMap map "
                + "left outer join fetch map.parent where j.id = :id",
                new Parameters().addId(jobId));
        if (job == null) {
            throw new ApiUsageException("Unknown job:" + jobId);
        }
        return job;
    }

    private Job secureSave(Job job) {
        job = sec.doAction(new SecureAction() {
            public <T extends IObject> T updateObject(T... objs) {
                T result = iUpdate.saveAndReturnObject(objs[0]);
                iUpdate.flush(); // was commit
                return result;
            }
        }, job);
        return job;
    }

}
