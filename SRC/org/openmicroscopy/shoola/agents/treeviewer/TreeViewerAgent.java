/*
 * org.openmicroscopy.shoola.agents.treemng.TreeViewerAgent
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

package org.openmicroscopy.shoola.agents.treeviewer;




//Java imports
import java.util.Collection;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.iviewer.CopyRndSettings;
import org.openmicroscopy.shoola.agents.events.iviewer.ImageProjected;
import org.openmicroscopy.shoola.agents.events.iviewer.RndSettingsCopied;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewerFactory;
import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.Environment;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.events.SaveEventRequest;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.env.event.EventBus;
import pojos.ExperimenterData;
import pojos.GroupData;

/** 
 * The TreeViewer agent. This agent manages and presents the
 * Project/Dataset/Image, CategoryGroup/Category/Image and Image hierarchy.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class TreeViewerAgent
    implements Agent, AgentEventListener
{

    /** Determine if the multiple users flag is on or off. */
    public static final String MULTI_USER = "MultiUser";
    
    /** Determine how to lay out the browsers. */
    public static final String LAYOUT_TYPE = "BrowserLayout";
    
	/** Reference to the registry. */
    private static Registry         registry;
    
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
     * Handles the {@link CopyRndSettings} event.
     * 
     * @param evt The event to handle.
     */
    private void handleCopyRndSettings(CopyRndSettings evt)
    {
    	TreeViewerFactory.copyRndSettings(evt.getImage());
    }
    
    /**
     * Handles the {@link SaveEventRequest} event.
     * 
     * @param evt The event to handle.
     */
    private void handleSaveEventRequest(SaveEventRequest evt)
    {
    	Object origin = evt.getOrigin();
    	if (!(origin instanceof TreeViewer)) return;
    	TreeViewerFactory.saveOnClose(evt, this);
    }
    
	/**
     * Handles the {@link RndSettingsCopied} event.
     * 
     * @param evt The event to handle.
     */
    private void handleRndSettingsCopied(RndSettingsCopied evt)
    {
    	Collection ids = evt.getImagesIDs();
    	TreeViewerFactory.onRndSettingsCopied(ids);
    }
    
	/**
     * Handles the {@link ImageProjected} event.
     * 
     * @param evt The event to handle.
     */
    private void handleImageProjected(ImageProjected evt)
    {
    	Environment env = (Environment) registry.lookup(LookupNames.ENV);
    	if (!env.isServerAvailable()) return;
    	ExperimenterData exp = (ExperimenterData) registry.lookup(
			        				LookupNames.CURRENT_USER_DETAILS);
    	GroupData gp = exp.getDefaultGroup();
    	long id = -1;
    	if (gp != null) id = gp.getId();
        TreeViewer viewer = TreeViewerFactory.getTreeViewer(exp, id);
        if (viewer != null) viewer.refreshTree();
    }
    
    /**
     * Implemented as specified by {@link Agent}.
     * @see Agent#activate()
     */
    public void activate()
    {
    	Environment env = (Environment) registry.lookup(LookupNames.ENV);
    	if (!env.isServerAvailable()) return;
    	ExperimenterData exp = (ExperimenterData) registry.lookup(
			        				LookupNames.CURRENT_USER_DETAILS);
    	GroupData gp = exp.getDefaultGroup();
    	long id = -1;
    	if (gp != null) id = gp.getId();
        TreeViewer viewer = TreeViewerFactory.getTreeViewer(exp, id);
        if (viewer != null) viewer.activate();
    }

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
        bus.register(this, CopyRndSettings.class);
        bus.register(this, SaveEventRequest.class);
        bus.register(this, RndSettingsCopied.class);
        bus.register(this, ImageProjected.class);
    }

    /**
     * Implemented as specified by {@link Agent}. 
     * @see Agent#canTerminate()
     */
    public boolean canTerminate() { return true; }

    /**
     * Implemented as specified by {@link Agent}. 
     * @see Agent#hasDataToSave()
     */
    public Map<String, Set> hasDataToSave()
    {
    	return TreeViewerFactory.hasDataToSave();
	}
    
    /**
     * Responds to an event fired trigger on the bus.
     * Listens to ViewImage event.
     * @see AgentEventListener#eventFired(AgentEvent)
     */
	public void eventFired(AgentEvent e)
	{
		if (e instanceof CopyRndSettings)
			handleCopyRndSettings((CopyRndSettings) e);
		else if (e instanceof SaveEventRequest) 
			handleSaveEventRequest((SaveEventRequest) e);
		else if (e instanceof RndSettingsCopied)
    		handleRndSettingsCopied((RndSettingsCopied) e);
		else if (e instanceof ImageProjected)
    		handleImageProjected((ImageProjected) e);
	}

}
