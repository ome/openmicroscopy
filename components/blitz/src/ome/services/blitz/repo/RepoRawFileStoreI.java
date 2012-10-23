/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.repo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import ome.services.blitz.impl.AbstractAmdServant;

import omero.ServerError;
import omero.api.AMD_RawFileStore_exists;
import omero.api.AMD_RawFileStore_read;
import omero.api.AMD_RawFileStore_save;
import omero.api.AMD_RawFileStore_setFileId;
import omero.api.AMD_RawFileStore_size;
import omero.api.AMD_RawFileStore_truncate;
import omero.api.AMD_RawFileStore_write;
import omero.api._RawFileStoreOperations;
import omero.util.IceMapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import Ice.Current;

/**
 * An implementation of the RepoRawFileStore interface
 *
 * @author Josh Moore, josh at glencoesoftware.com
 */
public class RepoRawFileStoreI extends AbstractAmdServant implements
_RawFileStoreOperations {

    private final static Log log = LogFactory.getLog(RepoRawFileStoreI.class);

    private final long fileId;

    private final File file;

    private final RandomAccessFile rafile;

    public RepoRawFileStoreI(long fileId, File file) {
        super(null, null);
        this.fileId = fileId;
        this.file = file;
        try {
            this.rafile = new RandomAccessFile(file, "r");
            log.info("Opened " + rafile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public RepoRawFileStoreI(String path, String mode) {
        super(null, null);
        this.fileId = -1;
        this.file = new File(path);
        try {
            this.rafile = new RandomAccessFile(file, mode);
            log.info("Opened " + rafile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    public void setFileId_async(AMD_RawFileStore_setFileId __cb, long fileId,
            Current __current) throws ServerError {
        omero.ApiUsageException aue = new omero.ApiUsageException();
        aue.message = "Cannot reset id from " + this.fileId + " to " + fileId;
        __cb.ice_exception(aue);
    }

    public void exists_async(AMD_RawFileStore_exists __cb, Current __current)
            throws ServerError {
        try {
            __cb.ice_response(file.exists());
        } catch (Exception e) {
            __cb.ice_exception(e);
        }
    }

    public void read_async(AMD_RawFileStore_read __cb, long position,
            int length, Current __current) throws ServerError {

        if (length > 64 * 1000 * 1000) {
            __cb.ice_exception(new omero.ApiUsageException(null, null,
                    "Too big: " + length));
            return; // EARLY EXIT!
        }

        try {
            final byte[] array = new byte[length];
            final ByteBuffer buffer = ByteBuffer.wrap(array);
            this.rafile.getChannel().read(buffer, position);
            __cb.ice_response(array);
        } catch (Throwable t) {
            __cb.ice_exception(convert(t));
        }

    }


    public void size_async(AMD_RawFileStore_size __cb, Current __current)
            throws ServerError {
        try {
            long size = this.rafile.getChannel().size();
            __cb.ice_response(size);
        } catch (Throwable t) {
            __cb.ice_exception(convert(t));
        }
    }

    public void truncate_async(AMD_RawFileStore_truncate __cb, long length,
            Current __current) throws ServerError {

        try {
            FileChannel fc = this.rafile.getChannel();
            if (fc.size() < length) {
                __cb.ice_response(false);
            } else {
                this.rafile.getChannel().truncate(length);
                __cb.ice_response(true);
            }
        } catch (Throwable t) {
            __cb.ice_exception(convert(t));
        }
    }

    public void write_async(AMD_RawFileStore_write __cb, byte[] buf,
            long position, int length, Current __current) throws ServerError {

        ByteBuffer buffer = MappedByteBuffer.wrap(buf);
        buffer.limit(length);

        try {
            rafile.getChannel().write(buffer, position);
            __cb.ice_response();
        } catch (Throwable t) {
            __cb.ice_exception(convert(t));
        }

    }

    public void save_async(AMD_RawFileStore_save __cb, Current __current)
            throws ServerError {
        __cb.ice_response(null); // DO NOTHING
    }

    @Override
    protected void preClose(Ice.Current current) throws Throwable {
        log.info("Closing " + rafile);
        this.rafile.close();
    }

    //
    // Helpers
    //

    private omero.ServerError convert(Throwable t) {
        if (t instanceof omero.ServerError) {
            return (omero.ServerError) t;
        }

        omero.ServerError se = null;
        if (t instanceof IOException) {
            se = new omero.ResourceError();
        } else {
            se = new omero.InternalException();
        }

        IceMapper.fillServerError(se, t);
        return se;

    }

}
