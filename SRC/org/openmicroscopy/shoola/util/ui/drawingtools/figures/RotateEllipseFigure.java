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
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.LinkedList;

//Third-party libraries

//Application-internal dependencies
import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.ConnectionFigure;
import org.jhotdraw.draw.Connector;
import org.jhotdraw.draw.Handle;
import org.jhotdraw.draw.ResizeHandleKit;
import org.jhotdraw.draw.TransformHandleKit;
import org.jhotdraw.geom.Geom;
import org.jhotdraw.samples.svg.Gradient;
import org.jhotdraw.samples.svg.SVGAttributeKeys;
import org.jhotdraw.samples.svg.figures.SVGAttributedFigure;

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
public class RotateEllipseFigure  extends SVGAttributedFigure  
{	
	 	protected Ellipse2D.Double ellipse;
	    /**
	     * This is used to perform faster drawing and hit testing.
	     */
	    protected Shape cachedTransformedShape;
	    
	    /** Creates a new instance. */
	    public RotateEllipseFigure() {
	        this(0, 0, 0, 0);
	    }
	    
	    public RotateEllipseFigure(double x, double y, double width, double height) {
	        ellipse = new Ellipse2D.Double(x, y, width, height);
	        SVGAttributeKeys.setDefaults(this);
	    }
	    
	    // DRAWING
	    protected void drawFill(Graphics2D g) {
	        g.fill(ellipse);
	        //g.fill(getTransformedShape());
	    }
	    
	    protected void drawStroke(Graphics2D g) {
	        g.draw(ellipse);
	        /*
	        if (TRANSFORM.get(this) == null) {
	            g.draw(ellipse);
	        } else {
	            AffineTransform savedTransform = g.getTransform();
	            g.transform(TRANSFORM.get(this));
	            g.draw(ellipse);
	            g.setTransform(savedTransform);
	        }*/
	    }
	    // SHAPE AND BOUNDS
	    public double getX() {
	        return ellipse.x;
	    }
	    public double getY() {
	        return ellipse.y;
	    }
	    public double getWidth() {
	        return getTransformedShape().getBounds().getWidth();
	    }
	    public double getHeight() {
	        return getTransformedShape().getBounds().getHeight();
	    }
	    
	    public Rectangle2D.Double getBounds() {
	        return (Rectangle2D.Double) ellipse.getBounds2D();
	    }
	    
