/*
 * org.openmicroscopy.shoola.util.roi.figures.MeasureRectangleFigure 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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


import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.jhotdraw.draw.AbstractAttributedFigure;
import org.jhotdraw.draw.FigureListener;

import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys;
import org.openmicroscopy.shoola.util.roi.model.annotation.MeasurementAttributes;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.util.MeasurementUnits;
import org.openmicroscopy.shoola.util.roi.model.util.UnitPoint;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.drawingtools.figures.FigureUtil;
import org.openmicroscopy.shoola.util.ui.drawingtools.figures.RectangleTextFigure;
import omero.model.Length;
import omero.model.LengthI;
import omero.model.enums.UnitsLength;

/**
 * Rectangle figure with measurement.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class MeasureRectangleFigure
	extends RectangleTextFigure
	implements ROIFigure
{
	
	/** Flag indicating the figure can/cannot be deleted.*/
	private boolean deletable;
	
	/** Flag indicating the figure can/cannot be annotated.*/
	private boolean annotatable;
	
	/** Flag indicating the figure can/cannot be edited.*/
	private boolean editable;
	
	/** Is this figure read only. */
	protected boolean readOnly;
	
	/** Is this figure a client object. */
	protected boolean clientObject;

	/** has the figure been modified. */
	protected boolean dirty;
	
	/**
    * This is used to perform faster drawing and hit testing.
    */
	protected	Rectangle2D 		bounds;
	
	/** The ROI containing the ROIFigure which in turn contains this Figure. */
	protected 	ROI					roi;

	/** The ROIFigure contains this Figure. */
	protected 	ROIShape 			shape;
	
	/** The Measurement units, and values of the image. */
	protected MeasurementUnits 		units;
	   
	/** 
	 * The status of the figure i.e. {@link ROIFigure#IDLE} or 
	 * {@link ROIFigure#MOVING}. 
	 */
	protected int 					status;
	
	/** Flag indicating if the user can move or resize the shape.*/
	private boolean interactable;

    /** Creates a new instance. */
    public MeasureRectangleFigure() 
    {
        this(DEFAULT_TEXT);
    }

    /** 
     * Creates a new instance.
     *
	 * @param readOnly The figure is read only.
	 * @param clientObject Flag indicating if the shape is a client shape.
	 * @param editable Flag indicating the figure can/cannot be edited.
	 * @param deletable Flag indicating the figure can/cannot be deleted.
	 * @param annotatable Flag indicating the figure can/cannot be annotated.
	 */
    public MeasureRectangleFigure(boolean readOnly, boolean clientObject,
    		boolean editable, boolean deletable, boolean annotatable) 
    {
        this(DEFAULT_TEXT, 0, 0, 0, 0, readOnly, clientObject, editable,
        		deletable, annotatable);
    }

    /** 
     * Creates a new instance.
     * 
     * @param text text of the ellipse. 
     * */
    public MeasureRectangleFigure(String text) 
    {
        this(text, 0, 0, 0, 0);
    }
 
    /** 
     * Creates a new instance.
     * 
     * @param x    coordinate of the figure. 
     * @param y    coordinate of the figure. 
     * @param width of the figure. 
     * @param height of the figure. 
      * */
    public MeasureRectangleFigure(String text, double x, double y, double width, 
			double height) 
    {
    	this(text, x, y, width, height, false, true, true, true, true);
    }
    
    /** 
     * Creates a new instance.
     * 
     * @param x    coordinate of the figure. 
     * @param y    coordinate of the figure. 
     * @param width of the figure. 
     * @param height of the figure. 
      * */
    public MeasureRectangleFigure(double x, double y, double width, 
			double height) 
    {
    	this(DEFAULT_TEXT, x, y, width, height);
    }

    /** 
     * Creates a new instance.
     * 
     * @param x    coordinate of the figure. 
     * @param y    coordinate of the figure. 
     * @param width of the figure. 
     * @param height of the figure. 
 	 * @param readOnly The figure is read only.
 	 * @param editable Flag indicating the figure can/cannot be edited.
	 * @param deletable Flag indicating the figure can/cannot be deleted.
	 * @param annotatable Flag indicating the figure can/cannot be annotated.
     */
    public MeasureRectangleFigure(double x, double y, double width, 
			double height, boolean readOnly, boolean clientObject,
			boolean editable, boolean deletable, boolean annotatable) 
    {
    	this(DEFAULT_TEXT, x, y, width, height, readOnly, clientObject,
    			editable, deletable, annotatable);
    }
    
    /** 
     * Creates a new instance.
     * 
     * @param text text of the ellipse. 
     * @param x    coordinate of the figure. 
     * @param y    coordinate of the figure. 
     * @param width of the figure. 
     * @param height of the figure. 
 	 * @param readOnly The figure is read only.
 	 * @param editable Flag indicating the figure can/cannot be edited.
	 * @param deletable Flag indicating the figure can/cannot be deleted.
	 * @param annotatable Flag indicating the figure can/cannot be annotated.
 	 */
    public MeasureRectangleFigure(String text, double x, double y, double width, 
    	double height, boolean readOnly, boolean clientObject,
    	boolean editable, boolean deletable, boolean annotatable) 
    {
		super(text, x, y, width, height);
		setAttributeEnabled(MeasurementAttributes.HEIGHT, true);
		setAttributeEnabled(MeasurementAttributes.WIDTH, true);
		setAttribute(MeasurementAttributes.WIDTH, width);
		setAttribute(MeasurementAttributes.HEIGHT, height);
		setAttribute(MeasurementAttributes.FONT_FACE, DEFAULT_FONT);
		setAttribute(MeasurementAttributes.FONT_SIZE, new Double(FONT_SIZE));
        setAttribute(MeasurementAttributes.SCALE_PROPORTIONALLY, Boolean.FALSE);
        shape = null;
		roi = null;
		status = IDLE;
		setReadOnly(readOnly);
		setClientObject(clientObject);
		this.deletable = deletable;
   		this.annotatable = annotatable;
   		this.editable = editable;
   		interactable = true;
    }
    
    /** 
     * Get the X coordinate of the figure, convert to microns if isInMicrons set. 
     * 
     * @return see above.
     */
    public Length getMeasurementX() 
    {
    	return transformX(getX());
    }
    
    /** 
     * Get the Y coordinate of the figure, convert to microns if isInMicrons set. 
     * 
     * @return see above.
     */
    public Length getMeasurementY() 
    {
    	return transformY(getY());
    }
    
    
    /** 
     * Get the width of the figure, convert to microns if isInMicrons set. 
     * 
     * @return see above.
     */
    public Length getMeasurementWidth() 
    {
    	return transformY(getWidth());
    }
    
    /** 
     * Get the height of the figure, convert to microns if isInMicrons set. 
     * 
     * @return see above.
     */
    public Length getMeasurementHeight() 
    {
    	return transformY(getHeight());
    }
    
    /** 
     * Get the x coordinate of the figure.
     *  
     * @return see above.
     */
    public double getX() { return rectangle.getX(); }
    
    /** 
     * Get the y coordinate of the figure. 
     * 
     * @return see above.
     */
    public double getY() { return rectangle.getY(); }
    
    /** 
     * Get the width of the figure. 
     * 
     * @return see above.
     */
    public double getWidth() { return rectangle.getWidth(); }

    /** 
     * Get the height of the figure. 
     * 
     * @return see above.
     */
    public double getHeight() { return rectangle.getHeight(); }

    /**
     * Draw the figure on the graphics context.
     * @param g the graphics context.
     */
	public void draw(Graphics2D g)
	{
		super.draw(g);
		
		if (MeasurementAttributes.SHOWMEASUREMENT.get(this) || 
				MeasurementAttributes.SHOWID.get(this))
		{
			Length a = getArea();
			String rectangleArea = UIUtilities.formatValue(a, true);
			Double sz = (Double) getAttribute(MeasurementAttributes.FONT_SIZE);
            Font font = (Font) getAttribute(MeasurementAttributes.FONT_FACE);
            if (font != null) g.setFont(font.deriveFont(sz.floatValue()));
            else {
                g.setFont(new Font(FONT_FAMILY, FONT_STYLE, sz.intValue()));
            }
			bounds = g.getFontMetrics().getStringBounds(rectangleArea, g);
			bounds = new Rectangle2D.Double(
						getBounds().getCenterX()-bounds.getWidth()/2,
						getBounds().getCenterY()+bounds.getHeight()/2,
					bounds.getWidth(), bounds.getHeight());
		
			if (MeasurementAttributes.SHOWMEASUREMENT.get(this))
			{
				g.setColor(MeasurementAttributes.MEASUREMENTTEXT_COLOUR.get(
						this));
				g.drawString(rectangleArea, (int) bounds.getX(), (int) 
							bounds.getY()); 
			}
			if (MeasurementAttributes.SHOWID.get(this))
			{
				g.setColor(this.getTextColor());
				bounds = g.getFontMetrics().getStringBounds(getROI().getID()+"", 
						g);
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
	 * Overridden to stop updating shape if read-only.
	 * @see AbstractAttributedFigure#transform(AffineTransform)
	 */
	public void transform(AffineTransform tx)
	{
		if (!readOnly && interactable)
		{
			super.transform(tx);
			this.setObjectDirty(true);
		}
	}
		
	/**
	 * Overridden to stop updating shape if read-only.
	 * @see AbstractAttributedFigure#setBounds(Point2D.Double, Point2D.Double)
	 */
	public void setBounds(Point2D.Double anchor, Point2D.Double lead) 
	{
		if (!readOnly && interactable)
		{
			super.setBounds(anchor, lead);
			this.setObjectDirty(true);
		}
	}
	
	/**
	 * Overridden to return the correct handles.
	 * @see AbstractAttributedFigure#createHandles(int)
	 */
	/* cannot do that otherwise enter in an infinite loop
	public Collection<Handle> createHandles(int detailLevel) 
	{
		if (!readOnly)
			return super.createHandles(detailLevel);
		else {
			LinkedList<Handle> handles = new LinkedList<Handle>();
			handles.add(new FigureSelectionHandle(this));
			return handles;
		}
	}
	*/
	
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
	 * Calculate the area of the figure. 
	 * @return see above.
	 */
	public Length getArea()
	{
		return new LengthI(getMeasurementWidth().getValue()*getMeasurementHeight().getValue(), getUnit());
	}
	
	/**
	 * Calculate the perimeter of the figure. 
	 * @return see above.
	 */
	public Length getPerimeter()
	{
		return new LengthI(getMeasurementWidth().getValue()*2+getMeasurementHeight().getValue()*2, getUnit());
	}

	/** 
	 * Calculate the centre of the figure. 
	 * @return see above.
	 */
	public UnitPoint getCentre()
	{
		double cx = rectangle.getCenterX();
		double cy = rectangle.getCenterY();
		
     	return new UnitPoint(transformX(cx), transformY(cy));
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
		AnnotationKeys.CENTREX.set(shape, getCentre().x);
		AnnotationKeys.CENTREY.set(shape, getCentre().y);
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
				if (rectangle.contains(x, y)) 
					vector.add(new Point(x, y));
		
		return vector;
	}
	
	/**
	 * Implemented as specified by the {@link ROIFigure} interface.
	 * @see ROIFigure#getSize()
	 */
	public int getSize()
	{
		Rectangle r = rectangle.getBounds();
		int total = 0;
		int xEnd = r.x+r.width, yEnd = r.y+r.height;
		int x, y;
		for (y = r.y; y < yEnd; ++y) 
			for (x = r.x; x < xEnd; ++x) 
				if (rectangle.contains(x, y)) 
					total++;
		
		return total;
	}

	/**
	 * Implemented as specified by the {@link ROIFigure} interface.
	 * @see ROIFigure#setStatus(int)
	 */
	public void setStatus(int status) { this.status = status; }
	
	/**
	 * Implemented as specified by the {@link ROIFigure} interface.
	 * @see ROIFigure#getStatus()
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
	
	/**
	 * Implemented as specified by the {@link ROIFigure} interface
	 * @see ROIFigure#isClientObject()
	 */
	public boolean isClientObject() 
	{
		return clientObject;
	}

	/**
	 * Implemented as specified by the {@link ROIFigure} interface
	 * @see ROIFigure#setClientObject(boolean)
	 */
	public void setClientObject(boolean clientSide) 
	{
		clientObject = clientSide;
	}

	/**
	 * Implemented as specified by the {@link ROIFigure} interface
	 * @see ROIFigure#isDirty()
	 */
	public boolean isDirty() { return dirty; }

	/**
	 * Implemented as specified by the {@link ROIFigure} interface
	 * @see ROIFigure#setObjectDirty(boolean)
	 */
	public void setObjectDirty(boolean dirty) { this.dirty = dirty; }

	/**
	 * Clones the figure.
	 * @see MeasureRectangleFigure#clone()
	 */
	public MeasureRectangleFigure clone()
	{
		MeasureRectangleFigure that = (MeasureRectangleFigure) super.clone();
		that.setReadOnly(this.isReadOnly());
		that.setClientObject(this.isClientObject());
		that.setObjectDirty(true);
		that.setInteractable(true);
		return that;
	}
	
	/**
	 * Marks the figure as dirty.
	 * @see MeasureRectangleFigure#setText(String)
	 */
	public void setText(String text)
	{
		super.setText(text);
		this.setObjectDirty(true);
	}
	
	/**
	 * Implemented as specified by the {@link ROIFigure} interface
	 * @see ROIFigure#getFigureListeners()
	 */
	public List<FigureListener> getFigureListeners()
	{
		List<FigureListener> figListeners = new ArrayList<FigureListener>();
		Object[] listeners = listenerList.getListenerList();
		for (Object listener : listeners)
			if (listener instanceof FigureListener)
				figListeners.add((FigureListener)listener);
		return figListeners;
	}
	
	/**
	 * Implemented as specified by the {@link ROIFigure} interface
	 * @see ROIFigure#canAnnotate()
	 */
	public boolean canAnnotate() { return annotatable; }

	/**
	 * Implemented as specified by the {@link ROIFigure} interface
	 * @see ROIFigure#canDelete()
	 */
	public boolean canDelete() { return deletable; }

	/**
	 * Implemented as specified by the {@link ROIFigure} interface
	 * @see ROIFigure#canAnnotate()
	 */
	public boolean canEdit() { return editable; }
	
	/**
	 * Implemented as specified by the {@link ROIFigure} interface
	 * @see ROIFigure#setInteractable(boolean)
	 */
	public void setInteractable(boolean interactable)
	{
		this.interactable = interactable;
	}
	
	/**
	 * Implemented as specified by the {@link ROIFigure} interface
	 * @see ROIFigure#canInteract()
	 */
	public boolean canInteract() { return interactable; }

	/**
	 * Transforms the given x pixel value into a unit object
	 * @param x A pixel value in x direction
	 */
	private Length transformX(double x) {
		return transformX((int)x);
	}
	
	/**
	 * Transforms the given y pixel value into a unit object
	 * @param y A pixel value in y direction
	 */
	private Length transformY(double y) {
		return transformY((int)y);
	}
	
	/**
	 * Transforms the given x pixel value into a unit object
	 * @param x A pixel value in x direction
	 */
	private Length transformX(int x) {
		if(units.getPixelSizeX()!=null) 
			return new LengthI(x*units.getPixelSizeX().getValue(), units.getPixelSizeX().getUnit());
		else
			return new LengthI(x, UnitsLength.PIXEL);
	}
	
	/**
	 * Transforms the given x pixel value into a unit object
	 * @param x A pixel value in x direction
	 */
	private Length transformY(int y) {
		if(units.getPixelSizeY()!=null) 
			return new LengthI(y*units.getPixelSizeY().getValue(), units.getPixelSizeY().getUnit());
		else
			return new LengthI(y, UnitsLength.PIXEL);
	}
	
	/**
	 * Get the unit which is used for the pixel sizes
	 */
	private UnitsLength getUnit() {
		if(units.getPixelSizeX()!=null)
			return units.getPixelSizeX().getUnit();
		else
			return UnitsLength.PIXEL;
	}
}