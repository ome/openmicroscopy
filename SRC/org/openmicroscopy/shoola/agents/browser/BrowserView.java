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
import org.openmicroscopy.shoola.agents.browser.events.MouseDownSensitive;
import org.openmicroscopy.shoola.agents.browser.events.MouseDragSensitive;
import org.openmicroscopy.shoola.agents.browser.events.MouseOverSensitive;
import org.openmicroscopy.shoola.agents.browser.images.Thumbnail;
import org.openmicroscopy.shoola.agents.browser.layout.FootprintAnalyzer;
import org.openmicroscopy.shoola.agents.browser.layout.LayoutMethod;
import org.openmicroscopy.shoola.agents.browser.ui.HoverSensitive;
import org.openmicroscopy.shoola.agents.browser.ui.RegionSensitive;

import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PDragSequenceEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PPickPath;

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
    // The backing browser model.
    private BrowserModel browserModel;
    
    // The backing browser overlay model.
    private BrowserTopModel overlayModel;
    
    // The browser overlay view.
    private BrowserCamera overlayCamera;
    
    // The environment (contains registry, etc.)
    private BrowserEnvironment env;
    
    // The layout map for thumbnails (maps thumbnails to offsets)
    private Map layoutMap;
    
    // The footprint taken up, in pixels at 100% zoom, of the entire
    // set of thumbnails.
    private Rectangle2D footprint;
    
    // Keeps track of all embedded HoverSensitive objects.
    private Set hoverSensitive;
    
    // Keeps track of all embedded RegionSensitive objects.
    private Set regionSensitive;
    
    // indicates whether or not the view should scale to show the entire
    // set, or should remain fixed at a certain zoom level.
    private boolean scaleToShow;

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
    
    //  initialization code
    private void init(BrowserTopModel topModel)
    {
        env = BrowserEnvironment.getInstance();
        setBackground(new Color(192,192,192));
        layoutMap = new HashMap();
        footprint = new Rectangle2D.Double(0,0,0,0);
        hoverSensitive = new HashSet();
        regionSensitive = new HashSet();
        
        removeInputEventListener(getZoomEventHandler());
        removeInputEventListener(getPanEventHandler());
       
        // default panning mode (may replace this, but probably not)
        overlayCamera = new BrowserCamera(topModel,getCamera());
        hoverSensitive.add(overlayCamera);
        regionSensitive.add(overlayCamera);
        scaleToShow = true;
       
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
        
        // OK, now, dispatch to the underlying nodes
        addInputEventListener(new PBasicInputEventHandler()
        {
            public void mouseClicked(PInputEvent e)
            {
                PPickPath pickPath = e.getPath();
                PNode node;
                while((node = pickPath.getPickedNode()) != null &&
                      !e.isHandled())
                {
                    if(node instanceof MouseDownSensitive)
                    {
                        ((MouseDownSensitive)node).respondMouseClick(e);
                        e.setHandled(true);
                    }
                    else
                    {
                        pickPath.popNode(node);
                    }
                }
            }
            
            public void mouseMoved(PInputEvent e)
            {
                PPickPath pickPath = e.getPath();
                PNode node;
                while((node = pickPath.getPickedNode()) != null &&
                    !e.isHandled())
                {
                    if(node instanceof MouseOverSensitive)
                    {
                        ((MouseOverSensitive)node).respondMouseEnter(e);
                        e.setHandled(true);
                    }
                    else
                    {
                        pickPath.popNode(node);
                    }
                }
            }
        });
        
        addInputEventListener(new PDragSequenceEventHandler()
        {
            public void drag(PInputEvent e)
            {
                PPickPath pickPath = e.getPath();
                PNode node;
                while((node = pickPath.getPickedNode()) != null &&
                      !e.isHandled())
                {
                    System.err.println(node);
                    if(node instanceof MouseDragSensitive)
                    {
                        ((MouseDragSensitive)node).respondDrag(e);
                        e.setHandled(true);
                    }
                    else
                    {
                        pickPath.popNode(node);
                    }
                }
            }
        });
    }
    
    /**
     * Sets the scale/zoom level.  1 is 100%.
     * @param zoomLevel The magnification of the camera.
     */
    public void setZoomLevel(double zoomLevel)
    {
        // error condition
        if(zoomLevel <= 0)
        {
            return;
        }
        scaleToShow = false;
        getCamera().setViewScale(zoomLevel);
        updateConstraints();
    }
    
    /**
     * Sets the scale level to automatically adjust with window size.
     */
    public void setZoomToScale()
    {
        scaleToShow = true;
        updateConstraints();
    }
    
    /**
     * Responds to a major UI mode change.
     */
    public void modeChanged(String className, BrowserMode mode)
    {
        // boooooo.
        if(className == null)
        {
            return;
        }
        else if(className.equals(BrowserModel.PAN_MODE_NAME))
        {
            if(mode == BrowserMode.HAND_MODE)
            {
                // TODO: change drag listener here
            }
            else if(mode == BrowserMode.NO_HAND_MODE)
            {
                // TODO: change back.
            }
        }
    }
    
    /**
     * Respond to a paint method change by repainting
     * 
     * @see org.openmicroscopy.shoola.agents.browser.BrowserModelListener#paintMethodsChanged()
     */
    public void paintMethodsChanged()
    {
        repaint();
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.BrowserModelListener#thumbnailsSelected(org.openmicroscopy.shoola.agents.browser.images.Thumbnail[])
     */
    public void thumbnailsSelected(Thumbnail[] thumbnails)
    {
        // TODO change the action and paint for behaviors for the images;
        // modify the selected region
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.BrowserModelListener#thumbnailsDeselected(org.openmicroscopy.shoola.agents.browser.images.Thumbnail[])
     */
    public void thumbnailsDeselected(Thumbnail[] thumbnails)
    {
        // TODO change the action and paint behaviors for the images;
        // modify the selected region
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
        
        if(scaleToShow)
        
        {
            if((xRatio < 1 || yRatio < 1) &&
               (xRatio != 0 && yRatio != 0))
            {
                double min = Math.min(xRatio,yRatio);
                getCamera().setViewScale(min);
            }
            else
            {
                getCamera().setViewScale(1);
            }
        }
        
        //      update things
        for(Iterator iter = regionSensitive.iterator(); iter.hasNext();)
        {
            RegionSensitive rs = (RegionSensitive)iter.next();
            rs.setActiveRegion(footprint);
        }
    
        overlayCamera.cameraResized(new Rectangle2D.Double(0,0,width,height));
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
        // TODO change to UserNotifier
        MessageHandler handler = env.getMessageHandler();
        handler.reportInternalError(message);
    }

    // send general error through the BrowserEnvironment pathway
    private void sendError(String message)
    {
        // TODO change to UserNotifier
        MessageHandler handler = env.getMessageHandler();
        handler.reportError(message);
    }
}
