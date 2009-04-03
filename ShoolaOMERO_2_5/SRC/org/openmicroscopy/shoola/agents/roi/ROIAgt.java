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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.roi.AddROICanvas;
import org.openmicroscopy.shoola.agents.events.roi.AnnotateROI;
import org.openmicroscopy.shoola.agents.events.roi.DisplayROI;
import org.openmicroscopy.shoola.agents.events.viewer.DisplayViewerRelatedAgent;
import org.openmicroscopy.shoola.agents.events.viewer.IATChanged;
import org.openmicroscopy.shoola.agents.roi.defs.ScreenROI;
import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.DSAccessException;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;
import org.openmicroscopy.shoola.env.data.SemanticTypesService;
import org.openmicroscopy.shoola.env.data.events.ServiceActivationRequest;
import org.openmicroscopy.shoola.env.data.model.ChannelData;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.env.rnd.events.AnalyzeROIs;
import org.openmicroscopy.shoola.env.rnd.events.ImageLoaded;
import org.openmicroscopy.shoola.env.rnd.events.ImageRendered;
import org.openmicroscopy.shoola.env.rnd.events.LoadImage;
import org.openmicroscopy.shoola.env.rnd.events.ROIAnalysisResults;
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsDimensions;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.image.roi.ROI3D;
import org.openmicroscopy.shoola.util.image.roi.ROI4D;
import org.openmicroscopy.shoola.util.image.roi.ROI5D;
import org.openmicroscopy.shoola.util.math.geom2D.PlaneArea;

