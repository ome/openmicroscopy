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
import static org.jhotdraw.draw.AttributeKeys.STROKE_WIDTH;
import static org.jhotdraw.draw.AttributeKeys.TRANSFORM;

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

//Application-internal dependencies
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
	 * Create a new rotated Ellipse with position x, y and width and height
	 * @param x see above.
	 * @param y see above.
	 * @param width see above.
	 * @param height see above.
	 */
	public RotateEllipseFigure(double x, double y, double width, double height)
	{
		ellipse=new Ellipse2D.Double(x, y, width, height);
		AffineTransform transform = new AffineTransform();
		transform.setToIdentity();
		AttributeKeys.TRANSFORM.set(this, transform);
	}
		    
	/**
	 *  
	 */
	public void setAttribute(AttributeKey key, Object newValue)
	{
		if (key == TRANSFORM) 
		{
	    	invalidate();
	    }
        super.setAttribute(key, newValue);
	} 
	    
		
	public Rectangle2D.Double getBounds()
	{
		return new Rectangle2D.Double(ellipse.getBounds2D().getX(),
			ellipse.getBounds2D().getY(),ellipse.getBounds2D().getWidth(),
			ellipse.getBounds2D().getHeight());
	}
	
	@Override
	public Rectangle2D.Double getDrawingArea()
	{
		Rectangle2D rx=getTransformedShape().getBounds2D();
		Rectangle2D.Double r=
				(rx instanceof Rectangle2D.Double) ? (Rectangle2D.Double) 
						rx : new Rectangle2D.Double(rx.getX(), rx.getY(), 
							rx.getWidth(), rx.getHeight());
		if (AttributeKeys.TRANSFORM.get(this)==null)
		{
			double g=SVGAttributeKeys.getPerpendicularHitGrowth(this)*2;
			Geom.grow(r, g, g);
		}
		else
		{
			double strokeTotalWidth=AttributeKeys.getStrokeTotalWidth(this);
			double width=strokeTotalWidth/2d;
			width*= Math.max(AttributeKeys.TRANSFORM.get(this).getScaleX(),
						AttributeKeys.TRANSFORM.get(this).getScaleY());
			Geom.grow(r, width, width);
		}
		return r;
	}
	
	/**
	 * Checks if a Point2D.Double is inside the figure.
	 */
	public boolean contains(Point2D.Double p)
	{
		// XXX - This does not take the stroke width into account!
		return getTransformedShape().contains(p);
	}
	
	public Ellipse2D.Double getTransformedEllipse()
	{
		if(AttributeKeys.TRANSFORM.get(this)==null)
			return ellipse;
		Ellipse2D.Double e = new Ellipse2D.Double(0,0,0,0);
		AffineTransform t = AttributeKeys.TRANSFORM.get(this);
		Point2D.Double  startW = (Point2D.Double)t.transform(new Point2D.Double(0, ellipse.getCenterY()), null);
		Point2D.Double  endW = (Point2D.Double)t.transform(new Point2D.Double(ellipse.getWidth(), ellipse.getCenterY()), null);
		Point2D.Double  startH = (Point2D.Double)t.transform(new Point2D.Double(ellipse.getCenterX(),0), null);
		Point2D.Double  endH = (Point2D.Double)t.transform(new Point2D.Double(ellipse.getCenterX(), ellipse.getHeight()), null);
		Point2D.Double lead = (Point2D.Double)t.transform(new Point2D.Double(0,0), null);
		e.width= Math.round(startW.distance(endW));
		e.height= Math.round(startH.distance(endH));
		e.x = Math.round(lead.getX());
		e.y = Math.round(lead.getY());
		return e;
		
	}
	
	protected Shape getTransformedShape()
	{
		if (AttributeKeys.TRANSFORM.get(this)==null)
		{
			cachedTransformedShape=ellipse;
		}
		else
		{
			cachedTransformedShape= AttributeKeys.TRANSFORM.get(this).createTransformedShape(ellipse);
		}
		return cachedTransformedShape; 
	}
	
	public Ellipse2D.Double getEllipse()
	{
		return ellipse;
	}
	
	@Override
	public Collection<Handle> createHandles(int detailLevel)
	{
		LinkedList<Handle> handles=new LinkedList<Handle>();
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
	
	public void setEllipse(double x, double y, double width, double height)
	{
		ellipse.x = x;
		ellipse.y = y;
		ellipse.width = width;
		ellipse.height = height;
		invalidate();
	}
	
	public void setBounds(Point2D.Double anchor, Point2D.Double lead)
	{
		ellipse.x=Math.min(anchor.x, lead.x);
		ellipse.y=Math.min(anchor.y, lead.y);
		ellipse.width=Math.max(0.1, Math.abs(lead.x-anchor.x));
		ellipse.height=Math.max(0.1, Math.abs(lead.y-anchor.y));
		invalidate();
	}

	public double getHeight()
	{
		return getTransformedEllipse().getHeight();
	}

	public double getWidth()
	{
		return getTransformedEllipse().getWidth();
	}
	
	public void setWidth(double newWidth)
	{
		if(AttributeKeys.TRANSFORM.get(this)==null)
			return;
		AffineTransform t = AttributeKeys.TRANSFORM.get(this);
		double centreX = getCentreX();
		double centreY = getCentreY();
		double height = getHeight();
		double[] matrix = new double[6];
		t.getMatrix(matrix);
		matrix[4] = 0;
		matrix[5] = 0;
		AffineTransform aT = new AffineTransform(matrix);
		Point2D.Double a = (Point2D.Double)aT.transform(new Point2D.Double(1,0), null);
		double theta = Math.asin(a.y/Math.sqrt(a.x*a.x+a.y*a.y));
		AffineTransform newT = new AffineTransform();
		newT.setToRotation(theta);
		
		Ellipse2D.Double newEllipse = new Ellipse2D.Double(0,0,newWidth, height);
		
		Shape rotatedShape = newT.createTransformedShape(newEllipse);
		double rotatedShapeCentreX = rotatedShape.getBounds2D().getCenterX();
		double rotatedShapeCentreY = rotatedShape.getBounds2D().getCenterY();
		
		double diffX = centreX-rotatedShapeCentreX;
		double diffY = centreY-rotatedShapeCentreY;
	
		Point2D.Double lead = new Point2D.Double(diffX, diffY);
		
		AffineTransform rT = AffineTransform.getTranslateInstance(lead.x, lead.y);
		rT.concatenate(newT);
		
		ellipse = newEllipse;
		AttributeKeys.TRANSFORM.set(this, rT);
		invalidate();
	}
	
	public void setHeight(double newHeight)
	{
		if(AttributeKeys.TRANSFORM.get(this)==null)
			return;
		AffineTransform t = AttributeKeys.TRANSFORM.get(this);
		double centreX = getCentreX();
		double centreY = getCentreY();
		double width = getWidth();
		double[] matrix = new double[6];
		t.getMatrix(matrix);
		matrix[4] = 0;
		matrix[5] = 0;
		AffineTransform aT = new AffineTransform(matrix);
		Point2D.Double a = (Point2D.Double)aT.transform(new Point2D.Double(1,0), null);
		double theta = Math.asin(a.y/Math.sqrt(a.x*a.x+a.y*a.y));
		AffineTransform newT = new AffineTransform();
		newT.setToRotation(theta);
		
		Ellipse2D.Double newEllipse = new Ellipse2D.Double(0,0,width, newHeight);
		
		Shape rotatedShape = newT.createTransformedShape(newEllipse);
		double rotatedShapeCentreX = rotatedShape.getBounds2D().getCenterX();
		double rotatedShapeCentreY = rotatedShape.getBounds2D().getCenterY();
		
		double diffX = centreX-rotatedShapeCentreX;
		double diffY = centreY-rotatedShapeCentreY;
	
		Point2D.Double lead = new Point2D.Double(diffX, diffY);
		
		AffineTransform rT = AffineTransform.getTranslateInstance(lead.x, lead.y);
		rT.concatenate(newT);
		
		ellipse = newEllipse;
		AttributeKeys.TRANSFORM.set(this, rT);
		invalidate();
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
	 * Transforms the figure.
	 *
	 * @param tx the transformation.
	 */
	public void transform(AffineTransform tx)
	{
		if (AttributeKeys.TRANSFORM.get(this)==null)
		{
			AttributeKeys.TRANSFORM.basicSetClone(this, tx);
		}
		else
		{
			AffineTransform t=AttributeKeys.TRANSFORM.getClone(this);
			t.preConcatenate(tx);
			AttributeKeys.TRANSFORM.basicSet(this, t);
		}
		/*if (AttributeKeys.TRANSFORM.get(this)!=null ||
				(tx.getType()&(AffineTransform.TYPE_TRANSLATION)) != tx.getType())
			{
				if (AttributeKeys.TRANSFORM.get(this)==null)
				{
					AttributeKeys.TRANSFORM.basicSetClone(this, tx);
				}
				else
				{
					AffineTransform t=AttributeKeys.TRANSFORM.getClone(this);
					t.preConcatenate(tx);
					AttributeKeys.TRANSFORM.basicSet(this, t);
				}
			}
			else
			{
				Point2D.Double anchor=getStartPoint();
				Point2D.Double lead=getEndPoint();
				setBounds((Point2D.Double) tx.transform(anchor, anchor),
					(Point2D.Double) tx.transform(lead, lead));
				if (SVGAttributeKeys.FILL_GRADIENT.get(this)!=null
					&&!SVGAttributeKeys.FILL_GRADIENT.get(this).isRelativeToFigureBounds())
				{
					Gradient g=SVGAttributeKeys.FILL_GRADIENT.getClone(this);
					g.transform(tx);
					SVGAttributeKeys.FILL_GRADIENT.basicSet(this, g);
				}
				if (SVGAttributeKeys.STROKE_GRADIENT.get(this)!=null
						&&!SVGAttributeKeys.STROKE_GRADIENT.get(this).isRelativeToFigureBounds())
				{
					Gradient g=SVGAttributeKeys.STROKE_GRADIENT.getClone(this);
					g.transform(tx);
					SVGAttributeKeys.STROKE_GRADIENT.basicSet(this, g);
				}
			}
			invalidate();*/
	}
	
	
	/*
	 * UNDO/REDO methods.
	 * @see org.jhotdraw.draw.Figure#restoreTransformTo(java.lang.Object)
	 */
	public void restoreTransformTo(Object geometry)
	{
		Object[] restoreData=(Object[]) geometry;
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
	 * Clone the figure.
	 */
	public RotateEllipseFigure clone()
	{
		RotateEllipseFigure that=(RotateEllipseFigure) super.clone();
		that.ellipse=(Ellipse2D.Double) this.ellipse.clone();
		that.cachedTransformedShape=null;
		return that;
	}
	
	// EVENT HANDLING
	public boolean isEmpty()
	{
		Rectangle2D.Double b=getBounds();
		return b.width<=0||b.height<=0;
	}
	
	@Override
	/**
	 * Invalidate the figure and remove the cachedTransformedShape, this means
	 * that the figures geometry has changed and it should be redrawn.
	 */
	public void invalidate()
	{
		super.invalidate();
		cachedTransformedShape=null;
	}
	
	/*
	 * Drawing code.
	 */
	/**
	 * Draw the figure 
	 */
	public void draw(Graphics2D g)
	{
		drawFigure(g);
	}
	
	/**
	 * This method is invoked before the rendered image of the figure is
	 * a composite figure.
	 */
	public void drawFigure(Graphics2D g)
	{
		Paint paint=SVGAttributeKeys.getFillPaint(this);
		if (paint!=null)
		{
			g.setPaint(paint);
			drawFill(g);
		}
		paint=SVGAttributeKeys.getStrokePaint(this);
		if (paint!=null&&STROKE_WIDTH.get(this)>0)
		{
			g.setPaint(paint);
			g.setStroke(AttributeKeys.getStroke(this));
			drawStroke(g);
		}
	}
	
	/**
	 * Draw the fill of the ellipse.
	 */
	protected void drawFill(Graphics2D g)
	{
		g.fill(getTransformedShape());
	}
	
	/**
	 * Draw the stroke of the ellipse.
	 */
	protected void drawStroke(Graphics2D g)
	{
		
		g.draw(getTransformedShape());
	}

	/*
	 * Connecting to the figure. Since this figure does not allow connecitons
	 * it will always return NULL.
	 */
	
	/**
	 * Return false as no connections can exist.
	 */
	public boolean canConnect()
	{
		return false; 
	}
	
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