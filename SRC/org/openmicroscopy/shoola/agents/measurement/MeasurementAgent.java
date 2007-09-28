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

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.iviewer.ChannelSelection;
import org.openmicroscopy.shoola.agents.events.iviewer.MeasurePlane;
import org.openmicroscopy.shoola.agents.events.iviewer.MeasurementTool;
import org.openmicroscopy.shoola.agents.events.iviewer.ViewerState;
import org.openmicroscopy.shoola.agents.measurement.view.MeasurementViewer;
import org.openmicroscopy.shoola.agents.measurement.view.MeasurementViewerFactory;
import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.env.event.EventBus;

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
    	MeasurementViewer viewer = MeasurementViewerFactory.getViewer(
				evt.getPixelsID());
    	if (viewer == null) {
    		viewer = MeasurementViewerFactory.getViewer(
        			evt.getPixelsID(), evt.getImageID(), evt.getName(),
        			evt.getRequesterBounds(), evt.getDefaultZ(), 
        			evt.getDefaultT(), evt.getMagnification(), 
        			evt.getActiveChannels());
    	}
    	if (viewer != null) {
    		MeasurementViewerFactory.addRequest(evt);
    		viewer.activate();
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
					viewer.close(false);
					break;
				case ViewerState.ICONIFIED:
					viewer.iconified(false);
					break;
				case ViewerState.DEICONIFIED:
					viewer.iconified(true);
					break;
			}
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
     * Helper method. 
     * 
     * @return A reference to the {@link Registry}.
     */
    public static Registry getRegistry() { return registry; }
    
    /**
     * Implemented as specified by {@link Agent}.
     * @see Agent#activate()
     */
	public void activate() {}

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
	}
	
}
