/*
 * ome.io.nio.PixelsService
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import ome.model.display.Thumbnail;


/**
 * @author callan
 *
 */
public class ThumbnailService extends AbstractFileSystemService
{

    public ThumbnailService(String path)
    {
        super(path);
    }
    
    public void createThumbnail(Thumbnail thumbnail, byte[] buf)
        throws IOException
    {
    	String path = getThumbnailPath(thumbnail.getId());
    	createSubpath(path);
    	
    	FileOutputStream stream = new FileOutputStream(path);
    	stream.write(buf);
    }
    
    public long getThumbnailLength(Thumbnail thumbnail)
    {
    	File f = new File(getThumbnailPath(thumbnail.getId()));
    	return f.length();
    }
    
    public byte[] getThumbnail(Thumbnail thumbnail) throws IOException
    {
    	byte[] buf = new byte[(int) getThumbnailLength(thumbnail)];
    	return getThumbnail(thumbnail, buf);
    }
    
    public byte[] getThumbnail(Thumbnail thumbnail, byte[] buf)
    	throws IOException
    {
    	String path = getThumbnailPath(thumbnail.getId());
    	FileInputStream stream = new FileInputStream(path);
    	stream.read(buf, 0, buf.length);
    	return buf;
    }
    
    public FileOutputStream getThumbnailOutputStream(Thumbnail thumbnail)
    	throws IOException
    {
    	String path = getThumbnailPath(thumbnail.getId());
    	createSubpath(path);
    	return new FileOutputStream(path);
    }
}
