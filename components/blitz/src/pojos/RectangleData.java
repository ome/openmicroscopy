/*
 * pojos.RectangleData
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
import omero.model.Rect;
import omero.model.RectI;
import omero.model.Shape;

/**
 * Represents a rectangle in the Euclidean space <b>R</b><sup>2</sup>.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @since 3.0-Beta4
 */
public class RectangleData 
	extends ShapeData
{

	/**
	 * Creates a new instance.
	 * 
	 * @param shape The shape this object represents.
	 */
	public RectangleData(Shape shape)
	{
		super(shape);
	}
	
	/** Creates a new instance with a default rectangle. */
	public RectangleData()
	{
		this(0.0, 0.0, 0.0, 0.0);
	}
	
	/**
	 * Creates a new instance of the RectangleData.
	 * 
	 * @param x The x-coordinate of the top-left corner.
	 * @param y The y-coordinate of the top-left corner.
	 * @param width The width of the rectangle.
	 * @param heightThe height of the rectangle.
	 */
	public RectangleData(double x, double y, double width, double height)
	{
		super(new RectI(), true);
		setX(x);
		setY(y);
		setWidth(width);
		setHeight(height);
	}
	
	/**
	 * Returns the text of the shape.
	 * 
	 * @return See above.
	 */
	public String getText()
	{
		Rect shape = (Rect) asIObject();
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
		Rect shape = (Rect) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		shape.setTextValue(rtypes.rstring(text));
	}

	/**
	 * Returns the x-coordinate of the top-left corner of an untransformed 
	 * rectangle.
	 * 
	 * @return See above.
	 */
	public double getX()
	{
		Rect shape = (Rect) asIObject();
		RDouble value = shape.getX();
		if (value == null) return 0;
		return value.getValue();
	}
	
	/**
	 * Sets the x-coordinate of the top-left corner of an 
	 * untransformed rectangle.
	 * 
	 * @param x See above.
	 */
	public void setX(double x)
	{
		if(isReadOnly())
			throw new IllegalArgumentException("Shape ReadOnly");
		Rect shape = (Rect) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		shape.setX(rtypes.rdouble(x));
	}
	
	/**
	 * Returns the y-coordinate of the top-left corner of an 
	 * untransformed rectangle.
	 * 
	 * @return See above.
	 */
	public double getY()
	{
		Rect shape = (Rect) asIObject();
		RDouble value = shape.getY();
		if (value == null) return 0;
		return value.getValue();
	}
	
	/**
	 * Sets the y-coordinate of the top-left corner of an untransformed 
	 * rectangle.
	 * 
	 * @param y See above.
	 */
	public void setY(double y)
	{
		if (isReadOnly())
			throw new IllegalArgumentException("Shape ReadOnly");
		Rect shape = (Rect) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		shape.setY(rtypes.rdouble(y));
	}
	
	/**
	 * Returns the width untransformed rectangle.
	 * 
	 * @return See above.
	 */
	public double getWidth()
	{
		Rect shape = (Rect) asIObject();
		RDouble value = shape.getWidth();
		if (value == null) return 0;
		return value.getValue();
	}
	
	/**
	 * Sets width of an untransformed rectangle.
	 * 
	 * @param width See above.
	 */
	public void setWidth(double width)
	{
		if (isReadOnly())
			throw new IllegalArgumentException("Shape ReadOnly");
		Rect shape = (Rect) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		shape.setWidth(rtypes.rdouble(width));
	}

	/**
	 * Returns the height untransformed rectangle.
	 * 
	 * @return See above.
	 */
	public double getHeight()
	{
		Rect shape = (Rect) asIObject();
		RDouble value = shape.getHeight();
		if (value == null) return 0;
		return value.getValue();
	}
	
	/**
	 * Sets the height of an untransformed rectangle.
	 * 
	 * @param height See above.
	 */
	public void setHeight(double height)
	{
		if (isReadOnly())
			throw new IllegalArgumentException("Shape ReadOnly");
		Rect shape = (Rect) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		shape.setHeight(rtypes.rdouble(height));
	}
	
}
