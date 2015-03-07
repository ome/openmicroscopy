/*
 * org.openmicroscopy.shoola.agents.treemng.TreeViewerAgent
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComponent;

//Third-party libraries










import org.openmicroscopy.shoola.agents.events.hiviewer.DownloadEvent;
//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.importer.BrowseContainer;
import org.openmicroscopy.shoola.agents.events.importer.ImportStatusEvent;
import org.openmicroscopy.shoola.agents.events.importer.LoadImporter;
import org.openmicroscopy.shoola.agents.events.iviewer.CopyRndSettings;
import org.openmicroscopy.shoola.agents.events.iviewer.RndSettingsCopied;
import org.openmicroscopy.shoola.agents.events.iviewer.ViewerCreated;
import org.openmicroscopy.shoola.agents.events.metadata.AnnotatedEvent;
import org.openmicroscopy.shoola.agents.events.treeviewer.BrowserSelectionEvent;
import org.openmicroscopy.shoola.agents.events.treeviewer.DataObjectSelectionEvent;
import org.openmicroscopy.shoola.agents.events.treeviewer.MoveToEvent;
import org.openmicroscopy.shoola.agents.events.treeviewer.NodeToRefreshEvent;
import org.openmicroscopy.shoola.agents.treeviewer.actions.SaveResultsAction;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.view.SearchEvent;
import org.openmicroscopy.shoola.agents.treeviewer.view.SearchSelectionEvent;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewerFactory;
import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.Environment;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.AdminService;
import org.openmicroscopy.shoola.env.data.events.ReconnectedEvent;
import org.openmicroscopy.shoola.env.data.events.SaveEventRequest;
import org.openmicroscopy.shoola.env.data.events.UserGroupSwitched;
import org.openmicroscopy.shoola.env.data.util.AgentSaveInfo;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.event.SaveEvent;
import org.openmicroscopy.shoola.env.ui.ActivityProcessEvent;
import org.openmicroscopy.shoola.env.ui.ViewObjectEvent;

import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ScreenData;

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
	public static Collection getAvailableUserGroups()
	{
		return (Collection) registry.lookup(LookupNames.USER_GROUP_DETAILS);
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
	 * Returns the context for an administrator.
	 * 
	 * @return See above.
	 */
	public static SecurityContext getAdminContext()
	{
		if (!isAdministrator()) return null;
		Collection<GroupData> groups = getAvailableUserGroups();
		Iterator<GroupData> i = groups.iterator();
		GroupData g;
		AdminService svc = registry.getAdminService();
		while (i.hasNext()) {
			g = i.next();
			if (svc.isSecuritySystemGroup(g.getId(), GroupData.SYSTEM))
			    return new SecurityContext(g.getId());
		}
		return null;
	}
	
	/**
	 * Returns <code>true</code> if the binary data are available, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public static boolean isBinaryAvailable()
	{
		Boolean b = (Boolean) registry.lookup(LookupNames.BINARY_AVAILABLE);
		if (b == null) return true;
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
		Collection groups = getAvailableUserGroups();
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
     * Returns <code>true</code> if all groups are displayed at the same time
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public static boolean isMultiGroups()
    {
    	Boolean b = (Boolean) registry.lookup("MutliGroup");
		if (b == null) return false;
		return b.booleanValue();
    }
    
	/**
	 * Returns the default hierarchy i.e. P/D, HCS etc.
	 * 
	 * @return See above.
	 */
    public static int getDefaultHierarchy()
    {
    	Environment env = (Environment) registry.lookup(LookupNames.ENV);
    	if (env == null) return Browser.PROJECTS_EXPLORER;
    	switch (env.getDefaultHierarchy()) {
			case LookupNames.PD_ENTRY:
				return Browser.PROJECTS_EXPLORER;
			case LookupNames.HCS_ENTRY:
				return Browser.SCREENS_EXPLORER;
			case LookupNames.TAG_ENTRY:
				return Browser.TAGS_EXPLORER;
			case LookupNames.ATTACHMENT_ENTRY:
				return Browser.FILES_EXPLORER;
		}
    	return Browser.PROJECTS_EXPLORER;
    }
    
    /**
     * Returns the identifier of the plugin to run.
     * 
     * @return See above.
     */
    public static int runAsPlugin()
    {
    	Environment env = (Environment) registry.lookup(LookupNames.ENV);
    	if (env == null) return -1;
    	return env.runAsPlugin();
    }
    
    /** 
     * Returns <code>true</code> if the application is used as a plugin,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public static boolean isRunAsPlugin() { return runAsPlugin() > 0; }
	
    /**
     * Handles the {@link CopyRndSettings} event.
     * 
     * @param evt The event to handle.
     */
    private void handleCopyRndSettings(CopyRndSettings evt)
    {
    	TreeViewerFactory.copyRndSettings(evt.getImage(), evt.getRndDef());
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
     * Handles the {@link ActivityProcessEvent} event.
     * 
     * @param evt The event to handle.
     */
    private void handleActivityProcessed(ActivityProcessEvent evt)
    {
    	ExperimenterData exp = (ExperimenterData) registry.lookup(
			        				LookupNames.CURRENT_USER_DETAILS);
    	if (exp == null) return;
        TreeViewer viewer = TreeViewerFactory.getTreeViewer(exp);
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
    }
    
    /**
     * Handles the {@link DataObjectSelectionEvent} event.
     * 
     * @param evt The event to handle.
     */
    private void handleDataObjectSelectionEvent(DataObjectSelectionEvent evt)
    {
    	ExperimenterData exp = (ExperimenterData) registry.lookup(
			        				LookupNames.CURRENT_USER_DETAILS);
    	if (exp == null) return;
        TreeViewer viewer = TreeViewerFactory.getTreeViewer(exp);
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
    	TreeViewerFactory.onGroupSwitched(evt.isSuccessful());
    }
    
    /**
     * Views the passed object if the object is an image.
     * 
     * @param evt The event to handle.
     */
    private void handleViewObjectEvent(ViewObjectEvent evt)
    {
    	if (evt == null) return;
    	Object o = evt.getObject();
    	if (!evt.browseObject()) return;
    	if (o instanceof DatasetData || o instanceof ProjectData || 
    		o instanceof PlateData || o instanceof ScreenData ||
    		o instanceof ImageData || o instanceof FileAnnotationData) {
    		DataObject data = (DataObject) o;
    		ExperimenterData exp = (ExperimenterData) registry.lookup(
    				LookupNames.CURRENT_USER_DETAILS);
    		if (exp == null) return;
			TreeViewer viewer = TreeViewerFactory.getTreeViewer(exp);
			if (viewer != null)  {
				viewer.browseContainer(data, null);
			}
			JComponent src = evt.getSource();
			if (src != null) src.setEnabled(true);
				//viewer.findDataObject(data.getClass(), data.getId(), false);
    	}
    }
    
    /**
     * Indicates if there are on-going imports.
     * 
     * @param evt The event to handle.
     */
    public void handleImportStatusEvent(ImportStatusEvent evt)
    {
    	if (evt == null) return;
    	ExperimenterData exp = (ExperimenterData) registry.lookup(
			        				LookupNames.CURRENT_USER_DETAILS);
    	if (exp == null) return;
        TreeViewer viewer = TreeViewerFactory.getTreeViewer(exp);
        if (viewer != null) 
        	viewer.setImporting(evt.isImporting(), evt.getContainers(), 
        			evt.isToRefresh(), evt.getImportResult());
    }
    
    /**
     * Browses the specified container.
     * 
     * @param evt The event to handle.
     */
    private void handleBrowseContainer(BrowseContainer evt)
    {
    	if (evt == null) return;
    	ExperimenterData exp = (ExperimenterData) registry.lookup(
			        				LookupNames.CURRENT_USER_DETAILS);
    	if (exp == null) return;
        TreeViewer viewer = TreeViewerFactory.getTreeViewer(exp);
        if (viewer != null) 
        	viewer.browseContainer(evt.getData(), evt.getNode());
    }
    
    /**
     * Marks the nodes to refresh.
     * 
     * @param evt The event to handle.
     */
    private void handleNodeToRefreshEvent(NodeToRefreshEvent evt)
    {
    	if (evt == null) return;
    	ExperimenterData exp = (ExperimenterData) registry.lookup(
			        				LookupNames.CURRENT_USER_DETAILS);
    	if (exp == null) return;
        TreeViewer viewer = TreeViewerFactory.getTreeViewer(exp);
        if (viewer != null) 
        	viewer.indicateToRefresh(evt.getObjects(), evt.getRefresh());
    }
    
    /**
     * Indicates that it was possible to reconnect.
     * 
     * @param evt The event to handle.
     */
    private void handleReconnectedEvent(ReconnectedEvent evt)
    {
    	//First check that the need to re-activate.
    	if (TreeViewerFactory.onReconnected()) return;
    }
    
    /**
     * Indicates to move the data.
     * 
     * @param evt The event to handle.
     */
    private void handleMoveToEvent(MoveToEvent evt)
    {
    	ExperimenterData exp = (ExperimenterData) registry.lookup(
    			LookupNames.CURRENT_USER_DETAILS);
    	if (exp == null) return;
    	TreeViewer viewer = TreeViewerFactory.getTreeViewer(exp);
    	if (viewer != null) viewer.moveTo(evt.getGroup(), evt.getObjects());
    }
    
    /**
     * Indicates that some objects have been annotated.
     * 
     * @param evt The event to handle.
     */
    private void handleAnnotatedEvent(AnnotatedEvent evt)
    {
    	TreeViewerFactory.onAnnotated(evt.getData(), evt.getCount());
    }
    
    /**
     * Passes the SearchEvent on to the Treeviewer 
     */
    private void handleSearchEvent(SearchEvent evt) {
        ExperimenterData exp = (ExperimenterData) registry.lookup(
                LookupNames.CURRENT_USER_DETAILS);
        if (exp == null) 
        	return;
        TreeViewer viewer = TreeViewerFactory.getTreeViewer(exp);
        viewer.handleSearchEvent(evt);
    }
    
    /**
     * Passes the SearchSelectionEvent on to the Treeviewer 
     */
    private void handleSearchSelectionEvent(SearchSelectionEvent evt) {
        ExperimenterData exp = (ExperimenterData) registry.lookup(
                LookupNames.CURRENT_USER_DETAILS);
        if (exp == null) 
        	return;
        TreeViewer viewer = TreeViewerFactory.getTreeViewer(exp);
        viewer.handleSearchSelectionEvent(evt);
    }

    /** Display the save dialog when used in plugin mode.*/
    private void handleSaveEvent(SaveEvent evt)
    {
        if (evt == null) return;
        ExperimenterData exp = (ExperimenterData) registry.lookup(
                LookupNames.CURRENT_USER_DETAILS);
        if (exp == null) 
            return;
        TreeViewer viewer = TreeViewerFactory.getTreeViewer(exp);
        SaveResultsAction a = new SaveResultsAction(viewer, LookupNames.IMAGE_J);
        a.actionPerformed(
                new ActionEvent(new JButton(), ActionEvent.ACTION_PERFORMED, ""));
    }

    /**
     * Downloads the files.
     * @param evt The event to handle.
     */
    private void handleDownloadEvent(DownloadEvent evt)
    {
        if (evt == null) return;
        ExperimenterData exp = (ExperimenterData) registry.lookup(
                LookupNames.CURRENT_USER_DETAILS);
        if (exp == null) 
            return;
        TreeViewer viewer = TreeViewerFactory.getTreeViewer(exp);
        viewer.download(evt.getFolder(), evt.isOverride());
    }

    /**
     * Implemented as specified by {@link Agent}.
     * @see Agent#activate(boolean)
     */
    public void activate(boolean master)
    {
    	if (!master) return;
    	Environment env = (Environment) registry.lookup(LookupNames.ENV);
    	if (env == null) return;
    	ExperimenterData exp = (ExperimenterData) registry.lookup(
			        				LookupNames.CURRENT_USER_DETAILS);
    	if (exp == null) return;
        TreeViewer viewer = TreeViewerFactory.getTreeViewer(exp);
        if (viewer != null) viewer.activate();
        if (runAsPlugin() == LookupNames.IMAGE_J_IMPORT) {
            EventBus bus = registry.getEventBus();
            GroupData gp = null;
            try {
                gp = exp.getDefaultGroup();
            } catch (Exception ex) {
                //No default group
            }
            long id = -1;
            if (gp != null) id = gp.getId();
            LoadImporter event = new LoadImporter(null,
                    BrowserSelectionEvent.PROJECT_TYPE);
            event.setGroup(id);
            bus.post(event);
        }
    }

    /**
     * Implemented as specified by {@link Agent}.
     * @see Agent#terminate()
     */
    public void terminate()
    {
    	Environment env = (Environment) registry.lookup(LookupNames.ENV);
    	if (env.isRunAsPlugin())
    		TreeViewerFactory.terminate();
    }

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
        bus.register(this, ActivityProcessEvent.class);
        bus.register(this, ViewerCreated.class);
        bus.register(this, UserGroupSwitched.class);
        bus.register(this, DataObjectSelectionEvent.class);
        bus.register(this, ImportStatusEvent.class);
        bus.register(this, BrowseContainer.class);
        bus.register(this, NodeToRefreshEvent.class);
        bus.register(this, ViewObjectEvent.class);
        bus.register(this, ReconnectedEvent.class);
        bus.register(this, MoveToEvent.class);
        bus.register(this, AnnotatedEvent.class);
        bus.register(this, SearchEvent.class);
        bus.register(this, SearchSelectionEvent.class);
        bus.register(this, SaveEvent.class);
        bus.register(this, DownloadEvent.class);
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
		else if (e instanceof ActivityProcessEvent)
			handleActivityProcessed((ActivityProcessEvent) e);
		else if (e instanceof ViewerCreated)
			handleViewerCreated((ViewerCreated) e);
		else if (e instanceof UserGroupSwitched)
			handleUserGroupSwitched((UserGroupSwitched) e);
		else if (e instanceof DataObjectSelectionEvent)
			handleDataObjectSelectionEvent((DataObjectSelectionEvent) e);
		else if (e instanceof ViewObjectEvent)
	        handleViewObjectEvent((ViewObjectEvent) e);
		else if (e instanceof ImportStatusEvent)
	        handleImportStatusEvent((ImportStatusEvent) e);
		else if (e instanceof BrowseContainer)
			handleBrowseContainer((BrowseContainer) e);
		else if (e instanceof NodeToRefreshEvent)
			handleNodeToRefreshEvent((NodeToRefreshEvent) e);
		else if (e instanceof ReconnectedEvent)
			handleReconnectedEvent((ReconnectedEvent) e);
		else if (e instanceof MoveToEvent)
			handleMoveToEvent((MoveToEvent) e);
		else if (e instanceof AnnotatedEvent)
			handleAnnotatedEvent((AnnotatedEvent) e);
		else if (e instanceof SearchEvent)
		    handleSearchEvent((SearchEvent) e);
		else if (e instanceof SearchSelectionEvent) 
            handleSearchSelectionEvent((SearchSelectionEvent) e);
		else if (e instanceof SaveEvent)
            handleSaveEvent((SaveEvent) e);
		else if (e instanceof DownloadEvent)
            handleDownloadEvent((DownloadEvent) e);
	}

}
