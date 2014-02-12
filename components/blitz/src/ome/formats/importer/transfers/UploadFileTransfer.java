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
import java.util.List;

import ome.util.checksum.ChecksumProvider;
import omero.ServerError;
import omero.api.RawFileStorePrx;

import org.apache.commons.lang.ArrayUtils;

/**
 * Traditional file transfer mechanism which uploads
 * files using the API. This is done by reading from
 * {@link TransferState#getFile()} into {@link TransferState#getBuffer()}
 * and then {@link RawFileStorePrx#write(byte[], long, int) writing} to the
 * server. <em>Not thread safe</em>
 *
 * @since 5.0
 */
public class UploadFileTransfer extends AbstractFileTransfer {

    public String transfer(TransferState state) throws IOException, ServerError {

        final RawFileStorePrx rawFileStore = start(state);
        final File file = state.getFile();
        final byte[] buf = state.getBuffer();
        final ChecksumProvider cp = state.getChecksumProvider();
        
        FileInputStream stream = null;

        try {
            stream = new FileInputStream(file);
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

            return finish(state, offset);
        } finally {
            cleanupUpload(rawFileStore, stream);
        }
    }

    /**
     * Since the {@link RawFileStorePrx} instances are cleaned up after each
     * transfer, there's no need to cleanup per {@link File}.
     */
    public void afterTransfer(int errors, List<String> srcFiles) throws CleanupFailure {
        // no-op
    }
}
