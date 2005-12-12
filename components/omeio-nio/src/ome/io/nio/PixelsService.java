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

import java.io.IOException;
import java.io.RandomAccessFile;

import ome.model.core.Pixels;


/**
 * @author callan
 *
 */
public class PixelsService
{
    private static PixelsService soleInstance;
    
    public static PixelsService getInstance()
    {
        if (soleInstance == null)
            soleInstance = new PixelsService();
        
        return soleInstance;
    }

    public PixelBuffer createPixelBuffer(Pixels pixels)
        throws IOException
    {
        Integer id = Helper.getNextPixelsId();

        pixels.setId(id);
        
        PixelBuffer pixbuf = new PixelBuffer(pixels);
        initPixelBuffer(pixbuf);
        
        return pixbuf;
    }
    
    public PixelBuffer getPixelBuffer(Pixels pixels)
    {
        return new PixelBuffer(pixels);
    }
    
    private void initPixelBuffer(PixelBuffer pixbuf)
        throws IOException
    {
        Integer size = pixbuf.getTotalSize();
        String path = Helper.getPixelsPath(pixbuf.getImageServerId());
        RandomAccessFile file = new RandomAccessFile(path, "w");
        
        file.seek(size);
        file.write(new byte[]{0});
    }
}
