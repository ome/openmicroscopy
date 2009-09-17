/*
 * org.openmicroscopy.shoola.util.roi.figures.MeasureEllipseFigure 
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
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
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
import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys;
import org.openmicroscopy.shoola.util.roi.model.annotation.MeasurementAttributes;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.util.MeasurementUnits;
import org.openmicroscopy.shoola.util.roi.util.FigureSelectionHandle;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.drawingtools.figures.FigureUtil;
import org.openmicroscopy.shoola.util.ui.drawingtools.figures.PointTextFigure;

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
public class MeasurePointFigure
	extends PointTextFigure
	implements ROIFigure
{
	/** Is this figure read only. */
	private boolean readOnly;
	
	/**
    * This is used to perform faster drawing and hit testing.
    */
	private	Rectangle2D bounds;
	/** The ROI containing the ROIFigure which in turn contains this Figure. */
	protected 	ROI					roi;

	/** The ROIFigure contains this Figure. */
	protected 	ROIShape 			shape;
	
	/** The Measurement units, and values of the image. */
	private MeasurementUnits 		units;
	
	/** 
	 * The status of the figure i.e. {@link ROIFigure#IDLE} or 
	 * {@link ROIFigure#MOVING}. 
	 */
	private int 					status;
	
	/** 
	 * Creates a new instance.
	 * @param text text of the ellipse. 
	 * @param x    coord of the figure. 
	 * @param y    coord of the figure. 
	 * @param width of the figure. 
	 * @param height of the figure. 
	 * @param readOnly The figure is read only.
	 */
	public MeasurePointFigure(String text, double x, double y, double width, 
												double height, boolean readOnly) 
    {
    	super(text, x, y, width, height);
    	setAttributeEnabled(MeasurementAttributes.TEXT_COLOR, true);
	    shape = null;
		roi = null;
		status = IDLE;
		setReadOnly(readOnly);
    }

	  /** 
     * Creates a new instance.
     * @param x    coord of the figure. 
     * @param y    coord of the figure. 
     * @param width of the figure. 
     * @param height of the figure. 
     * */  
    public MeasurePointFigure(double x, double y, double width, double height) 
    {
    	this("Text", x, y, width, height, false);
    }

    /**
	 * Create an instance of the Point Figure.
	 */
	public MeasurePointFigure()
	{
		this("Text", 0, 0, 0, 0, false);
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
     * Get the centre of the figure, convert to microns if isInMicrons set. 
     * 
     * @return see above.
     */
	public Point2D getMeasurementCentre()
    {
    	if (units.isInMicrons())
    		return new Point2D.Double(getCentre().getX()*
    			units.getMicronsPixelX(),getCentre().getY()*
    			units.getMicronsPixelY());
    	return getCentre();
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
    public double getX() { return ellipse.getX(); }
    
    /** 
     * Get the y coord of the figure. 
     * @return see above.
     */
    public double getY() { return ellipse.getY(); }
    
    /** 
     * Get the width coord of the figure. 
     * @return see above.
     */
    public double getWidth() { return ellipse.getWidth(); }
    
    /** 
     * Get the height coord of the figure. 
     * @return see above.
     */
    public double getHeight() { return ellipse.getHeight(); }
   
    
    /**
     * Draw the figure on the graphics context.
     * @param g the graphics context.
     */
	public void draw(Graphics2D g)
	{
		super.draw(g);
		if(MeasurementAttributes.SHOWMEASUREMENT.get(this)  || MeasurementAttributes.SHOWID.get(this))
		{
			NumberFormat formatter = new DecimalFormat("###.#");
			String pointCentre = 
				"("+formatter.format(getMeasurementCentre().getX()) 
				+ ","+formatter.format(getMeasurementCentre().getY())+")";
			//ellipseArea = addUnits(ellipseArea);
			double sz = ((Double)this.getAttribute(MeasurementAttributes.FONT_SIZE));
			g.setFont(new Font("Arial",Font.PLAIN, (int)sz));
			bounds = g.getFontMetrics().getStringBounds(pointCentre, g);
			bounds = new Rectangle2D.Double(this.getBounds().getCenterX()-bounds.getWidth()/2,
					this.getBounds().getCenterY()+bounds.getHeight()/2,
					bounds.getWidth(), bounds.getHeight());
			if(MeasurementAttributes.SHOWMEASUREMENT.get(this))
			{
				g.setColor(MeasurementAttributes.MEASUREMENTTEXT_COLOUR.get(this));
				g.drawString(pointCentre, (int)bounds.getX(), (int)bounds.getY());
			}
			if(MeasurementAttributes.SHOWID.get(this))
			{
				Rectangle2D 		bounds;
				bounds = g.getFontMetrics().getStringBounds(getROI().getID()+"", g);
				bounds = new Rectangle2D.Double(
							getBounds().getCenterX()-bounds.getWidth()/2,
							getBounds().getCenterY()+bounds.getHeight()/2,
						bounds.getWidth(), bounds.getHeight());
				g.setColor(this.getTextColor());
				g.drawString(getROI().getID()+"", (int)bounds.getX(), (int)bounds.getY());
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
		if(bounds!=null)
		{
			if(newBounds.getX()>bounds.getX())
			{
				double diff = newBounds.x-bounds.getX();
				newBounds.x = bounds.getX();
				newBounds.width = newBounds.width+diff;
			}
			if(newBounds.getY()>bounds.getY())
			{
				double diff = newBounds.y-bounds.getY();
				newBounds.y = bounds.getY();
				newBounds.height = newBounds.height+diff;
			}
			if(bounds.getX()+bounds.getWidth()>newBounds.getX()+newBounds.getWidth())
			{
				double diff = bounds.getX()+bounds.getWidth()-newBounds.getX()+newBounds.getWidth();
				newBounds.width = newBounds.width+diff;
			}
			if(bounds.getY()+bounds.getHeight()>newBounds.getY()+newBounds.getHeight())
			{
				double diff = bounds.getY()+bounds.getHeight()-newBounds.getY()+newBounds.getHeight();
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
		if (shape==null) return str;
		if (units.isInMicrons())
			return str+UIUtilities.MICRONS_SYMBOL+UIUtilities.SQUARED_SYMBOL;
		return str+UIUtilities.PIXELS_SYMBOL+UIUtilities.SQUARED_SYMBOL;
	}

	/** 
	 * Calculate the centre of the figure. 
	 * @return see above.
	 */
	public Point2D getCentre()
	{
		return new Point2D.Double(Math.round(ellipse.getCenterX()), 
								Math.round(ellipse.getCenterY()));
	}
	
	/**
	 * Implemented as specified by the {@link ROIFigure} interface.
	 * @see ROIFigure#getROI()
	 */
	public ROI getROI() { return roi;	}

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
	 * @see ROIFigure#getType()
	 */
	public void calculateMeasurements()
	{
		if (shape == null) return;
		AnnotationKeys.CENTREX.set(shape, getMeasurementCentre().getX());
		AnnotationKeys.CENTREY.set(shape, getMeasurementCentre().getY());
	}
	
	/**
	 * Implemented as specified by the {@link ROIFigure} interface.
	 * @see ROIFigure#getType()
	 */
	public String getType() { return FigureUtil.POINT_TYPE; }
	
	/**
	 * Implemented as specified by the {@link ROIFigure} interface.
	 * @see ROIFigure#setMeasurementUnits(MeasurementUnits)
	 */
	public void setMeasurementUnits(MeasurementUnits units)
	{
		this.units = units;
	}
	
	/**
	 * Implemented as specified by the {@link ROIFigure} interface.
	 * @see ROIFigure#getPoints()
	 */
	public List<Point> getPoints()
	{
		List<Point> points = new ArrayList<Point>(1);
		points.add(new Point((int) getX(), (int) getY())); 
		return points;
		/*
		PlanePoint2D[] points = new PlanePoint2D[1];
		points[0] = new PlanePoint2D(getX(), getY());
		return points;
		*/
		
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


