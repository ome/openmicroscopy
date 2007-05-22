/*
 * @(#)NodeFigure.java  1.0  July 4, 2006
 *
 * Copyright (c) 1996-2006 by the original authors of JHotDraw
 * and all its contributors ("JHotDraw.org")
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * JHotDraw.org ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance
 * with the terms of the license agreement you entered into with
 * JHotDraw.org.
 *
 * Original code copyright JHotDraw:
 * Project:		JHotdraw - a GUI framework for technical drawings
 *				http://www.jhotdraw.org
 *				http://jhotdraw.sourceforge.net
 * Copyright:	(c) by the original author(s) and all contributors
 * License:		Lesser GNU Public License (LGPL)
 *				http://www.opensource.org/licenses/lgpl-license.html
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

//Third-party libraries
import static org.jhotdraw.draw.AttributeKeys.FILL_COLOR;
import static org.jhotdraw.draw.AttributeKeys.FONT_UNDERLINED;
import static org.jhotdraw.draw.AttributeKeys.FONT_SIZE;
import static org.jhotdraw.draw.AttributeKeys.FONT_FACE;
import static org.jhotdraw.draw.AttributeKeys.TEXT_COLOR;
import static org.jhotdraw.draw.AttributeKeys.TEXT;

import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.BoxHandleKit;
import org.jhotdraw.draw.Handle;
import org.jhotdraw.draw.TextHolderFigure;
import org.jhotdraw.draw.TextTool;
import org.jhotdraw.draw.Tool;
import org.jhotdraw.geom.Geom;
import org.jhotdraw.geom.Insets2D;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;

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
	private boolean editable = true;
	
	private	Rectangle2D bounds;
	private ROI			roi;
	private ROIShape 	shape;
	
	private Color 		oldColor;
	private boolean 	fillChanged = false;
	
	// cache of the TextFigure's layout
	transient private  	TextLayout textLayout;
	private				Rectangle2D.Double textBounds;
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
		oldColor = null;
	}
	
	protected void drawFill(java.awt.Graphics2D g) 
	{
		if(fillChanged)
		{
			FILL_COLOR.set(this, oldColor);
			fillChanged = false;
		}
		super.drawFill(g);
		drawText(g);
	}

	protected void drawText(java.awt.Graphics2D g) 
	{
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
	
	/**
	 * Returns a specialized tool for the given coordinate.
	 * <p>Returns null, if no specialized tool is available.
	 */
	public Tool getTool(Point2D.Double p) 
	{
		if(isEditable() && (textBounds.contains(p) || contains(p))) 
		{
			fillChanged = true;
			oldColor = FILL_COLOR.get(this);
			FILL_COLOR.set(this, Color.white);
			invalidate();
			return new TextTool(this); 
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
	/* (non-Javadoc)
	 * @see org.openmicroscopy.shoola.util.ui.measurement.ui.figures.ROIFigure#getROI()
	 */
	public ROI getROI() 
	{
		return roi;
	}

	/* (non-Javadoc)
	 * @see org.openmicroscopy.shoola.util.ui.measurement.ui.figures.ROIFigure#getROIShape()
	 */
	public ROIShape getROIShape() 
	{
		return shape;
	}

	/* (non-Javadoc)
	 * @see org.openmicroscopy.shoola.util.ui.measurement.ui.figures.ROIFigure#setROI(org.openmicroscopy.shoola.util.ui.roi.model.ROI)
	 */
	public void setROI(ROI roi) 
	{
		this.roi = roi;
	}

	/* (non-Javadoc)
	 * @see org.openmicroscopy.shoola.util.ui.measurement.ui.figures.ROIFigure#setROIShape(org.openmicroscopy.shoola.util.ui.roi.model.ROIShape)
	 */
	public void setROIShape(ROIShape shape) 
	{
		this.shape = shape;
	}

	/* (non-Javadoc)
	 * @see org.openmicroscopy.shoola.util.ui.measurement.ui.figures.ROIFigure#calculateMeasurements()
	 */
	public void calculateMeasurements()
	{
		super.calculateMeasurements();
	}
}
