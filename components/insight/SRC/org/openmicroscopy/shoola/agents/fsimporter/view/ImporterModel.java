/*
 * org.openmicroscopy.shoola.agents.fsimporter.view.ImporterModel 
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
package org.openmicroscopy.shoola.agents.fsimporter.view;

//Java imports
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.filechooser.FileFilter;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.fsimporter.DataLoader;
import org.openmicroscopy.shoola.agents.fsimporter.DataObjectCreator;
import org.openmicroscopy.shoola.agents.fsimporter.DiskSpaceLoader;
import org.openmicroscopy.shoola.agents.fsimporter.ImagesImporter;
import org.openmicroscopy.shoola.agents.fsimporter.ImporterAgent;
import org.openmicroscopy.shoola.agents.fsimporter.TagsLoader;
import org.openmicroscopy.shoola.agents.fsimporter.util.ObjectToCreate;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.model.ImportableObject;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import pojos.ExperimenterData;
import pojos.GroupData;
import pojos.ProjectData;
import pojos.ScreenData;

/** 
 * The Model component in the <code>Importer</code> MVC triad.
 * This class tracks the <code>Importer</code>'s state and knows how to
 * initiate data retrievals. It also knows how to store and manipulate
 * the results. This class  provide  a suitable data loader. 
 * The {@link ImporterComponent} intercepts the 
 * results of data loadings, feeds them back to this class and fires state
 * transitions as appropriate.
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
class ImporterModel
{

	/** Holds one of the state flags defined by {@link Importer}. */
	private int					state;

	/** Reference to the component that embeds this model. */
	protected Importer			component;

	/** The collection of existing tags. */
	private Collection			tags;
	
	/** Keeps track of the different loaders. */
	private Map<Integer, ImagesImporter> loaders;
	
	/** The id of the selected group of the current user. */
	private long					groupId;

	/** The id of the user currently logged in.*/
	private long 					experimenterId;
	
	/** The security context.*/
	private SecurityContext ctx; //to be initialized.
	
	/** Returns <code>true</code> if it is opened as a standalone app.*/
	private boolean master;
	
	/** The display mode.*/
    private int displayMode;
    
	/** Initializes the model.*/
	private void initialize()
	{
		groupId = -1;
		experimenterId = -1;
		state = Importer.NEW;
		loaders = new HashMap<Integer, ImagesImporter>();
		checkDefaultDisplayMode();
	}

	/**
	 * Invokes the value is not set. 
	 */
	private void checkDefaultDisplayMode()
	{
		Integer value = (Integer) ImporterAgent.getRegistry().lookup(
    			LookupNames.DATA_DISPLAY);
		if (value == null) setDisplayMode(LookupNames.EXPERIMENTER_DISPLAY);
		else setDisplayMode(value.intValue());
	}
	
	/**
	 * Indicates to load all annotations available if the user can annotate
	 * and is an administrator/group owner or to only load the user's
	 * annotation.
	 * 
	 * @return See above
	 */
	private boolean canRetrieveAll()
	{
		GroupData group = getGroup(getGroupId());
		if (group == null) return false;
		if (GroupData.PERMISSIONS_GROUP_READ ==
			group.getPermissions().getPermissionsLevel()) {
			if (MetadataViewerAgent.isAdministrator()) return true;
			Set leaders = group.getLeaders();
			Iterator i = leaders.iterator();
			long userID = getExperimenterId();
			ExperimenterData exp;
			while (i.hasNext()) {
				exp = (ExperimenterData) i.next();
				if (exp.getId() == userID)
					return true;
			}
			return false;
		}
		return true;
	}
	
	/**
	 * Returns the group corresponding to the specified id or <code>null</code>.
	 * 
	 * @param groupId The identifier of the group.
	 * @return See above.
	 */
	private GroupData getGroup(long groupId)
	{
		Collection groups = ImporterAgent.getAvailableUserGroups();
		if (groups == null) return null;
		Iterator i = groups.iterator();
		GroupData group;
		while (i.hasNext()) {
			group = (GroupData) i.next();
			if (group.getId() == groupId) return group;
		}
		return null;
	}
	
	/** 
	 * Creates a new instance.
	 *
	 * @param groupID The id to the group selected for the current user.
	 * @param displayMode Group/Experimenter view.
	 */
	ImporterModel(long groupId, int displayMode)
	{
		this(groupId, false, displayMode);
	}
	
	/** 
	 * Creates a new instance.
	 *
	 * @param groupID The id to the group selected for the current user.
	 * @param master Pass <code>true</code> if the importer is used a stand-alone
	 * application, <code>false</code> otherwise.
	 * @param displayMode Group/Experimenter view.
	 */
	ImporterModel(long groupId, boolean master, int displayMode)
	{
		this.master = master;
		initialize();
		setGroupId(groupId);
		setDisplayMode(displayMode);
	}
	
	/**
	 * Sets the group's identifier.
	 * 
	 * @param groupId The group's identifier.
	 */
	void setGroupId(long groupId)
	{ 
		this.groupId = groupId;
		ctx = new SecurityContext(groupId);
		experimenterId = ImporterAgent.getUserDetails().getId();
		tags = null;
	}
	
	/**
	 * Returns the group's identifier.
	 * 
	 * @return See above.
	 */
	long getGroupId() { return groupId; }
	
	/**
	 * Returns the experimenter's identifier.
	 * 
	 * @return See above.
	 */
	long getExperimenterId() { return experimenterId; }
	
	/**
	 * Returns <code>true</code> if the agent is the entry point
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isMaster() { return master; }
	
	/**
	 * Called by the <code>FSImporter</code> after creation to allow this
	 * object to store a back reference to the embedding component.
	 * 
	 * @param component The embedding component.
	 */
	void initialize(Importer component)
	{
		this.component = component;
	}
	
	/**
	 * Returns the current state.
	 * 
	 * @return One of the flags defined by the {@link Importer} interface.  
	 */
	int getState() { return state; }

	/**
	 * Sets the state of the component.
	 * 
	 * @param state The value to set.
	 */
	void setState(int state) { this.state = state; }
	
	/**
	 * Sets the object in the {@link Importer#DISCARDED} state.
	 * Any ongoing data loading will be cancelled.
	 */
	void discard()
	{
		cancel();
		if (loaders.size() > 0) {
			Iterator<ImagesImporter> i = loaders.values().iterator();
			while (i.hasNext()) {
				i.next().cancel();
			}
			loaders.clear();
		}
		state = Importer.DISCARDED;
	}

	/**
	 * Sets the object in the {@link Importer#READY} state.
	 * Any ongoing data loading will be cancelled.
	 */
	void cancel()
	{
		//state = Importer.READY;
	}

	/**
	 * Cancels the specified on-going import.
	 * 
	 * @param loaderID The identifier of the loader.
	 */
	void cancel(int loaderID)
	{
		ImagesImporter loader = loaders.get(loaderID);
		if (loader != null) {
			//loader.cancel();
			loaders.remove(loaderID);
		}
	}
	
	/**
	 * Returns the list of the supported file formats.
	 * 
	 * @return See above.
	 */
	FileFilter[] getSupportedFormats()
	{
		return 
		ImporterAgent.getRegistry().getImageService().getSupportedFileFormats();
	}
	
	/**
	 * Fires an asynchronous call to import the files in the object.
	 * 
	 * @param data The file to import.
	 * @param loaderID The identifier of the component this loader is for.
	 */
	void fireImportData(ImportableObject data, int loaderID)
	{
		if (data == null) return;
		ImagesImporter loader = new ImagesImporter(component, data, loaderID);
		loaders.put(loaderID, loader);
		loader.load();
		state = Importer.IMPORTING;
	}
	
	/**
	 * Notifies that the import has finished.
	 * 
	 * @param loaderID  The identifier of the loader associated to the finished
	 * 					import.
	 */
	void importCompleted(int loaderID)
	{
		state = Importer.READY;
		loaders.remove(loaderID);
	}
	
	/**
	 * Returns the collection of the existing tags.
	 * 
	 * @return See above.
	 */
	Collection getTags() { return tags; }
	
	/**
	 * Sets the collection of the existing tags.
	 * 
	 * @param The value to set.
	 */
	void setTags(Collection tags)
	{ 
		this.tags = tags; 
	}
	
	/** Starts an asynchronous call to load the tags. */
	void fireTagsLoading()
	{
		if (tags != null) return; //already loading tags
		TagsLoader loader = new TagsLoader(component, ctx, canRetrieveAll());
		loader.load();
	}
	
	/** Starts an asynchronous call to load the available disk space. */
	void fireDiskSpaceLoading()
	{
		DiskSpaceLoader loader = new DiskSpaceLoader(component, ctx);
		loader.load();
	}
	
	/**
	 * Fires an asynchronous call to load the container.
	 * 
	 * @param rootType The type of nodes to load.
	 * @param refreshImport Flag indicating to refresh the on-going import.
	 * @param changeGroup Flag indicating that the group has been modified
	 * if <code>true</code>, <code>false</code> otherwise.
	 * @param userID The id of the user to load the data for.
	 */
	void fireContainerLoading(Class rootType, boolean refreshImport, boolean 
			changeGroup, long userID)
	{
		if (!(ProjectData.class.equals(rootType) ||
			ScreenData.class.equals(rootType))) return;
		if (userID < 0) userID = getExperimenterId();
		DataLoader loader = new DataLoader(component, ctx, rootType,
				refreshImport, changeGroup, userID);
		loader.load();
	}

	/**
	 * Creates a new data object.
	 * 
	 * @param data The object hosting information about the object to create.
	 */
	void fireDataCreation(ObjectToCreate data)
	{
		SecurityContext ctx = new SecurityContext(data.getGroup().getId());
		ctx.setServerInformation(this.ctx.getHostName(), this.ctx.getPort());
		ctx.setExperimenter(data.getExperimenter());
		DataObjectCreator loader = new DataObjectCreator(component, ctx,
				data.getChild(), data.getParent());
		loader.load();
	}
	
    /**
     * Returns <code>true</code> if only one group for the user,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean isSingleGroup()
    { 
    	Collection l = ImporterAgent.getAvailableUserGroups();
    	return (l.size() <= 1);
    }

    /**
	 * Returns the display mode.
	 * 
	 * @return See above.
	 */
	int getDisplayMode() { return displayMode; }

	/**
	 * Sets the display mode.
	 * 
	 * @param value The value to set.
	 */
	void setDisplayMode(int value)
	{
		if (value < 0) {
			checkDefaultDisplayMode();
			return;
		}
		switch (value) {
			case LookupNames.EXPERIMENTER_DISPLAY:
			case LookupNames.GROUP_DISPLAY:
				displayMode = value;
				break;
			default:
				displayMode = LookupNames.EXPERIMENTER_DISPLAY;
		}
		if (tags != null) {
			tags.clear();
			tags = null;
		}
	}

}