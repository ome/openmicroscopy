/*
 * org.openmicroscopy.shoola.agents.iviewer.ImViewerAgent
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.imviewer;



//Java imports
import java.awt.Rectangle;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.FocusGainedEvent;
import org.openmicroscopy.shoola.agents.events.iviewer.CopyRndSettings;
import org.openmicroscopy.shoola.agents.events.iviewer.ImageViewport;
import org.openmicroscopy.shoola.agents.events.iviewer.MeasurementTool;
import org.openmicroscopy.shoola.agents.events.iviewer.RndSettingsCopied;
import org.openmicroscopy.shoola.agents.events.iviewer.SaveRelatedData;
import org.openmicroscopy.shoola.agents.events.iviewer.ViewImage;
import org.openmicroscopy.shoola.agents.events.measurement.MeasurementToolLoaded;
import org.openmicroscopy.shoola.agents.events.measurement.SelectPlane;
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewerFactory;
import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.env.event.EventBus;
import pojos.DataObject;
import pojos.ExperimenterData;
import pojos.PixelsData;

/** 
 * The ImViewer agent. This agent displays an <code>Image</code> and the 
 * controls to modify the rendering settings.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">
 *              donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class ImViewerAgent
    implements Agent, AgentEventListener
{

    /** The default error message. */
    public static final String ERROR = " An error occured while modifying  " +
    		"the rendering settings.";
    
    /** Reference to the registry. */
    private static Registry         registry; 
    
    /**
     * Helper method. 
     * 
     * @return A reference to the <code>Registry</code>.
     */
    public static Registry getRegistry() { return registry; }
    
    /**
     * Returns <code>true</code> if the openGL flag is turned on,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public static boolean hasOpenGLSupport()
	{
		Boolean support = (Boolean) getRegistry().lookup("/library/opengl");
		return support.booleanValue();
	}
    
    /**
	 * Helper method returning the current user's details.
	 * 
	 * @return See above.
	 */
	public static ExperimenterData getUserDetails()
	{ 
		return (ExperimenterData) registry.lookup(
								LookupNames.CURRENT_USER_DETAILS);
	}
	
	/**
	 * Helper method returning <code>true</code> if the connection is fast,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public static boolean isFastConnection()
	{
		int value = (Integer) registry.lookup(LookupNames.CONNECTION_SPEED);
		return value == ImViewer.UNCOMPRESSED;
	}
	
    /**
     * Handles the {@link ViewImage} event.
     * 
     * @param evt The event to handle.
     */
    private void handleViewImage(ViewImage evt)
    {
        if (evt == null) return;
        DataObject image = evt.getImage();
        Rectangle r = evt.getRequesterBounds();
        ImViewer view;
        boolean b = evt.isSeparateWindow();
        if (image != null)
        	view = ImViewerFactory.getImageViewer(image, r, b);
        else
        	view = ImViewerFactory.getImageViewer(evt.getImageID(), r, b);
        if (view != null) {
        	view.activate(evt.getSettings(), evt.getSelectedUserID());
        	view.setContext(evt.getParent(), evt.getGrandParent());
        }
    }
    
    /**
     * Handles the {@link SaveRelatedData} event.
     * 
     * @param evt The event to handle.
     */
    private void handleSaveRelatedData(SaveRelatedData evt)
    {
        if (evt == null) return;
        ImViewerFactory.storeEvent(evt);
    }
    
    /**
     * Handles the {@link MeasurementToolLoaded} event.
     * 
     * @param evt The event to handle.
     */
    private void handleMeasurementToolLoaded(MeasurementToolLoaded evt)
    {
    	if (evt == null) return;
    	MeasurementTool request = (MeasurementTool) evt.getACT();
    	PixelsData pixels = request.getPixels();
    	if (pixels == null) return;
    	long pixelsID = pixels.getId();
    	ImViewer view = ImViewerFactory.getImageViewer(pixelsID);
    	if (view != null) {
    		switch (evt.getIndex()) {
				case MeasurementToolLoaded.ADD:
					view.addToView(evt.getView());
					break;
				case MeasurementToolLoaded.REMOVE:
					view.removeFromView(evt.getView());
			}
    	}
    }
    
    /**
     * Handles the {@link SelectPlane} event.
     * 
     * @param evt The event to handle.
     */
    private void handleSelectPlane(SelectPlane evt)
    {
    	if (evt == null) return;
    	long pixelsID = evt.getPixelsID();
    	ImViewer view = ImViewerFactory.getImageViewer(pixelsID);
    	if (view != null) 
    		view.setSelectedXYPlane(evt.getDefaultZ(), evt.getDefaultT());
    }
    
    /**
     * Handles the {@link CopyRndSettings} event.
     * 
     * @param evt The event to handle.
     */
    public void handleCopyRndSettings(CopyRndSettings evt)
    {
    	if (evt == null) return;
    	ImViewerFactory.copyRndSettings(evt.getImage());
    }
    
    /**
     * Indicates to bring up the window if a related window gained focus
     * 
     * @param evt The event to handle.
     */
    private void handleFocusGainedEvent(FocusGainedEvent evt)
    {
    	ImViewer viewer = ImViewerFactory.getImageViewer(
				evt.getPixelsID());
    	if (viewer == null) return;
    	if (viewer.getState() != ImViewer.DISCARDED ||
    		evt.getIndex() != FocusGainedEvent.VIEWER_FOCUS) {
			//viewer.toFront();
		}
    }
    
    /**
     * Reloads the rendering engine if the settings of an active viewer
     * have been updated.
     * 
     * @param evt The event to handle.
     */
    private void handleRndSettingsCopiedEvent(RndSettingsCopied evt)
    {
    	if (evt == null) return;
    	ImViewerFactory.reloadRenderingEngine(evt.getImagesIDs(), 
    							evt.getRefPixelsID());
    }
    
    /**
     * Displays the passed rectangle if possible
     * 
     * @param evt The event to handle.
     */
    private void handleImageViewportEvent(ImageViewport evt)
    {
    	if (evt == null) return;
    	ImViewer viewer = ImViewerFactory.getImageViewer(
				evt.getPixelsID());
    	if (viewer == null) return;
    	viewer.scrollToViewport(evt.getBounds());
    }
    
    /** Creates a new instance. */
    public ImViewerAgent() {}
    
    /**
     * Implemented as specified by {@link Agent}.
     * @see Agent#activate()
     */
    public void activate() {}

    /**
     * Implemented as specified by {@link Agent}. 
     * @see Agent#terminate()
     */
    public void terminate() {}

    /** 
     * Implemented as specified by {@link Agent}. 
     * @see Agent#setContext(Registry)
     */
    public void setContext(Registry ctx)
    {
        registry = ctx;
        EventBus bus = registry.getEventBus();
        bus.register(this, ViewImage.class);
        bus.register(this, MeasurementToolLoaded.class);
        bus.register(this, SelectPlane.class);
        bus.register(this, CopyRndSettings.class);
        bus.register(this, SaveRelatedData.class);
        bus.register(this, FocusGainedEvent.class);
        bus.register(this, RndSettingsCopied.class);
        bus.register(this, ImageViewport.class);
    }

    /**
     * Implemented as specified by {@link Agent}. 
     * @see Agent#canTerminate()
     */
    public boolean canTerminate()
    { 
    	//Map m = ImViewerFactory.hasDataToSave();
    	return true; 
    }

    /**
     * Implemented as specified by {@link Agent}. 
     * @see Agent#hasDataToSave()
     */
    public Map<String, Set> hasDataToSave()
    {
		// TODO Auto-generated method stub
		return ImViewerFactory.hasDataToSave();
	}
    
    /**
     * Responds to an event fired trigger on the bus.
     * Listens to ViewImage event.
     * @see AgentEventListener#eventFired(AgentEvent)
     */
    public void eventFired(AgentEvent e)
    {
        if (e instanceof ViewImage) handleViewImage((ViewImage) e);
        else if (e instanceof MeasurementToolLoaded)
        	handleMeasurementToolLoaded((MeasurementToolLoaded) e);
        else if (e instanceof SelectPlane)
        	handleSelectPlane((SelectPlane) e);
        else if (e instanceof CopyRndSettings)
        	handleCopyRndSettings((CopyRndSettings) e);
        else if (e instanceof SaveRelatedData)
        	handleSaveRelatedData((SaveRelatedData) e);
        else if (e instanceof FocusGainedEvent)
			handleFocusGainedEvent((FocusGainedEvent) e);
        else if (e instanceof RndSettingsCopied)
			handleRndSettingsCopiedEvent((RndSettingsCopied) e);
        else if (e instanceof ImageViewport)
			handleImageViewportEvent((ImageViewport) e);
    }

}
