/*
 * org.openmicroscopy.shoola.agents.browser.ui.BrowserCamera.java
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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * The class that encapsulates all the views/components of the top-level UI,
 * including palettes and panning hot spots.
 *
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class BrowserCamera implements RegionSensitive,
                                      HoverSensitive
{
    private Rectangle2D activeRegion;
    private Rectangle2D cameraBounds;
    private PCamera camera;
    private boolean invalidRegion;
    private int edgeDistance;
    private int panSpeed;
    
    private PNode leftNode;
    private PNode topNode;
    private PNode rightNode;
    private PNode bottomNode;
    
    private Set palettes;
    private List panNodeList;
    
    private PLayer paletteLayer;
    private PLayer panNodeLayer;
    
    /**
     * Constructs a browser overlay camera using the camera from the
     * specified view.
     * 
     * @param camera
     */
    public BrowserCamera(PCamera camera, Rectangle2D panRegion)
    {
        this.camera = camera;
        activeRegion = panRegion;
        edgeDistance = 20;
        panSpeed = 5;
        cameraBounds = camera.getBounds();
        paletteLayer = new PLayer();
        panNodeLayer = new PLayer();
        camera.addLayer(0,panNodeLayer);
        camera.addLayer(0,paletteLayer);
        panNodeList = new ArrayList();
        
        cameraResized(cameraBounds);
    }
    
    /**
     * Recompute the hotspot (pan) mouse-over regions.
     * @param cameraBounds
     */
    public void cameraResized(Rectangle2D cameraBounds)
    {
        // OK, so the hotspots will be the margins *unless* for some reason
         // someone was stupid enough to set the margins larger than the
         // bounds, in which case we'll do nothing because someone was stupid
         // enough to pull off that kind of nonsense.  Heavens to Betsy.
        if(cameraBounds == null ||
           cameraBounds.getWidth() <= edgeDistance*2 ||
           cameraBounds.getHeight() <= edgeDistance*2)
        {
            invalidRegion = true;
            return;
        }
        else invalidRegion = false;
        this.cameraBounds = cameraBounds;
        
        recalculatePanNodes();
    }
    
    protected void recalculatePanNodes()
    {
        for(Iterator iter = panNodeList.iterator(); iter.hasNext();)
        {
            camera.removeChild((PanCameraNode)iter.next());
        }
        panNodeList.clear();
        double netAmount =
            camera.localToView(new Point2D.Double(panSpeed,0)).getX();
        
        PanCameraNode nwNode =
            new PanCameraNode(new Rectangle2D.Double(0,0,
                                                     edgeDistance,
                                                     edgeDistance),
                              netAmount, netAmount);
        
        // make the happy fun activated arrow.
        GeneralPath arrow = new GeneralPath();
        arrow.moveTo(5,5);
        arrow.lineTo(35,5);
        arrow.lineTo(5,35);
        arrow.closePath();
        nwNode.setIndicatorShape(arrow);
                             
        PanCameraNode nNode =
            new PanCameraNode(new Rectangle2D.Double(
                                                     edgeDistance,0,
                                                     cameraBounds.getWidth()-
                                                     (edgeDistance*2),
                                                     edgeDistance),
                              0, netAmount);
                              
        arrow = new GeneralPath();
        arrow.moveTo((float)cameraBounds.getCenterX(),5);
        arrow.lineTo((float)cameraBounds.getCenterX()-21,26);
        arrow.lineTo((float)cameraBounds.getCenterX()+21,26);
        arrow.closePath();
        nNode.setIndicatorShape(arrow);
        
        PanCameraNode neNode =
            new PanCameraNode(new Rectangle2D.Double(
                                                     (cameraBounds.getWidth()-
                                                     edgeDistance),0,
                                                     edgeDistance,
                                                     edgeDistance),
                              -netAmount, netAmount);
                              
        arrow = new GeneralPath();
        arrow.moveTo((float)cameraBounds.getWidth()-5,5);
        arrow.lineTo((float)cameraBounds.getWidth()-35,5);
        arrow.lineTo((float)cameraBounds.getWidth()-5,35);
        arrow.closePath();
        neNode.setIndicatorShape(arrow);
        
                              
        PanCameraNode eNode =
            new PanCameraNode(new Rectangle2D.Double(
                                                     (cameraBounds.getWidth()-
                                                      edgeDistance),
                                                     edgeDistance,
                                                     edgeDistance,
                                                     cameraBounds.getHeight()-
                                                      (edgeDistance*2)),
                              -netAmount, 0);
                              
        arrow = new GeneralPath();
        arrow.moveTo((float)cameraBounds.getWidth()-5,
                     (float)cameraBounds.getCenterY());
        arrow.lineTo((float)cameraBounds.getWidth()-26,
                     (float)cameraBounds.getCenterY()-21);
        arrow.lineTo((float)cameraBounds.getWidth()-26,
                     (float)cameraBounds.getCenterY()+21);
        arrow.closePath();
        eNode.setIndicatorShape(arrow);
        
        PanCameraNode seNode =
            new PanCameraNode(new Rectangle2D.Double(
                                                     (cameraBounds.getWidth()-
                                                      edgeDistance),
                                                     (cameraBounds.getHeight()-
                                                      edgeDistance),
                                                     edgeDistance,
                                                     edgeDistance),
                              -netAmount, -netAmount);
                              
        arrow = new GeneralPath();
        arrow.moveTo((float)cameraBounds.getWidth()-5,
                     (float)cameraBounds.getHeight()-5);
        arrow.lineTo((float)cameraBounds.getWidth()-35,
                     (float)cameraBounds.getHeight()-5);
        arrow.lineTo((float)cameraBounds.getWidth()-5,
                     (float)cameraBounds.getHeight()-35);
        arrow.closePath();
        seNode.setIndicatorShape(arrow);
                              
        PanCameraNode sNode =
            new PanCameraNode(new Rectangle2D.Double(
                                                     edgeDistance,
                                                     (cameraBounds.getHeight()-
                                                      edgeDistance),
                                                     (cameraBounds.getWidth()-
                                                      edgeDistance*2),
                                                     edgeDistance),
                              0, -netAmount);
                              
        arrow = new GeneralPath();
        arrow.moveTo((float)cameraBounds.getCenterX(),
                     (float)cameraBounds.getHeight()-5);
        arrow.lineTo((float)cameraBounds.getCenterX()-21,
                     (float)cameraBounds.getHeight()-26);
        arrow.lineTo((float)cameraBounds.getCenterX()+21,
                     (float)cameraBounds.getHeight()-26);
        arrow.closePath();
        sNode.setIndicatorShape(arrow);
                              
        PanCameraNode swNode =
            new PanCameraNode(new Rectangle2D.Double(0,
                                                     (cameraBounds.getHeight()-
                                                      edgeDistance),
                                                     edgeDistance,
                                                     edgeDistance),
                              netAmount, -netAmount);
                              
        arrow = new GeneralPath();
        arrow.moveTo(5,(float)cameraBounds.getHeight()-5);
        arrow.lineTo(5,(float)cameraBounds.getHeight()-35);
        arrow.lineTo(35,(float)cameraBounds.getHeight()-5);
        arrow.closePath();
        swNode.setIndicatorShape(arrow);
                              
        PanCameraNode wNode =
            new PanCameraNode(new Rectangle2D.Double(0,
                                                     edgeDistance,
                                                     edgeDistance,
                                                     (cameraBounds.getHeight()-
                                                      edgeDistance*2)),
                              netAmount, 0);
                              
        arrow = new GeneralPath();
        arrow.moveTo(5,(float)cameraBounds.getCenterY());
        arrow.lineTo(26,(float)cameraBounds.getCenterY()-21);
        arrow.lineTo(26,(float)cameraBounds.getCenterY()+21);
        arrow.closePath();
        wNode.setIndicatorShape(arrow);
                                        
        panNodeList.add(nwNode);
        panNodeList.add(nNode);
        panNodeList.add(neNode);
        panNodeList.add(eNode);
        panNodeList.add(seNode);
        panNodeList.add(sNode);
        panNodeList.add(swNode);
        panNodeList.add(wNode);
        
        for(Iterator iter = panNodeList.iterator(); iter.hasNext();)
        {
            camera.addChild((PanCameraNode)iter.next());
        }
         
    }
    
    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.browser.ui.RegionSensitive#getActiveRegion()
     */
    public Rectangle2D getActiveRegion()
    {
        return activeRegion;
    }
    
    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.agents.browser.ui.RegionSensitive#setActiveRegion(java.awt.geom.Rectangle2D)
     */
    public void setActiveRegion(Rectangle2D region)
    {
        if(region != null)
        {
            activeRegion = region;
            recalculatePanNodes();
        }
        else
        {
            region = new Rectangle2D.Double(0,0,0,0);
        }
    }
    
    public void contextEntered()
    {
        // do nothing, mouseover will be picked up
    }
    
    /**
     * Kills panning
     */
    public void contextExited()
    {
        for(Iterator iter = panNodeList.iterator(); iter.hasNext();)
        {
            PanCameraNode node = (PanCameraNode)iter.next();
            node.cancelPan();
        }
        camera.repaint();
    }

    
    /**
     * A node that causes the camera to pan.
     *
     * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
     * <b>Internal version:</b> $Revision$ $Date$
     * @version 2.2
     * @since OME2.2
     */
    class PanCameraNode extends PNode
    {
        private Rectangle2D region;
        private Timer timer;
        private TimerTask activeTask;
        private boolean activated = false;
        private double dX;
        private double dY;
        
        private Shape indicatorShape;
        
        private Color fillColor = new Color(0,102,255,192);
        
        PanCameraNode(Rectangle2D region, double diffX, double diffY)
        {
            this.region = region;
            setBounds(region);
            this.dX = diffX;
            this.dY = diffY;
            
            // complicated add event listener code (not suitable for Action)
            addInputEventListener(new PBasicInputEventHandler()
            {
                public void mouseEntered(PInputEvent e)
                {
                    if(!activated)
                    {
                        timer = new Timer();
                        activeTask = new PanTask(e.getCamera(),dX,dY);
                        timer.scheduleAtFixedRate(activeTask,200,50);
                        activated = true;
                        e.getCamera().repaint();
                    }
                }
                
                public void mouseExited(PInputEvent e)
                {
                    if(activated)
                    {
                        activeTask.cancel();
                        timer.cancel();
                        activated = false;
                    }
                }
            });

        }
        
        /**
         * Sets the indicator shape to indicate that a pan is in progress.
         * Should be in the shape of an arrow, but you can make it whatever
         * you like.
         * 
         * @param s
         */
        public void setIndicatorShape(Shape s)
        {
            indicatorShape = s;
        }
        
        /**
         * Paints an indicator arrow to indicate imminent panning, or a
         * pan in progress.
         */
        public void paint(PPaintContext p)
        {
            if(indicatorShape != null && activated)
            {
                Graphics2D g2 = (Graphics2D)p.getGraphics();
                Paint paint = g2.getPaint();
                g2.setPaint(fillColor);
                g2.fill(indicatorShape);
                g2.setPaint(paint);
            }
        }
        
        /**
         * Explicit notiifcation that the panning should stop.
         */
        public void cancelPan()
        {
            if(timer != null && activeTask != null)
            {
                activeTask.cancel();
                timer.cancel();
                activated = false;
            }
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
    class PanTask extends TimerTask
    {
        protected double panDX = 0;
        protected double panDY = 0;
    
        protected PCamera camera;
    
        PanTask(PCamera camera, double dX, double dY)
        {
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

            double wOffset = activeRegion.getX();
            double nOffset = activeRegion.getY();
            double eOffset = activeRegion.getX()+activeRegion.getWidth();
            double sOffset = activeRegion.getY()+activeRegion.getHeight();

            double cWest = rBounds.getX();
            double cNorth = rBounds.getY();
            double cEast = rBounds.getX()+rBounds.getWidth();
            double cSouth = rBounds.getY()+rBounds.getHeight();

            double fdX = 0;
            double fdY = 0;

            // check horizontal panning bounds
            if((-deltaX+cWest < wOffset) ||
               (-deltaX+cEast > eOffset))
            {
                fdX = 0;
            }
            else
            {
                fdX = deltaX;
            }

            // check vertical panning bounds
            if((-deltaY+cNorth < nOffset) ||
               (-deltaY+cSouth > sOffset))
            {
                fdY = 0;
            }
            else
            {
                fdY = deltaY;
            }

            c.translateView(fdX,fdY);
            c.repaint();
        }
    
    
        public void run()
        {
            makeBoundedCameraMotion(camera,panDX,panDY);
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
