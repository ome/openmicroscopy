/*
 * org.openmicroscopy.shoola.util.ui.measurement.ui.figures.MeasureEllipseTextFigure 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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

package org.openmicroscopy.shoola.util.roi.figures;

// Java imports
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;

//Third-party libraries
import static org.jhotdraw.draw.AttributeKeys.FILL_COLOR;
import static org.jhotdraw.draw.AttributeKeys.FONT_UNDERLINED;
import static org.jhotdraw.draw.AttributeKeys.FONT_SIZE;
import static org.jhotdraw.draw.AttributeKeys.FONT_FACE;
import static org.jhotdraw.draw.AttributeKeys.TEXT_COLOR;
import static org.jhotdraw.draw.AttributeKeys.TEXT;

import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.TextHolderFigure;
import org.jhotdraw.draw.Tool;
import org.jhotdraw.geom.Geom;
import org.jhotdraw.geom.Insets2D;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.roi.figures.textutil.MeasureTextTool;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class RectAnnotationFigure 
	extends MeasureRectangleFigure
	implements TextHolderFigure, ROIFigure
{
	private boolean 						editable = true;
	private boolean 						displayText = true;
	
	private	Rectangle2D 					bounds;
	
	// cache of the TextFigure's layout
	transient private  	TextLayout			textLayout;
	private				Rectangle2D.Double	textBounds;
	
	public RectAnnotationFigure() 
	{
		this("Text", 0, 0, 30, 20);
	}

	public RectAnnotationFigure(double x, double y, double w, double h)
	{
		this("Text", x, y, w, h);
	}

	public RectAnnotationFigure(String text) 
	{
		this(text, 0, 0, 0, 0);
	}

	public RectAnnotationFigure(String text, double x, double y, double w, double h) 
	{
		super(x, y, w, h);
		setText(text);
		textLayout = null;
		textBounds = null;
	}
	
	protected void drawFill(java.awt.Graphics2D g) 
	{
		super.drawFill(g);
		 drawText(g);
	}

	protected void drawText(java.awt.Graphics2D g) 
	{
		if(displayText)
			if (getText()!=null || isEditable()) 
			{	
				TextLayout layout = getTextLayout();
				setTextBounds(g);
				layout.draw(g, (float) textBounds.x, (float)textBounds.y);
			}
	}

	protected void setTextBounds(Graphics2D g) 
	{
	textBounds = new Rectangle2D.Double(getTextX(g), getTextY(g),
				getTextWidth(g), getTextHeight(g));
	}

	protected double getTextX(Graphics2D g) 
	{
		return (rectangle.getX()+(rectangle.getWidth()/2) - (getTextWidth(g)/2));
	}

	protected double getTextY(Graphics2D g) 
	{
		return (rectangle.getCenterY()) + getTextHeight(g)/2;
	}

	protected double getTextWidth(Graphics2D g) 
	{
		return g.getFontMetrics(FONT_FACE.get(this)).stringWidth(getText().trim());
	}

	protected double getTextHeight(Graphics2D g) 
	{
		return g.getFontMetrics(FONT_FACE.get(this)).getAscent();
	}

	protected Rectangle2D.Double getTextBounds() 
	{
		if (textBounds == null)
			return new Rectangle2D.Double(0, 0, 0, 0);
		else
			return textBounds;
	}

	public Rectangle2D.Double getDrawingArea() 
	{
		Rectangle2D.Double r = (Rectangle2D.Double) rectangle.clone();
		double grow = AttributeKeys.getPerpendicularHitGrowth(this) + 1d;
		Geom.grow(r, grow, grow);
		r.add(getTextBounds());
		return r;
	}
	
	public void invalidate() {
        super.invalidate();
        textLayout = null;
    }
    
    protected void validate() {
        super.validate();
        textLayout = null;
    }
    
	/**
	 * Returns a specialized tool for the given coordinate.
	 * <p>Returns null, if no specialized tool is available.
	 */
	public Tool getTool(Point2D.Double p) 
	{
		if(isEditable() && (textBounds.contains(p) || contains(p))) 
		{
			invalidate();
			displayText = false;
			return new MeasureTextTool(this); 
		}
		return null;
	}

	private TextLayout getTextLayout() 
	{
		if (textLayout == null) {
			String text = getText();
			if (text == null || text.length() == 0) 
			{
				text = " ";
			}

			FontRenderContext frc = getFontRenderContext();
			HashMap<TextAttribute, Object> textAttributes = new HashMap<TextAttribute, Object>();
			textAttributes.put(TextAttribute.FONT, getFont());
			if (FONT_UNDERLINED.get(this)) 
			{
				textAttributes.put(TextAttribute.UNDERLINE,
						TextAttribute.UNDERLINE_LOW_ONE_PIXEL);
			}
			textLayout = new TextLayout(text, textAttributes, frc);
		}
		return textLayout;
	}

	// ATTRIBUTES
	/**
	 * Gets the text shown by the text figure.
	 */
	public String getText() 
	{
		return (String) getAttribute(TEXT);
	}

	/**
	 * Sets the text shown by the text figure.
	 */
	public void setText(String newText) 
	{
		displayText = true;
		setAttribute(TEXT, newText);
	}

	public int getTextColumns() 
	{
		return (getText() == null) ? 4 : Math.max(getText().length(), 4);
	}

	/**
	 * Gets the number of characters used to expand tabs.
	 */
	public int getTabSize() 
	{
		return 8;
	}

	public TextHolderFigure getLabelFor() 
	{
		return this;
	}

	public Insets2D.Double getInsets() 
	{
		return new Insets2D.Double();
	}

	public Font getFont() 
	{
		return AttributeKeys.getFont(this);
	}

	public Color getTextColor() 
	{
		return TEXT_COLOR.get(this);
	}

	public Color getFillColor() 
	{
		return FILL_COLOR.get(this);
	}

	public void setFontSize(float size) 
	{
		//    FONT_SIZE.set(this, new Double(size));
	}

	public float getFontSize() 
	{
		return FONT_SIZE.get(this).floatValue();
	}

	// EDITING
	public boolean isEditable() 
	{
		return editable;
	}

	public void setEditable(boolean b) 
	{
		this.editable = b;
	}
}
