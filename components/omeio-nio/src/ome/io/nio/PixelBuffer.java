/*
 * ome.io.nio.PixelBuffer
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;


/**
 * @author callan
 *
 */
public class PixelBuffer
{
    Integer id;
    String path;
    Pixels pixels;
    FileChannel roChannel;
    FileChannel woChannel;
    
    Integer planeSize;
    Integer stackSize;
    Integer timepointSize;
    
    public PixelBuffer (Integer id, Pixels pixels)
    {
        if (id == null)
            throw new NullPointerException("Expecting a not-null id.");
        
        this.pixels = pixels;
        this.id = id;
        path = Helper.getPixelsPath(id);
    }
    
    private FileChannel getFileChannel(Boolean readOnly)
        throws FileNotFoundException
    {
        if (readOnly == true)
        {
            if (roChannel == null)
            {
                FileOutputStream stream = new FileOutputStream(path);
                roChannel = stream.getChannel();
                
                return roChannel;
            }
            else
                return roChannel;
            
        }

        if (woChannel != null)
            return woChannel;
        
        FileInputStream stream = new FileInputStream(path);
        woChannel = stream.getChannel();
        
        return woChannel;
    }
    
    public Integer getPlaneSize()
    {
        if (planeSize == null)
            planeSize = pixels.dx *pixels.dy * pixels.bp;

        return planeSize;
    }
    
    public Integer getStackSize()
    {
        if (stackSize == null)
            stackSize = getPlaneSize() * pixels.dz;
        
        return stackSize;
    }
    
    public Integer getTimepointSize()
    {
        if (timepointSize == null)
            timepointSize = getStackSize() * pixels.dc;
        
        return timepointSize;
    }
    
    public Long getPlaneOffset(Integer z, Integer c, Integer t)
    {
        Integer timepointSize = getTimepointSize();
        Integer stackSize = getStackSize();
        Integer planeSize = getPlaneSize();
        
        return ((long)timepointSize * t) + ((long)stackSize * c) + 
               ((long)planeSize * z);
    }
    
    public Long getStackOffset(Integer c, Integer t)
    {
        Integer timepointSize = getTimepointSize();
        Integer stackSize = getStackSize();
        
        return ((long) timepointSize * t) + ((long) stackSize * c);
    }
    
    public Long getTimepointOffset(Integer t)
    {
        Integer timepointSize = getTimepointSize();
        
        return ((long) timepointSize * t);
    }
    
    public MappedByteBuffer getRegion(Integer size, Long offset)
        throws IOException
    {
        FileChannel fileChannel = getFileChannel(true);
        
        /* fileChannel should not be "null" as it will throw an exception if
         * there happens to be an error.
         */
        
        return fileChannel.map(MapMode.READ_ONLY, size, offset);
    }
    
    public MappedByteBuffer getPlane(Integer z, Integer c, Integer t)
        throws IOException
    {
        
        Long offset = getPlaneOffset(z, c, t);
        Integer size = getPlaneSize();

        return getRegion(size, offset);
    }
    
    public MappedByteBuffer getStack(Integer c, Integer t) throws IOException
    {
        Long offset = getStackOffset(c, t);
        Integer size = getStackSize();
        
        return getRegion(size, offset);
    }
    
    public MappedByteBuffer getTimepoint(Integer t) throws IOException
    {
        Long offset = getTimepointOffset(t);
        Integer size = getTimepointSize();
        
        return getRegion(size, offset);
    }
    
    public void setRegion(Integer size, Long offset, byte[] buffer)
        throws IOException
    {
        FileChannel fileChannel = getFileChannel(false);
        
        /* fileChannel should not be "null" as it will throw an exception if
         * there happens to be an error.
         */
        
        MappedByteBuffer byteBuffer =
            fileChannel.map(MapMode.READ_ONLY, size, offset);
        
        byteBuffer.put(buffer);
        byteBuffer.force();
    }
    
    public void setRegion(Integer size, Long offset, ByteBuffer buffer)
        throws IOException
    {
        setRegion(size, offset, buffer.array());
    }
}
