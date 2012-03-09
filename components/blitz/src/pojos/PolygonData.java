/*
 * pojos.PolygonData
 *
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
package pojos;

//Java imports
import java.util.List;
import java.util.ArrayList;
import java.awt.geom.Point2D;

//Third-party libraries

//Application-internal dependencies
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
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
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
	 * Creates a new instance of the PolygonData, set the points in the polygon.
	 * 
	 * @param points The points in the polygon.
	 */
	public PolygonData(List<Point2D.Double> points)
	{
		super(new PolygonI(), true);
		setPoints(points);
	}
	
	/**
	 * Returns the points in the polygon.
	 * 
	 * @return See above.
	 */
	public List<Point2D.Double> getPoints()
	{
		return parsePointsToPoint2DList(getPointsAsString());
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
		
		String pts = "";
		if (points != null)
			pts = toPoints(points.toArray(new Point2D.Double[points.size()]));
		shape.setPoints(rtypes.rstring(pts));
	}

}
