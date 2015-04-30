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
         * Raw file gateway which provides access to the OMERO file repository.
         *
         * Note: methods on this service are protected by a "DOWNLOAD" restriction.
         *
         * See also <a href="http://downloads.openmicroscopy.org/latest/omero5.1/api/ome/api/RawFileStore.html">RawFileStore.html</a>
         **/
        ["ami", "amd"] interface RawFileStore extends StatefulServiceInterface
            {

                /**
                 * This method manages the state of the service. This method
                 * will throw a [omero::SecurityViolation] if for the current user
                 * context either the file is not readable or a
                 * [omero::constants::permissions:DOWNLOAD] restriction is in
                 * place.
                 */
                void setFileId(long fileId) throws ServerError;

                /**
                 * Returns the current file id or null if none has been set.
                 */
                idempotent omero::RLong getFileId() throws ServerError;

                idempotent Ice::ByteSeq read(long position, int length) throws ServerError;
                idempotent long size() throws ServerError;
                idempotent bool truncate(long length) throws ServerError;
                idempotent void write(Ice::ByteSeq buf, long position, int length) throws ServerError;
                idempotent bool exists() throws ServerError;
                idempotent omero::model::OriginalFile save() throws ServerError;

            };
    };
};

#endif
