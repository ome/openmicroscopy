/*
 * ome.io.nio.FileBuffer
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.io.nio;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Raw file buffer which provides I/O operations within the OMERO file
 * repository.
 * 
 * @author Chris Allan &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:callan@blackcat.ca">callan@blackcat.ca</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision$ $Date:
 *          2005/06/08 15:21:59 $) </small>
 * @since OMERO3.0
 */
public class FileBuffer extends AbstractBuffer {

    /** The file's I/O channel. */
    FileChannel channel;

    final private String mode;

    /**
     * Default constructor allowing to pass in a non-"rw" file mode.
     * 
     * @param path
     *            path to the root of the <code>File</code> repository.
     * @param mode
     *            will be passed to the constructor of {@link RandomAccessFile}
     * @throws FileNotFoundException
     */
     public FileBuffer(String path, String mode) {
        super(path);
        this.mode = mode;
    }

    /**
     * Closes the buffer, cleaning up file state.
     * 
     * @throws IOException
     *             if an I/O error occurs.
     */
    public void close() throws IOException {
        if (channel != null) {
            channel.close();
        }
    }

    /**
     * Flush the buffer, writing any pending content to the underlying storage device,
     * optionally also the file's metadata.
     * @param includeMetadata flushes also the file metadata, not just the content
     * @throws IOException an I/O error that occurred
     */
    public void flush(boolean includeMetadata) throws IOException {
        if (channel != null) {
            channel.force(includeMetadata);
        }
    }

    /**
     * Retrieve the NIO channel that corresponds to this file.
     * 
     * @return the file channel.
     */
    private FileChannel getFileChannel() throws FileNotFoundException {
        if (channel == null) {
            RandomAccessFile file = new RandomAccessFile(getPath(), mode);
            channel = file.getChannel();
        }

        return channel;
    }

    /**
     * Delegates to {@link FileChannel}
     * 
     * @see FileChannel#read(ByteBuffer)
     */
    public int read(ByteBuffer dst) throws IOException {
        return getFileChannel().read(dst);
    }

    /**
     * Delegates to {@link FileChannel}
     * 
     * @see FileChannel#read(ByteBuffer, long)
     */
    public int read(ByteBuffer dst, long position) throws IOException {
        return getFileChannel().read(dst, position);
    }

    /**
     * Delegates to {@link FileChannel}
     * 
     * @see FileChannel#write(ByteBuffer, long)
     */
    public int write(ByteBuffer src, long position) throws IOException {
        return getFileChannel().write(src, position);
    }

    /**
     * Delegates to {@link FileChannel}
     * 
     * @see FileChannel#write(ByteBuffer)
     */
    public int write(ByteBuffer src) throws IOException {
        return getFileChannel().write(src);
    }

    public long size() throws IOException {
        return getFileChannel().size();
    }

    public void truncate(long size) throws IOException {
        getFileChannel().truncate(size);
    }

    /**
     * Only truncate if the size of the file is less than the size argument.
     *
     * @param size
     * @return true if truncation was performed.
     */
    public boolean truncateIfSmaller(long size) throws IOException {
        FileChannel fc = getFileChannel();
        if (fc.size() < size) {
            return false;
        } else {
            fc.truncate(size);
            return true;
        }
    }
}
