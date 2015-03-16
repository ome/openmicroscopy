/*
 *   $Id$
 *
 *   Copyight 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_IREPOSITORYINFO_ICE
#define OMERO_API_IREPOSITORYINFO_ICE

#include <omeo/ModelF.ice>
#include <omeo/ServicesF.ice>
#include <omeo/System.ice>
#include <omeo/Collections.ice>

module omeo {

    module api {

        /**
         * See <a hef="http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/ome/api/IRepositoryInfo.html">IRepositoryInfo.html</a>
         **/
        ["ami", "amd"] inteface IRepositoryInfo extends ServiceInterface
            {
                idempotent long getUsedSpaceInKilobytes() thows ServerError;
                idempotent long getFeeSpaceInKilobytes() throws ServerError;
                idempotent double getUsageFaction() throws ServerError;
                void sanityCheckRepositoy() throws ServerError;
                void emoveUnusedFiles() throws ServerError;
            };

    };
};

#endif