	    @Override public Rectangle2D.Double getDrawingArea() {
	        Rectangle2D rx = getTransformedShape().getBounds2D();
	        Rectangle2D.Double r = (rx instanceof Rectangle2D.Double) ? (Rectangle2D.Double) rx : new Rectangle2D.Double(rx.getX(), rx.getY(), rx.getWidth(), rx.getHeight());
	        if (AttributeKeys.TRANSFORM.get(this) == null) {
	            double g = SVGAttributeKeys.getPerpendicularHitGrowth(this) * 2;
	            Geom.grow(r, g, g);
	        } else {
	            double strokeTotalWidth = AttributeKeys.getStrokeTotalWidth(this);
	            double width = strokeTotalWidth / 2d;
	            width *= Math.max(AttributeKeys.TRANSFORM.get(this).getScaleX(), AttributeKeys.TRANSFORM.get(this).getScaleY());
	            Geom.grow(r, width, width);
	        }
	        return r;
	    }
	    /**
	     * Checks if a Point2D.Double is inside the figure.
	     */
	    public boolean contains(Point2D.Double p) {
	        // XXX - This does not take the stroke width into account!
	        return getTransformedShape().contains(p);
	    }
	    protected Shape getTransformedShape() {
	        if (cachedTransformedShape == null) {
	            if (AttributeKeys.TRANSFORM.get(this) == null) {
	                cachedTransformedShape = ellipse;
	            } else {
	                cachedTransformedShape = AttributeKeys.TRANSFORM.get(this).createTransformedShape(ellipse);
	            }
	        }
	        return cachedTransformedShape;
	    }
	    public void setBounds(Point2D.Double anchor, Point2D.Double lead) {
	        ellipse.x = Math.min(anchor.x, lead.x);
	        ellipse.y = Math.min(anchor.y , lead.y);
	        ellipse.width = Math.max(0.1, Math.abs(lead.x - anchor.x));
	        ellipse.height = Math.max(0.1, Math.abs(lead.y - anchor.y));
	    }
	    /**
	     * Transforms the figure.
	     *
	     * @param tx the transformation.
	     */
	    public void transform(AffineTransform tx) {
	        if (AttributeKeys.TRANSFORM.get(this) != null ||
	                (tx.getType() & (AffineTransform.TYPE_TRANSLATION)) != tx.getType()) {
	            if (AttributeKeys.TRANSFORM.get(this) == null) {
	            	AttributeKeys.TRANSFORM.basicSetClone(this, tx);
	            } else {
	                AffineTransform t = AttributeKeys.TRANSFORM.getClone(this);
	                t.preConcatenate(tx);
	                AttributeKeys.TRANSFORM.basicSet(this, t);
	            }
	        } else {
	            Point2D.Double anchor = getStartPoint();
	            Point2D.Double lead = getEndPoint();
	            setBounds(
	                    (Point2D.Double) tx.transform(anchor, anchor),
	                    (Point2D.Double) tx.transform(lead, lead)
	                    );
	            if (SVGAttributeKeys.FILL_GRADIENT.get(this) != null && 
	                    ! SVGAttributeKeys.FILL_GRADIENT.get(this).isRelativeToFigureBounds()) {
	                Gradient g = SVGAttributeKeys.FILL_GRADIENT.getClone(this);
	                g.transform(tx);
	                SVGAttributeKeys.FILL_GRADIENT.basicSet(this, g);
	            }
	            if (SVGAttributeKeys.STROKE_GRADIENT.get(this) != null &&
	                    ! SVGAttributeKeys.STROKE_GRADIENT.get(this).isRelativeToFigureBounds()) {
	                Gradient g = SVGAttributeKeys.STROKE_GRADIENT.getClone(this);
	                g.transform(tx);
	                SVGAttributeKeys.STROKE_GRADIENT.basicSet(this, g);
	            }
	        }
	        invalidate();
	    }
	    public void restoreTransformTo(Object geometry) {
	        Object[] restoreData = (Object[]) geometry;
	        ellipse = (Ellipse2D.Double) ((Ellipse2D.Double) restoreData[0]).clone();
	        SVGAttributeKeys.TRANSFORM.basicSetClone(this, (AffineTransform) restoreData[1]);
	        SVGAttributeKeys.FILL_GRADIENT.basicSetClone(this, (Gradient) restoreData[2]);
	        SVGAttributeKeys.STROKE_GRADIENT.basicSetClone(this, (Gradient) restoreData[3]);
	        invalidate();
	    }
	    
	    public Object getTransformRestoreData() {
	        return new Object[] {
	            ellipse.clone(),
	            SVGAttributeKeys.TRANSFORM.getClone(this),
	            SVGAttributeKeys.FILL_GRADIENT.getClone(this),
	            SVGAttributeKeys.STROKE_GRADIENT.getClone(this),
	        };
	    }
	    
	    
	    // ATTRIBUTES
	    // EDITING
	    @Override public Collection<Handle> createHandles(int detailLevel) {
	        LinkedList<Handle> handles = new LinkedList<Handle>();
	        switch (detailLevel % 2) {
	            case 0 :
	                ResizeHandleKit.addResizeHandles(this, handles);
	                break;
	            case 1 :
	                TransformHandleKit.addTransformHandles(this, handles);
	                break;
	            default:
	                break;
	        }
	        return handles;
	    }
	    // CONNECTING
	    public boolean canConnect() {
	        return false; // SVG does not support connecting
	    }
	    public Connector findConnector(Point2D.Double p, ConnectionFigure prototype) {
	        return null; // SVG does not support connectors
	    }
	    public Connector findCompatibleConnector(Connector c, boolean isStartConnector) {
	        return null; // SVG does not support connectors
	    }
	    // COMPOSITE FIGURES
	    // CLONING
	    public RotateEllipseFigure clone() {
	    	RotateEllipseFigure that = (RotateEllipseFigure) super.clone();
	        that.ellipse = (Ellipse2D.Double) this.ellipse.clone();
	        that.cachedTransformedShape = null;
	        return that;
	    }
	    
	    // EVENT HANDLING
	    public boolean isEmpty() {
	        Rectangle2D.Double b = getBounds();
	        return b.width <= 0 || b.height <= 0;
	    }
	    @Override public void invalidate() {
	        super.invalidate();
	        cachedTransformedShape = null;
	    }
}


