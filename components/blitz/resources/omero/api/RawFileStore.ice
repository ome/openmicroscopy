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
         * Note: methods on this service are protected by a ""DOWNLOAD""
         * restriction.
         **/
        ["ami", "amd"] interface RawFileStore extends StatefulServiceInterface
            {

                /**
                 * This method manages the state of the service. This method
                 * will throw a {@link omero.SecurityViolation} if for the
                 * current user context either the file is not readable or a
                 * {@link omero.constants.permissions#DOWNLOAD} restriction is
                 * in place.
                 */
                void setFileId(long fileId) throws ServerError;

                /**
                 * Returns the current file id or null if none has been set.
                 */
                idempotent omero::RLong getFileId() throws ServerError;

                /**
                 * Reads <code>length</code> bytes of data at the
                 * <code>position</code> from the raw file into an array of
                 * bytes.
                 */
                idempotent Ice::ByteSeq read(long position, int length) throws ServerError;

                /**
                 * Returns the size of the file on disk (not as stored in the
                 * database since that value will only be updated on
                 * {@link #save}. If the file has not yet been written to, and
                 * therefore does not exist, a {@link omero.ResourceError}
                 * will be thrown.
                 */
                idempotent long size() throws ServerError;

                /**
                 * Limits the size of a file to the given length. If the file
                 * is already shorter than length, no action is taken in which
                 * case false is returned.
                 */
                idempotent bool truncate(long length) throws ServerError;

                /**
                 * Writes <code>length</code> bytes of data from the specified
                 * <code>buf</code> byte array starting at at
                 * <code>position</code> to the raw file.
                 */
                idempotent void write(Ice::ByteSeq buf, long position, int length) throws ServerError;

                /**
                 * Checks to see if a raw file exists with the file ID that
                 * the service was initialized with.
                 *
                 * @return <code>true</code> if there is an accessible file
                 *         within the original file repository with the
                 *         correct ID. Otherwise <code>false</code>.
                 * @throws ResourceError if there is a problem accessing the
                 *         file due to permissions errors within the
                 *         repository or any other I/O error.
                 */
                idempotent bool exists() throws ServerError;

                /**
                 * Saves the {@link omero.model.OriginalFile} associated with
                 * the service if it has been modified. The returned valued
                 * should replace all instances of the
                 * {@link omero.model.OriginalFile} in the client.
                 *
                 * If save has not been called, {@link omero.api.RawFileStore}
                 * instances will save the {@link omero.model.OriginalFile}
                 * object associated with it on {@link #close}.
                 *
                 * See also <a href="https://trac.openmicroscopy.org/ome/ticket/1651">ticket 1651</a>
                 * and <a href="https://trac.openmicroscopy.org/ome/ticket/2161">ticket 2161</a>.
                 */
                idempotent omero::model::OriginalFile save() throws ServerError;

            };
    };
};

#endif
