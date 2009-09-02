/*
 * pojos.ShapeSettingsData
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
 *
 *
 *     This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package pojos;

//Java imports
import java.awt.Color;

//Third-party libraries

//Application-internal dependencies
import omero.RInt;
import omero.RString;
import omero.model.Shape;

/**
 * Stores the settings related to a given shape.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *     <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author    Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 *     <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class ShapeSettingsData
	extends DataObject
{

	/** The default color. */
	public static final Color 	DEFAULT_COLOR = Color.RED;
	
	/** The default font size. */
	public static final int 	DEFAULT_FONT_SIZE = 12;
	
	/** The default font style. */
	public static final String 	DEFAULT_FONT_STYLE = "Regular";
	
	/** The default font family. */
	public static final String 	DEFAULT_FONT_FAMILY = "Courier";
	
	/**
	 * Converts the string into a color.
	 * 
	 * @param value The value to convert.
	 * @return
	 */
	private Color stringToColor(String value)
	{
		if (value == null) return DEFAULT_COLOR;
		return DEFAULT_COLOR;
	}
	/**
	 * Creates a new instance.
	 * 
	 * @param shape The shape the settings are for.
	 */
	ShapeSettingsData(Shape shape)
	{
		super();
		setValue(shape);
	}
	
	/**
	 * Returns the fill rule.
	 * 
	 * @return See above.
	 */
	public String getFillRule()
	{
		Shape shape = (Shape) asIObject();
		RString value = shape.getFillRule();
		if (value == null) return "";
		return value.getValue();
	}
	
	/**
	 * Returns the fill color.
	 * 
	 * @return See above.
	 */
	public Color getFillColor()
	{
		Shape shape = (Shape) asIObject();
		RString value = shape.getFillColor();
		if (value == null) return stringToColor(null);
		return stringToColor(value.getValue());
	}
	
	/**
	 * Returns the color of the stroke.
	 * 
	 * @return See above.
	 */
	public Color getStrokeColor()
	{
		Shape shape = (Shape) asIObject();
		RString value = shape.getStrokeColor();
		if (value == null) return stringToColor(null);
		return stringToColor(value.getValue());
	}

	/**
	 * Returns the stroke's width.
	 * 
	 * @return See above.
	 */
	public double getStrokeWidth()
	{
		Shape shape = (Shape) asIObject();
		RInt value = shape.getStrokeWidth();
		if (value == null) return 1;
		return value.getValue();
	}
	
	/**
	 * Returns the stroke.
	 * 
	 * @return See above.
	 */
	public String getStrokeDashArray()
	{
		Shape shape = (Shape) asIObject();
		RString value = shape.getStrokeDashArray();
		if (value == null) return "";
		return value.getValue();
	}
	
	/**
	 * Returns the stroke.
	 * 
	 * @return See above.
	 */
	public String getLineCap()
	{
		Shape shape = (Shape) asIObject();
		RString value = shape.getStrokeLineCap();
		if (value == null) return "";
		return value.getValue();
	}
	
	/**
	 * Returns the stroke.
	 * 
	 * @return See above.
	 */
	public String getFontFamily()
	{
		return DEFAULT_FONT_FAMILY;
	}
	
	/**
	 * Returns the stroke.
	 * 
	 * @return See above.
	 */
	public int getFontSize()
	{
		return DEFAULT_FONT_SIZE;
	}

	/**
	 * Returns the stroke.
	 * 
	 * @return See above.
	 */
	public String getFontStyle()
	{
		return DEFAULT_FONT_STYLE;
	}

	/**
	 * Returns the stroke.
	 * 
	 * @return See above.
	 */
	public String getMarkerStart()
	{
		return "";
	}
	
	/**
	 * Returns the stroke.
	 * 
	 * @return See above.
	 */
	public String getMarkerEnd()
	{
		return "";
	}
	
	/**
	 * Returns the stroke.
	 * 
	 * @return See above.
	 */
	public String getText()
	{
		return "";
	}
	
}
