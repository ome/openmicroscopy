/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

import ome.api.JobHandle;
import ome.services.blitz.util.BlitzExecutor;
import omero.RString;
import omero.ServerError;
import omero.api.AMD_JobHandle_attach;
import omero.api.AMD_JobHandle_cancelJob;
import omero.api.AMD_JobHandle_getJob;
import omero.api.AMD_JobHandle_jobError;
import omero.api.AMD_JobHandle_jobFinished;
import omero.api.AMD_JobHandle_jobMessage;
import omero.api.AMD_JobHandle_jobRunning;
import omero.api.AMD_JobHandle_jobStatus;
import omero.api.AMD_JobHandle_setStatus;
import omero.api.AMD_JobHandle_setStatusAndMessage;
import omero.api.AMD_JobHandle_submit;
import omero.api._JobHandleOperations;
import omero.model.Job;
import Ice.Current;

/**
 * Implementation of the JobHandle service.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta4
 * @see ome.api.JobHandle
 */
public class JobHandleI extends AbstractCloseableAmdServant implements
        _JobHandleOperations {

    public JobHandleI(JobHandle service, BlitzExecutor be) {
        super(service, be);
    }

    // Interface methods
    // =========================================================================

    public void attach_async(AMD_JobHandle_attach __cb, long jobId,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, jobId);

    }

    public void cancelJob_async(AMD_JobHandle_cancelJob __cb, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current);

    }

    public void getJob_async(AMD_JobHandle_getJob __cb, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current);

    }

    public void jobError_async(AMD_JobHandle_jobError __cb, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current);

    }

    public void jobFinished_async(AMD_JobHandle_jobFinished __cb,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current);

    }

    public void jobMessage_async(AMD_JobHandle_jobMessage __cb,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current);

    }

    public void jobRunning_async(AMD_JobHandle_jobRunning __cb,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current);

    }

    public void jobStatus_async(AMD_JobHandle_jobStatus __cb, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current);

    }

    public void submit_async(AMD_JobHandle_submit __cb, Job j, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current, j);

    }

    public void setStatus_async(AMD_JobHandle_setStatus __cb, String status,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, status);
    }

    public void setStatusAndMessage_async(AMD_JobHandle_setStatusAndMessage __cb, String status,
            RString msg, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, status, msg);
    }

    public void setMessage_async(omero.api.AMD_JobHandle_setMessage __cb, String message,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, message);
    };

    @Override
    protected void preClose(Current current) throws Throwable {
        // no-op
    }

    @Override
    protected void postClose(Current current) {
        // no-op
    }
}
