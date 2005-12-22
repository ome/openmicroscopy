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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import ome.model.core.Pixels;
import ome.model.enums.PixelsType;


/**
 * @author callan
 *
 */
public class PixelBuffer extends AbstractBuffer
{
    private Pixels pixels;
    private FileChannel channel;
    
    private Integer rowSize;
    private Integer planeSize;
    private Integer stackSize;
    private Integer timepointSize;
    private Integer totalSize;
    
    PixelBuffer (String path, Pixels pixels)
    {
        super(path);
        if (pixels == null)
            throw new NullPointerException(
                    "Expecting a not-null pixels element.");
        
        this.pixels = pixels;
    }
    
    private void checkBounds(Integer y, Integer z, Integer c, Integer t)
        throws DimensionsOutOfBoundsException
    {
        if (y != null && (y > getSizeY() - 1 || y < 0))
            throw new DimensionsOutOfBoundsException("Y '" + y +
                    "' greater than height '" + getSizeY() + "'.");
        
        if (z != null && (z > getSizeZ() - 1 || z < 0))
            throw new DimensionsOutOfBoundsException("Z '" + z +
                    "' greater than height '" + getSizeZ() + "'.");
        
        if (c != null && (c > getSizeC() - 1 || c < 0))
            throw new DimensionsOutOfBoundsException("C '" + c +
                    "' greater than height '" + getSizeC() + "'.");
        
        if (t != null && (t > getSizeT() - 1 || t < 0))
            throw new DimensionsOutOfBoundsException("T '" + t +
                    "' greater than height '" + getSizeT() + "'.");
    }
    
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
    
    public Integer getPlaneSize()
    {
        if (planeSize == null)
            planeSize = getSizeX() * getSizeY() * getByteWidth();

        return planeSize;
    }
    
    public Integer getRowSize()
    {
        if (rowSize == null)
            rowSize = getSizeX() * getByteWidth();
        
        return rowSize;
    }
    
    public Integer getStackSize()
    {
        if (stackSize == null)
            stackSize = getPlaneSize() * getSizeZ();
        
        return stackSize;
    }
    
    public Integer getTimepointSize()
    {
        if (timepointSize == null)
            timepointSize = getStackSize() * getSizeC();
        
        return timepointSize;
    }
    
    public Integer getTotalSize()
    {
        if (totalSize == null)
            totalSize = getTimepointSize() * getSizeT();
        
        return totalSize;
    }
    
    public Long getRowOffset(Integer y, Integer z, Integer c, Integer t)
        throws DimensionsOutOfBoundsException
    {
        checkBounds(y, z, c, t);
        
        Integer rowSize = getRowSize();
        Integer timepointSize = getTimepointSize();
        Integer stackSize = getStackSize();
        Integer planeSize = getPlaneSize();
        
        return ((long)rowSize * y) + ((long)timepointSize * t) +
               ((long)stackSize * c) + ((long)planeSize * z);
    }
    public Long getPlaneOffset(Integer z, Integer c, Integer t)
        throws DimensionsOutOfBoundsException
    {
        checkBounds(null, z, c, t);
        
        Integer timepointSize = getTimepointSize();
        Integer stackSize = getStackSize();
        Integer planeSize = getPlaneSize();
        
        return ((long)timepointSize * t) + ((long)stackSize * c) + 
               ((long)planeSize * z);
    }
    
    public Long getStackOffset(Integer c, Integer t)
        throws DimensionsOutOfBoundsException
    {
        checkBounds(null, null, c, t);
        
        Integer timepointSize = getTimepointSize();
        Integer stackSize = getStackSize();
        
        return ((long) timepointSize * t) + ((long) stackSize * c);
    }
    
    public Long getTimepointOffset(Integer t)
        throws DimensionsOutOfBoundsException
    {
        checkBounds(null, null, null, t);
        
        Integer timepointSize = getTimepointSize();
        
        return ((long) timepointSize * t);
    }
    
    public MappedByteBuffer getRegion(Integer size, Long offset)
        throws IOException
    {
        FileChannel fileChannel = getFileChannel();
        
        /* fileChannel should not be "null" as it will throw an exception if
         * there happens to be an error.
         */
        
        return fileChannel.map(MapMode.READ_ONLY, offset, size);
    }
    
    public MappedByteBuffer getRow(Integer y, Integer z, Integer c, Integer t)
        throws IOException, DimensionsOutOfBoundsException
    {
        Long offset = getRowOffset(y, z, c, t);
        Integer size = getRowSize();
        
        return getRegion(size, offset);
    }
    
    public MappedByteBuffer getPlane(Integer z, Integer c, Integer t)
        throws IOException, DimensionsOutOfBoundsException
    {
        Long offset = getPlaneOffset(z, c, t);
        Integer size = getPlaneSize();
        MappedByteBuffer region = getRegion(size, offset);

        byte[] nullPlane = PixelsService.nullPlane;
        
        for (int i = 0; i < PixelsService.NULL_PLANE_SIZE; i++)
            if (region.get(i) != nullPlane[i])
                return region;
        
        return null;  // All of the nullPlane bytes match, non-filled plane
    }
    
