/*
 * ome.services.RawFileStore
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.api;

/**
 * Raw file gateway which provides access to the OMERO file repository.
 * 
 * @author Chris Allan &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:callan@blackcat.ca">callan@blackcat.ca</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision$ $Date:
 *          2005/06/08 15:21:59 $) </small>
 * @since OMERO3.0
 */
public interface RawFileStore extends StatefulServiceInterface {
    /**
     * This method manages the state of the service.
     * 
     * @param fileId
     *            an {@link ome.model.core.OriginalFile} id.
     */
    public void setFileId(long fileId);
    
    /**
     * Checks to see if a raw file exists with the file ID that the service was
     * initialized with.
     * @return <code>true</code> if there is an accessible file within the
     * original file repository with the correct ID. Otherwise
     * <code>false</code>.
     * @throws ResourceError if there is a problem accessing the file due to
     * permissions errors within the repository or any other I/O error.
     */
    public boolean exists();

    /**
     * Delegates to {@link ome.io.nio.FileBuffer}
     * 
     * @see ome.io.nio.FileBuffer#read(java.nio.ByteBuffer, long)
     */
    public byte[] read(long position, int length);

    /**
     * Delegates to {@link ome.io.nio.FileBuffer}
     * 
     * @see ome.io.nio.FileBuffer#write(java.nio.ByteBuffer, long)
     */
    public void write(byte[] buf, long position, int length);
}
