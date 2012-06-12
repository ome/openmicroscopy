/*
 * org.openmicroscopy.shoola.util.roi.figures.MeasureLineConnectionFigure 
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
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

//Third-party libraries
import org.jhotdraw.draw.AbstractAttributedFigure;
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
import org.openmicroscopy.shoola.util.ui.drawingtools.figures.FigureUtil;
import org.openmicroscopy.shoola.util.ui.drawingtools.figures.LineConnectionTextFigure;

/** 
 * Line connection with measurement.
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
public class MeasureLineConnectionFigure
	extends LineConnectionTextFigure
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
	
	/** The bounds of the line figure. */
	private List<Rectangle2D>	boundsArray;
	
	/** The list of lengths of sections on the line. */
	private List<Double>	lengthArray;
	/** The list of angles of sections on the line. */
	private List<Double> 	angleArray;

	/** The list of X coords of the nodes on the line. */
	private List<Double>	pointArrayX;
	
	/** The list of Y coords of the nodes on the line. */
	private List<Double>	pointArrayY;
	
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
	
	/** Creates instance of line connection figure.*/
	public MeasureLineConnectionFigure()
	{
		this(DEFAULT_TEXT, false, true, true, true);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param text text to assign to the figure. 
	 * @param readOnly The figure is read only.
	 * @param editable Flag indicating the figure can/cannot be edited.
	 * @param deletable Flag indicating the figure can/cannot be deleted.
	 * @param annotatable Flag indicating the figure can/cannot be annotated.
	 */
	public MeasureLineConnectionFigure(String text, boolean readOnly,
			boolean editable, boolean deletable, boolean annotatable)
	{
		super(text);
		setAttribute(MeasurementAttributes.FONT_FACE, DEFAULT_FONT);
		setAttribute(MeasurementAttributes.FONT_SIZE, new Double(FONT_SIZE));
		lengthArray = new ArrayList<Double>();
		angleArray = new ArrayList<Double>();
		pointArrayX = new ArrayList<Double>();
		pointArrayY = new ArrayList<Double>();
		boundsArray = new ArrayList<Rectangle2D>();
		shape = null;
		roi = null;
		status = IDLE;
		setReadOnly(readOnly);
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
		if (MeasurementAttributes.SHOWMEASUREMENT.get(this) || 
				MeasurementAttributes.SHOWID.get(this))
		{
			if (getPointCount() == 2)
			{
				NumberFormat formatter = new DecimalFormat(FORMAT_PATTERN);
				double angle = getAngle(0, 1);
				if (angle > 90)
					angle = Math.abs(angle-180);
				angleArray.add(angle);
				String lineAngle = formatter.format(angle);
				lineAngle = addDegrees(lineAngle);
				double sz = ( Double) getAttribute(
						MeasurementAttributes.FONT_SIZE);
				g.setFont(new Font(FONT_FAMILY, FONT_STYLE, (int) sz));
				Rectangle2D rect = g.getFontMetrics().getStringBounds(lineAngle,
						g);
				Point2D.Double lengthPoint = getLengthPosition(0, 1);
				Rectangle2D bounds = new 
					Rectangle2D.Double(lengthPoint.x,
							lengthPoint.y+rect.getHeight()*2, rect.getWidth(), 
							rect.getHeight());
				g.setColor(
						MeasurementAttributes.MEASUREMENTTEXT_COLOUR.get(this));
				g.drawString(lineAngle, (int)bounds.getX(), (int)bounds.getY());
				boundsArray.add(bounds);
			}
			for (int x = 1 ; x < this.getPointCount()-1; x++)
			{
				NumberFormat formatter = new DecimalFormat(FORMAT_PATTERN);
				double angle = getAngle(x-1, x, x+1);
				angleArray.add(angle);
				String lineAngle = formatter.format(angle);
				lineAngle = addDegrees(lineAngle);
				double sz = (Double) getAttribute(
						MeasurementAttributes.FONT_SIZE);
				g.setFont(new Font(FONT_FAMILY, FONT_STYLE, (int)sz));
				Rectangle2D rect = g.getFontMetrics().getStringBounds(lineAngle,
						g);
				Rectangle2D bounds = new Rectangle2D.Double(getPoint(x).x, 
						getPoint(x).y, rect.getWidth(), rect.getHeight());
				g.setColor(
						MeasurementAttributes.MEASUREMENTTEXT_COLOUR.get(this));
				g.drawString(lineAngle, (int)bounds.getX(), (int)bounds.getY());
				boundsArray.add(bounds);
			}
			for (int x = 1 ; x < this.getPointCount(); x++)
			{
				NumberFormat formatter = new DecimalFormat(FORMAT_PATTERN);
				double length = getLength(x-1, x);
				lengthArray.add(length);
				String lineLength = formatter.format(length);
				lineLength = addUnits(lineLength);
				double sz = ((Double)
						getAttribute(MeasurementAttributes.FONT_SIZE));
				g.setFont(new Font(FONT_FAMILY, FONT_STYLE, (int) sz));
				Rectangle2D rect = g.getFontMetrics().getStringBounds(
						lineLength, g);
				Rectangle2D bounds = new Rectangle2D.Double(getPoint(x).x-15, 
						getPoint(x).y-15,rect.getWidth()+30, rect.getHeight()+30);
				Point2D.Double lengthPoint = getLengthPosition(x-1, x);
				g.setColor(
						MeasurementAttributes.MEASUREMENTTEXT_COLOUR.get(this));
				g.drawString(lineLength, (int)lengthPoint.x, (int)lengthPoint.y);
				boundsArray.add(bounds);
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
	
	/**
	 * Overridden to stop updating shape if read-only
	 * @see AbstractAttributedFigure#transform(AffineTransform)
	 */
	public void transform(AffineTransform tx)
	{
		if (!readOnly && interactable)
			super.transform(tx);
	}
		
	/**
	 * Overridden to stop updating shape if read-only.
	 * @see AbstractAttributedFigure#setBounds(Double, Double)
	 */
	public void setBounds(Point2D.Double anchor, Point2D.Double lead) 
	{
		if (!readOnly && interactable)
			super.setBounds(anchor, lead);
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
		if (shape == null) 
			return str;
		
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
		if (boundsArray != null)
			for (int i = 0 ; i < boundsArray.size(); i++)
			{
				Rectangle2D bounds = boundsArray.get(i);
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
					double diff = bounds.getX()+
					bounds.getWidth()-newBounds.getX()+newBounds.getWidth();
					newBounds.width = newBounds.width+diff;
				}
				if (bounds.getY()+bounds.getHeight() > 
				newBounds.getY()+newBounds.getHeight())
				{
					double diff = bounds.getY()+bounds.getHeight()-
					newBounds.getY()+newBounds.getHeight();
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
	 * Get the point i in pixels or microns depending on the units used.
	 * 
	 * @param i node
	 * @return see above.
	 */
	private Point2D.Double getPt(int i)
	{
		if (shape != null) {
			if (units.isInMicrons())
			{
				Point2D.Double pt = getPoint(i);
				double tx = UIUtilities.transformSize(
						pt.getX()*units.getMicronsPixelX()).getValue();
				double ty = UIUtilities.transformSize(
						pt.getY()*units.getMicronsPixelY()).getValue();
				return new Point2D.Double(tx, ty);
			}
			return getPoint(i);
		}
			
		return getPoint(i);
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
		Point2D p0 = getPoint(i);
		Point2D p1 = getPoint(j);
		Point2D p2 = getPoint(k);
		Point2D v0 = new Point2D.Double(p0.getX()-p1.getX(), 
				p0.getY()-p1.getY());
		Point2D v1 = new Point2D.Double(p2.getX()-p1.getX(),
				p2.getY()-p1.getY());
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
		Point2D p0 = getPoint(i);
		Point2D p1 = getPoint(j);
		Point2D v0 = new Point2D.Double(p0.getX()-p1.getX(), 
				p0.getY()-p1.getY());
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
	public void setROIShape(ROIShape shape) { this.shape = shape; }

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
		AnnotationKeys.STARTPOINTX.set(shape, getPt(0).getY());
		AnnotationKeys.ENDPOINTX.set(shape, getPt(getPointCount()-1).getX());
		AnnotationKeys.ENDPOINTY.set(shape, getPt(getPointCount()-1).getY());
		AnnotationKeys.POINTARRAYX.set(shape, pointArrayX);
		AnnotationKeys.POINTARRAYY.set(shape, pointArrayY);
	}
		
	/**
	 * Implemented as specified by the {@link ROIFigure} interface.
	 * @see ROIFigure#getType()
	 */
	public String getType() { return FigureUtil.LINE_CONNECTION_TYPE; }

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
		Rectangle r = path.getBounds();
		List<Point> vector = new ArrayList<Point>();
		int yEnd = r.y+r.height;
		int x, y;
		int index = 0;
		for (y = r.y; y < yEnd; ++y) {
			x = r.x+index;
			if (r.contains(x, y)) vector.add(new Point(x, y));
			index++;
		}
		return vector;
	}
	
	/**
	 * Implemented as specified by the {@link ROIFigure} interface.
	 * @see ROIFigure#getSize()
	 */
	public int getSize()
	{
		Rectangle r = path.getBounds();
		int total = 0;
		int yEnd = r.y+r.height;
		int x, y;
		int index = 0;
		for (y = r.y; y < yEnd; ++y) {
			x = r.x+index;
			if (r.contains(x, y)) total++;
			index++;
		}
		return total;
	}

	/**
	 * Returns the number of points in the line. 
	 * 
	 * @result See above.
	 */
	public int getPointCount() { return getNodeCount(); }
	
	/**
	 * Overridden method for Line, make public what 7.0 made private.
	 */
	public void removeAllNodes()
	{
		super.removeAllNodes();
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
	 * @see MeasureLineConnectionFigure#clone()
	 */
	public MeasureLineConnectionFigure clone()
	{
		MeasureLineConnectionFigure that = 
			(MeasureLineConnectionFigure) super.clone();
		that.setReadOnly(this.isReadOnly());
		that.setClientObject(this.isClientObject());
		that.setObjectDirty(true);
		that.setInteractable(true);
		return that;
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