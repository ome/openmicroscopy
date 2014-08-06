/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.repo;

import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Ice.Current;

import ome.api.RawFileStore;
import ome.services.blitz.impl.RawFileStoreI;
import ome.services.blitz.util.BlitzExecutor;

import omero.ServerError;
import omero.api.AMD_RawFileStore_read;
import omero.api.AMD_RawFileStore_setFileId;
import omero.api.AMD_RawFileStore_write;

/**
 * An implementation of the RepoRawFileStore interface
 *
 * @author Josh Moore, josh at glencoesoftware.com
 */
public class RepoRawFileStoreI extends RawFileStoreI {

    private final static Logger log = LoggerFactory.getLogger(RepoRawFileStoreI.class);

    /**
     * Interface which can be implemented in order to inject logic into various
     * {@link RepoRawFileStoreI} methods. These are only called if the callback
     * instance is set via {@link RepoRawFileStoreI#setCallback(Callback)}.
     *
     */
    interface Callback {

        /**
         * Called if and only if the write method is successful.
         */
        void onWrite(byte[] buf, long position, long length);

        /**
         * Called in a finally block after
         * {@link RepoRawFileStoreI#postClose(Current)} completes.
         */
        void onPreClose();

        /**
         * Called in a finally block after
         * {@link RepoRawFileStoreI#postClose(Current)} completes.
         */
        void onPostClose();
    }

    /**
     * Implementation of {@link Callback} which does nothing for each of the
     * methods. Each method can be easily overridden in order to provide a
     * subset of the callback methods.
     */
    static class NoOpCallback implements Callback {

        @Override
        public void onWrite(byte[] buf, long position, long length) {
             // no-op
        }

        @Override
        public void onPreClose() {
            // no-op
        }

        @Override
        public void onPostClose() {
            // no-op
        }

    }

    private AtomicReference<Callback> cb = new AtomicReference<Callback>();

    public RepoRawFileStoreI(BlitzExecutor be, RawFileStore service, Ice.Current curr) {
        super(service, be);
    }

    public void setCallback(Callback cb) {
        this.cb.set(cb);
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

    @Override
    public void write_async(AMD_RawFileStore_write __cb, byte[] buf,
            long position, int length, Current __current) throws ServerError {
        super.write_async(__cb, buf, position, length, __current);
        Callback cb = this.cb.get();
        if (cb != null) {
            cb.onWrite(buf, position, length);
        }
    }

    @Override
    protected void preClose(Current current) throws Throwable {
        try {
            super.preClose(current);
        } finally {
            Callback cb = this.cb.get();
            if (cb != null) {
                cb.onPreClose();
            }
        }
    }

    @Override
    protected void postClose(Ice.Current c) {
        try {
            super.postClose(c);
        } finally {
            Callback cb = this.cb.get();
            if (cb != null) {
                cb.onPostClose();
            }
        }
    }
}
