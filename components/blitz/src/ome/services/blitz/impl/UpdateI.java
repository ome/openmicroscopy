/*
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

import java.util.List;

import ome.api.IUpdate;
import ome.services.blitz.util.BlitzExecutor;
import omero.ServerError;
import omero.api.AMD_IUpdate_deleteObject;
import omero.api.AMD_IUpdate_indexObject;
import omero.api.AMD_IUpdate_saveAndReturnArray;
import omero.api.AMD_IUpdate_saveAndReturnIds;
import omero.api.AMD_IUpdate_saveAndReturnObject;
import omero.api.AMD_IUpdate_saveArray;
import omero.api.AMD_IUpdate_saveCollection;
import omero.api.AMD_IUpdate_saveObject;
import omero.api._IUpdateOperations;
import omero.model.IObject;

import Ice.Current;

/**
 * Implementation of the IUpdate service.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta4
 * @see ome.api.IUpdate
 */
public class UpdateI extends AbstractAmdServant implements _IUpdateOperations {

    public UpdateI(IUpdate service, BlitzExecutor be) {
        super(service, be);
    }

    // Interface methods
    // =========================================================================

    public void deleteObject_async(AMD_IUpdate_deleteObject __cb, IObject row,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, row);

    }

    public void indexObject_async(AMD_IUpdate_indexObject __cb, IObject row,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, row);

    }

    public void saveAndReturnArray_async(AMD_IUpdate_saveAndReturnArray __cb,
            List<IObject> graph, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, graph);

    }

    public void saveAndReturnObject_async(AMD_IUpdate_saveAndReturnObject __cb,
            IObject obj, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, obj);

    }

    public void saveArray_async(AMD_IUpdate_saveArray __cb,
            List<IObject> graph, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, graph);

    }

    public void saveCollection_async(AMD_IUpdate_saveCollection __cb,
            List<IObject> objs, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, objs);
    }

    public void saveObject_async(AMD_IUpdate_saveObject __cb, IObject obj,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, obj);

    }

    public void saveAndReturnIds_async(AMD_IUpdate_saveAndReturnIds __cb,
            List<IObject> graph, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, graph);
    }

}
