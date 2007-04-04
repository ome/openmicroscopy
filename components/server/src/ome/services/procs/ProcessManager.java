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

import ome.api.IQuery;
import ome.api.ITypes;
import ome.api.IUpdate;
import ome.conditions.ApiUsageException;
import ome.model.jobs.Job;
import ome.model.jobs.JobStatus;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
public class ProcessManager {

    private static Log log = LogFactory.getLog(ProcessManager.class);

    private IQuery query;
    private ITypes types;
    private IUpdate update;

    /**
     * processors available for processing. This array will never be null.
     */
    protected List<Processor> processors;

    protected Map<Long,Process> procMap = Collections.synchronizedMap(new HashMap<Long,Process>());

    private ProcessManager() {
    }; // We need the processors

    /**
     * main constructor which takes a non-null array of {@link
     * Processor} instances as its only argument. This array is
     * copied, so modifications will not be noticed.
     * 
     * @param processors
     *            Array of Processors. Not null.
     */
    public ProcessManager(Processor...procs) {
        if (procs == null || procs.length == 0) {
            throw new ApiUsageException(
                    "Processor[] argument to ProcessManager constructor "
                            + "may not be null or empty.");
        }
	this.processors = Arrays.asList(procs);
    }

    public synchronized /*FIXME*/ void process() {

	if (log.isDebugEnabled()) {
	    log.debug("Starting processing...");
	}

	if (log.isDebugEnabled()) {
	    log.debug("Finished processing...");
	}

    }

    /**
     * 
     * @param <T>
     * @param queryID
     * @param params
     * @return
     */
    public Process startProcess(long jobId) {
        Process p = null;

        for (Processor proc : processors) {
            p = proc.process(jobId);
            if (p != null) {
                break;
            }
        }

        if (p == null) {
	    if (log.isInfoEnabled()) {
		log.info("No processor found for job:"+jobId);
	    }
	    Job job = job(jobId);
	    JobStatus waiting = types.getEnumeration(JobStatus.class, "Waiting");
	    job.setStatus(waiting);
	    job.setMessage("No processor found for job.");
	    update.saveObject(job);
        }

        return p;

    }

    public Process runningProcess(long jobId) {
	Process p = procMap.get(jobId);
	return p;
    }

    protected Job job(long id) {
	Job job = query.find(Job.class, id);
	return job;
    }

}
