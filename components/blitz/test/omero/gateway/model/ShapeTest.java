/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2017 University of Dundee & Open Microscopy Environment.
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

}
