/*
 * org.openmicroscopy.shoola.util.image.roi.TestROI5D
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
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

package org.openmicroscopy.shoola.util.image.roi;


//Java imports

//Third-party libraries
import junit.framework.TestCase;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.math.geom2D.RectangleArea;

/** 
 * Unit test for {@link ROI5D}.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class TestROI5D
    extends TestCase
{

    public void testSetAndGetChannel()
    {
        ROI5D roi5D = new ROI5D(3); //3 channels
        ROI4D roi4D = new ROI4D(1), roi4D1 = new ROI4D(1), roi4D2 = null; 
        roi5D.set(roi4D, 0);
        roi5D.set(roi4D1, 1);
        try {
            roi5D.set(roi4D2, 2);
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage(), roi4D2 == null);
        }
        assertNotNull(roi5D.get(0));
        assertNotNull(roi5D.get(1));
        assertNull(roi5D.get(2));
        assertEquals("Wrong 4D selection", roi4D, roi5D.get(0));
        assertEquals("Wrong 4D selection", roi4D1, roi5D.get(1));
        assertNotSame(roi5D.get(0), roi5D.get(1));
    }   
    
    public void testSetChannel()
    {
        ROI5D roi5D = new ROI5D(1);
        ROI4D roi4D = new ROI4D(1);
        roi5D.setChannel(roi4D, 0);
        assertEquals("Wrong 4D selection", roi4D, roi5D.getChannel(0));
    }
    
    public void testSetAndGetPlaneArea()
    {
        ROI5D roi5D = new ROI5D(1);
        ROI4D roi4D = new ROI4D(1);
        ROI3D roi3D = new ROI3D(2);
        RectangleArea pa = new RectangleArea(0, 0, 1, 1),
                      pa1 = new RectangleArea();
        roi5D.set(roi4D, 0);
        roi4D.set(roi3D, 0);
        roi5D.setPlaneArea(pa, 0, 0, 0);
        roi5D.setPlaneArea(pa1, 1, 0, 0);
        assertNotNull(roi5D.getPlaneArea(0, 0, 0));
        assertNotNull(roi5D.getPlaneArea(1, 0, 0));
        assertNotSame(roi5D.getPlaneArea(0, 0, 0), roi5D.getPlaneArea(1, 0, 0));
        assertEquals("Wrong planeArea", pa, roi5D.getPlaneArea(0, 0, 0));
        assertEquals("Wrong planeArea", pa1, roi5D.getPlaneArea(1, 0, 0));
    }
    
}

