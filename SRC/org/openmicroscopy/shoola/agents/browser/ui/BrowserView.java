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
package org.openmicroscopy.shoola.agents.browser.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JPopupMenu;

import org.openmicroscopy.shoola.agents.browser.BrowserEnvironment;
import org.openmicroscopy.shoola.agents.browser.BrowserMode;
import org.openmicroscopy.shoola.agents.browser.BrowserModel;
import org.openmicroscopy.shoola.agents.browser.BrowserModelListener;
import org.openmicroscopy.shoola.agents.browser.BrowserTopModel;
import org.openmicroscopy.shoola.agents.browser.MessageHandler;
import org.openmicroscopy.shoola.agents.browser.events.MouseDownActions;
import org.openmicroscopy.shoola.agents.browser.events.MouseDownSensitive;
import org.openmicroscopy.shoola.agents.browser.events.MouseDragActions;
import org.openmicroscopy.shoola.agents.browser.events.MouseDragSensitive;
import org.openmicroscopy.shoola.agents.browser.events.MouseOverActions;
import org.openmicroscopy.shoola.agents.browser.events.PiccoloAction;
import org.openmicroscopy.shoola.agents.browser.events.PiccoloActionFactory;
import org.openmicroscopy.shoola.agents.browser.events.PiccoloActions;
import org.openmicroscopy.shoola.agents.browser.events.PiccoloModifiers;
import org.openmicroscopy.shoola.agents.browser.images.PaintMethods;
import org.openmicroscopy.shoola.agents.browser.images.Thumbnail;
import org.openmicroscopy.shoola.agents.browser.layout.FootprintAnalyzer;
import org.openmicroscopy.shoola.agents.browser.layout.LayoutMethod;

