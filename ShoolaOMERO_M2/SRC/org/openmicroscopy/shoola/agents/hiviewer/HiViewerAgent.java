/*
 * org.openmicroscopy.shoola.agents.hiviewer.HiViewer
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

package org.openmicroscopy.shoola.agents.hiviewer;


//Java imports

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
        if (evt.getEventIndex() != Browse.IMAGES)
            browse(evt.getEventIndex(), evt.getHierarchyObjectID(),
                evt.getRootLevel(), evt.getRootID());
        else  {
            HiViewer viewer = null;
            viewer = HiViewerFactory.getImagesViewer(evt.getObjectIDs(), 
                evt.getRootLevel(), evt.getRootID());
            if (viewer != null) viewer.activate();
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
     * @param id    		The id of the dataObject to browse.
     * @param rootLevel     The level of the root either 
     *                      <code>GroupData</code> or 
     *                      <code>ExperimenterData</code>.
     * @param rootID        The Id of the root.
     */
    public static void browse(int eventIndex, long id, Class rootLevel,
                                long rootID)
    {
        HiViewer viewer = null;
        switch (eventIndex) {
            case Browse.PROJECT:
                viewer = HiViewerFactory.getProjectViewer(id, rootLevel, 
                                                            rootID);
                break;
            case Browse.DATASET:
                viewer = HiViewerFactory.getDatasetViewer(id, rootLevel, 
                                                        rootID);
                break;
            case Browse.CATEGORY_GROUP:
                viewer = HiViewerFactory.getCategoryGroupViewer(id, rootLevel, 
                                                                rootID);
                break;
            case Browse.CATEGORY:
                viewer = HiViewerFactory.getCategoryViewer(id, rootLevel, 
                                                            rootID);  
        }
        if (viewer != null) viewer.activate();
    }

}
