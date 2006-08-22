/*
 * org.openmicroscopy.shoola.util.math.geom2D.TestPlanePoint
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

package org.openmicroscopy.shoola.util.math.geom2D;


//Java imports

//Third-party libraries
import junit.framework.TestCase;

//Application-internal dependencies

/** 
 * Unit test for {@link PlanePoint}. 
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
public class TestPlanePoint
    extends TestCase
{

    private static final int    MAX_ITER = 30000;  //Max iterations in a test.
    
    
    public void testPoint()
    {
        PlanePoint p = new PlanePoint(Integer.MIN_VALUE, Integer.MAX_VALUE);
        assertEquals("Should set x1 to argument passed to constructor.", 
                Integer.MIN_VALUE, p.x1, 0);
        assertEquals("Should set x2 to argument passed to constructor.", 
                Integer.MAX_VALUE, p.x2, 0);
    }
    
    public void testDistanceNull()
    {
        PlanePoint p = new PlanePoint(0, 0);
        try {
            p.distance(null);
            fail("Shouldn't accept null.");
        } catch (NullPointerException npe) {
            //Ok, expected.
        }
    }
    
    public void testDistanceSqrt2()
    {
        double sqrt2 = Math.sqrt(2);
        PlanePoint p, q;
        for (int i = -MAX_ITER/2; i < MAX_ITER/2; ++i) {
            p = new PlanePoint(0+i, 1+i);
            q = new PlanePoint(1+i, 0+i);
            assertEquals("Wrong distance [i = "+i+"].", 
                    sqrt2, p.distance(q), 0);
        }
    }
    
    public void testDisance1()
    {
        PlanePoint p, q;
        for (int i = -MAX_ITER/2; i < MAX_ITER/2; ++i) {
            p = new PlanePoint(0+i, 0+i);
            q = new PlanePoint(0+i, 1+i);
            assertEquals("Wrong distance [i = "+i+"].", 
                    1, p.distance(q), 0);
        }
    }
    
    public void testDisance2()
    {
        PlanePoint p, q;
        for (int i = -MAX_ITER/2; i < MAX_ITER/2; ++i) {
            p = new PlanePoint(0+i, 0+i);
            q = new PlanePoint(2+i, 0+i);
            assertEquals("Wrong distance [i = "+i+"].", 
                    2, p.distance(q), 0);
        }
    }
    
    public void testSumNull()
    {
        PlanePoint p = new PlanePoint(0, 0);
        try {
            p.sum(null);
            fail("Shouldn't accept null.");
        } catch (NullPointerException npe) {
            //Ok, expected.
        }
    }
    
    public void testSum()
    {
        PlanePoint p, q;
        for (int i = -MAX_ITER/2; i < MAX_ITER/2; ++i) {
            p = new PlanePoint(1234.5678+i, -8765.4321+i);
            q = new PlanePoint(2+i, 0+i);
            assertEquals("Wrong sum [i = "+i+"].", 
                    new PlanePoint(1234.5678+i+2+i, -8765.4321+i+i), p.sum(q));
        }
    }
    
    public void testDiffNull()
    {
        PlanePoint p = new PlanePoint(0, 0);
        try {
            p.diff(null);
            fail("Shouldn't accept null.");
        } catch (NullPointerException npe) {
            //Ok, expected.
        }
    }
    
    public void testDiff()
    {
        PlanePoint p, q, diff = new PlanePoint(1234-2, -8765);
        for (int i = -MAX_ITER/2; i < MAX_ITER/2; ++i) {
            p = new PlanePoint(1234+i, -8765+i);
            q = new PlanePoint(2+i, 0+i);
            assertEquals("Wrong sum [i = "+i+"].", 
                    diff , p.diff(q));
        }
    }
    
    public void testScalarInteger()
    {
        PlanePoint p;
        for (int i = -MAX_ITER/2; i < MAX_ITER/2; ++i) {
            p = new PlanePoint(i, -i);
            assertEquals("Wrong scalar multiplication [i = "+i+"].", 
                    new PlanePoint(7*i, -7*i) , p.scalar(7));
        }
    }
    
    public void testScalarDouble()
    {
        PlanePoint p;
        for (int i = -MAX_ITER/2; i < MAX_ITER/2; ++i) {
            p = new PlanePoint(i, -i);
            assertEquals("Wrong scalar multiplication [i = "+i+"].", 
                    new PlanePoint(Math.PI*i, -Math.PI*i) , p.scalar(Math.PI));
        }
    }
    
    public void testVecNull()
    {
        PlanePoint p = new PlanePoint(0, 0);
        try {
            p.vec(null);
            fail("Shouldn't accept null.");
        } catch (NullPointerException npe) {
            //Ok, expected.
        }
    }
    
    public void testVec()
    {
        PlanePoint p, q, diff = new PlanePoint(2-1234, 8765);
        for (int i = -MAX_ITER/2; i < MAX_ITER/2; ++i) {
            p = new PlanePoint(1234+i, -8765+i);
            q = new PlanePoint(2+i, 0+i);
            assertEquals("Wrong vec [i = "+i+"].", 
                    diff , p.vec(q));
        }
    }
    
    public void testDotNull()
    {
        PlanePoint p = new PlanePoint(0, 0);
        try {
            p.dot(null);
            fail("Shouldn't accept null.");
        } catch (NullPointerException npe) {
            //Ok, expected.
        }
    }
    
    public void testDot()
    {
        PlanePoint p, q;
        for (int i = -MAX_ITER/2; i < MAX_ITER/2; ++i) {
            p = new PlanePoint(1+i, -8+i);
            q = new PlanePoint(2+i, 0+i);
            assertEquals("Wrong dot [i = "+i+"].", 
                    (1+i)*(2+i) + (-8+i)*i, p.dot(q), 0);
        }
    }
    
    public void testNormSqrt2()
    {
        double sqrt2 = Math.sqrt(2);
        PlanePoint p, q;
        for (int i = -MAX_ITER/2; i < MAX_ITER/2; ++i) {
            p = new PlanePoint(0+i, 1+i);
            q = new PlanePoint(1+i, 0+i);
            assertEquals("Wrong norm [i = "+i+"].", 
                    sqrt2, p.vec(q).norm(), 0);
        }
    }
    
    public void testNorm1()
    {
        PlanePoint p, q;
        for (int i = -MAX_ITER/2; i < MAX_ITER/2; ++i) {
            p = new PlanePoint(0+i, 0+i);
            q = new PlanePoint(0+i, 1+i);
            assertEquals("Wrong norm [i = "+i+"].", 
                    1, p.vec(q).norm(), 0);
        }
    }
    
    public void testNorm2()
    {
        PlanePoint p, q;
        for (int i = -MAX_ITER/2; i < MAX_ITER/2; ++i) {
            p = new PlanePoint(0+i, 0+i);
            q = new PlanePoint(2+i, 0+i);
            assertEquals("Wrong norm [i = "+i+"].", 
                    2, p.vec(q).norm(), 0);
        }
    }
    
    public void testNormalize()
    {
        PlanePoint p, u;
        double n;
        for (int i = 1; i < MAX_ITER/2; ++i) {
            p = new PlanePoint(0+i, 0+i);
            n = Math.sqrt(2*i*i);
            u = new PlanePoint(i/n, i/n);
            assertEquals("Wrong unit vector [i = "+i+"].", 
                    u, p.normalize());
        }
        
        p = new PlanePoint(0, 0);
        assertEquals("Null vector can't be normalized.", p, p.normalize());
        
        u = new PlanePoint(0, -1);
        for (int i = -MAX_ITER/2; i < 0; ++i) {
            p = new PlanePoint(0, 0+i);
            assertEquals("Wrong unit vector [i = "+i+"].", 
                    u, p.normalize());
        }
    }
    
    public void testAngleNull()
    {
        PlanePoint p = new PlanePoint(0, 0);
        try {
            p.angle(null);
            fail("Shouldn't accept null.");
        } catch (NullPointerException npe) {
            //Ok, expected.
        }
    }
    
    public void testAngleNullVector()
    {
        PlanePoint p = new PlanePoint(0, 0);
        try {
            p.angle(p);
            fail("Angle is not defined for a null vector.");
        } catch (IllegalArgumentException iae) {
            //Ok, expected.
        }
        try {
            p.angle(new PlanePoint(1, 1));
            fail("Angle is not defined for a null vector.");
        } catch (IllegalArgumentException iae) {
            //Ok, expected.
        }
        try {
            new PlanePoint(1, 1).angle(p);
            fail("Angle is not defined for a null vector.");
        } catch (IllegalArgumentException iae) {
            //Ok, expected.
        }
    }
    
    public void testAngle()
    {
        PlanePoint xAxis = new PlanePoint(1, 0), p;
        
        assertEquals("Should be 0.", 0, xAxis.angle(xAxis), 0);
        
        p = new PlanePoint(0, 1);
        assertEquals("Should be PI/2.", Math.PI/2, xAxis.angle(p), 0);
        
        p = new PlanePoint(-1, 0);
        assertEquals("Should be PI.", Math.PI, xAxis.angle(p), 0);
        
        p = new PlanePoint(0, -1);
        assertEquals("Should be PI/2.", Math.PI/2, xAxis.angle(p), 0);
    }
    
    public void testEquals()
    {
        PlanePoint p = new PlanePoint(0, 0);
        assertFalse("Should never be equal to null.", p.equals(null));
        assertFalse("Should never be equal to a different type.", 
                p.equals(new Object()));
        assertFalse("Should never be equal if different x1.", 
                p.equals(new PlanePoint(-1, 0)));
        assertFalse("Should never be equal if different x2.", 
                p.equals(new PlanePoint(0, 9)));
        assertFalse("Should never be equal if different x1 and x2.", 
                p.equals(new PlanePoint(-1, 1)));
        assertTrue("Object identity should never matter.", 
                p.equals(new PlanePoint(0, 0)));
    }
    
    public void testHashCodeDiffCalls()
    {
        PlanePoint p = new PlanePoint(500, -30000);
        int h = p.hashCode();
        for (int i = 0; i < MAX_ITER; ++i)
            assertEquals("Should return same value across different calls.", 
                    h, p.hashCode());
    }
    
    public void testHashCodeObjectEquality()
    {
        PlanePoint p, q;
        for (int i = -MAX_ITER/2; i < MAX_ITER/2; ++i) {
            p = new PlanePoint(i, -i);
            q = new PlanePoint(i, -i);
            assertEquals(
                    "Should return same value for equal objects [i = "+i+"].", 
                    p.hashCode(), q.hashCode());
        }
    }
    
}
