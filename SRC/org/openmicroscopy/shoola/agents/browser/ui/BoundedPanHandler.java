/*
 * org.openmicroscopy.shoola.agents.browser.ui.BoundedPanHandler.java
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

/*------------------------------------------------------------------------------
 *
 * Written by:    Jeff Mellen <jeffm@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */
 
package org.openmicroscopy.shoola.agents.browser.ui;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PPanEventHandler;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PDimension;

/**
 * A pan handler that ensures that the panning only takes place within the
 * bounds of the object, or the bounds with a specific margin.
 *
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class BoundedPanHandler extends PPanEventHandler
                               implements RegionSensitive
{
    protected Rectangle2D bounds;
    
    /**
     * The default horizontal margin (distance the camera view can offset
     * the union of dataset images)
     */
    public static final double DEFAULT_HMARGIN = 10;
    
    /**
     * The default vertical margin (distance the camera view can offset the
     * union of dataset images)
     */
    public static final double DEFAULT_VMARGIN = 10;
    
    /**
     * The horizontal margins (see DEFAULT_HMARGIN)
     */
    protected double hMargins = DEFAULT_HMARGIN;
    
    /**
     * The vertical margins (see DEFAULT_VMARGIN)
     */
    protected double vMargins = DEFAULT_VMARGIN;
    
    /**
     * Constructs a bounded pan handler with the specified region to pan
     * with.
     * 
     * @param region The region in which to bound panning.
     */
    public BoundedPanHandler(Rectangle2D region)
    {
        if(region == null)
        {
            bounds = new Rectangle2D.Double(0,0,0,0);
        }
        
        bounds = region;
    }
    
    /**
     * Sets the horizontal margins to the specified value.
     * @param margins See above.
     */
    public void setHorizontalMargins(double margins)
    {
        this.hMargins = margins;
    }
    
    /**
     * Sets the vertical margins to the specified value.
     * @param margins See above.
     */
    public void setVerticalMargins(double margins)
    {
        this.vMargins = margins;
    }
    
    /**
     * Returns the active region.
     * @return See above.
     */
    public Rectangle2D getActiveRegion()
    {
        return bounds;
    }
    
    /**
     * Sets the target panning region to the specified region.  A pan will
     * not occur if the it means the pan pushes the viewable window past the
     * image region plus specified margins.
     * 
     * @param region The legal panning region.
     */
    public void setActiveRegion(Rectangle2D region)
    {
        if(region == null)
        {
            return;
        }
        else
        {
            this.bounds = region;
        }
    }
    
    /**
     * Overrides the 
     */
    protected void pan(PInputEvent e)
    {
        PCamera c = e.getCamera();
        // OK, so first, get the delta and see if it's going to muck with any
        // of the points.
        PDimension delta = e.getDelta();
        double diffX = delta.getWidth();
        double diffY = delta.getHeight();
        
        makeBoundedCameraMotion(c,diffX,diffY);
    }
    
    /**
     * Move the camera within the constraints of the bounding.  Call this
     * instead of c.translateView(double,double).
     * 
     * @param deltaX The desired dx.
     * @param deltaY The desired dy.
     */
    protected void makeBoundedCameraMotion(PCamera c,
                                           double deltaX, double deltaY)
    {
        PBounds pBounds = c.getViewBounds();
        Rectangle2D rBounds = pBounds.getBounds2D();

        double wOffset = bounds.getX();
        double nOffset = bounds.getY();
        double eOffset = bounds.getX()+bounds.getWidth();
        double sOffset = bounds.getY()+bounds.getHeight();

        double cWest = rBounds.getX();
        double cNorth = rBounds.getY();
        double cEast = rBounds.getX()+rBounds.getWidth();
        double cSouth = rBounds.getY()+rBounds.getHeight();
        
        double fdX = 0;
        double fdY = 0;

        // check horizontal panning bounds
        if((-deltaX+cWest < wOffset - hMargins) ||
           (-deltaX+cEast > eOffset + hMargins))
        {
            fdX = 0;
        }
        else
        {
            fdX = deltaX;
        }

        // check vertical panning bounds
        if((-deltaY+cNorth < nOffset - vMargins) ||
           (-deltaY+cSouth > sOffset + vMargins))
        {
            fdY = 0;
        }
        else
        {
            fdY = deltaY;
        }

        c.translateView(fdX,fdY);
    }
    
    /**
     * Mirrors the same autopan behavior as the default PPanEventHandler,
     * but stops when it hits a barrier.
     */
    protected void dragActivityStep(PInputEvent aEvent)
    {
        if(!getAutopan()) return;
        
        PCamera c = aEvent.getCamera();
        PBounds b = c.getBoundsReference();
        Point2D l = aEvent.getPositionRelativeTo(c);
        int outcode = b.outcode(l);
        PDimension delta = new PDimension();
        
        // just like PPanEventHandler.dragActivityStep...
        // this does the funky slowdown effect.
        if((outcode & Rectangle.OUT_TOP) != 0)
        {
            delta.height =
                super.validatePanningSpeed(-1.0 - 
                                           (0.5 * Math.abs(l.getY()-b.getY())));
        }
        else if((outcode & Rectangle.OUT_BOTTOM) != 0)
        {
            delta.height =
                super.validatePanningSpeed(1.0 +
                    (0.5 * Math.abs(l.getY() - (b.getY() + b.getHeight()))));
        }
        
        if((outcode & Rectangle.OUT_RIGHT) != 0) {
            delta.width =
                super.validatePanningSpeed(1.0 +
                    (0.5 * Math.abs(l.getX() - (b.getX() + b.getWidth()))));
        }
        else if((outcode & Rectangle.OUT_LEFT) != 0)
        {
            delta.width =
                super.validatePanningSpeed(-1.0 -
                                           (0.5 * Math.abs(l.getX()-b.getX())));
        }
        
        c.localToView(delta);
        
        // and now, do the actual check (stop on exit pan bounds)
        if(delta.width != 0 || delta.height != 0)
        {
            makeBoundedCameraMotion(c,delta.width,delta.height);
        }
        
    }
}

