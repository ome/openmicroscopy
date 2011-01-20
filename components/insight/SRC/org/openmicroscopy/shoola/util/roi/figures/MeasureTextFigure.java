/*
 * org.openmicroscopy.shoola.util.roi.figures.MeasureTextFigure 
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
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

//Third-party libraries
import org.jhotdraw.draw.AbstractAttributedFigure;
import org.jhotdraw.draw.BoundsOutlineHandle;
import org.jhotdraw.draw.Handle;
import org.jhotdraw.draw.TextFigure;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.util.MeasurementUnits;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.ui.drawingtools.figures.FigureUtil;

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
public class MeasureTextFigure 
	extends TextFigure
	implements ROIFigure
{
	
	/** Is this figure read only. */
	private boolean readOnly;
	
	private Shape				cachedTransformedShape;

	/** This is used to perform faster drawing and hit testing.    */
	protected	Rectangle2D 		bounds;
	
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
	private int 				status;
	
    /** Creates a new instance. Default value <code>(0, 0) </code>.*/
    public MeasureTextFigure() 
    {
        this(0, 0, false);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param x	The x-coordinate of the top-left corner.
     * @param y The y-coordinate of the top-left corner.
  	 */
    public MeasureTextFigure(double x, double y)
    {
    	this(x, y, false);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param x	The x-coordinate of the top-left corner.
     * @param y The y-coordinate of the top-left corner.
 	 * @param readOnly The figure is read only.
 	 */
    public MeasureTextFigure(double x, double y, boolean readOnly) 
    {
    	super();
    	this.willChange();
    	this.setBounds(new Point2D.Double(x, y), new Point2D.Double(x, y));
    	this.changed();
    	shape = null;
   		roi = null;
   		status = IDLE;
   		setReadOnly(readOnly);
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
			handles.add(new BoundsOutlineHandle(this));
			return handles;
		}
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
	 * @see ROIFigure#getType()
	 */
	public String getType() { return FigureUtil.TEXT_TYPE; }
	
	/**
	 * Implemented as specified by the {@link ROIFigure} interface.
	 * @see ROIFigure#setMeasurementUnits(MeasurementUnits)
	 */
	public void setMeasurementUnits(MeasurementUnits units)
	{
		this.units = units;
	}
	
	/**
	 * Required by the {@link ROIFigure} interface but no-op implementation 
	 * in our case.
	 * @see ROIFigure#calculateMeasurements()
	 */
	public void calculateMeasurements() {}

	/**
	 * Required by the {@link ROIFigure} interface but no-op implementation 
	 * in our case.
	 * @see ROIFigure#getPoints()
	 */
	public List<Point> getPoints() {  return null; }
	
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


