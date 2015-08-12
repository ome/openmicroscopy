/*
 * org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserAgent 
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
package org.openmicroscopy.shoola.agents.dataBrowser;


//Java imports
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowserFactory;
import org.openmicroscopy.shoola.agents.events.iviewer.CopyRndSettings;
import org.openmicroscopy.shoola.agents.events.iviewer.RndSettingsCopied;
import org.openmicroscopy.shoola.agents.events.metadata.AnnotatedEvent;
import org.openmicroscopy.shoola.agents.events.treeviewer.CopyItems;
import org.openmicroscopy.shoola.agents.events.treeviewer.DisplayModeEvent;
import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.Environment;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.AdminService;
import org.openmicroscopy.shoola.env.data.events.ReconnectedEvent;
import org.openmicroscopy.shoola.env.data.events.UserGroupSwitched;
import org.openmicroscopy.shoola.env.data.util.AgentSaveInfo;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.env.event.EventBus;
import pojos.ExperimenterData;
import pojos.GroupData;

/** 
 * The DataBrowser agent. This agent manages and presents a thumbnail view
 * of a <code>DataObject</code> and its children.
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
public class DataBrowserAgent 
	implements Agent, AgentEventListener
{

	/** Reference to the registry. */
    private static Registry         registry; 
    
    /** The display mode.*/
	private int displayMode = -1;
	
    /**
     * Helper method. 
     * 
     * @return A reference to the <code>Registry</code>.
     */
    public static Registry getRegistry() { return registry; }

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
		Collection groups = (Collection)
		registry.lookup(LookupNames.USER_GROUP_DETAILS);
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
	 * Returns the experimenter corresponding to the passed id.
	 * 
	 * @param expID The experimenter's id.
	 * @return See above.
	 */
	public static ExperimenterData getExperimenter(long expID)
	{
		List l = (List) registry.lookup(LookupNames.USERS_DETAILS);
		if (l == null) return null;
		Iterator i = l.iterator();
		ExperimenterData exp;
		while (i.hasNext()) {
			exp = (ExperimenterData) i.next();
			if (exp.getId() == expID) return exp;
		}
		return null;
	}
	
	/**
     * Handles the {@link RndSettingsCopied} event.
     * 
     * @param evt The event to handle.
     */
    private void handleRndSettingsCopied(RndSettingsCopied evt)
    {
    	Collection ids = evt.getImagesIDs();
    	DataBrowserFactory.refreshThumbnails(ids);
    }
    
    /**
     * Handles the {@link CopyRndSettings} event.
     * 
     * @param evt The event to handle.
     */
    private void handleCopyRndSettings(CopyRndSettings evt)
    {
    	DataBrowserFactory.setRndSettingsToCopy(evt.getImage(), evt.getRndDef());
    }
    
    /**
     * Handles the {@link CopyItems} event.
     * 
     * @param evt The event to handle.
     */
    private void handleCopyItems(CopyItems evt)
    {
    	DataBrowserFactory.setDataToCopy(evt.getType());
    }
    
    /**
     * Handles the {@link UserGroupSwitched} event.
     * 
     * @param evt The event to handle.
     */
    private void handleUserGroupSwitched(UserGroupSwitched evt)
    {
    	if (evt == null) return;
    	DataBrowserFactory.onGroupSwitched(evt.isSuccessful());
    }
    
    /**
     * Handles the {@link ReconnectedEvent} event.
     * 
     * @param evt The event to handle.
     */
    private void handleReconnectedEvent(ReconnectedEvent evt)
    {
    	if (evt == null) return;
    	DataBrowserFactory.onGroupSwitched(true);
    }
    
    /**
     * Indicates that some objects have been annotated.
     * 
     * @param evt The event to handle.
     */
    private void handleAnnotatedEvent(AnnotatedEvent evt)
    {
    	DataBrowserFactory.onAnnotated(evt.getData(), evt.getCount());
    }
    
    /**
     * Updates the view when the mode is changed.
     * 
     * @param evt The event to handle.
     */
    private void handleDisplayModeEvent(DisplayModeEvent evt)
    {
    	displayMode = evt.getDisplayMode();
    	DataBrowserFactory.setDisplayMode(displayMode);
    }
    
	/** Creates a new instance. */
	public DataBrowserAgent() {}
	
	/**
     * Implemented as specified by {@link Agent}.
     * @see Agent#activate(boolean)
     */
    public void activate(boolean master) {}
    
    /**
     * Implemented as specified by {@link Agent}. 
     * @see Agent#terminate()
     */
    public void terminate()
    {
    	Environment env = (Environment) registry.lookup(LookupNames.ENV);
    	if (env.isRunAsPlugin())
    		DataBrowserFactory.onGroupSwitched(true);
    }

    /** 
     * Implemented as specified by {@link Agent}. 
     * @see Agent#setContext(Registry)
     */
    public void setContext(Registry ctx)
    {
        registry = ctx;
        EventBus bus = registry.getEventBus();
        bus.register(this, RndSettingsCopied.class);
        bus.register(this, CopyRndSettings.class);
        bus.register(this, CopyItems.class);
        bus.register(this, UserGroupSwitched.class);
        bus.register(this, ReconnectedEvent.class);
        bus.register(this, AnnotatedEvent.class);
        bus.register(this, DisplayModeEvent.class);
    }
    
    /**
     * Implemented as specified by {@link Agent}. 
     * @see Agent#canTerminate()
     */
    public boolean canTerminate() { return true; }

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
    	if (e instanceof RndSettingsCopied)
    		handleRndSettingsCopied((RndSettingsCopied) e);
    	else if (e instanceof CopyRndSettings)
			handleCopyRndSettings((CopyRndSettings) e);
    	else if (e instanceof CopyItems)
			handleCopyItems((CopyItems) e);
    	else if (e instanceof UserGroupSwitched)
			handleUserGroupSwitched((UserGroupSwitched) e);
    	else if (e instanceof ReconnectedEvent)
			handleReconnectedEvent((ReconnectedEvent) e);
    	else if (e instanceof AnnotatedEvent)
			handleAnnotatedEvent((AnnotatedEvent) e);
    	else if (e instanceof DisplayModeEvent)
			handleDisplayModeEvent((DisplayModeEvent) e);
    }
    
}
