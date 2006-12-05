/*
 * org.openmicroscopy.shoola.agents.annotator.AnnotatorAgent 
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
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.annotator;


//Java imports

//Third-party libraries

//Application-internal dependencies
import java.util.Set;

import org.openmicroscopy.shoola.agents.annotator.view.Annotator;
import org.openmicroscopy.shoola.agents.annotator.view.AnnotatorFactory;
import org.openmicroscopy.shoola.agents.events.annotator.AnnotateDataObjects;
import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.env.event.EventBus;

/** 
 * 
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
public class AnnotatorAgent 
	implements Agent, AgentEventListener
{

    /** Reference to the registry. */
    private static Registry         registry;
    
    /**
     * Handles the {@link AnnotateDataObjects} event.
     * 
     * @param e The event to handle.
     */
    private void handleAnnotate(AnnotateDataObjects e)
    {
    	Set nodes = e.getObjectsToAnnotate();
    	if (nodes == null || nodes.size() == 0) return;
    	Annotator annotator = AnnotatorFactory.getAnnotator(nodes);
    	if (annotator != null) annotator.activate();
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
        EventBus bus = registry.getEventBus();
        bus.register(this, AnnotateDataObjects.class);
    }
	
	/**
	 * Implemented as specified by {@link Agent}.
     * @see Agent#terminate()
	 */
    public void terminate() {}

    /**
     * Responds to an event fired trigger on the bus.
     * @see AgentEventListener#eventFired(AgentEvent)
     */
	public void eventFired(AgentEvent e)
	{
		if (e instanceof AnnotateDataObjects) handleAnnotate((AnnotateDataObjects) e);
	}

}
