/*
 * ome.io.nio.utests.OutOfBoundsUnitTest
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
package ome.io.nio.utests;

import junit.framework.TestCase;
import ome.io.nio.DimensionsOutOfBoundsException;
import ome.io.nio.PixelBuffer;
import ome.io.nio.PixelsService;
import ome.model.core.Pixels;
import ome.model.enums.PixelsType;


/**
 * @author callan
 *
 */
public class OutOfBoundsUnitTest extends TestCase
{
    private Pixels pixels;
    private PixelBuffer pixelBuffer;

    protected void setUp()
    {
        pixels = new Pixels();
        
        pixels.setId(1L);
        pixels.setSizeX(256);
        pixels.setSizeY(256);
        pixels.setSizeZ(64);
        pixels.setSizeC(3);
        pixels.setSizeT(50);
        
        PixelsType type = new PixelsType();
        pixels.setPixelsType(type); // FIXME
        
        PixelsService service = new PixelsService(PixelsService.ROOT_DEFAULT);
        pixelBuffer = service.getPixelBuffer(pixels);
    }
    
    public void testYUpperBounds()
    {
        try
        {
            pixelBuffer.getRowOffset(256, 0, 0, 0);
        }
        catch (DimensionsOutOfBoundsException e)
        {
            return;
        }
        
        fail("Out of bounds dimension was not caught.");
    }

    public void testZUpperBounds()
    {
        try
        {
            pixelBuffer.getPlaneOffset(64, 0, 0);
        }
        catch (DimensionsOutOfBoundsException e)
        {
            return;
        }
        
        fail("Out of bounds dimension was not caught.");
    }
    
    public void testCUpperBounds()
    {
        try
        {
            pixelBuffer.getStackOffset(3, 0);
        }
        catch (DimensionsOutOfBoundsException e)
        {
            return;
        }
        
        fail("Out of bounds dimension was not caught.");
    }
    
    public void testTUpperBounds()
    {
        try
        {
            pixelBuffer.getTimepointOffset(50);
        }
        catch (DimensionsOutOfBoundsException e)
        {
            return;
        }
        
        fail("Out of bounds dimension was not caught.");
    }
    
    public void testYLowerBounds()
    {
        try
        {
            pixelBuffer.getRowOffset(-1, 0, 0, 0);
        }
        catch (DimensionsOutOfBoundsException e)
        {
            return;
        }
        
        fail("Out of bounds dimension was not caught.");
    }

    public void testZLowerBounds()
    {
        try
        {
            pixelBuffer.getPlaneOffset(-1, 0, 0);
        }
        catch (DimensionsOutOfBoundsException e)
        {
            return;
        }
        
        fail("Out of bounds dimension was not caught.");
    }
    
    public void testCLowerBounds()
    {
        try
        {
            pixelBuffer.getStackOffset(-1, 0);
        }
        catch (DimensionsOutOfBoundsException e)
        {
            return;
        }
        
        fail("Out of bounds dimension was not caught.");
    }
    
    public void testTLowerBounds()
    {
        try
        {
            pixelBuffer.getTimepointOffset(-1);
        }
        catch (DimensionsOutOfBoundsException e)
        {
            return;
        }
        
        fail("Out of bounds dimension was not caught.");
    }
}
