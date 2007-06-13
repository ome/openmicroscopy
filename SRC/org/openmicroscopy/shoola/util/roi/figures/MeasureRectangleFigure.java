/*
 * org.openmicroscopy.shoola.util.roi.figures.MeasureRectangleFigure 
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


//Java imports
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;


//Third-party libraries
import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.RectangleFigure;

//Application-internal dependencies
import static org.openmicroscopy.shoola.util.roi.figures.DrawingAttributes.MEASUREMENTTEXT_COLOUR;
import static org.openmicroscopy.shoola.util.roi.figures.DrawingAttributes.SHOWMEASUREMENT;
import static org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys.WIDTH;
import static org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys.HEIGHT;
import static org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys.PERIMETER;
import static org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys.CENTREX;
import static org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys.CENTREY;
import static org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys.AREA;
import static org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys.INMICRONS;
import static org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys.MICRONSPIXELX;
import static org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys.MICRONSPIXELY;

import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.util.FigureType;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.figures.textutil.OutputUnit;

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
public class MeasureRectangleFigure
	extends RectangleFigure
	implements ROIFigure
{
	 /**
     * This is used to perform faster drawing and hit testing.
     */
	protected	Rectangle2D 		bounds;
	protected 	ROI					roi;
	protected 	ROIShape 			shape;

	   
    /** Creates a new instance. */
    public MeasureRectangleFigure() 
    {
        this(0, 0, 0, 0);
    }
    
    public MeasureRectangleFigure(double x, double y, double width, 
    							double height) 
    {
		super(x, y, width, height);
        shape = null;
		roi = null;
		
    }
    
    public double getMeasurementX() 
    {
    	if(INMICRONS.get(shape))
    		return getX()*MICRONSPIXELX.get(shape);
    	else
        	return getX();
    }
    
    public double getMeasurementY() 
    {
    	if(INMICRONS.get(shape))
    		return getY()*MICRONSPIXELY.get(shape);
    	else
        	return getY();
    }
    
    public double getMeasurementWidth() 
    {
    	
    	if(INMICRONS.get(shape))
    		return getWidth()*MICRONSPIXELX.get(shape);
    	else
    		return getWidth();
    }
    
    public double getMeasurementHeight() 
    {
    	if(INMICRONS.get(shape))
    		return getHeight()*MICRONSPIXELY.get(shape);
    	else
    		return getHeight();
    }
    
    public double getX() 
    {
      	return rectangle.x;
    }
    
    public double getY() 
    {
       	return rectangle.y;
    }
    
    public double getWidth() 
    {
    	return rectangle.getWidth();
    }
    
    public double getHeight() 
    {
    	return rectangle.getHeight();
    }
    

	public void draw(Graphics2D g)
	{
		super.draw(g);
		
		if (SHOWMEASUREMENT.get(this))
		{
			NumberFormat formatter = new DecimalFormat("###.#");
			String rectangleArea = formatter.format(getArea());
			rectangleArea = addUnits(rectangleArea);
			double sz = ((Double)this.getAttribute(AttributeKeys.FONT_SIZE));
			g.setFont(new Font("Arial",Font.PLAIN, (int)sz));
			bounds = g.getFontMetrics().getStringBounds(rectangleArea, g);
			bounds = new Rectangle2D.Double(
						getBounds().getCenterX()-bounds.getWidth()/2,
						getBounds().getCenterY()+bounds.getHeight()/2,
					bounds.getWidth(), bounds.getHeight());
			g.setColor(MEASUREMENTTEXT_COLOUR.get(this));
			g.drawString(rectangleArea, (int) bounds.getX(), (int) 
						bounds.getY()); 
					
		}
	}
				
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
	 

	public String addUnits(String str)
	{
		if(shape==null)
			return str;
		if(INMICRONS.get(shape))
			return str+OutputUnit.MICRONS+OutputUnit.SQUARED;
		else
			return str+OutputUnit.PIXELS+OutputUnit.SQUARED;
	}


	public double getArea()
	{
		return getMeasurementWidth()*getMeasurementHeight();
	}
	
	public double getPerimeter()
	{
		return getMeasurementWidth()*2+getMeasurementHeight()*2;
	}

	public Point2D getCentre()
	{
     	if (INMICRONS.get(shape))
    		return new Point2D.Double(
    				rectangle.getCenterX()*MICRONSPIXELX.get(shape), 
    				rectangle.getCenterY()*MICRONSPIXELY.get(shape));
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
		AREA.set(shape, getArea());
		WIDTH.set(shape, getMeasurementWidth());		
		HEIGHT.set(shape, getMeasurementHeight());		
		PERIMETER.set(shape, getPerimeter());		
		CENTREX.set(shape, getCentre().getX());
		CENTREY.set(shape, getCentre().getY());
	}

	/**
	 * Implemented as specified by the {@link ROIFigure} interface.
	 * @see ROIFigure#getType()
	 */
	public String getType() { return ROIFigure.RECTANGLE_TYPE; }
			
}


