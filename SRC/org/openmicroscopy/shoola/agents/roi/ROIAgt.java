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


//Third-party libraries

//Application-internal dependencies
import java.awt.Color;

import org.openmicroscopy.shoola.agents.roi.defs.ROISettings;
import org.openmicroscopy.shoola.agents.roi.events.AddROICanvas;
import org.openmicroscopy.shoola.agents.roi.events.DisplayROI;
import org.openmicroscopy.shoola.agents.roi.events.IATChanged;
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
import org.openmicroscopy.shoola.env.rnd.events.LoadImage;
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
public class ROIAgt
    implements Agent, AgentEventListener
{   
    
    public static final Color       STEELBLUE = new Color(0x4682B4);
    
    public static final int         INDEX_T = 0;
    public static final int         INDEX_Z = 1;
    public static final int         INDEX_ZT = 2;
    
    public static final int         MOVING = 1;
    
    public static final int         CONSTRUCTING = 2;
    
    public static final int         RESIZING = 3;
    
    /** Reference to the {@link Registry}. */
    private Registry                registry;
    
    private ROIAgtUIF               presentation;
    
    private ROIAgtCtrl              control;
    
    private int                     curImageID, curPixelsID;
    
    private String[]                channels;
    
    private boolean                 drawOnOff, canvasUp, postedAdd;
    
    private ROISettings             roiSettings;
    
    /** Implemented as specified by {@link Agent}. */
    public void activate() {}

    /** Implemented as specified by {@link Agent}. */
    public void terminate() {}

    /** Implemented as specified by {@link Agent}. */
    public void setContext(Registry ctx)
    {
        registry = ctx;
        drawOnOff = true;
        curImageID = -1;
        EventBus bus = registry.getEventBus();
        bus.register(this, ImageLoaded.class);
        bus.register(this, DisplayROI.class);
        bus.register(this, AddROICanvas.class);
        bus.register(this, IATChanged.class);
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
    }

    Registry getRegistry() { return registry; }
    
    String[] getChannels() { return channels; }
    
    void setDrawOnOff(boolean b) { drawOnOff = b; }
        
    /** Post an event to close the ROI widget. */
    void onOffDrawing(boolean b)
    {
        drawOnOff = b;
        postedAdd = true;
        registry.getEventBus().post(new AddROICanvas(b));
    }
    
    /** Handle the event @see IATChanged. */
    private void handleIATChanged(IATChanged response)
    {
        if (control != null) {
            control.setImageAffineTransform(response.getAffineTransform());
            if (drawOnOff && canvasUp) control.repaintDrawingCanvas(); 
        } 
    }
    
    /** Handle the event @see ImageLoaded. */
    private void handleImageLoaded(ImageLoaded response)
    {
        LoadImage request = (LoadImage) response.getACT();
        if (request.getImageID() != curImageID) {
            RenderingControl renderingControl = response.getProxy();
            curImageID = request.getImageID();
            curPixelsID = request.getPixelsID();
            PixelsDimensions pxsDims = renderingControl.getPixelsDims();
            initChannels(pxsDims);
            if (control != null && canvasUp) control.setDefault();
            if (presentation != null) removePresentation();
            buildPresentation(request.getImageName(), pxsDims);   
        }         
    }
    
    /** Handle the event @see DisplayROI. */
    private void handleDisplayROI(DisplayROI response)
    {
        if (response.getDrawingCanvas() != null) { //close window
            canvasUp = true;
            control.setImageAffineTransform(response.getAffineTransform());
            if (control.getDrawingCanvas() == null)
                control.setDrawingCanvas(response.getDrawingCanvas());
            bringUpPresentation();
        } else removePresentation();
    }
    
    /** Handle the event @see AddROICanvas. */
    private void handleAddROICanvas(AddROICanvas response)
    {
        if (response.isOnOff()) bringUpPresentation();
        else if (!postedAdd) closePresentation();
        postedAdd = false;
    }
    
    private void closePresentation()
    {
        setDrawOnOff(true);
        presentation.dispose();
    }
    
    private void bringUpPresentation()
    {
        presentation.setDrawOnOff(drawOnOff);
        presentation.deIconify();
    }
    
    /** Initializes the channel information. */
    private void initChannels(PixelsDimensions pxsDims) 
    {
        channels = new String[pxsDims.sizeW];
        //default.
        for (int i = 0; i < pxsDims.sizeW; i++)
            channels[i] = ""+i;
        try {
            DataManagementService ds = registry.getDataManagementService();
            ChannelData[] channelData = ds.getChannelData(curImageID); 
            if (channelData.length == pxsDims.sizeW){
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
    private void buildPresentation(String imageName, PixelsDimensions pxsDims)
    {
        int maxZ = pxsDims.sizeZ-1, maxT = pxsDims.sizeT-1;
        if (roiSettings == null)
            roiSettings = new ROISettings(0, maxZ, 0, maxT);
        control  = new ROIAgtCtrl(this);
        presentation = new ROIAgtUIF(control, registry, imageName, maxT, maxZ, 
                                        roiSettings);
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
        roiSettings = null;
        control = null;
        presentation = null;
        canvasUp = false;
    }
    
}
