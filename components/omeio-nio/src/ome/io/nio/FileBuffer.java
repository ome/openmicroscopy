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
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import ome.model.core.OriginalFile;


/**
 * @author callan
 *
 */
public class FileBuffer
{
    private RandomAccessFile delegate;
    private OriginalFile file;
    private String path;
    
    FileBuffer (OriginalFile file, String mode)
        throws FileNotFoundException
    {
        this.file = file;
        this.path = Helper.getFilesPath(file.getId());
        
        delegate = new RandomAccessFile(path, mode);
    }
    
    //
    // Explicit delegation methods
    //
    
    public FileChannel getChannel()
    {
        return delegate.getChannel();
    }
    
    //
    // Delegate methods to ease work with original file
    //
    
    long getId()
    {
        return file.getId();
    }
    
    String getName()
    {
        return file.getName();
    }
    
    String getPath()
    {
        return file.getPath();
    }
}
