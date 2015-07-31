/*
 * org.openmicroscopy.shoola.agents.fsimporter.view.ImporterModel 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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

import ij.ImagePlus;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.filechooser.FileFilter;

import omero.model.ImageI;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.openmicroscopy.shoola.agents.fsimporter.AnnotationDataLoader;
import org.openmicroscopy.shoola.agents.fsimporter.DataLoader;
import org.openmicroscopy.shoola.agents.fsimporter.DataObjectCreator;
import org.openmicroscopy.shoola.agents.fsimporter.DiskSpaceLoader;
import org.openmicroscopy.shoola.agents.fsimporter.ImagesImporter;
import org.openmicroscopy.shoola.agents.fsimporter.ImportResultLoader;
import org.openmicroscopy.shoola.agents.fsimporter.ImporterAgent;
import org.openmicroscopy.shoola.agents.fsimporter.MeasurementsSaver;
import org.openmicroscopy.shoola.agents.fsimporter.ROISaver;
import org.openmicroscopy.shoola.agents.fsimporter.TagsLoader;
import org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponent;
import org.openmicroscopy.shoola.agents.fsimporter.util.ObjectToCreate;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.model.FileObject;
import org.openmicroscopy.shoola.env.data.model.ImportableObject;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.util.roi.io.ROIReader;

import com.google.common.io.Files;

import pojos.DataObject;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.ProjectData;
import pojos.ROIData;
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
	private int state;

	/** Reference to the component that embeds this model. */
	protected Importer component;

	/** The collection of existing tags. */
	private Collection tags;
	
	/** Keeps track of the different loaders. */
	private Map<Integer, ImagesImporter> loaders;
	
	/** The id of the selected group of the current user. */
	private long groupId;

	/** The id of the user currently logged in.*/
	private long experimenterId;
	
	/** The security context.*/
	private SecurityContext ctx; //to be initialized.
	
	/** Returns <code>true</code> if it is opened as a standalone app.*/
	private boolean master;
	
	/** The display mode.*/
    private int displayMode;

    /** The id of the user to import for.*/
    private long userId;
    
	/** Initializes the model.*/
	private void initialize()
	{
		groupId = -1;
		experimenterId = -1;
		userId = -1;
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
			if (ImporterAgent.isAdministrator()) return true;
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
		ExperimenterData exp = ImporterAgent.getUserDetails();
		if (groupId < 0) {
		    this.groupId = exp.getDefaultGroup().getGroupId();
		}
		ctx = new SecurityContext(this.groupId);
		experimenterId = exp.getId();
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
	 * @param user The user to load the data for.
	 */
	void fireContainerLoading(Class rootType, boolean refreshImport, boolean 
			changeGroup, ExperimenterData user)
	{
		if (!(ProjectData.class.equals(rootType) ||
			ScreenData.class.equals(rootType))) return;
		if (user != null) {
		    ctx.setExperimenter(user);
		    ctx.sudo();
		}
		DataLoader loader = new DataLoader(component, ctx, rootType,
				refreshImport, changeGroup);
		loader.load();
	}

	/**
	 * Fires an asynchronous call to load the import log file.
	 *
	 * @param fileSetID The fileSet id.
	 * @param index The index of the UI element.
	 */
	void fireImportLogFileLoading(long fileSetID, int index)
	{
		AnnotationDataLoader loader = new AnnotationDataLoader(component, ctx,
				fileSetID, index);
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
	
	/**
	 * Returns the security context.
	 * 
	 * @return See above.
	 */
	SecurityContext getSecurityContext() { return ctx; }

	/**
	 * Fires a call to load the thumbnails or the plate.
	 * 
	 * @param pixels The objects to load.
	 * @param type The type of data to handle.
	 * @param component The component the result is for.
	 * @param user The experimenter to import data for.
	 */
	void fireImportResultLoading(Collection<DataObject> pixels, Class<?> type,
			Object component, ExperimenterData user)
	{
	    if (user != null) {
	        long currentUser = ImporterAgent.getUserDetails().getId();
	        if (currentUser != user.getId()) {
	            ctx.setExperimenter(user);
	            ctx.sudo();
	        }
        }
		ImportResultLoader loader = new ImportResultLoader(this.component, ctx,
				pixels, type, component);
		loader.load();
	}
	
    /**
     * Returns the id of the user to import data for.
     *
     * @return See above.
     */
    long getImportFor()
    {
        if (!canImportAs() || userId < 0) return experimenterId;
        return userId;
    }

    /**
     * Sets the id of the user to import the data for.
     * 
     * @param userId The id of the user.
     */
    void setImportFor(long userId)
    {
        if (canImportAs()) this.userId = userId;
    }

    /**
     * Returns <code>true</code> if the user currently log in can import as,
     * <code>false</code> otherwise.
     *
     * @return
     */
    boolean canImportAs()
    {
        if (ImporterAgent.isAdministrator()) return true;
        return CollectionUtils.isNotEmpty(getGroupsLeaderOf());
    }

    /**
     * Returns <code>true</code> if the group is a system group e.g. System
     * <code>false</code> otherwise.
     *
     * @param id The identifier of the group.
     * @param key One of the constants defined by <code>GroupData</code>
     * @return See above.
     */
    boolean isSystemGroup(long id, String key)
    {
        return ImporterAgent.getRegistry().getAdminService().isSecuritySystemGroup(id, key);
    }

    /**
     * Returns the collection of groups the current user is the leader of.
     * 
     * @return See above.
     */
    Set<GroupData> getGroupsLeaderOf()
    {
        Set<GroupData> values = new HashSet<GroupData>();
        Collection<GroupData> groups = getAvailableGroups();
        Iterator<GroupData> i = groups.iterator();
        GroupData g;
        Set<ExperimenterData> leaders;
        ExperimenterData exp = ImporterAgent.getUserDetails();
        long id = exp.getId();
        Iterator<ExperimenterData> j;
        while (i.hasNext()) {
            g = (GroupData) i.next();
            leaders = g.getLeaders();
            if (CollectionUtils.isNotEmpty(leaders)) {
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
     * Returns the groups the current user is a member of.
     * 
     * @return See above.
     */
    Collection<GroupData> getAvailableGroups()
    {
        return ImporterAgent.getAvailableUserGroups();
    }

    /**
     * Saves the roi if any associated to the image.
     *
     * @param c The component to handle.
     * @param images The images to handle.
     */
    void saveROI(FileImportComponent c, List<ImageData> images)
    {
        FileObject object = c.getOriginalFile();
        if (object.isImagePlus() && CollectionUtils.isNotEmpty(images)) {
            ROIReader reader = new ROIReader();
            SecurityContext ctx = new SecurityContext(c.getGroupID());
            ImagePlus img = (ImagePlus) object.getFile();
            List<FileObject> files = object.getAssociatedFiles();
            List<ROIData> rois;
            Map<Integer, List<ROIData>> indexes =
                new HashMap<Integer, List<ROIData>>();
            int index;
            boolean mif = false;
            if (CollectionUtils.isNotEmpty(files)) {
                mif = true;
                Iterator<FileObject> j = files.iterator();
                FileObject o;
                while (j.hasNext()) {
                    o = j.next();
                    if (o.isImagePlus()) {
                        index = o.getIndex();
                        rois = reader.readImageJROI(-1, (ImagePlus) o.getFile());
                        indexes.put(index, rois);
                        if (index < 0) {
                            mif = false;
                        }
                    }
                }
            }

            //convert rois from manager.
            //rois from manager so we need to link them to all the images
            ImageData data;
            long id;
            Iterator<ImageData> i = images.iterator();
            while (i.hasNext()) {
                data = i.next();
                id = data.getId();
                index = data.getSeries();
                //First check overlay
                rois = null;
                if (indexes.containsKey(index)) {
                   rois = indexes.get(index);
                   linkRoisToImage(id, rois);
                } else {
                   if (!mif) {
                       rois = reader.readImageJROI(id, img);
                   }
                }
                //check roi manager
                if (CollectionUtils.isEmpty(rois)) {
                    rois = reader.readImageJROI(id);
                }
                if (CollectionUtils.isNotEmpty(rois)) {
                    ROISaver saver = new ROISaver(component, ctx, rois, id,
                        c.getExperimenterID(), c);
                    saver.load();
                }
                //Save the measurements
                File f = createFile(data.getName());
                if (f != null) {
                    MeasurementsSaver ms = new MeasurementsSaver(
                            component, ctx, new FileAnnotationData(f),
                            data, c.getExperimenterID());
                    ms.load();
                }
            }
        }
    }

    /**
     * Create a temporary file
     *
     * @param imageName The image object to handle.
     * @return See above.
     */
    private File createFile(String imageName)
    {
        File dir = Files.createTempDir();
        String name = "ImageJ-"+FilenameUtils.getBaseName(
                FilenameUtils.removeExtension(imageName))+"-Results-";
        name += new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        name += ".csv";
        try {
            File f = new File(dir, name);
            //read data
            ROIReader reader = new ROIReader();
            if (!reader.readResults(f)) {
                f.delete();
                dir.delete();
                return null;
            }
            dir.deleteOnExit();
            return f;
        } catch (Exception e) {
            ImporterAgent.getRegistry().getLogger().error(this,
                    "Cannot create file to save results"+e.getMessage());
        }
        return null;
    }
    /**
     * Links the rois to the image.
     *
     * @param imageID The image's id.
     * @param rois The rois to link to the image.
     */
    private void linkRoisToImage(long imageID, List<ROIData> rois)
    {
        if (CollectionUtils.isEmpty(rois)) return;
        Iterator<ROIData> i = rois.iterator();
        while (i.hasNext()) {
            i.next().setImage(new ImageI(imageID, false));
        }
    }

}