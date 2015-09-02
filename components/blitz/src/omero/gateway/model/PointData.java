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


import omero.RDouble;
import omero.RString;
import omero.rtypes;
import omero.model.Point;
import omero.model.PointI;
import omero.model.Shape;

/**
 * Represents a point in the Euclidean space <b>R</b><sup>2</sup>.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class PointData
	extends ShapeData
{

	/**
	 * Creates a new instance.
	 * 
	 * @param shape The shape to host.
	 */
	public PointData(Shape shape)
	{
		super(shape);
	}
	

	/**
	 * Create a new instance of PointData, creating a new pointI Object.
	 */
	public PointData()
	{
		this(0.0, 0.0);
	}
	
	/**
	 * Create a new instance of the PointData,
	 * 
	 * @param x x-coordinate of the shape.
	 * @param y y-coordinate of the shape.
	 */
	public PointData(double x, double y)
	{
		super(new PointI(), true);
		setX(x);
		setY(y);
	}
	
	/**
	 * Returns the text of the shape.
	 * 
	 * @return See above.
	 */
	public String getText()
	{
		Point shape = (Point) asIObject();
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
		Point shape = (Point) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		shape.setTextValue(rtypes.rstring(text));
	}
	
	
	/**
	 * Returns the x-coordinate of the shape.
	 * 
	 * @return See above.
	 */
	public double getX()
	{
		Point shape = (Point) asIObject();
		if (shape == null) return 0;
		RDouble value = shape.getCx();
		if (value == null) return 0;
		return value.getValue();
	}
	
	/**
	 * set the x-coordinate of the shape.
	 * 
	 * @param x See above.
	 */
	public void setX(double x)
	{
		if (isReadOnly())
			throw new IllegalArgumentException("Shape ReadOnly");
		Point shape = (Point) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		shape.setCx(rtypes.rdouble(x));
	}
	
	/**
	 * Returns the y coordinate of the shape.
	 * 
	 * @return See above.
	 */
	public double getY()
	{
		Point shape = (Point) asIObject();
		if (shape == null) return 0;
		RDouble value = shape.getCy();
		if (value == null) return 0;
		return value.getValue();
	}
	
	/**
	 * set the y-coordinate of the shape.
	 * 
	 * @param y See above.
	 */
	public void setY(double y)
	{
		if (isReadOnly())
			throw new IllegalArgumentException("Shape ReadOnly");
		Point shape = (Point) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		shape.setCy(rtypes.rdouble(y));
	}
	
}
