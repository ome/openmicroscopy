/*
 * org.openmicroscopy.shoola.agents.roi.RoiAgent
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

package org.openmicroscopy.shoola.agents.roi;


//Java imports
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.defs.ScreenROI;
import org.openmicroscopy.shoola.agents.roi.events.AddROICanvas;
import org.openmicroscopy.shoola.agents.roi.events.AnnotateROI;
import org.openmicroscopy.shoola.agents.roi.events.DisplayROI;
import org.openmicroscopy.shoola.agents.viewer.events.IATChanged;
import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.DSAccessException;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;
import org.openmicroscopy.shoola.env.data.DataManagementService;
import org.openmicroscopy.shoola.env.data.events.ServiceActivationRequest;
import org.openmicroscopy.shoola.env.data.model.ChannelData;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.env.rnd.events.ImageLoaded;
import org.openmicroscopy.shoola.env.rnd.events.ImageRendered;
import org.openmicroscopy.shoola.env.rnd.events.LoadImage;
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsDimensions;
import org.openmicroscopy.shoola.util.image.roi.ROI3D;
import org.openmicroscopy.shoola.util.image.roi.ROI4D;
import org.openmicroscopy.shoola.util.math.geom2D.PlaneArea;

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
public class ROIAgt
    implements Agent, AgentEventListener
{   

    /** Reference to the {@link RenderingControl}. */
    private RenderingControl        renderingControl;
    
    /** Reference to the {@link Registry}. */
    private Registry                registry;
    
    /** Reference to the {@link ROIAgtUIF view}. */
    private ROIAgtUIF               presentation;
    
    /** Reference to the {@link ROIAgtCtrl control}. */
    private ROIAgtCtrl              control;
    
    /** Map of the current ROI. */
    private Map                     listScreenROI;
    
    private int                     curImageID;
    
    private String                  imageName;
    
    private PixelsDimensions        pxsDims;
    
    private String[]                channels;
    
    private double                  magFactor;
    
    private boolean                 sameImage;
    
    private BufferedImage           imageOnScreen;
    
    private int                     curZ, curT;
    
    /** Implemented as specified by {@link Agent}. */
    public void activate() {}

    /** Implemented as specified by {@link Agent}. */
    public void terminate() {}

    /** Implemented as specified by {@link Agent}. */
    public void setContext(Registry ctx)
    {
        registry = ctx;
        curImageID = -1;
        magFactor = 1;
        sameImage = false;
        curZ = curT = -1;
        listScreenROI = new TreeMap();
        control  = new ROIAgtCtrl(this);
        EventBus bus = registry.getEventBus();
        bus.register(this, ImageLoaded.class);
        bus.register(this, DisplayROI.class);
        bus.register(this, AddROICanvas.class);
        bus.register(this, IATChanged.class);
        bus.register(this, ImageRendered.class);
    }

    /** Implemented as specified by {@link Agent}. */
    public boolean canTerminate() { return true; }

    /** Implement as specified by {@link AgentEventListener}. */
    public void eventFired(AgentEvent e)
    {
        if (e instanceof ImageLoaded) 
            handleImageLoaded((ImageLoaded) e);   
        else if (e instanceof DisplayROI) 
            handleDisplayROI((DisplayROI) e); 
        else if (e instanceof AddROICanvas) 
            handleAddROICanvas((AddROICanvas) e); 
        else if (e instanceof IATChanged)
            handleIATChanged((IATChanged) e);
        else if (e instanceof ImageRendered)
            handleImageRendered();
    }

    Registry getRegistry() { return registry; }
    
    String[] getChannels() { return channels; }
    
    Map getListScreenROI() { return listScreenROI; }
    
    int getCurrentZ() { return renderingControl.getDefaultZ(); }
    
    int getCurrentT() { return renderingControl.getDefaultT(); }
    
    double getMagFactor() { return magFactor; }
        
    /** Post an event to close the ROI widget. */
    void onOffDrawing(boolean b)
    {
        //drawOnOff = b;
        registry.getEventBus().post(new AddROICanvas(b));
    }
    
    /** Display the annotation in the viewer. */
    void displayROIDescription(int roiIndex)
    {
        ScreenROI roi = (ScreenROI) listScreenROI.get(new Integer(roiIndex));
        String text = null;
        if (roi != null) {
            text = "#"+roi.getIndex();
            if (roi.getName() != null) text += " "+roi.getName();
            else text += " "+roi.getAnnotation();
        }
        registry.getEventBus().post(new AnnotateROI(text));
    }

    /** Create a new {@link ScreenROI}. */
    void createScreenROI(int index, String name, String annotation, Color c)
    {
        PixelsDimensions pxsDims = renderingControl.getPixelsDims();
        ROI4D logicalROI = new ROI4D(pxsDims.sizeT);
        
        for (int t = 0; t < pxsDims.sizeT; t++) 
            logicalROI.setStack(new ROI3D(pxsDims.sizeZ), t);
        
        ScreenROI roi = new ScreenROI(index, name, annotation, c, logicalROI);
        listScreenROI.put(new Integer(index), roi);
    }
    
    /** Set the {@link PlaneaArea} drawn on screen. */
    void setPlaneArea(PlaneArea pa, int roiIndex)
    {
        setPlaneArea(pa, renderingControl.getDefaultZ(), 
                    renderingControl.getDefaultT(), roiIndex);
    }
    
    /** Set the {@link PlaneaArea} drawn on screen. */
    void setPlaneArea(PlaneArea pa, int z, int t, int roiIndex)
    {
        pa.scale(1/magFactor);
        ScreenROI roi = (ScreenROI) listScreenROI.get(new Integer(roiIndex));
        ROI4D logicalROI = roi.getLogicalROI();
        logicalROI.setPlaneArea(pa, z, t);
    }

    PlaneArea getPlaneArea(int z, int t, int roiIndex)
    {
        ScreenROI roi = (ScreenROI) listScreenROI.get(new Integer(roiIndex));
        ROI4D logicalROI = roi.getLogicalROI();
        return logicalROI.getPlaneArea(z, t);
    }
    
    ScreenROI getScreenROI(int roiIndex)
    {
        return (ScreenROI) listScreenROI.get(new Integer(roiIndex));
    }

    void removeAllPlaneAreas()
    {
        Iterator i = listScreenROI.values().iterator();
        ScreenROI roi;
        while (i.hasNext()) {
            roi = (ScreenROI) i.next();
            roi.getLogicalROI().setPlaneArea(null, getCurrentZ(), 
                                            getCurrentT());
        }
    }
    
    void copyPlaneArea(PlaneArea pa, int index, int newZ, int newT)
    {
        ScreenROI roi = (ScreenROI) listScreenROI.get(new Integer(index));
        if (roi != null) 
            roi.getLogicalROI().setPlaneArea(pa, newZ, newT); 
    }
    
    void copyAcrossZ(PlaneArea pa, int index, int from, int to, int t)
    {
        ScreenROI roi = (ScreenROI) listScreenROI.get(new Integer(index));
        if (roi != null) roi.copyAcrossZ(pa, from, to, t);
    }
    
    void copyAcrossT(PlaneArea pa, int index, int from, int to, int z)
    {
        ScreenROI roi = (ScreenROI) listScreenROI.get(new Integer(index));
        if (roi != null) roi.copyAcrossT(pa, from, to, z);
    }
    
    void copyStackAcrossT(int index, int from, int to)
    {
        ScreenROI roi = (ScreenROI) listScreenROI.get(new Integer(index));
        if (roi != null) roi.copyStackAcrossT(from, to, pxsDims.sizeZ);
    }
    
    void copyStack(int index, int from, int to)
    {
        ScreenROI roi = (ScreenROI) listScreenROI.get(new Integer(index));
        if (roi != null) roi.copyStack(from, to, pxsDims.sizeZ);
    }

    BufferedImage getImageOnScreen() { return imageOnScreen; }
    
    /** Handle the event @see ImageRendered. */
    private void handleImageRendered()
    {
        if (presentation == null) buildPresentation();
        if (listScreenROI.size() != 0) {
            if (curZ != renderingControl.getDefaultZ() || 
                curT != renderingControl.getDefaultT()) 
                control.paintScreenROIs(renderingControl.getDefaultZ(), 
                        renderingControl.getDefaultT(), magFactor);
        }
        curZ = renderingControl.getDefaultZ();
        curT = renderingControl.getDefaultT();
    }
    
    /** Handle the event @see IATChanged. */
    private void handleIATChanged(IATChanged response)
    {
        double oldMagFactor = magFactor;
        magFactor = response.getAffineTransform().getMagFactor();
        imageOnScreen = response.getImageDisplayed();
        if (listScreenROI.size() != 0) 
            control.magnifyScreenROIs(magFactor/oldMagFactor);
    }
    
    /** Handle the event @see ImageLoaded. */
    private void handleImageLoaded(ImageLoaded response)
    {
        LoadImage request = (LoadImage) response.getACT();
        if (request.getImageID() != curImageID) {
            renderingControl = response.getProxy();
            curImageID = request.getImageID();
            pxsDims = renderingControl.getPixelsDims();
            imageName = request.getImageName();
            initChannels();
            sameImage = false;
            curZ = renderingControl.getDefaultZ();
            curT = renderingControl.getDefaultT();
            if (presentation != null) removePresentation();   
        }         
    }
    
    /** Handle the event @see DisplayROI. */
    private void handleDisplayROI(DisplayROI response)
    {
        if (response.getDrawingCanvas() != null) { //close window
            if (control.getDrawingCanvas() == null)
                control.setDrawingCanvas(response.getDrawingCanvas());
            presentation.deIconify();
        } else removePresentation();
    }
    
    /** Handle the event @see AddROICanvas. */
    private void handleAddROICanvas(AddROICanvas response)
    {
        if (response.isOnOff()) presentation.deIconify();
    }

    
    /** Initializes the channel information. */
    private void initChannels() 
    {
        channels = new String[pxsDims.sizeW];
        //default.
        for (int i = 0; i < pxsDims.sizeW; i++)
            channels[i] = ""+i;
        try {
            DataManagementService ds = registry.getDataManagementService();
            ChannelData[] channelData = ds.getChannelData(curImageID); 
            if (channelData != null && channelData.length == pxsDims.sizeW) {
                for (int i = 0; i < channelData.length; i++)
                    channels[i] = ""+channelData[i].getNanometer();
            }
        } catch(DSAccessException dsae) {
            String s = "Can't retrieve the channel data for "+curImageID+".";
            registry.getLogger().error(this, s+" Error: "+dsae); 
            registry.getUserNotifier().notifyError("Data Retrieval Failure", s,
                                                    dsae);
        } catch(DSOutOfServiceException dsose) {    
            ServiceActivationRequest request = new ServiceActivationRequest(
                                    ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        } 
    }
        
    /** Build the AgentUIF. */
    private void buildPresentation()
    {
        presentation = new ROIAgtUIF(control, imageName, pxsDims.sizeT, 
                        pxsDims.sizeZ);
        control.setPresentation(presentation);
    }
    
    /** 
     * Remove and rebuild the presentation. 
     * The method is invoked when a new image is loaded and the presentation
     * is displayed.
     *
     */
    private void removePresentation()
    {
        presentation.dispose();
        presentation = null;
    }
    
}
