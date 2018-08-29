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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

import ome.util.checksum.ChecksumProvider;
import omero.ServerError;
import omero.api.RawFileStorePrx;

import org.apache.commons.lang.ArrayUtils;

/**
 * Similar to {@link UploadFileTransfer} but uses a mmap'd file
 * for reading from disk. <em>Not thread safe</em>
 *
 * @since 5.4.8
 */
public class MMapUploadFileTransfer extends AbstractFileTransfer {

    public String transfer(TransferState state) throws IOException, ServerError {

        final RawFileStorePrx rawFileStore = start(state);
        final File file = state.getFile();
        final byte[] buf = state.getBuffer();
        final ChecksumProvider cp = state.getChecksumProvider();

        RandomAccessFile raf = null;
        FileChannel ch = null;
        try {
            int rlen = 0;
            long offset = 0;

            state.uploadStarted();

            // "touch" the file otherwise zero-length files
            rawFileStore.write(ArrayUtils.EMPTY_BYTE_ARRAY, offset, 0);
            state.stop();
            state.uploadBytes(offset);

            raf = new RandomAccessFile(file, "r");
            ch = raf.getChannel();
            MappedByteBuffer buffer = ch.map(FileChannel.MapMode.READ_ONLY, 0, ch.size());

            while (buffer.hasRemaining()) {
                state.start();
                rlen = Math.min(buffer.remaining(), buf.length);
                if (rlen <= 0) {
                    break;
                }
                buffer.get(buf, 0, rlen);
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
            doClose(raf);
            doClose(ch);
            cleanupUpload(rawFileStore, null);
        }
    }

    private  void doClose(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
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
