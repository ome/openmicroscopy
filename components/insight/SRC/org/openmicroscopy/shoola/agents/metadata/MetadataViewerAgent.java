/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.metadata;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import ome.model.units.BigResult;

import org.apache.commons.collections.CollectionUtils;

import org.openmicroscopy.shoola.agents.events.iviewer.RndSettingsCopied;
import org.openmicroscopy.shoola.agents.events.metadata.ChannelSavedEvent;
import org.openmicroscopy.shoola.agents.events.treeviewer.DisplayModeEvent;
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewer;
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewerFactory;
import org.openmicroscopy.shoola.agents.metadata.view.RndSettingsPasted;
import org.openmicroscopy.shoola.agents.events.iviewer.CopyRndSettings;
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
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import omero.gateway.model.ChannelData;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;
import omero.gateway.model.ImageData;


/** 
 * The MetadataViewerAgent agent. This agent displays metadata related to 
 * an object.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class MetadataViewerAgent 
	implements Agent, AgentEventListener
{

	/** Reference to the registry. */
    private static Registry         registry; 

    /** The display mode.*/
	private int displayMode = -1;
	
    /**
     * Helper method. 
     * 
     * @return A reference to the <code>Registry</code>
     */
    public static Registry getRegistry() { return registry; }
    
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
	 * Helper method returning <code>true</code> if the connection is fast,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public static boolean isFastConnection()
	{
		int value = (Integer) registry.lookup(LookupNames.CONNECTION_SPEED);
		return value == RenderingControl.UNCOMPRESSED;
	}
	
	/** 
	 * Returns the path of the 'omero home' directory e.g. user/omero.
	 * 
	 * @return See above. 
	 */ 
	public static String getOmeroHome() 
	{ 
		Environment env = (Environment) registry.lookup(LookupNames.ENV); 
		String omeroDir = env.getOmeroHome(); 
		File home = new File(omeroDir); 
		if (!home.exists()) home.mkdir(); 
		return omeroDir; 
	}
	
	/** 
	 * Returns the path of the 'omero home' directory e.g. user/omero.
	 * 
	 * @return See above. 
	 */ 
	public static String getOmeroFilesHome() 
	{ 
		Environment env = (Environment) registry.lookup(LookupNames.ENV); 
		String omeroDir = env.getOmeroFilesHome();
		File home = new File(omeroDir); 
		if (!home.exists()) home.mkdir(); 
		return omeroDir; 
	}
 	
	/** 
	 * Returns the temporary directory.
	 * 
	 * @return See above. 
	 */ 
	public static String getTmpDir()
	{ 
		Environment env = (Environment) registry.lookup(LookupNames.ENV); 
		return env.getTmpDir();
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
     * Convenience method for logging BigResult exceptions
     * 
     * @param src
     *            The origin of the exception
     * @param exception
     *            The exception
     * @param property
     *            The property which conversion triggered the exception
     */
    public static void logBigResultExeption(Object src, Object exception,
            String property) {
        if (exception instanceof BigResult) {
            MetadataViewerAgent
                    .getRegistry()
                    .getLogger()
                    .warn(src,
                            "Arithmetic overflow; "
                                    + property
                                    + " is "
                                    + ((BigResult) exception).result
                                            .doubleValue());
        }
    }
	
    /**
     * Handles the {@link UserGroupSwitched} event.
     * 
     * @param evt The event to handle.
     */
    private void handleUserGroupSwitched(UserGroupSwitched evt)
    {
    	if (evt == null) return;
    	MetadataViewerFactory.onGroupSwitched(evt.isSuccessful());
    }
    
    /**
     * Indicates that it was possible to reconnect.
     * 
     * @param evt The event to handle.
     */
    private void handleReconnectedEvent(ReconnectedEvent evt)
    {
    	MetadataViewerFactory.onGroupSwitched(true);
    }
    
    /**
     * Updates the view when the channels have been updated.
     * 
     * @param evt The event to handle.
     */
    private void handleChannelSavedEvent(ChannelSavedEvent evt)
    {
    	List<ChannelData> channels = evt.getChannels();
    	Iterator<Long> i = evt.getImageIds().iterator();
    	MetadataViewer viewer;
    	while (i.hasNext()) {
    		viewer = MetadataViewerFactory.getViewerFromId(
    				ImageData.class.getName(), i.next());
			if (viewer != null) {
				viewer.onUpdatedChannels(channels);
			}
		}
    }
    
    /**
     * Handles the {@link RndSettingsCopied} event.
     * 
     * @param evt
     *            The event to handle.
     */
    private void handleRndSettingsCopied(RndSettingsCopied evt) {
        Collection<Long> ids = evt.getImagesIDs();
        if (CollectionUtils.isEmpty(ids))
            return;
        Iterator<Long> i = ids.iterator();
        MetadataViewer viewer;
        while (i.hasNext()) {
            viewer = MetadataViewerFactory.getViewerFromId(
                    ImageData.class.getName(), i.next());
            if (viewer != null && viewer.isRendererLoaded()) {
                viewer.resetRenderingControl();
            }
        }
    }
    
    /**
     * Handles a {@link CopyRndSettings} event, i. e. passes the image
     * reference on to the {@link MetadataViewer}s.
     * @param evt The event
     */
    private void handleCopyRndSettings(CopyRndSettings evt) {
        MetadataViewerFactory.setCopyRenderingSettingsFrom(evt.getImage(), evt.getRndDef());
    }
    
    /**
     * Handles a {@link RndSettingsPasted} event, i. e. notifies 
     * the {@link MetadataViewer}s to apply the settings of an
     * previously set image; see also {@link CopyRndSettings}
     * @param e
     */
    private void handleRndSettingsPasted(RndSettingsPasted e) {
        MetadataViewerFactory.applyCopiedRndSettings(e.getImageId());
    }
    
    /**
     * Updates the view when the mode is changed.
     * 
     * @param evt The event to handle.
     */
    private void handleDisplayModeEvent(DisplayModeEvent evt)
    {
    	displayMode = evt.getDisplayMode();
    	MetadataViewerFactory.setDiplayMode(displayMode);
    }
    
    /** Creates a new instance. */
    public MetadataViewerAgent() {}
    
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
    		MetadataViewerFactory.onGroupSwitched(true);
    }

    /** 
     * Implemented as specified by {@link Agent}. 
     * @see Agent#setContext(Registry)
     */
    public void setContext(Registry ctx)
    {
        registry = ctx;
        EventBus bus = ctx.getEventBus();
        bus.register(this, UserGroupSwitched.class);
        bus.register(this, ReconnectedEvent.class);
        bus.register(this, ChannelSavedEvent.class);
        bus.register(this, DisplayModeEvent.class);
        bus.register(this, RndSettingsCopied.class);
        bus.register(this, CopyRndSettings.class);
        bus.register(this, RndSettingsPasted.class);
    }

    /**
     * Implemented as specified by {@link Agent}. 
     * @see Agent#canTerminate()
     */
    public boolean canTerminate() {  return true;  }
    
    /**
     * Implemented as specified by {@link Agent}. 
     * @see Agent#getDataToSave()
     */
    public AgentSaveInfo getDataToSave()
    {
    	List<Object> instances = MetadataViewerFactory.getInstancesToSave();
    	if (instances == null || instances.size() == 0) return null;
    	return new AgentSaveInfo("Edition", instances);
	}
    
    /**
     * Implemented as specified by {@link Agent}. 
     * @see Agent#save(List)
     */
    public void save(List<Object> instances)
    { 
    	MetadataViewerFactory.saveInstances(instances);
    }
    
    /**
     * Responds to events fired trigger on the bus.
     * @see AgentEventListener#eventFired(AgentEvent)
     */
	public void eventFired(AgentEvent e)
	{
		if (e instanceof UserGroupSwitched)
			handleUserGroupSwitched((UserGroupSwitched) e);
		else if (e instanceof ReconnectedEvent)
			handleReconnectedEvent((ReconnectedEvent) e);
		else if (e instanceof ChannelSavedEvent)
			handleChannelSavedEvent((ChannelSavedEvent) e);
		else if (e instanceof DisplayModeEvent)
			handleDisplayModeEvent((DisplayModeEvent) e);
		else if (e instanceof RndSettingsCopied)
	            	handleRndSettingsCopied((RndSettingsCopied) e);
		else if (e instanceof CopyRndSettings) 
                   	 handleCopyRndSettings((CopyRndSettings) e);
		else if (e instanceof RndSettingsPasted) 
                    	handleRndSettingsPasted((RndSettingsPasted) e);
	}

}
