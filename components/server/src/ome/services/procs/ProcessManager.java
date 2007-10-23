/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.procs;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ome.api.IQuery;
import ome.api.ITypes;
import ome.api.IUpdate;
import ome.api.JobHandle;
import ome.conditions.ApiUsageException;
import ome.model.IObject;
import ome.model.jobs.Job;
import ome.model.jobs.JobStatus;
import ome.parameters.Parameters;
import ome.security.SecureAction;
import ome.security.SecuritySystem;
import ome.system.Principal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
public class ProcessManager implements IProcessManager {

    private static Log log = LogFactory.getLog(ProcessManager.class);

    // TODO this should probably be replaced by a DAO
    private SecuritySystem sec;
    private IQuery query;
    private ITypes types;
    private IUpdate update;

    /**
     * processors available for processing. This array will never be null.
     */
    protected List<Processor> processors;

    protected Map<Long, Process> procMap = Collections
            .synchronizedMap(new HashMap<Long, Process>());

    private ProcessManager() {
    }; // We need the processors

    /**
     * main constructor which takes a non-null array of {@link Processor}
     * instances as its only argument. This array is copied, so modifications
     * will not be noticed.
     * 
     * @param processors
     *            Array of Processors. Not null.
     */
    public ProcessManager(Processor... procs) {
        if (procs == null || procs.length == 0) {
            throw new ApiUsageException(
                    "Processor[] argument to ProcessManager constructor "
                            + "may not be null or empty.");
        }
        this.processors = Arrays.asList(procs);
    }

    public void setQueryService(IQuery queryService) {
        this.query = queryService;
    }

    public void setTypesService(ITypes typesService) {
        this.types = typesService;
    }

    public void setUpdateService(IUpdate updateService) {
        this.update = updateService;
    }

    public void setSecuritySystem(SecuritySystem securitySystem) {
        this.sec = securitySystem;
    }

    // Main methods ~
    // =========================================================================

    public void process() {

        if (log.isDebugEnabled()) {
            log.debug("Starting processing...");
        }

        try {
            sec.login(new Principal("root", "user", "Processing"));
            List<Job> jobs = query.findAllByQuery(
                    "select j from Job j where status.id = :id",
                    new Parameters().addId(getSubmittedStatus().getId()));

            for (Job job : jobs) {
                startProcess(job.getId());
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Error while processing", e);
            }
        } finally {
            sec.logout();
        }

        if (log.isDebugEnabled()) {
            log.debug("Finished processing...");
        }

    }

    /**
     * 
     */
    public void startProcess(long jobId) {
        Process p = null;

        for (Processor proc : processors) {
            p = proc.process(jobId);
            // Take first processor
            if (p != null) {
                break;
            }
        }

        if (p == null) {
            if (log.isWarnEnabled()) {
                log.warn("No processor found for job:" + jobId);
            }
            Job job = job(jobId);
            job.setStatus(getWaitingStatus());
            job.setMessage("No processor found for job.");
            sec.doAction(job, new SecureAction() {
                public <T extends IObject> T updateObject(T obj) {
                    return update.saveAndReturnObject(obj);
                }

            });
        } else {
            procMap.put(jobId, p);
        }

    }

    public Process runningProcess(long jobId) {
        Process p = procMap.get(jobId);
        return p;
    }

    // Helpers ~
    // =========================================================================

    protected Job job(long id) {
        Job job = query.find(Job.class, id);
        return job;
    }

    private JobStatus getStatus(String status) {
        JobStatus statusObj = types.getEnumeration(JobStatus.class, status);
        return statusObj;
    }

    private JobStatus getSubmittedStatus() {
        return getStatus(JobHandle.SUBMITTED);
    }

    private JobStatus getRunningStatus() {
        return getStatus(JobHandle.RUNNING);
    }

    private JobStatus getWaitingStatus() {
        return getStatus(JobHandle.WAITING);
    }

}
