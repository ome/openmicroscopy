package ome.io.nio.utests.deltavision;

import java.nio.MappedByteBuffer;

import org.testng.annotations.Test;

import junit.framework.TestCase;

import ome.io.nio.DeltaVision;
import ome.model.core.OriginalFile;

public class BasicIOUnitTest extends TestCase
{
	public DeltaVision getDeltaVisionPixelBuffer()
	{
    	OriginalFile file = new DeltaVisionOriginalFile();
    	DeltaVision dv = new DeltaVision(file);
    	return dv;
	}
	
    @Test(groups={"manual"})
    public void testFirstPlaneOffset() throws Exception
    {
    	DeltaVision dv = getDeltaVisionPixelBuffer();
    	long offset = dv.getPlaneOffset(0, 0, 0);
    	assertEquals(205824, offset);
    }
    
    @Test(groups={"manual"})
    public void testFirstPlaneSize() throws Exception
    {
    	DeltaVision dv = getDeltaVisionPixelBuffer();
    	MappedByteBuffer buf = dv.getPlane(0, 0, 0);
    	assertEquals(131072, buf.capacity());
    }
    
    @Test(groups={"manual"})
    public void testFirstPlaneMd5() throws Exception
    {
    	DeltaVision dv = getDeltaVisionPixelBuffer();
    	MappedByteBuffer buf = dv.getPlane(0, 0, 0);
    	String md = Helper.bytesToHex(Helper.calculateMessageDigest(buf));
    	assertEquals("1fa547fa11e3defe7057f3c88cf3c049", md);
    }
}
