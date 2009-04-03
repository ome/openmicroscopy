/*
 * org.openmicroscopy.shoola.util.math.geom2D.TestLine
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
 * Unit test for {@link Line}. 
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
public class TestLine
    extends TestCase
{
    
    private static final int    MAX_ITER = 30000;  //Max iterations in a test.

    
    public void testLineBadArgs()
    {
        try {
            new Line(null, null);
            fail("Shouldn't allow nulls.");
        } catch (NullPointerException npe) {
            //Ok, expected.
        }
        try {
            new Line(null, new PlanePoint(0, 0));
            fail("Shouldn't allow null origin.");
        } catch (NullPointerException npe) {
            //Ok, expected.
        }
        try {
            new Line(new PlanePoint(0, 0), null);
            fail("Shouldn't allow null head.");
        } catch (NullPointerException npe) {
            //Ok, expected.
        }
        try {
            new Line(new PlanePoint(1, 1), new PlanePoint(1, 1));
            fail("Shouldn't allow same points.");
        } catch (IllegalArgumentException iae) {
            //Ok, expected.
        }
    }
    
    public void testLineBadArgs2()
    {
        try {
            new Line(null, null, null);
            fail("Shouldn't allow nulls.");
        } catch (NullPointerException npe) {
            //Ok, expected.
        }
        try {
            new Line(null, new PlanePoint(0, 0), new PlanePoint(0, 1));
            fail("Shouldn't allow null tail.");
        } catch (NullPointerException npe) {
            //Ok, expected.
        }
        try {
            new Line(new PlanePoint(0, 0), null, new PlanePoint(0, 1));
            fail("Shouldn't allow null head.");
        } catch (NullPointerException npe) {
            //Ok, expected.
        }
        try {
            new Line(new PlanePoint(0, 0), new PlanePoint(0, 1), null);
            fail("Shouldn't allow null origin.");
        } catch (NullPointerException npe) {
            //Ok, expected.
        }
        try {
            new Line(new PlanePoint(1, 1), new PlanePoint(1, 1), new PlanePoint(0, 0));
            fail("Shouldn't allow same head and tail.");
        } catch (IllegalArgumentException iae) {
            //Ok, expected.
        }
    }
    
    public void testLine()
    {
        PlanePoint o = new PlanePoint(0, 0), p = new PlanePoint(1, 1), 
                u = new PlanePoint(1/Math.sqrt(2), 1/Math.sqrt(2));
        Line r = new Line(o, p);
        assertEquals("Shouldn't change the origin.", 
                o, r.origin);
        assertEquals("Calculated wrong unit vector.", 
                u, r.direction);
    }
    
    public void testLine2()
    {
        PlanePoint o = new PlanePoint(0, 0), p = new PlanePoint(1, 1), 
                u = new PlanePoint(1/Math.sqrt(2), 1/Math.sqrt(2));
        Line r = new Line(o, p, o);
        assertEquals("Shouldn't change the origin.", 
                o, r.origin);
        assertEquals("Calculated wrong unit vector.", 
                u, r.direction);
    }
    
    public void testGetPointXAxis()
    {
        PlanePoint o = new PlanePoint(0, 0), p = new PlanePoint(1, 0); 
        Line r = new Line(o, p);
        for (int i = -MAX_ITER/2; i < MAX_ITER/2; ++i) {
            p = new PlanePoint(i, 0);
            assertEquals("Wrong point [i = "+i+"].", 
                    p, r.getPoint(i));
        }
    }
    
    public void testGetPointYAxis()
    {
        PlanePoint o = new PlanePoint(0, 0), p = new PlanePoint(0, 1); 
        Line r = new Line(o, p);
        for (int i = -MAX_ITER/2; i < MAX_ITER/2; ++i) {
            p = new PlanePoint(0, i);
            assertEquals("Wrong point [i = "+i+"].", 
                    p, r.getPoint(i));
        }
    }
    
    public void testGetPointParallelXAxis()
    {
        PlanePoint o = new PlanePoint(0, 1), p = new PlanePoint(-1, 1); 
        Line r = new Line(o, p);
        for (int i = -MAX_ITER/2; i < MAX_ITER/2; ++i) {
            p = new PlanePoint(-i, 1);  //Orientation is from right to left.
            assertEquals("Wrong point [i = "+i+"].", 
                    p, r.getPoint(i));
        }
    }
    
    public void testGetPointParallelYAxis()
    {
        PlanePoint o = new PlanePoint(1, 0), p = new PlanePoint(1, 1); 
        Line r = new Line(o, p);
        for (int i = -MAX_ITER/2; i < MAX_ITER/2; ++i) {
            p = new PlanePoint(1, i);
            assertEquals("Wrong point [i = "+i+"].", 
                    p, r.getPoint(i));
        }
    }
    
    public void testLiesNull()
    {
        PlanePoint o = new PlanePoint(0, 1), p = new PlanePoint(1, 1); 
        Line r = new Line(o, p);
        try {
            r.lies(null);
            fail("Souldn't accept null.");
        } catch (NullPointerException npe) {
            //Ok, expected.
        }
        try {
            r.lies(null, true);
            fail("Souldn't accept null.");
        } catch (NullPointerException npe) {
            //Ok, expected.
        }
        try {
            r.lies(null, false);
            fail("Souldn't accept null.");
        } catch (NullPointerException npe) {
            //Ok, expected.
        }
    }
    
    public void testLies1()
    {
        PlanePoint o = new PlanePoint(0, 1), p = new PlanePoint(1, 1); 
        Line r = new Line(o, p);
        int i;
        for (i = -MAX_ITER/2; i < 0; ++i) {
            p = new PlanePoint(i, 1);
            assertTrue("Actually lies on r [i = "+i+"].", 
                    r.lies(p));
            assertTrue("Actually lies on negative half of r [i = "+i+"].", 
                    r.lies(p, false));
            
            p = new PlanePoint(i, 0);
            assertFalse("Doesn't lie on r [i = "+i+"].", 
                    r.lies(p));
            assertFalse("Doesn't lie on r [i = "+i+"].", 
                    r.lies(p, true));
        }
        for (; i < MAX_ITER/2; ++i) {
            p = new PlanePoint(i, 1);
            assertTrue("Actually lies on r [i = "+i+"].", 
                    r.lies(p));
            assertTrue("Actually lies on positive half of r [i = "+i+"].", 
                    r.lies(p, true));
            
            p = new PlanePoint(i, 0);
            assertFalse("Doesn't lie on r [i = "+i+"].", 
                    r.lies(p));
            assertFalse("Doesn't lie on r [i = "+i+"].", 
                    r.lies(p, false));
        }
    }
    
    public void testLies2()
    {
        PlanePoint o = new PlanePoint(-1, 0), p = new PlanePoint(-1, -1); 
        Line r = new Line(o, p);
        int i;
        for (i = -MAX_ITER/2; i <= 0; ++i) {
            p = new PlanePoint(-1, i);
            assertTrue("Actually lies on r [i = "+i+"].", 
                    r.lies(p));
            assertTrue("Actually lies on positive half of r [i = "+i+"].", 
                    r.lies(p, true));
            
            p = new PlanePoint(0, i);
            assertFalse("Doesn't lie on r [i = "+i+"].", 
                    r.lies(p));
            assertFalse("Doesn't lie on r [i = "+i+"].", 
                    r.lies(p, true));
        }
        for (; i < MAX_ITER/2; ++i) {
            p = new PlanePoint(-1, i);
            assertTrue("Actually lies on r [i = "+i+"].", 
                    r.lies(p));
            assertTrue("Actually lies on negative half of r [i = "+i+"].", 
                    r.lies(p, false));
            
            p = new PlanePoint(0, i);
            assertFalse("Doesn't lie on r [i = "+i+"].", 
                    r.lies(p));
            assertFalse("Doesn't lie on r [i = "+i+"].", 
                    r.lies(p, false));
        }
    }
    
    public void testLies3()
    {
        PlanePoint o = new PlanePoint(0, 0), p = new PlanePoint(1, 1); 
        Line r = new Line(o, p);
        int i;
        for (i = -MAX_ITER/2; i <= 0; ++i) {
            p = new PlanePoint(-i, -i);
            assertTrue("Actually lies on r [i = "+i+"].", 
                    r.lies(p));
            assertTrue("Actually lies on negative half of r [i = "+i+"].", 
                    r.lies(p, true));
            p = new PlanePoint(1, i);
            assertFalse("Doesn't lie on r [i = "+i+"].", 
                    r.lies(p));
            assertFalse("Doesn't lie on r [i = "+i+"].", 
                    r.lies(p, true));
        }
        for (; i < MAX_ITER/2; ++i) {
            p = new PlanePoint(i, i);
            assertTrue("Actually lies on r [i = "+i+"].", 
                    r.lies(p));
            assertTrue("Actually lies on positive half of r [i = "+i+"].", 
                    r.lies(p, true));
            p = new PlanePoint(0, -i);
            assertFalse("Doesn't lie on r [i = "+i+"].", 
                    r.lies(p));
            assertFalse("Doesn't lie on r [i = "+i+"].", 
                    r.lies(p, false));
        } 
    }
    
    public void testEquals()
    {
        PlanePoint o = new PlanePoint(0, 0), p = new PlanePoint(1, 1);
        Line r = new Line(o, p);
        assertFalse("Should never be equal to null.", r.equals(null));
        assertFalse("Should never be equal to a different type.", 
                r.equals(new Object()));
        assertFalse("Should never be equal if different origin.", 
                r.equals(new Line(o, p, p)));
        assertFalse("Should never be equal if different direction.", 
                r.equals(new Line(p, o)));
    }
    
    public void testHashCodeDiffCalls()
    {
        PlanePoint p = new PlanePoint(500, -30000), q = new PlanePoint(0, 0);
        Line r = new Line(p, q);
        int h = r.hashCode();
        for (int i = 0; i < MAX_ITER; ++i)
            assertEquals("Should return same value across different calls.", 
                    h, r.hashCode());
    }
    
    public void testHashCodeObjectEquality()
    {
        PlanePoint p, q;
        Line r, s;
        for (int i = -MAX_ITER/2; i < MAX_ITER/2; ++i) {
            p = new PlanePoint(i, -i);
            q = new PlanePoint(i+1, -i+1);
            r = new Line(p, q);
            s = new Line(p, q);
            assertEquals(
                    "Should return same value for equal objects [i = "+i+"].", 
                    r.hashCode(), s.hashCode());
        }
    }
    
}
