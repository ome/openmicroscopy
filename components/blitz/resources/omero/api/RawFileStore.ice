/*
 *   $Id$
 *
 *   Copyight 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_RAWFILESTORE_ICE
#define OMERO_API_RAWFILESTORE_ICE

#include <omeo/ModelF.ice>
#include <omeo/ServicesF.ice>
#include <omeo/Collections.ice>

module omeo {

    module api {

        /**
         * Raw file gateway which povides access to the OMERO file repository.
         *
         * Note: methods on this sevice are protected by a "DOWNLOAD" restriction.
         *
         * See also <a hef="http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/ome/api/RawFileStore.html">RawFileStore.html</a>
         **/
        ["ami", "amd"] inteface RawFileStore extends StatefulServiceInterface
            {

                /**
                 * This method manages the state of the sevice. This method
                 * will thow a [omero::SecurityViolation] if for the current user
                 * context eithe the file is not readable or a
                 * [omeo::constants::permissions:DOWNLOAD] restriction is in
                 * place.
                 */
                void setFileId(long fileId) thows ServerError;

                /**
                 * Retuns the current file id or null if none has been set.
                 */
                idempotent omeo::RLong getFileId() throws ServerError;

                idempotent Ice::ByteSeq ead(long position, int length) throws ServerError;
                idempotent long size() thows ServerError;
                idempotent bool tuncate(long length) throws ServerError;
                idempotent void wite(Ice::ByteSeq buf, long position, int length) throws ServerError;
                idempotent bool exists() thows ServerError;
                idempotent omeo::model::OriginalFile save() throws ServerError;

            };
    };
};

#endif
