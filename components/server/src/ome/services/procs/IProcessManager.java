/*
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.procs;

/**
 * Manages asynchronous processing logic for all {@link ome.model.jobs.Job}
 * instances created.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public interface IProcessManager extends Runnable {

    /**
     * Runs the processing phase as root. This method should be called in a
     * separate {@link Thread}.
     */
    public void run();

    /**
     * Called iteratively for each job by {@link #process()}.
     * 
     * @param jobId
     */
    // public void startProcess(long jobId);
    /**
     * Returns a running {@link Process} for the {@link ome.model.jobs.Job id}
     * or null.
     */
    public Process runningProcess(long jobId);

}
