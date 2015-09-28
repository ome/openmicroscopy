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
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.Collection;
import java.util.LinkedList;

import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.EllipseFigure;
import org.jhotdraw.draw.Handle;
import org.jhotdraw.draw.TextHolderFigure;
import org.jhotdraw.draw.Tool;
import org.jhotdraw.draw.TransformHandleKit;
import org.jhotdraw.geom.Insets2D;

import org.openmicroscopy.shoola.util.roi.model.annotation.MeasurementAttributes;
import org.openmicroscopy.shoola.util.ui.drawingtools.attributes.DrawingAttributes;
import org.openmicroscopy.shoola.util.ui.drawingtools.texttools.TransformedDrawingTextTool;

/** 
 * An ellipse figure with text.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class EllipseTextFigure
	extends RotateEllipseFigure
	implements TextHolderFigure
{
	
	/** Is the attribute update from a transform update. */
	protected boolean 				fromTransformUpdate;
	
	/** Flag indicating if the figure is editable or not. */
	protected boolean 							editable;

	/** The bounds of the text. */
	private				Rectangle2D.Double 		textBounds;

	/** 
	 * Creates a default figure of dimension (0, 0) located at the Point (0,0).
	 * 
	 * @param text The text to set.
	 */
	public EllipseTextFigure(String text) 
	{
		this(text, 0, 0, 0, 0);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param t 	The text to set.
	 * @param x		The x-coordinate.
	 * @param y		The y-coordinate.
	 * @param w		The width of the figure.
	 * @param h		The height of the figure.
	 */
	public EllipseTextFigure(String t, double x, double y, double w, double h) 
	{
		super(x, y, w, h);
    	setAttributeEnabled(AttributeKeys.TEXT_COLOR, true);
    	setAttributeEnabled(MeasurementAttributes.HEIGHT, true);
		setAttributeEnabled(MeasurementAttributes.WIDTH, true);
		super.setAttribute(MeasurementAttributes.WIDTH, w);
		super.setAttribute(MeasurementAttributes.HEIGHT, h);
  		setText(t);
		textBounds = null;
		editable = true;
		fromTransformUpdate = true;
	}	
	
	/**
	 * Overridden to set the value of the transform.
	 * @see #setAttribute(AttributeKey, Object)
	 */
	public void setAttribute(AttributeKey key, Object newValue) 
	{
		super.setAttribute(key, newValue);
		if (!fromTransformUpdate)
		{
			if (MeasurementAttributes.HEIGHT.getKey().equals(key.getKey()))
			{
				double newHeight = MeasurementAttributes.HEIGHT.get(this);
				this.setHeight(newHeight);
			}
			if (MeasurementAttributes.WIDTH.getKey().equals(key.getKey()))
			{
				double newWidth = MeasurementAttributes.WIDTH.get(this);
				this.setWidth(newWidth);
			}
		}
	}
	
	/**
	 * Transform the shape by the affineTransform, tx. This methods will  
	 * scale, rotate and translate the ellipse.
	 * @param tx see above.
	 */
	public void transform(AffineTransform tx)
	{
		super.transform(tx);
		fromTransformUpdate = true;
		MeasurementAttributes.HEIGHT.set(this, 
				getTransformedEllipse().getBounds2D().getHeight());
		MeasurementAttributes.WIDTH.set(this,
				getTransformedEllipse().getBounds2D().getWidth());
		fromTransformUpdate = false;
	}
	
	/**
	 * Set the bounds of the ellipse from the anchor to lead.
	 *  
	 * @param anchor The start point of the drawing action.
	 * @param lead The end point the drawing action.
	 * 
	 */
	public void setBounds(Point2D.Double anchor, Point2D.Double lead) 
	{
		super.setBounds(anchor, lead);
		fromTransformUpdate = true;
		Rectangle2D r = getTransformedEllipse().getBounds2D();
		MeasurementAttributes.HEIGHT.set(this, r.getHeight());
		MeasurementAttributes.WIDTH.set(this, r.getWidth());
		fromTransformUpdate = false;
	}

	/**
	 * Overridden to add the handle only if the passed value is <code>0</code>.
	 * @see #createHandles(int)
	 */
	@Override 
	public Collection<Handle> createHandles(int detailLevel) 
	{
		LinkedList<Handle> handles = new LinkedList<Handle>();
	    if (detailLevel == 0) 
	    	TransformHandleKit.addTransformHandles(this, handles);
	    return handles;
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
	 * Overridden to set the correct tool.
	 * @see EllipseFigure#getTool(Point2D.Double)
	 */
	public Tool getTool(Point2D.Double p) 
	{
		if (isEditable() && contains(p)) 
		{
			invalidate();
			return new TransformedDrawingTextTool(this, getTransformedShape()); 
		}
		return null;
	}
	
	/**
	 * Overridden to draw the text.
	 * @see EllipseFigure#drawFill(Graphics2D)
	 */
	protected void drawFill(Graphics2D g) 
	{
		super.drawFill(g);
		drawText(g);
	}

	/**
	 * Overridden to draw the text.
	 * @see EllipseFigure#drawText(Graphics2D)
	 */
	protected void drawText(Graphics2D g) 
	{
		if (!(MeasurementAttributes.SHOWTEXT.get(this))) return;
		String text = getText();
		if (text != null)// && isEditable()) 
		{	
			text = text.trim();
			if (text.length() == 0) return;
			Rectangle r = getTransformedShape().getBounds();
			// TODO: I BROKE THIS.
			//Rectangle2D r = this.getBounds();
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
			double x = r.getCenterX()-width/2;
			double y = r.getCenterY();
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
	 * Overridden to set the layout to <code>null</code>. 
	 * @see EllipseFigure#invalidate()
	 */
	public void invalidate() 
	{
		super.invalidate();
	}

	/** 
	 * Overridden to set the layout to <code>null</code>. 
	 * @see EllipseFigure#validate()
	 */
	protected void validate() 
	{
		super.validate();
	}

	/**
	 * Overridden to return the bounds of the text area.
	 * @see EllipseFigure#getDrawingArea()
	 */
	public Rectangle2D.Double getDrawingArea() 
	{
		Rectangle2D.Double r = super.getDrawingArea();
		r.add(getTextBounds());
		return r;
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
	public boolean isEditable() { return this.editable;	}
	
	/**
	 * Required by the {@link TextHolderFigure} I/F but no-op implementation
	 * in our case.
	 * @see TextHolderFigure#setFontSize(float)
	 */
	public void setFontSize(float size)  {}

	/**
	 * Overridden to always return <code>false</code>.
	 * @see TextHolderFigure#isTextOverflow()
	 */
	public boolean isTextOverflow()	{ return false; }

	/**
	 * Overridden to set the text.
	 * @see RotateEllipseFigure#clone()
	 */
	public EllipseTextFigure clone()
	{
		EllipseTextFigure that = (EllipseTextFigure) super.clone();
		that.setText(this.getText());
		that.editable = true;
		that.fromTransformUpdate = true;
		return that;
	}
	
}