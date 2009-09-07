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

	/** The points in the polygon as list. */
	List<Point2D> points;

	/**
	 * Creates a new instance.
	 * 
	 * @param shape The shape this object represents.
	 */
	public PolygonData(Shape shape)
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
	* Parse the points list from the polygon object to a list of 
	* point2d objects.
	*/
	private List<Point2D> parsePointsToList()
	{
		List<Point2D> points = new ArrayList<Point2D>();
		Polygon shape = (Polygon) asIObject();
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
