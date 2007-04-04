/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.api;

import java.sql.Timestamp;

import ome.model.jobs.Job;
import ome.model.jobs.JobStatus;

/**
 * Allows submission of asynchronous jobs.
 * <p>
 * NOTE: The calling order for the service is as follows:
 * <ol>
 * <li>submit({@link Job}) <em>or</em> attach(long)</li>
 * <li>any of the other methods</li>
 * <li>close()</li>
 * </ol>
 * </p>
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
public interface JobHandle extends StatefulServiceInterface {

    long submit(Job job);

    /**
     * @return null if no {@link Job} with the given id was 
     *         found, otherwise the current {@link JobStatus}
     */
    JobStatus attach(long jobId);

    /**
     * @return the current {@link JobStatus}. Will never
     *         return null.
     */
    JobStatus jobStatus();

    /**
     * @return null if the {@link Job} is not finished,
     *         otherwise the {@link Timestamp} for when
     *         it completted.
     */
    Timestamp jobFinished();

    /**
     * @return current message for job. May be set during
     *         processing.
     */
    String  jobMessage();

    boolean jobRunning();
    boolean jobError();

    void cancelJob();
    void close();

}
