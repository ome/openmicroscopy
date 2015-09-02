/*
*------------------------------------------------------------------------------
*  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
*
*
*  This program is free software; you can redistribute it and/or modify
*  it under the terms of the GNU General Public License as published by
*  the Free Software Foundation; either version 2 of the License, or
*  (at your option) any later version.
*  This program is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU General Public License for more details.
*
*  You should have received a copy of the GNU General Public License along
*  with this program; if not, write to the Free Software Foundation, Inc.,
*  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*
*------------------------------------------------------------------------------
*/
package omero.gateway.model;

import omero.RDouble;
import omero.RString;
import omero.rtypes;
import omero.model.Shape;
import omero.model.Label;
import omero.model.LabelI;

/**
 * Represents a Text in the Euclidean space <b>R</b><sup>2</sup>.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class TextData 
	extends ShapeData
{

	/**
	 * Creates a new instance of Text data from an existing shape.
	 * 
	 * @param shape The shape this object represents.
	 */
	public TextData(Shape shape)
	{
		super(shape);
	}
	
	/** Creates a new instance of TextData, creating a new TextI Object. */
	public TextData()
	{
		this("String", 0.0, 0.0);
	}
	
	/**
	 * Creates a new instance of the TextData, sets the centre and major, minor
	 * axes.
	 * 
	 * @param text Object text.
	 * @param x X-Coordinate of the text.
	 * @param y Y-Coordinate of the text.
	 */
	public TextData(String text, double x, double y)
	{
		super(new LabelI(), true);
		setX(x);
		setY(y);
		setText(text);
	}
	
	/**
	 * Returns the text of the shape.
	 * 
	 * @return See above.
	 */
	public String getText()
	{
		Label shape = (Label) asIObject();
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
		Label shape = (Label) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		shape.setTextValue(rtypes.rstring(text));
	}

	
	/**
	 * Returns the x-coordinate text field.
	 * 
	 * @return See above.
	 */
	public double getX()
	{
		Label shape = (Label) asIObject();
		RDouble value = shape.getX();
		if (value == null) return 0;
		return value.getValue();
	}
	
	/**
	 * Sets the x-coordinate of the text field.
	 * 
	 * @param x See above.
	 */
	public void setX(double x)
	{
		if(isReadOnly())
			throw new IllegalArgumentException("Shape ReadOnly");
		Label shape = (Label) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		shape.setX(rtypes.rdouble(x));
	}

	/**
	 * Returns the y-coordinate text field.
	 * 
	 * @return See above.
	 */
	public double getY()
	{
		Label shape = (Label) asIObject();
		RDouble value = shape.getY();
		if (value == null) return 0;
		return value.getValue();
	}
	
	/**
	 * Sets the y-coordinate of the text field.
	 * 
	 * @param y See above.
	 */
	public void setY(double y)
	{
		if (isReadOnly())
			throw new IllegalArgumentException("Shape ReadOnly");
		Label shape = (Label) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		shape.setY(rtypes.rdouble(y));
	}
	
}


