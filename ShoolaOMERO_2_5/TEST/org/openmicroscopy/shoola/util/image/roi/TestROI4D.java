/*
 * org.openmicroscopy.shoola.util.image.roi.TestROI4D
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
 * Unit test for {@link ROI4D}.
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
public class TestROI4D
    extends TestCase
{
    
    public void testSetAndGetStack()
    {
        ROI4D roi4D = new ROI4D(3); //3 stacks
        ROI3D roi3D1 = new ROI3D(2), roi3D2 = new ROI3D(2), roi3D3 = null;
        roi4D.set(roi3D1, 0);
        roi4D.set(roi3D2, 1);
        try {
            roi4D.set(roi3D3, 2);
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage(), roi3D3 == null);
        }
        assertNotNull(roi4D.get(0));
        assertNotNull(roi4D.get(1));
        assertNull(roi4D.get(2));
        assertEquals("Wrong stack", roi3D1, roi4D.getStack(0));
        assertEquals("Wrong stack", roi3D2, roi4D.getStack(1));
        assertNotSame(roi4D.getStack(0), roi4D.getStack(1));
    }   
   
    public void testSetStack()
    {
        ROI4D roi4D = new ROI4D(1);
        ROI3D roi3D = new ROI3D(2);
        roi4D.setStack(roi3D, 0);
        assertEquals("Wrong stack", roi3D, roi4D.getStack(0));
    }
    
    public void testSetAndGetPlaneArea()
    {
        ROI4D roi4D = new ROI4D(1);
        ROI3D roi3D = new ROI3D(2);
        RectangleArea pa = new RectangleArea(0, 0, 1, 1),
                      pa1 = new RectangleArea();
        roi4D.set(roi3D, 0);
        roi4D.setPlaneArea(pa, 0, 0);
        roi4D.setPlaneArea(pa1, 1, 0);
        assertNotNull(roi4D.getPlaneArea(0, 0));
        assertNotNull(roi4D.getPlaneArea(1, 0));
        assertNotSame(roi4D.getPlaneArea(0, 0), roi4D.getPlaneArea(1, 0));
        assertEquals("Wrong planeArea", pa, roi4D.getPlaneArea(0, 0));
        assertEquals("Wrong planeArea", pa1, roi4D.getPlaneArea(1, 0));
    }
    
}
