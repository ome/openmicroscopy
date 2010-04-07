/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_RAWFILESTORE_ICE
#define OMERO_API_RAWFILESTORE_ICE

#include <omero/ModelF.ice>
#include <omero/ServicesF.ice>
#include <omero/Collections.ice>

module omero {

    module api {

        /**
         * See <a href="http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/ome/api/RawFileStore.html">RawFileStore.html</a>
         **/
        ["ami", "amd"] interface RawFileStore extends StatefulServiceInterface
            {
                void setFileId(long fileId) throws ServerError;
                idempotent Ice::ByteSeq read(long position, int length) throws ServerError;
                idempotent void write(Ice::ByteSeq buf, long position, int length) throws ServerError;
                idempotent bool exists() throws ServerError;
            };
    };
};

#endif
