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
import java.io.FileInputStream;
import java.io.IOException;

import ome.formats.importer.FileTransfer;
import ome.util.checksum.ChecksumProvider;
import omero.ServerError;
import omero.api.RawFileStorePrx;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Traditional file transfer mechanism which uploads
 * files using the API.
 *
 * @since 5.0
 */
public class UploadFileTransfer implements FileTransfer {

    private static final Logger log = LoggerFactory.getLogger(UploadFileTransfer.class);

    public String transfer(TransferState state) throws IOException, ServerError {

        final File file = state.getFile();
        final byte[] buf = state.getBuffer();
        final ChecksumProvider cp = state.getChecksumProvider();

        log.info("Transferring {}...", state.getFile());
        state.start();
        FileInputStream stream = null;
        RawFileStorePrx rawFileStore = null;

        try {
            stream = new FileInputStream(file);
            rawFileStore = state.getUploader(); 
            int rlen = 0;
            long offset = 0;

            state.uploadStarted();
      
                // "touch" the file otherwise zero-length files
            rawFileStore.write(ArrayUtils.EMPTY_BYTE_ARRAY, offset, 0);
            state.stop();
            state.uploadBytes(offset);
    
            while (true) {
                state.start();
                rlen = stream.read(buf);
                if (rlen == -1) {
                    break;
                }
                cp.putBytes(buf, 0, rlen);
                final byte[] bufferToWrite;
                if (rlen < buf.length) {
                    bufferToWrite = new byte[rlen];
                    System.arraycopy(buf, 0, bufferToWrite, 0, rlen);
                } else {
                    bufferToWrite = buf;
                }
                rawFileStore.write(bufferToWrite, offset, rlen);
                offset += rlen;
                state.stop(rlen);
                state.uploadBytes(offset);
            }

            state.start();
            state.save();
            state.stop();
            state.uploadComplete(offset);
            return state.getChecksum();
        } finally {
            cleanupUpload(rawFileStore, stream);
        }

    }

    protected void cleanupUpload(RawFileStorePrx rawFileStore,
            FileInputStream stream) throws ServerError {
        try {
            if (rawFileStore != null) {
                try {
                    rawFileStore.close();
                } catch (Exception e) {
                    log.error("error in closing raw file store", e);
                }
            }
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    log.error("I/O in error closing stream", e);
                }
            }
        }

    }
}
