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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.iviewer.CopyRndSettings;
import org.openmicroscopy.shoola.agents.events.iviewer.ImageProjected;
import org.openmicroscopy.shoola.agents.events.iviewer.RndSettingsCopied;
import org.openmicroscopy.shoola.agents.events.iviewer.ViewerCreated;
import org.openmicroscopy.shoola.agents.events.treeviewer.DataObjectSelectionEvent;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewerFactory;
import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.Environment;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.events.SaveEventRequest;
import org.openmicroscopy.shoola.env.data.events.UserGroupSwitched;
import org.openmicroscopy.shoola.env.data.util.AgentSaveInfo;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.ui.ActivityProcessEvent;
import org.openmicroscopy.shoola.env.ui.ViewObjectEvent;

import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.GroupData;
import pojos.ProjectData;

/** 
 * The TreeViewer agent. This agent manages and presents the
 * Project/Dataset/Image, Screen/Plate, Image hierarchy etc.
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
	 * Returns the available user groups.
	 * 
	 * @return See above.
	 */
	public static Set getAvailableUserGroups()
	{
		return (Set) registry.lookup(LookupNames.USER_GROUP_DETAILS);
	}
	
	/**
	 * Returns <code>true</code> if the currently logged in user
	 * is an administrator, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public static boolean isAdministrator()
	{
		Boolean b = (Boolean) registry.lookup(LookupNames.USER_ADMINISTRATOR);
		if (b == null) return false;
		return b.booleanValue();
	}
	
	/**
	 * Returns the collection of groups the current user is the leader of.
	 * 
	 * @return See above.
	 */
	public static Set getGroupsLeaderOf()
	{
		Set values = new HashSet();
		Set groups = getAvailableUserGroups();
		Iterator i = groups.iterator();
		GroupData g;
		Set leaders;
		ExperimenterData exp = getUserDetails();
		long id = exp.getId();
		Iterator j;
		while (i.hasNext()) {
			g = (GroupData) i.next();
			leaders = g.getLeaders();
			if (leaders != null && leaders.size() > 0) {
				j = leaders.iterator();
				while (j.hasNext()) {
					exp = (ExperimenterData) j.next();
					if (exp.getId() == id)
						values.add(g);
				}
			}
		}
		return values;
	}
	
	/**
	 * Returns <code>true</code> if the user currently logged in 
	 * is an owner of the current group, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public static boolean isLeaderOfCurrentGroup()
	{
		ExperimenterData exp = getUserDetails();
		Set groups = getGroupsLeaderOf();
		if (groups.size() == 0) return false;
		GroupData group = exp.getDefaultGroup();
		Iterator i = groups.iterator();
		GroupData g;
		while (i.hasNext()) {
			g = (GroupData) i.next();
			if (g.getId() == group.getId())
				return true;
		}
		return false;
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
    	TreeViewerFactory.onRndSettingsCopied(evt.getImagesIDs());
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
     * Handles the {@link ActivityProcessEvent} event.
     * 
     * @param evt The event to handle.
     */
    private void handleActivityFinished(ActivityProcessEvent evt)
    {
    	Environment env = (Environment) registry.lookup(LookupNames.ENV);
    	if (!env.isServerAvailable()) return;
    	ExperimenterData exp = (ExperimenterData) registry.lookup(
			        				LookupNames.CURRENT_USER_DETAILS);
    	GroupData gp = exp.getDefaultGroup();
    	long id = -1;
    	if (gp != null) id = gp.getId();
        TreeViewer viewer = TreeViewerFactory.getTreeViewer(exp, id);
        if (viewer != null) {
        	viewer.onActivityProcessed(evt.getActivity(), evt.isFinished());
        }
    }
    
    /**
     * Handles the {@link ViewerCreated} event.
     * 
     * @param evt The event to handle.
     */
    private void handleViewerCreated(ViewerCreated evt)
    {
    	Environment env = (Environment) registry.lookup(LookupNames.ENV);
    	if (!env.isServerAvailable()) return;
    	ExperimenterData exp = (ExperimenterData) registry.lookup(
			        				LookupNames.CURRENT_USER_DETAILS);
    	GroupData gp = exp.getDefaultGroup();
    	long id = -1;
    	if (gp != null) id = gp.getId();
        TreeViewer viewer = TreeViewerFactory.getTreeViewer(exp, id);
        //if (viewer != null)
        	//viewer.displayViewer(evt.getViewer(), evt.getControls(), 
        	//		evt.isToAdd(), evt.isToDetach());
    }
    
    /**
     * Handles the {@link DataObjectSelectionEvent} event.
     * 
     * @param evt The event to handle.
     */
    private void handleDataObjectSelectionEvent(DataObjectSelectionEvent evt)
    {
    	Environment env = (Environment) registry.lookup(LookupNames.ENV);
    	if (!env.isServerAvailable()) return;
    	ExperimenterData exp = (ExperimenterData) registry.lookup(
			        				LookupNames.CURRENT_USER_DETAILS);
    	GroupData gp = exp.getDefaultGroup();
    	long id = -1;
    	if (gp != null) id = gp.getId();
        TreeViewer viewer = TreeViewerFactory.getTreeViewer(exp, id);
        if (viewer != null)
        	viewer.findDataObject(evt.getDataType(), evt.getID(), 
        			evt.isSelectTab());
    }
    
    /**
     * Handles the {@link UserGroupSwitched} event.
     * 
     * @param evt The event to handle.
     */
    private void handleUserGroupSwitched(UserGroupSwitched evt)
    {
    	if (evt == null) return;
    	Environment env = (Environment) registry.lookup(LookupNames.ENV);
    	if (!env.isServerAvailable()) return;
    	TreeViewerFactory.onGroupSwitched(evt.isSuccessful());
    }
    
    /**
     * Views the passed object if the object is an image.
     * 
     * @param evt The event to handle.
     */
    private void handleViewObjectEvent(ViewObjectEvent evt)
    {
    	Environment env = (Environment) registry.lookup(LookupNames.ENV);
    	if (!env.isServerAvailable()) return;
    	
    	if (evt == null) return;
    	Object o = evt.getObject();
    	if (o instanceof DatasetData || o instanceof ProjectData) {
    		DataObject data = (DataObject) o;
    		ExperimenterData exp = (ExperimenterData) registry.lookup(
    				LookupNames.CURRENT_USER_DETAILS);
    		GroupData gp = exp.getDefaultGroup();
    		long id = -1;
			if (gp != null) id = gp.getId();
			TreeViewer viewer = TreeViewerFactory.getTreeViewer(exp, id);
			if (viewer != null)
				viewer.findDataObject(data.getClass(), data.getId(), false);
    	}
    }
    
    /**
     * Implemented as specified by {@link Agent}.
     * @see Agent#activate()
     */
    public void activate()
    {
    	Environment env = (Environment) registry.lookup(LookupNames.ENV);
    	if (env == null) return;
    	if (!env.isServerAvailable()) return;
    	ExperimenterData exp = (ExperimenterData) registry.lookup(
			        				LookupNames.CURRENT_USER_DETAILS);
    	//if (exp == null) return;
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
        bus.register(this, ActivityProcessEvent.class);
        bus.register(this, ViewerCreated.class);
        bus.register(this, UserGroupSwitched.class);
        bus.register(this, DataObjectSelectionEvent.class);
    }

    /**
     * Implemented as specified by {@link Agent}. 
     * @see Agent#canTerminate()
     */
    public boolean canTerminate()
    { 
    	TreeViewerFactory.writeExternalApplications();
    	return true;
    }

    /**
     * Implemented as specified by {@link Agent}. 
     * @see Agent#getDataToSave()
     */
    public AgentSaveInfo getDataToSave() { return null; }
    
    /**
     * Implemented as specified by {@link Agent}. 
     * @see Agent#save(List)
     */
    public void save(List<Object> instances) {}
    
    /**
     * Responds to events fired trigger on the bus.
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
		else if (e instanceof ActivityProcessEvent)
			handleActivityFinished((ActivityProcessEvent) e);
		else if (e instanceof ViewerCreated)
			handleViewerCreated((ViewerCreated) e);
		else if (e instanceof UserGroupSwitched)
			handleUserGroupSwitched((UserGroupSwitched) e);
		else if (e instanceof DataObjectSelectionEvent)
			handleDataObjectSelectionEvent((DataObjectSelectionEvent) e);
		else if (e instanceof ViewObjectEvent)
	        handleViewObjectEvent((ViewObjectEvent) e);
	}

}
