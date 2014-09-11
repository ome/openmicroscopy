/*
 *   Copyright (C) 2009-2011 University of Dundee & Open Microscopy Environment.
 *   All rights reserved.
 *
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package omeis.providers.re.utests;

import omeis.providers.re.data.PlaneDef;

import org.perf4j.LoggingStopWatch;
import org.perf4j.StopWatch;
import org.testng.annotations.Test;

public class TestStandard16BitRendererLUTSizes extends BaseRenderingTest
{
	
	@Override
	protected int getSizeX()
	{
		return 2;
	}
	
	@Override
	protected int getSizeY()
	{
		return 2;
	}
	
	@Override
	protected byte[] getPlane()
	{
		return new byte[] {
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
				(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
				(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
				};
	}
	
	@Test
	public void testPixelValues() throws Exception
	{
		assertEquals(0.0, data.getPixelValue(0));
		assertEquals(0.0, data.getPixelValue(1));
		assertEquals(0.0, data.getPixelValue(2));
		assertEquals(0.0, data.getPixelValue(3));
		assertEquals(Math.pow(2, 16)-1, data.getPixelValue(4));
		assertEquals(Math.pow(2, 16)-1, data.getPixelValue(5));
		assertEquals(Math.pow(2, 16)-1, data.getPixelValue(6));
		assertEquals(Math.pow(2, 16)-1, data.getPixelValue(7));
		try
		{
			assertEquals(0.0, data.getPixelValue(8));
			fail("Should have thrown an IndexOutOfBoundsException.");
		}
		catch (IndexOutOfBoundsException e) { }
	}

	@Test(timeOut=30000)
	public void testRenderAsPackedInt() throws Exception
	{
		PlaneDef def = new PlaneDef(PlaneDef.XY, 0);
		for (int i = 0; i < RUN_COUNT; i++)
		{
			StopWatch stopWatch = 
				new LoggingStopWatch("testRendererAsPackedInt");
			renderer.renderAsPackedInt(def, pixelBuffer);
			stopWatch.stop();
		}
	}
}
