/*
 * pojos.PolygonData
 *
Ê*------------------------------------------------------------------------------
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
import java.util.StringTokenizer;
import java.awt.geom.Point2D;

//Third-party libraries

//Application-internal dependencies
import omero.RDouble;
import omero.RString;
import omero.rtypes;
import omero.model.Shape;
import omero.model.Polyline;

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
public class PolylineData 
	extends ShapeData
{

	/** The points in the polygon as list. */
	List<Point2D> points;

	/**
	 * Creates a new instance.
	 * 
	 * @param shape The shape this object represents.
	 */
	public PolylineData(Shape shape)
	{
		super(shape);
		points = parsePointsToList();
	}
	
	/**
	 * Returns the points in the polygon.
	 * 
	 * @return See above.
	 */
	public List<Point2D> getPoints()
	{
		return points;
	}
	
	
	/**
	 * Set the points in the polygon.
	 * 
	 * @param points See above.
	 */
	public void setPoints(List<Point2D> points)
	{
		if(isReadOnly())
			throw new IllegalArgumentException("Shape ReadOnly");
		Polyline shape = (Polyline) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		String pts = "";
		Point2D pt;
		for(int cnt = 0 ; cnt < points.size()-1 ; cnt++)
		{
			pt = points.get(cnt);
			pts = pts + pt.getX() + "," + pt.getY() + ",";
		}
		pt = points.get(points.size()-1);
		pts = pts + pt.getX() + "," + pt.getY();
		shape.setPoints(rtypes.rstring(pts));
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
		if(isReadOnly())
			throw new IllegalArgumentException("Shape ReadOnly");
		Polyline shape = (Polyline) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		shape.setTextValue(rtypes.rstring(text));
	}
	
	
	/** 
	* Parse the points list from the Polyline object to a list of 
	* point2d objects.
	*/
	private List<Point2D> parsePointsToList()
	{
		List<Point2D> points = new ArrayList<Point2D>();
		Polyline shape = (Polyline) asIObject();
		RString value = shape.getPoints();
		if (value == null) return points;
		String str = value.getValue();

		StringTokenizer tt=new StringTokenizer(str, " ,");
		int numTokens = tt.countTokens()/2;
		for (int i=0; i< numTokens; i++)
			points.add(
					new Point2D.Double(new Double(tt.nextToken()), new Double(
						tt.nextToken())));
		return points;
	}

}
