/*
 * Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
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

package ome.formats.importer.transfers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ome.formats.importer.ImportEvent;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.util.TimeEstimator;
import ome.util.checksum.ChecksumProvider;
import omero.ServerError;
import omero.api.RawFileStorePrx;
import omero.grid.ImportProcessPrx;
import omero.model.OriginalFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Non-thread-safe argument holder for {@link FileTransfer} implementations.
 * A single instance will be created per invocation of
 * {@link FileTransfer#transfer(TransferState)}. Several instance methods are
 * provided for common reporting actions (See usage in existing
 * {@link FileTransfer} implementations.
 *
 * @since 5.0
 */
public class TransferState implements TimeEstimator {

    private static final Logger log = LoggerFactory.getLogger(TransferState.class);

    private final File file;

    private final long length;

    private final int index;

    private final int total;

    private final ImportProcessPrx proc;

    private final ImportLibrary library;

    private final TimeEstimator estimator;

    private final ChecksumProvider cp;

    private final byte[] buf;

    private OriginalFile ofile;

    private String checksum;

    /**
     * Cache of the latest return value from
     * {@link #getUploader(String)} which can be used to cleanup
     * server state.
     */
    private RawFileStorePrx prx;

    /**
     * State of the current file transfer.
     *
     * @param file Source file which is to be transferred.
     * @param index Which of the total files to upload this is.
     * @param total Total number of files to upload.
     * @param proc {@link ImportProcessPrx} which is being imported to.
     * @param library {@link ImportLibrary} to use for notifications.
     * @param estimator
     * @param cp
     * @param buf optional buffer. Need not be used or updated.
     */
    public TransferState(File file,
            int index, int total, // as index of
            ImportProcessPrx proc, // to
            ImportLibrary library,
            TimeEstimator estimator,
            ChecksumProvider cp,
            byte[] buf) throws IOException, ServerError {
                this.file = file;
                this.length = file.length();
                this.index = index;
                this.total = total;
                this.proc = proc;
                this.library = library;
                this.estimator = estimator;
                this.cp = cp;
                this.buf = buf;
            }

    /**
     * Calls {@link RawFileStorePrx#save()} and stores the resultant
     * {@link OriginalFile} for future inspection along with the <em>local</em>
     * checksum. (The remote checksum is available from the
     * {@link OriginalFile}.
     *
     * @throws ServerError
     */
    public void save() throws ServerError {
        // We don't need write access here, and considering that
        // a symlink or similar to a non-executable file may have
        // replaced the previous test file (see checkLocation), we
        // try to be as conservative as possible.
        RawFileStorePrx rawFileStore = getUploader("r");
        checksum = cp.checksumAsString();
        ofile = rawFileStore.save();
        if (log.isDebugEnabled()) {
            log.debug(String.format("%s/%s id=%s",
                    ofile.getPath().getValue(),
                    ofile.getName().getValue(),
                    ofile.getId().getValue()));
            log.debug(String.format("checksums: client=%s,server=%s",
                    checksum, ofile.getHash().getValue()));
        }
    }

    //
    // ACCESSORS
    //

    /**
     * <em>(Not thread safe)</em> Get a moderately large buffer for use in
     * reading/writing data. To prevent the creation of many MB-sized byte
     * arrays, this value can be re-used but requires external synchronization.
     */
    public byte[] getBuffer() {
        return this.buf;
    }

    /**
     * Get the digest string for the local file. This will only be available,
     * i.e. non-null, after {@link #save()} has been called.
     */
    public String getChecksum() {
        return this.checksum;
    }

    /**
     * Get the {@link ChecksumProvider} passed to the constructor.
     * Since the {@link ChecksumProvider} has a number of different usage styles,
     * {@link TransferState} doesn't attempt to delegate but just returns the
     * instance.
     */
    public ChecksumProvider getChecksumProvider() {
        return this.cp;
    }

    /**
     * Return the target file passed to the constructor.
     */
    public File getFile() {
        return this.file;
    }

    /**
     * Return the length of the {@link #getFile() target file}.
     */
    public long getLength() {
        return this.length;
    }

    /**
     * Find original file as defined by the ID in the {@link RawFileStorePrx}
     * regardless of group.
     */
    public OriginalFile getOriginalFile() throws ServerError {
        return library.loadOriginalFile(getUploader());
    }

    /**
     * Find original file represented by the managed repository that
     * import is taking place to.
     */
    public OriginalFile getRootFile() throws ServerError {
        return library.lookupManagedRepository().root();
    }

    /**
     * Return the {@link RawFileStorePrx} instance for this index.
     */
    public RawFileStorePrx getUploader() throws ServerError {
        return getUploader(null);
    }

    /**
     * Return the {@link RawFileStorePrx} instance for this index setting
     * the mode if not null. Valid values include "r" and "rw". If a non-null
     * {@link #prx} is available, it will be returned <em>instead</em>.
     *
     * <em>Every</em> instance which is returned from this method should
     * eventually have {@link RawFileStorePrx#close()} called on it.
     * {@link #close()} can be used to facilitate this.
     */
    public RawFileStorePrx getUploader(String mode) throws ServerError {
        if (prx != null) {
            return prx;
        } else if (mode != null) {
            Map<String, String> ctx = new HashMap<String, String>();
            ctx.put("omero.fs.mode", mode);
            prx = this.proc.getUploader(this.index, ctx);
        } else {
            prx = this.proc.getUploader(this.index);
        }
        return prx;
    }

    /**
     * Call {@link RawFileStorePrx#close()} on the cached {@link #prx}
     * instance if non-null and null the instance. If
     * {@link Ice.ObjectNotExistException} is thrown, the service is
     * assumed closed. All other exceptions will be printed at WARN.
     */
    public void closeUploader() {
        if (prx != null) {
            try {
                prx.close();
            } catch (Ice.ObjectNotExistException onee) {
                // no-op
            } catch (Exception e) {
                log.warn("Exception closing " + prx, e);
            } finally {
                prx = null;
            }
        }
    }

    //
    // NOTIFICATIONS AND LOGGING
    //

    /**
     * Raise the {@link ImportEvent.FILE_UPLOAD_STARTED} event to all
     * observers.
     */
    public void uploadStarted() {
        library.notifyObservers(
                new ImportEvent.FILE_UPLOAD_STARTED(
                file.getAbsolutePath(), index, total,
                null, length, null));
    }

    /**
     * Raise the {@link ImportEvent.FILE_UPLOAD_BYTES} event to all
     * observers.
     */
    public void uploadBytes(long offset) {
        library.notifyObservers(
                new ImportEvent.FILE_UPLOAD_BYTES(
                file.getAbsolutePath(), index, total,
                offset, length, estimator.getUploadTimeLeft(), null));
    }

    /**
     * Raise the {@link ImportEvent.FILE_UPLOAD_COMPLETE} event to all
     * observers.
     */
    public void uploadComplete(long offset) {
        library.notifyObservers(new ImportEvent.FILE_UPLOAD_COMPLETE(
                file.getAbsolutePath(), index, total,
                offset, length, null));
    }

    //
    // ESTIMATOR DELEGATION
    //

    public void start() {
        this.estimator.start();
    }

    public void stop() {
        this.estimator.stop();
    }

    public void stop(long uploadedBytes) {
        this.estimator.stop(uploadedBytes);
    }

    public long getUploadTimeLeft() {
        return this.estimator.getUploadTimeLeft();
    }

}
