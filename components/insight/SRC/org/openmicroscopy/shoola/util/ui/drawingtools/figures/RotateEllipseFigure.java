/*
 * org.openmicroscopy.shoola.util.ui.drawingtools.figures.RotateEllipseFigure 
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
package org.openmicroscopy.shoola.util.ui.drawingtools.figures;



//Java imports
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.LinkedList;

//Third-party libraries
import org.jhotdraw.draw.AbstractAttributedFigure;
import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.ConnectionFigure;
import org.jhotdraw.draw.Connector;
import org.jhotdraw.draw.Handle;
import org.jhotdraw.draw.ResizeHandleKit;
import org.jhotdraw.draw.TransformHandleKit;
import org.jhotdraw.geom.Geom;
import org.jhotdraw.samples.svg.Gradient;
import org.jhotdraw.samples.svg.SVGAttributeKeys;

//Application-internal dependencies

/** 
 * Version of an EllipseFigure that the user can rotate.
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
public class RotateEllipseFigure  
	extends AbstractAttributedFigure  
{	

	/** The geometry of the ellipse figure. */
	protected Ellipse2D.Double ellipse;
	
	/**
	 * This is used to perform faster drawing and hit testing.
	 */
	protected Shape cachedTransformedShape;
	    
	/** Creates a new instance. */
	public RotateEllipseFigure() 
	{
		this(0, 0, 0, 0);
	}
	
	/**
	 * Created a new rotated Ellipse with position x, y and width and height.
	 * 
	 * @param x 		The x-coordinate of the top-left corner.
	 * @param y 		The y-coordinate of the top-left corner.
	 * @param width 	The width of the ellipse.
	 * @param height 	The height of the ellipse.
	 */
	public RotateEllipseFigure(double x, double y, double width, double height)
	{
		ellipse = new Ellipse2D.Double(x, y, width, height);
		AffineTransform transform = new AffineTransform();
		transform.setToIdentity();
		AttributeKeys.TRANSFORM.set(this, transform);
	}
		    
	/**
	 *  Sets the attribute key to the value newValue on the figure. If the 
	 *  attribute is an AffineTransform then we need to redraw the image.
	 *  
	 *  @param key The attribute key.
	 *  @param newValue The new value of the attribute. 
	 */
	public void setAttribute(AttributeKey key, Object newValue)
	{
		if (key == AttributeKeys.TRANSFORM) {
			invalidate();
		} 
		super.setAttribute(key, newValue);
	} 

	/**
	 * Returns the bounds of the ellipse before applying the affine transform.
	 * 
	 * @return See above. 
	 */
	public Rectangle2D.Double getBounds()
	{
		return new Rectangle2D.Double(ellipse.getBounds2D().getX(),
			ellipse.getBounds2D().getY(), ellipse.getBounds2D().getWidth(),
			ellipse.getBounds2D().getHeight());
	}
	
	/**
	 * Overridden to return the drawing area, taking into account the 
	 * affine transformation.
	 * @see AbstractAttributedFigure#getDrawingArea()
	 */
	public Rectangle2D.Double getDrawingArea()
	{
		Rectangle2D rx = getTransformedShape().getBounds2D();
		Rectangle2D.Double r =
				(rx instanceof Rectangle2D.Double) ? (Rectangle2D.Double) 
						rx : new Rectangle2D.Double(rx.getX(), rx.getY(), 
							rx.getWidth(), rx.getHeight());
		AffineTransform object = AttributeKeys.TRANSFORM.get(this);
		if (object == null)
		{
			double g = SVGAttributeKeys.getPerpendicularHitGrowth(this)*2;
			Geom.grow(r, g, g);
		}
		else
		{
			double strokeTotalWidth = AttributeKeys.getStrokeTotalWidth(this);
			double width = strokeTotalWidth/2d;
			width *= Math.max(object.getScaleX(), object.getScaleY());
			Geom.grow(r, width, width);
		}
		return r;
	}
	
	public boolean containsMapped(double oX, double oY)
	{
		AffineTransform t = AttributeKeys.TRANSFORM.get(this);
		double x = oX-ellipse.getCenterX();
		double y = oY-ellipse.getCenterY();
		double[] matrix = new double[6];
		t.getMatrix(matrix);
		matrix[4] = 0;
		matrix[5] = 0;
		
		// Create a new transform with rotation only, 
		AffineTransform aT = new AffineTransform(matrix);
		Point2D.Double startPt = new Point2D.Double(1,0);
		Point2D.Double a = (Point2D.Double)aT.transform(startPt, null);
		// Calculate the starting rotation point.
		double thetaStart = Math.acos(startPt.y/Math.sqrt
				(startPt.x*startPt.x+startPt.y*startPt.y));
		
		// Calculate the current rotation the ellipse has undergone.
		double rotation = Math.acos(a.y/Math.sqrt(a.x*a.x+a.y*a.y));
		// Normalise to compensate for rotations > 180'
		double theta = rotation-Math.floor(rotation/Math.PI)*Math.PI-thetaStart;
		AffineTransform newT = new AffineTransform();
		newT.setToRotation(theta);
		Point2D rotatedPoint = new Point2D.Double();
		newT.transform(new Point2D.Double(x, y), rotatedPoint);
		return containsEllipseAlgorithm(rotatedPoint.getX(), rotatedPoint.getY(),
				0,0, ellipse.getWidth(), ellipse.getHeight());
	}
	
	private boolean containsEllipseAlgorithm(double x, double y, 
							 double cx, double cy, 
							 double w, double h)
	{
		double wr = w/2;
		double hr = h/2;
		double xx = x-cx;
		double yy = y-cy;
		double dist = (xx*xx)/(wr*wr)+(yy*yy)/(hr*hr);
		return dist< 1;
	}

	
	/**
	 * Checks if a Point2D.Double is inside the figure.
	 * @see AbstractAttributedFigure#contains(Point2D.Double)
	 */
	public boolean contains(Point2D.Double p)
	{
		// XXX - This does not take the stroke width into account!
		return getTransformedShape().contains(p);
	}

	/**
	 * Returns the ellipse after the affine transform has been applied to it.
	 * This will return the ellipse with the correct width and height, but the 
	 * x, y coords will be the lead.x, lead.y of the transformed shape.
	 * 
	 * @return see above.
	 */
	public Ellipse2D.Double getTransformedEllipse()
	{
		AffineTransform t = AttributeKeys.TRANSFORM.get(this);
		if (t == null) return ellipse;
		
		Ellipse2D.Double e = new Ellipse2D.Double(0,0,0,0);
		
		Point2D.Double startW = (Point2D.Double) t.transform(
				new Point2D.Double(0, ellipse.getCenterY()), null);
		Point2D.Double endW = (Point2D.Double) t.transform(
				new Point2D.Double(ellipse.getWidth(), 
						ellipse.getCenterY()), null);
		Point2D.Double startH = (Point2D.Double) t.transform(
				new Point2D.Double(ellipse.getCenterX(), 0), null);
		Point2D.Double endH = (Point2D.Double) t.transform(
				new Point2D.Double(ellipse.getCenterX(), ellipse.getHeight()),
				null);
		Point2D.Double lead = (Point2D.Double) t.transform(
				new Point2D.Double(0,0), null);
		e.width = Math.round(startW.distance(endW));
		e.height = Math.round(startH.distance(endH));
		e.x = Math.round(lead.getX());
		e.y = Math.round(lead.getY());
		return e;
	}
	
	/**
	 * Returns the Transformed ellipse as a transformedShape, of type Shape.
	 * 
	 * @return See above.
	 */
	protected Shape getTransformedShape()
	{
		AffineTransform t = AttributeKeys.TRANSFORM.get(this);
		if (t == null) cachedTransformedShape = ellipse;
		else cachedTransformedShape = t.createTransformedShape(ellipse);
		return cachedTransformedShape; 
	}
	
	/**
	 * Returns the Transformed ellipse as a transformedShape, of type Shape.
	 * 
	 * @return See above.
	 */
	protected Shape getTransformedShape(double i, double j)
	{
		AffineTransform t = AttributeKeys.TRANSFORM.get(this);
		if (t == null) cachedTransformedShape = ellipse;
		else
		{
			Ellipse2D.Double newEllipse = new Ellipse2D.Double(
					ellipse.x-i, ellipse.y-j, ellipse.width+i, ellipse.height+j);
			cachedTransformedShape = t.createTransformedShape(newEllipse);
		}
		return cachedTransformedShape; 
	}
	
	/**
	 * Returns the ellipse.
	 * 
	 * @return See above.
	 */
	public Ellipse2D.Double getEllipse() { return ellipse; }
	
	/**
	 * Overridden to return the correct handles.
	 * @see AbstractAttributedFigure#createHandles(int)
	 */
	public Collection<Handle> createHandles(int detailLevel)
	{
		LinkedList<Handle> handles = new LinkedList<Handle>();
		switch (detailLevel%2)
		{
			case 0:
				ResizeHandleKit.addResizeHandles(this, handles);
				break;
			case 1:
				TransformHandleKit.addTransformHandles(this, handles);
				break;
			default:
				break;
		}
		return handles;
	} 
	
	/**
	 * Sets the ellipse geometry.
	 * @param x see above.
	 * @param y see above.
	 * @param width see above.
	 * @param height see above.
	 */
	public void setEllipse(double x, double y, double width, double height)
	{
		ellipse.x = x;
		ellipse.y = y;
		ellipse.width = width;
		ellipse.height = height;
		invalidate();
	}
	
	/**
	 * Sets the bounds of the ellipse from the anchor to lead. 
	 * 
	 * @param anchor The start point of the drawing action.
	 * @param lead The end point the drawing action.
	 * 
	 */
	public void setBounds(Point2D.Double anchor, Point2D.Double lead)
	{
		ellipse.x = Math.min(anchor.x, lead.x);
		ellipse.y = Math.min(anchor.y, lead.y);
		ellipse.width = Math.max(0.1, Math.abs(lead.x-anchor.x));
		ellipse.height = Math.max(0.1, Math.abs(lead.y-anchor.y));
		invalidate();
	}

	/**
	 * Returns the height of the transformed ellipse.
	 * 
	 * @return See above.
	 */
	public double getHeight()
	{
		return getTransformedEllipse().getHeight();
	}

	/**
	 * Returns the width of the transformed ellipse.
	 * 
	 * @return See above.
	 */
	public double getWidth()
	{
		return getTransformedEllipse().getWidth();
	}
	
	/**
	 * Set the width of the ellipse to the newWidth, the new ellipse will 
	 * still be centered around the same point as the original ellipse.
	 * 
	 * @param newWidth see above.
	 */
	public void setWidth(double newWidth)
	{
		AffineTransform t = AttributeKeys.TRANSFORM.get(this);
		if (t == null) return;
		
		double centreX = getCentreX();
		double centreY = getCentreY();
		double height = getHeight();
		double[] matrix = new double[6];
		t.getMatrix(matrix);
		matrix[4] = 0;
		matrix[5] = 0;
		AffineTransform aT = new AffineTransform(matrix);
		Point2D.Double a = (Point2D.Double) aT.transform(
				new Point2D.Double(1,0), null);
		double theta = Math.asin(a.y/Math.sqrt(a.x*a.x+a.y*a.y));
		AffineTransform newT = new AffineTransform();
		newT.setToRotation(theta);
		
		Ellipse2D.Double newEllipse = new Ellipse2D.Double(0, 0,
									newWidth, height);
		
		Shape rotatedShape = newT.createTransformedShape(newEllipse);
		double rotatedShapeCentreX = rotatedShape.getBounds2D().getCenterX();
		double rotatedShapeCentreY = rotatedShape.getBounds2D().getCenterY();
		
		double diffX = centreX-rotatedShapeCentreX;
		double diffY = centreY-rotatedShapeCentreY;
	
		Point2D.Double lead = new Point2D.Double(diffX, diffY);
		
		AffineTransform rT = AffineTransform.getTranslateInstance(lead.x, 
							lead.y);
		rT.concatenate(newT);
		
		ellipse = newEllipse;
		AttributeKeys.TRANSFORM.set(this, rT);
		invalidate();
	}
	
	/**
	 * Returns the height of the ellipse to the newHieght, the new ellipse will 
	 * still be centered around the same point as the original ellipse.
	 * 
	 * @param newHeight see above.
	 */
	public void setHeight(double newHeight)
	{
		AffineTransform t = AttributeKeys.TRANSFORM.get(this);
		if (t == null) return;
		
		double centreX = getCentreX();
		double centreY = getCentreY();
		double width = getWidth();
		double[] matrix = new double[6];
		t.getMatrix(matrix);
		matrix[4] = 0;
		matrix[5] = 0;
		AffineTransform aT = new AffineTransform(matrix);
		Point2D.Double a = (Point2D.Double) aT.transform(
					new Point2D.Double(1, 0), null);
		double theta = Math.asin(a.y/Math.sqrt(a.x*a.x+a.y*a.y));
		AffineTransform newT = new AffineTransform();
		newT.setToRotation(theta);
		
		Ellipse2D.Double newEllipse = new Ellipse2D.Double(0, 0, width, 
									newHeight);
		
		Shape rotatedShape = newT.createTransformedShape(newEllipse);
		double rotatedShapeCentreX = rotatedShape.getBounds2D().getCenterX();
		double rotatedShapeCentreY = rotatedShape.getBounds2D().getCenterY();
		
		double diffX = centreX-rotatedShapeCentreX;
		double diffY = centreY-rotatedShapeCentreY;
	
		Point2D.Double lead = new Point2D.Double(diffX, diffY);
		
		AffineTransform rT = AffineTransform.getTranslateInstance(lead.x, 
									lead.y);
		rT.concatenate(newT);
		
		ellipse = newEllipse;
		AttributeKeys.TRANSFORM.set(this, rT);
		invalidate();
	}
	
	/** 
	 * Returns the x coordinate of the figure. 
	 * 
	 * @return See above.
	 */
	public double getCentreX()
	{
		AffineTransform t = AttributeKeys.TRANSFORM.get(this);
		if (t == null) return ellipse.getCenterX();
		Point2D src = new Point2D.Double(ellipse.getCenterX(), 
									ellipse.getCenterY());
		Point2D dest = new Point2D.Double();
		t.transform(src, dest);
		return dest.getX();
	}
	
	/** 
	 * Returns the y coordinate of the figure. 
	 * 
	 * @return See above.
	 */
	public double getCentreY()
	{
		AffineTransform t = AttributeKeys.TRANSFORM.get(this);
		if (t == null) return ellipse.getCenterY();
		
		Point2D src = new Point2D.Double(ellipse.getCenterX(), 
					ellipse.getCenterY());
		Point2D dest = new Point2D.Double();
		t.transform(src, dest);
		return dest.getY();
	}
	
	/**
	 * Transforms the figure.
	 *
	 * @param tx the transformation.
	 */
	public void transform(AffineTransform tx)
	{
		if (AttributeKeys.TRANSFORM.get(this) == null)
		{
			AttributeKeys.TRANSFORM.basicSetClone(this, tx);
		}
		else
		{
			AffineTransform t = AttributeKeys.TRANSFORM.getClone(this);
			t.preConcatenate(tx);
			AttributeKeys.TRANSFORM.basicSet(this, t);
		}
	}
	
	
	/*
	 * UNDO/REDO methods.
	 * @see org.jhotdraw.draw.Figure#restoreTransformTo(java.lang.Object)
	 */
	public void restoreTransformTo(Object geometry)
	{
		Object[] restoreData = (Object[]) geometry;
		ellipse=(Ellipse2D.Double) ((Ellipse2D.Double) restoreData[0]).clone();
		SVGAttributeKeys.TRANSFORM.basicSetClone(this, (AffineTransform) restoreData[1]);
		SVGAttributeKeys.FILL_GRADIENT.basicSetClone(this, (Gradient) restoreData[2]);
		SVGAttributeKeys.STROKE_GRADIENT.basicSetClone(this, (Gradient) restoreData[3]);
		invalidate();
	}
	
	/*
	 * UNDO/REDO methods.
	 * @see org.jhotdraw.draw.Figure#getTransformRestoreData(java.lang.Object)
	 */
	public Object getTransformRestoreData()
	{
		return new Object[] 
		      { 
				ellipse.clone(),
				SVGAttributeKeys.TRANSFORM.getClone(this),
				SVGAttributeKeys.FILL_GRADIENT.getClone(this),
				SVGAttributeKeys.STROKE_GRADIENT.getClone(this), 
		      };
	}
	
	
	/**
	 * Clones the figure.
	 */
	public RotateEllipseFigure clone()
	{
		RotateEllipseFigure that = (RotateEllipseFigure) super.clone();
		that.ellipse = (Ellipse2D.Double) this.ellipse.clone();
		that.cachedTransformedShape = null;
		return that;
	}
	
	// EVENT HANDLING
	public boolean isEmpty()
	{
		Rectangle2D.Double b = getBounds();
		return b.width <= 0|| b.height <= 0;
	}
	
	/**
	 * Invalidate the figure and remove the cachedTransformedShape, this means
	 * that the figures geometry has changed and it should be redrawn.
	 * @see AbstractAttributedFigure#invalidate()
	 */
	public void invalidate()
	{			
		super.invalidate();
		cachedTransformedShape = null;
	}
	
	/*
	 * Drawing code.
	 */
	/**
	 * Draws the figure 
	 * @see AbstractAttributedFigure#draw(Graphics2D)
	 */
	public void draw(Graphics2D g) { drawFigure(g); }
	
	/**
	 * This method is invoked before the rendered image of the figure is
	 * a composite figure.
	 * 
	 * @param g The graphics context.
	 */
	private void drawFigure(Graphics2D g)
	{
		Paint paint = SVGAttributeKeys.getFillPaint(this);
		if (paint != null)
		{
			g.setPaint(paint);
			drawFill(g);
		}
		paint = SVGAttributeKeys.getStrokePaint(this);
		if (paint != null && AttributeKeys.STROKE_WIDTH.get(this) > 0)
		{
			g.setPaint(paint);
			g.setStroke(AttributeKeys.getStroke(this));
			drawStroke(g);
		}
	}
	
	/**
	 * Draws the fill of the ellipse.
	 */
	protected void drawFill(Graphics2D g)
	{
		g.fill(getTransformedShape());
	}
	
	/**
	 * Draws the stroke of the ellipse.
	 */
	protected void drawStroke(Graphics2D g)
	{
		g.draw(getTransformedShape());
	}

	/*
	 * Connecting to the figure. Since this figure does not allow connections
	 * it will always return NULL.
	 */
	
	/**
	 * Return false as no connections can exist.
	 */
	public boolean canConnect() { return false; }
	
	/**
	 * Return null as no connections can exist.
	 */
	public Connector findConnector(Point2D.Double p, ConnectionFigure prototype)
	{
		return null; 
	}
	
	/**
	 * Return null as no connections can exist.
	 */
	public Connector findCompatibleConnector(Connector c,
			boolean isStartConnector)
	{
		return null; 
	}

}


