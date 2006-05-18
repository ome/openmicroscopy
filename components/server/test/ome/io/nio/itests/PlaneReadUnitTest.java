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
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;

import org.testng.annotations.Test;

import junit.framework.TestCase;
import ome.io.nio.DimensionsOutOfBoundsException;
import ome.io.nio.PixelBuffer;
import ome.io.nio.PixelsService;
import ome.model.core.Pixels;
import ome.server.itests.AbstractManagedContextTest;


/**
 * @author callan
 *
 */
public class PlaneReadUnitTest extends AbstractManagedContextTest
{
    private Integer planeCount;
    private Integer planeSize;
    private byte[][] originalDigests;
    private String path;
    private Pixels pixels;
    private PixbufIOFixture baseFixture;
    
    private int getDigestOffset(int z, int c, int t)
    {
        int planeCountT = pixels.getSizeZ().intValue() *
                          pixels.getSizeC().intValue();
        
        return (planeCountT * t) + (pixels.getSizeZ() * c) + z;
    }
    
    private String getPlaneCheckErrStr(int z, int c, int t)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Error with plane: ");
        sb.append("Z[");
        sb.append(z);
        sb.append("] ");
        sb.append("C[");
        sb.append(c);
        sb.append("] ");
        sb.append("T[");
        sb.append(t);
        sb.append("].");
        return sb.toString();
    }

    private byte[] createPlane(int planeSize, byte planeNo)
    {
        byte[] plane = new byte[planeSize];
        
        for (int i = 0; i < planeSize; i++)
            plane[i] = planeNo;
        
        return plane;
    }
    
    private void createPlanes() throws IOException
    {
        planeCount = pixels.getSizeZ() * pixels.getSizeC() *
                     pixels.getSizeT();
        // FIXME: *Hack* right now we assume everything is 16-bits wide
        planeSize  = pixels.getSizeX() * pixels.getSizeY() *
                     2;
        path = new PixelsService(PixelsService.ROOT_DEFAULT).getPixelsPath(pixels.getId());
        originalDigests = new byte[planeCount][];
        
        FileOutputStream stream = new FileOutputStream(path);
        
        for (int i = 0; i < planeCount; i++)
        {
            byte[] plane = createPlane(planeSize.intValue(), (byte)(i - 128));
            originalDigests[i] = Helper.calculateMessageDigest(plane);
            stream.write(plane);
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
        createPlanes();
    }
    
    @Test
    public void testInitialPlane()
        throws IOException, DimensionsOutOfBoundsException
    {
        PixelsService service = new PixelsService(PixelsService.ROOT_DEFAULT);
        PixelBuffer pixbuf = service.getPixelBuffer(pixels);
        MappedByteBuffer plane = pixbuf.getPlane(0, 0, 0);

        byte[] messageDigest = Helper.calculateMessageDigest(plane);
        
        assertEquals(Helper.bytesToHex(originalDigests[0]),
                     Helper.bytesToHex(messageDigest));
    }
    
    @Test
    public void testLastPlane()
        throws IOException, DimensionsOutOfBoundsException
    {
        PixelsService service = new PixelsService(PixelsService.ROOT_DEFAULT);
        PixelBuffer pixbuf = service.getPixelBuffer(pixels);
        MappedByteBuffer plane = pixbuf.getPlane(pixels.getSizeZ() - 1,
                                                 pixels.getSizeC() - 1,
                                                 pixels.getSizeT() - 1);
        int digestOffset = getDigestOffset(pixels.getSizeZ() - 1,
                                           pixels.getSizeC() - 1,
                                           pixels.getSizeT() - 1);

        byte[] messageDigest = Helper.calculateMessageDigest(plane);
        
        assertEquals(Helper.bytesToHex(originalDigests[digestOffset]),
                     Helper.bytesToHex(messageDigest));
    }

    @Test
    public void testAllPlanes()
    throws IOException, DimensionsOutOfBoundsException
    {
        PixelsService service = new PixelsService(PixelsService.ROOT_DEFAULT);
        PixelBuffer pixbuf = service.getPixelBuffer(pixels);
        
        String newMessageDigest;
        String oldMessageDigest;
        int digestOffset;
        for (int t = 0; t < pixels.getSizeT(); t++)
        {
            for (int c = 0; c < pixels.getSizeC(); c++)
            {
                for (int z = 0; z < pixels.getSizeZ(); z++)
                {
                    digestOffset = getDigestOffset(z, c, t);
                    MappedByteBuffer plane = pixbuf.getPlane(z, c, t);
                    newMessageDigest = 
                        Helper.bytesToHex(Helper.calculateMessageDigest(plane));
                    oldMessageDigest =
                        Helper.bytesToHex(originalDigests[digestOffset]);
                    
                    assertEquals(getPlaneCheckErrStr(z, c, t),
                                 oldMessageDigest, newMessageDigest);
                }
            }
        }
    }
    
    @Override
    protected void onTearDown() throws Exception
    {
        // Tear down the resources created as part of the base fixture
        baseFixture.tearDown();
        
        // Tear down the resources create in this fixture
        File f = new File(path);
        f.delete();
        
        super.onTearDown();
    }
}
