package ome.io.nio.utests.deltavision;

import static org.testng.AssertJUnit.*;

import java.nio.ByteBuffer;

import org.testng.annotations.Test;

import ome.io.nio.DeltaVision;
import ome.model.core.OriginalFile;
import ome.util.Utils;

public class BasicIOUnitTest
{
	public DeltaVision getDeltaVisionPixelBuffer()
	{
    	OriginalFile file = new DeltaVisionOriginalFile();
    	DeltaVision dv = new DeltaVision(file.getPath(), file);
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
    	ByteBuffer buf = dv.getPlane(0, 0, 0).getData();
    	assertEquals(131072, buf.capacity());
    }
    
    @Test(groups={"manual"})
    public void testFirstPlaneMd5() throws Exception
    {
    	DeltaVision dv = getDeltaVisionPixelBuffer();
    	ByteBuffer buf = dv.getPlane(0, 0, 0).getData();
    	String md = Utils.bytesToHex(Utils.calculateMessageDigest(buf));
    	assertEquals("1fa547fa11e3defe7057f3c88cf3c049", md);
    }
    
    @Test(groups={"manual"})
    public void testFirstPlaneReorderedMd5() throws Exception
    {
    	DeltaVision dv = getDeltaVisionPixelBuffer();
    	byte[] buf = new byte[131072];
    	buf = dv.getPlaneDirect(0, 0, 0, buf);
    	String md = Utils.bytesToHex(Utils.calculateMessageDigest(buf));
    	assertEquals("011fd6a06763b8c21dd5de0ece65baa7", md);
    }
    
    @Test(groups={"manual"})
    public void testFirstPlaneRegionMd5() throws Exception
    {
    	DeltaVision dv = getDeltaVisionPixelBuffer();
    	byte[] buf = new byte[16];
    	buf = dv.getPlaneRegionDirect(0, 0, 0, 8, 32, buf);
    	String md = Utils.bytesToHex(Utils.calculateMessageDigest(buf));
    	assertEquals("66a6bc285b8354c7fea1fa745a642ecc", md);
    }
    
    @Test(groups={"manual"})
    public void testSecondPlaneRegionMd5() throws Exception
    {
    	DeltaVision dv = getDeltaVisionPixelBuffer();
    	byte[] buf = new byte[16];
    	buf = dv.getPlaneRegionDirect(0, 0, 0, 8, 128, buf);
    	String md = Utils.bytesToHex(Utils.calculateMessageDigest(buf));
    	assertEquals("d5fbe14c0be849f9fc49f5758ba5f35a", md);
    }
}
