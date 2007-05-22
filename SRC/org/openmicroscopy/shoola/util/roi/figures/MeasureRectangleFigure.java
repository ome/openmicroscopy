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
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.Action;

//Third-party libraries
import org.jhotdraw.draw.AbstractAttributedFigure;
import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.Handle;
import org.jhotdraw.draw.RectangleFigure;
import org.jhotdraw.draw.RotateHandle;
import org.jhotdraw.geom.Geom;
import org.jhotdraw.util.ResourceBundleUtil;

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
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;

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
	extends AbstractAttributedFigure
	implements ROIFigure
{
	AttributeKey<AffineTransform>TRANSFORM = new AttributeKey<AffineTransform>("transform", null, true);

	protected Rectangle2D.Double rectangle;
    /**
     * This is used to perform faster drawing and hit testing.
     */
	protected Shape				cachedTransformedShape;
	protected	Rectangle2D 		bounds;
	protected ROI					roi;
	protected ROIShape 			shape;

	   
    /** Creates a new instance. */
    public MeasureRectangleFigure() 
    {
        this(0, 0, 0, 0);
    }
    
    public MeasureRectangleFigure(double x, double y, double width, double height) 
    {
		super();
    	rectangle = new Rectangle2D.Double(x, y, width, height);
        shape = null;
		roi = null;
		
    }
    
    // DRAWING
    protected void drawFill(Graphics2D g) 
    {
        g.fill(getTransformedShape());
    }
    
    protected void drawStroke(Graphics2D g) 
    {
        g.draw(getTransformedShape());
    }
    // SHAPE AND BOUNDS
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
    	if(TRANSFORM.get(this) == null)
            return rectangle.getWidth();
    	AffineTransform value = TRANSFORM.get(this);
    	Point2D upperBound = new Point2D.Double(rectangle.getWidth(), 0);
    	Point2D lowerBound = new Point2D.Double(0, 0);
    	Point2D transformedUpperBound = value.transform(upperBound, null);
    	Point2D transformedLowerBound = value.transform(lowerBound, null);
    	return transformedUpperBound.distance(transformedLowerBound);
    }
    
    public double getHeight() 
    {
    	if(TRANSFORM.get(this) == null)
            return rectangle.getHeight();
    	AffineTransform value = TRANSFORM.get(this);
    	Point2D upperBound = new Point2D.Double(rectangle.getWidth()/2, 
    			rectangle.getHeight());
    	Point2D lowerBound = new Point2D.Double(rectangle.getWidth()/2, 0);
    	Point2D transformedUpperBound = value.transform(upperBound, null);
    	Point2D transformedLowerBound = value.transform(lowerBound, null);
    	return transformedUpperBound.distance(transformedLowerBound);
    }
    
    public Rectangle2D.Double getBounds() 
    {
        Rectangle2D rx = getTransformedShape().getBounds2D();
        Rectangle2D.Double r = (rx instanceof Rectangle2D.Double) ? 
        		(Rectangle2D.Double) rx : new Rectangle2D.Double(rx.getX(), rx.getY(), rx.getWidth(), rx.getHeight());
        return r;
    }
    public Rectangle2D.Double getFigureDrawBounds() 
    {
        Rectangle2D rx = getTransformedShape().getBounds2D();
        Rectangle2D.Double r = (rx instanceof Rectangle2D.Double) ? 
        		(Rectangle2D.Double) rx : new Rectangle2D.Double(rx.getX(), rx.getY(), rx.getWidth(), rx.getHeight());
        double g = AttributeKeys.getPerpendicularHitGrowth(this);
        Geom.grow(r, g, g);
        return r;
    }
    /**
     * Checks if a Point2D.Double is inside the figure.
     */
    public boolean contains(Point2D.Double p) 
    {
        return getTransformedShape().contains(p);
    }
    
    protected void invalidateTransformedShape() 
    {
        cachedTransformedShape = null;
    }
    
    protected Shape getTransformedShape() 
    {
        if (cachedTransformedShape == null) 
            if (TRANSFORM.get(this) == null) 
                cachedTransformedShape = rectangle;
            else 
                cachedTransformedShape = TRANSFORM.get(this).createTransformedShape(rectangle);
        return cachedTransformedShape;
    }
    
    public void basicSetBounds(Point2D.Double anchor, Point2D.Double lead) 
    {
    	rectangle.x = Math.min(anchor.x, lead.x);
    	rectangle.y = Math.min(anchor.y , lead.y);
    	rectangle.width = Math.max(0.1, Math.abs(lead.x - anchor.x));
    	rectangle.height = Math.max(0.1, Math.abs(lead.y - anchor.y));
    }
    
    /**
     * Transforms the figure.
     *
     * @param tx the transformation.
     */
    public void basicTransform(AffineTransform tx) 
    {
        invalidateTransformedShape();
        if (TRANSFORM.get(this) != null ||
                (tx.getType() & 
                		(AffineTransform.TYPE_TRANSLATION | 
                			AffineTransform.TYPE_MASK_SCALE)) != tx.getType()) 
        {
            if (TRANSFORM.get(this) == null) 
            {
                TRANSFORM.basicSet(this, (AffineTransform) tx.clone());
            } 
            else 
            {
                TRANSFORM.get(this).preConcatenate(tx);
            }
        } 
        else 
        {
            Point2D.Double anchor = getStartPoint();
            Point2D.Double lead = getEndPoint();
            basicSetBounds(
                    (Point2D.Double) tx.transform(anchor, anchor),
                    (Point2D.Double) tx.transform(lead, lead)
                    );
        }
    }
    
    public void restoreTransformTo(Object geometry) 
    {
            invalidateTransformedShape();
            Object[] o = (Object[]) geometry;
            rectangle = (Rectangle2D.Double) ((Rectangle2D.Double) o[0]).clone();
            if (o[1] == null) 
            {
                TRANSFORM.set(this, null);
            } 
            else 
            {
            	TRANSFORM.set(this, (AffineTransform) ((AffineTransform) o[1]).clone());
            }
    }
    
    public Object getTransformRestoreData() 
    {
        return new Object[] {
            rectangle.clone(),
            TRANSFORM.get(this)
        };
    }
    
    public Collection<Handle> createHandles(int detailLevel) 
    {
        LinkedList<Handle> handles = (LinkedList<Handle>) super.createHandles(detailLevel);
        handles.add(new RotateHandle(this));
        return handles;
    }
    
    @Override public Collection<Action> getActions(Point2D.Double p) 
    {
        ResourceBundleUtil labels = ResourceBundleUtil.getLAFBundle("org.jhotdraw.samples.svg.Labels");
        LinkedList<Action> actions = new LinkedList<Action>();
        if (TRANSFORM.get(this) != null) 
        {
            actions.add(new AbstractAction(labels.getString("removeTransform")) 
            {
                public void actionPerformed(ActionEvent evt) 
                {
                    TRANSFORM.set(MeasureRectangleFigure.this, null);
                }
            });
        }
        return actions;
    }

    @Override public void invalidate() 
    {
        super.invalidate();
        invalidateTransformedShape();
    }

    public MeasureRectangleFigure clone() {
    	MeasureRectangleFigure that = (MeasureRectangleFigure) super.clone();
        that.rectangle = (Rectangle2D.Double) this.rectangle.clone();
        return that;
    }
    
	public void draw(Graphics2D g)
	{
		super.draw(g);
		
		if(SHOWMEASUREMENT.get(this))
		{
			NumberFormat formatter = new DecimalFormat("###.#");
			String rectangleArea = formatter.format(getArea());
			rectangleArea = addUnits(rectangleArea);
			double sz = ((Double)this.getAttribute(AttributeKeys.FONT_SIZE));
			g.setFont(new Font("Arial",Font.PLAIN, (int)sz));
			bounds = g.getFontMetrics().getStringBounds(rectangleArea, g);
			bounds = new Rectangle2D.Double(this.getBounds().getCenterX()-bounds.getWidth()/2,
					this.getBounds().getCenterY()+bounds.getHeight()/2,
					bounds.getWidth(), bounds.getHeight());
			g.setColor(MEASUREMENTTEXT_COLOUR.get(this));
			g.drawString(rectangleArea, (int)bounds.getX(), (int)bounds.getY()); 
					
		}
	}
				
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
	
	
	    

	public String addUnits(String str)
	{
		if(shape==null)
			return str;
		if(INMICRONS.get(shape))
			return str+"\u00B5m\u00B2";
		else
			return str+"px\u00B2";
	}


	public double getArea()
	{
		return getWidth()*getHeight();
	}
	
	public double getPerimeter()
	{
		return getWidth()*2+getHeight()*2;
	}

	public Point2D getCentre()
	{
		return new Point2D.Double(rectangle.getCenterX(), rectangle.getCenterY());
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
			if(shape==null)
				return;
			AREA.set(shape, getArea());
			WIDTH.set(shape, getWidth());		
			HEIGHT.set(shape, getHeight());		
			PERIMETER.set(shape, getPerimeter());		
			CENTREX.set(shape, getCentre().getX());
			CENTREY.set(shape, getCentre().getY());
	}

}


