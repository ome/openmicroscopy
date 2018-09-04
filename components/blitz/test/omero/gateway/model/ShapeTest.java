/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2017-2018 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package omero.gateway.model;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test the encoding of some shapes e.g. polyline
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.3
 */
@Test(groups = "unit")
public class ShapeTest {

    @Test
    public void testSetPoints()
    {
        List<Point2D.Double> points = new ArrayList<Point2D.Double>();
        String start = "1,2 3,4";
        points.add(new Point2D.Double(1, 2));
        points.add(new Point2D.Double(3, 4));
        PolylineData pl = new PolylineData();
        pl.setPoints(points);
        String v = ShapeData.toPoints(points.toArray(new Point2D.Double[points.size()]));
        Assert.assertEquals(v, start);
    }

    /**
     * Tests the underline methods used to determine the points composing the
     * shape.
     */
    @Test
    public void testParsePoints()
    {
        String start = "1,2 3,4";
        PolylineData pl = new PolylineData();
        List<Point2D.Double> points = pl.parsePointsToPoint2DList(start);
        Point2D.Double p = points.get(0);
        Assert.assertEquals(p.x, 1.0);
        Assert.assertEquals(p.y, 2.0);
        p = points.get(1);
        Assert.assertEquals(p.x, 3.0);
        Assert.assertEquals(p.y, 4.0);
    }

    /**
     * Tests the underline methods used to determine the points composing the
     * shape.
     */
    @Test
    public void testParsePointsWithComma()
    {
        String start = "1,2, 3,4";
        PolylineData pl = new PolylineData();
        List<Point2D.Double> points = pl.parsePointsToPoint2DList(start);
        Point2D.Double p = points.get(0);
        Assert.assertEquals(p.x, 1.0);
        Assert.assertEquals(p.y, 2.0);
        p = points.get(1);
        Assert.assertEquals(p.x, 3.0);
        Assert.assertEquals(p.y, 4.0);
    }

    private void testAndResetDirty(ShapeData s) {
        Assert.assertTrue(s.isDirty());
        s.setDirty(false);
    }

    @Test
    public void testDirty() {
        EllipseData s = new EllipseData(10, 10, 5, 5);
        s.setDirty(false);
        s.setX(1);
        testAndResetDirty(s);
        s.setY(1);
        testAndResetDirty(s);
        s.setRadiusX(1);
        testAndResetDirty(s);
        s.setRadiusY(1);
        testAndResetDirty(s);
        s.setText("bla");
        testAndResetDirty(s);
        s.setC(2);
        testAndResetDirty(s);
        s.setT(2);
        testAndResetDirty(s);
        s.setT(2);
        testAndResetDirty(s);
        s.setC(-1);
        testAndResetDirty(s);
        s.setT(-1);
        testAndResetDirty(s);
        s.setT(-1);
        testAndResetDirty(s);
        
        LineData l = new LineData(10, 10, 10, 10);
        l.setDirty(false);
        l.setX1(1);
        testAndResetDirty(l);
        l.setY1(1);
        testAndResetDirty(l);
        l.setX2(1);
        testAndResetDirty(l);
        l.setY2(1);
        testAndResetDirty(l);
        l.setText("bla");
        testAndResetDirty(l);
        l.setC(2);
        testAndResetDirty(l);
        l.setT(2);
        testAndResetDirty(l);
        l.setT(2);
        testAndResetDirty(l);
        l.setC(-1);
        testAndResetDirty(l);
        l.setT(-1);
        testAndResetDirty(l);
        l.setT(-1);
        testAndResetDirty(l);
        
        MaskData m = new MaskData(10, 10, 10, 10, new byte[10]);
        m.setDirty(false);
        m.setX(1);
        testAndResetDirty(m);
        m.setY(1);
        testAndResetDirty(m);
        m.setWidth(1);
        testAndResetDirty(m);
        m.setHeight(1);
        testAndResetDirty(m);
        m.setMask(new byte[10]);
        testAndResetDirty(m);
        m.setText("bla");
        testAndResetDirty(m);
        m.setC(2);
        testAndResetDirty(m);
        m.setT(2);
        testAndResetDirty(m);
        m.setT(2);
        testAndResetDirty(m);
        m.setC(-1);
        testAndResetDirty(m);
        m.setT(-1);
        testAndResetDirty(m);
        m.setT(-1);
        testAndResetDirty(m);
        
        PointData p = new PointData(10, 10);
        p.setDirty(false);
        p.setX(1);
        testAndResetDirty(p);
        p.setY(1);
        testAndResetDirty(p);
        p.setText("bla");
        testAndResetDirty(p);
        p.setC(2);
        testAndResetDirty(p);
        p.setT(2);
        testAndResetDirty(p);
        p.setT(2);
        testAndResetDirty(p);
        p.setC(-1);
        testAndResetDirty(p);
        p.setT(-1);
        testAndResetDirty(p);
        p.setT(-1);
        testAndResetDirty(p);
        
        List<Point2D.Double> points = Arrays.asList(new Point2D.Double[]{new Point2D.Double(1,2), new Point2D.Double(2,3), new Point2D.Double(3,4), new Point2D.Double(4,5)});
        PolygonData pg = new PolygonData(points);
        pg.setDirty(false);
        pg.setPoints(points);
        testAndResetDirty(pg);
        pg.setText("bla");
        testAndResetDirty(pg);
        pg.setC(2);
        testAndResetDirty(pg);
        pg.setT(2);
        testAndResetDirty(pg);
        pg.setT(2);
        testAndResetDirty(pg);
        pg.setC(-1);
        testAndResetDirty(pg);
        pg.setT(-1);
        testAndResetDirty(pg);
        pg.setT(-1);
        testAndResetDirty(pg);
        
        PolylineData pl = new PolylineData(points);
        pl.setDirty(false);
        pl.setPoints(points);
        testAndResetDirty(pl);
        pl.setText("bla");
        testAndResetDirty(pl);
        pl.setC(2);
        testAndResetDirty(pl);
        pl.setT(2);
        testAndResetDirty(pl);
        pl.setT(2);
        testAndResetDirty(pl);
        pl.setC(-1);
        testAndResetDirty(pl);
        pl.setT(-1);
        testAndResetDirty(pl);
        pl.setT(-1);
        testAndResetDirty(pl);
        
        RectangleData r = new RectangleData(10, 10, 10, 10);
        r.setDirty(false);
        r.setX(1);
        testAndResetDirty(r);
        r.setY(1);
        testAndResetDirty(r);
        r.setWidth(1);
        testAndResetDirty(r);
        r.setHeight(1);
        testAndResetDirty(r);
        r.setText("bla");
        testAndResetDirty(r);
        r.setC(2);
        testAndResetDirty(r);
        r.setT(2);
        testAndResetDirty(r);
        r.setT(2);
        testAndResetDirty(r);
        r.setC(-1);
        testAndResetDirty(r);
        r.setT(-1);
        testAndResetDirty(r);
        r.setT(-1);
        testAndResetDirty(r);
        
        TextData t = new TextData("blup", 10, 10);
        t.setDirty(false);
        t.setX(1);
        testAndResetDirty(t);
        t.setY(1);
        testAndResetDirty(t);
        t.setText("bla");
        testAndResetDirty(t);
        t.setC(2);
        testAndResetDirty(t);
        t.setT(2);
        testAndResetDirty(t);
        t.setT(2);
        testAndResetDirty(t);
        t.setC(-1);
        testAndResetDirty(t);
        t.setT(-1);
        testAndResetDirty(t);
        t.setT(-1);
        testAndResetDirty(t);
    }
}
