/*
 * org.openmicroscopy.shoola.agents.measurement.MeasurementAgent 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.measurement;


//Java imports
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.FocusGainedEvent;
import org.openmicroscopy.shoola.agents.events.SaveData;
import org.openmicroscopy.shoola.agents.events.iviewer.ChannelSelection;
import org.openmicroscopy.shoola.agents.events.iviewer.ImageRendered;
import org.openmicroscopy.shoola.agents.events.iviewer.MeasurePlane;
import org.openmicroscopy.shoola.agents.events.iviewer.MeasurementTool;
import org.openmicroscopy.shoola.agents.events.iviewer.ViewerState;
import org.openmicroscopy.shoola.agents.measurement.view.MeasurementViewer;
import org.openmicroscopy.shoola.agents.measurement.view.MeasurementViewerFactory;
import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.Environment;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.events.ReconnectedEvent;
import org.openmicroscopy.shoola.env.data.events.UserGroupSwitched;
import org.openmicroscopy.shoola.env.data.util.AgentSaveInfo;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.ui.ActivityComponent;
import org.openmicroscopy.shoola.env.ui.ActivityProcessEvent;
import org.openmicroscopy.shoola.env.ui.DeleteActivity;

import pojos.ExperimenterData;
import pojos.PixelsData;

/** 
 * The Measurement agent. This agent displays 
 * controls to create and manipulate Regions of Interest.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class MeasurementAgent 
	implements Agent, AgentEventListener
{

	/** Reference to the registry. */
    private static Registry         registry;
    
    /**
     * Reacts to the passed event.
     * 
     * @param evt The event to handle.
     */
    private void handleMeasurementToolEvent(MeasurementTool evt)
    {
    	PixelsData pixels = evt.getPixels();
    	if (pixels == null) return;
    	MeasurementViewer viewer = MeasurementViewerFactory.getViewer(
    			pixels.getId());
    	if (viewer == null) {
    		viewer = MeasurementViewerFactory.getViewer(
        			evt.getPixels(), evt.getImageID(), evt.getName(),
        			evt.getRequesterBounds(), evt.getDefaultZ(), 
        			evt.getDefaultT(), evt.getMagnification(), 
        			evt.getActiveChannels(), evt.getChannelData());
    	}
    	if (viewer != null) {
    		viewer.setIconImage(evt.getThumbnail());
    		viewer.setRndImage(evt.getRenderedImage());
    		MeasurementViewerFactory.addRequest(evt);
    		viewer.activate(evt.getMeasurements(), evt.isHCSData(),
    				evt.isBigImage());
    	}
    }

    /**
     * Reacts to the passed event.
     * 
     * @param evt The event to handle.
     */
    private void handleMeasurePlaneEvent(MeasurePlane evt)
    {
    	MeasurementViewer viewer = MeasurementViewerFactory.getViewer(
    									evt.getPixelsID());
    	if (viewer != null) 
    		viewer.setMagnifiedPlane(evt.getDefaultZ(), evt.getDefaultT(), 
    				evt.getMagnification());
    }
    
    /**
     * Reacts to the passed event.
     * 
     * @param evt The event to handle.
     */
    private void handleViewerStateEvent(ViewerState evt)
    {
    	MeasurementViewer viewer = MeasurementViewerFactory.getViewer(
    									evt.getPixelsID());
    	if (viewer != null) {
    		switch (evt.getIndex()) {
				case ViewerState.CLOSE:
					viewer.discard();
					break;
				case ViewerState.ICONIFIED:
					viewer.iconified(false);
					break;
				case ViewerState.DEICONIFIED:
					viewer.iconified(true);
			}
    	}
    }
    
    /**
     * Indicates to bring up the window if a related window gained focus
     * 
     * @param evt The event to handle.
     */
    private void handleFocusGainedEvent(FocusGainedEvent evt)
    {
    	MeasurementViewer viewer = MeasurementViewerFactory.getViewer(
				evt.getPixelsID());
    	if (viewer == null) return;
    	if (viewer.getState() != MeasurementViewer.DISCARDED ||
    		evt.getIndex() != FocusGainedEvent.MEASUREMENT_TOOL_FOCUS) {
			//viewer.toFront();
		}
    }
    
    /**
     * Reacts to the passed event.
     * 
     * @param evt The event to handle.
     */
    private void handleChannelSelectionEvent(ChannelSelection evt)
    {
    	MeasurementViewer viewer = MeasurementViewerFactory.getViewer(
    									evt.getPixelsID());
    	if (viewer != null) {
    		switch (evt.getIndex()) {
				case ChannelSelection.CHANNEL_SELECTION:
					viewer.setActiveChannels(evt.getChannels());
					break;
				case ChannelSelection.COLOR_SELECTION:
					viewer.setActiveChannelsColor(evt.getChannels());
					break;
			}
    	}
    }
    
    /**
     * Reacts to the passed event.
     * 
     * @param evt The event to handle.
     */
    private void handleSaveData(SaveData evt)
    {
    	MeasurementViewer viewer = MeasurementViewerFactory.getViewer(
    									evt.getPixelsID());
    	if (viewer != null && evt.getType() == SaveData.MEASUREMENT_TYPE) {
    		viewer.saveROIToServer();
    		viewer.discard();
    	}
    }
    
    /**
     * Reacts to the passed event.
     * 
     * @param evt The event to handle.
     */
    private void handleImageRenderedEvent(ImageRendered evt)
    {
    	MeasurementViewer viewer = MeasurementViewerFactory.getViewer(
    									evt.getPixelsID());
    	if (viewer != null) {
    		viewer.setIconImage(evt.getThumbnail());
    		viewer.setRndImage(evt.getRenderedImage());
    	}
    }
    
    /**
     * Removes all the references to the existing viewers.
     * 
     * @param evt The event to handle.
     */
    private void handleUserGroupSwitched(UserGroupSwitched evt)
    {
    	if (evt == null) return;
    	MeasurementViewerFactory.onGroupSwitched(evt.isSuccessful());
    }
    
    /**
     * Indicates that it was possible to reconnect.
     * 
     * @param evt The event to handle.
     */
    private void handleReconnectedEvent(ReconnectedEvent evt)
    {
    	Environment env = (Environment) registry.lookup(LookupNames.ENV);
    	if (!env.isServerAvailable()) return;
    	MeasurementViewerFactory.onGroupSwitched(true);
    }
    
    /**
     * Handles the {@link ActivityProcessEvent} event.
     * 
     * @param evt The event to handle.
     */
    private void handleActivityFinished(ActivityProcessEvent evt)
    {
    	if (evt == null) return;
    	ActivityComponent comp = evt.getActivity();
    	if (comp instanceof DeleteActivity)
    		MeasurementViewerFactory.onROIDeleted(
    			((DeleteActivity) comp).getImageID());
    }
    
    /**
     * Helper method. 
     * 
     * @return A reference to the {@link Registry}.
     */
    public static Registry getRegistry() { return registry; }
    
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
     * Implemented as specified by {@link Agent}.
     * @see Agent#activate(boolean)
     */
	public void activate(boolean master) {}

	/**
     * Implemented as specified by {@link Agent}.
     * @see Agent#canTerminate()
     */
	public boolean canTerminate() { return true; }

	/**
     * Implemented as specified by {@link Agent}.
     * @see Agent#setContext(Registry)
     */
	public void setContext(Registry ctx)
	{
		registry = ctx;
		EventBus bus = ctx.getEventBus();
		bus.register(this, MeasurementTool.class);
		bus.register(this, MeasurePlane.class);
		bus.register(this, ViewerState.class);
		bus.register(this, ChannelSelection.class);
		bus.register(this, SaveData.class);
		bus.register(this, FocusGainedEvent.class);
		bus.register(this, ImageRendered.class);
		bus.register(this, UserGroupSwitched.class);
		bus.register(this, ActivityProcessEvent.class);
		bus.register(this, ReconnectedEvent.class);
	}

    /**
     * Implemented as specified by {@link Agent}. 
     * @see Agent#getDataToSave()
     */
    public AgentSaveInfo getDataToSave()
    {
    	List<Object> instances = MeasurementViewerFactory.getInstancesToSave();
    	if (instances == null || instances.size() == 0) return null;
    	return new AgentSaveInfo("Measurements", instances);
	}
    
    /**
     * Implemented as specified by {@link Agent}. 
     * @see Agent#save(List)
     */
    public void save(List<Object> instances)
    {
    	MeasurementViewerFactory.saveInstances(instances);
    }
    
	/**
     * Implemented as specified by {@link Agent}.
     * @see Agent#terminate()
     */
	public void terminate() {}

	/**
	 * Listens to events.
	 * @see AgentEventListener#eventFired(AgentEvent)
	 */
	public void eventFired(AgentEvent e)
	{
		if (e instanceof MeasurementTool) 
			handleMeasurementToolEvent((MeasurementTool) e);
		else if (e instanceof MeasurePlane)
			handleMeasurePlaneEvent((MeasurePlane) e);
		else if (e instanceof ViewerState)
			handleViewerStateEvent((ViewerState) e);
		else if (e instanceof ChannelSelection)
			handleChannelSelectionEvent((ChannelSelection) e);
		else if (e instanceof SaveData)
			handleSaveData((SaveData) e);
		else if (e instanceof FocusGainedEvent)
			handleFocusGainedEvent((FocusGainedEvent) e);
		else if (e instanceof ImageRendered)
			handleImageRenderedEvent((ImageRendered) e);
		else if (e instanceof UserGroupSwitched)
			handleUserGroupSwitched((UserGroupSwitched) e);
		else if (e instanceof ActivityProcessEvent)
			handleActivityFinished((ActivityProcessEvent) e);
		else if (e instanceof ReconnectedEvent)
			handleReconnectedEvent((ReconnectedEvent) e);
	}
	
}