    public MappedByteBuffer getStack(Integer c, Integer t)
        throws IOException, DimensionsOutOfBoundsException
    {
        Long offset = getStackOffset(c, t);
        Integer size = getStackSize();
        
        return getRegion(size, offset);
    }
    
    public MappedByteBuffer getTimepoint(Integer t)
        throws IOException, DimensionsOutOfBoundsException
    {
        Long offset = getTimepointOffset(t);
        Integer size = getTimepointSize();
        
        return getRegion(size, offset);
    }
    
    public void setRegion(Integer size, Long offset, byte[] buffer)
        throws IOException, BufferOverflowException
    {
        FileChannel fileChannel = getFileChannel();
        
        /* fileChannel should not be "null" as it will throw an exception if
         * there happens to be an error.
         */
        
        MappedByteBuffer byteBuffer =
            fileChannel.map(MapMode.READ_WRITE, offset, size);
        
        byteBuffer.put(buffer);
        byteBuffer.force();
        fileChannel.force(false);
    }
    
    public void setRegion(Integer size, Long offset, ByteBuffer buffer)
        throws IOException, BufferOverflowException
    {
        setRegion(size, offset, buffer.array());
    }
    
    public void setRow(ByteBuffer buffer, Integer y, Integer z,
                                          Integer c, Integer t)
        throws IOException, DimensionsOutOfBoundsException, BufferOverflowException
    {
        Long offset = getRowOffset(y, z, c, t);
        Integer size = getRowSize();
        
        setRegion(size, offset, buffer);
    }
    
    public void setPlane(ByteBuffer buffer, Integer z, Integer c, Integer t)
        throws IOException, DimensionsOutOfBoundsException, BufferOverflowException
    {
        Long offset = getPlaneOffset(z, c, t);
        Integer size = getPlaneSize();
        
        setRegion(size, offset, buffer);
    }
    
    public void setPlane(byte[] buffer, Integer z, Integer c, Integer t)
        throws IOException, DimensionsOutOfBoundsException, BufferOverflowException
    {
        Long offset = getPlaneOffset(z, c, t);
        Integer size = getPlaneSize();
    
        setRegion(size, offset, buffer);
    }
    
    public void setStack(ByteBuffer buffer, Integer z, Integer c, Integer t)
        throws IOException, DimensionsOutOfBoundsException, BufferOverflowException
    {
        Long offset = getStackOffset(c, t);
        Integer size = getStackSize();
        
        setRegion(size, offset, buffer);
    }
    
    public void setStack(byte[] buffer, Integer z, Integer c, Integer t)
        throws IOException, DimensionsOutOfBoundsException, BufferOverflowException
    {
        Long offset = getStackOffset(c, t);
        Integer size = getStackSize();

        setRegion(size, offset, buffer);
    }
    
    public void setTimepoint(ByteBuffer buffer, Integer t)
        throws IOException, DimensionsOutOfBoundsException, BufferOverflowException
    {
        Long offset = getTimepointOffset(t);
        Integer size = getTimepointSize();
        
        setRegion(size, offset, buffer);
    }
    
    public void setTimepoint(byte[] buffer, Integer t)
        throws IOException, DimensionsOutOfBoundsException, BufferOverflowException
    {
        Long offset = getTimepointOffset(t);
        Integer size = getTimepointSize();
    
        setRegion(size, offset, buffer);
    }
    
    public byte[] calculateMessageDigest() throws IOException
    {
        MessageDigest md;
        
        try
        {
            md = MessageDigest.getInstance("SHA-1");
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(
                    "Required SHA-1 message digest algorithm unavailable.");
        }
        
        for (int t = 0; t < getSizeT(); t++)
        {
            try
            {
                MappedByteBuffer buffer = getTimepoint(t);
                md.update(buffer);
            }
            catch (DimensionsOutOfBoundsException e)
            {
                // This better not happen. :)
                throw new RuntimeException(e);
            }
        }
        
        return md.digest();
    }
    
    //
    // Delegate methods to ease work with pixels
    //
    
    int getByteWidth ()
    {
        return getBitDepth() / 8;
    }
    
    int getBitDepth()
    {
        return 16;  // FIXME pixels.getPixelsType();
    }

    int getSizeC()
    {
        return pixels.getSizeC();
    }

    int getSizeT()
    {
        return pixels.getSizeT();
    }

    int getSizeX()
    {
        return pixels.getSizeX();
    }

    int getSizeY()
    {
        return pixels.getSizeY();
    }

    int getSizeZ()
    {
        return pixels.getSizeZ();
    }
    
    long getId()
    {
        return pixels.getId();
    }
    
    String getSha1()
    {
        return pixels.getSha1();
    }
}
