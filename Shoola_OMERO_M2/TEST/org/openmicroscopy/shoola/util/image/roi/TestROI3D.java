/*
 * org.openmicroscopy.shoola.util.image.roi.TestROI3D
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
import org.openmicroscopy.shoola.util.math.geom2D.PlaneArea;
import org.openmicroscopy.shoola.util.math.geom2D.RectangleArea;

/** 
 * Unit test for {@link ROI3D}.
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
public class TestROI3D
    extends TestCase
{

    public void testSetPlaneAreaAndGetPlaneArea()
    {
        ROI3D roi3D = new ROI3D(2);
        RectangleArea pa1 = new RectangleArea(0, 0, 1, 1),
                      pa2 = new RectangleArea(0, 0, 10, 10);
        roi3D.setPlaneArea(pa1, 0);
        roi3D.setPlaneArea(pa2, 1);
        assertNotNull(roi3D.getPlaneArea(0));
        assertNotNull(roi3D.getPlaneArea(1));
        assertEquals("Wrong planeArea", pa1, roi3D.getPlaneArea(0));
        assertEquals("Wrong planeArea", pa2, roi3D.getPlaneArea(1));
        assertNotSame("PlaneArea shouldn't be the same", roi3D.getPlaneArea(0),
                    roi3D.getPlaneArea(1));
    }
    
    public void testSetPlaneAreaAndGetPlaneAreaNull()
    {
        ROI3D roi3D = new ROI3D(1);
        roi3D.setPlaneArea(null, 0);
        assertNull(roi3D.getPlaneArea(0));
    }
    
    public void testSet()
    {
        ROI3D roi3D = new ROI3D(3), roi3D1 = new ROI3D(1);
        RectangleArea pa = new RectangleArea(0, 0, 1, 1);
        try {
            roi3D.set(null, 0); 
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage(), roi3D.get(0) == null);
        }
        try {
            roi3D.set(pa, 1); 
        } catch (IllegalArgumentException e) {
            assertFalse(roi3D.get(1) instanceof PlaneArea);
        }
        try {
            roi3D.set(roi3D1, 2);
        } catch (IllegalArgumentException e) {
            assertFalse(roi3D.get(2) instanceof PlaneArea);
        }
        assertNull(roi3D.getPlaneArea(0));
        assertNotNull(roi3D.getPlaneArea(1));
    }
    
}
