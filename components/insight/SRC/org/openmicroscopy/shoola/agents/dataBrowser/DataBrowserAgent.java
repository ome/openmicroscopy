/*
 * org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserAgent 
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
package org.openmicroscopy.shoola.agents.dataBrowser;


//Java imports
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowserFactory;
import org.openmicroscopy.shoola.agents.events.iviewer.CopyRndSettings;
import org.openmicroscopy.shoola.agents.events.iviewer.RndSettingsCopied;
import org.openmicroscopy.shoola.agents.events.treeviewer.CopyItems;
import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.env.event.EventBus;
import pojos.ExperimenterData;

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
    
    /**
     * Helper method. 
     * 
     * @return A reference to the <code>Registry</code>.
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
    	DataBrowserFactory.setRndSettingsToCopy(evt.getImage() != null);
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
    
	/** Creates a new instance. */
	public DataBrowserAgent() {}
	
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
        bus.register(this, RndSettingsCopied.class);
        bus.register(this, CopyRndSettings.class);
        bus.register(this, CopyItems.class);
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
		return null;
	}
    
    /**
     * Responds to an event fired trigger on the bus.
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
    }
    
}
