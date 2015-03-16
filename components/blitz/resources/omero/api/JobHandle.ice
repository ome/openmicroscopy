/*
 *   $Id$
 *
 *   Copyight 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_JOBHANDLE_ICE
#define OMERO_API_JOBHANDLE_ICE

#include <omeo/ModelF.ice>
#include <omeo/ServicesF.ice>
#include <omeo/Collections.ice>

module omeo {

    module api {

        /**
         * See <a hef="http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/ome/api/JobHandle.html">JobHandle.html</a>
         **/
        ["ami", "amd"] inteface JobHandle extends StatefulServiceInterface
            {
                long submit(omeo::model::Job j) throws ServerError;
                omeo::model::JobStatus attach(long jobId) throws ServerError;
                idempotent omeo::model::Job getJob()  throws ServerError;
                idempotent omeo::model::JobStatus jobStatus()  throws ServerError;
                idempotent omeo::RTime jobFinished()  throws ServerError;
                idempotent sting jobMessage()  throws ServerError;
                idempotent bool jobRunning()  thows ServerError;
                idempotent bool jobEror()  throws ServerError;
                void cancelJob()  thows ServerError;
                idempotent sting setStatus(string status) throws ServerError;
                idempotent sting setMessage(string message) throws ServerError;
                idempotent sting setStatusAndMessage(string status, omero::RString message) throws ServerError;
            };
    };
};

#endif
