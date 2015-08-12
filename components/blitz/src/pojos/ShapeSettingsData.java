/*
 * pojos.ShapeSettingsData
 *
 *------------------------------------------------------------------------------
 * Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;

import ome.formats.model.UnitsFactory;
import ome.model.units.BigResult;
import omero.RInt;
import omero.RString;
import omero.rtypes;
import omero.model.Length;
import omero.model.LengthI;
import omero.model.Shape;
import omero.model.enums.UnitsLength;

/**
 * Stores the settings related to a given shape.
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

	/** The default font size in "pt". */
	public static final int DEFAULT_FONT_SIZE = 12;
	
	/** The default font family. */
	public static final String 	DEFAULT_FONT_FAMILY = "sans-serif";
	
	/** The default stroke width. */
	public final static double DEFAULT_STROKE_WIDTH = 1.0f;

	/** Set if font italic. */
	public final static String FONT_ITALIC = "Italic";
	
	/** Set if font bold. */
	public final static String FONT_BOLD = "Bold";

	/** Set if font bold. */
	public final static String FONT_BOLD_ITALIC = "BoldItalic";
	
	/** Set if font bold. */
	public final static String FONT_REGULAR = "Normal";
	
	/**
	 * Returns the font style is supported.
	 * 
	 * @param value The value to format.
	 * @return See above.
	 */
	private String formatFontStyle(String value)
	{
		if (value == null) return FONT_REGULAR;
		String v = value.trim();
		v = v.toLowerCase();
		if (FONT_ITALIC.toLowerCase().equals(v)) return FONT_ITALIC;
		else if (FONT_BOLD.toLowerCase().equals(v)) return FONT_BOLD;
		else if (FONT_BOLD_ITALIC.toLowerCase().equals(v)) 
			return FONT_BOLD_ITALIC;
		return FONT_REGULAR;
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
		setFontStyle(FONT_REGULAR);
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
	 * Set the fill rule.
	 * 
	 * @param fillRule See above.
	 */
	public void setFillRule(String fillRule)
	{
		Shape shape = (Shape) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		shape.setFillRule(rtypes.rstring(fillRule));
		setDirty(true);
	}
	
	/**
	 * Returns the fill color.
	 * 
	 * @return See above.
	 */
	public Color getFill()
	{
		Shape shape = (Shape) asIObject();
		RInt value = shape.getFillColor();
		if (value == null) return DEFAULT_FILL_COLOUR;
		Color c = new Color(value.getValue(), true);
        if (c.getAlpha() == 0) return new Color(value.getValue(), false);
        return c;
	}
	
	/**
	 * Set the fill color.
	 * 
	 * @param fillColour See above.
	 */
	public void setFill(Color fillColour)
	{
		Shape shape = (Shape) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		if (fillColour == null) return;
		shape.setFillColor(rtypes.rint(fillColour.getRGB()));
		setDirty(true);
	}

	
	/**
	 * Returns the color of the stroke.
	 * 
	 * @return See above.
	 */
	public Color getStroke()
	{
		Shape shape = (Shape) asIObject();
		RInt value = shape.getStrokeColor();
		if (value == null) return DEFAULT_STROKE_COLOUR;
		Color c = new Color(value.getValue(), true);
		if (c.getAlpha() == 0) return new Color(value.getValue(), false);
		return c;
	}

	/**
	 * Set the stroke color.
	 * 
	 * @param strokeColour See above.
	 */
	public void setStroke(Color strokeColour)
	{
		Shape shape = (Shape) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		if (strokeColour == null) return;
		shape.setStrokeColor(rtypes.rint(strokeColour.getRGB()));
		setDirty(true);
	}
	
	/**
	 * Returns the stroke's width.
	 * 
	 * @return See above.
	 * @deprecated Replaced by {@link #getStrokeWidth(UnitsLength)}
	 */
	@Deprecated
	public double getStrokeWidth()
	{
		Shape shape = (Shape) asIObject();
		Length value = shape.getStrokeWidth();
		if (value == null) return 1.0;
		try {
            return new LengthI(value, UnitsFactory.Shape_StrokeWidth).getValue();
        } catch (BigResult e) {
            return value.getValue();
        }
	}
	
	/**
	 * Returns the stroke's width (or 1 px if it's not set or <= 0)
	 * 
	 * @param unit
	 *            The unit (may be null, in which case no conversion will be
	 *            performed)
	 * @return See above.
	 * @throws BigResult If an arithmetic under-/overflow occurred 
	 */
	public Length getStrokeWidth(UnitsLength unit) throws BigResult
	{
		Shape shape = (Shape) asIObject();
		Length value = shape.getStrokeWidth();
		if (value == null || value.getValue()<=0) 
			return new LengthI(1, UnitsLength.PIXEL);
		return unit == null ? value : new LengthI(value, unit);
	}

	/**
	 * Set the stroke width.
	 * 
	 * @param strokeWidth See above.
	 * @deprecated Replaced by {@link #setStrokeWidth(Length)}
	 */
	@Deprecated
	public void setStrokeWidth(Double strokeWidth)
	{
		Shape shape = (Shape) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		shape.setStrokeWidth(new LengthI(strokeWidth, UnitsFactory.Shape_StrokeWidth));
		setDirty(true);
	}
	
	/**
	 * Set the stroke width.
	 * 
	 * @param strokeWidth See above.
	 */
	public void setStrokeWidth(Length strokeWidth)
	{
		Shape shape = (Shape) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		shape.setStrokeWidth(strokeWidth);
		setDirty(true);
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
	 * Set the stroke dashes.
	 * 
	 * @param dashArray See above.
	 */
	public void setStrokeDashArray(double [] dashArray)
	{
		Shape shape = (Shape) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		if (dashArray == null || dashArray.length < 2) return;
		StringBuffer values = new StringBuffer();
		for(int i = 0 ; i < dashArray.length-1 ; i++)
			values.append(dashArray[i]+ ",");
		String v = values.toString() + dashArray[dashArray.length-1];
		shape.setStrokeDashArray(rtypes.rstring(v));
		setDirty(true);
	}
	
	/**
	 * Returns the shape of the end of the line..
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
	 * Sets the line cap.
	 * 
	 * @param lineCap See above.
	 */
	public void setLineCap(int lineCap)
	{
		Shape shape = (Shape) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		switch(lineCap)
		{
			case BasicStroke.CAP_BUTT:
				shape.setStrokeLineCap(rtypes.rstring(LINE_CAP_BUTT));
				break;
			case BasicStroke.CAP_ROUND:
				shape.setStrokeLineCap(rtypes.rstring(LINE_CAP_BUTT));
				break;
			case BasicStroke.CAP_SQUARE:
				shape.setStrokeLineCap(rtypes.rstring(LINE_CAP_BUTT));
				break;
			default:
				shape.setStrokeLineCap(rtypes.rstring(LINE_CAP_BUTT));
				break;
		}
		setDirty(true);
	}
	
	/** 
	 * Get the style of the font for Shape. 
	 * @return See above.
	 */
	public Font getFont()
	{
		int style = Font.PLAIN;
		if (isFontBold())
			style = style | Font.BOLD;
		if (isFontItalic())
			style = style | Font.ITALIC;
		return new Font(getFontFamily(), style, (int) getFontSize());
	}
	
	/**
	 * Returns the stroke.
	 * 
	 * @return See above.
	 */
	public String getFontFamily()
	{
		Shape shape = (Shape) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		RString family = shape.getFontFamily();
		if (family != null) return family.getValue();
		return DEFAULT_FONT_FAMILY;
	}

	/**
	 * Returns the stroke.
	 */
	public void setFontFamily(String fontFamily)
	{
		Shape shape = (Shape) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		if (fontFamily == null || fontFamily.trim().length() == 0)
			fontFamily = DEFAULT_FONT_FAMILY;
		shape.setFontFamily(rtypes.rstring(fontFamily));
		setDirty(true);
	}
	
	/**
	 * Returns the stroke.
	 * 
	 * @return See above.
	 * @deprecated Replaced by {@link #getFontSize(UnitsLength)}
	 */
	@Deprecated
	public double getFontSize()
	{
		Shape shape = (Shape) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		Length size = shape.getFontSize();
		if (size != null) {
		    try {
		        if (size.getUnit().equals(UnitsLength.PIXEL)) {
		            return size.getValue();
		        }
                return (new LengthI(size, UnitsLength.POINT)).getValue();
            } catch (Exception e) {
                if (e instanceof BigResult) {
                    BigResult ex = (BigResult) e;
                    if (ex.result != null) {
                        return ex.result.doubleValue();
                    }
                }
            }
		}
		return DEFAULT_FONT_SIZE;
	}
	
	/**
	 * Returns the stroke.
	 * 
	 * @param unit
	 *            The unit (may be null, in which case no conversion will be
	 *            performed)
	 * @return See above.
	 * @throws BigResult If an arithmetic under-/overflow occurred
	 */
	public Length getFontSize(UnitsLength unit) throws BigResult
	{
		Shape shape = (Shape) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		Length size = shape.getFontSize();
		if (size != null) 
			return unit == null ? size : new LengthI(size, unit);
		return new LengthI(DEFAULT_FONT_SIZE, UnitsLength.POINT);
	}

    /**
	 * Set the size of the font.
	 * @deprecated Replaced by {@link #setFontSize(Length)}
	 */
	@Deprecated
	public void setFontSize(int fontSize)
	{
		Shape shape = (Shape) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		if (fontSize <= 0) fontSize = DEFAULT_FONT_SIZE;
		shape.setFontSize(new LengthI(fontSize, UnitsLength.POINT));
		setDirty(true);
	}
	
	/**
	 * Set the size of the font.
	 */
	public void setFontSize(Length fontSize)
	{
		Shape shape = (Shape) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		if (fontSize ==null)
			fontSize = new LengthI(DEFAULT_FONT_SIZE, UnitsLength.POINT);
		shape.setFontSize(fontSize);
		setDirty(true);
	}
	
	/**
	 * Returns the font style.
	 * 
	 * @return See above.
	 */
	public String getFontStyle()
	{
		Shape shape = (Shape) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		RString style = shape.getFontStyle();
		if (style != null) return style.getValue();
		return FONT_REGULAR;
	}

	/**
	 * Sets the style of the font.
	 */
	public void setFontStyle(String fontStyle)
	{
		Shape shape = (Shape) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		shape.setFontStyle(rtypes.rstring(formatFontStyle(fontStyle)));
		//update the font weight.
		shape.setFontWeight(null);
		setDirty(true);
	}

	/**
	 * Returns the marker start.
	 * 
	 * @return See above.
	 */
	public String getMarkerStart()
	{
		return "";
	}
	
	/**
	 * Returns the marker end.
	 * 
	 * @return See above.
	 */
	public String getMarkerEnd()
	{
		return "";
	}
	
	/**
	 * Returns the marker start.
	 * 
	 * @param start The value to set.
	 */
	public String setMarkerStart(String start)
	{
		return "";
	}
	
	/**
	 * Returns the marker end.
	 * 
	 * @param end The value to set.
	 */
	public String setMarkerEnd(String end)
	{
		return "";
	}
	
	/**
	 * Returns <code>true</code> if it is italic, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isFontItalic()
	{ 
		String value = getFontStyle();
		value = value.toLowerCase();
		String f = FONT_ITALIC.toLowerCase();
		String f1 = FONT_BOLD_ITALIC.toLowerCase();
		return (f.equals(value) || f1.equals(value));
	}
	
	/**
	 * Returns <code>true</code> if it is bold, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isFontBold()
	{ 
		//in order to handle the previous value.
		Shape shape = (Shape) asIObject();
		if (shape == null) 
			throw new IllegalArgumentException("No shape specified.");
		String f = FONT_BOLD.toLowerCase();
		String f1 = FONT_BOLD_ITALIC.toLowerCase();
		String value;
		RString weight = shape.getFontWeight();
		if (weight != null) {
			value = weight.getValue();
			value = value.toLowerCase();
			if (value.trim().length() > 0)
				return (f.equals(value) || f1.equals(value));
		}
		value = getFontStyle();
		value = value.toLowerCase();
		return (f.equals(value) || f1.equals(value));
	}
	
}
