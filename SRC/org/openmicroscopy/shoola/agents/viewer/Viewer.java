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
import javax.swing.JInternalFrame;
//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.ui.TopFrame;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class Viewer
	extends JInternalFrame
	implements Agent, AgentEventListener, EventBus
{
	/** Reference to the {@link Registry}. */
	private Registry		registry;
	
	private ViewerUIF		presentation;
	private ViewerCtrl		control;
	private TopFrame		topFrame;
	
	/** Implemented as specified by {@link Agent}. */
	public void activate()
	{       
		topFrame.addToDesktop(presentation, TopFrame.PALETTE_LAYER);
		presentation.setVisible(true);
	}
	
	/** Implemented as specified by {@link Agent}. */
	public void terminate()
	{
	}

	/** Implemented as specified by {@link Agent}. */
	public void setContext(Registry ctx) 
	{
		registry = ctx;
		//TO be removed
		control = new ViewerCtrl(this);
		presentation = new ViewerUIF(control, registry);
		topFrame = registry.getTopFrame();
		topFrame.addToMenu(TopFrame.VIEW, presentation.getViewMenuItem());

		//register(this, ImageLoaded.class);
		//register(this, imageRendered.class);
	}

	/** Implemented as specified by {@link Agent}. */
	public boolean canTerminate() 
	{
		return true;
	}

	ViewerUIF getPresentation()
	{
		return presentation;
	}
	
	Registry getRegistry()
	{
		return registry;
	}
	
	/** Implement as specified by {@link AgentEventListener}. */
	public void eventFired(AgentEvent e) 
	{
		//if (e instanceof ImageLoaded)
		//else if (e instanceof ImageRendered)
	}


	/** Implement as specified by {@link EventBus}. */ 
	public void register(AgentEventListener subscriber, Class event) 
	{
		registry.getEventBus().register(subscriber, event);
	}

	/** Implement as specified by {@link EventBus}. */ 
	public void register(AgentEventListener subscriber, Class[] events)
	{
		for (int i = 0; i < events.length; i++) 
			registry.getEventBus().register(subscriber, events[i]);
	}

	/**
	* Required by I/F but not actually needed in our case, no op implementation.
	*/
	public void remove(AgentEventListener subscriber) {}


	/**
	* Required by I/F but not actually needed in our case, no op implementation.
	*/
	public void remove(AgentEventListener subscriber, Class event) {}

	/**
	* Required by I/F but not actually needed in our case, no op implementation.
	*/
	public void remove(AgentEventListener subscriber, Class[] events) {}

	/**
	* Required by I/F but not actually needed in our case, no op implementation.
	*/
	public void post(AgentEvent e) {}

}
