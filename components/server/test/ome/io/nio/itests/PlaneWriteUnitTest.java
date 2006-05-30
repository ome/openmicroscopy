/*
 * ome.io.nio.itests.PlaneIOUnitTest
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */
package ome.io.nio.itests;

import java.io.File;
import java.io.IOException;
import java.nio.MappedByteBuffer;

import org.testng.annotations.Test;

import ome.io.nio.DimensionsOutOfBoundsException;
import ome.io.nio.PixelBuffer;
import ome.io.nio.PixelsService;
import ome.model.core.Pixels;
import ome.server.itests.AbstractManagedContextTest;


/**
 * @author callan
 *
 */
public class PlaneWriteUnitTest extends AbstractManagedContextTest
{
    private PixelsService service;
    private Pixels pixels;
    private PixbufIOFixture baseFixture;
    private PixelBuffer pixbuf;
    private byte[] testPlane;
    private String originalDigest;
    
    private byte[] getTestPlane()
    {
        if (testPlane == null)
        {
            Integer planeSize = pixbuf.getPlaneSize();
            testPlane = new byte[planeSize.intValue()];
            
            for (int i = 0; i < planeSize; i++)
            {
                if (i % 2 == 0)
                    testPlane[i] = -128;
                else
                    testPlane[i] = 126; // Changed to not match PixelService.nullPlane -j.m
            }
            
            byte[] md = Helper.calculateMessageDigest(testPlane);
            originalDigest = Helper.bytesToHex(md);
        }
        
        return testPlane;
    }
    
    @Test
    public void testLowerPlaneWrite ()
        throws IOException, DimensionsOutOfBoundsException
    {
        byte[] testPlane = getTestPlane();
        pixbuf.setPlane(testPlane, 0, 0, 0);
        
        pixbuf = service.getPixelBuffer(pixels);
        MappedByteBuffer plane = pixbuf.getPlane(0, 0, 0);
        assertNotNull(plane);
        byte[] newMD = Helper.calculateMessageDigest(plane);
        
        assertEquals(originalDigest, Helper.bytesToHex(newMD));
    }
    
    @Test
    public void testUpperPlaneWrite ()
        throws IOException, DimensionsOutOfBoundsException
    {
        int z = pixels.getSizeZ() - 1;
        int c = pixels.getSizeC() - 1;
        int t = pixels.getSizeT() - 1;
        
        byte[] testPlane = getTestPlane();
        pixbuf.setPlane(testPlane, z, c, t);
        
        pixbuf = service.getPixelBuffer(pixels);
        MappedByteBuffer plane = pixbuf.getPlane(z, c, t);
        assertNotNull(plane);
        byte[] newMD = Helper.calculateMessageDigest(plane);
        
        assertEquals(originalDigest, Helper.bytesToHex(newMD));
    }
    
    @Override
    protected void onSetUp() throws Exception
    {
        super.onSetUp();
        
        // Create set up the base fixture which sets up the database for us
        baseFixture = new PixbufIOFixture(this.iUpdate);
        pixels = baseFixture.setUp();
        
        // "Our" fixture which creates the planes needed for this test case.
        service = new PixelsService(PixelsService.ROOT_DEFAULT);
        pixbuf = service.createPixelBuffer(pixels);
    }
    
    @Override
    protected void onTearDown() throws Exception
    {
        // Tear down the resources create in this fixture
        String path = pixbuf.getPath();
        File f = new File(path);
        f.delete();

        // Tear down the resources created as part of the base fixture
        baseFixture.tearDown();
        super.onTearDown();
    }
}
