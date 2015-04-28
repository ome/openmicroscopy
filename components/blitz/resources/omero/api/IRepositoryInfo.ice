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

        ["ami", "amd"] interface IRepositoryInfo extends ServiceInterface
            {
                idempotent long getUsedSpaceInKilobytes() throws ServerError;
                idempotent long getFreeSpaceInKilobytes() throws ServerError;
                idempotent double getUsageFraction() throws ServerError;
                void sanityCheckRepository() throws ServerError;
                void removeUnusedFiles() throws ServerError;
            };

    };
};

#endif
