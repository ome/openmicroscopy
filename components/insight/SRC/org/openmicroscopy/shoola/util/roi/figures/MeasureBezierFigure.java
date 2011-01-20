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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

//Third-party libraries
import org.jhotdraw.draw.AbstractAttributedFigure;
import org.jhotdraw.draw.Handle;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys;
import org.openmicroscopy.shoola.util.roi.model.annotation.MeasurementAttributes;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.util.MeasurementUnits;
import org.openmicroscopy.shoola.util.roi.util.FigureSelectionHandle;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.drawingtools.figures.BezierTextFigure;
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
public class MeasureBezierFigure 
	extends BezierTextFigure
	implements ROIFigure
{
	/** Is this figure read only. */
	private boolean readOnly;

	/** The list of X coords of the nodes on the line. */
	private List<Double>			pointArrayX;

	/** The list of Y coords of the nodes on the line. */
	private List<Double>			pointArrayY;
	
	/** The list of lengths of sections on the line. */
	private List<Double>			lengthArray;
	
	/** The bounds of the bezier figure. */
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
	 * Returns the points(pixels) on the polyline return this as an array.
	 * 
	 * @return See above.
	 */
	private List<Point> getLinePoints()
	{
		List<Point> vector = new ArrayList<Point>();
		Point2D pt1, pt2;
		for (int i = 0 ; i < getNodeCount()-1; i++)
		{
			pt1 = getPoint(i);
			pt2 = getPoint(i+1);
			iterateLine(new Line2D.Double(pt1, pt2), vector);
		}
		return vector;
	}
		
	/**
	 * Iterates the line to get the points under it.
	 * 
	 * @param line the line to iterate.
	 * @param vector the vector to add the point to.
	 */
	private void iterateLine(Line2D line, List<Point> vector)
	{
		Point2D start = line.getP1();
		Point2D end = line.getP2();
		Point2D m = new Point2D.Double(end.getX()-start.getX(),
				end.getY()-start.getY());
		double lengthM = (Math.sqrt(m.getX()*m.getX()+m.getY()*m.getY()));
		Point2D mNorm = new Point2D.Double(m.getX()/lengthM,m.getY()/lengthM);
		LinkedHashMap<Point2D, Boolean> 
		map = new LinkedHashMap<Point2D, Boolean>();
		Point2D pt;
		Point2D quantisedPoint;
		for (double i = 0 ; i < lengthM ; i+=0.1)
		{
			pt = new Point2D.Double(start.getX()+i*mNorm.getX(),
				start.getY()+i*mNorm.getY());
			quantisedPoint = new Point2D.Double(Math.floor(pt.getX()), 
				Math.floor(pt.getY()));
			if (!map.containsKey(quantisedPoint))
				map.put(quantisedPoint, Boolean.TRUE);
		}
		Iterator<Point2D> i = map.keySet().iterator();
		while (i.hasNext())
		{
			pt  = i.next();
			vector.add(new Point((int) pt.getX(), (int) pt.getY()));
		}
	}
	
	/** Creates an instance of the bezier figure. */
	public MeasureBezierFigure()
	{
		this(false);
	}
	
	/**
	 * Creates an instance of the bezier figure.
	 * 
	 * @param closed Pass <code>true</code> if the figure is a polygon,
	 * 				 <code>false</code> if it is a polyline.
	 */
	public MeasureBezierFigure(boolean closed)
	{
		this(ROIFigure.DEFAULT_TEXT, closed);
	}
	
	/**
	 * Creates an instance of the bezier figure (closed).
	 * 
	 * @param text The string displayed in the figure. 
	 */
	public MeasureBezierFigure(String text)
	{
		this(text, false, false);
	}
	
	/**
	 * Creates an instance of the Bezier figure (closed).
	 * 
	 * @param text The string displayed in the figure. 
	 * @param closed Pass <code>true</code> if the figure is a polygon,
	 * 				 <code>false</code> if it is a polyline.
	 */
	public MeasureBezierFigure(String text, boolean closed)
	{
		this(text, closed, false);
	}
	
	/**
	 * Creates an instance of the bezier figure.
	 * 
	 * @param text 	 The string displayed in the figure. 
	 * @param closed Pass <code>true</code> if the figure is a polygon,
	 * 				 <code>false</code> if it is a polyline.
	 * @param readOnly The figure is read only.
	 */
	public MeasureBezierFigure(String text, boolean closed, boolean readOnly)
	{
		super(text, closed);
		this.readOnly = readOnly;
		pointArrayX = new ArrayList<Double>();
		pointArrayY = new ArrayList<Double>();
		lengthArray = new ArrayList<Double>();
		status = IDLE;
		setReadOnly(readOnly);
	}

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
			if (isClosed())
			{
				NumberFormat formatter = new DecimalFormat("###.#");
				String polygonArea = formatter.format(getArea());
				polygonArea = addAreaUnits(polygonArea);
				double sz = ((Double) this.getAttribute(
							MeasurementAttributes.FONT_SIZE));
				g.setFont(new Font("Arial",Font.PLAIN, (int) sz));
				bounds = g.getFontMetrics().getStringBounds(polygonArea, g);
				bounds = new Rectangle2D.Double(
						this.getBounds().getCenterX()-bounds.getWidth()/2,
					this.getBounds().getCenterY()+bounds.getHeight()/2,
					bounds.getWidth(), bounds.getHeight());
				if (MeasurementAttributes.SHOWMEASUREMENT.get(this))
				{
					g.setColor(
							MeasurementAttributes.MEASUREMENTTEXT_COLOUR.get(this));
					g.drawString(polygonArea, (int) bounds.getX(), 
							(int) bounds.getY());
				}
				if (MeasurementAttributes.SHOWID.get(this))
				{
					g.setColor(this.getTextColor());
					g.drawString(this.getROI().getID()+"", (int) bounds.getX(), 
							(int) bounds.getY());
				}
			}
			else
			{
				NumberFormat formatter = new DecimalFormat("###.#");
				String polygonLength = formatter.format(getLength());
				polygonLength = addLineUnits(polygonLength);
				double sz = ((Double) 
						this.getAttribute(MeasurementAttributes.FONT_SIZE));
				g.setFont(new Font("Arial", Font.PLAIN, (int) sz));
				bounds = g.getFontMetrics().getStringBounds(polygonLength, g);
				
				if (super.getNodeCount() > 1)
				{
					int midPoint = this.getNodeCount()/2-1;
					if(midPoint<0)
						midPoint = 0;
					Point2D p0 = getPoint(midPoint);
					Point2D p1 = getPoint(midPoint+1);
					double x, y;
					x = Math.min(p0.getX(),p1.getX())+Math.abs(p0.getX()
							-p1.getX());
					y = Math.min(p0.getY(),p1.getY())+Math.abs(p0.getY()
							-p1.getY());
					bounds = new Rectangle2D.Double(x-bounds.getWidth()/2,
							y+bounds.getHeight()/2,
							bounds.getWidth(), bounds.getHeight());
					if (MeasurementAttributes.SHOWMEASUREMENT.get(this))
					{
						g.setColor(
						MeasurementAttributes.MEASUREMENTTEXT_COLOUR.get(this));
						g.drawString(
								this.getROI().getID()+"", 
								(int) path.getCenter().getX(), 
								(int) path.getCenter().getY());
					}
					if (MeasurementAttributes.SHOWID.get(this))
					{
						g.setColor(this.getTextColor());
						g.drawString(this.getROI().getID()+"", 
								(int) path.getCenter().getX(), 
								(int) path.getCenter().getY());
					}
				}
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
		Rectangle2D.Double newBounds = super.getDrawingArea();
		if (bounds == null) return newBounds;
		double diff;
		if (newBounds.getX() > bounds.getX())
		{
			diff = newBounds.x-bounds.getX();
			newBounds.x = bounds.getX();
			newBounds.width = newBounds.width+diff;
		}
		if (newBounds.getY() > bounds.getY())
		{
			diff = newBounds.y-bounds.getY();
			newBounds.y = bounds.getY();
			newBounds.height = newBounds.height+diff;
		}
		if (bounds.getX()+bounds.getWidth() > 
			newBounds.getX()+newBounds.getWidth())
		{
			diff = bounds.getX()+bounds.getWidth()-newBounds.getX()
				+newBounds.getWidth();
			newBounds.width = newBounds.width+diff;
		}
		if (bounds.getY()+bounds.getHeight() > 
			newBounds.getY()+newBounds.getHeight())
		{
			diff = bounds.getY()+bounds.getHeight()-newBounds.getY()
				+newBounds.getHeight();
			newBounds.height = newBounds.height+diff;
		}
		return newBounds;
	}
	
	/**
	 * Add degrees to the measurements. 
	 * @param str the measurement.
	 * @return see above.
	 */
	public String addDegrees(String str)
	{
		return str + UIUtilities.DEGREES_SYMBOL;
	}
	
	/**
	 * Add length unit, (pixels, microns) to the measurements. 
	 * @param str the measurement.
	 * @return see above.
	 */
	public String addLineUnits(String str)
	{
		if (shape == null) return str;
		
		if (units.isInMicrons()) return str+UIUtilities.MICRONS_SYMBOL;
		return str+UIUtilities.PIXELS_SYMBOL;
	}
	
	/**
	 * Add area ^2 to the measurements. 
	 * @param str the measurement.
	 * @return see above.
	 */
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
		Point2D.Double pt = getNode(i).getControlPoint(0); 
		if (units.isInMicrons())
			return new Point2D.Double(	pt.getX()*units.getMicronsPixelX(), 
										pt.getY()*units.getMicronsPixelY());
		return pt;
	}
	
	/**
	 * Calculate the length of the line.
	 * @return see above.
	 */
	public double getLength()
	{
		double length = 0;
		Point2D p0, p1;
		for (int i = 0 ; i < path.size()-1 ; i++)
		{
			p0 = path.get(i).getControlPoint(0);
			p1 = path.get(i+1).getControlPoint(0);
			length += p0.distance(p1);
		}
		return length;
	}
	
	/** 
	 * Get the number of points.
	 * @return see above. 
	 */
	public int getPointCount()
	{
		return this.getPoints().size();
	}
	
	/**
	 * Calculate the centre of the object (in pixels, or microns). 
	 * @return see above.
	 */
	public Point2D getCentre()
	{
		if (units.isInMicrons())
		{
			Point2D.Double pt1 =  path.getCenter();
			pt1.setLocation(pt1.getX()*units.getMicronsPixelX(), 
					pt1.getY()*units.getMicronsPixelY());
			return pt1;
		}
		return path.getCenter();
	}
	
	/**
	 * Get the Area of the object, in Pixels or microns.
	 * @return see above.
	 */
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

	/** 
	 * Remove a node from the bezier figure.
	 * @param index node to remove.
	 */
	public void measureBasicRemoveNode(int index)
	{
		this.removeNode(index);
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
		if (getNodeCount() < 2) return;
		pointArrayX.clear();
		pointArrayY.clear();

		for (int i = 0 ; i < path.size(); i++)
		{
			pointArrayY.add(path.get(i).getControlPoint(0).getY());
		}
		AnnotationKeys.POINTARRAYX.set(shape, pointArrayX);
		AnnotationKeys.POINTARRAYY.set(shape, pointArrayY);
		if (super.isClosed())
		{
			AnnotationKeys.AREA.set(shape,getArea());
			AnnotationKeys.PERIMETER.set(shape, getLength());
			AnnotationKeys.CENTREX.set(shape, getCentre().getX());
			AnnotationKeys.CENTREY.set(shape, getCentre().getY());
		}
		else
		{
			lengthArray.clear();
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
	 * Overridden to return a copy of the figure
	 * @see BezierTextFigure#clone()
	 */
	public MeasureBezierFigure clone()
	{
		return (MeasureBezierFigure) super.clone();
	}
	
	/**
	 * Returns the points(pixels) in the polygon return this as an array.
	 * 
	 * @return See above.
	 */
	private List<Point> getAreaPoints()
	{
		Rectangle r = path.getBounds();
		double iX = Math.floor(r.getX());
		double iY = Math.floor(r.getY());
		List<Point> vector = new ArrayList<Point>();
		path.toPolygonArray();
		Point2D point = new Point2D.Double(0,0);
		for (int x = 0 ; x < Math.ceil(r.getWidth()); x++)
		{
			for ( int y = 0 ; y < Math.ceil(r.getHeight()) ; y++)
			{
				point.setLocation(iX+x, iY+y);
				if (path.contains(point))
					vector.add(new Point((int) point.getX(), (int)  
							point.getY()));
			}
		}
		return vector;
	}

	
	/**
	 * Implemented as specified by the {@link ROIFigure} interface.
	 * @see ROIFigure#getType()
	 */
	public String getType()
	{
		if (isClosed()) return FigureUtil.POLYGON_TYPE;
		return FigureUtil.SCRIBBLE_TYPE;
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
	public List<Point> getPoints()
	{
		if (isClosed()) return getAreaPoints();
		return getLinePoints();
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
	 * Overridden to stop updating shape if read only
	 * @see AbstractAttributedFigure#transform(AffineTransform)
	 */
	public void transform(AffineTransform tx)
	{
		if (!readOnly)
			super.transform(tx);
	}
	
	/**
	 * Overridden to stop updating shape if read only.
	 * @see AbstractAttributedFigure#setBounds(Double, Double)
	 */
	public void setBounds(Point2D.Double anchor, Point2D.Double lead) 
	{
		if (!readOnly)
			super.setBounds(anchor, lead);
	}
	
	/**
	 * Overridden to return the correct handles.
	 * @see AbstractAttributedFigure#createHandles(int)
	 */
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
}


