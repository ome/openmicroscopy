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
import java.nio.ByteBuffer;

import ome.io.nio.DimensionsOutOfBoundsException;
import ome.io.nio.PixelBuffer;
import ome.io.nio.PixelsService;
import ome.model.core.Pixels;
import ome.server.itests.AbstractManagedContextTest;

import junit.framework.TestCase;


/**
 * @author callan
 *
 */
public class PixbufCreationUnitTest extends AbstractManagedContextTest
{
    Pixels pixels;
    PixbufIOFixture baseFixture;
    PixelBuffer pixbuf;
    
    public void testValidPixbuf() throws IOException
    {
        String validSHA1 = "caf922a2e71828fd287b75ef30d9bf1c918c96e2";
        
        byte[] md = pixbuf.calculateMessageDigest();
        assertEquals(validSHA1, Helper.bytesToHex(md));
    }
    
    public void testNullPlanes()
        throws IOException, DimensionsOutOfBoundsException
    {
        for (int t = 0; t < pixels.getSizeT(); t++)
        {
            for (int c = 0; c < pixels.getSizeC(); c++)
            {
                for (int z = 0; z < pixels.getSizeZ(); z++)
                {
                    ByteBuffer buf = pixbuf.getPlane(z, c, t);
                    assertNull(buf);
                }
            }
        }
    }
    
    @Override
    protected void onSetUp() throws Exception
    {
        super.onSetUp();

        // Create set up the base fixture which sets up the database for us
        baseFixture = new PixbufIOFixture(this.iUpdate);
        pixels = baseFixture.setUp();
        
        // "Our" fixture which creates the planes needed for this test case.
        PixelsService service = new PixelsService(PixelsService.ROOT_DEFAULT);
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
