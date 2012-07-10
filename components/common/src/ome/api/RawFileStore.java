/*
 * ome.services.RawFileStore
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.api;

import ome.conditions.ResourceError;
import ome.model.core.OriginalFile;

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
     * Returns the size of the file on disk (not as stored in the database since
     * that value will only be updated on {@link #save()}.
     */
    public long size();

    /**
     * Limits the size of a file to the given length. If the file is already
     * shorter than length, no action is taken in which case false is returned.
     */
    public boolean truncate(long length);

    /**
     * Delegates to {@link ome.io.nio.FileBuffer}
     * 
     * @see ome.io.nio.FileBuffer#write(java.nio.ByteBuffer, long)
     */
    public void write(byte[] buf, long position, int length);

    /**
     * Saves the {@link OriginalFile} associated with the service if it has
     * been modified. The returned valued should replace all instances of the
     * {@link OriginalFile} in the client.
     *
     * If save has not been called, {@link RawFileStore} instances will save the
     * {@link OriginalFile} object associated with it on {@link #close()}.
     *
     * @see <a href="http://trac.openmicroscopy.org.uk/ome/ticket/1651>1651</a>
     * @see <a href="http://trac.openmicroscopy.org.uk/ome/ticket/2161>2161</a>
     */
    public OriginalFile save();
}
