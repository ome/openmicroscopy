/*
 * org.openmicroscopy.shoola.agents.fsimporter.ImporterAgent 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.fsimporter;


//Java imports
import java.util.List;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.importer.LoadImporter;
import org.openmicroscopy.shoola.agents.fsimporter.view.Importer;
import org.openmicroscopy.shoola.agents.fsimporter.view.ImporterFactory;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.Environment;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.events.UserGroupSwitched;
import org.openmicroscopy.shoola.env.data.util.AgentSaveInfo;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.env.event.EventBus;
import pojos.ExperimenterData;
import pojos.GroupData;

/** 
 * This agent interacts is used to import images.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class ImporterAgent 
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
	 * Returns how deep to scan when a folder is selected.
	 * 
	 * @return See above.
	 */
	public static int getScanningDepth()
	{
		Integer value = (Integer) registry.lookup("/options/ScanningDepth");
		if (value == null || value.intValue() < 0) return 1;
		return value.intValue();
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
	 * Handles the {@link LoadImporter} event.
	 * 
	 * @param evt The event to handle.
	 */
    private void handleLoadImporter(LoadImporter evt)
    {
    	if (evt == null) return;
    	Importer importer = ImporterFactory.getImporter();
    	if (importer != null) {
    		int type = evt.getType();
    		switch (evt.getType()) {
				case LoadImporter.PROJECT_TYPE:
					type = Importer.PROJECT_TYPE;
					break;
				case LoadImporter.SCREEN_TYPE:
					type = Importer.SCREEN_TYPE;
			}
    		importer.activate(type, evt.getSelectedContainer(), 
    				evt.getObjects());
    	}
    }

    /**
     * Removes all the references to the existing viewers.
     * 
     * @param evt The event to handle.
     */
    private void handleUserGroupSwitched(UserGroupSwitched evt)
    {
    	if (evt == null) return;
    	ImporterFactory.onGroupSwitched(evt.isSuccessful());
    }
    
	/** Creates a new instance. */
	public ImporterAgent() {}
	
	 /**
     * Implemented as specified by {@link Agent}.
     * @see Agent#activate(boolean)
     */
    public void activate(boolean master)
    {
    	if (!master) return;

    	ExperimenterData exp = (ExperimenterData) registry.lookup(
    			LookupNames.CURRENT_USER_DETAILS);
    	if (exp == null) return;
    	GroupData gp = null;
    	try {
    		gp = exp.getDefaultGroup();
    	} catch (Exception e) {
    		//No default group
    	}
    	long id = -1;
    	if (gp != null) id = gp.getId();
    	Importer importer = ImporterFactory.getImporter(id);
    	if (importer != null) {
    		//get type from config
    		Environment env = (Environment) registry.lookup(LookupNames.ENV);
    		int type = Importer.PROJECT_TYPE;
        	if (env != null) {
        		switch (env.getDefaultHierarchy()) {
        			case LookupNames.PD_ENTRY:
        			default:
        				type = Importer.PROJECT_TYPE;
        				break;
        			case LookupNames.HCS_ENTRY:
        				type = Importer.SCREEN_TYPE;
        		}
        	}
    		
    		
    		importer.activate(type, null, null);
    	}
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
        bus.register(this, LoadImporter.class);
        bus.register(this, UserGroupSwitched.class);
    }

    /**
     * Implemented as specified by {@link Agent}. 
     * @see Agent#canTerminate()
     */
    public boolean canTerminate()
    { 
    	Importer importer = ImporterFactory.getImporter();
    	if (importer == null) return true;
    	return !importer.hasOnGoingImport();
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
     * Responds to an event fired trigger on the bus.
     * 
     * @see AgentEventListener#eventFired(AgentEvent)
     */
    public void eventFired(AgentEvent e)
    {
    	if (e instanceof LoadImporter)
			handleLoadImporter((LoadImporter) e);
    	else if (e instanceof UserGroupSwitched)
			handleUserGroupSwitched((UserGroupSwitched) e);
    }

}
