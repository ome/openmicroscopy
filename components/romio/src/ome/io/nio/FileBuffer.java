/*
 * ome.io.nio.FileBuffer
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */
package ome.io.nio;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import ome.model.core.OriginalFile;


/** 
 * Raw file buffer which provides I/O operations within the OMERO file
 * repository. 
 *
 * @author  Chris Allan &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:callan@blackcat.ca">callan@blackcat.ca</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: 1.2 $ $Date: 2005/06/08 15:21:59 $)
 * </small>
 * @since OMERO3.0
 */
public class FileBuffer extends AbstractBuffer
{
    /** The original file object that this file buffer maps to. */
    private OriginalFile file;
    
    /** The file's I/O channel. */
    FileChannel channel;
    
    /**
     * Default constructor.
     * 
     * @param path path to the root of the <code>File</code> repository.
     * @param file the original file this buffer maps to.
     * @throws FileNotFoundException
     */
    FileBuffer (String path, OriginalFile file)
    {
        super(path);
        if (file == null)
            throw new NullPointerException(
                    "Expecting a not-null file element.");
        
        this.file = file;
    }
    
    /**
     * Retrieve the NIO channel that corresponds to this file.
     * @return the file channel.
     */
    private FileChannel getFileChannel()
    throws FileNotFoundException
    {
    	if (channel == null)
    	{
    		RandomAccessFile file = new RandomAccessFile(getPath(), "rw");
    		channel = file.getChannel();
    	}

    	return channel;
    }
    
    /**
     * Delegates to {@link java.nio.FileChannel}
     * 
     * @see java.nio.FileChannel#read(java.nio.ByteBuffer)
     */
    public int read(ByteBuffer dst) throws IOException
    {
    	return getFileChannel().read(dst);
    }
    
    /**
     * Delegates to {@link java.nio.FileChannel}
     * 
     * @see java.nio.FileChannel#read(java.nio.ByteBuffer, long)
     */
    public int read(ByteBuffer dst, long position) throws IOException
    {
    	return getFileChannel().read(dst, position);
    }
    
    /**
     * Delegates to {@link java.nio.FileChannel}
     * 
     * @see java.nio.FileChannel#write(java.nio.ByteBuffer, long)
     */
    public int write(ByteBuffer src, long position) throws IOException
    {
    	return getFileChannel().write(src, position);
    }
    
    /**
     * Delegates to {@link java.nio.FileChannel}
     * 
     * @see java.nio.FileChannel#write(java.nio.ByteBuffer)
     */
    public int write(ByteBuffer src) throws IOException
    {
    	return getFileChannel().write(src);
    }
    
    /**
     * Retrieve the file's identifier.
     * @return the file's id.
     */
    long getId()
    {
        return file.getId();
    }
    
    /**
     * Retrieve the file's name.
     * @return the file's name.
     */
    String getName()
    {
        return file.getName();
    }
}
