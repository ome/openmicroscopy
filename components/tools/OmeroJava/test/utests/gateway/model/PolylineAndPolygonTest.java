/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015 University of Dundee. All rights reserved.
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
package utests.gateway.model;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import omero.rtypes;
import omero.gateway.model.PolygonData;
import omero.gateway.model.PolylineData;
import omero.model.Polygon;
import omero.model.PolygonI;
import omero.model.Polyline;
import omero.model.PolylineI;

import org.testng.annotations.Test;

import junit.framework.TestCase;


/**
 * Tests the storage of points for polyline and polygon according to the
 * schema.
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.1
 */
public class PolylineAndPolygonTest extends TestCase {

    /** Returns a double array as a number attribute value. */
    private String toNumber(double number)
    {
        String str = Double.toString(number);
        if (str.endsWith(".0"))
            str = str.substring(0, str.length()-2);
        return str;
    }

    /**
     * Converts the specified points.
     *
     * @param points The points to handle.
     * @return See above.
     */
    private String toPoints(Point2D.Double[] points)
    {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < points.length; i++)
        {
            if (i != 0)
            {
                buf.append(", ");
            }
            buf.append(toNumber(points[i].x));
            buf.append(',');
            buf.append(toNumber(points[i].y));
        }
        return buf.toString();
    }

    @Test
    public void testSavePointsForPolyline() {
        PolylineData data = new PolylineData();
        List<Point2D.Double> points = new ArrayList<Point2D.Double>();
        int n = 5;
        for (int i = 0; i < n; i++) {
            Point2D.Double p = new Point2D.Double(i, n);
            points.add(p);
        }
        data.setPoints(points, points, points, new ArrayList<Integer>());
        Polyline shape = (Polyline) data.asIObject();
        String pointsAsString = shape.getPoints().getValue();
        //Check that the string no longer contains
        //"points", "points1", "points2" or "masks"
        assertFalse(pointsAsString.contains("points"));
        assertFalse(pointsAsString.contains("points1"));
        assertFalse(pointsAsString.contains("points2"));
        assertFalse(pointsAsString.contains("masks"));
    }

    @Test
    public void testSavePointsForPolygon() {
        PolygonData data = new PolygonData();
        List<Point2D.Double> points = new ArrayList<Point2D.Double>();
        int n = 5;
        for (int i = 0; i < n; i++) {
            Point2D.Double p = new Point2D.Double(i, n);
            points.add(p);
        }
        data.setPoints(points, points, points, new ArrayList<Integer>());
        Polygon shape = (Polygon) data.asIObject();
        String pointsAsString = shape.getPoints().getValue();
        //Check that the string no longer contains
        //"points", "points1", "points2" or "masks"
        assertFalse(pointsAsString.contains("points"));
        assertFalse(pointsAsString.contains("points1"));
        assertFalse(pointsAsString.contains("points2"));
        assertFalse(pointsAsString.contains("masks"));
    }

    @Test
    public void testSetPointsForPolyline() {
        PolylineData data = new PolylineData();
        List<Point2D.Double> points = new ArrayList<Point2D.Double>();
        int n = 5;
        for (int i = 0; i < n; i++) {
            Point2D.Double p = new Point2D.Double(i, n);
            points.add(p);
        }
        data.setPoints(points);
        Polyline shape = (Polyline) data.asIObject();
        String pointsAsString = shape.getPoints().getValue();
        //Check that the string no longer contains
        //"points"
        assertFalse(pointsAsString.contains("points"));
    }

    @Test
    public void testSetPointsForPolygon() {
        PolygonData data = new PolygonData();
        List<Point2D.Double> points = new ArrayList<Point2D.Double>();
        int n = 5;
        for (int i = 0; i < n; i++) {
            Point2D.Double p = new Point2D.Double(i, n);
            points.add(p);
        }
        data.setPoints(points);
        Polygon shape = (Polygon) data.asIObject();
        String pointsAsString = shape.getPoints().getValue();
        //Check that the string no longer contains
        //"points"
        assertFalse(pointsAsString.contains("points"));
    }

    @Test
    public void testNewInstanceForPolyline() {
        
        List<Point2D.Double> points = new ArrayList<Point2D.Double>();
        int n = 5;
        for (int i = 0; i < n; i++) {
            Point2D.Double p = new Point2D.Double(i, n);
            points.add(p);
        }
        PolylineData data = new PolylineData(points);
        Polyline shape = (Polyline) data.asIObject();
        String pointsAsString = shape.getPoints().getValue();
        //Check that the string no longer contains
        //"points"
        assertFalse(pointsAsString.contains("points"));
        List<Point2D.Double> list = data.getPoints();
        Point2D.Double p, p1;
        for (int i = 0; i < n; i++) {
            p = points.get(i);
            p1 = list.get(i);
            assertEquals(p.getX(), p1.getX());
            assertEquals(p.getY(), p1.getY());
        }
    }

    @Test
    public void testNewInstanceForPolygon() {
        
        List<Point2D.Double> points = new ArrayList<Point2D.Double>();
        int n = 5;
        for (int i = 0; i < n; i++) {
            Point2D.Double p = new Point2D.Double(i, n);
            points.add(p);
        }
        PolygonData data = new PolygonData(points);
        Polygon shape = (Polygon) data.asIObject();
        String pointsAsString = shape.getPoints().getValue();
        //Check that the string no longer contains
        //"points"
        assertFalse(pointsAsString.contains("points"));
        List<Point2D.Double> list = data.getPoints();
        Point2D.Double p, p1;
        for (int i = 0; i < n; i++) {
            p = points.get(i);
            p1 = list.get(i);
            assertEquals(p.getX(), p1.getX());
            assertEquals(p.getY(), p1.getY());
        }
    }

    @Test
    public void testSavePointsLegacyModeForPolygon() {
        Polygon shape = new PolygonI();
        int n = 5;
        Point2D.Double[] points = new Point2D.Double[n];
        List<Integer> masks = new ArrayList<Integer>();
        for (int i = 0; i < n; i++) {
            Point2D.Double p = new Point2D.Double(i, n);
            points[i] = p;
            masks.add(i);
        }
        String maskValues = "";
        for (int i = 0 ; i < masks.size()-1; i++)
            maskValues = maskValues + masks.get(i)+",";
        maskValues = maskValues+masks.get(masks.size()-1)+"";

        String pointsValues = toPoints(points);
        String pts = "points["+pointsValues+"] ";
        pts = pts + "points1["+pointsValues+"] ";
        pts = pts + "points2["+pointsValues+"] ";
        pts = pts + "mask["+maskValues+"] ";
        shape.setPoints(rtypes.rstring(pts));
        PolygonData data = new PolygonData(shape);
        List<Point2D.Double> list = data.getPoints();
        Point2D.Double p, p1;
        for (int i = 0; i < n; i++) {
            p = points[i];
            p1 = list.get(i);
            assertEquals(p.getX(), p1.getX());
            assertEquals(p.getY(), p1.getY());
        }
        list = data.getPoints1();
        for (int i = 0; i < n; i++) {
            p = points[i];
            p1 = list.get(i);
            assertEquals(p.getX(), p1.getX());
            assertEquals(p.getY(), p1.getY());
        }
        list = data.getPoints2();
        for (int i = 0; i < n; i++) {
            p = points[i];
            p1 = list.get(i);
            assertEquals(p.getX(), p1.getX());
            assertEquals(p.getY(), p1.getY());
        }
        List<Integer> ml = data.getMaskPoints();
        for (int i = 0; i < n; i++) {
            assertEquals(masks.get(i), ml.get(i));
        }
    }

    @Test
    public void testSavePointsLegacyModeForPolyline() {
        Polyline shape = new PolylineI();
        int n = 5;
        Point2D.Double[] points = new Point2D.Double[n];
        List<Integer> masks = new ArrayList<Integer>();
        for (int i = 0; i < n; i++) {
            Point2D.Double p = new Point2D.Double(i, n);
            points[i] = p;
            masks.add(i);
        }
        String maskValues = "";
        for (int i = 0 ; i < masks.size()-1; i++)
            maskValues = maskValues + masks.get(i)+",";
        maskValues = maskValues+masks.get(masks.size()-1)+"";

        String pointsValues = toPoints(points);
        String pts = "points["+pointsValues+"] ";
        pts = pts + "points1["+pointsValues+"] ";
        pts = pts + "points2["+pointsValues+"] ";
        pts = pts + "mask["+maskValues+"] ";
        shape.setPoints(rtypes.rstring(pts));
        PolygonData data = new PolygonData(shape);
        List<Point2D.Double> list = data.getPoints();
        Point2D.Double p, p1;
        for (int i = 0; i < n; i++) {
            p = points[i];
            p1 = list.get(i);
            assertEquals(p.getX(), p1.getX());
            assertEquals(p.getY(), p1.getY());
        }
        list = data.getPoints1();
        for (int i = 0; i < n; i++) {
            p = points[i];
            p1 = list.get(i);
            assertEquals(p.getX(), p1.getX());
            assertEquals(p.getY(), p1.getY());
        }
        list = data.getPoints2();
        for (int i = 0; i < n; i++) {
            p = points[i];
            p1 = list.get(i);
            assertEquals(p.getX(), p1.getX());
            assertEquals(p.getY(), p1.getY());
        }
        List<Integer> ml = data.getMaskPoints();
        for (int i = 0; i < n; i++) {
            assertEquals(masks.get(i), ml.get(i));
        }
    }
    
    @Test
    public void testGetPointsForPolygon() {
        PolygonData data = new PolygonData();
        List<Point2D.Double> points = new ArrayList<Point2D.Double>();
        int n = 5;
        Point2D.Double p;
        for (int i = 0; i < n; i++) {
            p = new Point2D.Double(i, n);
            points.add(p);
        }
        data.setPoints(points, points, points, new ArrayList<Integer>());
        List<Point2D.Double> list = data.getPoints();
        Point2D.Double p1;
        for (int i = 0; i < n; i++) {
            p = points.get(i);
            p1 = list.get(i);
            assertEquals(p.getX(), p1.getX());
            assertEquals(p.getY(), p1.getY());
        }
        list = data.getPoints1();
        for (int i = 0; i < n; i++) {
            p = points.get(i);
            p1 = list.get(i);
            assertEquals(p.getX(), p1.getX());
            assertEquals(p.getY(), p1.getY());
        }
        list = data.getPoints2();
        for (int i = 0; i < n; i++) {
            p = points.get(i);
            p1 = list.get(i);
            assertEquals(p.getX(), p1.getX());
            assertEquals(p.getY(), p1.getY());
        }
    }

    @Test
    public void testGetPointsForPolyline() {
        PolylineData data = new PolylineData();
        List<Point2D.Double> points = new ArrayList<Point2D.Double>();
        int n = 5;
        Point2D.Double p;
        List<Integer> masks = new ArrayList<Integer>();
        for (int i = 0; i < n; i++) {
            p = new Point2D.Double(i, n);
            points.add(p);
            masks.add(i);
        }
        data.setPoints(points, points, points, masks);
        List<Point2D.Double> list = data.getPoints();
        Point2D.Double p1;
        for (int i = 0; i < n; i++) {
            p = points.get(i);
            p1 = list.get(i);
            assertEquals(p.getX(), p1.getX());
            assertEquals(p.getY(), p1.getY());
        }
        list = data.getPoints1();
        for (int i = 0; i < n; i++) {
            p = points.get(i);
            p1 = list.get(i);
            assertEquals(p.getX(), p1.getX());
            assertEquals(p.getY(), p1.getY());
        }
        list = data.getPoints2();
        for (int i = 0; i < n; i++) {
            p = points.get(i);
            p1 = list.get(i);
            assertEquals(p.getX(), p1.getX());
            assertEquals(p.getY(), p1.getY());
        }
    }
}
