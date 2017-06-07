/*
 *------------------------------------------------------------------------------
 * Copyright (C) 2006-2017 University of Dundee. All rights reserved.
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
import omero.model.PolygonI;
import omero.model.Shape;
import omero.model.Polygon;

/**
 * Represents an Polygon shape in the Euclidean space <b>R</b><sup>2</sup>.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class PolygonData
    extends ShapeData
{

    /**
     * Creates a new instance.
     *
     * @param shape The shape this object represents.
     */
    public PolygonData(Shape shape)
    {
        super(shape);
    }

    /** Creates a new instance of PolygonData. */
    public PolygonData()
    {
        this(new ArrayList<Point2D.Double>());
    }

    /**
     * Create a new instance of the PolylineData, set the points in the polyline.
     *
     * @param points See Above.
     */
    public PolygonData(List<Point2D.Double> points)
    {
        super(new PolygonI(), true);
        setPoints(points);
    }

    /**
     * Returns the text of the shape.
     *
     * @return See above.
     */
    public String getText()
    {
        Polygon shape = (Polygon) asIObject();
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
        Polygon shape = (Polygon) asIObject();
        if (shape == null) 
            throw new IllegalArgumentException("No shape specified.");
        shape.setTextValue(rtypes.rstring(text));
    }

    /**
     * Returns the points in the polygon.
     *
     * @return See above.
     */
    public List<Point2D.Double> getPoints()
    {
        String pts = fromPoints("points");
        return parsePointsToPoint2DList(pts);
    }

    /**
     * Returns the points in the polygon.
     *
     * @return See above.
     */
    public List<Integer> getMaskPoints()
    {
        String pts = fromPoints("mask");
        return parsePointsToIntegerList(pts);
    }


    /**
     * Sets the points in the polygon.
     *
     * @param points The points in the polygon.
     */
    public void setPoints(List<Point2D.Double> points)
    {
        if (isReadOnly())
            throw new IllegalArgumentException("Shape ReadOnly");
        Polygon shape = (Polygon) asIObject();
        if (shape == null) 
            throw new IllegalArgumentException("No shape specified.");

        String pointsValues =
                toPoints(points.toArray(new Point2D.Double[points.size()]));
        shape.setPoints(rtypes.rstring(pointsValues));
    }

}
