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

//Third-party libraries

//Application-internal dependencies
import javax.swing.JMenuItem;

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
	implements Agent, AgentEventListener, EventBus
{
		
	/** Reference to the {@link Registry}. */
	private Registry			registry;
	
	private ViewerUIF			presentation;
	private ViewerCtrl			control;
	private TopFrame			topFrame;
	private RenderingControl	renderingControl;
	
	private int					curImageID, curPixelsID;
	
	private JMenuItem 			viewItem;
	
	/** Implemented as specified by {@link Agent}. */
	public void activate()
	{   //TODO: add control. 
		if (presentation != null) {  
			topFrame.addToDesktop(presentation, TopFrame.PALETTE_LAYER);
			presentation.setVisible(true);
		}
	}
	
	/** Implemented as specified by {@link Agent}. */
	public void terminate()
	{
	}

	/** Implemented as specified by {@link Agent}. */
	public void setContext(Registry ctx) 
	{
		registry = ctx;
		register(this, ImageLoaded.class);
		register(this, ImageRendered.class);
		
		topFrame = registry.getTopFrame();
		viewItem = getViewMenuItem();
		topFrame.addToMenu(TopFrame.VIEW, viewItem);
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
	
	PixelsDimensions getPixelsDims()
	{
		return renderingControl.getPixelsDims();
	}
	
	int getDefaultT()
	{
		return renderingControl.getDefaultT();
	}
	
	int getDefaultZ()
	{
		return renderingControl.getDefaultZ();
	}
	
	void onPlaneSelected(int z, int t)
	{
		PlaneDef def = new PlaneDef(PlaneDef.XY, t);
		def.setZ(z);
		RenderImage event = new RenderImage(curPixelsID, def);
		registry.getEventBus().post(event);	
	}
	
	/** Implement as specified by {@link AgentEventListener}. */
	public void eventFired(AgentEvent e) 
	{
		if (e instanceof ImageLoaded)
			handleImageLoaded((ImageLoaded) e);
		else if (e instanceof ImageRendered)
			handleImageRendered((ImageRendered) e);
	}
	
	private void handleImageLoaded(ImageLoaded response)
	{
		LoadImage request = (LoadImage) response.getACT();
		renderingControl = response.getProxy();
		buildPresentation();
		curImageID = request.getImageID();
		curPixelsID = request.getPixelsID();
		RenderImage event = new RenderImage(curPixelsID);
		registry.getEventBus().post(event);
	}
	
	private void handleImageRendered(ImageRendered response)
	{
		presentation.setImage(response.getRenderedImage());
	}
	
	private void buildPresentation()
	{
		control = new ViewerCtrl(this);
		presentation = new ViewerUIF(control, registry);
		control.setMenuItemListener(viewItem, ViewerCtrl.V_VISIBLE);
		viewItem.setEnabled(true);
		topFrame.addToDesktop(presentation, TopFrame.PALETTE_LAYER);
		presentation.setVisible(true);
		
	}
	
	/** 
	 * Menu item to add to the 
	 * {@link org.openmicroscopy.shoola.env.ui.TopFrame} menu bar.
	 */
	JMenuItem getViewMenuItem()
	{
		JMenuItem menuItem = new JMenuItem("Viewer");
		menuItem.setEnabled(false);
		return menuItem;
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
	 * Required by I/F but not actually needed in our case, no op 
	 * implementation.
	 */
	public void remove(AgentEventListener subscriber) {}


	/**
	 * Required by I/F but not actually needed in our case, no op 
	 * implementation.
	 */
	public void remove(AgentEventListener subscriber, Class event) {}

	/**
	 * Required by I/F but not actually needed in our case, no op 
	 * implementation.
	 */
	public void remove(AgentEventListener subscriber, Class[] events) {}

	/**
	 * Required by I/F but not actually needed in our case, no op 
	 * implementation.
	 */
	public void post(AgentEvent e) {}

}
