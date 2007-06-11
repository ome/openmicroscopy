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
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

//Third-party libraries
import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.BezierFigure;

//Application-internal dependencies
import static org.openmicroscopy.shoola.util.roi.figures.DrawingAttributes.MEASUREMENTTEXT_COLOUR;
import static org.openmicroscopy.shoola.util.roi.figures.DrawingAttributes.SHOWMEASUREMENT;
import static org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys.ENDPOINTX;
import static org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys.ENDPOINTY;
import static org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys.INMICRONS;
import static org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys.AREA;
import static org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys.MICRONSPIXELX;
import static org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys.MICRONSPIXELY;
import static org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys.PERIMETER;
import static org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys.LENGTH;
import static org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys.CENTREX;
import static org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys.CENTREY;
import static org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys.POINTARRAYX;
import static org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys.POINTARRAYY;
import static org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys.STARTPOINTX;
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
public class MeasureBezierFigure 
	extends BezierFigure
	implements ROIFigure
{

	private ArrayList<Double>			pointArrayX;
	private ArrayList<Double>			pointArrayY;
	private ArrayList<Double>			lengthArray;
	
	private	Rectangle2D bounds;
	private ROI			roi;
	private ROIShape 	shape;
	
	public MeasureBezierFigure()
	{
		super();
		shape = null;
		roi = null;
		pointArrayX = new ArrayList<Double>();
		pointArrayY = new ArrayList<Double>();
		lengthArray = new ArrayList<Double>();
	}
	
	public MeasureBezierFigure(boolean closed)
	{
		super(closed);
		pointArrayX = new ArrayList<Double>();
		pointArrayY = new ArrayList<Double>();
		lengthArray = new ArrayList<Double>();
	}
	
	public void draw(Graphics2D g)
	{
		super.draw(g);
		if(SHOWMEASUREMENT.get(this))
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
				g.setColor(MEASUREMENTTEXT_COLOUR.get(this));
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
					g.setColor(MEASUREMENTTEXT_COLOUR.get(this));
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
		return str + "\u00B0";
	}
	
	public String addLineUnits(String str)
	{
		if(shape==null)
			return str;
		
		if(INMICRONS.get(shape))
			return str+"\u00B5m";
		else
			return str+"px";
	}
	
	public String addAreaUnits(String str)
	{
		if(shape==null)
			return str;
		if(INMICRONS.get(shape))
			return str+"\u00B5m\u00B2";
		else
			return str+"px\u00B2";
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
		if(INMICRONS.get(shape))
		{
			return new Point2D.Double(	pt.getX()*MICRONSPIXELX.get(shape), 
										pt.getY()*MICRONSPIXELY.get(shape));
		}
		else
			return pt;
	}
	
	public double getLength()
	{
		double length = 0;
		for(int i = 0 ; i < getPointCount()-1 ; i++)
		{
			Point2D p0 = getPt(i);
			Point2D p1 = getPt(i+1);
			length += p0.distance(p1);
		}
		return length;
	}
	
	public Point2D getCentre()
	{
		if(INMICRONS.get(shape))
		{
			Point2D.Double pt1 =  path.getCenter();
			pt1.setLocation(pt1.getX()*MICRONSPIXELX.get(shape), pt1.getY()*MICRONSPIXELY.get(shape));
			return pt1;
		}
		else
			return path.getCenter();
	}
	
	public double getArea()
	{
		double area = 0;
		Point2D centre = getCentre();
	
		for(int i = 0 ; i < path.size() ; i++)
		{
			Point2D p0 = getPt(i);
			Point2D p1;
			if(i==path.size()-1)
				p1 = getPt(0);
			else
				p1 = getPt(i+1);
		
			p0.setLocation(p0.getX()-centre.getX(), p0.getY()-centre.getY());
			p1.setLocation(p1.getX()-centre.getX(), p1.getY()-centre.getY());
			area += (p0.getX()*p1.getY()-p1.getX()*p0.getY());
		}
		return Math.abs(area/2);
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
		//pointArrayX = new ArrayList<Double>();
		//pointArrayY = new ArrayList<Double>();
		
		pointArrayX.clear();
		pointArrayY.clear();
		Point2D.Double pt;
		for (int i = 0 ; i < path.size(); i++)
		{
			pt = getPt(i);
			pointArrayX.add(pt.getX());
			pointArrayY.add(pt.getY());
		}
		POINTARRAYX.set(shape, pointArrayX);
		POINTARRAYY.set(shape, pointArrayY);
		if (CLOSED.get(this))
		{
			AREA.set(shape,getArea());
			PERIMETER.set(shape, getLength());
			CENTREX.set(shape, getCentre().getX());
			CENTREY.set(shape, getCentre().getY());
		}
		else
		{
			//ArrayList<Double> lengthArray = new ArrayList<Double>();
			lengthArray.add(getLength());
			
			LENGTH.set(shape, lengthArray);
			CENTREX.set(shape, getCentre().getX());
			CENTREY.set(shape, getCentre().getY());
			STARTPOINTX.set(shape, getPt(0).getX());
			STARTPOINTX.set(shape, getPt(0).getY());
			ENDPOINTX.set(shape, getPt(path.size()-1).getX());
			ENDPOINTY.set(shape, getPt(path.size()-1).getY());
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

}