import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PBounds;

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
                         implements BrowserModelListener
{
    // The backing browser model.
    private BrowserModel browserModel;
    
    // The backing browser overlay model.
    private BrowserTopModel overlayModel;
    
    // The browser overlay view.
    private BrowserCamera overlayCamera;
    
    // Semantic zoom node manager.
    private HoverManager semanticLayer;
    
    // The environment (contains registry, etc.)
    private BrowserEnvironment env;
    
    // The layout map for thumbnails (maps thumbnails to offsets)
    private Map layoutMap;
    
    // The footprint taken up, in pixels at 100% zoom, of the entire
    // set of thumbnails.
    private Rectangle2D footprint;
    
    // the initial selection point for selecting multiple thumbnails in
    // the region.
    private Point2D initialSelectPoint;
    
    // the translated selection point.
    private Point2D initialViewPoint;
    
    // the current selection point for selecting multiple thumbnails in
    // the region.
    private Point2D currentSelectPoint;
    
    // indicates that a selection is occurring.
    private boolean selectionInProgress;
    
    // The region being selected, right now.
    private Rectangle2D selectingRegion;
    
    // Keeps track of all embedded HoverSensitive objects.
    private Set hoverSensitive;
    
    // Keeps track of all embedded RegionSensitive objects.
    private Set regionSensitive;
    
    // indicates whether or not the view should scale to show the entire
    // set, or should remain fixed at a certain zoom level.
    private boolean scaleToShow;
    
    // a node that catches mouse events that don't get picked up by
    // thumbnails and cameras and palettes (signifying the start of a multiple
    // select, perhaps)
    private PNode backgroundNode;
    
    /** REUSABLE PICCOLO ACTIONS... **/
    private PiccoloAction selectThumbnailAction;
    private PiccoloAction deselectThumbnailAction;
    private PiccoloAction semanticHoverThumbnailAction;
    private PiccoloAction semanticExitThumbnailAction;
    
    /** CURRENT THUMBNAIL ACTIONS (FOR THUMBNAILS TO BE ADDED) **/
    private MouseDownActions defaultTDownActions;
    private MouseOverActions defaultTOverActions;

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
            initActions(browserModel);
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
    
    private void initActions(BrowserModel targetModel)
    {
        // theoretically shouldn't happen
        if(targetModel == null)
        {
            return;
        }
        
        defaultTDownActions = new MouseDownActions();
        defaultTOverActions = new MouseOverActions();
        
        selectThumbnailAction =
            PiccoloActionFactory.getSelectThumbnailAction(targetModel);
        
        defaultTDownActions.setMouseClickAction(PiccoloModifiers.NORMAL,
                                                selectThumbnailAction);
        defaultTDownActions.setMouseClickAction(PiccoloModifiers.NORMAL,
                                                PiccoloActions.OPEN_IMAGE_ACTION);
        defaultTDownActions.setMouseClickAction(PiccoloModifiers.POPUP,
                                                PiccoloActions.POPUP_MENU_ACTION);
        

        semanticLayer = new HoverManager();
        semanticHoverThumbnailAction =
            PiccoloActionFactory.getSemanticEnterAction(semanticLayer);
            
        semanticExitThumbnailAction =
            PiccoloActionFactory.getOverlayExitAction(semanticLayer);
            
        defaultTOverActions.setMouseEnterAction(PiccoloModifiers.NORMAL,
                                                semanticHoverThumbnailAction);
        defaultTOverActions.setMouseExitAction(PiccoloModifiers.NORMAL,
                                               semanticExitThumbnailAction);
        
    }
    
    //  initialization code
    private void init(BrowserTopModel topModel)
    {
        env = BrowserEnvironment.getInstance();
        
        setBackground(new Color(192,192,192));
        backgroundNode = new BackgroundNode();
        getLayer().addChild(backgroundNode);
        
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
        addInputEventListener(BrowserViewEventDispatcher.getDefaultMouseHandler());
        addInputEventListener(BrowserViewEventDispatcher.getDefaultDragHandler());
    }
    
    /**
     * Returns a reference to the browser camera.
     * @return
     */
    public BrowserCamera getViewCamera()
    {
        return overlayCamera;
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
        
        Dimension dim = getSize();
        double width = dim.getWidth();
        double height = dim.getHeight();
        double scale = getCamera().getViewScale();
        double viewWidth = getCamera().getViewBounds().getWidth();
        double viewHeight = getCamera().getViewBounds().getHeight();
        double viewCenterX = getCamera().getViewBounds().getCenterX();
        double viewCenterY = getCamera().getViewBounds().getCenterY();
        
        double scaleX = 0;
        double scaleY = 0;
        
        System.err.println(footprint.getWidth()+","+footprint.getWidth());
        System.err.println("dim:"+width+","+height);
        if(width/zoomLevel < footprint.getWidth())
        {
            scaleX = viewCenterX;
            if((viewCenterX - (scale/zoomLevel)*viewWidth/2) < 0)
            {
                scaleX = 0;
            }
        }
        if(height/zoomLevel < footprint.getHeight())
        {
            scaleY = viewCenterY;
            if((viewCenterY - (scale/zoomLevel)*viewWidth/2) < 0)
            {
                scaleY = 0;
            }
        }
        System.err.println("scaleX="+scaleX+", scaleY="+scaleY);
        getCamera().scaleViewAboutPoint(zoomLevel/scale,scaleX,scaleY);
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
        else if(className.equals(BrowserModel.MAJOR_UI_MODE_NAME))
        {
            if(mode == BrowserMode.DEFAULT_MODE)
            {
                // TODO: fill this in
            }
        }
        else if(className.equals(BrowserModel.SELECT_MODE_NAME))
        {
            if(mode == BrowserMode.SELECTING_MODE)
            {
                selectionInProgress = true;
                repaint();
            }
            else
            {
                selectionInProgress = false;
                repaint();
            }
        }
        else if(className.equals(BrowserModel.ZOOM_MODE_NAME))
        {
            if(mode == BrowserMode.ZOOM_TO_FIT_MODE)
            {
                setZoomToScale();
            }
            else if(mode == BrowserMode.ZOOM_ACTUAL_MODE)
            {
                setZoomLevel(1);
            }
            else if(mode == BrowserMode.ZOOM_50_MODE)
            {
                setZoomLevel(0.5);
            }
            else if(mode == BrowserMode.ZOOM_75_MODE)
            {
                setZoomLevel(0.75);
            }
            else if(mode == BrowserMode.ZOOM_200_MODE)
            {
                setZoomLevel(2);
            }
        }
        else if(className.equals(BrowserModel.SEMANTIC_MODE_NAME))
        {
            if(mode == BrowserMode.IMAGE_NAME_MODE)
            {
                semanticHoverThumbnailAction =
                    PiccoloActionFactory.getImageNameEnterAction(semanticLayer);
                defaultTOverActions.setMouseEnterAction(PiccoloModifiers.NORMAL,
                                                        semanticHoverThumbnailAction);
                setThumbnailOverActions(defaultTOverActions);
            }
            else if(mode == BrowserMode.SEMANTIC_ZOOMING_MODE)
            {
                semanticHoverThumbnailAction =
                    PiccoloActionFactory.getSemanticEnterAction(semanticLayer);
                defaultTOverActions.setMouseEnterAction(PiccoloModifiers.NORMAL,
                                                        semanticHoverThumbnailAction);
                setThumbnailOverActions(defaultTOverActions);
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
        if(thumbnails == null || thumbnails.length == 0)
        {
            return;
        }
        
        browserModel.setCurrentMode(BrowserModel.SELECT_MODE_NAME,
                                    BrowserMode.SELECTED_MODE);
        // here's the paint method assignment
        for(int i=0;i<thumbnails.length;i++)
        {
            thumbnails[i].addMiddlePaintMethod(PaintMethods.DRAW_SELECT_METHOD);
        }
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.BrowserModelListener#thumbnailsDeselected(org.openmicroscopy.shoola.agents.browser.images.Thumbnail[])
     */
    public void thumbnailsDeselected(Thumbnail[] thumbnails)
    {
        // here's the paint method deassignment.
        for(int i=0;i<thumbnails.length;i++)
        {
            thumbnails[i].removeMiddlePaintMethod(PaintMethods.DRAW_SELECT_METHOD);
        }
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
        repaint();
    }
    
    /**
     * Makes sure the camera doesn't go off the edge.
     */
    public void boundCameraPosition()
    {
        PBounds bounds = getCamera().getViewBounds();
        Rectangle2D rBounds = bounds.getBounds2D();
        
        double correctX = rBounds.getX()+rBounds.getWidth() -
                          (footprint.getX()+footprint.getWidth());
        
        double correctY = rBounds.getY()+rBounds.getHeight() -
                          (footprint.getY()+footprint.getHeight());
        
        boolean move = false;
        double moveX = 0;
        double moveY = 0;
        
        if(correctX > 0)
        {
            move = true;
            if(footprint.getWidth() < rBounds.getWidth())
            {
                moveX = rBounds.getX();
            }
            else
            {
                moveX = correctX;
            }
        }
        if(correctY > 0)
        {
            move = true;
            if(footprint.getHeight() < rBounds.getHeight())
            {
                moveY = rBounds.getY();
            }
            else
            {
                moveY = correctY;
            }
        }
        
        if(rBounds.getX() < 0)
        {
            move = true;
            moveX = rBounds.getX();
        }
        if(rBounds.getY() < 0)
        {
            move = true;
            moveY = rBounds.getY();
        }
        
        if(move && (moveX != 0 || moveY != 0))
        {
            getCamera().translateView(moveX,moveY);
        } 
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
        boundCameraPosition();
        
        double viewScale = getCamera().getViewScale();
        backgroundNode.setBounds(0,0,width/viewScale,height/viewScale);
        
        //      update things
        for(Iterator iter = regionSensitive.iterator(); iter.hasNext();)
        {
            RegionSensitive rs = (RegionSensitive)iter.next();
            rs.setActiveRegion(footprint);
        }
        
        System.err.println(footprint);
        System.err.println("Dim: "+width+","+height);
        overlayCamera.cameraResized(new Rectangle2D.Double(0,0,width,height));
    }
    
    /**
     * Responds to a model-triggered update.
     * @see org.openmicroscopy.shoola.agents.browser.BrowserModelListener#modelUpdated()
     */
    public void modelUpdated()
    {
        updateThumbnails();
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.BrowserModelListener#thumbnailAdded(org.openmicroscopy.shoola.agents.browser.images.Thumbnail)
     */
    public void thumbnailAdded(Thumbnail t)
    {
        // apply the current UI modes...
        t.setMouseDownActions(defaultTDownActions);
        t.setMouseOverActions(defaultTOverActions);
        getLayer().addChild(t);
        updateThumbnails();
    }
    
    public void thumbnailsAdded(Thumbnail[] ts)
    {
        if(ts == null || ts.length == 0) return;
        for(int i=0;i<ts.length;i++)
        {
            ts[i].setMouseDownActions(defaultTDownActions);
            ts[i].setMouseOverActions(defaultTOverActions);
            getLayer().addChild(ts[i]);
        }
        updateThumbnails();
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.BrowserModelListener#thumbnailRemoved(org.openmicroscopy.shoola.agents.browser.images.Thumbnail)
     */
    public void thumbnailRemoved(Thumbnail t)
    {
        getLayer().removeChild(t);
        updateThumbnails();
    }
    
    public void thumbnailsRemoved(Thumbnail[] ts)
    {
        if(ts == null || ts.length == 0) return;
        for(int i=0;i<ts.length;i++)
        {
            getLayer().removeChild(ts[i]);
        }
        updateThumbnails();
    }


    
    /*** UI MODE MASS-APPLICATION METHODS ***/
    
    // sets the mouse down actions (default) for each thumbnail
    private void setThumbnailDownActions(MouseDownActions actions)
    {
        List thumbnailList = browserModel.getThumbnails();
        for(Iterator iter = thumbnailList.iterator(); iter.hasNext();)
        {
            Thumbnail t = (Thumbnail)iter.next();
            t.setMouseDownActions(actions);
        }
    }
    
    // sets the mouse over actions (default) for each thumbnail
    private void setThumbnailOverActions(MouseOverActions actions)
    {
        List thumbnailList = browserModel.getThumbnails();
        for(Iterator iter = thumbnailList.iterator(); iter.hasNext();)
        {
            Thumbnail t = (Thumbnail)iter.next();
            t.setMouseOverActions(actions);
        }
    }
    
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        
        if(selectionInProgress)
        {
            Color c = g2.getColor();
            Paint p = g2.getPaint();
            
            double minX = Math.min(initialViewPoint.getX(),
                                   currentSelectPoint.getX());
            double minY = Math.min(initialViewPoint.getY(),
                                   currentSelectPoint.getY());
            double maxX = Math.max(initialViewPoint.getX(),
                                   currentSelectPoint.getX());
            double maxY = Math.max(initialViewPoint.getY(),
                                   currentSelectPoint.getY());
                                   
            Rectangle2D region =
                new Rectangle2D.Double(minX,minY,maxX-minX,maxY-minY);
            
            g2.setPaint(new Color(153,153,153,153));
            g2.fill(region);
            g2.setColor(Color.white);
            g2.draw(region);
            g2.setPaint(p);
            g2.setColor(c);
            
        }
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
    
    /**
     * The background node of this view that will handle selection events.
     */
    class BackgroundNode extends PNode implements MouseDragSensitive,
                                                  MouseDownSensitive
    {
        private MouseDragActions mouseDragActions;
        private MouseDownActions mouseDownActions;
        
        public BackgroundNode()
        {
            mouseDragActions = new MouseDragActions();
            mouseDownActions = new MouseDownActions();
        }
        
        /**
         * @see org.openmicroscopy.shoola.agents.browser.events.MouseDragSensitive#getMouseDragActions()
         */
        public MouseDragActions getMouseDragActions()
        {
            return mouseDragActions;
        }
        
        /**
         * @see org.openmicroscopy.shoola.agents.browser.events.MouseDownSensitive#getMouseDownActions()
         */
        public MouseDownActions getMouseDownActions()
        {
            return mouseDownActions;
        }
        
        /**
         * @see org.openmicroscopy.shoola.agents.browser.events.MouseDragSensitive#setMouseDragActions(org.openmicroscopy.shoola.agents.browser.events.MouseDragActions)
         */
        public void setMouseDragActions(MouseDragActions actions)
        {
            if(actions != null)
            {
                mouseDragActions = actions;
            }
        }
        
        /**
         * @see org.openmicroscopy.shoola.agents.browser.events.MouseDownSensitive#setMouseDownActions(org.openmicroscopy.shoola.agents.browser.events.MouseDownActions)
         */
        public void setMouseDownActions(MouseDownActions actions)
        {
            if(actions != null)
            {
                mouseDownActions = actions;
            }
        }
        
        /**
         * Non-overrideable behavior.
         * 
         * @see org.openmicroscopy.shoola.agents.browser.events.MouseDragSensitive#respondDrag(edu.umd.cs.piccolo.event.PInputEvent)
         */
        public void respondDrag(PInputEvent e)
        {
            currentSelectPoint = e.getCamera().viewToLocal(e.getPosition());
            repaint();
        }
        
        /**
         * Non-overrideable behavior.
         * 
         * @see org.openmicroscopy.shoola.agents.browser.events.MouseDragSensitive#respondStartDrag(edu.umd.cs.piccolo.event.PInputEvent)
         */
        public void respondStartDrag(PInputEvent e)
        {
            Point2D position = e.getPosition();
            Dimension2D offset = e.getDelta();
            initialSelectPoint = new Point2D.Double(position.getX()-
                                                    offset.getWidth(),
                                                    position.getY()-
                                                    offset.getHeight());
            Point2D dummy = new Point2D.Double(initialSelectPoint.getX(),
                                               initialSelectPoint.getY());
            initialViewPoint = e.getCamera().viewToLocal(dummy);
            currentSelectPoint = e.getCamera().viewToLocal(position);
                                                    
            int modifier = PiccoloModifiers.getModifier(e);
            if(modifier != PiccoloModifiers.MOUSE_INDIV_SELECT &&
               modifier != PiccoloModifiers.SHIFT_DOWN)
            {
                browserModel.deselectAllThumbnails();
            }
            browserModel.setCurrentMode(BrowserModel.SELECT_MODE_NAME,
                                        BrowserMode.SELECTING_MODE);
        }
        
        /**
         * Non-overrideable behavior.
         * 
         * @see org.openmicroscopy.shoola.agents.browser.events.MouseDragSensitive#respondEndDrag(edu.umd.cs.piccolo.event.PInputEvent)
         */
        public void respondEndDrag(PInputEvent e)
        {
            Point2D endSelectPoint = e.getPosition();
            double tlX = Math.min(initialSelectPoint.getX(),
                                  endSelectPoint.getX());
            double tlY = Math.min(initialSelectPoint.getY(),
                                  endSelectPoint.getY());
            double brX = Math.max(initialSelectPoint.getX(),
                                  endSelectPoint.getX());
            double brY = Math.max(initialSelectPoint.getY(),
                                  endSelectPoint.getY());
            
            Rectangle2D region = new Rectangle2D.Double(tlX,tlY,brX-tlX,
                                                        brY-tlY);
                                                        
            List thumbnailList = browserModel.getThumbnails();
            for(Iterator iter = thumbnailList.iterator(); iter.hasNext();)
            {
                Thumbnail t = (Thumbnail)iter.next();
                Rectangle2D tBounds =
                    new Rectangle2D.Double(t.getOffset().getX(),
                                           t.getOffset().getY(),
                                           t.getBounds().getWidth(),
                                           t.getBounds().getHeight());
                if(tBounds.intersects(region))
                {
                    browserModel.selectThumbnail(t);
                }
            }
            
            if(browserModel.getSelectedImages().size() > 0)
            {
                browserModel.setCurrentMode(BrowserModel.SELECT_MODE_NAME,
                                            BrowserMode.SELECTED_MODE);
            }
            else
            {
                browserModel.setCurrentMode(BrowserModel.SELECT_MODE_NAME,
                                            BrowserMode.UNSELECTED_MODE);
            }
        }
        
        /**
         * Non-overrideable.
         * @see org.openmicroscopy.shoola.agents.browser.events.MouseDownSensitive#respondMouseClick(edu.umd.cs.piccolo.event.PInputEvent)
         */
        public void respondMouseClick(PInputEvent event)
        {
            if(browserModel.getCurrentMode(BrowserModel.SELECT_MODE_NAME) ==
               BrowserMode.SELECTED_MODE)
            {
                int modifiers = PiccoloModifiers.getModifier(event);
                if(modifiers != PiccoloModifiers.MOUSE_INDIV_SELECT &&
                   modifiers != PiccoloModifiers.SHIFT_DOWN)
                {
                    browserModel.deselectAllThumbnails();
                    browserModel.setCurrentMode(BrowserModel.SELECT_MODE_NAME,
                                                BrowserMode.UNSELECTED_MODE);
                }
                else if(modifiers == PiccoloModifiers.POPUP)
                {
                    // TODO: write popup (multi-selected) code
                }
            }
            else
            {
                int modifiers = PiccoloModifiers.getModifier(event);
                if(modifiers == PiccoloModifiers.POPUP)
                {
                    JPopupMenu menu = PopupMenuFactory.getMenu(this);
                    Point2D position = event.getPosition();
                    event.getCamera().viewToLocal(position);
                    int offsetX = (int)Math.round(position.getX());
                    int offsetY = (int)Math.round(position.getY());
                
                    // this could be error prone, but hopefully not in context
                    PCanvas canvas = (PCanvas)event.getComponent();
                    menu.show(canvas,offsetX,offsetY);
                }
            }
        }
        
        /**
         * For now, don't do anything-- just pass as if it were a single click.
         * @see org.openmicroscopy.shoola.agents.browser.events.MouseDownSensitive#respondMouseDoubleClick(edu.umd.cs.piccolo.event.PInputEvent)
         */
        public void respondMouseDoubleClick(PInputEvent event)
        {
            respondMouseClick(event);
        }

        
        /**
         * @see org.openmicroscopy.shoola.agents.browser.events.MouseDownSensitive#respondMousePress(edu.umd.cs.piccolo.event.PInputEvent)
         */
        public void respondMousePress(PInputEvent event)
        {
            PiccoloAction action =
                mouseDownActions.getMousePressAction(PiccoloModifiers.getModifier(event));
            action.execute(event);
        }
        
        /**
         * @see org.openmicroscopy.shoola.agents.browser.events.MouseDownSensitive#respondMouseRelease(edu.umd.cs.piccolo.event.PInputEvent)
         */
        public void respondMouseRelease(PInputEvent event)
        {
            PiccoloAction action =
                mouseDownActions.getMouseReleaseAction(PiccoloModifiers.getModifier(event));
            action.execute(event);
        }
    }
}
