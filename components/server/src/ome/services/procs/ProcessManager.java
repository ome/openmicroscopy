/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.procs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ome.api.JobHandle;
import ome.model.IObject;
import ome.model.jobs.Job;
import ome.model.jobs.JobStatus;
import ome.parameters.Parameters;
import ome.security.SecureAction;
import ome.security.SecuritySystem;
import ome.services.sessions.SessionManager;
import ome.services.util.ExecutionThread;
import ome.services.util.Executor;
import ome.system.Principal;
import ome.system.ServiceFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Session;

/**
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
public class ProcessManager extends ExecutionThread implements IProcessManager {

    /**
     * Task performed by the {@link ProcessManager} on each invocation of
     * {@link ProcessManager#run()}.
     */
    public static class Work implements Executor.Work {

        /**
         * processors available for processing. This array will never be null.
         */
        final protected List<Processor> processors;

        /**
         * {@link SecuritySystem} in order to perform a secure save on the
         * {@link Job} instance.
         */
        final protected SecuritySystem sec;

        /**
         * Map of all active {@link Process processes}.
         */
        protected Map<Long, Process> procMap = Collections
                .synchronizedMap(new HashMap<Long, Process>());

        public Work(SecuritySystem sec, Processor... procs) {
            this.sec = sec;
            if (procs == null) {
                this.processors = new ArrayList<Processor>();
            } else {
                this.processors = Arrays.asList(procs);
            }
        }

        public String description() {
            return "ProcessManager";
        }
        
        public List<Job> doWork(Session session, ServiceFactory sf) {
            final List<Job> jobs = sf.getQueryService().findAllByQuery(
                    "select j from Job j where status.id = :id",
                    new Parameters().addId(getSubmittedStatus(sf).getId()));

            for (Job job : jobs) {
                startProcess(sf, job.getId());
            }

            return null;
        }

        /**
         * 
         */
        public void startProcess(final ServiceFactory sf, final long jobId) {
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

                Job job = job(sf, jobId);
                job.setStatus(getWaitingStatus(sf));
                job.setMessage("No processor found for job.");
                sec.doAction(new SecureAction() {
                    public <T extends IObject> T updateObject(T... objs) {
                        return sf.getUpdateService().saveAndReturnObject(
                                objs[0]);
                    }

                }, job);
            } else {
                procMap.put(jobId, p);
            }

        }

        // Helpers ~
        // =========================================================================

        protected Job job(ServiceFactory sf, long id) {
            Job job = sf.getQueryService().find(Job.class, id);
            return job;
        }

        private JobStatus getStatus(ServiceFactory sf, String status) {
            JobStatus statusObj = sf.getTypesService().getEnumeration(
                    JobStatus.class, status);
            return statusObj;
        }

        private JobStatus getSubmittedStatus(ServiceFactory sf) {
            return getStatus(sf, JobHandle.SUBMITTED);
        }

        private JobStatus getWaitingStatus(ServiceFactory sf) {
            return getStatus(sf, JobHandle.WAITING);
        }
    } // End Work

    //
    // ProcessManager
    //

    private static Logger log = LoggerFactory.getLogger(ProcessManager.class);

    private static Principal PRINCIPAL = new Principal("root", "user",
            "Processing");

    /**
     * main constructor which takes a non-null array of {@link Processor}
     * instances as its only argument. This array is copied, so modifications
     * will not be noticed.
     *
     * @param manager
     *              Reference to the session manager.
     * @param sec
     *          Reference to the security manager
     * @param executor
     *              The executor.
     * @param procs
     *            Array of Processors. Not null.
     */
    public ProcessManager(SessionManager manager, SecuritySystem sec,
            Executor executor, Processor... procs) {
        super(manager, executor, new Work(sec, procs), PRINCIPAL);
    }

    // Main methods ~
    // =========================================================================

    @Override
    @SuppressWarnings("unchecked")
    public void doRun() {

        if (log.isDebugEnabled()) {
            log.debug("Starting processing...");
        }

        try {

            this.executor.execute(getPrincipal(), this.work);

        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Error while processing", e);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Finished processing...");
        }

    }

    public Process runningProcess(long jobId) {
        Process p = ((Work) work).procMap.get(jobId);
        return p;
    }

}
