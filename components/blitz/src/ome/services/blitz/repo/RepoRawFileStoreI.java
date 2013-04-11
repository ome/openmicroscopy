/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.repo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Ice.Current;

import ome.api.RawFileStore;
import ome.services.blitz.impl.RawFileStoreI;
import ome.services.blitz.util.BlitzExecutor;

import omero.ServerError;
import omero.api.AMD_RawFileStore_read;
import omero.api.AMD_RawFileStore_setFileId;

/**
 * An implementation of the RepoRawFileStore interface
 *
 * @author Josh Moore, josh at glencoesoftware.com
 */
public class RepoRawFileStoreI extends RawFileStoreI {

    private final static Logger log = LoggerFactory.getLogger(RepoRawFileStoreI.class);

    public RepoRawFileStoreI(BlitzExecutor be, RawFileStore service, Ice.Current curr) {
        super(service, be);
    }

    @Override
    public void setFileId_async(AMD_RawFileStore_setFileId __cb, long fileId,
            Current __current) throws ServerError {
        omero.ApiUsageException aue = new omero.ApiUsageException();
        aue.message = "Cannot reset id to " + fileId;
        __cb.ice_exception(aue);
    }

    @Override
    public void read_async(AMD_RawFileStore_read __cb, long position,
            int length, Current __current) throws ServerError {

        if (length > 64 * 1000 * 1000) {
            __cb.ice_exception(new omero.ApiUsageException(null, null,
                    "Too big: " + length));
            return; // EARLY EXIT!
        }
        super.read_async(__cb, position, length, __current);
    }

}
