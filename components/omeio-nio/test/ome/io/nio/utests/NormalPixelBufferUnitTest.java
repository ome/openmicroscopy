package ome.io.nio.utests;

import ome.io.nio.PixelBuffer;
import ome.model.core.Pixels;
import ome.model.enums.PixelsType;

import junit.framework.TestCase;


public class NormalPixelBufferUnitTest extends TestCase
{
    private Pixels pixels;
    private PixelBuffer pixelBuffer;
    private static final int planeSize = 256 * 256 * 2;
    private static final int stackSize = planeSize * 64;
    private static final int timepointSize = stackSize * 3;
    
    protected void setUp()
    {
        pixels = new Pixels();
        
        pixels.setSizeX(256);
        pixels.setSizeY(256);
        pixels.setSizeZ(64);
        pixels.setSizeC(3);
        pixels.setSizeT(50);
        
        PixelsType type = new PixelsType();
        pixels.setPixelsType(type); // FIXME

        pixelBuffer = new PixelBuffer(1, pixels);
    }
    
    public void testGetPlaneSize()
    {
        assertEquals(pixelBuffer.getPlaneSize().intValue(), planeSize);
    }
    
    public void testGetStackSize()
    {
        assertEquals(pixelBuffer.getStackSize().intValue(), stackSize);
    }
    
    public void testGetTimepointSize()
    {
        assertEquals(pixelBuffer.getTimepointSize().intValue(), timepointSize);
    }
    
    public void testGetInitialPlaneOffset()
    {
        assertEquals(pixelBuffer.getPlaneOffset(0, 0, 0).longValue(), 0L);
    }
    
    public void testGetPlaneOffset1()
    {
        long offset = ((long)timepointSize * 25) + ((long)planeSize * 25);
        assertEquals(pixelBuffer.getPlaneOffset(25, 0, 25).longValue(), offset);
    }
    
    public void testGetPlaneOffset2()
    {
        long offset = ((long)timepointSize * 25) + ((long)stackSize * 1) +
                      ((long)planeSize * 25);
        assertEquals(pixelBuffer.getPlaneOffset(25, 1, 25).longValue(), offset);
    }
}
