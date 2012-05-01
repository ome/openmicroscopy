/*
 * pojos.PolylineData
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
import omero.RString;
import omero.rtypes;
import omero.model.Marker;
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
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
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
	 * Create a new instance of the PolylineData.
	 * 
	 * @param points The points to set.
	 */
	public PolylineData(List<Point2D.Double> points)
	{
		super(new PolylineI(), true);
		setPoints(points);
	}

	/**
	 * Returns the points in the Polyline.
	 * 
	 * @return See above.
	 */
	public List<Point2D.Double> getPoints()
	{
		return parsePointsToPoint2DList(getPointsAsString());
	}
	
	/**
	 * Sets the points in the polyline.
	 * 
	 * @param points The points to set.
	 * @param points See above.
	 */
	public void setPoints(List<Point2D.Double> points)
	{
		if (isReadOnly())
			throw new IllegalArgumentException("Shape ReadOnly");
		Polyline shape = (Polyline) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		
		String pts = "";
		if (points != null)
			pts = toPoints(points.toArray(new Point2D.Double[points.size()]));
		shape.setPoints(rtypes.rstring(pts));
	}

	/**
	 * Returns the marker start.
	 * 
	 * @return See above.
	 */
	public String getMarkerStart()
	{
		Polyline shape = (Polyline) asIObject();
		if (shape == null) return "";
		Marker value = shape.getMarkerStart();
		if (value == null) return "";
		return value.getValue().getValue();
	}
	
	/**
	 * Returns the marker end.
	 * 
	 * @return See above.
	 */
	public String getMarkerEnd()
	{
		Polyline shape = (Polyline) asIObject();
		if (shape == null) return "";
		Marker value = shape.getMarkerEnd();
		if (value == null) return "";
		return value.getValue().getValue();
	}
	
	/**
	 * Sets the marker start.
	 * 
	 * @param markerStart The value to set.
	 */
	public void setMarkerStart(Marker markerStart)
	{
		if (markerStart == null) return;
		Polyline shape = (Polyline) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		shape.setMarkerStart(markerStart);
	}
	
	/**
	 * Sets the marker end.
	 * 
	 * @param markerEnd The value to set.
	 */
	public void setMarkerEnd(Marker markerEnd)
	{
		if (markerEnd == null) return;
		Polyline shape = (Polyline) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		shape.setMarkerEnd(markerEnd);
	}

}
