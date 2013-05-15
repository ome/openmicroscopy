/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

import ome.api.IDelete;
import ome.io.nio.AbstractFileSystemService;
import ome.services.blitz.util.BlitzExecutor;
import ome.services.blitz.util.BlitzOnly;
import ome.services.blitz.util.ServiceFactoryAware;
import ome.services.graphs.GraphEntry;
import ome.services.graphs.GraphSpec;
import ome.services.scheduler.ThreadPool;
import ome.tools.hibernate.ExtendedMetadata;

import omero.ApiUsageException;
import omero.SecurityViolation;
import omero.ServerError;
import omero.ValidationException;
import omero.api.AMD_IDelete_availableCommands;
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
import omero.api.delete._DeleteHandleTie;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

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

    private final ExtendedMetadata em;

    private/* final */ServiceFactoryI sf;

    public DeleteI(ExtendedMetadata em, IDelete service, BlitzExecutor be,
            ThreadPool threadPool, int cancelTimeoutMs, String omeroDataDir) {
        super(service, be);
        this.em = em;
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
            boolean force, final Current __current) throws ApiUsageException,
            SecurityViolation, ServerError, ValidationException {

        safeRunnableCall(__current, __cb, true, new Callable<Object>() {
            public Object call() throws Exception {
                DeleteCommand dc = new DeleteCommand("/Image", imageId, null);
                makeAndRun(__current, handleId(), dc);
                return null;
            }});

    }

    public void previewImageDelete_async(AMD_IDelete_previewImageDelete __cb,
            long id, boolean force, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, id, force);
    }

    public void deleteImages_async(AMD_IDelete_deleteImages __cb,
            final List<Long> ids, boolean force, final Current __current)
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
                makeAndRun(__current, handleId(), commands);
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
            final long imageId, final Current __current) throws ServerError {

        safeRunnableCall(__current, __cb, true, new Callable<Object>() {
            public Object call() throws Exception {
                DeleteCommand dc = new DeleteCommand("/Image/Pixels/RenderingDef", imageId, null);
                makeAndRun(__current, handleId(), dc);
                return null;
            }});
    }

    public void deletePlate_async(AMD_IDelete_deletePlate __cb,
            final long plateId, final Current __current) throws ServerError {

        safeRunnableCall(__current, __cb, true, new Callable<Object>() {
            public Object call() throws Exception {
                DeleteCommand dc = new DeleteCommand("/Plate", plateId, null);
                makeAndRun(__current, handleId(), dc);
                return null;
            }});

    }

    public void queueDelete_async(final AMD_IDelete_queueDelete __cb,
            final DeleteCommand[] commands, final Current __current)
            throws ApiUsageException, ServerError {

        safeRunnableCall(__current, __cb, false, new Callable<DeleteHandlePrx>() {
            public DeleteHandlePrx call() throws Exception {
                Ice.Identity id = handleId();
                DeleteHandleI handle = makeAndLaunchHandle(__current, id, commands);
                DeleteHandlePrx prx = DeleteHandlePrxHelper.
                    uncheckedCast(sf.registerServant(id,
                            new _DeleteHandleTie(handle)));
                return prx;
            }});
    }

    public void availableCommands_async(final AMD_IDelete_availableCommands __cb,
            final Current __current)
            throws ServerError {
        safeRunnableCall(__current, __cb, false, new Callable<DeleteCommand[]>() {
            public DeleteCommand[] call() throws Exception {
                ApplicationContext ctx = loadSpecs();
                final String[] keys = ctx.getBeanNamesForType(GraphSpec.class);
                final DeleteCommand[] dcs = new DeleteCommand[keys.length];
                for (int i = 0; i < dcs.length; i++) {
                    String key = keys[i];
                    GraphSpec spec = ctx.getBean(key, GraphSpec.class);
                    Map<String, String> options = new HashMap<String, String>();
                    for (GraphEntry entry : spec.entries()) {
                        options.put(entry.log(""), "");
                    }
                    dcs[i] = new DeleteCommand(key, -1, options);
                }
                return dcs;
            }
        });
    }

    public DeleteHandleI makeAndLaunchHandle(final Ice.Identity id, final DeleteCommand...commands) throws ServerError {
        return makeAndLaunchHandle(null, id, commands);
    }

    public DeleteHandleI makeAndLaunchHandle(final Ice.Current current, final Ice.Identity id,
            final DeleteCommand...commands) throws ServerError {
        DeleteHandleI handle = new DeleteHandleI(em,
                loadSpecs(), id, sf, afs, commands, cancelTimeoutMs, current.ctx);
        handle.setApplicationContext(ctx);
        threadPool.getExecutor().execute(handle);
        return handle;
    }

    public void makeAndRun(final Ice.Identity id, final DeleteCommand...commands) throws ServerError {
        makeAndRun(null, id, commands);
    }

    public void makeAndRun(final Ice.Current current, final Ice.Identity id, final DeleteCommand...commands) throws ServerError {
        DeleteHandleI handle = new DeleteHandleI(em,
                loadSpecs(), id, sf, afs, commands, cancelTimeoutMs, current.ctx);
        handle.setApplicationContext(ctx);
        handle.run();
    }

    private Ice.Identity handleId() {
        Ice.Identity id = sf.getIdentity("DeleteHandle"+UUID.randomUUID().toString());
        return id;
    }

    public ApplicationContext loadSpecs() {
        return new ClassPathXmlApplicationContext(
                new String[]{"classpath:ome/services/spec.xml"}, ctx);

    }

}
