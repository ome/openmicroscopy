/*
 * org.openmicroscopy.shoola.agents.viewer.Viewer
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

package org.openmicroscopy.shoola.agents.viewer;

//Java imports
import java.awt.Frame;
import java.awt.image.BufferedImage;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.rnd.events.DisplayRendering;
import org.openmicroscopy.shoola.agents.roi.canvas.DrawingCanvas;
import org.openmicroscopy.shoola.agents.roi.events.AddROICanvas;
import org.openmicroscopy.shoola.agents.roi.events.AnnotateROI;
import org.openmicroscopy.shoola.agents.roi.events.DisplayROI;
import org.openmicroscopy.shoola.agents.viewer.defs.ImageAffineTransform;
import org.openmicroscopy.shoola.agents.viewer.events.IATChanged;
//import org.openmicroscopy.shoola.agents.viewer3D.events.DisplayViewer3D;
import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.env.rnd.defs.PlaneDef;
import org.openmicroscopy.shoola.env.rnd.events.ImageLoaded;
import org.openmicroscopy.shoola.env.rnd.events.ImageRendered;
import org.openmicroscopy.shoola.env.rnd.events.LoadImage;
import org.openmicroscopy.shoola.env.rnd.events.RenderImage;
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsDimensions;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *                  a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class Viewer
implements Agent, AgentEventListener
{
    
    /** Reference to the {@link Registry}. */
    private Registry                registry;
    
    private ViewerUIF               presentation;
    
    private ViewerCtrl              control;
        
    private RenderingControl        renderingControl;
    
    private int                     curImageID, curPixelsID;
    
    private BufferedImage           curImage;
    
    private String                  curImageName;
    
    /** Implemented as specified by {@link Agent}. */
    public void activate() {}
    
    /** Implemented as specified by {@link Agent}. */
    public void terminate() {}
    
    /** Implemented as specified by {@link Agent}. */
    public void setContext(Registry ctx) 
    {
        registry = ctx;
        EventBus bus = registry.getEventBus();
        bus.register(this, LoadImage.class);
        bus.register(this, ImageLoaded.class);
        bus.register(this, ImageRendered.class);
        bus.register(this, AddROICanvas.class);
        bus.register(this, AnnotateROI.class);
        //bus.register(this, DisplayViewer3D.class);
        control = new ViewerCtrl(this);
    }
    
    /** Implemented as specified by {@link Agent}. */
    public boolean canTerminate() { return true; }

    /** Implement as specified by {@link AgentEventListener}. */
    public void eventFired(AgentEvent e) 
    {
        if (e instanceof ImageLoaded)
            handleImageLoaded((ImageLoaded) e);
        else if (e instanceof ImageRendered)
            handleImageRendered((ImageRendered) e);
        else if (e instanceof LoadImage)
            handleLoadImage((LoadImage) e);
        else if (e instanceof AddROICanvas)
            handleAddROI((AddROICanvas) e);
        else if (e instanceof AnnotateROI)
            handleAnnotateROI((AnnotateROI) e);
        //else if (e instanceof DisplayViewer3D)
        //    handleDisplayViewer3D((DisplayViewer3D) e);
    }
    
    ViewerUIF getPresentation() { return presentation; }
    
    Registry getRegistry() { return registry; }
    
    int getModel() { return renderingControl.getModel(); }
    
    void setModel(int model) { renderingControl.setModel(model); }
    
    PixelsDimensions getPixelsDims()
    { 
        return renderingControl.getPixelsDims();
    }
    
    int getImageWidth() { return curImage.getWidth(); }
    
    int getImageHeight() { return curImage.getHeight(); }
    
    int getCurPixelsID() { return curPixelsID; } 
    
    /** Default timepoint. */
    int getDefaultT() { return renderingControl.getDefaultT(); }
    
    /** Default z-section in the stack. */
    int getDefaultZ() { return renderingControl.getDefaultZ(); }
    
    /** Return the current buffered image. */
    BufferedImage getCurImage() { return curImage; }
    
    String getCurImageName() { return curImageName; }
    
    /** 2D-plane selected. */
    void onPlaneSelected(int z, int t)
    {
        PlaneDef def = new PlaneDef(PlaneDef.XY, t);
        def.setZ(z);
        renderingControl.setDefaultZ(z);
        renderingControl.setDefaultT(t);
        RenderImage renderImage = new RenderImage(curPixelsID, def);
        registry.getEventBus().post(renderImage);   
    }
    
    void onPlaneSelected(int z)
    {
        onPlaneSelected(z, getDefaultT());
    }
    
    /** 
     * Post an event to bring up the 
     * {@link org.openmicroscopy.shoola.agents.roi.ROIAgt ROIAgt}. 
     */
    void showViewer3D()
    {
        /**
        DisplayViewer3D event = new DisplayViewer3D(true);
        event.setOwner(presentation);
        registry.getEventBus().post(event);
        */
    }
    
    /** 
     * Post an event to bring up the 
     * {@link org.openmicroscopy.shoola.agents.roi.ROIAgt ROIAgt}. 
     */
    void showROI(DrawingCanvas drawingCanvas, ImageAffineTransform iat)
    {
        registry.getEventBus().post(new DisplayROI(drawingCanvas, iat));
    }
    
    /** 
     * Post an event to bring up the 
     * {@link org.openmicroscopy.shoola.agents.rnd.RenderingAgt RenderingAgt}. 
     */
    void showRendering()
    {
        registry.getEventBus().post(new DisplayRendering());
    }
    
    void imageDisplayedUpdated(ImageAffineTransform iat, BufferedImage img)
    {
        registry.getEventBus().post(new IATChanged(iat, img));
    }
    
    /** Close ROIAgt when the viewer is closed. */
    void addRoiCanvas(boolean b)
    {
        registry.getEventBus().post(new AddROICanvas(b));
    }
    
    /** Handle event @see DisplayViewer3D. */
    /*
    private void handleDisplayViewer3D(DisplayViewer3D response)
    {
        if (!response.isVisible()) {
            int z = getDefaultZ();
            control.resetZSlider(z);
            control.resetZField(z);
        }
    }
    */
    
    private void handleAnnotateROI(AnnotateROI response)
    {
        control.setAnnotationText(response.getAnnotation());
    }
    
    /** Handle event @see AddROICanvas. */
    private void handleAddROI(AddROICanvas response)
    {
        control.setRoiOnOff(response.isOnOff());
    }
    
    /** Handle event @see LoadImage. */
    private void handleLoadImage(LoadImage request)
    {
        //TODO: REMOVE COMMENTS
        //control.showProgressNotifier(request.getImageName());
    }
    
    /** Handle event @see ImageLoaded. */
    private void handleImageLoaded(ImageLoaded response)
    {
        LoadImage request = (LoadImage) response.getACT();
        renderingControl = response.getProxy();
        PixelsDimensions pxsDims = renderingControl.getPixelsDims();
        //TODO: REMOVE COMMENTS
        //control.removeProgressNotifier();
        if (curImageID != request.getImageID()) {  
            if (presentation == null) buildPresentation(pxsDims);
            initPresentation(request.getImageName(), pxsDims, false);
            curImageID = request.getImageID();
            curPixelsID = request.getPixelsID();
            registry.getEventBus().post(new RenderImage(curPixelsID));
            // have to dispose all windows linked 
            control.disposeDialogs();
        } showPresentation();
    }
    
    /** Handle event @see ImageRendered. */
    private void handleImageRendered(ImageRendered response)
    {
        curImage = null;
        curImage = response.getRenderedImage();
        presentation.setImage(curImage);
    }
    
    /** Set the default. */
    private void initPresentation(String imageName, PixelsDimensions pxsDims, 
                            boolean active)
    {
        curImageName = imageName;
        presentation.setDefaultZT(getDefaultT(), getDefaultZ(), 
                                    pxsDims.sizeT, pxsDims.sizeZ);
        presentation.setImageName(imageName);
        presentation.setActive(active);
        presentation.resetMagFactor();
        presentation.setUnitBarSize(pxsDims.pixelSizeX);
    }
    
    /** Build the GUI. */
    private void buildPresentation(PixelsDimensions pxsDims)
    {
        presentation = new ViewerUIF(control, registry, pxsDims, getDefaultT(), 
                                    getDefaultZ());
        control.setPresentation(presentation);
        control.attachListener();   
    }
    
    private void showPresentation()
    {
        if (presentation.getExtendedState() == Frame.ICONIFIED)
            presentation.setExtendedState(Frame.NORMAL);
        presentation.setVisible(true);
    }

}
