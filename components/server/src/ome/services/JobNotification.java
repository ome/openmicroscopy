/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services;

import ome.model.jobs.Job;
import ome.services.procs.ProcessManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;

/**
 * Temporary replacement for the central notification system that will be added
 * to OMERO. The intent of the central system will be that the creation of any
 * type (for example a {@link Job} will be sent to all registered listeners.
 * Once that is in place, this class and all its references can be removed.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 * 
 */
public class JobNotification {

    /** The logger for this class. */
    private static Log log = LogFactory.getLog(JobNotification.class);

    private Scheduler scheduler;

    /** default constructor */
    public JobNotification() {
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void notice(long jobId) {
        try {
            scheduler
                    .triggerJob("process-jobs-manual", Scheduler.DEFAULT_GROUP);
        } catch (Exception e) {
            log.error("Could not trigger process-jobs-manual", e);
        }

    }

    public static class Go implements org.quartz.Job {
        public void execute(JobExecutionContext arg0)
                throws JobExecutionException {
            ProcessManager pm = (ProcessManager) arg0.getJobDetail()
                    .getJobDataMap().get("processManager");
            pm.process();
        }
    }
}
