package ome.io.nio.utests.deltavision;

import static org.testng.AssertJUnit.*;

import java.nio.ByteBuffer;

import org.testng.annotations.Test;

import ome.io.nio.DeltaVision;
import ome.model.core.OriginalFile;
import ome.util.Utils;

public class EightBitBasicIOUnitTest
{
	private static final String path = 
		"/Users/callan/testimages/22jul05_rhum_start01_02_R3D_D3D_VOL.dv";
	
	public DeltaVision getDeltaVisionPixelBuffer()
	{
    	OriginalFile file = new DeltaVisionOriginalFile(path);
    	DeltaVision dv = new DeltaVision(file.getPath(), file);
    	return dv;
	}
	
    @Test(groups={"manual"})
    public void testFirstPlaneOffset() throws Exception
    {
    	DeltaVision dv = getDeltaVisionPixelBuffer();
    	long offset = dv.getPlaneOffset(0, 0, 0);
    	assertEquals(2048, offset);
    }
    
    @Test(groups={"manual"})
    public void testFirstPlaneSize() throws Exception
    {
    	DeltaVision dv = getDeltaVisionPixelBuffer();
    	ByteBuffer buf = dv.getPlane(0, 0, 0).getData();
    	assertEquals(541696, buf.capacity());
    }
    
    @Test(groups={"manual"})
    public void testFirstPlaneMd5() throws Exception
    {
    	DeltaVision dv = getDeltaVisionPixelBuffer();
    	ByteBuffer buf = dv.getPlane(0, 0, 0).getData();
    	String md = Utils.bytesToHex(Utils.calculateMessageDigest(buf));
    	assertEquals("860dc15d50bfa08fe27f84e3a5ed937a", md);
    }
    
    @Test(groups={"manual"})
    public void testFirstPlaneReorderedMd5() throws Exception
    {
    	DeltaVision dv = getDeltaVisionPixelBuffer();
    	byte[] buf = new byte[541696];
    	buf = dv.getPlaneDirect(0, 0, 0, buf);
    	String md = Utils.bytesToHex(Utils.calculateMessageDigest(buf));
    	assertEquals("c52bc92efc138533fbf9b4f7469ffad0", md);
    }
    
    @Test(groups={"manual"})
    public void testFirstPlaneRegionMd5() throws Exception
    {
    	DeltaVision dv = getDeltaVisionPixelBuffer();
    	byte[] buf = new byte[8];
    	buf = dv.getPlaneRegionDirect(0, 0, 0, 8, 32, buf);
    	String md = Utils.bytesToHex(Utils.calculateMessageDigest(buf));
    	assertEquals("7dea362b3fac8e00956a4952a3d4f474", md);
    }
    
    @Test(groups={"manual"})
    public void testSecondPlaneRegionMd5() throws Exception
    {
    	DeltaVision dv = getDeltaVisionPixelBuffer();
    	byte[] buf = new byte[8];
    	buf = dv.getPlaneRegionDirect(0, 0, 0, 8, 128, buf);
    	String md = Utils.bytesToHex(Utils.calculateMessageDigest(buf));
    	// Identical to testFirstPlaneRegionMd5() because of the large number
    	// of padding zeros at the top and bottom of the image.
    	assertEquals("7dea362b3fac8e00956a4952a3d4f474", md);
    }
}
