/*
 * pojos.LineData
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

import omero.RDouble;
import omero.RString;
import omero.rtypes;
import omero.model.Line;
import omero.model.LineI;
import omero.model.Shape;

/**
 * Represents a line in the Euclidean space <b>R</b><sup>2</sup>.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @since 3.0-Beta4
 */
public class LineData 
	extends ShapeData
{

	/**
	 * Creates a new instance.
	 * 
	 * @param shape The shape to host.
	 */
	public LineData(Shape shape)
	{
		super(shape);
	}
	
	/**
	 * Create a new instance of LineData, creating a new LineI Object.
	 */
	public LineData()
	{
		this(0.0, 0.0, 0.0, 0.0);
	}
	
	/**
	 * Create a new instance of the LineData, 
	 * @param x1 x1-coordinate of the shape.
	 * @param y1 y1-coordinate of the shape.
	 * @param x2 x2-coordinate of the shape.
	 * @param y2 y2-coordinate of the shape.
	 */
	public LineData(double x1, double y1, double x2, double y2)
	{
		super(new LineI(), true);
		setX1(x1);
		setY1(y1);
		setX2(x2);
		setY2(y2);
	}
	
	/**
	 * Returns the text of the shape.
	 * 
	 * @return See above.
	 */
	public String getText()
	{
		Line shape = (Line) asIObject();
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
		Line shape = (Line) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		shape.setTextValue(rtypes.rstring(text));
	}
	
	
	/**
	 * Returns the x-coordinate of the starting point of an untransformed line.
	 * 
	 * @return See above.
	 */
	public double getX1()
	{
		Line shape = (Line) asIObject();
		RDouble value = shape.getX1();
		if (value == null) return 0;
		return value.getValue();
	}
	
	/**
	 * Set the x-coordinate of the starting point of an untransformed line.
	 * 
	 * @param x1 See above.
	 */
	public void setX1(double x1)
	{
		if(isReadOnly())
			throw new IllegalArgumentException("Shape ReadOnly");
		Line shape = (Line) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		shape.setX1(rtypes.rdouble(x1));
	}
	
	/**
	 * Returns the x-coordinate of the end point of an untransformed line.
	 * 
	 * @return See above.
	 */
	public double getX2()
	{
		Line shape = (Line) asIObject();
		RDouble value = shape.getX2();
		if (value == null) return 0;
		return value.getValue();
	}
	
	/**
	 * Set the x-coordinate of the end point of an untransformed line.
	 * 
	 * @param x2 See above.
	 */
	public void setX2(double x2)
	{
		if(isReadOnly())
			throw new IllegalArgumentException("Shape ReadOnly");
		Line shape = (Line) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		shape.setX2(rtypes.rdouble(x2));
	}
	
	/**
	 * Returns the y-coordinate of the starting point of an untransformed line.
	 * 
	 * @return See above.
	 */
	public double getY1()
	{
		Line shape = (Line) asIObject();
		RDouble value = shape.getY1();
		if (value == null) return 0;
		return value.getValue();
	}
	
	/**
	 * Set the y-coordinate of the starting point of an untransformed line.
	 * 
	 * @param y1 See above.
	 */
	public void setY1(double y1)
	{
		if(isReadOnly())
			throw new IllegalArgumentException("Shape ReadOnly");
		Line shape = (Line) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		shape.setY1(rtypes.rdouble(y1));
	}
	
	/**
	 * Returns the y-coordinate of the end point of an untransformed line.
	 * 
	 * @return See above.
	 */
	public double getY2()
	{
		Line shape = (Line) asIObject();
		RDouble value = shape.getY2();
		if (value == null) return 0;
		return value.getValue();
	}

	/**
	 * Set the y-coordinate of the end point of an untransformed line.
	 * 
	 * @param y2 See above.
	 */
	public void setY2(double y2)
	{
		if(isReadOnly())
			throw new IllegalArgumentException("Shape ReadOnly");
		Line shape = (Line) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		shape.setY2(rtypes.rdouble(y2));
	}
	
}
