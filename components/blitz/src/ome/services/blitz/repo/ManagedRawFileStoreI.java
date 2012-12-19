/*
 * Copyright (C) 2012 Glencoe Software, Inc. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package ome.services.blitz.repo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import Ice.Current;

import ome.api.RawFileStore;
import ome.services.blitz.impl.RawFileStoreI;
import ome.services.blitz.util.BlitzExecutor;

import omero.ServerError;
import omero.api.AMD_RawFileStore_write;
import omero.api.AMD_StatefulServiceInterface_close;
import omero.grid.ImportProcess;

/**
 * Extension of the regular {@link RawFileStoreI} servant
 * to properly track the state of the uploads for the
 * current {@link ImportProcess}.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.5
 */
public class ManagedRawFileStoreI extends RawFileStoreI {

    private final static Log log = LogFactory.getLog(ManagedRawFileStoreI.class);

    private final ManagedImportProcessI proc;

    /**
     * The index of this raw file store instance in the import process
     * which it is a member of. This is used to callback and tell the
     * import process to remove this instance.
     */
    private final int idx;

    public ManagedRawFileStoreI(RawFileStore service, BlitzExecutor be,
            ManagedImportProcessI proc, int idx) {
        super(service, be);
        this.proc = proc;
        this.idx = idx;
    }

    public void write_async(AMD_RawFileStore_write __cb, byte[] buf,
            long position, int length, Current __current) throws ServerError {
        super.write_async(__cb, buf, position, length, __current);
        proc.setOffset(idx, position+length);
    }

    protected void postClose(Ice.Current c) {
        try {
            super.postClose(c);
        } finally {
            proc.closeCalled(idx);
        }
    }
}
