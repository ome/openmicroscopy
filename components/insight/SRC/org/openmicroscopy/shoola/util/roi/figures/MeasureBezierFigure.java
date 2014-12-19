/*
 * org.openmicroscopy.shoola.util.roi.figures.MeasureBezierFigure 
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


//Java imports
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
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
import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys;
import org.openmicroscopy.shoola.util.roi.model.annotation.MeasurementAttributes;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.util.MeasurementUnits;
import org.openmicroscopy.shoola.util.roi.model.util.UnitPoint;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.drawingtools.figures.BezierTextFigure;
import org.openmicroscopy.shoola.util.ui.drawingtools.figures.FigureUtil;
import omero.model.Length;
import omero.model.LengthI;
import omero.model.enums.UnitsLength;

/** 
 * Bezier figure with measurement.
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
	
	/** Flag indicating if the user can move or resize the shape.*/
	private boolean interactable;
	
	/** The list of X coordinates of the nodes on the line. */
	private List<Length>			pointArrayX;

	/** The list of Y coordinates of the nodes on the line. */
	private List<Length>			pointArrayY;
	
	/** The list of lengths of sections on the line. */
	private List<Length>			lengthArray;
	
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
	 * Returns the number of points(pixels) on the polyline.
	 * 
	 * @return See above.
	 */
	private int getLineSize()
	{
		int total = 0;
		Point2D pt1, pt2;
		for (int i = 0 ; i < getNodeCount()-1; i++)
		{
			pt1 = getPoint(i);
			pt2 = getPoint(i+1);
			iterateLine(new Line2D.Double(pt1, pt2), total);
		}
		return total;
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
	
	/**
	 * Iterates the line to get the points under it.
	 * 
	 * @param line The line to iterate.
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
		total += map.size();
	}

	/** Creates an instance of the Bezier figure. */
	public MeasureBezierFigure()
	{
		this(false, false, true, true, true, true);
	}
	
	/** Creates an instance of the Bezier figure. */
	public MeasureBezierFigure(boolean closed)
	{
		this(closed, false, true, true, true, true);
	}

	/**
	 * Creates an instance of the Bezier figure.
	 * 
	 * @param closed Pass <code>true</code> if the figure is a polygon,
	 * 				 <code>false</code> if it is a polyline.
	 * @param editable Flag indicating the figure can/cannot be edited.
	 * @param deletable Flag indicating the figure can/cannot be deleted.
	 * @param annotatable Flag indicating the figure can/cannot be annotated.
	 */
	public MeasureBezierFigure(boolean closed, boolean readOnly, 
			boolean clientObject, boolean editable, boolean deletable,
			boolean annotatable)
	{
		this(ROIFigure.DEFAULT_TEXT, closed, readOnly, clientObject, editable,
				deletable, annotatable);
	}
	
	/**
	 * Creates an instance of the Bezier figure (closed).
	 * 
	 * @param text The string displayed in the figure. 
	 */
	public MeasureBezierFigure(String text)
	{
		this(text, false);
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
		this(text, closed, false, true, true, true, true);
	}
	
	/**
	 * Creates an instance of the bezier figure.
	 * 
	 * @param text 	 The string displayed in the figure. 
	 * @param closed Pass <code>true</code> if the figure is a polygon,
	 * 				 <code>false</code> if it is a polyline.
	 * @param readOnly The figure is read only.
	 * @param clientObject The figure is a client object.
	 * @param editable Flag indicating the figure can/cannot be edited.
	 * @param deletable Flag indicating the figure can/cannot be deleted.
	 * @param annotatable Flag indicating the figure can/cannot be annotated.
	 */
	public MeasureBezierFigure(String text, boolean closed, boolean readOnly, 
			boolean clientObject, boolean editable, boolean deletable,
			boolean annotatable)
	{
		super(text, closed);
		setAttribute(MeasurementAttributes.FONT_FACE, DEFAULT_FONT);
		setAttribute(MeasurementAttributes.FONT_SIZE, new Double(FONT_SIZE));
		this.readOnly = readOnly;
		pointArrayX = new ArrayList<Length>();
		pointArrayY = new ArrayList<Length>();
		lengthArray = new ArrayList<Length>();
		status = IDLE;
		setReadOnly(readOnly);
		setClientObject(clientObject);
		this.deletable = deletable;
   		this.annotatable = annotatable;
   		this.editable = editable;
   		interactable = true;
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
		    Double sz = (Double) getAttribute(MeasurementAttributes.FONT_SIZE);
            Font font = (Font) getAttribute(MeasurementAttributes.FONT_FACE);
            if (font != null) g.setFont(font.deriveFont(sz.floatValue()));
            else {
                g.setFont(new Font(FONT_FAMILY, FONT_STYLE, sz.intValue()));
            }
			if (isClosed())
			{
				Length a = getArea();
				String polygonArea = UIUtilities.formatValue(a, true);
				bounds = g.getFontMetrics().getStringBounds(polygonArea, g);
				bounds = new Rectangle2D.Double(
						this.getBounds().getCenterX()-bounds.getWidth()/2,
					this.getBounds().getCenterY()+bounds.getHeight()/2,
					bounds.getWidth(), bounds.getHeight());
				if (MeasurementAttributes.SHOWMEASUREMENT.get(this))
				{
					g.setColor(
							MeasurementAttributes.MEASUREMENTTEXT_COLOUR.get(
									this));
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
				Length l = getLength();
				String polygonLength = UIUtilities.formatValue(l);
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
								polygonLength+"", (int) path.getCenter().getX(), 
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
		if (str == null)
			str = "0";
		return str + UIUtilities.DEGREE_SYMBOL;
	}
	
	/**
	 * Gets the point i in pixels or microns depending on the units used.
	 * 
	 * @param i node
	 * @return see above.
	 */
	private UnitPoint getPt(int i)
	{
		Point2D.Double pt = getNode(i).getControlPoint(0); 
		return new UnitPoint(transformX(pt.getX()), transformY(pt.getY()));
	}
	
	/**
	 * Calculates the length of the line.
	 * 
	 * @return see above.
	 */
	public Length getLength()
	{
		double length = 0;
		Point2D p0, p1;
		for (int i = 0 ; i < path.size()-1 ; i++)
		{
			p0 = path.get(i).getControlPoint(0);
			p1 = path.get(i+1).getControlPoint(0);
			length += p0.distance(p1);
		}
		return new LengthI(length, getUnit());
	}
	
	/** 
	 * Returns the number of points.
	 * 
	 * @return see above. 
	 */
	public int getPointCount() { return this.getPoints().size(); }
	
	/**
	 * Calculates the centre of the object (in pixels, or microns). 
	 * 
	 * @return see above.
	 */
	public UnitPoint getCentre()
	{
		Point2D.Double pt =  path.getCenter();
		return new UnitPoint(transformX(pt.x), transformY(pt.y));
	}
	
	/**
	 * Returns the Area of the object, in Pixels or microns.
	 * 
	 * @return see above.
	 */
	public Length getArea()
	{
		double area = 0;
		UnitPoint centre = getCentre();
		UnitPoint p0, p1;
		for (int i = 0 ; i < path.size() ; i++)
		{
			p0 = getPt(i);
			if (i == path.size()-1) p1 = getPt(0);
			else p1 = getPt(i+1);
		
			p0.setLocation(p0.x.getValue()-centre.x.getValue(), p0.y.getValue()-centre.y.getValue());
			p1.setLocation(p1.x.getValue()-centre.x.getValue(), p1.y.getValue()-centre.y.getValue());
			area += (p0.x.getValue()*p1.y.getValue()-p1.x.getValue()*p0.y.getValue());
		}
		return new LengthI(Math.abs(area/2), getUnit());
	}

	/** 
	 * Remove a node from the Bezier figure.
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
			pointArrayY.add(new LengthI(path.get(i).getControlPoint(0).getY(), getUnit()));
		}
		AnnotationKeys.POINTARRAYX.set(shape, pointArrayX);
		AnnotationKeys.POINTARRAYY.set(shape, pointArrayY);
		if (super.isClosed())
		{
			AnnotationKeys.AREA.set(shape,getArea());
			AnnotationKeys.PERIMETER.set(shape, getLength());
			AnnotationKeys.CENTREX.set(shape, getCentre().x);
			AnnotationKeys.CENTREY.set(shape, getCentre().y);
		}
		else
		{
			lengthArray.clear();
			lengthArray.add(getLength());
			AnnotationKeys.LENGTH.set(shape, lengthArray);
			AnnotationKeys.CENTREX.set(shape, getCentre().x);
			AnnotationKeys.CENTREY.set(shape, getCentre().y);
			AnnotationKeys.STARTPOINTX.set(shape, getPt(0).x);
			AnnotationKeys.STARTPOINTX.set(shape, getPt(0).y);
			AnnotationKeys.ENDPOINTX.set(shape, getPt(path.size()-1).x);
			AnnotationKeys.ENDPOINTY.set(shape, getPt(path.size()-1).y);
		}
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
	 * Returns the number of points(pixels) in the polygon.
	 * 
	 * @return See above.
	 */
	private int getAreaSize()
	{
		Rectangle r = path.getBounds();
		double iX = Math.floor(r.getX());
		double iY = Math.floor(r.getY());
		int total = 0;
		path.toPolygonArray();
		Point2D point = new Point2D.Double(0,0);
		for (int x = 0 ; x < Math.ceil(r.getWidth()); x++)
		{
			for ( int y = 0 ; y < Math.ceil(r.getHeight()) ; y++)
			{
				point.setLocation(iX+x, iY+y);
				if (path.contains(point))
					total++;
			}
		}
		return total;
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
	 * @see ROIFigure#getPoints()
	 */
	public List<Point> getPoints()
	{
		if (isClosed()) return getAreaPoints();
		return getLinePoints();
	}
	
	/**
	 * Implemented as specified by the {@link ROIFigure} interface.
	 * @see ROIFigure#getSize()
	 */
	public int getSize()
	{
		if (isClosed()) return getAreaSize();
		return getLineSize();
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
		if (!readOnly && interactable)
		{
			super.transform(tx);
			this.setObjectDirty(true);
		}
	}
	
	/**
	 * Overridden to stop updating shape if read only.
	 * @see AbstractAttributedFigure#setBounds(Point2D, Point2D)
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
		else
		{
			LinkedList<Handle> handles = new LinkedList<Handle>();
			handles.add(new FigureSelectionHandle(this));
			return handles;
		}
	}
	*/
	
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
	 * BezierFigure#clone()
	 */
	public MeasureBezierFigure clone()
	{
		MeasureBezierFigure that = (MeasureBezierFigure) super.clone();
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
		this.setObjectDirty(true);
		return super.joinSegments(join, tolerance);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.openmicroscopy.shoola.util.ui.drawingtools.figures.
	 * BezierTextFigure#setText(String)
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
		for(Object listener : listeners)
			if(listener instanceof FigureListener)
				figListeners.add((FigureListener)listener);
		return figListeners;
	}

	public BezierPath.Node removeNode(int node)
	{
		return super.removeNode(node);
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