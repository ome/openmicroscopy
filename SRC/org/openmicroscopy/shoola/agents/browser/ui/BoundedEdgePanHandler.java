/*
 * org.openmicroscopy.shoola.agents.browser.ui.BoundedEdgePanHandler.java
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

import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Timer;
import java.util.TimerTask;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * A panner that is triggered by mousing over the edge, although still
 * bounded.
 *
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class BoundedEdgePanHandler extends PBasicInputEventHandler
                                   implements RegionSensitive
{
    /**
     * The default distance (in pixels) away from the browser edge that
     * should trigger the pan.
     */
    public static int DEFAULT_MARGIN = 20;
    
    /**
     * The default time (in milliseconds) to wait before triggering the
     * scroll.  This prevents inadvertent scrolling when a user moves the
     * mouse out of the window.
     */
    public static int DEFAULT_WAITTIME = 500;
    
    /**
     * The default interval (in milliseconds) to trigger each additional pan.
     */
    public static int DEFAULT_INTERVAL = 50;
    
    /**
     * The default number of pixels to advance with each pan event if
     * DEFAULT_INVERSE_INCREASE is false..
     */
    public static final int DEFAULT_PANDIST = 4;
    
    /**
     * Whether or not to increase forward speed as the user moves closer
     * to the edge.
     */
    public static boolean DEFAULT_INVERSE_INCREASE = true;
    
    /**
     * activation edge distance.
     */
    protected int edgeDistance = DEFAULT_MARGIN;
    
    /**
     * inverse increase directive.
     */
    protected boolean inverseIncrease = DEFAULT_INVERSE_INCREASE;
    
    /**
     * advance time interval.
     */
    protected int advanceInterval = DEFAULT_INTERVAL;
    
    /**
     * per-interval pixel advance.
     */
    protected int advanceDistance = 1;
    
    /**
     * Specifies that the hotspot region is invalid (margins too large or
     * window too small)
     */
    protected boolean invalidRegion = false;
    
    /**
     * The state ahead of starting the pan.
     */
    protected boolean activated = false;
    
    /**
     * The activation timer used to trigger the pan.
     */
    protected Timer activationTimer;
    
    /**
     * The timer task used to execute the pan.
     */
    protected PanTask activeTask;
    
    /**
     * The area in which the panner can pan.
     */
    protected Rectangle2D areaBounds;
    
    /**
     * cached camera bounds.
     */
    protected Rectangle2D cachedBounds;
    
    /**
     * Cached exclude region.
     */
    protected Rectangle2D excludeRegion;
    
    /**
     * cached top hotspot bounds.
     */
    protected Rectangle2D nRegion;
    
    /**
     * cached left hotspot bounds.
     */
    protected Rectangle2D wRegion;
    
    /**
     * cached right hotspot bounds.
     */
    protected Rectangle2D eRegion;
    
    /**
     * cached bottom hotspot bounds.
     */
    protected Rectangle2D sRegion;
    
    /**
     * Pan buffer extent (horizontal)
     */
    protected double hMargins = 10;
    
    /**
     * Pan buffer extent (vertical)
     */
    protected double vMargins = 10;
    
    /**
     * (for now, dummy constructor)
     *
     */
    public BoundedEdgePanHandler()
    {
        cachedBounds = new Rectangle2D.Double(0,0,0,0);
        areaBounds = new Rectangle2D.Double(0,0,0,0);
        nRegion = new Rectangle2D.Double(0,0,0,0);
        excludeRegion = new Rectangle2D.Double(0,0,0,0);
        wRegion = new Rectangle2D.Double(0,0,0,0);
        eRegion = new Rectangle2D.Double(0,0,0,0);
        sRegion = new Rectangle2D.Double(0,0,0,0);
        activationTimer = new Timer();
    }
    
    /**
     * Gets the active region-- the region that the pan must be within.
     * @see org.openmicroscopy.shoola.agents.browser.ui.RegionSensitive#getActiveRegion()
     */
    public Rectangle2D getActiveRegion()
    {
        return areaBounds;
    }
    
    /**
     * Sets the active region-- the region that the pan must be within.
     * @see org.openmicroscopy.shoola.agents.browser.ui.RegionSensitive#setActiveRegion(java.awt.geom.Rectangle2D)
     */
    public void setActiveRegion(Rectangle2D region)
    {
        if(region != null)
        {
            areaBounds = region;
        }
        else
        {
            areaBounds = new Rectangle2D.Double(0,0,0,0);
        }
    }


    
    /**
     * Reenter the hotspot (pan) mouse-over regions.
     * @param cameraBounds
     */
    protected void recalculateBounds(Rectangle2D cameraBounds)
    {
        // OK, so the hotspots will be the margins *unless* for some reason
         // someone was stupid enough to set the margins larger than the
         // bounds, in which case we'll do nothing because someone was stupid
         // enough to pull off that kind of nonsense.  Heavens to Betsy.
        if(cameraBounds.getWidth() <= edgeDistance*2 ||
           cameraBounds.getHeight() <= edgeDistance*2)
        {
            invalidRegion = true;
            return;
        }
        else invalidRegion = false;
        
        nRegion = new Rectangle2D.Double(cameraBounds.getX(),cameraBounds.getY(),
                                         cameraBounds.getWidth(),edgeDistance);
        
        wRegion = new Rectangle2D.Double(cameraBounds.getX(),cameraBounds.getY(),
                                         edgeDistance,cameraBounds.getHeight());
        
        eRegion = new Rectangle2D.Double(cameraBounds.getWidth()-edgeDistance,
                                         cameraBounds.getY(),
                                         edgeDistance,cameraBounds.getHeight());
        
        sRegion = new Rectangle2D.Double(cameraBounds.getX(),
                                         cameraBounds.getHeight()-edgeDistance,
                                         cameraBounds.getWidth(),edgeDistance);
        
        excludeRegion =
            new Rectangle2D.Double(cameraBounds.getX()+edgeDistance,
                                   cameraBounds.getY()+edgeDistance,
                                   cameraBounds.getWidth()-(edgeDistance*2),
                                   cameraBounds.getHeight()-(edgeDistance*2));
        
        cachedBounds = cameraBounds;
        System.err.println("nRegion: " + nRegion);
        System.err.println("wRegion: " + wRegion);
        System.err.println("eRegion: " + eRegion);
        System.err.println("sRegion: " + sRegion);
        System.err.println("excludeRegion: " + excludeRegion);
        
    }
    
    /**
     * Overrides the mouseMoved method for Piccolo event handlers.
     */
    public void mouseMoved(PInputEvent e)
    {
        PCamera camera = e.getCamera();
        Point2D point = e.getCanvasPosition();
        Rectangle2D totalBounds = camera.getBounds().getBounds2D();
        
        // optimized (save)
        if(!totalBounds.equals(cachedBounds))
        {
            recalculateBounds(totalBounds);
        }
        
        // do nothing if this is the case
        if(invalidRegion)
        {
            return;
        }
        
        System.err.println(cachedBounds);
        System.err.println(point);
        if(!cachedBounds.contains(point))
        {
            activationTimer.cancel();
            activeTask.cancel();
            activated = false;
        }
        
        // stop panning or cancel the pan
        if(excludeRegion.contains(point))
        {
            if(activated)
            {
                activationTimer.cancel();
                activeTask.cancel();
                activated = false;
            }
            return; // and bail
        }
        
        // time to assign/change panning parameters if necessary
        double dX = 0;
        double dY = 0;
        
        // ok: max per second scroll = 20 pixels.  period.
        if(nRegion.contains(point))
        {
            if(inverseIncrease)
            {
                dY = -20 * ((cachedBounds.getY() +
                             edgeDistance -
                             point.getY())
                             /
                             (double)edgeDistance);
            }
            else
            {
                dY = -advanceDistance;
            }
        }
        else if(sRegion.contains(point))
        {
            if(inverseIncrease)
            {
                dY = 20 * ((point.getY() -
                           (cachedBounds.getY()+cachedBounds.getHeight()-
                            edgeDistance))
                          /
                          (double)edgeDistance);
            }
            else
            {
                dY = advanceDistance;
            }
        }
        
        if(wRegion.contains(point))
        {
            if(inverseIncrease)
            {
                dY = -20 * ((cachedBounds.getX() +
                             edgeDistance -
                             point.getX())
                             /
                             (double)edgeDistance);
            }
            else
            {
                dX = -advanceDistance;
            }
        }
        else if(eRegion.contains(point))
        {
            if(inverseIncrease)
            {
                dX = 20 * ((point.getX() -
                           (cachedBounds.getX()+cachedBounds.getWidth()-
                            edgeDistance))
                          /
                          (double)edgeDistance);
            }
            else
            {
                dX = advanceDistance;
            }
        }
        
        System.err.println("incoming: "+dX+","+dY);
        Point2D xForm = camera.localToView(new Point2D.Double(dX,dY));
        dX = xForm.getX();
        dY = xForm.getY();
        
        if(!activated)
        {
            activationTimer = new Timer();
            activeTask = new PanTask(camera,dX,dY);
            activationTimer.scheduleAtFixedRate(activeTask,DEFAULT_WAITTIME,
                                                advanceInterval);
            activated = true;
        }
        else
        {
            activeTask.setDX(dX);
            activeTask.setDY(dY);
        }
        
    }
    
    

    /**
     * Pan task that runs at the behest of the panning timer.
     *
     * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
     * <b>Internal version:</b> $Revision$ $Date$
     * @version 2.2
     * @since OME2.2
     */
    protected class PanTask extends TimerTask
    {
        protected double panDX = 0;
        protected double panDY = 0;
        
        protected PCamera camera;
        
        PanTask(PCamera camera, double dX, double dY)
        {
            System.err.println("task:" + dX + "," + dY);
            this.camera = camera;
            panDX = dX;
            panDY = dY;
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

            System.err.println(areaBounds);
            double wOffset = areaBounds.getX();
            double nOffset = areaBounds.getY();
            double eOffset = areaBounds.getX()+areaBounds.getWidth();
            double sOffset = areaBounds.getY()+areaBounds.getHeight();

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
        
        
        public void run()
        {
            makeBoundedCameraMotion(camera,-panDX,-panDY);
        }
        
        public void setDX(double dx)
        {
            this.panDX = dx;
        }
        
        public void setDY(double dy)
        {
            this.panDY = dy;
        }
    }
}
