/*
 * ome.util.math.geom2D.TestSegment
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util.math.geom2D;

import org.testng.annotations.*;

import junit.framework.TestCase;

/**
 * Unit test for {@link Segment}.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @since OME2.2
 */
public class TestSegment extends TestCase {

    private static final int MAX_ITER = 30000; // Max iterations in a test.

    private static final int INTERVAL = 10;

    @Test
    public void testSegmentBadArgs() {
        try {
            new Segment(1, 1, 1, 1);
            fail("Shouldn't allow same points.");
        } catch (IllegalArgumentException iae) {
            // Ok, expected.
        }
    }

    @Test
    public void testSegment() {
        Segment r = new Segment(0, 0, 1, 1);
        assertEquals("Shouldn't change the originX1.", 0.0, r.originX1);
        assertEquals("Shouldn't change the originX2.", 0.0, r.originX2);
    }

    @Test
    public void testGetPointXAxis() {
        PlanePoint p;
        Segment r = new Segment(0, 0, 1, 0);
        double d;
        for (int i = 0; i < INTERVAL; ++i) {
            d = (double) i / INTERVAL;
            p = new PlanePoint(d, 0);
            assertEquals("Wrong point [i = " + i + "].", p, r.getPoint(d));
        }
    }

    @Test
    public void testGetPointYAxis() {
        PlanePoint p;
        Segment r = new Segment(0, 0, 0, 1);
        double d;
        for (int i = 0; i < INTERVAL; ++i) {
            d = (double) i / INTERVAL;
            p = new PlanePoint(0, d);
            assertEquals("Wrong point [i = " + i + "].", p, r.getPoint(d));
        }
    }

    @Test
    public void testGetPointParallelXAxis() {
        PlanePoint p;
        Segment r = new Segment(0, 1, 1, 1);
        double d;
        for (int i = 0; i < INTERVAL; ++i) {
            d = (double) i / INTERVAL;
            p = new PlanePoint(d, 1);
            assertEquals("Wrong point [i = " + i + "].", p, r.getPoint(d));
        }
    }

    @Test
    public void testGetPointParallelYAxis() {
        PlanePoint p;
        Segment r = new Segment(1, 0, 1, 1);
        double d;
        for (int i = 0; i < INTERVAL; ++i) {
            d = (double) i / INTERVAL;
            p = new PlanePoint(1, d);
            assertEquals("Wrong point [i = " + i + "].", p, r.getPoint(d));
        }
    }

    @Test
    public void testLies1() {
        Segment r = new Segment(0, 1, 1, 1);
        double d;
        for (int i = 0; i < INTERVAL; ++i) {
            d = (double) i / INTERVAL;
            assertTrue("Actually lies on r [i = " + i + "].", r.lies(d, 1));
            assertFalse("Doesn't lie on r [i = " + i + "].", r.lies(d, 0));
        }
    }

    @Test
    public void testLies2() {
        Segment r = new Segment(1, 0, 1, 1);
        double d;
        for (int i = 0; i < INTERVAL; ++i) {
            d = (double) i / INTERVAL;
            assertTrue("Actually lies on r [i = " + i + "].", r.lies(1, d));
            assertFalse("Doesn't lie on r [i = " + i + "].", r.lies(d, 0));
        }
    }

    @Test
    public void testLies3() {
        Segment r = new Segment(0, 0, 1, 1);
        double d;
        for (int i = 1; i < INTERVAL; ++i) {
            d = (double) i / INTERVAL;
            assertTrue("Actually lies on r [i = " + i + "].", r.lies(d, d));
            assertFalse("Doesn't lie on r [i = " + i + "].", r.lies(d, 0));
        }
    }

    @Test
    public void testEquals() {
        PlanePoint o = new PlanePoint(0, 0), p = new PlanePoint(1, 1);
        Segment r = new Segment(0, 0, 1, 1);
        assertFalse("Should never be equal to null.", r.equals(null));
        assertFalse("Should never be equal to a different type.", r
                .equals(new Object()));
        assertFalse("Should never be equal if different origin.", r
                .equals(new Line(o, p, p)));
        assertFalse("Should never be equal if different direction.", r
                .equals(new Line(p, o)));
    }

    @Test
    public void testHashCodeDiffCalls() {
        Segment r = new Segment(500, -30000, 0, 0);
        int h = r.hashCode();
        for (int i = 0; i < MAX_ITER; ++i) {
            assertEquals("Should return same value across different calls.", h,
                    r.hashCode());
        }
    }

    @Test
    public void testHashCodeObjectEquality() {
        Segment r, s;
        for (int i = -MAX_ITER / 2; i < MAX_ITER / 2; ++i) {
            r = new Segment(i, -i, i + 1, -i + 1);
            s = new Segment(i, -i, i + 1, -i + 1);
            assertEquals("Should return same value for equal objects [i = " + i
                    + "].", r.hashCode(), s.hashCode());
        }
    }

}
