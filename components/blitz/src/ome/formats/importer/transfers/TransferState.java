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
 * Traditional file transfer mechanism which uploads
 * files using the API.
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

    public void save() throws ServerError {
        RawFileStorePrx rawFileStore = getUploader();
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

    public byte[] getBuffer() {
        return this.buf;
    }

    public String getChecksum() {
        return this.checksum;
    }

    public ChecksumProvider getChecksumProvider() {
        return this.cp;
    }

    public File getFile() {
        return this.file;
    }

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

    public RawFileStorePrx getUploader() throws ServerError {
        return this.proc.getUploader(this.index);
    }

    //
    // NOTIFICATIONS AND LOGGING
    //

    public void uploadStarted() {
        library.notifyObservers(
                new ImportEvent.FILE_UPLOAD_STARTED(
                file.getAbsolutePath(), index, total,
                null, length, null));
    }

    public void uploadBytes(long offset) {
        library.notifyObservers(
                new ImportEvent.FILE_UPLOAD_BYTES(
                file.getAbsolutePath(), index, total,
                offset, length, estimator.getUploadTimeLeft(), null));
    }

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
