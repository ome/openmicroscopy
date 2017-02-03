/*
 *   Copyright (C) 2009-2011 University of Dundee & Open Microscopy Environment.
 *   All rights reserved.
 *
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.io.nio.utests;

import org.testng.Assert;

import java.nio.ByteBuffer;

import ome.util.PixelData;

/**
 * Integration tests of the PixelData implementation for the bit pixel type.
 * @author Chris Allan <callan at blackcat dot ca>
 */
public class PixelDataBitTest
{
	private PixelData data;

    public void setUp()
    {
    	byte[] byteArray = new byte[] { (byte) 0xFF, (byte) 193 }; 
	data = new PixelData("bit", ByteBuffer.wrap(byteArray));
    }
 
    public void testGetBits()
    {
        Assert.assertEquals(data.getPixelValue(0), 1.0, 0.0);  // Start byte 1
        Assert.assertEquals(data.getPixelValue(1), 1.0, 0.0);
        Assert.assertEquals(data.getPixelValue(2), 1.0, 0.0);
        Assert.assertEquals(data.getPixelValue(3), 1.0, 0.0);
        Assert.assertEquals(data.getPixelValue(4), 1.0, 0.0);
        Assert.assertEquals(data.getPixelValue(5), 1.0, 0.0);
        Assert.assertEquals(data.getPixelValue(6), 1.0, 0.0);
        Assert.assertEquals(data.getPixelValue(7), 1.0, 0.0);
        Assert.assertEquals(data.getPixelValue(8), 1.0, 0.0);  // Start byte 2 
        Assert.assertEquals(data.getPixelValue(9), 1.0, 0.0);
        Assert.assertEquals(data.getPixelValue(10), 0.0, 0.0);
        Assert.assertEquals(data.getPixelValue(11), 0.0, 0.0);
        Assert.assertEquals(data.getPixelValue(12), 0.0, 0.0);
        Assert.assertEquals(data.getPixelValue(13), 0.0, 0.0);
        Assert.assertEquals(data.getPixelValue(14), 0.0, 0.0);
        Assert.assertEquals(data.getPixelValue(15), 1.0, 0.0);
    }
    
    public void testSetAndGetBits()
    {
    	// 0
        Assert.assertEquals(data.getPixelValue(0), 1.0, 0.0);
    	data.setPixelValue(0, 0.0);
    	Assert.assertEquals(data.getPixelValue(0), 0.0, 0.0);
    	// 7
    	Assert.assertEquals(data.getPixelValue(7), 1.0, 0.0);
    	data.setPixelValue(7, 0.0);
    	Assert.assertEquals(data.getPixelValue(7), 0.0, 0.0);
    	// 8
    	Assert.assertEquals(data.getPixelValue(8), 1.0, 0.0);
    	data.setPixelValue(8, 1.0);
    	Assert.assertEquals(data.getPixelValue(8), 1.0, 0.0);
    	// 14
    	Assert.assertEquals(data.getPixelValue(14), 0.0, 0.0);
    	data.setPixelValue(14, 1.0);
    	Assert.assertEquals(data.getPixelValue(14), 1.0, 0.0);
    }
    
    public void testSize()
    {
        Assert.assertEquals(data.size(), 2);
    }
}
