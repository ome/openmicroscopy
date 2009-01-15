/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

import ome.api.RawFileStore;
import ome.services.blitz.util.BlitzExecutor;
import omero.ServerError;
import omero.api.AMD_RawFileStore_exists;
import omero.api.AMD_RawFileStore_read;
import omero.api.AMD_RawFileStore_setFileId;
import omero.api.AMD_RawFileStore_write;
import omero.api.AMD_StatefulServiceInterface_activate;
import omero.api.AMD_StatefulServiceInterface_close;
import omero.api.AMD_StatefulServiceInterface_passivate;
import omero.api.AMD_StatefulServiceInterface_getCurrentEventContext;
import omero.api._RawFileStoreOperations;
import Ice.Current;

/**
 * Implementation of the RawFileStore service.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta4
 * @see ome.api.RawFileStore
 */
public class RawFileStoreI extends AbstractAmdServant implements
        _RawFileStoreOperations {

    public RawFileStoreI(RawFileStore service, BlitzExecutor be) {
        super(service, be);
    }

    // Interface methods
    // =========================================================================

    public void exists_async(AMD_RawFileStore_exists __cb, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current);

    }

    public void read_async(AMD_RawFileStore_read __cb, long position,
            int length, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, position, length);

    }

    public void setFileId_async(AMD_RawFileStore_setFileId __cb, long fileId,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, fileId);

    }

    public void write_async(AMD_RawFileStore_write __cb, byte[] buf,
            long position, int length, Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current, buf, position, length);

    }

    // Stateful interface methods
    // =========================================================================

    public void activate_async(AMD_StatefulServiceInterface_activate __cb,
            Current __current) {
        callInvokerOnRawArgs(__cb, __current);

    }

    public void passivate_async(AMD_StatefulServiceInterface_passivate __cb,
            Current __current) {
        callInvokerOnRawArgs(__cb, __current);

    }

    public void close_async(AMD_StatefulServiceInterface_close __cb,
            Current __current) {
        callInvokerOnRawArgs(__cb, __current);

    }

    public void getCurrentEventContext_async(
            AMD_StatefulServiceInterface_getCurrentEventContext __cb,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current);

    }

}
