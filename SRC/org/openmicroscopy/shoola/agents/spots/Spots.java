/*
 * org.openmicroscopy.shoola.agents.spots.Spots
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

package org.openmicroscopy.shoola.agents.spots;

//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.ViewTrackSpotsEvent;
import org.openmicroscopy.shoola.agents.spots.data.SpotsTrajectorySet;
import org.openmicroscopy.shoola.agents.spots.ui.SpotsWindow;
import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.ChainExecutionData;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.env.event.EventBus;


/** 
 * The trajectory viewer agent
 *
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */

public class Spots  implements Agent, AgentEventListener
{
	
	/** 
	 * Holds the agent's configuration and lets the agent access all services.
	 */
	private Registry	registry;
	
	
	/**
	 * Does nothing.
	 * However, all agents must have a no-params public constructor.
	 */
	public Spots() {}
	
	public void setContext(Registry ctx)
	{			
		System.err.println("setting context for spots..");
		registry = ctx;  //The container built our registry, store a reference.
		EventBus bus  = registry.getEventBus();
	    bus.register(this,ViewTrackSpotsEvent.class);
	}
	
	public void activate() 
	{
	}

	public boolean canTerminate() 
	{
		return true;
	}
	
	public void terminate() 
	{
	}
	
	/** Implement as specified by {@link AgentEventListener}. */
	public void eventFired(AgentEvent e) {
		if (e instanceof ViewTrackSpotsEvent) 
			handleViewSpots((ViewTrackSpotsEvent) e);
	}
		
	private void handleViewSpots(ViewTrackSpotsEvent event) {
		ChainExecutionData exec = event.getChainExecution();
		System.err.println("viewing spots for execution..."+exec.getID());
		SpotsTrajectorySet trajectories = new SpotsTrajectorySet(registry,exec);
		SpotsWindow window = new SpotsWindow(registry,trajectories);
	}

}
