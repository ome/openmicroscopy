/*
* org.openmicroscopy.shoola.util.roi.figures.MeasureMaskFigure
*
 *------------------------------------------------------------------------------
*  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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




//Java imports
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

//Third-party libraries
import org.jhotdraw.draw.AbstractAttributedFigure;
import org.jhotdraw.draw.Handle;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys;
import org.openmicroscopy.shoola.util.roi.model.annotation.MeasurementAttributes;
import org.openmicroscopy.shoola.util.roi.model.util.MeasurementUnits;
import org.openmicroscopy.shoola.util.roi.util.FigureSelectionHandle;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.drawingtools.figures.FigureUtil;
import org.openmicroscopy.shoola.util.ui.drawingtools.figures.RectangleTextFigure;

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
 * @since 3.0-Beta4
 */
public class MeasureMaskFigure 
	extends RectangleTextFigure
	implements ROIFigure
{
	
	/** Is this figure read only. */
	private boolean readOnly;
	
	/**
	 * This is used to perform faster drawing and hit testing.
	 */
	protected	Rectangle2D 		bounds;
	
	/** The ROI containing the ROIFigure which in turn contains this Figure. */
	protected 	ROI					roi;

	/** The ROIFigure contains this Figure. */
	protected 	ROIShape 			shape;
	
	/** The Measurement units, and values of the image. */
	private MeasurementUnits 		units;
	
	/** The BufferedImage of the Mask. */
	private BufferedImage 			mask;
	
	/** 
	 * The status of the figure i.e. {@link ROIFigure#IDLE} or 
	 * {@link ROIFigure#MOVING}. 
	 */
	private int 					status;

    /** Creates a new instance. */
    public MeasureMaskFigure() 
    {
        this("Text", 0, 0, 0, 0, null, false);
    }

    /** 
     * Creates a new instance.
     * @param text text of the ellipse. 
     * */
    public MeasureMaskFigure(String text) 
    {
        this(text, 0, 0, 0, 0, null, false);
    }
    
    /** 
     * Creates a new instance.
     * @param x    coord of the figure. 
     * @param y    coord of the figure. 
     * @param width of the figure. 
     * @param height of the figure. 
     * */
    public MeasureMaskFigure(double x, double y, double width, 
			double height, BufferedImage mask) 
    {
    	this("Text", x, y, width, height, mask, false);
    }
    

    /** 
     * Creates a new instance.
     * @param x    coord of the figure. 
     * @param y    coord of the figure. 
     * @param width of the figure. 
     * @param height of the figure. 
     * @param readOnly Is the figure read only.
     * */
    public MeasureMaskFigure(double x, double y, double width, 
			double height, BufferedImage mask, boolean readOnly) 
    {
    	this("Text", x, y, width, height, mask, readOnly);
    }
    
    /** 
     * Creates a new instance.
     * @param text text of the ellipse. 
     * @param x    coord of the figure. 
     * @param y    coord of the figure. 
     * @param width of the figure. 
     * @param height of the figure. 
     * @param readOnly the figure is readOnly
     * */
    public MeasureMaskFigure(String text, double x, double y, double width, 
    							double height, BufferedImage mask, boolean readOnly) 
    {
		super(text, x, y, width, height);
		this.mask = mask;
		setAttributeEnabled(MeasurementAttributes.HEIGHT, true);
		setAttributeEnabled(MeasurementAttributes.WIDTH, true);
		setAttribute(MeasurementAttributes.WIDTH, width);
		setAttribute(MeasurementAttributes.HEIGHT, height);
        shape = null;
		roi = null;
		status = IDLE;
		setReadOnly(readOnly);
    }
    
    /**
     * Set the mask of the maskFigure to the mask parameter.
     * @param mask See above.
     */
    public void setMask(BufferedImage mask)
    {
    	this.mask = mask;
    }    
    
    /**
     * get the mask of the maskFigure.
     * return See above.
     */
    public BufferedImage getMask()
    {
    	return this.mask;
    }
    
    /** 
     * Get the X Coord of the figure, convert to microns if isInMicrons set. 
     * 
     * @return see above.
     */
    public double getMeasurementX() 
    {
    	if (units.isInMicrons()) return getX()*units.getMicronsPixelX();
    	return getX();
    }
    
    /** 
     * Get the Y Coord of the figure, convert to microns if isInMicrons set. 
     * 
     * @return see above.
     */
    public double getMeasurementY() 
    {
    	if (units.isInMicrons()) return getY()*units.getMicronsPixelY();
    	return getY();
    }
    
    
    /** 
     * Get the width of the figure, convert to microns if isInMicrons set. 
     * 
     * @return see above.
     */
    public double getMeasurementWidth() 
    {
    	if (units.isInMicrons()) return getWidth()*units.getMicronsPixelX();
    	return getWidth();
    }
    
    /** 
     * Get the height of the figure, convert to microns if isInMicrons set. 
     * 
     * @return see above.
     */
    public double getMeasurementHeight() 
    {
    	if (units.isInMicrons()) return getHeight()*units.getMicronsPixelY();
    	return getHeight();
    }
    
    /** 
     * Get the x coord of the figure. 
     * @return see above.
     */
    public double getX() { return rectangle.getX(); }
    
    /** 
     * Get the y coord of the figure. 
     * @return see above.
     */
    public double getY() { return rectangle.getY(); }
    
    /** 
     * Get the width coord of the figure. 
     * @return see above.
     */
    public double getWidth() { return rectangle.getWidth(); }
    
    /** 
     * Get the height coord of the figure. 
     * @return see above.
     */
    public double getHeight() { return rectangle.getHeight(); }
    
    /**
     * Draw the figure on the graphics context.
     * @param g the graphics context.
     */
	public void draw(Graphics2D g)
	{
		if(mask==null)
			return;
		g.drawImage(mask, (int)rectangle.getX(), (int)rectangle.getY(), 
				(int)rectangle.getWidth(), (int)rectangle.getHeight(), null);	
		
		if (MeasurementAttributes.SHOWMEASUREMENT.get(this) || MeasurementAttributes.SHOWID.get(this))
		{
			NumberFormat formatter = new DecimalFormat("###.#");
			String rectangleArea = formatter.format(getArea());
			rectangleArea = addUnits(rectangleArea);
			double sz = ((Double)this.getAttribute(MeasurementAttributes.FONT_SIZE));
			g.setFont(new Font("Arial",Font.PLAIN, (int)sz));
			bounds = g.getFontMetrics().getStringBounds(rectangleArea, g);
			bounds = new Rectangle2D.Double(
						getBounds().getCenterX()-bounds.getWidth()/2,
						getBounds().getCenterY()+bounds.getHeight()/2,
					bounds.getWidth(), bounds.getHeight());
		
			if(MeasurementAttributes.SHOWMEASUREMENT.get(this))
			{
				g.setColor(MeasurementAttributes.MEASUREMENTTEXT_COLOUR.get(this));
				g.drawString(rectangleArea, (int) bounds.getX(), (int) 
							bounds.getY()); 
			}
			if(MeasurementAttributes.SHOWID.get(this))
			{
				g.setColor(this.getTextColor());
				bounds = g.getFontMetrics().getStringBounds(getROI().getID()+"", g);
				bounds = new Rectangle2D.Double(
							getBounds().getCenterX()-bounds.getWidth()/2,
							getBounds().getCenterY()+bounds.getHeight()/2,
						bounds.getWidth(), bounds.getHeight());
				g.drawString(getROI().getID()+"", (int) bounds.getX(), (int) 
							bounds.getY()); 
			}
			
		}
	}
	
	/**
	 * Overridden to stop updating shape if read only
	 * @see AbstractAttributedFigure#transform(AffineTransform)
	 */
	public void transform(AffineTransform tx)
	{
		if(!readOnly)
			super.transform(tx);
	}
	
	/**
	 * Overridden to stop updating shape if readonly.
	 * @see AbstractAttributedFigure#setBounds(Double, Double)
	 */
	public void setBounds(Point2D.Double anchor, Point2D.Double lead) 
	{
		if(!readOnly)
			super.setBounds(anchor, lead);
	}
			
	/**
	 * Overridden to return the correct handles.
	 * @see AbstractAttributedFigure#createHandles(int)
	 */
	public Collection<Handle> createHandles(int detailLevel) 
	{
		if(!readOnly)
			return super.createHandles(detailLevel);
		else
		{
			LinkedList<Handle> handles = new LinkedList<Handle>();
			handles.add(new FigureSelectionHandle(this));
			return handles;
		}
	}
	
	/**
	 * Calculates the bounds of the rendered figure, including the text rendered. 
	 * @return see above.
	 */
	public Rectangle2D.Double getDrawingArea()
	{
		Rectangle2D.Double newBounds = super.getDrawingArea();
		if (bounds != null)
		{
			if (newBounds.getX() > bounds.getX())
			{
				double diff = newBounds.x-bounds.getX();
				newBounds.x = bounds.getX();
				newBounds.width = newBounds.width+diff;
			}
			if (newBounds.getY() > bounds.getY())
			{
				double diff = newBounds.y-bounds.getY();
				newBounds.y = bounds.getY();
				newBounds.height = newBounds.height+diff;
			}
			if (bounds.getX()+bounds.getWidth() > 
				newBounds.getX()+newBounds.getWidth())
			{
				double diff = bounds.getX()+bounds.getWidth()-
							newBounds.getX()+newBounds.getWidth();
				newBounds.width = newBounds.width+diff;
			}
			if (bounds.getY()+bounds.getHeight() >
				newBounds.getY()+newBounds.getHeight())
			{
				double diff = bounds.getY()+bounds.getHeight()
								-newBounds.getY()+newBounds.getHeight();
				newBounds.height = newBounds.height+diff;
			}
		}
		return newBounds;
	}
	 

	/**
	 * Add units to the string 
	 * @param str see above.
	 * @return returns the string with the units added. 
	 */
	public String addUnits(String str)
	{
		if (shape == null) return str;
		if (units.isInMicrons()) 
			return str+UIUtilities.MICRONS_SYMBOL+UIUtilities.SQUARED_SYMBOL;
		return str+UIUtilities.PIXELS_SYMBOL+UIUtilities.SQUARED_SYMBOL;
	}


	/**
	 * Calculate the area of the figure. 
	 * @return see above.
	 */
	public double getArea()
	{
		return getMeasurementWidth()*getMeasurementHeight();
	}
	
	/**
	 * Calculate the perimeter of the figure. 
	 * @return see above.
	 */
	public double getPerimeter()
	{
		return getMeasurementWidth()*2+getMeasurementHeight()*2;
	}

	/** 
	 * Calculate the centre of the figure. 
	 * @return see above.
	 */
	public Point2D getCentre()
	{
     	if (units.isInMicrons())
    		return new Point2D.Double(
    				rectangle.getCenterX()*units.getMicronsPixelX(), 
    				rectangle.getCenterY()*units.getMicronsPixelY());
    	return new Point2D.Double(rectangle.getCenterX(), 
    							rectangle.getCenterY());
	}

	/**
	 * Implemented as specified by the {@link ROIFigure} interface.
	 * @see ROIFigure#getROI()
	 */
	public ROI getROI() { return roi; }

	/**
	 * Implemented as specified by the {@link ROIFigure} interface.
	 * @see ROIFigure#getROIShape()
	 */
	public ROIShape getROIShape() { return shape; }

	/**
	 * Implemented as specified by the {@link ROIFigure} interface.
	 * @see ROIFigure#setROI(ROI)
	 */
	public void setROI(ROI roi) { this.roi = roi; }

	/**
	 * Implemented as specified by the {@link ROIFigure} interface.
	 * @see ROIFigure#setROIShape(ROIShape)
	 */
	public void setROIShape(ROIShape shape) { this.shape = shape; }

	/**
	 * Implemented as specified by the {@link ROIFigure} interface.
	 * @see ROIFigure#calculateMeasurements()
	 */
	public void calculateMeasurements()
	{
		if (shape == null) return;
		AnnotationKeys.AREA.set(shape, getArea());
		AnnotationKeys.WIDTH.set(shape, getMeasurementWidth());		
		AnnotationKeys.HEIGHT.set(shape, getMeasurementHeight());		
		AnnotationKeys.PERIMETER.set(shape, getPerimeter());		
		AnnotationKeys.CENTREX.set(shape, getCentre().getX());
		AnnotationKeys.CENTREY.set(shape, getCentre().getY());
	}

	/**
	 * Implemented as specified by the {@link ROIFigure} interface.
	 * @see ROIFigure#getType()
	 */
	public String getType() { return FigureUtil.RECTANGLE_TYPE; }

	/**
	 * Implemented as specified by the {@link ROIFigure} interface.
	 * @see ROIFigure#setMeasurementUnits(MeasurementUnits)
	 */
	public void setMeasurementUnits(MeasurementUnits units)
	{
		this.units = units;
	}
	
	/**
	 * Has the mask got a pixel.
	 * @param rgb
	 * @return
	 */
	private boolean hasColour(int rgb)
	{
		Color toColor = new Color(rgb);
		return (toColor.getAlpha()!=0);
	}
	
	/**
	 * Implemented as specified by the {@link ROIFigure} interface.
	 * @see ROIFigure#getPoints()
	 */
	public List<Point> getPoints()
	{
		Rectangle r = rectangle.getBounds();
		List<Point> vector = new ArrayList<Point>(r.height*r.width);
		int xEnd = r.x+r.width, yEnd = r.y+r.height;
		int x, y;
		for (y = r.y; y < yEnd; ++y) 
			for (x = r.x; x < xEnd; ++x) 
				if (hasColour(mask.getRGB(x,y))) 
					vector.add(new Point(x, y));
		
		return vector;
	}

	/**
	 * Implemented as specified by the {@link ROIFigure} interface.
	 * @see {@link ROIFigure#setStatus(boolean)}
	 */
	public void setStatus(int status) { this.status = status; }
	
	/**
	 * Implemented as specified by the {@link ROIFigure} interface.
	 * @see {@link ROIFigure#getStatus()}
	 */
	public int getStatus() { return status; }
	
	/**
	 * Implemented as specified by the {@link ROIFigure} interface.
	 * @see ROIFigure#isReadOnly()
	 */
	public boolean isReadOnly() { return readOnly;}
	
	/**
	 * Implemented as specified by the {@link ROIFigure} interface.
	 * @see ROIFigure#setReadOnly(boolean)
	 */
	public void setReadOnly(boolean readOnly) 
	{ 
		this.readOnly = readOnly; 
		setEditable(!readOnly);
	}

}

