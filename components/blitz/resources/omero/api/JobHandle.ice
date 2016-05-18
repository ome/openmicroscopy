/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_JOBHANDLE_ICE
#define OMERO_API_JOBHANDLE_ICE

#include <omero/ModelF.ice>
#include <omero/ServicesF.ice>
#include <omero/Collections.ice>

module omero {

    module api {

        /**
         * Allows submission of asynchronous jobs.
         * <p>
         * NOTE: The calling order for the service is as follows:
         * <ol>
         * <li>{@link #submit} <em>or</em> {@link #attach}</li>
         * <li>any of the other methods</li>
         * <li>{@link #close}</li>
         * </ol>
         * </p>
         * Calling {@link #close} does not cancel or otherwise change
         * the Job state. See {@link #cancelJob}.
         **/
        ["ami", "amd"] interface JobHandle extends StatefulServiceInterface
            {
                /**
                 * Submits a {@link omero.model.Job} and returns its database
                 * id. The only fields directly on status which are editable
                 * are <em>message</em>, <em>scheduledFor</em> and
                 * <em>status</em>. The latter two must be sensible.
                 *
                 * @param job Not null
                 */
                long submit(omero::model::Job j) throws ServerError;

                /**
                 * Returns the current {@link omero.model.JobStatus} for the
                 * Job id.
                 *
                 * @throws ApiUsageException
                 *             if the {@link Job id} does not exist.
                 */
                omero::model::JobStatus attach(long jobId) throws ServerError;

                /**
                 * Returns the current {@link omero.model.Job}
                 */
                idempotent omero::model::Job getJob()  throws ServerError;

                /**
                 * Returns the current {@link omero.model.JobStatus}. Will
                 * never return null.
                 */
                idempotent omero::model::JobStatus jobStatus()  throws ServerError;

                /**
                 * Returns <code>null</code> if the {@link omero.model.Job} is
                 * not finished, otherwise the {@link omero.RTime} for when it
                 * completed.
                 */
                idempotent omero::RTime jobFinished()  throws ServerError;

                /**
                 * Returns the current message for job. May be set during
                 * processing.
                 */
                idempotent string jobMessage()  throws ServerError;

                /**
                 * Returns <code>true</code> if the {@link omero.model.Job} is
                 * running, I.e. has an attached process.
                 */
                idempotent bool jobRunning()  throws ServerError;

                /**
                 * Returns <code>true</code> if the {@link omero.model.Job}
                 * has thrown an error.
                 */
                idempotent bool jobError()  throws ServerError;

                /**
                 * Marks a job for cancellation. Not every processor will
                 * check for the cancelled flag for a running job, but no
                 * non-running job will start if it has been cancelled.
                 */
                void cancelJob()  throws ServerError;

                /**
                 * Updates the {@link omero.model.JobStatus} for the current
                 * job. The previous status is returned as a string. If the
                 * status is {@link #CANCELLED}, this method is equivalent to
                 * {@link #cancelJob}.
                 */
                idempotent string setStatus(string status) throws ServerError;

                /**
                 * Sets the job's message string, and returns the previous
                 * value.
                 *
                 * @return the previous message value
                 */
                idempotent string setMessage(string message) throws ServerError;

                /**
                 * Like {@link #setStatus} but also sets the message.
                 */
                idempotent string setStatusAndMessage(string status, omero::RString message) throws ServerError;
            };
    };
};

#endif
