package ome.io.nio.utests;

import ome.io.nio.PixelBuffer;
import ome.io.nio.Pixels;
import junit.framework.TestCase;


public class LargePixelBufferUnitTest extends TestCase
{
    private Pixels pixels;
    private PixelBuffer pixelBuffer;
    private static final int planeSize = 512 * 512 * 2;
    private static final int stackSize = planeSize * 64;
    private static final int timepointSize = stackSize * 3;
    
    protected void setUp()
    {
        pixels = new Pixels();
        
        pixels.dx = 512;
        pixels.dy = 512;
        pixels.dz = 64;
        pixels.dc = 3;
        pixels.dt = 50;
        pixels.bp = 2;

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