/** 
 * The ROI agent. This agent displays the selection widgets and 
 * manages the ROI selection. 
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
    
    /** List of the current ROI. */
    private List                    listScreenROI;
    
    private List                    analyzedROI;
    
    private String[]                analyzedChannels;
    
    /** ID of the current image loaded. */
    private int                     curImageID;
    
    /** Name of the current image loaded. */
    private String                  imageName;
    
    /** Dimensions of the set of Pixels loaded. */
    private PixelsDimensions        pxsDims;
    
    private String[]                channels;
    
    /** Magnification factor of the image currently in the viewer. */
    private double                  magFactor;
    
    /** Image currently displayed in the viewer. */
    private BufferedImage           imageOnScreen;
    
    /** Current z-section and timepoint. */
    private int                     curZ, curT;
    
    private Map                     roiResults, channelsMap;
    
    /** Implemented as specified by {@link Agent}. */
    public void activate() {}

    /** Implemented as specified by {@link Agent}. */
    public void terminate() {}

    /** Implemented as specified by {@link Agent}. */
    public void setContext(Registry ctx)
    {
        registry = ctx;
        //Initializes and sets the default.
        curImageID = -1;
        magFactor = 1;
        curZ = curT = -1;
        listScreenROI = new ArrayList();
        analyzedROI = new ArrayList();
        channelsMap = new HashMap();
        control  = new ROIAgtCtrl(this);
        EventBus bus = registry.getEventBus();
        bus.register(this, ImageLoaded.class);
        bus.register(this, DisplayROI.class);
        bus.register(this, AddROICanvas.class);
        bus.register(this, IATChanged.class);
        bus.register(this, ImageRendered.class);
        bus.register(this, ROIAnalysisResults.class);
        bus.register(this, DisplayViewerRelatedAgent.class);
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
        else if (e instanceof ROIAnalysisResults)
            handleROIAnalysisResults((ROIAnalysisResults) e);
        else if (e instanceof DisplayViewerRelatedAgent)
            handleDisplayViewerRelatedAgents((DisplayViewerRelatedAgent) e);
    }

    /** Return reference to the {@link Registry}. */
    Registry getRegistry() { return registry; }
    
    /** Return an array with wavelength values. */
    String[] getChannels() { return channels; }
    
    List getListScreenROI() { return listScreenROI; }
    
    List getAnalyzedROI() { return analyzedROI; }
    
    String[] getAnalyzedChannels() { return analyzedChannels; }
    
    Map getChannelsMap() { return channelsMap; }
    
    Map getROIResults() {return roiResults; }
    
    int getAnalyzedChannel(int index) 
    {
        return ((Integer) channelsMap.get(new Integer(index))).intValue();
    }
    
    double getPixelSizeX() { return pxsDims.pixelSizeX; }
    
    int getCurrentZ() { return renderingControl.getDefaultZ(); }
    
    int getCurrentT() { return renderingControl.getDefaultT(); }
    
    double getMagFactor() { return magFactor; }
    
    BufferedImage getImageOnScreen() { return imageOnScreen; }
    
    /** Post an event to remove the ROICanvas from the Viewer layer. */
    void removeDrawingCanvas()
    {
        registry.getEventBus().post(new AddROICanvas(false));
    }
    
    /** Prepare the roi5D object and post an event to compute the statistics. */
    void computeROIStatistics(List selectedChannels, List selectedROIs)
    {
        if (!listScreenROI.isEmpty()) { //now we prepare the ROI5D object.
            //remove all previous analyzed selection
            analyzedROI.removeAll(analyzedROI); 
            createAnalyzedChannels(selectedChannels);
            ScreenROI roi;
            ROI5D roi5D;
            Iterator j = selectedROIs.iterator();
            ROI5D[] rois = new ROI5D[selectedROIs.size()];
            int c = 0;
            int index;
            while (j.hasNext()) {
                index = ((Integer) j.next()).intValue();
                roi = (ScreenROI) listScreenROI.get(index);
                if (roi != null && roi.getIndex() == index) {
                    roi5D = createROI5DElement(roi, selectedChannels);
                    rois[c++] = roi5D;
                    roi.setActualROI(roi5D); 
                    analyzedROI.add(roi);
                }
            }
            registry.getEventBus().post(new AnalyzeROIs(rois)); 
        } else {
            UserNotifier un = registry.getUserNotifier();
            un.notifyInfo("Invalid selection", "No ROI selection."); 
        }
    }

    /** Display the annotation in the viewer. */
    void displayROIDescription(int roiIndex)
    {
        if (roiIndex < 0) {
            registry.getEventBus().post(new AnnotateROI(null));
            return;
        }
        ScreenROI roi = (ScreenROI) listScreenROI.get(roiIndex);
        String text = null;
        if (roi != null) {
            text = "#"+roi.getIndex();
            if (roi.getName() != null) text += " "+roi.getName();
            else text += " "+roi.getAnnotation();
        }
        registry.getEventBus().post(new AnnotateROI(text));
    }

    /** Remove the {@link ScreenROI}  from the list. */
    void removeScreenROI(int index)
    {
        Iterator i = listScreenROI.iterator();
        ScreenROI roi;
        int j;
        while (i.hasNext()) {
            roi = (ScreenROI) i.next();
            j = roi.getIndex();
            if (j > index) roi.setIndex(j-1);
        }
        listScreenROI.remove(index);
    }
    
    /** Create a new {@link ScreenROI} and add it to the list. */
    void createScreenROI(int index, String name, String annotation, Color c)
    {
        PixelsDimensions pxsDims = renderingControl.getPixelsDims();
        ROI4D logicalROI = new ROI4D(pxsDims.sizeT);
        
        //Should be modified, need to review the ROI array concept.
        for (int t = 0; t < pxsDims.sizeT; t++) 
            logicalROI.setStack(new ROI3D(pxsDims.sizeZ), t);
        
        ScreenROI roi = new ScreenROI(index, name, annotation, c, logicalROI);
        listScreenROI.add(roi);
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
        ScreenROI roi = (ScreenROI) listScreenROI.get(roiIndex);
        ROI4D logicalROI = roi.getLogicalROI();
        logicalROI.setPlaneArea(pa, z, t);
    }

    void moveResizePlaneArea(PlaneArea pa, int roiIndex)
    {
        pa.scale(1/magFactor);
        ScreenROI roi = (ScreenROI) listScreenROI.get(roiIndex);
        ROI4D logicalROI = roi.getLogicalROI();
        int t = getCurrentT();
        PlaneArea copy;
        for (int z = 0; z < pxsDims.sizeZ; z++) {
            if (logicalROI.getPlaneArea(z, t) != null) {
                copy = (PlaneArea) (pa.copy());
                logicalROI.setPlaneArea(copy, z, t);
            }
        }
    }
    
    /** 
     * Retrieve the {@link PlaneaArea} at the specified position.
     * 
     * @param z         z-section
     * @param t         timepoint
     * @param roiIndex  ROI index.
     * @return
     */
    PlaneArea getPlaneArea(int z, int t, int roiIndex)
    {
        ScreenROI roi = (ScreenROI) listScreenROI.get(roiIndex);
        ROI4D logicalROI = roi.getLogicalROI();
        return logicalROI.getPlaneArea(z, t);
    }
    
    ScreenROI getScreenROI(int roiIndex)
    {
        return (ScreenROI) listScreenROI.get(roiIndex);
    }

    /** Remove all {@link PlaneArea}s from the current ROI4D object. */
    void removeAllPlaneAreas()
    {
        Iterator i = listScreenROI.iterator();
        ScreenROI roi;
        while (i.hasNext()) {
            roi = (ScreenROI) i.next();
            roi.getLogicalROI().setPlaneArea(null, getCurrentZ(), 
                                            getCurrentT());
        }
    }
    
    /** 
     * Copy the specified {@link PlaneArea} at the specified position.
     * 
     * @param pa    {@link PlaneArea} to copy.
     * @param index roi index.
     * @param newZ  z-section.
     * @param newT  timepoint.
     */
    void copyPlaneArea(PlaneArea pa, int index, int newZ, int newT)
    {
        ScreenROI roi = (ScreenROI) listScreenROI.get(index);
        if (roi != null) 
            roi.getLogicalROI().setPlaneArea(pa, newZ, newT); 
    }
    
    /** 
     * Fill the interval defined by the positions 
     * <code>(from, t)</code> and <code>(to, t)</code>.
     * with the the specified {@link PlaneArea}.
     * 
     * @param pa    {@link PlaneArea} to copy.
     * @param index roi index.
     * @param from  start z-section.
     * @param to    end z-section.
     * @param t     timepoint. 
     */
    void copyAcrossZ(PlaneArea pa, int index, int from, int to, int t)
    {
        ScreenROI roi = (ScreenROI) listScreenROI.get(index);
        if (roi != null) roi.copyAcrossZ(pa, from, to, t);
    }
    
    /**
     * Fill the interval defined by the positions 
     * <code>(z, from)</code> and <code>(z, to)</code>.
     * with the the specified {@link PlaneArea}.
     * 
     * @param pa    {@link PlaneArea} to copy.
     * @param index roi index.
     * @param from  start timepoint.
     * @param to    end timepoint.
     * @param z     z-section.
     */
    void copyAcrossT(PlaneArea pa, int index, int from, int to, int z)
    {
        ScreenROI roi = (ScreenROI) listScreenROI.get(index);
        if (roi != null) roi.copyAcrossT(pa, from, to, z);
    }
    
    /**
     * Fill the area defined by the positions 
     * <code>(fromZ, fromT)</code> and <code>(toZ, toT)</code>.
     * with the the specified {@link PlaneArea}.
     * 
     * @param pa        {@link PlaneArea} to copy.
     * @param index     roi index.
     * @param fromZ     z-section.
     * @param toZ       z-section.
     * @param fromT     timepoint.
     * @param toT       timepoint.
     */
    void copyAcrossZAndT(PlaneArea pa, int index, int fromZ, int toZ, int fromT,
                            int toT)
    {
        ScreenROI roi = (ScreenROI) listScreenROI.get(index);
        if (roi != null) roi.copyAcrossZAndT(pa, fromZ, toZ, fromT, toT);
    }
    
    /**
     * Copy the stack from timepoint <code>from</code> to timepoint 
     * <code>to</code>.
     * 
     * @param index roi index.
     * @param from  start timepoint.
     * @param to    end timepoint.
     */
    void copyStackAcrossT(int index, int from, int to)
    {
        ScreenROI roi = (ScreenROI) listScreenROI.get(index);
        if (roi != null) roi.copyStackAcrossT(from, to, pxsDims.sizeZ);
    }
    
    /**
     * Copy the stack positioned at timepoint <code>from</code> into timepoint 
     * <code>to</code>.
     * 
     * @param index roi index.
     * @param from  start timepoint.
     * @param to    end timepoint.
     */
    void copyStack(int index, int from, int to)
    {
        ScreenROI roi = (ScreenROI) listScreenROI.get(index);
        if (roi != null) roi.copyStack(from, to, pxsDims.sizeZ);
    }
    
    /** Handle the event @see DisplayViewerRelatedAgents. */
    private void handleDisplayViewerRelatedAgents(DisplayViewerRelatedAgent 
                                                    response)
    {
        if (!response.isOnOff())    removePresentation();
    }
    
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
        magFactor = response.getAffineTransform().getMagFactor();
        imageOnScreen = response.getImageDisplayed();
        if (listScreenROI.size() != 0) 
            control.magnifyScreenROIs();
            
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
            curZ = renderingControl.getDefaultZ();
            curT = renderingControl.getDefaultT();
            if (presentation != null) removePresentation();  
            listScreenROI.clear();  //clear the map
            control.clearScreenPlaneAreas();
        }         
    }
    
    /** Handle the event @see DisplayROI. */
    private void handleDisplayROI(DisplayROI response)
    {
        //if (response.getDrawingCanvas() != null) { //close window
            //if (control.getDrawingCanvas() == null)
            control.setDrawingCanvas(response.getDrawingCanvas());
            if (presentation == null) buildPresentation();
            presentation.deIconify();
        //} else removePresentation();
    }
    
    /** Handle the event @see AddROICanvas. */
    private void handleAddROICanvas(AddROICanvas response)
    {
        if (response.isOnOff()) 
            presentation.deIconify();
    }

    private void handleROIAnalysisResults(ROIAnalysisResults response)
    {
        roiResults = response.getResults();
        control.displayROIAnalysisResults(pxsDims.sizeT, pxsDims.sizeZ);
    }
    
    /** Initializes the channel information. */
    private void initChannels() 
    {
        channels = new String[pxsDims.sizeW];
        //default.
        for (int i = 0; i < pxsDims.sizeW; i++)
            channels[i] = ""+i;
        try {
            SemanticTypesService sts = registry.getSemanticTypesService();
            ChannelData[] channelData = sts.getChannelData(curImageID); 
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
        if (presentation != null) presentation.dispose();
        presentation = null;
    }
    

    /** 
     * Create an {@link ROI5D} object.
     * 
     * @param roi   corresponding GUI 4D-object.
     * @param selectedChannels list of channels.
     *  */
    private ROI5D createROI5DElement(ScreenROI roi, List selectedChannels)
    {
        ROI5D roi5D = new ROI5D(channels.length);
        Iterator i = selectedChannels.iterator();
        while (i.hasNext()) 
            roi5D.setChannel(roi.getLogicalROI(), 
                            ((Integer) i.next()).intValue());
        return roi5D;
    }
    
    private void createAnalyzedChannels(List l)
    {
        analyzedChannels = new String[l.size()];
        Iterator i = l.iterator();
        Integer index;
        int c = 0;
        while (i.hasNext()) {
            index = (Integer) i.next();
            analyzedChannels[c] = channels[index.intValue()];
            channelsMap.put(new Integer(c), index);
            c++;
        }
    }
}
