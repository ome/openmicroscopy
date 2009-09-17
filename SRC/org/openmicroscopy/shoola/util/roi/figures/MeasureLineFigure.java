/*
 * org.openmicroscopy.shoola.util.roi.figures.MeasureLineFigure 
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
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys;
import org.openmicroscopy.shoola.util.roi.model.annotation.MeasurementAttributes;
import org.openmicroscopy.shoola.util.roi.model.util.MeasurementUnits;
import org.openmicroscopy.shoola.util.roi.util.FigureSelectionHandle;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.drawingtools.figures.FigureUtil;
import org.openmicroscopy.shoola.util.ui.drawingtools.figures.LineTextFigure;

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
public class MeasureLineFigure
	extends LineTextFigure
	implements ROIFigure
{
	
	/** Is this figure read only. */
	private boolean readOnly;

	/** The bounds of the bezier figure. */
	private List<Rectangle2D> 		boundsArray;
	
	/** The list of lengths of sections on the line. */
	private List<Double> 				lengthArray;
	
	/** The list of angles of sections on the line. */
	private List<Double> 				angleArray;

	/** The list of X coords of the nodes on the line. */
	private List<Double>			pointArrayX;
	
	/** The list of Y coords of the nodes on the line. */
	private List<Double>			pointArrayY;
	
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
	 * Returns the point i in pixels or microns depending on the units used.
	 * 
	 * @param i node
	 * @return See above.
	 */
	private Point2D.Double getPt(int i)
	{
		if (units.isInMicrons())
		{
			Point2D.Double pt = getPoint(i);
			return new Point2D.Double(pt.getX()*units.getMicronsPixelX(), 
					pt.getY()*units.getMicronsPixelY());
		}
		return getPoint(i);
	}
	
	/** Creates a new instance. */
	public MeasureLineFigure()
	{
		this("text", false);
	}


	/** Creates a new instance. 
	 * @param readOnly the figure is read only.
	 */
	public MeasureLineFigure(boolean readOnly)
	{
		this("text", readOnly);
	}
	
	/**
	 * Create instance of the line figure.
	 * @param text The text to add to the figure.
	 * @param readOnly the figure is read only.
	 */
	public MeasureLineFigure(String text, boolean readOnly)
	{
		super(text);
		boundsArray = new ArrayList<Rectangle2D>();
		lengthArray = new ArrayList<Double>();
		angleArray = new ArrayList<Double>();
		pointArrayX = new ArrayList<Double>();
		pointArrayY = new ArrayList<Double>();
		shape = null;
		roi = null;
		status = IDLE;
		setReadOnly(readOnly);
	}
	
	/**
     * Draw the figure on the graphics context.
     * @param g the graphics context.
     */
	public void draw(Graphics2D g)
	{
		super.draw(g);
		boundsArray.clear();
		lengthArray.clear();
		angleArray.clear();
		if(MeasurementAttributes.SHOWMEASUREMENT.get(this))
		{
			if(getPointCount()==2)
			{
				NumberFormat formatter = new DecimalFormat("###.#");
				double angle = getAngle(0, 1);
				if(angle>90)
					angle = Math.abs(angle-180);
				angleArray.add(angle);
				String lineAngle = formatter.format(angle);
				lineAngle = addDegrees(lineAngle);
				double sz = ((Double)this.getAttribute(MeasurementAttributes.FONT_SIZE));
				g.setFont(new Font("Arial",Font.PLAIN, (int)sz));
				Rectangle2D rect = g.getFontMetrics().getStringBounds(lineAngle, g);
				Point2D.Double lengthPoint = getLengthPosition(0, 1);
				Rectangle2D bounds = new Rectangle2D.Double(lengthPoint.x,
						lengthPoint.y+rect.getHeight()*2, rect.getWidth(),
						rect.getHeight());
				g.setColor(MeasurementAttributes.MEASUREMENTTEXT_COLOUR.get(this));
				g.drawString(lineAngle, (int)bounds.getX(), (int)bounds.getY());
				boundsArray.add(bounds);
			}
			for( int x = 1 ; x < this.getPointCount()-1; x++)
			{
				NumberFormat formatter = new DecimalFormat("###.#");
				double angle = getAngle(x-1, x, x+1);
				angleArray.add(angle);
				String lineAngle = formatter.format(angle);
				lineAngle = addDegrees(lineAngle);
				double sz = ((Double)this.getAttribute(MeasurementAttributes.FONT_SIZE));
				g.setFont(new Font("Arial",Font.PLAIN, (int)sz));
				Rectangle2D rect = g.getFontMetrics().getStringBounds(lineAngle, g);
				Rectangle2D bounds = new Rectangle2D.Double(getPoint(x).x,
						getPoint(x).y, rect.getWidth(), rect.getHeight());
				g.setColor(MeasurementAttributes.MEASUREMENTTEXT_COLOUR.get(this));
				g.drawString(lineAngle, (int)bounds.getX(), (int)bounds.getY());
				boundsArray.add(bounds);
			}
			for( int x = 1 ; x < this.getPointCount(); x++)
			{
				NumberFormat formatter = new DecimalFormat("###.#");
				double length = getLength(x-1, x);
				lengthArray.add(length);
				String lineLength = formatter.format(length);
				lineLength = addUnits(lineLength);
				double sz = ((Double)this.getAttribute(MeasurementAttributes.FONT_SIZE));
				g.setFont(new Font("Arial",Font.PLAIN, (int)sz));
				Point2D.Double lengthPoint = getLengthPosition(x-1, x);
				Rectangle2D rect = g.getFontMetrics().getStringBounds(lineLength, g);
				Rectangle2D bounds = new Rectangle2D.Double(lengthPoint.x-15, 
						lengthPoint.y-15,rect.getWidth()+30, 
						rect.getHeight()+30);
				g.setColor(MeasurementAttributes.MEASUREMENTTEXT_COLOUR.get(this));
				g.drawString(lineLength, (int)lengthPoint.x, (int)lengthPoint.y);
				boundsArray.add(bounds);
			}
		}
		if(MeasurementAttributes.SHOWID.get(this))
		{
			Rectangle2D 		bounds;
			
			g.setColor(this.getTextColor());
			bounds = g.getFontMetrics().getStringBounds(getROI().getID()+"", g);
			bounds = new Rectangle2D.Double(
						path.getCenter().getX()-bounds.getWidth()/2,
						path.getCenter().getY()+bounds.getHeight()/2,
					bounds.getWidth(), bounds.getHeight());
			g.drawString(this.getROI().getID()+"", (int)bounds.getX(), (int)bounds.getY());
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
	 * Get the length array. These are the lengths of each segment of the line. 
	 * @return see above.
	 */
	public List<Double> getLengthArray()
	{
		return lengthArray;
	}
	
	/**
	 * Get the angle array. These are the angles between each segment of the line. 
	 * @return see above.
	 */
	public List<Double> getAngleArray()
	{
		return angleArray;
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
	public String addUnits(String str)
	{
		if (shape == null) return str;
		
		if (units.isInMicrons()) return str+UIUtilities.MICRONS_SYMBOL;
		return str+UIUtilities.PIXELS_SYMBOL;
	}
					
	/**
	 * Calculates the bounds of the rendered figure, including the text rendered. 
	 * @return see above.
	 */
	public Rectangle2D.Double getDrawingArea()
	{
		Rectangle2D.Double newBounds = super.getDrawingArea();
		if(boundsArray!=null)
			for(int i = 0 ; i < boundsArray.size(); i++)
			{
				Rectangle2D bounds = boundsArray.get(i);
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
	 * Return the middle of the line segment from i, j, used to display text.
	 * @param i see above.
	 * @param j see above.
	 * @return see above.
	 */
	public Point2D.Double getLengthPosition(int i, int j)
	{
		Point2D.Double p0 = getPoint(i);
		Point2D.Double p1 = getPoint(j);
		
		double lx = (p0.x-p1.x)/2;
		double ly = (p0.y-p1.y)/2;
		double x = p0.x-lx;
		double y = p0.y-ly;
		
		return new Point2D.Double(x, y);
	}
	
	/**
	 * Return the length of the line segment from i, j
	 * @param i see above.
	 * @param j see above.
	 * @return see above.
	 */
	public double getLength(int i , int j)
	{
			Point2D.Double pt1 = getPt(i);
			Point2D.Double pt2 = getPt(j);
			return pt1.distance(pt2);
	}
	
	/**
	 * Return the angle between the line segment from i, j and j,k
	 * @param i see above.
	 * @param j see above.
	 * @param k see above.
	 * @return see above.
	 */
	public double getAngle(int i, int j, int k)
	{
		Point2D p0 = getPt(i);
		Point2D p1 = getPt(j);
		Point2D p2 = getPt(k);
		Point2D v0 = new Point2D.Double(p0.getX()-p1.getX(), p0.getY()-p1.getY());
		Point2D v1 = new Point2D.Double(p2.getX()-p1.getX(), p2.getY()-p1.getY());
		return Math.toDegrees(Math.acos(dotProd(v0, v1)));
	}
	
	/**
	 * Return the angle between the line segment from i, j from the x-axis.
	 * @param i see above.
	 * @param j see above.
	 * @return see above.
	 */
	public double getAngle(int i, int j)
	{
		Point2D p0 = getPt(i);
		Point2D p1 = getPt(j);
		Point2D v0 = new Point2D.Double(p0.getX()-p1.getX(), p0.getY()-p1.getY());
		Point2D v1 = new Point2D.Double(1,0);
		return Math.toDegrees(Math.acos(dotProd(v0, v1)));
	}
	
	
	/**
	 * Calculate the dot product of p0, p1.
	 * @param p0 see above.
	 * @param p1 see above.
	 * @return see above.
	 */
	public double dotProd(Point2D p0, Point2D p1)
	{
		double adotb = p0.getX()*p1.getX()+p0.getY()*p1.getY();
		double normab = Math.sqrt(p0.getX()*p0.getX()+p0.getY()*p0.getY())*
						Math.sqrt(p1.getX()*p1.getX()+p1.getY()*p1.getY());
		return adotb/normab;
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
	public void setROIShape(ROIShape shape)  { this.shape = shape; }

	/**
	 * Implemented as specified by the {@link ROIFigure} interface.
	 * @see ROIFigure#getType()
	 */
	public void calculateMeasurements() 
	{
		if (shape == null) return;
			
		pointArrayX.clear();
		pointArrayY.clear();
		lengthArray.clear();
		angleArray.clear();
		Point2D.Double pt;
		for (int i = 0 ; i < getPointCount(); i++)
		{
			pt = getPt(i);
			pointArrayX.add(pt.getX());
			pointArrayY.add(pt.getY());
		}
		
		if (getPointCount() == 2)
		{
			double angle = getAngle(0, 1);
			if (angle > 90) angle = Math.abs(angle-180);
			angleArray.add(angle);
			AnnotationKeys.ANGLE.set(shape, angleArray);
			lengthArray.add(getLength(0, 1));
			AnnotationKeys.LENGTH.set(shape, lengthArray);
		}
		else
		{
			for (int x = 1 ; x < this.getPointCount()-1; x++)
				angleArray.add(getAngle(x-1, x, x+1));

			for (int x = 1 ; x < this.getPointCount(); x++)
				lengthArray.add(getLength(x-1, x));

			AnnotationKeys.ANGLE.set(shape, angleArray);
			AnnotationKeys.LENGTH.set(shape, lengthArray);
		}
		AnnotationKeys.STARTPOINTX.set(shape, getPt(0).getX());
		AnnotationKeys.STARTPOINTY.set(shape, getPt(0).getY());
		AnnotationKeys.ENDPOINTX.set(shape, getPt(getPointCount()-1).getX());
		AnnotationKeys.ENDPOINTY.set(shape, getPt(getPointCount()-1).getY());
		AnnotationKeys.POINTARRAYX.set(shape, pointArrayX);
		AnnotationKeys.POINTARRAYY.set(shape, pointArrayY);
	}
		
	/**
	 * Implemented as specified by the {@link ROIFigure} interface.
	 * @see ROIFigure#getType()
	 */
	public String getType() { return FigureUtil.LINE_TYPE; }

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
		/*
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
		*/
		//Rectangle r = path.getBounds();
		List<Point> vector = new ArrayList<Point>();
		Line2D line;
		for (int i = 0 ; i < getNodeCount()-1; i++)
		{
			line = new Line2D.Double(getPoint(i), getPoint(i+1));
			iterateLine(line, vector);
		}
		
		return vector;
	}
	
	/**
	 * Iterate the line to get the points under it.
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
		LinkedHashMap<Point2D, Boolean> map = 
			new LinkedHashMap<Point2D, Boolean>();
		
		Point2D pt, quantisedPoint;
		for (double i = 0 ; i < lengthM ; i += 0.1)
		{
			pt = new Point2D.Double(start.getX()+i*mNorm.getX(),
					start.getY()+i*mNorm.getY());
			quantisedPoint = new Point2D.Double(Math.floor(pt.getX()), 
					Math.floor(pt.getY()));
			if (!map.containsKey(quantisedPoint))
				map.put(quantisedPoint, Boolean.valueOf(true));
		}
		Iterator<Point2D> i = map.keySet().iterator();
		
		while (i.hasNext())
		{
			pt = i.next();
			vector.add(new Point((int) pt.getX(), (int) pt.getY()));
		}
	}

	/**
	 * Overridden method for bezier, make public what 7.0 made private.
	 */
	public void removeAllNodes() { super.removeAllNodes(); }
	
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


