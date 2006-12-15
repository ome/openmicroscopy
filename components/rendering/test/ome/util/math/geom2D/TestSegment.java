/*
 * ome.util.math.geom2D.TestSegment
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util.math.geom2D;

// Java imports

// Third-party libraries
import org.testng.annotations.*;
import junit.framework.TestCase;

// Application-internal dependencies

/**
 * Unit test for {@link Segment}.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @version 2.2 <small> (<b>Internal version:</b> $Revision: 1.1 $ $Date:
 *          2005/06/25 18:09:01 $) </small>
 * @since OME2.2
 */
public class TestSegment extends TestCase {

    private static final int MAX_ITER = 30000; // Max iterations in a test.

    private static final int INTERVAL = 10;

    @Test
    public void testSegmentBadArgs() {
        try {
            new Segment(null, null);
            fail("Shouldn't allow nulls.");
        } catch (NullPointerException npe) {
            // Ok, expected.
        }
        try {
            new Segment(null, new PlanePoint(0, 0));
            fail("Shouldn't allow null origin.");
        } catch (NullPointerException npe) {
            // Ok, expected.
        }
        try {
            new Segment(new PlanePoint(0, 0), null);
            fail("Shouldn't allow null head.");
        } catch (NullPointerException npe) {
            // Ok, expected.
        }
        try {
            new Segment(new PlanePoint(1, 1), new PlanePoint(1, 1));
            fail("Shouldn't allow same points.");
        } catch (IllegalArgumentException iae) {
            // Ok, expected.
        }
    }

    @Test
    public void testSegment() {
        PlanePoint o = new PlanePoint(0, 0), p = new PlanePoint(1, 1);
        Segment r = new Segment(o, p);
        assertEquals("Shouldn't change the origin.", o, r.origin);
    }

    @Test
    public void testGetPointXAxis() {
        PlanePoint o = new PlanePoint(0, 0), p = new PlanePoint(1, 0);
        Segment r = new Segment(o, p);
        double d;
        for (int i = 0; i < INTERVAL; ++i) {
            d = (double) i / INTERVAL;
            p = new PlanePoint(d, 0);
            assertEquals("Wrong point [i = " + i + "].", p, r.getPoint(d));
        }
    }

    @Test
    public void testGetPointYAxis() {
        PlanePoint o = new PlanePoint(0, 0), p = new PlanePoint(0, 1);
        Segment r = new Segment(o, p);
        double d;
        for (int i = 0; i < INTERVAL; ++i) {
            d = (double) i / INTERVAL;
            p = new PlanePoint(0, d);
            assertEquals("Wrong point [i = " + i + "].", p, r.getPoint(d));
        }
    }

    @Test
    public void testGetPointParallelXAxis() {
        PlanePoint o = new PlanePoint(0, 1), p = new PlanePoint(1, 1);
        Segment r = new Segment(o, p);
        double d;
        for (int i = 0; i < INTERVAL; ++i) {
            d = (double) i / INTERVAL;
            p = new PlanePoint(d, 1);
            assertEquals("Wrong point [i = " + i + "].", p, r.getPoint(d));
        }
    }

    @Test
    public void testGetPointParallelYAxis() {
        PlanePoint o = new PlanePoint(1, 0), p = new PlanePoint(1, 1);
        Segment r = new Segment(o, p);
        double d;
        for (int i = 0; i < INTERVAL; ++i) {
            d = (double) i / INTERVAL;
            p = new PlanePoint(1, d);
            assertEquals("Wrong point [i = " + i + "].", p, r.getPoint(d));
        }
    }

    @Test
    public void testLiesNull() {
        PlanePoint o = new PlanePoint(0, 1), p = new PlanePoint(1, 1);
        Segment r = new Segment(o, p);
        try {
            r.lies(null);
            fail("Souldn't accept null.");
        } catch (NullPointerException npe) {
            // Ok, expected.
        }
    }

    @Test
    public void testLies1() {
        PlanePoint o = new PlanePoint(0, 1), p = new PlanePoint(1, 1);
        Segment r = new Segment(o, p);
        double d;
        for (int i = 0; i < INTERVAL; ++i) {
            d = (double) i / INTERVAL;
            p = new PlanePoint(d, 1);
            assertTrue("Actually lies on r [i = " + i + "].", r.lies(p));
            p = new PlanePoint(d, 0);
            assertFalse("Doesn't lie on r [i = " + i + "].", r.lies(p));
        }
    }

    @Test
    public void testLies2() {
        PlanePoint o = new PlanePoint(1, 0), p = new PlanePoint(1, 1);
        Segment r = new Segment(o, p);
        double d;
        for (int i = 0; i < INTERVAL; ++i) {
            d = (double) i / INTERVAL;
            p = new PlanePoint(1, d);
            assertTrue("Actually lies on r [i = " + i + "].", r.lies(p));
            p = new PlanePoint(d, 0);
            assertFalse("Doesn't lie on r [i = " + i + "].", r.lies(p));
        }
    }

    @Test
    public void testLies3() {
        PlanePoint o = new PlanePoint(0, 0), p = new PlanePoint(1, 1);
        Segment r = new Segment(o, p);
        double d;
        for (int i = 1; i < INTERVAL; ++i) {
            d = (double) i / INTERVAL;
            p = new PlanePoint(d, d);
            assertTrue("Actually lies on r [i = " + i + "].", r.lies(p));
            p = new PlanePoint(d, 0);
            assertFalse("Doesn't lie on r [i = " + i + "].", r.lies(p));
        }
    }

    @Test
    public void testEquals() {
        PlanePoint o = new PlanePoint(0, 0), p = new PlanePoint(1, 1);
        Segment r = new Segment(o, p);
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
        PlanePoint p = new PlanePoint(500, -30000), q = new PlanePoint(0, 0);
        Segment r = new Segment(p, q);
        int h = r.hashCode();
        for (int i = 0; i < MAX_ITER; ++i) {
            assertEquals("Should return same value across different calls.", h,
                    r.hashCode());
        }
    }

    @Test
    public void testHashCodeObjectEquality() {
        PlanePoint p, q;
        Segment r, s;
        for (int i = -MAX_ITER / 2; i < MAX_ITER / 2; ++i) {
            p = new PlanePoint(i, -i);
            q = new PlanePoint(i + 1, -i + 1);
            r = new Segment(p, q);
            s = new Segment(p, q);
            assertEquals("Should return same value for equal objects [i = " + i
                    + "].", r.hashCode(), s.hashCode());
        }
    }

}
