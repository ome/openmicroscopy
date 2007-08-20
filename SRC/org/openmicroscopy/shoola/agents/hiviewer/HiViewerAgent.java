/*
 * org.openmicroscopy.shoola.agents.hiviewer.HiViewer
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

package org.openmicroscopy.shoola.agents.hiviewer;


//Java imports
import java.awt.Rectangle;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.hiviewer.Browse;
import org.openmicroscopy.shoola.agents.hiviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewerFactory;
import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.env.event.EventBus;

import pojos.ExperimenterData;

/** 
 * The HiViewer agent. This agent manages and presents a <code>DataObject</code>
 * and its children.
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
public class HiViewerAgent
    implements Agent, AgentEventListener
{

    /** Reference to the registry. */
    private static Registry         registry; 
    
    /**
     * Helper method. 
     * 
     * @return A reference to the {@link Registry}.
     */
    public static Registry getRegistry() { return registry; }
    
    /**
     * Handles the {@link Browse event}.
     * 
     * @param evt The event to handle.
     */
    private void handleBrowse(Browse evt)
    {
        if (evt == null) return;
        HiViewer viewer = null;
        switch (evt.getEventIndex()) {
			case Browse.IMAGES:
	            viewer = HiViewerFactory.getImagesViewer(evt.getObjectIDs(), 
	                evt.getExperimenter());
	            if (viewer != null) viewer.activate(evt.getRequesterBounds());
				break;
			case Browse.DATASETS:
				viewer = HiViewerFactory.getDatasetsViewer(evt.getObjectIDs(), 
						evt.getExperimenter());
		        if (viewer != null) viewer.activate(evt.getRequesterBounds());
				break;
			case Browse.CATEGORIES:
				viewer = HiViewerFactory.getCategoriesViewer(evt.getObjectIDs(), 
						evt.getExperimenter());
		        if (viewer != null) viewer.activate(evt.getRequesterBounds());
				break;
			case Browse.PROJECTS:
				viewer = HiViewerFactory.getProjectsViewer(evt.getObjectIDs(), 
						evt.getExperimenter());
		        if (viewer != null) viewer.activate(evt.getRequesterBounds());
				break;
			case Browse.CATEGORY_GROUPS:
				viewer = HiViewerFactory.getCategoryGroupsViewer(
									evt.getObjectIDs(),evt.getExperimenter());
		        if (viewer != null) viewer.activate(evt.getRequesterBounds());
				break;
			default:
				browse(evt.getEventIndex(), evt.getHierarchyObjectID(),
						evt.getExperimenter(), evt.getRequesterBounds());
				break;
		}
    }
    
    /** Creates a new instance. */
    public HiViewerAgent() {}
    
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
        bus.register(this, Browse.class);
    }

    /**
     * Implemented as specified by {@link Agent}. 
     * @see Agent#canTerminate()
     */
    public boolean canTerminate() { return true; }

    /**
     * Responds to an event fired trigger on the bus.
     * Listens to BrowseProject, BrowseDataset, BrowseCategoryGroup, 
     * BrowseCategory.
     * @see AgentEventListener#eventFired(AgentEvent)
     */
    public void eventFired(AgentEvent e)
    {
        if (e instanceof Browse)
            handleBrowse((Browse) e);
    }
    
    /** 
     * Browse the specified element.
     * 
     * @param eventIndex 	One of the constant defined by the {@link Browser}
     * 						class event.
     * @param id    		The ID of the dataObject to browse.
     * @param exp			The selected experimenter.
     * @param bounds        The bounds of the component invoking this method.
     */
    public static void browse(int eventIndex, long id, ExperimenterData exp,
                              Rectangle bounds)
    {
        HiViewer viewer = null;
        switch (eventIndex) {
            case Browse.PROJECT:
                viewer = HiViewerFactory.getProjectViewer(id, exp);
                break;
            case Browse.DATASET:
                viewer = HiViewerFactory.getDatasetViewer(id, exp);
                break;
            case Browse.CATEGORY_GROUP:
                viewer = HiViewerFactory.getCategoryGroupViewer(id, exp);
                break;
            case Browse.CATEGORY:
                viewer = HiViewerFactory.getCategoryViewer(id, exp);  
        }
        if (viewer != null) viewer.activate(bounds);
    }

}
