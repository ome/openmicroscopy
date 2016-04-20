/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_IREPOSITORYINFO_ICE
#define OMERO_API_IREPOSITORYINFO_ICE

#include <omero/ModelF.ice>
#include <omero/ServicesF.ice>
#include <omero/System.ice>
#include <omero/Collections.ice>

module omero {

    module api {

        /**
         * Provides methods for obtaining information for server repository
         * disk space allocation. Could be used generically to obtain usage
         * information for any mount point, however, this interface is
         * prepared for the API to provide methods to obtain usage info for
         * the server filesystem containing the image uploads. For the OMERO
         * server base this is /OMERO. For this implementation it could be
         * anything e.g. /Data1.
         *
         * Methods that fail or cannot execute on the server will throw an
         * InternalException. This would not be normal and would indicate some
         * server or disk failure.
         **/
        ["ami", "amd"] interface IRepositoryInfo extends ServiceInterface
            {
                /**
                 * Returns the total space in bytes for this file system
                 * including nested subdirectories.
                 *
                 * @return Total space used on this file system.
                 * @throws ResourceError If there is a problem retrieving disk
                 *         space used.
                 **/
                idempotent long getUsedSpaceInKilobytes() throws ServerError;

                /**
                 * Returns the free or available space on this file system
                 * including nested subdirectories.
                 *
                 * @return Free space on this file system in KB.
                 * @throws ResourceError If there is a problem retrieving disk
                 *         space free.
                 **/
                idempotent long getFreeSpaceInKilobytes() throws ServerError;

                /**
                 * Returns a double of the used space divided by the free
                 * space.
                 * This method will be called by a client to watch the
                 * repository filesystem so that it doesn't exceed 95% full.
                 *
                 * @return Fraction of used/free.
                 * @throws ResourceError If there is a problem calculating the
                 *         usage fraction.
                 **/
                idempotent double getUsageFraction() throws ServerError;

                /**
                 * Checks that image data repository has not exceeded 95% disk
                 * space use level.
                 * @throws ResourceError If the repository usage has exceeded
                 *         95%.
                 * @throws InternalException If there is a critical failure
                 *         while sanity checking the repository.
                 **/
                void sanityCheckRepository() throws ServerError;

                /**
                 * Removes all files from the server that do not have an
                 * OriginalFile complement in the database, all the Pixels
                 * that do not have a complement in the database and all the
                 * Thumbnail's that do not have a complement in the database.
                 *
                 * @throws ResourceError If deletion fails.
                 **/
                void removeUnusedFiles() throws ServerError;
            };

    };
};

#endif
