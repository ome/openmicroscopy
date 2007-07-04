/*
 *   $Id: HelperUnitTest.java 1489 2007-04-26 14:48:50Z david $
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.io.nio.utests.deltavision;

import java.nio.MappedByteBuffer;

import org.testng.annotations.*;

import junit.framework.TestCase;
import ome.io.nio.DeltaVision;
import ome.io.nio.DeltaVisionHeader;
import ome.model.core.OriginalFile;

public class DeltaVisionZWTUnitTest extends TestCase
{
	private DeltaVision getDeltaVisionPixelBuffer()
	{
    	OriginalFile file = new DeltaVisionOriginalFile();
    	DeltaVision dv = new DeltaVision(file);
    	dv.setSequence(DeltaVisionHeader.ZWT_SEQUENCE);
    	return dv;
	}
    
    @Test
    public void testFirstPlaneSecondTimepointFirstChannelMd5() throws Exception
    {
    	DeltaVision dv = getDeltaVisionPixelBuffer();
    	MappedByteBuffer buf = dv.getPlane(0, 0, 1);
    	String md = Helper.bytesToHex(Helper.calculateMessageDigest(buf));
    	assertEquals("9618bf50881340fbc925abb3b458de2a", md);
    }

    @Test
    public void testFirstPlaneSecondChannelMd5() throws Exception
    {
    	DeltaVision dv = getDeltaVisionPixelBuffer();
    	MappedByteBuffer buf = dv.getPlane(0, 1, 0);
    	String md = Helper.bytesToHex(Helper.calculateMessageDigest(buf));
    	assertEquals("a2c8e1551c3a5857c005223a98330842", md);
    }
    
    @Test
    public void testSecondPlaneFirstChannelMd5() throws Exception
    {
    	DeltaVision dv = getDeltaVisionPixelBuffer();
    	MappedByteBuffer buf = dv.getPlane(1, 0, 0);
    	String md = Helper.bytesToHex(Helper.calculateMessageDigest(buf));
    	assertEquals("73ab6431bca5f102882f956162d30d3b", md);
    }
    
    @Test
    public void testLastPlaneMd5() throws Exception
    {
    	DeltaVision dv = getDeltaVisionPixelBuffer();
    	MappedByteBuffer buf = dv.getPlane(dv.getSizeZ() - 1,
    	                                   dv.getSizeC() - 1,
    	                                   dv.getSizeT() - 1);
    	String md = Helper.bytesToHex(Helper.calculateMessageDigest(buf));
    	assertEquals("a78c365ab044e179b6cae0b6150df3a4", md);
    }
}
