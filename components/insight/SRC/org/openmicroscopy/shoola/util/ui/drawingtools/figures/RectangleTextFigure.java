/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.ui.drawingtools.figures;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.RectangleFigure;
import org.jhotdraw.draw.TextHolderFigure;
import org.jhotdraw.draw.Tool;
import org.jhotdraw.geom.Geom;
import org.jhotdraw.geom.Insets2D;

import org.openmicroscopy.shoola.util.roi.model.annotation.MeasurementAttributes;
import org.openmicroscopy.shoola.util.ui.drawingtools.attributes.DrawingAttributes;
import org.openmicroscopy.shoola.util.ui.drawingtools.texttools.DrawingTextTool;

/**
 * A rectangle figure with text.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class RectangleTextFigure 
	extends RectangleFigure 
	implements TextHolderFigure
{
	
	private boolean 				fromAttributeUpdate;
	
	/** Flag indicating if the figure is editable or not. */
	private boolean 				editable;

	/** The bounds of the text. */
	private Rectangle2D.Double		textBounds;
	
	/** Creates a new instance. */
	public RectangleTextFigure()
	{
		this("", 0, 0, 0, 0);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param text The text to display.
	 */
	public RectangleTextFigure(String text)
	{
		this(text, 0, 0, 0, 0);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param text	The text to display.
	 * @param x		The x-coordinate.
	 * @param y		The x-coordinate. 
	 * @param w		The width.
	 * @param h		The height.
	 */
	public RectangleTextFigure(String text, double x, double y, double w,
			double h)
	{
		super(x, y, w, h);
		setAttributeEnabled(AttributeKeys.TEXT_COLOR, true);
		setAttribute(AttributeKeys.TEXT, text);
		//setAttribute(DrawingAttributes.SHOWTEXT, true);
		textBounds = null;
		editable = true;
		fromAttributeUpdate = false;
	}
	
	/** 
	 * Returns the bounds of the text.
	 * 
	 * @return See above.
	 */
	protected Rectangle2D.Double getTextBounds() 
	{
		if (textBounds == null) return new Rectangle2D.Double(0, 0, 0, 0);
		else return textBounds;
	}
	
	/**
	 * Sets the editable flag.
	 * 
	 * @param b Passed <code>true</code> to be editable, <code>false</code>
	 * 			otherwise.
	 */
	public void setEditable(boolean b) { this.editable = b; }
	
	/**
	 * Overridden to draw the text.
	 * @see RectangleFigure#drawFill(Graphics2D)
	 */
	protected void drawFill(Graphics2D g)
	{
		super.drawFill(g);
		drawText(g);
	}
	
	/**
	 * Overridden to handle the {@link MeasurementAttributes#HEIGHT}
	 * and {@link MeasurementAttributes#WIDTH}.
	 * @see #setAttribute(AttributeKey, Object)
	 */
	public void setAttribute(AttributeKey key, Object newValue) 
	{
		super.setAttribute(key, newValue);
		if (MeasurementAttributes.HEIGHT.getKey().equals(key.getKey()))
		{
			double newHeight = MeasurementAttributes.HEIGHT.get(this);
			Rectangle2D.Double bounds = getBounds();
			double centreY = bounds.getCenterY();
			double diffHeight = newHeight/2;
			Rectangle2D.Double newBounds = new Rectangle2D.Double(
				bounds.getX(), centreY-diffHeight, bounds.getWidth(),
				newHeight);
			fromAttributeUpdate = true;
			this.setBounds(newBounds);
			fromAttributeUpdate = false;
		}
		if (MeasurementAttributes.WIDTH.getKey().equals(key.getKey()))
		{
			double newWidth = MeasurementAttributes.WIDTH.get(this);
			Rectangle2D.Double bounds = getBounds();
			double centreX = bounds.getCenterX();
			double diffWidth = newWidth/2;
			Rectangle2D.Double newBounds = new Rectangle2D.Double(
				centreX-diffWidth, bounds.getY(), newWidth, 
				bounds.getHeight());
			fromAttributeUpdate = true;
			this.setBounds(newBounds);
			fromAttributeUpdate = false;
		}
	}
		 
	/**
	 * Overridden to handle the {@link MeasurementAttributes#HEIGHT}
	 * and {@link MeasurementAttributes#WIDTH}.
	 * @see #setBounds(Point2D.Double, Point2D.Double)
	 */
	public void setBounds(Point2D.Double anchor, Point2D.Double lead)
	{
		super.setBounds(anchor, lead);
		if (!fromAttributeUpdate)
		{
			MeasurementAttributes.HEIGHT.set(this, getBounds().getHeight());
			MeasurementAttributes.WIDTH.set(this, getBounds().getWidth());
		}
	}

	/**
	 * Overridden to draw the text.
	 * @see RectangleFigure#drawText(Graphics2D)
	 */
	protected void drawText(Graphics2D g)
	{
		if (!(DrawingAttributes.SHOWTEXT.get(this))) return;
		String text = getText();
		if (text != null )// && isEditable()) 
		{	
			text = text.trim();
			if (text.length() == 0) return;
			Font font = AttributeKeys.FONT_FACE.get(this);
			font = font.deriveFont(
                                AttributeKeys.FONT_SIZE.get(this).floatValue());
			
			FontMetrics fm = g.getFontMetrics(font);
			double textWidth = fm.stringWidth(text);
			
			//Determine with and height of the text.
			double width = textWidth;
			double avgCharWidth = textWidth/text.length();
                        double maxTextWidth = avgCharWidth*FigureUtil.TEXT_WIDTH;
                        if(textWidth > maxTextWidth) {
                            width = maxTextWidth;
                        }
			double textHeight = (textWidth/width+1)*(fm.getAscent()
					+fm.getDescent()+fm.getLeading());
			double x = rectangle.x+rectangle.width/2-width/2;
			double y = rectangle.y+textHeight/2;
			textBounds = new Rectangle2D.Double(x, y, width, textHeight);
			FontRenderContext frc = g.getFontRenderContext();

			// prepare font calculations
			AttributedString styledText = new AttributedString(text);
			FigureUtil.formatLayout(font, styledText, this);
			AttributedCharacterIterator i = styledText.getIterator();
			LineBreakMeasurer measurer = new LineBreakMeasurer(i, frc);

			// draw
            Color c = AttributeKeys.STROKE_COLOR.get(this);
            if (c != null) {
                g.setColor(c);
            }
			int w = (int) width;
			TextLayout layout;
			while (measurer.getPosition() < text.length()) {
				layout = measurer.nextLayout(w);
				y += layout.getAscent();
				layout.draw(g, (float) x, (float) y);
				y += layout.getDescent()+layout.getLeading();
			}
		}	
	}

	/**
	 * Overridden to return the bounds of the text area.
	 * @see RectangleFigure#getDrawingArea()
	 */
	public Rectangle2D.Double getDrawingArea()
	{
		Rectangle2D.Double r = (Rectangle2D.Double) rectangle.clone();
		double grow = AttributeKeys.getPerpendicularHitGrowth(this)+1d;
		Geom.grow(r, grow, grow);
		r.add(getTextBounds());
		return r;
	}
	
	/** 
	 * Overridden to set the layout to <code>null</code>.
	 * @see RectangleFigure#invalidate()
	 */
	public void invalidate() 
	{
		super.invalidate();
	}

	/** 
	 * Overridden to set the layout to <code>null</code>.
	 * @see RectangleFigure#validate()
	 */
	public void validate() 
	{
		super.validate();
	}
	
	/**
	 * Overridden to set the correct tool.
	 * @see RectangleFigure#getTool(Point2D.Double)
	 */
	public Tool getTool(Point2D.Double p)
	{
		if (isEditable() && contains(p))  {
			invalidate();
			return new DrawingTextTool(this); 
		}
		return null;
	}

	/**
	 * Implemented as specified by the {@link TextHolderFigure} I/F.
	 * @see TextHolderFigure#getText()
	 */
	public String getText()
	{ 
		return (String) getAttribute(AttributeKeys.TEXT); 
	}
	
	/**
	 * Implemented as specified by the {@link TextHolderFigure} I/F.
	 * @see TextHolderFigure#setText(String)
	 */
	public void setText(String newText) 
	{
		boolean b = (newText != null && newText.trim().length() > 0);
		setAttribute(DrawingAttributes.SHOWTEXT, b);
		setAttribute(AttributeKeys.TEXT, newText);
	}
	
	/**
	 * Implemented as specified by the {@link TextHolderFigure} I/F.
	 * @see TextHolderFigure#getTextColumns()
	 */
	public int getTextColumns() 
	{
		String t = getText();
		int n = FigureUtil.TEXT_COLUMNS;
		return (t == null) ? n : Math.max(t.length(), n);
	}
	
	/**
	 * Implemented as specified by the {@link TextHolderFigure} I/F.
	 * @see TextHolderFigure#getTabSize()
	 */
	public int getTabSize() { return FigureUtil.TAB_SIZE; }
	
	/**
	 * Implemented as specified by the {@link TextHolderFigure} I/F.
	 * @see TextHolderFigure#getLabelFor()
	 */
	public TextHolderFigure getLabelFor() { return this; }

	/**
	 * Implemented as specified by the {@link TextHolderFigure} I/F.
	 * @see TextHolderFigure#getInsets()
	 */
	public Insets2D.Double getInsets() { return new Insets2D.Double(); }
	
	/**
	 * Implemented as specified by the {@link TextHolderFigure} I/F.
	 * @see TextHolderFigure#getFont()
	 */
	public Font getFont() { return AttributeKeys.getFont(this); }
	
	/**
	 * Implemented as specified by the {@link TextHolderFigure} I/F.
	 * @see TextHolderFigure#getTextColor()
	 */
	public Color getTextColor() { return AttributeKeys.TEXT_COLOR.get(this); }

	/**
	 * Implemented as specified by the {@link TextHolderFigure} I/F.
	 * @see TextHolderFigure#getFillColor()
	 */
	public Color getFillColor() { return AttributeKeys.FILL_COLOR.get(this); }

	/**
	 * Implemented as specified by the {@link TextHolderFigure} I/F.
	 * @see TextHolderFigure#getFontSize()
	 */
	public float getFontSize()
	{ 
		return AttributeKeys.FONT_SIZE.get(this).floatValue(); 
	}
	
	/**
	 * Implemented as specified by the {@link TextHolderFigure} I/F.
	 * @see TextHolderFigure#isEditable()
	 */
	public boolean isEditable() { return editable; }
	
	/**
	 * Required by the {@link TextHolderFigure} I/F but no-op implementation
	 * in our case.
	 * @see TextHolderFigure#setFontSize(float)
	 */
	public void setFontSize(float size)  {}

	/* (non-Javadoc)
	 * @see org.jhotdraw.draw.TextHolderFigure#isTextOverflow()
	 */
	public boolean isTextOverflow() { return false; }

	/*
	 * (non-Javadoc)
	 * @see org.openmicroscopy.shoola.util.ui.drawingtools.figures.
	 * RectangleFigure#clone()
	 */
	public RectangleTextFigure clone()
	{
		RectangleTextFigure that = (RectangleTextFigure) super.clone();
		that.setText(this.getText());
		return that;
	}
}
