/*
 *   Copyright (C) 2009-2011 University of Dundee & Open Microscopy Environment.
 *   All rights reserved.
 *
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package omeis.providers.re.utests;

import ome.io.nio.PixelBuffer;
import ome.io.nio.PixelsService;
import ome.model.core.Pixels;

public class TestPixelsService extends PixelsService
{
	private byte[] dummyPlane;
	
	public TestPixelsService(Pixels pixels)
	{
		super("/tmp");
	}
	
	public void setDummyPlane(byte[] dummyPlane)
	{
		this.dummyPlane = dummyPlane;
	}
	
	@Override
	public PixelBuffer getPixelBuffer(Pixels pixels, boolean write)
	{
		return new TestPixelBuffer(pixels, dummyPlane);
	}
}
