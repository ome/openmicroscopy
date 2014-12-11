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
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;



//Third-party libraries
import org.jhotdraw.draw.AbstractAttributedFigure;
import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.FigureListener;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys;
import org.openmicroscopy.shoola.util.roi.model.annotation.MeasurementAttributes;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.util.MeasurementUnits;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.UnitsObject;
import org.openmicroscopy.shoola.util.ui.drawingtools.figures.EllipseTextFigure;
import org.openmicroscopy.shoola.util.ui.drawingtools.figures.FigureUtil;

/** 
 * Ellipse figure with measurement.
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
public class MeasureEllipseFigure 
	extends EllipseTextFigure 
	implements ROIFigure
{

	/** Flag indicating the figure can/cannot be deleted.*/
	private boolean deletable;
	
	/** Flag indicating the figure can/cannot be annotated.*/
	private boolean annotatable;
	
	/** Flag indicating the figure can/cannot be edited.*/
	private boolean editable;
	
	/** Is this figure read only. */
	private boolean 			readOnly;
	
	/** Is this figure a client object. */
	private boolean clientObject;
	
	/** has the figure been modified. */
	private boolean dirty;

	/** Bounds of the measurement. */
	private Rectangle2D			measurementBounds;
	
	/** The ROI containing the ROIFigure which in turn contains this Figure. */
	protected ROI				roi;
	
	/** The ROIFigure contains this Figure. */
	protected ROIShape			shape;
	
	/** The Measurement units, and values of the image. */
	private MeasurementUnits	units;
	
	/** 
	 * The status of the figure i.e. {@link ROIFigure#IDLE} or 
	 * {@link ROIFigure#MOVING}. 
	 */
	private int 				status;
	
	/** Flag indicating if the user can move or resize the shape.*/
	private boolean interactable;
	
	/** The units of reference.*/
	private String refUnits;

	   /**
     * Formats the area.
     * 
     * @param value The value to format.
     * @return See above.
     */
    private String formatValue(double value)
    {
        NumberFormat formatter = new DecimalFormat(FORMAT_PATTERN);
        if (units.isInMicrons()){ 
            UnitsObject v = UIUtilities.transformSquareSize(value);
            StringBuffer buffer = new StringBuffer();
            buffer.append(formatter.format(v.getValue()));
            buffer.append(v.getUnits());
            return buffer.toString();
        }
        return addUnits(formatter.format(value));
    }

	/** Creates a new instance. */
	public MeasureEllipseFigure()
	{
		this(DEFAULT_TEXT);
	}
	
	/** 
	 * Creates a new instance. 
	 * 
	 * @param text The text of the ellipse. 
	 */
	public MeasureEllipseFigure(String text)
	{
		this(text, 0, 0, 0, 0, false, true, true, true, true);
	}

	/** 
	 * Creates a new instance. 
	 * 
	 * @param readOnly Pass <code>true</code> if the ROI is read only, 
	 * 				   <code>false</code> otherwise.
     * @param isClientObject the figure is a client object.
     * @param editable Flag indicating the figure can/cannot be edited.
	 * @param deletable Flag indicating the figure can/cannot be deleted.
	 * @param annotatable Flag indicating the figure can/cannot be annotated.
	 */
	public MeasureEllipseFigure(boolean readOnly, boolean isClientObject,
			boolean editable, boolean deletable, boolean annotatable)
	{
		this(ROIFigure.DEFAULT_TEXT, 0, 0, 0, 0, readOnly, isClientObject, 
				editable, deletable, annotatable);
	}
	
	/** 
	 * Creates a new instance.
	 * 
	 * @param x     The x-coordinate of the figure. 
	 * @param y     The y-coordinate of the figure.
	 * @param width	The width of the figure. 
	 * @param height The height of the figure. 
	 */
	public MeasureEllipseFigure(double x, double y, double width, double height)
	{
		this(ROIFigure.DEFAULT_TEXT, x, y, width, height, false, true,
				true, true, true);
	}
	
	/** 
	 * Creates a new instance.
	 * 
	 * @param text  The text of the ellipse. 
	 * @param x     The x-coordinate of the figure. 
	 * @param y     The y-coordinate of the figure.
	 * @param width	The width of the figure. 
	 * @param height The height of the figure. 
	 * @param readOnly The figure is read only.
     * @param clientObject the figure is a client object.
     * @param editable Flag indicating the figure can/cannot be edited.
	 * @param deletable Flag indicating the figure can/cannot be deleted.
	 * @param annotatable Flag indicating the figure can/cannot be annotated.
	 */
	public MeasureEllipseFigure(String text, double x, double y, double width,
			double height, boolean readOnly, boolean clientObject,
			boolean editable, boolean deletable, boolean annotatable)
	{
		super(text, x, y, width, height);
		setAttributeEnabled(MeasurementAttributes.TEXT_COLOR, true);
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
   		refUnits = UnitsObject.MICRONS;
	}
	
	/** 
	 * Returns the x-coordinate of the figure, 
	 * convert to the size in the reference units 
	 * 
	 * @return See above.
	 */
	public double getMeasurementX()
	{
		if (units.isInMicrons()) 
			return UIUtilities.transformSize(
					getX()*units.getMicronsPixelX(), refUnits);
		return getX();
	}
	
	/** 
	 * Returns the y-coordinate of the figure, 
	 * convert to the size in the reference units 
	 * 
	 * @return See above.
	 */
	public double getMeasurementY()
	{
		if (units.isInMicrons())
			return UIUtilities.transformSize(
					getY()*units.getMicronsPixelY(), refUnits);
		return getY();
	}
	
	/** 
	 * Returns the width of the figure, 
	 * convert to microns if <code>isInMicrons</code> is <code>true</code>. 
	 * 
	 * @return See above.
	 */
	public double getMeasurementWidth()
	{
		if (units.isInMicrons())
			return UIUtilities.transformSize(
					getWidth()*units.getMicronsPixelX(), refUnits);
		return getWidth();
	}
		
	/** 
	 * Returns the height of the figure, 
	 * convert to microns if <code>isInMicrons</code> is <code>true</code>. 
	 * 
	 * @return See above.
	 */
	public double getMeasurementHeight()
	{
		if (units.isInMicrons())
			return UIUtilities.transformSize(
					getHeight()*units.getMicronsPixelY(), refUnits);
		return getHeight();
	}
		
	/** 
	 * Returns the centre of the figure,
	 * convert to microns if <code>isInMicrons</code> is <code>true</code>. 
	 * 
	 * @return See above.
	 */
	public Point2D getMeasurementCentre()
	{
		if (units.isInMicrons()) {
			double tx = UIUtilities.transformSize(
					getCentre().getX()*units.getMicronsPixelX(), refUnits);
			double ty = UIUtilities.transformSize(
					getCentre().getY()*units.getMicronsPixelY(), refUnits);
			return new Point2D.Double(tx, ty);
		}
		return getCentre();
	}
		
	/** 
	 * Returns the y-coordinate of the figure.
	 * 
	 * @return See above.
	 */
	public double getX()
	{
		AffineTransform t = AttributeKeys.TRANSFORM.get(this);
		if (t == null) return ellipse.getX();
		Point2D src = new Point2D.Double(ellipse.getX(), ellipse.getY());
		Point2D dest = new Point2D.Double();
		t.transform(src, dest);
		return dest.getX();
	}
	
	/** 
	 * Returns the y-coordinate of the figure.
	 * 
	 * @return See above.
	 */
	public double getY() 
	{
		AffineTransform t = AttributeKeys.TRANSFORM.get(this);
		if (t == null) return ellipse.getY();
		Point2D src = new Point2D.Double(ellipse.getX(), ellipse.getY());
		Point2D dest = new Point2D.Double();
		t.transform(src, dest);
		return dest.getY();
	}

	/** 
	 * Returns the width of the figure.
	 * 
	 * @return See above.
	 */
	public double getWidth() { return super.getWidth(); }
	
	/** 
	 * Returns the height of the figure.
	 * 
	 * @return See above.
	 */
	public double getHeight() { return super.getHeight(); }
	
	/**
	 * Draws the figure on the graphics context.
	 * 
	 * @param g The graphics context.
	 */
	public void draw(Graphics2D g)
	{
		super.draw(g);
		if (MeasurementAttributes.SHOWMEASUREMENT.get(this) || 
				MeasurementAttributes.SHOWID.get(this))
		{
			String ellipseArea = formatValue(getArea());
			Double sz = (Double) getAttribute(MeasurementAttributes.FONT_SIZE);
            Font font = (Font) getAttribute(MeasurementAttributes.FONT_FACE);
            if (font != null) g.setFont(font.deriveFont(sz.floatValue()));
            else {
                g.setFont(new Font(FONT_FAMILY, FONT_STYLE, sz.intValue()));
            }
			Rectangle2D stringBoundsbounds = 
				g.getFontMetrics().getStringBounds(ellipseArea, g);
			measurementBounds =
					new Rectangle2D.Double(getCentreX()
							-stringBoundsbounds.getWidth()/2, this.getCentreY()
							+stringBoundsbounds.getHeight()/2, 
							stringBoundsbounds.getWidth(), 
							stringBoundsbounds.getHeight());
			if (MeasurementAttributes.SHOWMEASUREMENT.get(this))
			{
				g.setColor(
						MeasurementAttributes.MEASUREMENTTEXT_COLOUR.get(this));
				g.drawString(ellipseArea, 
						(int) measurementBounds.getX(), 
						(int) measurementBounds.getY());
			}
			if (MeasurementAttributes.SHOWID.get(this))
			{
				g.setColor(this.getTextColor());
				measurementBounds = 
					g.getFontMetrics().getStringBounds(getROI().getID()+"", g);
				measurementBounds = new Rectangle2D.Double(
							getBounds().getCenterX()-
							measurementBounds.getWidth()/2,
							getBounds().getCenterY()+
							measurementBounds.getHeight()/2,
							measurementBounds.getWidth(), 
							measurementBounds.getHeight());
				g.drawString(this.getROI().getID()+"", 
						(int) measurementBounds.getX(), 
						(int) measurementBounds.getY());
			}
		}
	}
	
	/**
	 * Calculates the bounds of the rendered figure, including the text 
	 * rendered. 
	 * 
	 * @return See above.
	 */
	public Rectangle2D.Double getDrawingArea()
	{
		Rectangle2D.Double newBounds=super.getDrawingArea();
		if (measurementBounds != null)
		{
			if (newBounds.getX() > measurementBounds.getX())
			{
				double diff = newBounds.x-measurementBounds.getX();
				newBounds.x = measurementBounds.getX();
				newBounds.width = newBounds.width+diff;
			}
			if (newBounds.getY() > measurementBounds.getY())
			{
				double diff = newBounds.y-measurementBounds.getY();
				newBounds.y = measurementBounds.getY();
				newBounds.height = newBounds.height+diff;
			}
			if (measurementBounds.getX()+
					measurementBounds.getWidth() > newBounds.getX()
					+newBounds.getWidth())
			{
				double diff =
					measurementBounds.getX()+
					measurementBounds.getWidth()-newBounds.getX()
								+newBounds.getWidth();
				newBounds.width = newBounds.width+diff;
			}
			if (measurementBounds.getY()+
					measurementBounds.getHeight() > newBounds.getY()
					+newBounds.getHeight())
			{
				double diff =
					measurementBounds.getY()+
					measurementBounds.getHeight()-newBounds.getY()
								+newBounds.getHeight();
				newBounds.height = newBounds.height+diff;
			}
		}
		return newBounds;
	}
	
	/**
	 * Adds units to the string.
	 *  
	 * @param str see above.
	 * @return returns the string with the units added. 
	 */
	public String addUnits(String str)
	{
		if (shape == null) return str;
		if (units.isInMicrons()) 
			return str+refUnits+UIUtilities.SQUARED_SYMBOL;
		return str+UIUtilities.PIXELS_SYMBOL+UIUtilities.SQUARED_SYMBOL;
	}
	
	/**
	 * Calculates the area of the figure. 
	 * 
	 * @return see above.
	 */
	public double getArea()
	{
		
		return (getMeasurementHeight()/2)*(getMeasurementWidth()/2)*Math.PI;
	}
	
	/**
	 * Calculates the perimeter of the figure. 
	 * 
	 * @return see above.
	 */
	public double getPerimeter()
	{
		if (getMeasurementWidth() == getMeasurementHeight()) 
			return getMeasurementWidth()*2*Math.PI;
		
		double a = Math.max(getMeasurementWidth(), getMeasurementHeight());
		double b = Math.min(getMeasurementWidth(), getMeasurementHeight());
		// approximation of c for ellipse. 
		return Math.PI*(3*a+3*b-Math.sqrt((a+3*b)*(b+3*a)));
	}

	/** 
	 * Calculate the centre of the figure. 
	 * @return see above.
	 */
	public Point2D getCentre()
	{
		return new Point2D.Double(Math.round(getCentreX()), Math
			.round(getCentreY()));
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
	public void calculateMeasurements()
	{
		if (shape == null) return;
		AnnotationKeys.AREA.set(shape, getArea());
		AnnotationKeys.HEIGHT.set(shape, getMeasurementHeight());
		AnnotationKeys.WIDTH.set(shape, getMeasurementWidth());
		AnnotationKeys.PERIMETER.set(shape, getPerimeter());
		AnnotationKeys.CENTREX.set(shape, getMeasurementCentre().getX());
		AnnotationKeys.CENTREY.set(shape, getMeasurementCentre().getY());
	}
	
	/**
	 * Implemented as specified by the {@link ROIFigure} interface.
	 * @see ROIFigure#getType()
	 */
	public String getType() { return FigureUtil.ELLIPSE_TYPE; }

	/**
	 * Implemented as specified by the {@link ROIFigure} interface.
	 * @see ROIFigure#setMeasurementUnits(MeasurementUnits)
	 */
	public void setMeasurementUnits(MeasurementUnits units)
	{
		this.units = units;
		refUnits = UIUtilities.transformSize(
				units.getMicronsPixelX()).getUnits();
	}

	/**
	 * Implemented as specified by the {@link ROIFigure} interface.
	 * @see ROIFigure#getPoints()
	 */
	public List<Point> getPoints()
	{
		Shape transformedEllipse = getTransformedShape();
		Rectangle2D r = transformedEllipse.getBounds2D();
		//getP

		List<Point> vector = new ArrayList<Point>
				((int) r.getHeight()*(int) r.getWidth());
		double xEnd = (r.getX()+r.getWidth());
		double yEnd = (r.getY()+r.getHeight());
		double startX = r.getX();
		double startY =  r.getY();
		double x, y;
		for (y = startY; y < yEnd; ++y)
			for (x = startX; x < xEnd; ++x)
//				if(containsMapped(x,y))
				if (transformedEllipse.intersects(x, y, 0.001, 0.001))
					vector.add(new Point((int) x, (int) y));
		return vector; 
	}
	
	/**
	 * Implemented as specified by the {@link ROIFigure} interface.
	 * @see ROIFigure#getSize()
	 */
	public int getSize()
	{
		Shape transformedEllipse = getTransformedShape();
		Rectangle2D r = transformedEllipse.getBounds2D();
		//getP

		int total = 0;
		double xEnd = (r.getX()+r.getWidth());
		double yEnd = (r.getY()+r.getHeight());
		double startX = r.getX();
		double startY =  r.getY();
		double x, y;
		for (y = startY; y < yEnd; ++y)
			for (x = startX; x < xEnd; ++x)
				if (transformedEllipse.intersects(x, y, 0.001, 0.001))
					total++;
		return total; 
	}
	
	/**
	 * Overridden to stop updating shape if read only
	 * @see AbstractAttributedFigure#transform(AffineTransform)
	 */
	public void transform(AffineTransform tx)
	{
		if (!readOnly && interactable)
		{
			this.setObjectDirty(true);
			super.transform(tx);
		}
	}
		
	/**
	 * Overridden to stop updating shape if read only.
	 * @see AbstractAttributedFigure#setBounds(Double, Double)
	 */
	public void setBounds(Point2D.Double anchor, Point2D.Double lead) 
	{
		if (!readOnly && interactable)
		{
			this.setObjectDirty(true);
			super.setBounds(anchor, lead);
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
		else
		{
			LinkedList<Handle> handles = new LinkedList<Handle>();
			handles.add(new FigureSelectionHandle(this));
			return handles;
		}
	}
	*/

	/**
	 * Invalidate the figure and remove the cachedTransformedShape, this means
	 * that the figures geometry has changed and it should be redrawn.
	 * @see AbstractAttributedFigure#invalidate()
	 */
	public void invalidate()
	{
		if (!readOnly)
			super.invalidate();
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
	public boolean isClientObject() { return clientObject; }

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
	 * Overridden to set the various flags.
	 * @see MeasureEllipseFigure#clone()
	 */
	public MeasureEllipseFigure clone()
	{
		MeasureEllipseFigure that = (MeasureEllipseFigure) super.clone();
		that.setReadOnly(this.isReadOnly());
		that.setClientObject(this.isClientObject());
		that.setObjectDirty(true);
		that.setInteractable(true);
		return that;
	}
	
	/**
	 * Overridden to mark the object has dirty.
	 * @see MeasureEllipseFigure#setText(String)
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
				figListeners.add((FigureListener) listener);
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

}