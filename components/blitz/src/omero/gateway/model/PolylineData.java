/*
 *------------------------------------------------------------------------------
 * Copyright (C) 2006-2009 University of Dundee. All rights reserved.
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package omero.gateway.model;

import java.util.List;
import java.util.ArrayList;
import java.awt.geom.Point2D;

import omero.RString;
import omero.rtypes;
import omero.model.PolylineI;
import omero.model.Shape;
import omero.model.Polyline;

/**
 * Represents an Polyline shape in the Euclidean space <b>R</b><sup>2</sup>.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class PolylineData
    extends ShapeData
{

    /**
     * Creates a new instance.
     *
     * @param shape The shape this object represents.
     */
    public PolylineData(Shape shape)
    {
        super(shape);
    }

    /**
     * Creates a new instance of polyline, creating a new PolylineI Object.
     */
    public PolylineData()
    {
        this(new ArrayList<Point2D.Double>());
    }

    /**
     * Create a new instance of the PolylineData, set the points in the polyline.
     *
     * @param points See Above.
     */
    public PolylineData(List<Point2D.Double> points, List<Point2D.Double> points1,
            List<Point2D.Double> points2, List<Integer> maskList)
    {
        super(new PolylineI(), true);
        setPoints(points, points1, points2, maskList);
    }

    /**
     * Create a new instance of the PolylineData, set the points in the polyline.
     *
     * @param points See Above.
     */
    public PolylineData(List<Point2D.Double> points)
    {
        super(new PolylineI(), true);
        setPoints(points);
    }

    /**
     * Returns the text of the shape.
     *
     * @return See above.
     */
    public String getText()
    {
        Polyline shape = (Polyline) asIObject();
        RString value = shape.getTextValue();
        if (value == null) return "";
        return value.getValue();
    }

    /**
     * Sets the text of the shape.
     *
     * @param text See above.
     */
    public void setText(String text)
    {
        if (isReadOnly())
            throw new IllegalArgumentException("Shape ReadOnly");
        Polyline shape = (Polyline) asIObject();
        if (shape == null) 
            throw new IllegalArgumentException("No shape specified.");
        shape.setTextValue(rtypes.rstring(text));
    }

    /**
     * Returns the points in the Polyline.
     *
     * @return See above.
     */
    public List<Point2D.Double> getPoints()
    {
        String pts = fromPoints("points");
        return parsePointsToPoint2DList(pts);
    }

    /**
     * Returns the points in the Polyline.
     *
     * @return See above.
     */
    public List<Point2D.Double> getPoints1()
    {
        String pts = fromPoints("points1");
        return parsePointsToPoint2DList(pts);
    }

    /**
     * Returns the points in the Polyline.
     *
     * @return See above.
     */
    public List<Point2D.Double> getPoints2()
    {
        String pts = fromPoints("points2");
        return parsePointsToPoint2DList(pts);
    }

    /**
     * Returns the points in the Polyline.
     *
     * @return See above.
     */
    public List<Integer> getMaskPoints()
    {
        String pts = fromPoints("mask");
        return parsePointsToIntegerList(pts);
    }

    /**
     * Sets the points in the polyline.
     *
     * @param points The points to set.
     * @param points1 The points to set.
     * @param points2 The points to set.
     * @param maskList The points to set.
     */
    public void setPoints(List<Point2D.Double> points,
            List<Point2D.Double> points1,
            List<Point2D.Double> points2, List<Integer> maskList)
    {
        if (isReadOnly())
            throw new IllegalArgumentException("Shape ReadOnly");
        Polyline shape = (Polyline) asIObject();
        if (shape == null) 
            throw new IllegalArgumentException("No shape specified.");

        String pointsValues =
                toPoints(points.toArray(new Point2D.Double[points.size()]));
        shape.setPoints(rtypes.rstring(pointsValues));
    }

    /**
     * Sets the points in the polyline.
     *
     * @param points The points to set.
     */
    public void setPoints(List<Point2D.Double> points)
    {
        if (isReadOnly())
            throw new IllegalArgumentException("Shape ReadOnly");
        Polyline shape = (Polyline) asIObject();
        if (shape == null) 
            throw new IllegalArgumentException("No shape specified.");

        String pointsValues =
                toPoints(points.toArray(new Point2D.Double[points.size()]));
        shape.setPoints(rtypes.rstring(pointsValues));
    }
}
