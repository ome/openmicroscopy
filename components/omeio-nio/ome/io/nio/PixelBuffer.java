package ome.io.nio;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;


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
    
    private Integer getPlaneSize()
    {
        if (planeSize == null)
            planeSize = pixels.dx *pixels.dy * pixels.bp;

        return planeSize;
    }
    
    private Integer getStackSize()
    {
        if (stackSize == null)
            stackSize = getPlaneSize() * pixels.dz;
        
        return stackSize;
    }
    
    private Integer getTimepointSize()
    {
        if (timepointSize == null)
            timepointSize = getStackSize() * pixels.dc;
        
        return timepointSize;
    }
    
    private Long getPlaneOffset(Integer z, Integer c, Integer t)
    {
        Integer timepointSize = getTimepointSize();
        Integer stackSize = getStackSize();
        Integer planeSize = getPlaneSize();
        
        return new Long((timepointSize * t) + (stackSize * c) + 
                        (planeSize * z));
    }
    
    private Long getStackOffset(Integer c, Integer t)
    {
        Integer timepointSize = getTimepointSize();
        Integer stackSize = getStackSize();
        
        return new Long((timepointSize * t) + (stackSize * c));
    }
    
    private Long getTimepointOffset(Integer t)
    {
        Integer timepointSize = getTimepointSize();
        
        return new Long((timepointSize * t));
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
}
