/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_IDELETE_ICE
#define OMERO_API_IDELETE_ICE

#include <omero/ModelF.ice>
#include <omero/ServicesF.ice>
#include <omero/Collections.ice>


module omero {

    module api {

        /**
         * See <a href="http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/ome/api/IDelete.html">IDelete.html</a>
         **/
        ["ami", "amd"] interface IDelete extends omero::api::ServiceInterface
            {
                omero::api::IObjectList checkImageDelete(long id, bool force) throws ServerError;
                omero::api::IObjectList previewImageDelete(long id, bool force) throws ServerError;
                void deleteImage(long id, bool force) throws ApiUsageException, ValidationException, SecurityViolation, ServerError;
                void deleteImages(LongList ids, bool force) throws ApiUsageException, ValidationException, SecurityViolation, ServerError;
                void deleteImagesByDataset(long datasetId, bool force) throws ApiUsageException, ValidationException, SecurityViolation, ServerError;
                void deleteSettings(long imageId) throws ServerError;
                void deletePlate(long plateId) throws ServerError;
            };

    };
};

#endif
