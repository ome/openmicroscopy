/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import ome.api.IDelete;
import ome.io.nio.AbstractFileSystemService;
import ome.services.blitz.util.BlitzExecutor;
import ome.services.blitz.util.BlitzOnly;
import ome.services.blitz.util.ServiceFactoryAware;
import ome.services.scheduler.ThreadPool;
import omero.ApiUsageException;
import omero.SecurityViolation;
import omero.ServerError;
import omero.ValidationException;
import omero.api.AMD_IDelete_checkImageDelete;
import omero.api.AMD_IDelete_deleteImage;
import omero.api.AMD_IDelete_deleteImages;
import omero.api.AMD_IDelete_deleteImagesByDataset;
import omero.api.AMD_IDelete_deletePlate;
import omero.api.AMD_IDelete_deleteSettings;
import omero.api.AMD_IDelete_previewImageDelete;
import omero.api.AMD_IDelete_queueDelete;
import omero.api._IDeleteOperations;
import omero.api.delete.DeleteCommand;
import omero.api.delete.DeleteHandlePrx;
import omero.api.delete.DeleteHandlePrxHelper;
import Ice.Current;

/**
 * Implementation of the IDelete service.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta4
 * @see ome.api.IDelete
 */
public class DeleteI extends AbstractAmdServant implements _IDeleteOperations,
    ServiceFactoryAware, BlitzOnly {

    private final ThreadPool threadPool;

    private final int cancelTimeoutMs;

    private final AbstractFileSystemService afs;

    private/* final */ServiceFactoryI sf;

    public DeleteI(IDelete service, BlitzExecutor be, ThreadPool threadPool, int cancelTimeoutMs, String omeroDataDir) {
        super(service, be);
        this.threadPool = threadPool;
        this.cancelTimeoutMs = cancelTimeoutMs;
        this.afs = new AbstractFileSystemService(omeroDataDir);
    }

    public void setServiceFactory(ServiceFactoryI sf) throws ServerError {
        this.sf = sf;
    }

    // Interface methods
    // =========================================================================

    public void checkImageDelete_async(AMD_IDelete_checkImageDelete __cb,
            long id, boolean force, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, id, force);
    }

    public void deleteImage_async(AMD_IDelete_deleteImage __cb, final long imageId,
            boolean force, Current __current) throws ApiUsageException,
            SecurityViolation, ServerError, ValidationException {

        safeRunnableCall(__current, __cb, true, new Callable<Object>() {
            public Object call() throws Exception {
                DeleteCommand dc = new DeleteCommand("/Image", imageId, null);
                makeAndRun(handleId(), dc);
                return null;
            }});

    }

    public void previewImageDelete_async(AMD_IDelete_previewImageDelete __cb,
            long id, boolean force, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, id, force);
    }

    public void deleteImages_async(AMD_IDelete_deleteImages __cb,
            final List<Long> ids, boolean force, Current __current)
            throws ApiUsageException, SecurityViolation, ServerError,
            ValidationException {

        safeRunnableCall(__current, __cb, true, new Callable<Object>() {
            public Object call() throws Exception {
                if (ids == null || ids.size() == 0) {
                    return null;
                }
                DeleteCommand[] commands = new DeleteCommand[ids.size()];
                for (int i = 0; i < ids.size(); i++) {
                    commands[i] = new DeleteCommand("/Image", ids.get(i), null);
                }
                makeAndRun(handleId(), commands);
                return null;
            }});

    }

    public void deleteImagesByDataset_async(
            AMD_IDelete_deleteImagesByDataset __cb, long datasetId,
            boolean force, Current __current) throws ApiUsageException,
            SecurityViolation, ServerError, ValidationException {
        callInvokerOnRawArgs(__cb, __current, datasetId, force);
    }

    public void deleteSettings_async(AMD_IDelete_deleteSettings __cb,
            final long imageId, Current __current) throws ServerError {

        safeRunnableCall(__current, __cb, true, new Callable<Object>() {
            public Object call() throws Exception {
                DeleteCommand dc = new DeleteCommand("/Image/Pixels/RenderingDef", imageId, null);
                makeAndRun(handleId(), dc);
                return null;
            }});
    }

    public void deletePlate_async(AMD_IDelete_deletePlate __cb,
            final long plateId, Current __current) throws ServerError {

        safeRunnableCall(__current, __cb, true, new Callable<Object>() {
            public Object call() throws Exception {
                DeleteCommand dc = new DeleteCommand("/Plate", plateId, null);
                makeAndRun(handleId(), dc);
                return null;
            }});

    }

    public void queueDelete_async(final AMD_IDelete_queueDelete __cb,
            final DeleteCommand[] commands, final Current __current)
            throws ApiUsageException, ServerError {

        safeRunnableCall(__current, __cb, false, new Callable<DeleteHandlePrx>() {
            public DeleteHandlePrx call() throws Exception {
                Ice.Identity id = handleId();
                DeleteHandleI handle = makeAndLaunchHandle(id, commands);
                DeleteHandlePrx prx = DeleteHandlePrxHelper.
                    uncheckedCast(sf.registerServant(id, handle));
                return prx;
            }});

    }

    public DeleteHandleI makeAndLaunchHandle(final Ice.Identity id, final DeleteCommand...commands) {
        DeleteHandleI handle = new DeleteHandleI(id, sf, afs, commands, cancelTimeoutMs);
        threadPool.getExecutor().execute(handle);
        return handle;
    }

    public void makeAndRun(final Ice.Identity id, final DeleteCommand...commands) {
        DeleteHandleI handle = new DeleteHandleI(id, sf, afs, commands, cancelTimeoutMs);
        handle.run();
    }

    private Ice.Identity handleId() {
        Ice.Identity id = sf.getIdentity("DeleteHandle"+UUID.randomUUID().toString());
        return id;
    }

}
