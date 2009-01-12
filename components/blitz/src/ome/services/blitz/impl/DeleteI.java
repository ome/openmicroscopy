/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

import java.util.List;

import ome.api.IDelete;
import ome.services.blitz.util.BlitzExecutor;
import omero.ApiUsageException;
import omero.SecurityViolation;
import omero.ServerError;
import omero.ValidationException;
import omero.api.AMD_IDelete_checkImageDelete;
import omero.api.AMD_IDelete_deleteImage;
import omero.api.AMD_IDelete_deleteImagesByDataset;
import omero.api.AMD_IDelete_deleteImages;
import omero.api.AMD_IDelete_deleteSettings;
import omero.api.AMD_IDelete_previewImageDelete;
import omero.api._IDeleteOperations;
import Ice.Current;

/**
 * Implementation of the IDelete service.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta4
 * @see ome.api.IDelete
 */
public class DeleteI extends AbstractAmdServant implements _IDeleteOperations {

    public DeleteI(IDelete service, BlitzExecutor be) {
        super(service, be);
    }

    // Interface methods
    // =========================================================================

    public void checkImageDelete_async(AMD_IDelete_checkImageDelete __cb,
            long id, boolean force, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, id, force);
    }

    public void deleteImage_async(AMD_IDelete_deleteImage __cb, long id,
            boolean force, Current __current) throws ApiUsageException,
            SecurityViolation, ServerError, ValidationException {
        callInvokerOnRawArgs(__cb, __current, id, force);
    }

    public void previewImageDelete_async(AMD_IDelete_previewImageDelete __cb,
            long id, boolean force, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, id, force);
    }

    public void deleteImages_async(AMD_IDelete_deleteImages __cb,
            List<Long> ids, boolean force, Current __current)
            throws ApiUsageException, SecurityViolation, ServerError,
            ValidationException {
        callInvokerOnRawArgs(__cb, __current, ids, force);
    }

    public void deleteImagesByDataset_async(
            AMD_IDelete_deleteImagesByDataset __cb, long datasetId,
            boolean force, Current __current) throws ApiUsageException,
            SecurityViolation, ServerError, ValidationException {
        callInvokerOnRawArgs(__cb, __current, datasetId, force);
    }

    public void deleteSettings_async(AMD_IDelete_deleteSettings __cb,
            long imageId, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, imageId);
    }
}
