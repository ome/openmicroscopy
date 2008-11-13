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
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.AttributeKeys;
import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys;
import org.openmicroscopy.shoola.util.roi.model.annotation.MeasurementAttributes;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.util.MeasurementUnits;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.drawingtools.figures.EllipseTextFigure;
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
public class MeasureEllipseFigure 
	extends EllipseTextFigure 
	implements ROIFigure
{
	Rectangle2D measurementBounds;
	
	/** The ROI containing the ROIFigure which in turn contains this Figure. */
	protected ROI				roi;
	
	/** The ROIFigure contains this Figure. */
	protected ROIShape			shape;
	
	/** The Measurement units, and values of the image. */
	private MeasurementUnits	units;
	
	private int 				status;
	
	/** Creates a new instance. */
	public MeasureEllipseFigure()
	{
		this("Text", 0, 0, 0, 0);
	}
	
	/** 
	 * Creates a new instance.
	 * 
	 * @param text text of the ellipse. 
	 * @param x    coord of the figure. 
	 * @param y    coord of the figure. 
	 * @param width of the figure. 
	 * @param height of the figure. 
	 */
	public MeasureEllipseFigure(String text, double x, double y, double width,
			double height)
	{
		super(text, x, y, width, height);
		setAttributeEnabled(MeasurementAttributes.TEXT_COLOR, true);
	   	shape=null;
		roi=null;
		status = IDLE;
		}
	
	/** 
	 * Creates a new instance. 
	 * 
	 * @param text string shown in the ellipse.
	 */
	public MeasureEllipseFigure(String text)
	{
		this(text, 0, 0, 0, 0);
	}
	
	public Ellipse2D getEllipse()
	{
		return ellipse;
	}
	
	/** 
	 * Creates a new instance.
	 * 
	 * @param x    coord of the figure. 
	 * @param y    coord of the figure. 
	 * @param width of the figure. 
	 * @param height of the figure. 
	 */
	public MeasureEllipseFigure(double x, double y, double width, double height)
	{
		this("Text", x, y, width, height);
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
	 * Get the centre of the figure, convert to microns if isInMicrons set. 
	 * 
	 * @return see above.
	 */
	public Point2D getMeasurementCentre()
	{
		if (units.isInMicrons()) 
			return new Point2D.Double(getCentre().getX()
				*units.getMicronsPixelX(), getCentre().getY()
				*units.getMicronsPixelY());
		return getCentre();
	}
		
	/** 
	 * Get the x coord of the figure. 
	 * @return see above.
	 */
	public double getX()
	{
		if(AttributeKeys.TRANSFORM.get(this)!=null)
		{
			AffineTransform t = AttributeKeys.TRANSFORM.get(this);
			Point2D src = new Point2D.Double(ellipse.getX(), ellipse.getY());
			Point2D dest = new Point2D.Double();
			t.transform(src, dest);
			return dest.getX();
		}
		return ellipse.getX();
	}
	
	  
	/** 
	 * Get the y coord of the figure. 
	 * 
	 * @return see above.
	 */
	public double getY() 
	{
		if(AttributeKeys.TRANSFORM.get(this)!=null)
		{
			AffineTransform t = AttributeKeys.TRANSFORM.get(this);
			Point2D src = new Point2D.Double(ellipse.getX(), ellipse.getY());
			Point2D dest = new Point2D.Double();
			t.transform(src, dest);
			return dest.getY();
		}
		return ellipse.getY(); 
	}
	
	/** 
	 * Get the x coord of the figure. 
	 * @return see above.
	 */
	public double getCentreX()
	{
		if(AttributeKeys.TRANSFORM.get(this)!=null)
		{
			AffineTransform t = AttributeKeys.TRANSFORM.get(this);
			Point2D src = new Point2D.Double(ellipse.getCenterX(), ellipse.getCenterY());
			Point2D dest = new Point2D.Double();
			t.transform(src, dest);
			return dest.getX();
		}
		return ellipse.getCenterX();
	}
	
	/** 
	 * Get the y coord of the figure. 
	 * 
	 * @return see above.
	 */
	public double getCentreY()
	{
		if(AttributeKeys.TRANSFORM.get(this)!=null)
		{
			AffineTransform t = AttributeKeys.TRANSFORM.get(this);
			Point2D src = new Point2D.Double(ellipse.getCenterX(), ellipse.getCenterY());
			Point2D dest = new Point2D.Double();
			t.transform(src, dest);
			return dest.getY();
		}
		return ellipse.getCenterY();
	}
	
	/** 
	 * Get the width coord of the figure.
	 *  
	 * @return see above.
	 */
	public double getWidth()
	{
		return super.getWidth();
	}
	
	/** 
	 * Get the height coord of the figure.
	 *  
	 * @return see above.
	 */
	public double getHeight()
	{
		return super.getHeight();
	}
	
	/**
	 * Draw the figure on the graphics context.
	 * 
	 * @param g the graphics context.
	 */
	public void draw(Graphics2D g)
	{
		super.draw(g);
		if (MeasurementAttributes.SHOWMEASUREMENT.get(this))
		{
			NumberFormat formatter=new DecimalFormat("###.#");
			String ellipseArea=formatter.format(getArea());
			ellipseArea=addUnits(ellipseArea);
			double sz=((Double) this.getAttribute(MeasurementAttributes.FONT_SIZE));
			g.setFont(new Font("Arial", Font.PLAIN, (int) sz));
			Rectangle2D stringBoundsbounds= g.getFontMetrics().getStringBounds(ellipseArea, g);
			measurementBounds=
					new Rectangle2D.Double(getCentreX()
							-stringBoundsbounds.getWidth()/2, this.getCentreY()
							+stringBoundsbounds.getHeight()/2, stringBoundsbounds.getWidth(), 
							stringBoundsbounds.getHeight());
			g.setColor(MeasurementAttributes.MEASUREMENTTEXT_COLOUR.get(this));
			g.drawString(ellipseArea, (int) measurementBounds.getX(), (int) measurementBounds.getY());
		}
	}
	
	/**
	 * Calculates the bounds of the rendered figure, including the text rendered. 
	 * @return see above.
	 */
	public Rectangle2D.Double getDrawingArea()
	{
		Rectangle2D.Double newBounds=super.getDrawingArea();
		if (measurementBounds!=null)
		{
			if (newBounds.getX()>measurementBounds.getX())
			{
				double diff=newBounds.x-measurementBounds.getX();
				newBounds.x=measurementBounds.getX();
				newBounds.width=newBounds.width+diff;
			}
			if (newBounds.getY()>measurementBounds.getY())
			{
				double diff=newBounds.y-measurementBounds.getY();
				newBounds.y=measurementBounds.getY();
				newBounds.height=newBounds.height+diff;
			}
			if (measurementBounds.getX()+measurementBounds.getWidth()>newBounds.getX()
					+newBounds.getWidth())
			{
				double diff=
					measurementBounds.getX()+measurementBounds.getWidth()-newBounds.getX()
								+newBounds.getWidth();
				newBounds.width=newBounds.width+diff;
			}
			if (measurementBounds.getY()+measurementBounds.getHeight()>newBounds.getY()
					+newBounds.getHeight())
			{
				double diff=
					measurementBounds.getY()+measurementBounds.getHeight()-newBounds.getY()
								+newBounds.getHeight();
				newBounds.height=newBounds.height+diff;
			}
		}
		return newBounds;
	}
	
	/**
	 * Add units to the string.
	 *  
	 * @param str see above.
	 * @return returns the string with the units added. 
	 */
	public String addUnits(String str)
	{
		if (shape==null) return str;
		if (units.isInMicrons()) return str+UIUtilities.MICRONS_SYMBOL
				+UIUtilities.SQUARED_SYMBOL;
		return str+UIUtilities.PIXELS_SYMBOL+UIUtilities.SQUARED_SYMBOL;
	}
	
	/**
	 * Calculate the area of the figure. 
	 * @return see above.
	 */
	public double getArea()
	{
		
		return (getMeasurementHeight()/2)*(getMeasurementWidth()/2)*Math.PI;
	}
	
	/**
	 * Calculate the perimeter of the figure. 
	 * @return see above.
	 */
	public double getPerimeter()
	{
		if (getMeasurementWidth()==getMeasurementHeight()) return getMeasurementWidth()
				*2*Math.PI;
		
		double a=Math.max(getMeasurementWidth(), getMeasurementHeight());
		double b=Math.min(getMeasurementWidth(), getMeasurementHeight());
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
	public ROI getROI()
	{
		return roi;
	}
	
	
	
	/**
	 * Implemented as specified by the {@link ROIFigure} interface.
	 * @see ROIFigure#getROIShape()
	 */
	public ROIShape getROIShape()
	{
		return shape;
	}
	
	
	
	/**
	 * Implemented as specified by the {@link ROIFigure} interface.
	 * @see ROIFigure#setROI(ROI)
	 */
	public void setROI(ROI roi)
	{
		this.roi=roi;
	}
	
	
	
	/**
	 * Implemented as specified by the {@link ROIFigure} interface.
	 * @see ROIFigure#setROIShape(ROIShape)
	 */
	public void setROIShape(ROIShape shape)
	{
		this.shape=shape;
	}
	
	
	
	/**
	 * Implemented as specified by the {@link ROIFigure} interface.
	 * @see ROIFigure#getType()
	 */
	public void calculateMeasurements()
	{
		if (shape==null) return;
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
	public String getType()
	{
		return FigureUtil.ELLIPSE_TYPE;
	}
	
	
	
	/**
	 * Implemented as specified by the {@link ROIFigure} interface.
	 * @see ROIFigure#setMeasurementUnits(MeasurementUnits)
	 */
	public void setMeasurementUnits(MeasurementUnits units)
	{
		this.units=units;
	}
	
	/**
	 * Implemented as specified by the {@link ROIFigure} interface.
	 * @see ROIFigure#getPoints()
	 */
	public List<Point> getPoints()
	{
		Rectangle r = this.getTransformedShape().getBounds();
		List<Point> vector = new ArrayList<Point>(r.height*r.width);
		int xEnd = r.x+r.width, yEnd = r.y+r.height;
		int x, y;
		for (y=r.y; y<yEnd; ++y)
			for (x=r.x; x<xEnd; ++x)
				if (this.getTransformedShape().contains(x, y)) 
					vector.add(new Point(x, y));
		return vector; 
	}
	
	public void setStatus(int status) { this.status = status; }
	
	public int getStatus() { return status; 
	
	}
}
