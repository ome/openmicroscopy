/*
 * org.openmicroscopy.shoola.util.roi.figures.MeasureBezierFigure 
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
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

//Third-party libraries
import org.jhotdraw.draw.AttributeKeys;

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.measurement.util.MeasurementAttributes;
import org.openmicroscopy.shoola.util.math.geom2D.PlanePoint2D;
import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.util.MeasurementUnits;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.drawingtools.attributes.DrawingAttributes;
import org.openmicroscopy.shoola.util.ui.drawingtools.figures.BezierTextFigure;

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
public class MeasureBezierFigure 
	extends BezierTextFigure
	implements ROIFigure
{

	private ArrayList<Double>			pointArrayX;
	private ArrayList<Double>			pointArrayY;
	private ArrayList<Double>			lengthArray;
	
	private	Rectangle2D bounds;
	/** The ROI containing the ROIFigure which in turn contains this Figure. */
	protected 	ROI					roi;

	/** The ROIFigure contains this Figure. */
	protected 	ROIShape 			shape;
	
	/** The Measurement units, and values of the image. */
	private MeasurementUnits 		units;
		
	public MeasureBezierFigure()
	{
		super("Text");
		shape = null;
		roi = null;
		pointArrayX = new ArrayList<Double>();
		pointArrayY = new ArrayList<Double>();
		lengthArray = new ArrayList<Double>();
	}
	
	public MeasureBezierFigure(boolean closed)
	{
		super("Text", closed);
		pointArrayX = new ArrayList<Double>();
		pointArrayY = new ArrayList<Double>();
		lengthArray = new ArrayList<Double>();
	}
	
	public MeasureBezierFigure(String text)
	{
		super(text, true);
		pointArrayX = new ArrayList<Double>();
		pointArrayY = new ArrayList<Double>();
		lengthArray = new ArrayList<Double>();
	}
	
	public MeasureBezierFigure(String text, boolean closed)
	{
		super(text, closed);
		pointArrayX = new ArrayList<Double>();
		pointArrayY = new ArrayList<Double>();
		lengthArray = new ArrayList<Double>();
	}
	
	public void draw(Graphics2D g)
	{
		super.draw(g);
		if(MeasurementAttributes.SHOWMEASUREMENT.get(this))
		{
			if(CLOSED.get(this))
			{
				NumberFormat formatter = new DecimalFormat("###.#");
				String polygonArea = formatter.format(getArea());
				polygonArea = addAreaUnits(polygonArea);
				double sz = ((Double)this.getAttribute(AttributeKeys.FONT_SIZE));
				g.setFont(new Font("Arial",Font.PLAIN, (int)sz));
				bounds = g.getFontMetrics().getStringBounds(polygonArea, g);
				bounds = new Rectangle2D.Double(this.getBounds().getCenterX()-bounds.getWidth()/2,
					this.getBounds().getCenterY()+bounds.getHeight()/2,
					bounds.getWidth(), bounds.getHeight());
				g.setColor(MeasurementAttributes.MEASUREMENTTEXT_COLOUR.get(this));
				g.drawString(polygonArea, (int)bounds.getX(), (int)bounds.getY()); 
			}
			else
			{
				NumberFormat formatter = new DecimalFormat("###.#");
				String polygonLength = formatter.format(getLength());
				polygonLength = addLineUnits(polygonLength);
				double sz = ((Double)this.getAttribute(AttributeKeys.FONT_SIZE));
				g.setFont(new Font("Arial",Font.PLAIN, (int)sz));
				bounds = g.getFontMetrics().getStringBounds(polygonLength, g);
				
				if(getPointCount() > 1)
				{
					int midPoint = this.getPointCount()/2-1;
					if(midPoint<0)
						midPoint = 0;
					Point2D p0 = getPoint(midPoint);
					Point2D p1 = getPoint(midPoint+1);
					double x, y;
					x = Math.min(p0.getX(),p1.getX())+Math.abs(p0.getX()-p1.getX());
					y = Math.min(p0.getY(),p1.getY())+Math.abs(p0.getY()-p1.getY());
					bounds = new Rectangle2D.Double(x-bounds.getWidth()/2,
							y+bounds.getHeight()/2,
							bounds.getWidth(), bounds.getHeight());
					g.setColor(MeasurementAttributes.MEASUREMENTTEXT_COLOUR.get(this));
					g.drawString(polygonLength, (int)path.getCenter().getX(), (int)path.getCenter().getY());
				}
			}
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
	
	
	public String addDegrees(String str)
	{
		return str + UIUtilities.DEGREES_SYMBOL;
	}
	
	public String addLineUnits(String str)
	{
		if (shape == null) return str;
		
		if (units.isInMicrons()) return str+UIUtilities.MICRONS_SYMBOL;
		return str+UIUtilities.PIXELS_SYMBOL;
	}
	
	public String addAreaUnits(String str)
	{
		if (shape == null) return str;
		if (units.isInMicrons())
			return str+UIUtilities.MICRONS_SYMBOL+UIUtilities.SQUARED_SYMBOL;
		return str+UIUtilities.PIXELS_SYMBOL+UIUtilities.SQUARED_SYMBOL;
	}
	
	/**
	 * Get the point i in pixels or microns depending on the units used.
	 * 
	 * @param i node
	 * @return see above.
	 */
	private Point2D.Double getPt(int i)
	{
		Point2D.Double pt = getPoint(i); 
			//new Point2D.Double(path.get(i).x[0],path.get(i).y[0]);
		if (units.isInMicrons())
			return new Point2D.Double(	pt.getX()*units.getMicronsPixelX(), 
										pt.getY()*units.getMicronsPixelY());
		return pt;
	}
	
	public double getLength()
	{
		double length = 0;
		Point2D p0, p1;
		for (int i = 0 ; i < getPointCount()-1 ; i++)
		{
			p0 = getPt(i);
			p1 = getPt(i+1);
			length += p0.distance(p1);
		}
		return length;
	}
	
	public Point2D getCentre()
	{
		if (units.isInMicrons())
		{
			Point2D.Double pt1 =  path.getCenter();
			pt1.setLocation(pt1.getX()*units.getMicronsPixelX(), pt1.getY()*units.getMicronsPixelY());
			return pt1;
		}
		return path.getCenter();
	}
	
	public double getArea()
	{
		double area = 0;
		Point2D centre = getCentre();
		Point2D p0, p1;
		for (int i = 0 ; i < path.size() ; i++)
		{
			p0 = getPt(i);
			if (i == path.size()-1) p1 = getPt(0);
			else p1 = getPt(i+1);
		
			p0.setLocation(p0.getX()-centre.getX(), p0.getY()-centre.getY());
			p1.setLocation(p1.getX()-centre.getX(), p1.getY()-centre.getY());
			area += (p0.getX()*p1.getY()-p1.getX()*p0.getY());
		}
		return Math.abs(area/2);
	}

	public void measureBasicRemoveNode(int index)
	{
		this.basicRemoveNode(index);
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
		if (shape==null) return;
		
		pointArrayX.clear();
		pointArrayY.clear();
		Point2D.Double pt;
		for (int i = 0 ; i < path.size(); i++)
		{
			pt = getPt(i);
			pointArrayX.add(pt.getX());
			pointArrayY.add(pt.getY());
		}
		AnnotationKeys.POINTARRAYX.set(shape, pointArrayX);
		AnnotationKeys.POINTARRAYY.set(shape, pointArrayY);
		if (CLOSED.get(this))
		{
			AnnotationKeys.AREA.set(shape,getArea());
			AnnotationKeys.PERIMETER.set(shape, getLength());
			AnnotationKeys.CENTREX.set(shape, getCentre().getX());
			AnnotationKeys.CENTREY.set(shape, getCentre().getY());
		}
		else
		{
			lengthArray.add(getLength());
			
			AnnotationKeys.LENGTH.set(shape, lengthArray);
			AnnotationKeys.CENTREX.set(shape, getCentre().getX());
			AnnotationKeys.CENTREY.set(shape, getCentre().getY());
			AnnotationKeys.STARTPOINTX.set(shape, getPt(0).getX());
			AnnotationKeys.STARTPOINTX.set(shape, getPt(0).getY());
			AnnotationKeys.ENDPOINTX.set(shape, getPt(path.size()-1).getX());
			AnnotationKeys.ENDPOINTY.set(shape, getPt(path.size()-1).getY());
		}
	}
	
	/**
	 * Implemented as specified by the {@link ROIFigure} interface.
	 * @see ROIFigure#getType()
	 */
	public String getType()
	{
		if (CLOSED.get(this)) return ROIFigure.POLYGON_TYPE;
		return ROIFigure.SCRIBBLE_TYPE;
	}

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
	public PlanePoint2D[] getPoints()
	{
		if(isClosed())
			return getAreaPoints();
		else
			return getLinePoints();
	}
	
	private PlanePoint2D[] getAreaPoints()
	{
		Rectangle r = path.getBounds();
		double iX = Math.floor(r.getX());
		double iY = Math.floor(r.getY());
		ArrayList<PlanePoint2D> vector = new ArrayList<PlanePoint2D>();
		path.toPolygonArray();
		Point2D point = new Point2D.Double(0,0);
		for(int x = 0 ; x < Math.ceil(r.getWidth()); x++)
		{
			for( int y = 0 ; y < Math.ceil(r.getHeight()) ; y++)
			{
				point.setLocation(iX+x, iY+y);
				if(path.contains(point))
					vector.add(new PlanePoint2D(point.getX(), point.getY()));
			}
		}
		return (PlanePoint2D[])vector.toArray(new PlanePoint2D[vector.size()]);

	}
	
	private PlanePoint2D[] getLinePoints()
	{
		Rectangle r = path.getBounds();
		ArrayList<PlanePoint2D> vector = new ArrayList<PlanePoint2D>();
		for(int i = 0 ; i < getNodeCount()-1; i++)
		{
			Point2D pt1 = getPoint(i);
			Point2D pt2 = getPoint(i+1);
			Line2D line = new Line2D.Double(pt1, pt2);
			iterateLine(line, vector);
		}
		return (PlanePoint2D[])vector.toArray(new PlanePoint2D[vector.size()]);
	}
		
	private void iterateLine(Line2D line, ArrayList<PlanePoint2D> vector)
	{
		Point2D start = line.getP1();
		Point2D end = line.getP2();
		Point2D m = new Point2D.Double(end.getX()-start.getX(), end.getY()-start.getY());
		double lengthM = (Math.sqrt(m.getX()*m.getX()+m.getY()*m.getY()));
		Point2D mNorm = new Point2D.Double(m.getX()/lengthM,m.getY()/lengthM);
		LinkedHashMap<Point2D, Boolean> map = new LinkedHashMap<Point2D, Boolean>();
		for(double i = 0 ; i < lengthM ; i+=0.1)
		{
			Point2D pt = new Point2D.Double(start.getX()+i*mNorm.getX(),
				start.getY()+i*mNorm.getY());
			Point2D quantisedPoint = new Point2D.Double(Math.floor(pt.getX()), 
				Math.floor(pt.getY()));
			if(!map.containsKey(quantisedPoint))
				map.put(quantisedPoint, new Boolean(true));
		}
		Iterator<Point2D> i = map.keySet().iterator();
		while(i.hasNext())
		{
			Point2D p  = i.next();
			vector.add(new PlanePoint2D(p.getX(), p.getY()));
		}
		
	}
	
}


