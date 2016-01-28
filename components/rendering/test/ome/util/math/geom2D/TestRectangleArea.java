/*
 * ome.util.math.geom2D.TestRectangleArea
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util.math.geom2D;

import java.awt.Rectangle;

import org.testng.annotations.*;

import junit.framework.TestCase;

/**
 * Unit test for {@link RectangleArea}.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @since OME2.2
 */
public class TestRectangleArea extends TestCase {

    private static final int MAX_ITER = 100;

    @Test
    public void testRectangle1() {
        RectangleArea area = new RectangleArea();
        Rectangle r = area.getBounds();
        assertEquals("Should set x to 0.", 0, r.x, 0);
        assertEquals("Should set y to 0.", 0, r.y, 0);
        assertEquals("Should set w to 0.", 0, r.width, 0);
        assertEquals("Should set h to 0.", 0, r.height, 0);
    }

    @Test
    public void testRectangle2() {
        RectangleArea area = new RectangleArea(Integer.MIN_VALUE,
                Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
        Rectangle r = area.getBounds();
        assertEquals("Should set x to argument passed to constructor.",
                Integer.MIN_VALUE, r.x, 0);
        assertEquals("Should set y to argument passed to constructor.",
                Integer.MAX_VALUE, r.y, 0);
        assertEquals("Should set w to argument passed to constructor.",
                Integer.MIN_VALUE, r.width, 0);
        assertEquals("Should set h to argument passed to constructor.",
                Integer.MAX_VALUE, r.height, 0);
    }

    @Test
    public void testSetBounds() {
        RectangleArea area = new RectangleArea();
        area.setBounds(Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE,
                Integer.MAX_VALUE);
        Rectangle r = area.getBounds();
        assertEquals("Should set x to argument passed to setBounds() method.",
                Integer.MIN_VALUE, r.x, 0);
        assertEquals("Should set y to argument passed to setBounds() method.",
                Integer.MAX_VALUE, r.y, 0);
        assertEquals("Should set w to argument passed to setBounds() method.",
                Integer.MIN_VALUE, r.width, 0);
        assertEquals("Should set h to argument passed to setBounds() method.",
                Integer.MAX_VALUE, r.height, 0);
    }

    @Test
    public void testScale() {
        RectangleArea area = new RectangleArea(0, 0, 1, 1);
        double j;
        Rectangle r = area.getBounds();
        Rectangle rScale;
        for (int i = 0; i < MAX_ITER; i++) {
            j = (double) i / MAX_ITER;
            area.scale(j);
            rScale = area.getBounds();
            assertEquals("Wrong scale x [i = " + i + "].", rScale.x,
                    (int) (r.x * j), 0);
            assertEquals("Wrong scale y [i = " + i + "].", rScale.y,
                    (int) (r.y * j), 0);
            assertEquals("Wrong scale w [i = " + i + "].", rScale.width,
                    (int) (r.width * j), 0);
            assertEquals("Wrong scale h [i = " + i + "].", rScale.height,
                    (int) (r.height * j), 0);
        }
    }

    @Test
    public void testPlanePoints1() {
        RectangleArea area = new RectangleArea();
        PlanePoint[] points = area.getPoints();
        // Empty array
        assertEquals("Wrong size of the array", 0, points.length, 0);
    }

    @Test
    public void testPlanePoints2() {
        RectangleArea area = new RectangleArea(0, 0, MAX_ITER, MAX_ITER);
        PlanePoint[] points = area.getPoints();
        assertEquals("Wrong size of the array", MAX_ITER * MAX_ITER,
                points.length, 0);
    }

    @Test
    public void testPlanePoints3() {
        RectangleArea area = new RectangleArea(0, 0, MAX_ITER, MAX_ITER);
        PlanePoint[] points = area.getPoints();
        PlanePoint point;
        int k = 0, j = 0, l = 1;
        for (int i = 0; i < points.length; i++) {
            point = points[i];
            assertEquals("Wrong x coordinate", k, point.x1, 0);
            assertEquals("Wrong y coordinate", j, point.x2, 0);
            if (i == l * MAX_ITER - 1) {
                l++;
                j++;
                k = -1;
            }
            k++;
        }
    }

    @Test
    public void testOnBoundaries() {
        RectangleArea area = new RectangleArea(0, 0, MAX_ITER, MAX_ITER);
        for (int i = 1; i < MAX_ITER; i++) {
            assertTrue(area.onBoundaries(i, 0));
            assertTrue(area.onBoundaries(i, MAX_ITER));
            assertTrue(area.onBoundaries(MAX_ITER, i));
            assertTrue(area.onBoundaries(0, i));
            assertFalse(area.onBoundaries(i, i));
        }
    }

}
