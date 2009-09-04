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
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;

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

	/** The <code>Butt</code> descriptor. */
	public static final String	LINE_CAP_BUTT = "Butt";
	
	/** The <code>Round</code> descriptor. */
	public static final String	LINE_CAP_ROUND = "Round";
	
	/** The <code>Square</code> descriptor. */
	public static final String	LINE_CAP_SQUARE = "Square";
	
	/** The default fill color. */
	public final static Color DEFAULT_FILL_COLOUR = new Color(0, 0, 0, 64);
	
	/** The default fill color. */
	public final static Color DEFAULT_FILL_COLOUR_ALPHA = new Color(0, 0, 0, 
			32);
	
	/** The default stroke color. */
	public final static Color DEFAULT_STROKE_COLOUR = new Color(196, 196, 196, 
			196);

	/** The default font size. */
	public static final int 	DEFAULT_FONT_SIZE = 12;
	
	/** The default font style. */
	public static final int 	DEFAULT_FONT_STYLE = Font.PLAIN;
	
	/** The default font family. */
	public static final String 	DEFAULT_FONT_FAMILY = "Courier";
	
	/** The default stroke width. */
	public final static double DEFAULT_STROKE_WIDTH =  1.0f;
	
	/**
	 * Converts the string into a color.
	 * 
	 * @param value The value to convert.
	 * @return
	 */
	private Color stringToColor(String value)
	{
		if (value == null) return null;
		return Color.decode(value);
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
		if (value == null) return DEFAULT_FILL_COLOUR;
		Color c = stringToColor(value.getValue());
		if (c == null) return DEFAULT_FILL_COLOUR;
		return c;
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
		if (value == null) return DEFAULT_STROKE_COLOUR;
		Color c = stringToColor(value.getValue());
		if (c == null) return DEFAULT_STROKE_COLOUR;
		return c;
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
	 * Returns the stroke dashes.
	 * 
	 * @return See above.
	 */
	public double[] getStrokeDashArray()
	{
		Shape shape = (Shape) asIObject();
		RString value = shape.getStrokeDashArray();
		if (value == null) return null;
		String v = value.getValue();
		String[] values = v.split("\\s*,\\s*");
		double[] dashes = new double[values.length];
        for (int i = 0; i < values.length; i++) 
            dashes[i] = new Double(values[i]);
		return dashes;
	}
	
	/**
	 * Returns the stroke.
	 * 
	 * @return See above.
	 */
	public int getLineCap()
	{
		Shape shape = (Shape) asIObject();
		RString value = shape.getStrokeLineCap();
		if (value == null) return BasicStroke.CAP_BUTT;
		String v = value.getValue();
		if (LINE_CAP_BUTT.equals(v)) return BasicStroke.CAP_BUTT;
		else if (LINE_CAP_ROUND.equals(v)) return BasicStroke.CAP_ROUND;
		else if (LINE_CAP_SQUARE.equals(v)) return BasicStroke.CAP_SQUARE;
		return BasicStroke.CAP_BUTT;
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
	public int getFontStyle()
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
	
	/**
	 * Returns the font.
	 * 
	 * @return See above.
	 */
	public Font getFont()
	{
		return new Font(getFontFamily(), getFontStyle(), getFontSize());
	}
	
	/**
	 * Returns <code>true</code> if it is italic, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isFontItalic() { return getFontStyle() == Font.ITALIC; }
	
	/**
	 * Returns <code>true</code> if it is bold, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isFontBold() { return getFontStyle() == Font.BOLD; }
	
}
