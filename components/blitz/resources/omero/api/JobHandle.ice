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
         * See <a href="http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/ome/api/JobHandle.html">JobHandle.html</a>
         **/
        ["ami", "amd"] interface JobHandle extends StatefulServiceInterface
            {
                long submit(omero::model::Job j) throws ServerError;
                omero::model::JobStatus attach(long jobId) throws ServerError;
                idempotent omero::model::Job getJob()  throws ServerError;
                idempotent omero::model::JobStatus jobStatus()  throws ServerError;
                idempotent omero::RTime jobFinished()  throws ServerError;
                idempotent string jobMessage()  throws ServerError;
                idempotent bool jobRunning()  throws ServerError;
                idempotent bool jobError()  throws ServerError;
                void cancelJob()  throws ServerError;
                idempotent string setStatus(string status) throws ServerError;
                idempotent string setMessage(string message) throws ServerError;
                idempotent string setStatusAndMessage(string status, omero::RString message) throws ServerError;
            };
    };
};

#endif
