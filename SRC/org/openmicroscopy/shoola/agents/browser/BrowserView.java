/*
 * org.openmicroscopy.shoola.agents.browser.BrowserView
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
package org.openmicroscopy.shoola.agents.browser;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmicroscopy.shoola.agents.browser.datamodel.ProgressListener;
import org.openmicroscopy.shoola.agents.browser.images.Thumbnail;
import org.openmicroscopy.shoola.agents.browser.layout.FootprintAnalyzer;
import org.openmicroscopy.shoola.agents.browser.layout.LayoutMethod;
import org.openmicroscopy.shoola.agents.browser.ui.HoverSensitive;
import org.openmicroscopy.shoola.agents.browser.ui.RegionSensitive;

import edu.umd.cs.piccolo.PCanvas;

/**
 * The view component of the top-level browser MVC architecture.  Where the
 * thumbnails are physically drawn.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class BrowserView extends PCanvas
                         implements BrowserModelListener, ProgressListener
{
    private BrowserModel browserModel;
    private BrowserTopModel overlayModel;
    private BrowserCamera overlayCamera;
    private BrowserEnvironment env;
    private Map layoutMap;
    private Rectangle2D footprint;
    
    private Set hoverSensitive;
    private Set regionSensitive;

    private void init(BrowserTopModel topModel)
    {
        env = BrowserEnvironment.getInstance();
        setBackground(new Color(192,192,192));
        layoutMap = new HashMap();
        footprint = new Rectangle2D.Double(0,0,0,0);
        hoverSensitive = new HashSet();
        regionSensitive = new HashSet();
        
        // here we disable zoom/pan (TODO: save for later, reinstate on mode)
        removeInputEventListener(getZoomEventHandler());
        removeInputEventListener(getPanEventHandler());
        
        // default panning mode (may replace this, but probably not)
        overlayCamera = new BrowserCamera(topModel,getCamera());
        hoverSensitive.add(overlayCamera);
        regionSensitive.add(overlayCamera);
        
        addMouseListener(new MouseAdapter()
        {
            public void mouseEntered(MouseEvent me)
            {
                for(Iterator iter = hoverSensitive.iterator(); iter.hasNext();)
                {
                    HoverSensitive hover = (HoverSensitive)iter.next();
                    hover.contextEntered();
                }
            }
            
            public void mouseExited(MouseEvent me)
            {
                for(Iterator iter = hoverSensitive.iterator(); iter.hasNext();)
                {
                    HoverSensitive hover = (HoverSensitive)iter.next();
                    hover.contextExited();
                }
            }
        });
    }

    /**
     * Constructs the browser view with the two backing models-- one for the
     * thumbnails and the other for any sticky overlays.
     * 
     * @param browserModel The thumbnail/canvas model.
     * @param overlayModel The overlay/sticky node model.
     */
    public BrowserView(BrowserModel browserModel, BrowserTopModel overlayModel)
    {
        if (browserModel == null || overlayModel == null)
        {
            sendInternalError("Null parameters in BrowserView constructor");
        }
        else
        {
            this.browserModel = browserModel;
            this.overlayModel = overlayModel;
            init(overlayModel);
            
            List thumbnailList = browserModel.getThumbnails();
            for(Iterator iter = thumbnailList.iterator(); iter.hasNext();)
            {
                getLayer().addChild((Thumbnail)iter.next());
            }
            updateThumbnails();
        }
        
        this.addComponentListener(new ComponentAdapter()
        {
            /* (non-Javadoc)
             * @see java.awt.event.ComponentAdapter#componentResized(java.awt.event.ComponentEvent)
             */
            public void componentResized(ComponentEvent arg0)
            {
                updateConstraints();
            }
        });

    }
    
    // TODO: retrofit to groups
    public void updateThumbnails()
    {
        List thumbnailList = browserModel.getThumbnails();
        Thumbnail[] thumbnails = new Thumbnail[thumbnailList.size()];
        thumbnailList.toArray(thumbnails);
        
        LayoutMethod method = browserModel.getLayoutMethod();
        
        if(method == null) // TODO: fallback to default?
        {
            System.err.println("null layout method");
            return;
        }
        
        layoutMap = method.getAnchorPoints(thumbnails);
        
        for(Iterator iter = layoutMap.keySet().iterator(); iter.hasNext();)
        {
            Thumbnail t = (Thumbnail)iter.next();
            Point2D offset = (Point2D)layoutMap.get(t);
            t.setOffset(offset);
        }
        
        footprint = FootprintAnalyzer.getArea(layoutMap);
        
        // TODO: determine case (mode) in which this doesn't happen
        updateConstraints();
    }
    
    /**
     * Updates the zoom camera (dependent on mode) to fit the complete dataset.
     *
     */
    public void updateConstraints()
    {
        Dimension dimension = getSize();
        double width = dimension.getWidth();
        double height = dimension.getHeight();
        
        
        double xRatio = width / footprint.getWidth();
        double yRatio = height / footprint.getHeight();
        
        
        
        // for some reason, setting setViewScale(0) screws things up
        // in a big way.
        
        /*
        if((xRatio < 1 || yRatio < 1) &&
           (xRatio != 0 && yRatio != 0))
        {
            double min = Math.min(xRatio,yRatio);
            getCamera().setViewScale(min);
        }
        else
        {
            getCamera().setViewScale(1);
        }*/
        getCamera().setViewScale(0.75);
        
        //      update things
        for(Iterator iter = regionSensitive.iterator(); iter.hasNext();)
        {
            RegionSensitive rs = (RegionSensitive)iter.next();
            rs.setActiveRegion(footprint);
        }
    
        overlayCamera.cameraResized(new Rectangle2D.Double(0,0,width,height));
    }

    /**
     * Show the overlay (sticky) nodes.
     */
    public void showModalNodes()
    {
        // TODO: fill in code
    }

    /**
     * Hide the overlay (sticky) nodes.
     */
    public void hideModalNodes()
    {
        // TODO: fill in code
    }
    
    /**
     * Responds to a model-triggered update.
     * @see org.openmicroscopy.shoola.agents.browser.BrowserModelListener#modelUpdated()
     */
    public void modelUpdated() // TODO: refine this brute-force method
    {
        getLayer().removeAllChildren();
        List thumbnailList = browserModel.getThumbnails();
        
        for(Iterator iter = thumbnailList.iterator(); iter.hasNext();)
        {
            getLayer().addChild((Thumbnail)iter.next());
        }
        // TODO Auto-generated method stub
        updateThumbnails();
    }
    
    /**
     * Indicates to the user that an iterative, potentially time-consuming
     * process has started.
     * 
     * @param piecesOfData The number of steps in the process about to start.
     * @see org.openmicroscopy.shoola.agents.browser.datamodel.ProgressListener#processStarted(int)
     */
    public void processStarted(int piecesOfData)
    {
        // bring up process view window?
        // TODO: make BProgressIndicator
    }
    
    /**
     * Indicates to the user that a process has advanced a step.
     * 
     * @param info The message to display.
     * @see org.openmicroscopy.shoola.agents.browser.datamodel.ProgressListener#processAdvanced(java.lang.String)
     */
    public void processAdvanced(String info)
    {
        // TODO: advance BProgressIndicator, show message
    }

    /**
     * Display that the process has failed for some reason.
     * 
     * @param The displayed reason why a process failed.
     * @see org.openmicroscopy.shoola.agents.browser.datamodel.ProgressListener#processFailed(java.lang.String)
     */
    public void processFailed(String reason)
    {
        // TODO: close BProgressIndicator, launch User notifier?
    }
    
    /**
     * Display that a process has succeeded.
     * @see org.openmicroscopy.shoola.agents.browser.datamodel.ProgressListener#processSucceeded()
     */
    public void processSucceeded()
    {
        // TODO: close BProgressIndicator, nothing more (success implicit)
    }


    // send internal error through the BrowserEnvironment pathway
    private void sendInternalError(String message)
    {
        MessageHandler handler = env.getMessageHandler();
        handler.reportInternalError(message);
    }

    // send general error through the BrowserEnvironment pathway
    private void sendError(String message)
    {
        MessageHandler handler = env.getMessageHandler();
        handler.reportError(message);
    }
}
