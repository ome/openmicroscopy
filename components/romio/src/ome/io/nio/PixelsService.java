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
import java.io.FileOutputStream;
import java.io.IOException;

import ome.model.core.Pixels;


/**
 * @author callan
 *
 */
public class PixelsService extends AbstractFileSystemService
{

    public PixelsService(String path)
    {
        super(path);
    }
    
    public static final int NULL_PLANE_SIZE = 64;
    public static final byte[] nullPlane = new byte[]
        { -128, 127, -128, 127, -128, 127, -128, 127, -128, 127,  // 10
          -128, 127, -128, 127, -128, 127, -128, 127, -128, 127,  // 20
          -128, 127, -128, 127, -128, 127, -128, 127, -128, 127,  // 30
          -128, 127, -128, 127, -128, 127, -128, 127, -128, 127,  // 40
          -128, 127, -128, 127, -128, 127, -128, 127, -128, 127,  // 50
          -128, 127, -128, 127, -128, 127, -128, 127, -128, 127,  // 60
          -128, 127, -128, 127 };                                 // 64
    
    public PixelBuffer createPixelBuffer(Pixels pixels)
        throws IOException
    {
        PixelBuffer pixbuf = new PixelBuffer(getPixelsPath(pixels.getId()),pixels);
        initPixelBuffer(pixbuf);
        
        return pixbuf;
    }
    
    public PixelBuffer getPixelBuffer(Pixels pixels)
    {
        return new PixelBuffer(getPixelsPath(pixels.getId()),pixels);
    }
    
    private void initPixelBuffer(PixelBuffer pixbuf)
        throws IOException
    {
        String path = getPixelsPath(pixbuf.getId());
        recursivelyCreateDirectory( new File(path).getParent());
        byte[] padding = new byte[pixbuf.getPlaneSize() - NULL_PLANE_SIZE];
        FileOutputStream stream = new FileOutputStream(path);
        
        for (int z = 0; z < pixbuf.getSizeZ(); z++)
        {
            for (int c = 0; c < pixbuf.getSizeC(); c++)
            {
                for (int t = 0; t < pixbuf.getSizeT(); t++)
                {
                    stream.write(nullPlane);
                    stream.write(padding);
                }
            }
        }
    }
    
    private void recursivelyCreateDirectory(String path)
    {
    	File dir = new File(path);
    	File parent = new File(dir.getParent());
        if ( ! parent.exists() )
        {
        	recursivelyCreateDirectory(dir.getParent());
        }
        if ( ! dir.exists() )
        {
        	dir.mkdir();
        }
       
    }
}
