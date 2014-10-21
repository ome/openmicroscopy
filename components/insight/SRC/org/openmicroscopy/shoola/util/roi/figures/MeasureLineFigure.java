/*
 * org.openmicroscopy.shoola.util.roi.figures.MeasureLineFigure 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;






//Third-party libraries
import org.jhotdraw.draw.AbstractAttributedFigure;
import org.jhotdraw.draw.FigureListener;
import org.jhotdraw.geom.BezierPath;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys;
import org.openmicroscopy.shoola.util.roi.model.annotation.MeasurementAttributes;
import org.openmicroscopy.shoola.util.roi.model.util.MeasurementUnits;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.UnitsObject;
import org.openmicroscopy.shoola.util.ui.drawingtools.figures.FigureUtil;
import org.openmicroscopy.shoola.util.ui.drawingtools.figures.LineTextFigure;

/** 
 * Line with measurement.
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
	
	/** Flag indicating the figure can/cannot be deleted.*/
	private boolean deletable;
	
	/** Flag indicating the figure can/cannot be annotated.*/
	private boolean annotatable;
	
	/** Flag indicating the figure can/cannot be edited.*/
	private boolean editable;
	
	/** Is this figure read only. */
	private boolean readOnly;

	/** Is this figure a client object. */
	private boolean clientObject;
	
	/** has the figure been modified. */
	private boolean dirty;
	
	/** The bounds of the Line figure. */
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
	
	/** Flag indicating if the user can move or resize the shape.*/
	private boolean interactable;
	
	/** The units of reference.*/
	private String refUnits;
	
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
			double tx = UIUtilities.transformSize(
					pt.getX()*units.getMicronsPixelX(), refUnits);
			double ty = UIUtilities.transformSize(
					pt.getY()*units.getMicronsPixelY(), refUnits);
			return new Point2D.Double(tx, ty);
		}
		return getPoint(i);
	}

	/**
	 * Formats the area.
	 * 
	 * @param value The value to format.
	 * @return See above.
	 */
	private String formatValue(double value, boolean degree)
	{
	    NumberFormat formatter = new DecimalFormat(FORMAT_PATTERN);
	    if (units.isInMicrons()){ 
	        UnitsObject v = UIUtilities.transformSize(value);
	        StringBuffer buffer = new StringBuffer();
	        buffer.append(formatter.format(v.getValue()));
	        buffer.append(v.getUnits());
	        return buffer.toString();
	    }
	    if (degree) return addDegrees(formatter.format(value));
	    else return addUnits(formatter.format(value));
	}

	/** Creates a new instance. */
	public MeasureLineFigure()
	{
		this(DEFAULT_TEXT, false, true, true, true, true);
	}


	/** 
	 * Creates a new instance.
	 *  
	 * @param readOnly the figure is read only.
     * @param clientObject the figure is a client object.
     * @param editable Flag indicating the figure can/cannot be edited.
	 * @param deletable Flag indicating the figure can/cannot be deleted.
	 * @param annotatable Flag indicating the figure can/cannot be annotated.
	 */
	public MeasureLineFigure(boolean readOnly, boolean clientObject, 
			boolean editable, boolean deletable, boolean annotatable)
	{
		this(DEFAULT_TEXT, readOnly, clientObject, editable, deletable, 
				annotatable);
	}
	
	/**
	 * Creates a new instance
	 * 
	 * @param text The text to add to the figure.
	 * @param readOnly the figure is read only.
     * @param clientObject the figure is a client object.
     * @param editable Flag indicating the figure can/cannot be edited.
	 * @param deletable Flag indicating the figure can/cannot be deleted.
	 * @param annotatable Flag indicating the figure can/cannot be annotated.
	 */
	public MeasureLineFigure(String text, boolean readOnly,
			boolean clientObject, boolean editable, boolean deletable,
			boolean annotatable)
	{
		super(text);
		setAttribute(MeasurementAttributes.FONT_FACE, DEFAULT_FONT);
		setAttribute(MeasurementAttributes.FONT_SIZE, new Double(FONT_SIZE));
		boundsArray = new ArrayList<Rectangle2D>();
		lengthArray = new ArrayList<Double>();
		angleArray = new ArrayList<Double>();
		pointArrayX = new ArrayList<Double>();
		pointArrayY = new ArrayList<Double>();
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
     * Draw the figure on the graphics context.
     * @param g the graphics context.
     */
	public void draw(Graphics2D g)
	{
		super.draw(g);
		boundsArray.clear();
		lengthArray.clear();
		angleArray.clear();
		if (MeasurementAttributes.SHOWMEASUREMENT.get(this))
		{
		    Double sz = (Double) getAttribute(MeasurementAttributes.FONT_SIZE);
            Font font = (Font) getAttribute(MeasurementAttributes.FONT_FACE);
            if (font != null) g.setFont(font.deriveFont(sz.floatValue()));
            else {
                g.setFont(new Font(FONT_FAMILY, FONT_STYLE, sz.intValue()));
            }
		    g.setColor(MeasurementAttributes.STROKE_COLOR.get(this));
			if (getPointCount() == 2)
			{
				double angle = getAngle(0, 1);
				if(angle>90)
					angle = Math.abs(angle-180);
				angleArray.add(angle);
				String lineAngle = formatValue(angle, true);
				Rectangle2D rect = g.getFontMetrics().getStringBounds(
						lineAngle, g);
				Point2D.Double lengthPoint = getLengthPosition(0, 1);
				Rectangle2D bounds = new Rectangle2D.Double(lengthPoint.x,
						lengthPoint.y+rect.getHeight()*2, rect.getWidth(),
						rect.getHeight());
				g.drawString(lineAngle, (int)bounds.getX(), (int)bounds.getY());
				boundsArray.add(bounds);
			}
			for( int x = 1 ; x < this.getPointCount()-1; x++)
			{
				double angle = getAngle(x-1, x, x+1);
				angleArray.add(angle);
				String lineAngle = formatValue(angle, true);
				Rectangle2D rect = g.getFontMetrics().getStringBounds(lineAngle,
						g);
				Rectangle2D bounds = new Rectangle2D.Double(getPoint(x).x,
						getPoint(x).y, rect.getWidth(), rect.getHeight());

				g.drawString(lineAngle, (int) bounds.getX(), (int) bounds.getY());
				boundsArray.add(bounds);
			}
			double total = 0;
			int n = getPointCount();
			String v = "";
			NumberFormat formatter = new DecimalFormat(FORMAT_PATTERN);
			
			int px, py;
			for( int x = 1 ; x < n; x++)
			{
				double length = getLength(x-1, x);
				lengthArray.add(length);
				String lineLength = formatValue(length, false);
				Point2D.Double lengthPoint = getLengthPosition(x-1, x);
				Rectangle2D rect = g.getFontMetrics().getStringBounds(
						lineLength, g);
				Rectangle2D bounds = new Rectangle2D.Double(lengthPoint.x-15, 
						lengthPoint.y-15,rect.getWidth()+30, 
						rect.getHeight()+30);
				px = (int) lengthPoint.x;
				py = (int) lengthPoint.y;
				g.drawString(lineLength, px, py);
				boundsArray.add(bounds);
				total += length;
				v += formatter.format(length);
				if (x != (n-1)) v +="+";
			}
			v += "="+formatter.format(total);
			v = addUnits(v);
			if (n > 2) {
				List<Point> l = getPoints();
				Iterator<Point> j = l.iterator();
				Point p;
				int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, yLoc = 0;
				while (j.hasNext()) {
					p = j.next();
					if (minX > p.x) minX = p.x;
					if (maxX < p.x) maxX = p.x;
					if (yLoc < p.y) yLoc = p.y;
				}
				Rectangle2D b = g.getFontMetrics().getStringBounds(v, g);
				g.drawString(v, (int) minX, (int) (yLoc+b.getHeight()));
			}
		}
		if (MeasurementAttributes.SHOWID.get(this))
		{
			Rectangle2D bounds;
			g.setColor(this.getTextColor());
			bounds = g.getFontMetrics().getStringBounds(getROI().getID()+"", g);
			bounds = new Rectangle2D.Double(
						path.getCenter().getX()-bounds.getWidth()/2,
						path.getCenter().getY()+bounds.getHeight()/2,
					bounds.getWidth(), bounds.getHeight());
			g.drawString(getROI().getID()+"", (int) bounds.getX(), 
					(int)bounds.getY());
		}
	}
	
	/**
	 * Overridden to stop updating shape if read-only
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
	 * Overridden to stop updating shape if readonly.
	 * @see AbstractAttributedFigure#setBounds(Double, Double)
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
		if(!readOnly)
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
		return str + UnitsObject.DEGREES;
	}
	
	/**
	 * Add length unit, (pixels, microns) to the measurements. 
	 * @param str the measurement.
	 * @return see above.
	 */
	public String addUnits(String str)
	{
		if (shape == null) return str;
		
		if (units.isInMicrons()) return str+refUnits;
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
	public void setROI(ROI roi)
	{ 
		this.roi = roi; 
	}

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
			lengthArray.add(getLength(0, 1));
		}
		else
		{
			for (int x = 1 ; x < this.getPointCount()-1; x++)
				angleArray.add(getAngle(x-1, x, x+1));

			for (int x = 1 ; x < this.getPointCount(); x++)
				lengthArray.add(getLength(x-1, x));

		}
		AnnotationKeys.ANGLE.set(shape, angleArray);
		AnnotationKeys.LENGTH.set(shape, lengthArray);
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
		refUnits = UIUtilities.transformSize(
				units.getMicronsPixelX()).getUnits();
	}
	
	/**
	 * Implemented as specified by the {@link ROIFigure} interface.
	 * @see ROIFigure#getPoints()
	 */
	public List<Point> getPoints()
	{
		List<Point> vector = new ArrayList<Point>();
		for (int i = 0 ; i < getNodeCount()-1; i++)
			iterateLine(new Line2D.Double(getPoint(i), getPoint(i+1)), vector);

		return vector;
	}
	
	/**
	 * Implemented as specified by the {@link ROIFigure} interface.
	 * @see ROIFigure#getSize()
	 */
	public int getSize()
	{
		int total = 0;
		for (int i = 0 ; i < getNodeCount()-1; i++)
			iterateLine(new Line2D.Double(getPoint(i), getPoint(i+1)), total);
		
		return total;
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
	 * Iterate the line to get the points under it.
	 * @param line the line to iterate.
	 * @param total The total number of points.
	 */
	private void iterateLine(Line2D line, int total)
	{
		Point2D start = line.getP1();
		Point2D end = line.getP2();
		Point2D m = new Point2D.Double(end.getX()-start.getX(), 
				end.getY()-start.getY());
		double lengthM = (Math.sqrt(m.getX()*m.getX()+m.getY()*m.getY()));
		Point2D mNorm = new Point2D.Double(m.getX()/lengthM,m.getY()/lengthM);
		Map<Point2D, Boolean> map = new HashMap<Point2D, Boolean>();
		
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
		total += map.size();
	}
	
	/**
	 * Overridden method for bezier, make public what 7.0 made private.
	 */
	public void removeAllNodes() 
	{ 
		super.removeAllNodes(); 
		this.setObjectDirty(true);
	}
	
	/**
	 * Implemented as specified by the {@link ROIFigure} interface.
	 * @see ROIFigure#setStatus(boolean)
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
	public boolean isDirty() 
	{
		return dirty;
	}

	/**
	 * Implemented as specified by the {@link ROIFigure} interface
	 * @see ROIFigure#setObjectDirty(boolean)
	 */
	public void setObjectDirty(boolean dirty) 
	{
		this.dirty = dirty;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.openmicroscopy.shoola.util.ui.drawingtools.figures.
	 * MeasureLineFigure#clone()
	 */
	public MeasureLineFigure clone()
	{
		MeasureLineFigure that = (MeasureLineFigure) super.clone();
		that.setReadOnly(this.isReadOnly());
		that.setClientObject(this.isClientObject());
		that.setObjectDirty(true);
		that.setInteractable(true);
		return that;
	}
	/*
	 * (non-Javadoc)
	 * @see org.openmicroscopy.shoola.util.ui.drawingtools.figures.
	 * BezierFigure#setClosed(boolean)
	 */
	public void setClosed(boolean newValue) 
	{
		super.setClosed(newValue);
		this.setObjectDirty(true);
	}

	/*
	 * (non-Javadoc)
	 * @see org.openmicroscopy.shoola.util.ui.drawingtools.figures.
	 * BezierFigure#setBezierPath(BezierPath)
	 */
	public void setBezierPath(BezierPath newValue) 
	{
		if (isReadOnly() || !interactable) return;
		super.setBezierPath(newValue);
		this.setObjectDirty(true);
	}
	   
	/*
	 * (non-Javadoc)
	 * @see org.openmicroscopy.shoola.util.ui.drawingtools.figures.
	 * BezierFigure#setEndPoint(Point2D)
	 */
	public void setEndPoint(Point2D.Double p) 
	{
		if (isReadOnly() || !interactable) return;
		super.setEndPoint(p);
		this.setObjectDirty(true);
	}

	/*
	 * (non-Javadoc)
	 * @see org.openmicroscopy.shoola.util.ui.drawingtools.figures.
	 * BezierFigure#setNode(Point2D)
	 */
	public void setNode(int index, BezierPath.Node p) 
	{
		if (isReadOnly() || !interactable) return;
		super.setNode(index, p);
		this.setObjectDirty(true);
	}

	/*
	 * (non-Javadoc)
	 * @see org.openmicroscopy.shoola.util.ui.drawingtools.figures.
	 * BezierFigure#setPoint(int, int, Point2D)
	 */
	public void setPoint(int index, int coord, Point2D.Double p) 
	{
		if (isReadOnly() || !interactable) return;
		super.setPoint(index, coord, p);
		this.setObjectDirty(true);
	}

	/*
	 * (non-Javadoc)
	 * @see org.openmicroscopy.shoola.util.ui.drawingtools.figures.
	 * BezierFigure#setStartPoint(Point2D)
	 */
	public void setStartPoint(Point2D.Double p) 
	{
		if (isReadOnly() || !interactable) return;
		super.setStartPoint(p);
		this.setObjectDirty(true);
	}

	/*
	 * (non-Javadoc)
	 * @see org.openmicroscopy.shoola.util.ui.drawingtools.figures.
	 * BezierFigure#splitSegment(Point2D)
	 */
	public int splitSegment(Point2D.Double split) 
	{
		if (isReadOnly() || !interactable) return -1;
		this.setObjectDirty(true);
		return super.splitSegment(split);
	}

	/*
	 * (non-Javadoc)
	 * @see org.openmicroscopy.shoola.util.ui.drawingtools.figures.
	 * BezierFigure#splitSegment(Point2D, float)
	 */
	public int splitSegment(Point2D.Double split, float tolerance) 
	{
		if (isReadOnly() || !interactable) return -1;
		this.setObjectDirty(true);
		return super.splitSegment(split, tolerance);
	}

	/*
	 * (non-Javadoc)
	 * @see org.openmicroscopy.shoola.util.ui.drawingtools.figures.
	 * BezierFigure#joinSegments(Point2D, float)
	 */
	public int joinSegments(Point2D.Double join, float tolerance) 
	{
		if (isReadOnly() || !interactable) return -1;
		this.setObjectDirty(true);
		return super.joinSegments(join, tolerance);
	}
	
	/**
	 * Overridden to mark the object has dirty.
	 * @see MeasureLineFigure#setText(String)
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