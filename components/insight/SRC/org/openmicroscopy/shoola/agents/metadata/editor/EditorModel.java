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
package org.openmicroscopy.shoola.agents.metadata.editor;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.swing.Icon;
import javax.swing.JFrame;

import org.apache.commons.collections.CollectionUtils;
import org.openmicroscopy.shoola.util.CommonsLangUtils;

import omero.model.OriginalFile;
import omero.model.PlaneInfo;
import org.openmicroscopy.shoola.agents.metadata.AcquisitionDataLoader;
import org.openmicroscopy.shoola.agents.metadata.AnalysisResultsFileLoader;
import org.openmicroscopy.shoola.agents.metadata.FileAnnotationChecker;
import org.openmicroscopy.shoola.agents.metadata.AttachmentsLoader;
import org.openmicroscopy.shoola.agents.metadata.ChannelDataLoader;
import org.openmicroscopy.shoola.agents.metadata.ChannelDataSaver;
import org.openmicroscopy.shoola.agents.metadata.DiskSpaceLoader;
import org.openmicroscopy.shoola.agents.metadata.EditorLoader;
import org.openmicroscopy.shoola.agents.metadata.EnumerationLoader;
import org.openmicroscopy.shoola.agents.metadata.FileLoader;
import org.openmicroscopy.shoola.agents.metadata.FilesetLoader;
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.ImageSizeLoader;
import org.openmicroscopy.shoola.agents.metadata.InstrumentDataLoader;
import org.openmicroscopy.shoola.agents.metadata.LDAPLoader;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.metadata.OriginalMetadataLoader;
import org.openmicroscopy.shoola.agents.metadata.PasswordEditor;
import org.openmicroscopy.shoola.agents.metadata.PlaneInfoLoader;
import org.openmicroscopy.shoola.agents.metadata.ROILoader;
import org.openmicroscopy.shoola.agents.metadata.RenderingControlLoader;
import org.openmicroscopy.shoola.agents.metadata.ScriptLoader;
import org.openmicroscopy.shoola.agents.metadata.ScriptsLoader;
import org.openmicroscopy.shoola.agents.metadata.TagsLoader;
import org.openmicroscopy.shoola.agents.metadata.UserPhotoLoader;
import org.openmicroscopy.shoola.agents.metadata.UserPhotoUploader;
import org.openmicroscopy.shoola.agents.metadata.browser.Browser;
import org.openmicroscopy.shoola.agents.metadata.rnd.Renderer;
import org.openmicroscopy.shoola.agents.metadata.rnd.RendererFactory;
import org.openmicroscopy.shoola.agents.metadata.util.AnalysisResultsItem;
import org.openmicroscopy.shoola.agents.metadata.util.DataToSave;
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewer;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import org.openmicroscopy.shoola.agents.util.ui.PermissionMenu;
import org.openmicroscopy.shoola.env.Environment;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.AdminService;
import org.openmicroscopy.shoola.env.data.OmeroImageService;
import org.openmicroscopy.shoola.env.data.OmeroMetadataService;
import org.openmicroscopy.shoola.env.data.model.AdminObject;
import org.openmicroscopy.shoola.env.data.model.AnnotationLinkData;
import org.openmicroscopy.shoola.env.data.model.DeletableObject;
import org.openmicroscopy.shoola.env.data.model.DeleteActivityParam;
import org.openmicroscopy.shoola.env.data.model.DownloadActivityParam;
import org.openmicroscopy.shoola.env.data.model.DownloadArchivedActivityParam;
import org.openmicroscopy.shoola.env.data.model.EnumerationObject;
import org.openmicroscopy.shoola.env.data.model.SaveAsParam;
import org.openmicroscopy.shoola.env.data.model.ScriptObject;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.util.StructuredDataResults;
import omero.log.LogMessage;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.file.modulo.ModuloInfo;
import org.openmicroscopy.shoola.util.file.modulo.ModuloParser;
import org.openmicroscopy.shoola.util.ui.component.ObservableComponent;

import omero.gateway.model.AnnotationData;
import omero.gateway.model.BooleanAnnotationData;
import omero.gateway.model.ChannelAcquisitionData;
import omero.gateway.model.ChannelData;
import omero.gateway.model.DataObject;
import omero.gateway.model.DatasetData;
import omero.gateway.model.DoubleAnnotationData;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.FileData;
import omero.gateway.model.FilesetData;
import omero.gateway.model.GroupData;
import omero.gateway.model.ImageAcquisitionData;
import omero.gateway.model.ImageData;
import omero.gateway.model.InstrumentData;
import omero.gateway.model.LongAnnotationData;
import omero.gateway.model.MapAnnotationData;
import omero.gateway.model.PermissionData;
import omero.gateway.model.PixelsData;
import omero.gateway.model.PlateAcquisitionData;
import omero.gateway.model.PlateData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.RatingAnnotationData;
import omero.gateway.model.ScreenData;
import omero.gateway.model.TagAnnotationData;
import omero.gateway.model.TermAnnotationData;
import omero.gateway.model.TextualAnnotationData;
import omero.gateway.model.WellData;
import omero.gateway.model.WellSampleData;
import omero.gateway.model.XMLAnnotationData;

/** 
 * The Model component in the <code>EditorViewer</code> MVC triad.
 * This class knows how to store and manipulate the results. 
 * It provides a suitable data loader.
 * The {@link EditorComponent} intercepts the results of data loadings, 
 * feeds them back to this class and fires state transitions as appropriate.
 *
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
class EditorModel 
{
	
	/** Identifies <code>all</code> the objects.*/
	static final int ALL = PermissionMenu.ALL;
	
	/** Identifies the objects added by current user.*/
	static final int ME = PermissionMenu.ME;
	
	/** Identifies the objects added by others.*/
	static final int OTHER = PermissionMenu.OTHER;
	
	/** The index of the default channel. */
	static final int	DEFAULT_CHANNEL = 0;

	/** Enum to distinguish between different kind of 
	 * {@link MapAnnotationData}, see {@link #getMapAnnotations(MapAnnotationType)}
	 */
	public static enum MapAnnotationType {
		USER, OTHER_USERS, OTHER
	}
	
	/** The file namespaces to exclude.*/
	private final static List<String> EXCLUDED_FILE_NS;
	
	static {
		EXCLUDED_FILE_NS = new ArrayList<String>();
		EXCLUDED_FILE_NS.add(FileAnnotationData.COMPANION_FILE_NS);
		EXCLUDED_FILE_NS.add(FileAnnotationData.FLIM_NS);
	}
	
	/** The parent of this editor. */
	private  MetadataViewer			parent;
	
	/** Reference to the component that embeds this model. */
	private Editor					component;
	
	/** The object this editor is for. */
	private Object					refObject;

	/** The parent of the object this editor is for. */
	private Object					parentRefObject;
	
	/** The parent of the object this editor is for. */
	private Object					gpRefObject;
	
    /** 
     * Map containing the annotations made by users.
     * The keys are the user, the value the collection of
     * textual annotations.
     */ 
    private Map<Long, List>         textualAnnotationsByUsers;
    
    /** The annotations ordered by date. */
    private List					textualAnnotationsByDate;
    
    /** Collection of loaders. */
    private List<EditorLoader>		loaders;

    /** Collection of existing tags if any. */
    private Collection				existingTags;
    
    /** Collection of existing attachments if any. */
    private Collection				existingAttachments;
    
    /** The list of emission wavelengths for a given set of pixels. */
    private Map						emissionsWavelengths;
    
    /** Used to sort the various collections. */
    private ViewerSorter			sorter;

	/** Reference to the browser. */
	private Browser					browser;
	
	/** The image acquisition data. */
	private ImageAcquisitionData	imageAcquisitionData;
	
	/** The instrument data. */
	private InstrumentData			instrumentData;
	
	/** The enumerations related to channel metadata. */
	private Map<String, List<EnumerationObject>>	channelEnumerations;
	
	/** The enumerations related to image metadata. */
	private Map<String, List<EnumerationObject>>	imageEnumerations;
	
	/** The map hosting the channels acquisition data. */
	private Map<Integer, ChannelAcquisitionData> 	channelAcquisitionDataMap;
	
	/** The map hosting the channels plane info. */
	private Map<Integer, Collection> 	channelPlaneInfoMap;
	
	/** Reference to the renderer. */
	private Renderer 					renderer;
	
	/** Collection of annotations to unlink and delete. */
	private List<AnnotationData>		toDelete;
	
	/** Collection of uploaded scripts. */
	private Map<Long, ScriptObject>		scripts;
	
	/** Scripts with a UI. */
	private List<ScriptObject> scriptsWithUI;
	
	/** The file annotation with the original metadata. */
	private FileAnnotationData originalMetadata;
	
	/** The collection of analysis results. */
	private Map<AnalysisResultsItem, EditorLoader> resultsLoader;
	
    /** The photo of the current user.*/
    private Map<Long, BufferedImage>	usersPhoto;
    
    /** Flag indicating if the image is a big image or not.*/
    private boolean largeImage;
    
    /** The file set associated to the image if an image is selected.*/
    private Set<FilesetData> set;
    
    /** Checks if the image is a large image or not. */
    private void fireLargeImageLoading()
    {
    	ImageData img = null;
		if (refObject instanceof ImageData) {
    		img = (ImageData) refObject;
    	} else if (refObject instanceof WellSampleData) {
    		img = ((WellSampleData) refObject).getImage();
    	}
		PixelsData data = img.getDefaultPixels();
		if (data == null) {
			largeImage = false;
			return;
		}
		ImageSizeLoader loader = new  ImageSizeLoader(component, 
				parent.getSecurityContext(), data.getId()); 
		loader.load();
    }
    
	/**
	 * Downloads the files.
	 * 
	 * @param folder The folder to save the file into.
	 * @param override Flag indicating to override the existing file if it
     * exists, <code>false</code> otherwise.
	 */
	private void downloadFiles(File folder, boolean override)
	{
		UserNotifier un = MetadataViewerAgent.getRegistry().getUserNotifier();
		FileAnnotationData fa = (FileAnnotationData) getRefObject();
		OriginalFile f = (OriginalFile) fa.getContent();
		IconManager icons = IconManager.getInstance();
		
		DownloadActivityParam activity = new DownloadActivityParam(f,
				folder, icons.getIcon(IconManager.DOWNLOAD_22));
		un.notifyActivity(getSecurityContext(), activity);
		
		Collection l = parent.getRelatedNodes();
		if (l == null) return;
		Iterator i = l.iterator();
		Object o;
		while (i.hasNext()) {
			o = i.next();
			if (o instanceof FileAnnotationData) {
				fa = (FileAnnotationData) o;
				f = (OriginalFile) fa.getContent();
				if (f.isLoaded()) {
					activity = new DownloadActivityParam(f,
							folder, icons.getIcon(IconManager.DOWNLOAD_22));
				} else {
					long id = fa.getId();
					Environment env = (Environment) 
					MetadataViewerAgent.getRegistry().lookup(LookupNames.ENV);
					String path = env.getOmeroFilesHome();
					path += File.separator+fa.getFileName();
					activity = new DownloadActivityParam(id, 
							DownloadActivityParam.FILE_ANNOTATION, 
							new File(path), 
							icons.getIcon(IconManager.DOWNLOAD_22));
				}
				activity.setFileName(fa.getFileName());
				un.notifyActivity(getSecurityContext(), activity);
			}
		}
	}
	
	/** 
	 * Downloads the archived images. 
	 * 
	 * @param file The file where to download the content.
	 * @param override Flag indicating to override the existing file if it
     * exists, <code>false</code> otherwise.
	 */
	private void downloadImages(File file, boolean override)
	{
	    List<ImageData> images = new ArrayList<ImageData>();
	    List<DataObject> l = getSelectedObjects();
	    if (!CollectionUtils.isEmpty(l)) {
	        Iterator<DataObject> i = l.iterator();
	        DataObject o;
	        List<Long> filesetIds = new ArrayList<Long>();
	        long id;
	        ImageData image;
	        while (i.hasNext()) {
	            o = i.next();
	            if (isArchived(o)) {
	                image = (ImageData) o;
	                id = image.getFilesetId();
	                if (id < 0) images.add(image);
	                else if (!filesetIds.contains(id)) {
	                    images.add(image);
	                    filesetIds.add(id);
	                }
	            }
	        }
	    }
	    if (!CollectionUtils.isEmpty(images)) {
	        Iterator<ImageData> i = images.iterator();
	        DownloadArchivedActivityParam p;
	        UserNotifier un =
	                MetadataViewerAgent.getRegistry().getUserNotifier();
	        IconManager icons = IconManager.getInstance();
	        Icon icon = icons.getIcon(IconManager.DOWNLOAD_22);
	        SecurityContext ctx = getSecurityContext();
            p = new DownloadArchivedActivityParam(file, images, icon);
            p.setOverride(override);
            un.notifyActivity(ctx, p);
	    }
	}

    /** 
     * Sorts the passed collection of annotations by date starting with the
     * most recent.
     * 
     * @param annotations   Collection of {@link AnnotationData} linked to 
     *                      the currently edited <code>DataObject</code>.
     */
    private void sortAnnotationByDate(List annotations)
    {
        if (annotations == null || annotations.size() == 0) return;
        Comparator c = new Comparator() {
            public int compare(Object o1, Object o2)
            {
                Timestamp t1 = ((AnnotationData) o1).getLastModified(),
                          t2 = ((AnnotationData) o2).getLastModified();
                long n1 = t1.getTime();
                long n2 = t2.getTime();
                int v = 0;
                if (n1 < n2) v = -1;
                else if (n1 > n2) v = 1;
                return -v;
            }
        };
        Collections.sort(annotations, c);
    }
    
    /** 
	 * Sorts the passed nodes by row.
	 * 
	 * @param nodes The nodes to sort.
	 * @return See above.
	 */
	private List sortPlane(Collection nodes)
	{
		List<Object> l = new ArrayList<Object>();
		if (nodes == null) return l;
		Iterator i = nodes.iterator();
		while (i.hasNext()) {
			l.add(i.next());
		}
		Comparator c = new Comparator() {
            public int compare(Object o1, Object o2)
            {
                PlaneInfo i1 = (PlaneInfo) o1, i2 = (PlaneInfo) o2;
                int t1 = i1.getTheT().getValue();
                int t2 = i2.getTheT().getValue();
                int v = 0;
                if (t1 < t2) v = -1;
                else if (t1 > t2) v = 1;
                return v;
            }
        };
        Collections.sort(l, c);
		return l;
	}
	
    /** 
     * Sorts the passed collection of enumerations.
     * 
     * @param enumerations   Collection of {@link EnumerationObject}.
     */
    private void sortEnumerations(List<EnumerationObject> enumerations)
    {
        if (enumerations == null || enumerations.size() == 0) return;
        Comparator c = new Comparator() {
            public int compare(Object o1, Object o2)
            {
                String s1 = ((EnumerationObject) o1).getValue(),
                        s2 = ((EnumerationObject) o2).getValue();
                int v = 0;
                int result = (s1.toLowerCase()).compareTo(s2.toLowerCase());
                if (result < 0) v = -1;
                else if (result > 0) v = 1;
                return v;
            }
        };
        Collections.sort(enumerations, c);
    }
    
    /**
     * Starts an asynchronous call to load the photo of the currently
     * selected user.
     */
    private void fireExperimenterPhotoLoading()
    {
    	if (refObject instanceof ExperimenterData) {
    		ExperimenterData exp = (ExperimenterData) refObject;
    		if (usersPhoto == null || !usersPhoto.containsKey(exp.getId())) {
    			UserPhotoLoader loader = new UserPhotoLoader(component,
    					parent.getSecurityContext(), exp);
        		loader.load();
    		}
    	}
    }
    
	/**
	 * Returns the group corresponding to the specified id or <code>null</code>.
	 * 
	 * @param groupId The identifier of the group.
	 * @return See above.
	 */
	private GroupData getGroup(long groupId)
	{
		Collection groups = MetadataViewerAgent.getAvailableUserGroups();
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
	 * Indicates to load all annotations available if the user can annotate
	 * and is an administrator/group owner or to only load the user's
	 * annotation.
	 * 
	 * @return See above
	 */
	private boolean canRetrieveAll()
	{
		if (!canAnnotate()) return false;
		//check the group level
		GroupData group = getGroup(getRefObjectGroupID());
		if (group == null) return false;
		switch (group.getPermissions().getPermissionsLevel()) {
			case GroupData.PERMISSIONS_GROUP_READ:
				if (MetadataViewerAgent.isAdministrator()) return true;
				Set leaders = group.getLeaders();
				Iterator i = leaders.iterator();
				long userID = getCurrentUser().getId();
				ExperimenterData exp;
				while (i.hasNext()) {
					exp = (ExperimenterData) i.next();
					if (exp.getId() == userID)
						return true;
				}
				return false;
			case GroupData.PERMISSIONS_PRIVATE:
				return false;
		}
		return true;
	}

	/**
	 * Returns <code>true</code> if the object can be deleted,
	 * <code>false</code> otherwise.
	 * 
	 * @param data The data to handle.
	 * @param result the object of reference.
	 * @return See above.
	 */
	private boolean canDeleteLink(Object data, StructuredDataResults result)
	{
		if (!(data instanceof DataObject)) return false;
		DataObject d = (DataObject) data;
		if (result == null) return false;
		Collection<AnnotationLinkData> links = result.getAnnotationLinks();
		if (links == null) return false;
		Iterator<AnnotationLinkData> i = links.iterator();
		AnnotationLinkData link;
		
		while (i.hasNext()) {
			link = i.next();
			if (d.getId() == link.getChild().getId())
				return link.canDelete();
		}
		return false;
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param refObject	The object this editor is for.
	 * @param parent	The parent of this browser.
	 */
	EditorModel(Object refObject, MetadataViewer parent) 
	{
		if (refObject == null)
			throw new IllegalArgumentException("No object set.");
		this.parent = parent;
		this.refObject = refObject;
		loaders = new ArrayList<EditorLoader>();
		sorter = new ViewerSorter();
	}
	
	/**
	 * Adds the annotation to the collection of annotation to be deleted.
	 * 
	 * @param annotation The value to add.
	 */
	void deleteAnnotation(AnnotationData annotation)
	{
		if (annotation == null) return;
		if (toDelete == null) toDelete = new ArrayList<AnnotationData>();
		toDelete.add(annotation);
	}
	
	/**
	 * Returns the collection of annotation to delete.
	 * 
	 * @return See above.
	 */
	List<AnnotationData> getAnnotationToDelete() { return toDelete; }
	
	/**
	 * Returns <code>true</code> if multiple selection is on, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isMultiSelection() { return !parent.isSingleMode(); }

	
	/**
	 * Returns the observable.
	 * 
	 * @return See above.
	 */
	ObservableComponent getObservable() { return parent; }
	
	/**
	 * Sets the browser.
	 * 
	 * @param browser The value to set.
	 */
	void setBrowser(Browser browser) { this.browser = browser; }
	
	/**
	 * Returns the browser.
	 * 
	 * @return See above.
	 */
	Browser getBrowser() { return browser; }
	
	/**
	 * Called by the <code>Editor</code> after creation to allow this
	 * object to store a back reference to the embedding component.
	 * 
	 * @param component The embedding component.
	 */
	void initialize(Editor component) { this.component = component; }
	
	/**
     * Returns <code>true</code> if the permissions can be shown,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
	boolean isPermissionsShowable()
	{
		Object ref = getRefObject();
		if (ref == null) return false;
    	return ((ref instanceof ProjectData) || 
    			(ref instanceof DatasetData));
	}

	/**
     * Returns the permission of the currently edited object. 
     * Returns <code>null</code> if no permission associated. (This should never
     * happens).
     * 
     * @return See above.
     */
	PermissionData getRefObjectPermissions()
	{
		Object ref = getRefObject();
		if (ref instanceof DataObject)
			return ((DataObject) ref).getPermissions();
		return null;
	}
	
	/**
	 * Returns the name of the object if any.
	 * 
	 * @return See above.
	 */
	String getRefObjectName() 
	{
		return getObjectName(getPrimarySelect());
	}
	
	/**
	 * Returns the name of the specified object.
	 * 
	 * @param ref The object to handle.
	 * @return See above.
	 */
	String getObjectName(Object ref)
	{
		String name = "";
		if (ref instanceof ImageData)
			name = ((ImageData) ref).getName();
		else if (ref instanceof DatasetData)
			name = ((DatasetData) ref).getName();
		else if (ref instanceof ProjectData)
			name = ((ProjectData) ref).getName();
		else if (ref instanceof TagAnnotationData ||
				ref instanceof TermAnnotationData)
			name = ((AnnotationData) ref).getContentAsString();
		else if (ref instanceof ScreenData)
			name = ((ScreenData) ref).getName();
		else if (ref instanceof PlateData)
			name = ((PlateData) ref).getName();
		else if (ref instanceof PlateAcquisitionData)
			name = ((PlateAcquisitionData) ref).getLabel();
		else if (ref instanceof FileAnnotationData)
			name = ((FileAnnotationData) ref).getFileName();
		else if (ref instanceof WellSampleData) {
			WellSampleData ws = (WellSampleData) ref;
			ImageData img = ws.getImage();
			if (img != null && img.getId() >= 0) name = img.getName();
		} else if (ref instanceof FileData)
			name = ((FileData) ref).getName();
		if (name == null) return "";
		return name.trim();
	}
	
	/**
	 * Returns the name associated to the specified object.
	 * 
	 * @param ref The object to handle.
	 * @return See above.
	 */
	String getObjectTypeAsString(Object ref)
	{
		if (ref instanceof ImageData) return "Image";
        else if (ref instanceof DatasetData) return "Dataset";
        else if (ref instanceof ProjectData) return "Project";
        else if (ref instanceof ScreenData) return "Screen";
        else if (ref instanceof PlateData) return "Plate";
        else if (refObject instanceof PlateAcquisitionData)
        	return"Plate Run";
        else if (refObject instanceof FileAnnotationData) {
        	return "File";
        } else if (refObject instanceof WellSampleData) return "Field";
        else if (refObject instanceof TagAnnotationData) {
        	TagAnnotationData tag = (TagAnnotationData) refObject;
        	if (TagAnnotationData.INSIGHT_TAGSET_NS.equals(tag.getNameSpace()))
        		return "Tag Set";
        	else return "Tag";
        } else if (refObject instanceof FileData) {
        	FileData f = (FileData) refObject;
        	if (f.isDirectory()) return "Folder";
        	return "File";
        } else if (refObject instanceof TermAnnotationData) {
        	return "Term";
        } else if (refObject instanceof XMLAnnotationData) {
        	return "XML file";
        } else if (refObject instanceof LongAnnotationData ||
        		refObject instanceof DoubleAnnotationData) {
        	return "Numerical value";
        }
        else if (refObject instanceof BooleanAnnotationData) {
        	return "Boolean value";
        }
		return "";
	}
	
	/**
	 * Returns the description of the object if any.
	 * 
	 * @return See above.
	 */
	String getRefObjectDescription()
	{
		String description = "";
		Object ref = getPrimarySelect();
		if (ref instanceof ImageData)
			description = ((ImageData) ref).getDescription();
		else if (ref instanceof DatasetData)
			description = ((DatasetData) ref).getDescription();
		else if (ref instanceof ProjectData)
			description = ((ProjectData) ref).getDescription();
		else if (ref instanceof ScreenData)
			description = ((ScreenData) ref).getDescription();
		else if (ref instanceof PlateData)
			description = ((PlateData) ref).getDescription();
		else if (ref instanceof PlateAcquisitionData)
			description = ((PlateAcquisitionData) ref).getDescription();
		else if (ref instanceof TagAnnotationData) {
			description = ((TagAnnotationData) ref).getTagDescription();
		} else if (ref instanceof WellSampleData) {
			if (parentRefObject instanceof WellData) {
				WellData ws = (WellData) parentRefObject;
				description = ws.getWellType();
			}
			ImageData img = ((WellSampleData) ref).getImage();
			if (!CommonsLangUtils.isEmpty(img.getDescription()))
			    description = img.getDescription();
		} else if (ref instanceof FileData) 
			description = null;//((FileData) ref).getDescription();
		if (description == null) return "";
		return description.trim();
	}
	
	/**
	 * Returns the id of the reference object if it is an instance of 
	 * <code>DataObject</code> or <code>-1</code> otherwise.
	 * 
	 * @return See above.
	 */
	long getRefObjectID()
	{
		Object ref = getRefObject();
		if (ref instanceof DataObject)
			return ((DataObject) ref).getId();
		return -1;
	}
	
	/**
	 * Returns the group's id of the reference object if it is an instance of 
	 * <code>DataObject</code> or <code>-1</code> otherwise.
	 * 
	 * @return See above.
	 */
	long getRefObjectGroupID()
	{
		Object ref = getRefObject();
		if (ref instanceof DataObject)
			return ((DataObject) ref).getGroupId();
		return -1;
	}
	
	/**
	 * Returns <code>true</code> if the user currently logged in is
	 * the one currently edited, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isSelf()
	{
		Object ref = getRefObject();
		if (!(ref instanceof ExperimenterData)) return false;
		ExperimenterData exp = getCurrentUser();
		return exp.getId() == getRefObjectID();
	}
	
	/**
	 * Returns the <code>DataObject</code> this editor is for.
	 * 
	 * @return See above.
	 */
	Object getRefObject()
	{ 
		StructuredDataResults data = parent.getStructuredData();
		if (data == null) return refObject;
		return refObject; 
	}
	
	/**
	 * Returns the primary select object.
	 * 
	 * @return See above.
	 */
	Object getPrimarySelect()
	{
		if (!isMultiSelection()) return getRefObject();
		List list = parent.getRelatedNodes();
		if (CollectionUtils.isEmpty(list)) return getRefObject();
		return list.get(0);
	}
	
	/**
	 * Returns <code>true</code> if the object can be edited,
	 * <code>false</code> otherwise.
	 *
	 * @return See above.
	 */
	boolean canEdit() { return canEdit(refObject); }
	
	/**
	 * Returns <code>true</code> if the object can be edited,
	 * <code>false</code> otherwise.
	 * 
	 * @param data The data to handle.
	 * @return See above.
	 */
	boolean canEdit(Object data)
	{
	    if (!(data instanceof DataObject)) return false;
	    DataObject d = (DataObject) data;
	    return d.canEdit();
	}
	
	/**
	 * Returns <code>true</code> if the object can be linked,
	 * <code>false</code> otherwise.
	 *
	 * @return See above.
	 */
	boolean canLink() { return canLink(refObject); }
	
	/**
	 * Returns <code>true</code> if the object can be linked, e.g. 
	 * image added to dataset, <code>false</code> otherwise.
	 * 
	 * @param data The data to handle.
	 * @return See above.
	 */
	boolean canLink(Object data)
	{
		switch (getDisplayMode()) {
			case LookupNames.GROUP_DISPLAY:
				if (data instanceof DataObject)
					return ((DataObject) data).canLink();
				return false;
			case LookupNames.EXPERIMENTER_DISPLAY:
			default:
				return EditorUtil.isUserOwner(data, getUserID());
		}
	}

	/**
	 * Returns <code>true</code> if the object can be annotated,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean canAnnotate() { return canAnnotate(refObject); }
	
	/**
	 * Returns <code>true</code> if the object can be annotated,
	 * <code>false</code> otherwise.
	 * 
	 * @param data The data to handle.
	 * @return See above.
	 */
	boolean canAnnotate(Object data)
	{ 
		if (!(data instanceof DataObject)) return false;
		DataObject d = (DataObject) data;
		return d.canAnnotate();
	}
	
	/**
	 * Returns <code>true</code> if the object can be deleted,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean canDelete() { return canDelete(refObject); }
	
	/**
	 * Returns <code>true</code> if the object can be deleted,
	 * <code>false</code> otherwise.
	 * 
	 * @param data The data to handle.
	 * @return See above.
	 */
	boolean canDelete(Object data)
	{ 
		if (!(data instanceof DataObject)) return false;
		DataObject d = (DataObject) data;
		return d.canDelete();
	}
	
	/**
	 * Returns <code>true</code> if the object can be deleted,
	 * <code>false</code> otherwise.
	 * 
	 * @param data The data to handle.
	 * @return See above.
	 */
	boolean canDeleteLink(Object data)
	{
		Map<DataObject, StructuredDataResults> 
		r = parent.getAllStructuredData();
		if (r == null) return false;
		Entry<DataObject, StructuredDataResults> e;
		Iterator<Entry<DataObject, StructuredDataResults>>
		i = r.entrySet().iterator();
		while (i.hasNext()) {
			e = i.next();
			if (canDeleteLink(data, e.getValue()))
				return true;
		}
		return false;
	}

	/**
	 * Returns <code>true</code> if the object is a modulo annotation,
	 * <code>false</code> otherwise.
	 * 
	 * @param data The data to handle.
	 * @return See above.
	 */
    boolean isModulo(Object data)
    {
        if (!(data instanceof XMLAnnotationData)) return false;
        //parse the annotation.
        XMLAnnotationData d = (XMLAnnotationData) data;
        ModuloParser parser = new ModuloParser(d.getText());
        try {
            parser.parse();
            return !CollectionUtils.isEmpty(parser.getModulos());
        } catch (Exception e) {
            LogMessage msg = new LogMessage();
            msg.append("Error while reading modulo annotation.");
            msg.print(e);
            MetadataViewerAgent.getRegistry().getLogger().error(this, msg);
        }
        return false;
    }
	
	/**
	 * Returns <code>true</code> if the selected objects belong to several
	 * groups, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isAcrossGroups()
	{
		List<DataObject> l = getSelectedObjects();
		if (l == null || l.size() == 0) return false;
		List<Long> ids = new ArrayList<Long>();
		Iterator<DataObject> i = l.iterator();
		DataObject data;
		while (i.hasNext()) {
			data = i.next();
			if (!ids.contains(data.getGroupId())) 
				ids.add(data.getGroupId());
		}
		return ids.size() > 1;
	}
	
	/**
	 * Returns <code>true</code> if the annotation can be added, should
	 * only be invoked for tagging or adding attachments, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	boolean canAddAnnotationLink()
	{
		if (!canAnnotate()) return false;
		if (!isMultiSelection()) return true;
		//multi selection.
		List l = parent.getRelatedNodes();
		if (l == null) return false;
		Iterator i = l.iterator();
		DataObject data;
		List<Long> ids = new ArrayList<Long>();
		while (i.hasNext()) {
			data = (DataObject) i.next();
			if (!(data instanceof GroupData || 
					data instanceof ExperimenterData)) {
				if (!ids.contains(data.getGroupId()))
					ids.add(data.getGroupId());
			}
		}
		return ids.size() <= 1;
	}
	
	/**
	 * Returns <code>true</code> if the annotation can be deleted, should
	 * only be invoked for tagging or adding attachments, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	boolean canDeleteAnnotationLink()
	{
		Map<DataObject, StructuredDataResults> 
		r = parent.getAllStructuredData();
		if (r == null) return false;
		Entry<DataObject, StructuredDataResults> e;
		Iterator<Entry<DataObject, StructuredDataResults>>
		i = r.entrySet().iterator();
		StructuredDataResults data;
		Collection<AnnotationLinkData> links;
		Iterator<AnnotationLinkData> j;
		while (i.hasNext()) {
			e = i.next();
			data = e.getValue();
			links = data.getAnnotationLinks();
			if (links != null) {
				j = links.iterator();
				while (j.hasNext())
					if ( j.next().canDelete()) return true;
			}
		}
		return false;
	}

	/**
	 * Returns <code>true</code> if the currently logged in user is the 
	 * owner of the object, <code>false</code> otherwise.
	 * 
	 * @param object The object to handle.
	 * @return See above.
	 */
	boolean isUserOwner(Object object)
	{
		long id = getUserID();
		if (object == null) return false;
		if (object instanceof ExperimenterData) 
			return (((ExperimenterData) object).getId() == id);
		if (!(object instanceof DataObject)) return false;
		if (object instanceof FileData || object instanceof ImageData) {
			DataObject f = (DataObject) object;
			if (f.getId() < 0) return id == getUserID();
		} 
		return EditorUtil.isUserOwner(object, id);
	}
	
	/**
	 * Returns <code>true</code> if the current user is the owner of the link
	 * between the passed annotation and the currently selected object.
	 * 
	 * @param annotation The annotation to handle.
	 * @return See above.
	 */
	boolean isLinkOwner(Object annotation)
	{
		Map<DataObject, StructuredDataResults> data = getAllStructuredData();
		if (data == null) return false;
		
		Entry<DataObject, StructuredDataResults> e;
		Iterator<Entry<DataObject, StructuredDataResults>> 
		j = data.entrySet().iterator();
		
		Iterator<AnnotationLinkData> i;
		Collection<AnnotationLinkData> links;
		AnnotationLinkData link;
		DataObject ann = (DataObject) annotation;
		
		long id = getUserID();
		
		while (j.hasNext()) {
			e = j.next();
			links = e.getValue().getAnnotationLinks();
			if (links != null) {
				i = links.iterator();
				while (i.hasNext()) {
					link = i.next();
					if (link.getChild().getId() == ann.getId() &&
						link.getOwner().getId() == id) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Returns <code>true</code> if the annotation  has been added by others,
	 * <code>false</code> otherwise.
	 * 
	 * @param data The annotation to handle.
	 * @return See above.
	 */
	boolean isAnnotatedByOther(DataObject data)
	{
		if (data == null) return false;
		
		List<ExperimenterData> annotators = getAnnotators(data);
		if (annotators == null || annotators.size() == 0) return false;
		if (annotators.size() == 1) {
			ExperimenterData exp = annotators.get(0);
			long id = getUserID();
			return exp.getId() != id;
		}
		return true;
	}
	
	/**
	 * Returns the collection of experimenters who use the annotation.
	 * 
	 * @param annotation 	The annotation to handle.
	 * @return See above.
	 */
	List<ExperimenterData> getAnnotators(Object annotation)
	{
		return getAnnotators(null, annotation);
	}
	
	/**
	 * Returns the collection of experimenters who use the annotation.
	 * 
	 * @param ref The object of reference.
	 * @param annotation The annotation to handle.
	 * @return See above.
	 */
	List<ExperimenterData> getAnnotators(DataObject ref, Object annotation)
	{
		List<ExperimenterData> list = new ArrayList<ExperimenterData>();
		Map<DataObject, StructuredDataResults> data = getAllStructuredData();
		if (data == null) return list;
		
		Entry<DataObject, StructuredDataResults> e;
		Iterator<Entry<DataObject, StructuredDataResults>> 
		j = data.entrySet().iterator();
		
		Iterator<AnnotationLinkData> i;
		Collection<AnnotationLinkData> links;
		AnnotationLinkData link;
		DataObject ann = (DataObject) annotation;
		
		List<Long> ids = new ArrayList<Long>();
		while (j.hasNext()) {
			e = j.next();
			if (ref == null) {
				links = e.getValue().getAnnotationLinks();
				if (links != null) {
					i = links.iterator();
					while (i.hasNext()) {
						link = i.next();
						if (link.getChild().getId() == ann.getId()) {
							
							if (!ids.contains(link.getOwner().getId())) {
								list.add(link.getOwner());
								ids.add(link.getOwner().getId());
							}
						}
					}
				}
			} else {
				if (ref.getId() == e.getKey().getId()) {
					links = e.getValue().getAnnotationLinks();
					if (links != null) {
						i = links.iterator();
						while (i.hasNext()) {
							link = i.next();
							if (link.getChild().getId() == ann.getId()) {
								
								if (!ids.contains(link.getOwner().getId())) {
									list.add(link.getOwner());
									ids.add(link.getOwner().getId());
								}
							}
						}
					}
					break;
				}
			}
			
		}
		return list;
	}
	
	/**
	 * Returns the links corresponding to the level and the annotation.
	 * 
	 * @param level The level to handle.
	 * @param ho The annotation.
	 * @return
	 */
	List<Object> getLinks(int level, AnnotationData ho)
	{
		Map<DataObject, StructuredDataResults> data = getAllStructuredData();
		if (data == null) return null;
		
		Entry<DataObject, StructuredDataResults> e;
		Iterator<Entry<DataObject, StructuredDataResults>> 
		j = data.entrySet().iterator();
		
		Iterator<AnnotationLinkData> i;
		AnnotationLinkData d;
		List<Object> results = new ArrayList<Object>();
		long userID = getCurrentUser().getId();
		Collection<AnnotationLinkData> links;
		while (j.hasNext()) {
			e = j.next();
			links = e.getValue().getAnnotationLinks();
			if (links != null) {
				i = links.iterator();
				switch (level) {
					case ALL:
						while (i.hasNext()) {
							d = i.next();
							if (ho.getId() == d.getChild().getId()) {
								results.add(d.getLink());
							}
						}
						break;
					case ME:
						while (i.hasNext()) {
							d = i.next();
							if (ho.getId() == d.getChild().getId() &&
									userID == d.getOwner().getId()) {
								results.add(d.getLink());
							}
						}
						break;
					case OTHER:
						while (i.hasNext()) {
							d = i.next();
							if (ho.getId() == d.getChild().getId() &&
									userID != d.getOwner().getId()) {
								results.add(d.getLink());
							}
						}
				}
			}
			
		}
		return results;
	}
	
	/**
	 * Returns the FileAnnotations corresponding to the given level.
	 * 
	 * @param level The level to handle.
	 * @return See above.
	 */
        List<FileAnnotationData> getFileAnnotatationsByLevel(int level) {
            List<FileAnnotationData> result = new ArrayList<FileAnnotationData>();
            Collection<FileAnnotationData> all = getAllAttachments();
            for (FileAnnotationData f : all) {
                switch (level) {
                    case ALL:
                        result.add(f);
                        break;
                    case ME:
                        if (MetadataViewerAgent.getUserDetails().getId() == f.getOwner().getId()) {
                            result.add(f);
                        }
                        break;
                    case OTHER:
                        if (MetadataViewerAgent.getUserDetails().getId() != f.getOwner().getId()) {
                            result.add(f);
                        }
                }
            }
            return result;
        }

	/**
	 * Returns <code>true</code> if the annotation is already used by the 
	 * current user, <code>false</code> otherwise.
	 * 
	 * @param annotation The annotation to handle.
	 * @return See above.
	 */
	boolean isAnnotationUsedByUser(Object annotation)
	{
		Map<DataObject, StructuredDataResults> data = getAllStructuredData();
		if (data == null) return false;
		Entry<DataObject, StructuredDataResults> e;
		Iterator<Entry<DataObject, StructuredDataResults>> 
		j = data.entrySet().iterator();
		Collection<AnnotationLinkData> links;
		Iterator<AnnotationLinkData> i;
		AnnotationLinkData link;
		
		long id = MetadataViewerAgent.getUserDetails().getId();
		DataObject ann = (DataObject) annotation;
		
		while (j.hasNext()) {
			e = j.next();
			links = e.getValue().getAnnotationLinks();
			i = links.iterator();
			while (i.hasNext()) {
				link = i.next();
				if (link.getChild().getId() == ann.getId() &&
					link.getOwner().getId() == id)
					return true;
			}
			
		}
		return false;
	}
	
	/**
	 * Returns <code>true</code> if the user currently logged in, is a leader
	 * of the selected group, <code>false</code> otherwise.
	 * 
	 * @return
	 */
	boolean isAdministrator()
	{
		return MetadataViewerAgent.isAdministrator();
	}

    /**
     * Returns <code>true</code> if the user currently logged in, is a leader
     * of their default group, <code>false</code> otherwise.
     */
    boolean isGroupLeader()
    {
        return isGroupLeader(MetadataViewerAgent.getUserDetails().getDefaultGroup());
    }

	/**
	 * Returns <code>true</code> if the user currently logged in, is a leader
	 * of the given group, <code>false</code> otherwise.
	 */
	boolean isGroupLeader(GroupData group)
	{
		ExperimenterData exp = MetadataViewerAgent.getUserDetails();
		Collection groups = MetadataViewerAgent.getAvailableUserGroups();
		if (groups == null) return false;
		final long groupID = group.getId();
		Iterator i = groups.iterator();
		GroupData g;
		Set leaders = null;
		while (i.hasNext()) {
			g = (GroupData) i.next();
			if (g.getId() == groupID) {
				leaders = g.getLeaders();
				break;
			}
		}
		if (leaders == null) return false;
		i = leaders.iterator();
		ExperimenterData data;
		while (i.hasNext()) {
			data = (ExperimenterData) i.next();
			if (data.getId() == exp.getId()) return true;
		}
		return false;
	}
	
	/**
	 * Returns the first name and the last name of the owner.
	 * 
	 * @param object The object to handle.
	 * @return See above.
	 */
	String formatOwner(DataObject object)
	{
		if (object == null) return "";
		if (object instanceof ExperimenterData) {
			return EditorUtil.formatExperimenter((ExperimenterData) object);
		}
		return formatOwner(object.getOwner());
	}
	
	/**
	 * Returns the date.
	 * 
	 * @param object The object to handle.
	 * @return See above.
	 */
	String formatDate(DataObject object)
	{ 
		return EditorUtil.formatDate(object);
	}
	
	/**
	 * Returns the number of URLs linked to the <code>DataObject</code>.
	 * 
	 * @return See above.
	 */
	int getTermsCount()
	{ 
		Collection<TermAnnotationData> terms = getTerms();
		if (terms == null) return 0;
		return terms.size();
	}
	
	/**
	 * Returns the collection of the URLs linked to the <code>DataObject</code>.
	 * 
	 * @return See above.
	 */
	Collection<TermAnnotationData> getTerms()
	{ 
		StructuredDataResults data = parent.getStructuredData();
		if (data == null) return null;
		return data.getTerms(); 
	}
	
	/**
	 * Returns the number of tags linked to the <code>DataObject</code>.
	 * 
	 * @return See above.
	 */
	int getTagsCount()
	{
		Collection tags = getTags();
		if (tags == null) return 0;
		return tags.size();
	}
	
	/**
	 * Returns the collection of the tags linked to the <code>DataObject</code>.
	 * 
	 * @return See above.
	 */
	Collection<TagAnnotationData> getTags()
	{ 
		StructuredDataResults data = parent.getStructuredData();
		if (data == null) return new ArrayList<TagAnnotationData>();
		Collection<TagAnnotationData> tags = data.getTags();
		if (tags == null || tags.size() == 0) 
			return new ArrayList<TagAnnotationData>();
		return (Collection<TagAnnotationData>) sorter.sort(tags);
	}
	
	/**
	 * Returns the collection of the tags linked to the
	 * <code>DataObject</code>s.
	 * 
	 * @return See above.
	 */
	Collection<TagAnnotationData> getAllTags()
	{
		Map<DataObject, StructuredDataResults> 
		r = parent.getAllStructuredData();
		if (r == null) return new ArrayList<TagAnnotationData>();
		Entry<DataObject, StructuredDataResults> e;
		Iterator<Entry<DataObject, StructuredDataResults>>
		i = r.entrySet().iterator();
		Collection<TagAnnotationData> tags;
		List<TagAnnotationData> results = new ArrayList<TagAnnotationData>();
		List<Long> ids = new ArrayList<Long>();
		Iterator<TagAnnotationData> j;
		TagAnnotationData tag;
		while (i.hasNext()) {
			e = i.next();
			tags = e.getValue().getTags();
			if (tags != null) {
				j = tags.iterator();
				while (j.hasNext()) {
					tag = j.next();
					if (!ids.contains(tag.getId())) {
						results.add(tag);
						ids.add(tag.getId());
					}
				}
			}
		}
		return (Collection<TagAnnotationData>) sorter.sort(results);
	}
	
	/**
	 * Returns the collection of the tags that are linked to all the selected
	 * objects.
	 * 
	 * @return See above.
	 */
	Collection<TagAnnotationData> getCommonTags()
	{
		Map<DataObject, StructuredDataResults> 
		r = parent.getAllStructuredData();
		if (r == null) return new ArrayList<TagAnnotationData>();
		Entry<DataObject, StructuredDataResults> e;
		Iterator<Entry<DataObject, StructuredDataResults>>
		i = r.entrySet().iterator();
		Collection<TagAnnotationData> tags;
		Map<Long, Integer> 
			ids = new HashMap<Long, Integer>();
		Iterator<TagAnnotationData> j;
		TagAnnotationData tag;
		
		Integer value;
		while (i.hasNext()) {
			e = i.next();
			tags = e.getValue().getTags();
			if (tags != null) {
				j = tags.iterator();
				while (j.hasNext()) {
					tag = j.next();
					value = ids.get(tag.getId());
					if (value != null) {
						value++;
					} else value = 1;
					ids.put(tag.getId(), value);
				}
			}
		}
		
		//Extract the common tags.
		//The number of selected objects.
		List<TagAnnotationData> results = new ArrayList<TagAnnotationData>();
		List<Long> count = new ArrayList<Long>();
		
		int max = r.size();
		i = r.entrySet().iterator();
		while (i.hasNext()) {
			e = i.next();
			tags = e.getValue().getTags();
			if (tags != null) {
				j = tags.iterator();
				while (j.hasNext()) {
					tag = j.next();
					value = ids.get(tag.getId());
					if (value != null &&
						value == max && !count.contains(tag.getId())) {
						results.add(tag);
						count.add(tag.getId());
					}
				}
			}
		}
		
		return (Collection<TagAnnotationData>) sorter.sort(results);
	}
	
	/**
	 * Returns the objects tagged by the specified object.
	 * 
	 * @param refTag The tag of reference.
	 * @return See above.
	 */
	Map<DataObject, Boolean> getTaggedObjects(AnnotationData refTag)
	{
		Map<DataObject, StructuredDataResults> 
		r = parent.getAllStructuredData();
		Map<DataObject, Boolean> m = new HashMap<DataObject, Boolean>();
		if (r == null) return m;
		Entry<DataObject, StructuredDataResults> e;
		Iterator<Entry<DataObject, StructuredDataResults>>
		i = r.entrySet().iterator();
		Collection<TagAnnotationData> tags;
		Iterator<TagAnnotationData> j;
		TagAnnotationData tag;
		DataObject o;
		StructuredDataResults result;
		while (i.hasNext()) {
			e = i.next();
			result = e.getValue();
			tags = result.getTags();
			if (tags != null) {
				j = tags.iterator();
				while (j.hasNext()) {
					tag = j.next();
					if (tag.getId() == refTag.getId()) {
						o = (DataObject) result.getRelatedObject();
						m.put(o, canDeleteLink(tag, result));
						break;
					}
				}
			}
		}
		return m;
	}
	
	/**
	 * Returns the objects linked by the specified object.
	 * 
	 * @param refFile The file of reference.
	 * @return See above.
	 */
	Map<DataObject, Boolean> getObjectsWith(AnnotationData refFile)
	{
		Map<DataObject, StructuredDataResults> 
		r = parent.getAllStructuredData();
		Map<DataObject, Boolean> m = new HashMap<DataObject, Boolean>();
		if (r == null) return m;
		Entry<DataObject, StructuredDataResults> e;
		Iterator<Entry<DataObject, StructuredDataResults>>
		i = r.entrySet().iterator();
		if (refFile instanceof TermAnnotationData) {
			Collection<TermAnnotationData> files;
			Iterator<TermAnnotationData> j;
			TermAnnotationData file;
			DataObject o;
			StructuredDataResults result;
			while (i.hasNext()) {
				e = i.next();
				result = e.getValue();
				files = result.getTerms();
				if (files != null) {
					j = files.iterator();
					while (j.hasNext()) {
						file = j.next();
						if (file.getId() == refFile.getId()) {
							o = (DataObject) result.getRelatedObject();
							m.put(o, canDeleteLink(file, result));
							break;
						}
					}
				}
			}
		} else if (refFile instanceof FileAnnotationData) {
			Collection<FileAnnotationData> files;
			Iterator<FileAnnotationData> j;
			FileAnnotationData file;
			DataObject o;
			StructuredDataResults result;
			while (i.hasNext()) {
				e = i.next();
				result = e.getValue();
				files = result.getAttachments();
				if (files != null) {
					j = files.iterator();
					while (j.hasNext()) {
						file = j.next();
						if (file.getId() == refFile.getId()) {
							o = (DataObject) result.getRelatedObject();
							m.put(o, canDeleteLink(file, result));
							break;
						}
					}
				}
			}
		} else if (refFile instanceof XMLAnnotationData) {
			Collection<XMLAnnotationData> files;
			Iterator<XMLAnnotationData> j;
			XMLAnnotationData file;
			DataObject o;
			StructuredDataResults result;
			while (i.hasNext()) {
				e = i.next();
				result = e.getValue();
				files = result.getXMLAnnotations();
				if (files != null) {
					j = files.iterator();
					while (j.hasNext()) {
						file = j.next();
						if (file.getId() == refFile.getId()) {
							o = (DataObject) result.getRelatedObject();
							m.put(o, canDeleteLink(file, result));
							break;
						}
					}
				}
			}
		}
		return m;
	}
	
	/**
	 * Returns the collection of the files linked to the 
	 * <code>DataObject</code> at import.
	 * 
	 * @return See above.
	 */
	Collection<FileAnnotationData> getCompanionFiles()
	{
		StructuredDataResults data = parent.getStructuredData();
		List<FileAnnotationData> list = new ArrayList<FileAnnotationData>();
		if (data == null) return list;
		Collection<FileAnnotationData> attachments = data.getAttachments();
		if (attachments == null) return list;
		Iterator<FileAnnotationData> i = attachments.iterator();
		FileAnnotationData f;
		String ns;
		while (i.hasNext()) {
			f = i.next();
			ns = f.getNameSpace();
			if (FileAnnotationData.COMPANION_FILE_NS.equals(ns) && 
					f != originalMetadata) {
				list.add(f);
			}
		}
		if (refObject instanceof WellSampleData) {
			data = parent.getParentStructuredData();
			if (data != null) {
				attachments = data.getAttachments();
				if (attachments != null) {
					while (i.hasNext()) {
						f = i.next();
						ns = f.getNameSpace();
						if (FileAnnotationData.COMPANION_FILE_NS.equals(ns) && 
								f != originalMetadata) {
							list.add(f);
						}
					}
				}
			}
		}
		return (Collection<FileAnnotationData>) sorter.sort(list); 
	}
	
	/**
	 * Returns the collection of the attachments linked to the 
	 * <code>DataObject</code>.
	 * 
	 * @return See above.
	 */
	Collection<FileAnnotationData> getAttachments()
	{ 
		StructuredDataResults data = parent.getStructuredData();
		List<FileAnnotationData> l = new ArrayList<FileAnnotationData>();
		if (data == null) return l;
		Collection<FileAnnotationData> attachments = data.getAttachments(); 
		if (attachments == null) return l;
		Iterator<FileAnnotationData> i = attachments.iterator();
		FileAnnotationData f;
		String ns;
		while (i.hasNext()) {
			f = i.next();
			ns = f.getNameSpace();
			if (FileAnnotationData.COMPANION_FILE_NS.equals(ns)) {
				//tmp
				String name = f.getFileName();
				if (name.contains(FileAnnotationData.ORIGINAL_METADATA_NAME))
					originalMetadata = f;
			} else if (!isNameSpaceExcluded(ns)) {
				l.add(f);
			}
			
		}
		if (getRefObject() instanceof WellSampleData) {
			data = parent.getParentStructuredData();
			if (data != null) {
				attachments = data.getAttachments();
				if (attachments != null) {
					i = attachments.iterator();
					while (i.hasNext()) {
						f = i.next();
						ns = f.getNameSpace();
						if (FileAnnotationData.COMPANION_FILE_NS.equals(ns)) {
							String name = f.getFileName();
							if (name.contains(
									FileAnnotationData.ORIGINAL_METADATA_NAME))
								originalMetadata = f;
						}
					}
				}
			}
		}
		return (Collection<FileAnnotationData>) sorter.sort(l);
	}

	/**
	 * Returns the collection of the attachments linked to the 
	 * <code>DataObject</code>.
	 * 
	 * @return See above.
	 */
	Collection<FileAnnotationData> getAllAttachments()
	{
		Map<DataObject, StructuredDataResults> 
		r = parent.getAllStructuredData();
		if (r == null) return new ArrayList<FileAnnotationData>();
		Entry<DataObject, StructuredDataResults> e;
		Iterator<Entry<DataObject, StructuredDataResults>>
		i = r.entrySet().iterator();
		
		
		Collection<FileAnnotationData> files;
		List<FileAnnotationData> results = new ArrayList<FileAnnotationData>();
		List<Long> ids = new ArrayList<Long>();
		Iterator<FileAnnotationData> j;
		FileAnnotationData file;
		String ns;
		while (i.hasNext()) {
			e = i.next();
			files = e.getValue().getAttachments();
			if (files != null) {
				j = files.iterator();
				while (j.hasNext()) {
					file = j.next();
					ns = file.getNameSpace();
					if (!isNameSpaceExcluded(ns)) {
						if (!ids.contains(file.getId())) {
							results.add(file);
							ids.add(file.getId());
						}
					}
				}
			}
		}
		return (Collection<FileAnnotationData>) sorter.sort(results);
	}
	
	/**
	 * Returns the collection of the files that are linked to all the selected
	 * objects.
	 * 
	 * @return See above.
	 */
	Collection<FileAnnotationData> getCommonAttachments()
	{
		Map<DataObject, StructuredDataResults> 
		r = parent.getAllStructuredData();
		if (r == null) return new ArrayList<FileAnnotationData>();
		Entry<DataObject, StructuredDataResults> e;
		Iterator<Entry<DataObject, StructuredDataResults>>
		i = r.entrySet().iterator();
		Collection<FileAnnotationData> tags;
		Map<Long, Integer> 
			ids = new HashMap<Long, Integer>();
		Iterator<FileAnnotationData> j;
		FileAnnotationData tag;
		
		Integer value;
		String ns;
		while (i.hasNext()) {
			e = i.next();
			tags = e.getValue().getAttachments();
			if (tags != null) {
				j = tags.iterator();
				while (j.hasNext()) {
					tag = j.next();
					ns = tag.getNameSpace();
					if (!isNameSpaceExcluded(ns)) {
						value = ids.get(tag.getId());
						if (value != null) {
							value++;
						} else value = 1;
						ids.put(tag.getId(), value);
					}
					
				}
			}
		}
		
		//Extract the common tags.
		//The number of selected objects.
		List<FileAnnotationData> results = new ArrayList<FileAnnotationData>();
		List<Long> count = new ArrayList<Long>();
		
		int max = r.size();
		i = r.entrySet().iterator();
		while (i.hasNext()) {
			e = i.next();
			tags = e.getValue().getAttachments();
			if (tags != null) {
				j = tags.iterator();
				while (j.hasNext()) {
					tag = j.next();
					value = ids.get(tag.getId());
					if (value != null && 
							value == max && !count.contains(tag.getId())) {
						results.add(tag);
						count.add(tag.getId());
					}
				}
			}
		}
		
		return (Collection<FileAnnotationData>) sorter.sort(results);
	}
	
	/**
	 * Returns the collection of others annotations like Term, XML
	 * 
	 * @return See above.
	 */
	Collection<AnnotationData> getOtherAnnotations()
	{
		StructuredDataResults data = parent.getStructuredData();
		List<AnnotationData> l = new ArrayList<AnnotationData>();
		if (data == null) return l;
		Collection<XMLAnnotationData> xml = data.getXMLAnnotations();
		if (xml != null && !xml.isEmpty())
			l.addAll(xml);
		Collection<AnnotationData> others = data.getOtherAnnotations();
		if (others != null && !others.isEmpty())
			l.addAll(others);
		return l;
	}
	
	/**
	 * Returns the collection of map annotations.
	 * 
	 * @param type The kind of map annotations to return, see {@link MapAnnotationType}
	 * @return See above.
	 */
	List<MapAnnotationData> getMapAnnotations(MapAnnotationType type) {
		StructuredDataResults data = parent.getStructuredData();
		if (data == null)
			return Collections.emptyList();

		List<MapAnnotationData> result = new ArrayList<MapAnnotationData>();

		Collection<MapAnnotationData> maps = data.getMapAnnotations();
		if (!CollectionUtils.isEmpty(maps)) {
			for (MapAnnotationData d : maps) {
				if ((type == MapAnnotationType.USER || type == MapAnnotationType.OTHER_USERS ) && MapAnnotationData.NS_CLIENT_CREATED.equals(d.getNameSpace())) {
					if (type == MapAnnotationType.USER && MetadataViewerAgent.getUserDetails().getId() == d
							.getOwner().getId())
						result.add(d);
					else if(type == MapAnnotationType.OTHER_USERS && MetadataViewerAgent.getUserDetails().getId() != d
							.getOwner().getId())
						result.add(d);
				} else if (type == MapAnnotationType.OTHER && !MapAnnotationData.NS_CLIENT_CREATED.equals(d.getNameSpace())){
					result.add(d);
				}
			}
		}
		
		// Just to make sure, to always get the same order
		Comparator<MapAnnotationData> comp = new Comparator<MapAnnotationData>() {
			@Override
			public int compare(MapAnnotationData o1, MapAnnotationData o2) {
				if(o1.getId()<o2.getId())
					return -1;
				else
					return 1;
			}
		};
		Collections.sort(result, comp);
		return result;
	}
	
	/**
	 * Returns the collection of the other annotations linked to the 
	 * <code>DataObject</code>.
	 * 
	 * @return See above.
	 */
	Collection<AnnotationData> getAllOtherAnnotations()
	{
		Map<DataObject, StructuredDataResults> 
		r = parent.getAllStructuredData();
		if (r == null) return new ArrayList<AnnotationData>();
		Entry<DataObject, StructuredDataResults> e;
		Iterator<Entry<DataObject, StructuredDataResults>>
		i = r.entrySet().iterator();

		Collection<XMLAnnotationData> files;
		Collection<AnnotationData> others;
		List<AnnotationData> results = new ArrayList<AnnotationData>();
		List<Long> ids = new ArrayList<Long>();
		Iterator<XMLAnnotationData> j;
		Iterator<AnnotationData> k;
		XMLAnnotationData file;
		AnnotationData other;
		while (i.hasNext()) {
			e = i.next();
			files = e.getValue().getXMLAnnotations();
			if (files != null) {
				j = files.iterator();
				while (j.hasNext()) {
					file = j.next();
					if (!ids.contains(file.getId())) {
						results.add(file);
						ids.add(file.getId());
					}
				}
			}
			others = e.getValue().getOtherAnnotations();
			if (others != null) {
				k = others.iterator();
				while (k.hasNext()) {
					other = k.next();
					if (!ids.contains(other.getId())) {
						results.add(other);
						ids.add(other.getId());
					}
				}
			}
		}
		return (Collection<AnnotationData>) sorter.sort(results);
	}

	/**
	 * Returns the modulo information if any associated to a given image.
	 *
	 * @return See above.
	 */
	Map<Integer, ModuloInfo> getModulo()
	{
	    Collection<XMLAnnotationData> annotations = getXMLAnnotations();
	    Map<Integer, ModuloInfo> modulo = new HashMap<Integer, ModuloInfo>();
        if (CollectionUtils.isEmpty(annotations)) return modulo;
        ModuloParser parser;
        Iterator<XMLAnnotationData> i = annotations.iterator();
        XMLAnnotationData data;
        List<ModuloInfo> infos;
        Iterator<ModuloInfo> j;
        ModuloInfo info;
        while (i.hasNext()) {
            data = i.next();
            parser = new ModuloParser(data.getText());
            try {
                parser.parse();
                infos = parser.getModulos();
                j = infos.iterator();
                while (j.hasNext()) {
                   info = j.next();
                    modulo.put(info.getModuloIndex(), info);
                }
            } catch (Exception e) {
                LogMessage msg = new LogMessage();
                msg.append("Error while reading modulo annotation.");
                msg.print(e);
                MetadataViewerAgent.getRegistry().getLogger().error(this, msg);
            }
        }
        return modulo;
	}

	/**
     * Returns the collection of XML annotations linked to the 
     * <code>DataObject</code>.
     * 
     * @return See above.
     */
    Collection<XMLAnnotationData> getXMLAnnotations()
    {
        Map<DataObject, StructuredDataResults>
        r = parent.getAllStructuredData();
        if (r == null) return new ArrayList<XMLAnnotationData>();
        Entry<DataObject, StructuredDataResults> e;
        Iterator<Entry<DataObject, StructuredDataResults>>
        i = r.entrySet().iterator();

        Collection<XMLAnnotationData> files;
        List<AnnotationData> results = new ArrayList<AnnotationData>();
        List<Long> ids = new ArrayList<Long>();
        Iterator<XMLAnnotationData> j;
        XMLAnnotationData file;
        while (i.hasNext()) {
            e = i.next();
            files = e.getValue().getXMLAnnotations();
            if (files != null) {
                j = files.iterator();
                while (j.hasNext()) {
                    file = j.next();
                    if (!ids.contains(file.getId())) {
                        results.add(file);
                        ids.add(file.getId());
                    }
                }
            }
        }
        return (Collection<XMLAnnotationData>) sorter.sort(results);
    }

	/**
	 * Returns the collection of the other annotations that are linked to all
	 * the selected objects.
	 * 
	 * @return See above.
	 */
	Collection<AnnotationData> getCommonOtherAnnotations()
	{
		Map<DataObject, StructuredDataResults> 
		r = parent.getAllStructuredData();
		if (r == null) return new ArrayList<AnnotationData>();
		Entry<DataObject, StructuredDataResults> e;
		Iterator<Entry<DataObject, StructuredDataResults>>
		i = r.entrySet().iterator();
		Collection<XMLAnnotationData> tags;
		Collection<AnnotationData> others;
		Map<Long, Integer> 
			ids = new HashMap<Long, Integer>();
		Iterator<XMLAnnotationData> j;
		XMLAnnotationData tag;
		Iterator<AnnotationData> k;
		AnnotationData other;
		Integer value;
		String ns;
		while (i.hasNext()) {
			e = i.next();
			tags = e.getValue().getXMLAnnotations();
			if (tags != null) {
				j = tags.iterator();
				while (j.hasNext()) {
					tag = j.next();
					value = ids.get(tag.getId());
					if (value != null) {
						value++;
					} else value = 1;
					ids.put(tag.getId(), value);
				}
			}
			others = e.getValue().getOtherAnnotations();
			if (others != null) {
				k = others.iterator();
				while (k.hasNext()) {
					other = k.next();
					value = ids.get(other.getId());
					if (value != null) {
						value++;
					} else value = 1;
					ids.put(other.getId(), value);
				}
			}
		}
		
		//The number of selected objects.
		List<AnnotationData> results = new ArrayList<AnnotationData>();
		List<Long> count = new ArrayList<Long>();
		
		int max = r.size();
		i = r.entrySet().iterator();
		while (i.hasNext()) {
			e = i.next();
			tags = e.getValue().getXMLAnnotations();
			if (tags != null) {
				j = tags.iterator();
				while (j.hasNext()) {
					tag = j.next();
					value = ids.get(tag.getId());
					if (value != null && 
							value == max && !count.contains(tag.getId())) {
						results.add(tag);
						count.add(tag.getId());
					}
				}
			}
			others = e.getValue().getOtherAnnotations();
			if (others != null) {
				k = others.iterator();
				while (k.hasNext()) {
					other = k.next();
					value = ids.get(other.getId());
					if (value != null && 
							value == max && !count.contains(other.getId())) {
						results.add(other);
						count.add(other.getId());
					}
				}
			}
		}
		
		return (Collection<AnnotationData>) sorter.sort(results);
	}
	
	/** 
	 * Returns the objects displaying analysis results.
	 * 
	 * @return See above.
	 */
	List<AnalysisResultsItem> getAnalysisResults()
	{
		Map<DataObject, StructuredDataResults> 
		r = parent.getAllStructuredData();
		if (r == null) return null;
		Entry<DataObject, StructuredDataResults> e;
		Iterator<Entry<DataObject, StructuredDataResults>>
		j = r.entrySet().iterator();
		StructuredDataResults data;
		Collection<FileAnnotationData> attachments;
		Iterator<FileAnnotationData> i;
		FileAnnotationData f;
		String ns;
		AnalysisResultsItem item;
		
		Map<Long, FileAnnotationData> 
		ids = new HashMap<Long, FileAnnotationData>();
		
		while (j.hasNext()) {
			e = j.next();
			data = e.getValue();
			if (data != null) {
				attachments = data.getAttachments();
				if (attachments != null) {
					i = attachments.iterator();
					while (i.hasNext()) {
						f = i.next();
						ns = f.getNameSpace();
						if (FileAnnotationData.FLIM_NS.equals(ns)) {
							ids.put(f.getId(), f);
						}
					}
				}
			}
		}

		if (ids.size() == 0) return null;
		List<Long> orderedIds =  (List<Long>) sorter.sort(ids.keySet());
		
		int index = 0; //this should be modified.
		Iterator<Long> k = orderedIds.iterator();
		Long id;
		List<AnalysisResultsItem> 
		results = new ArrayList<AnalysisResultsItem>();
		item = null;
		int n = 6;
		int number = 1;
		while (k.hasNext()) {
			id = k.next();
			if (index == 0) {
				item = new AnalysisResultsItem((DataObject) getRefObject(), 
						FileAnnotationData.FLIM_NS, number);
				results.add(item);
				number++;
			} else if (index == n) {
				index = -1;
			}
			item.addAttachment(ids.get(id));
			index++;
		}
		return results;
	}
	
	/**
	 * Returns the companion file generated while importing the file
	 * and containing the metadata found in the file, or <code>null</code>
	 * if no file was generated.
	 * 
	 * @return See above
	 */
	FileAnnotationData getOriginalMetadata() { return originalMetadata; }
	
	/**
	 * Returns the number of people who viewed the image.
	 * 
	 * @return See above.
	 */
	int getViewedByCount()
	{
		Collection viewedBy = getViewedBy();
		if (viewedBy == null) return 0;
		return viewedBy.size();
	}
	
	/**
	 * Returns the collection of people who viewed the image.
	 * 
	 * @return See above.
	 */
	Collection getViewedBy()
	{ 
		/*
		StructuredDataResults data = parent.getStructuredData();
		if (data == null) return null;
		Collection l = data.getViewedBy(); 
		if (l == null) return null;
		Iterator i = l.iterator();
		ViewedByDef def;
		long userID = MetadataViewerAgent.getUserDetails().getId();
		List<ViewedByDef> results = new ArrayList<ViewedByDef>();
		Map<String, ViewedByDef> 
			m = new HashMap<String, ViewedByDef>();
		String value;
		List<String> names = new ArrayList<String>();
		ExperimenterData exp;
		while (i.hasNext()) {
			def = (ViewedByDef) i.next();
			exp = def.getExperimenter();
			if (exp.getId() != userID) {
				value = formatOwner(exp);
				names.add(value);
				m.put(value, def);
				def.setFormattedExperimenter(value);
			}
		}
		l = sorter.sort(names);
		i = l.iterator();
		while (i.hasNext()) {
			value = (String) i.next();
			results.add(m.get(value));
		}
		return results; 
		*/
		return null;
	}

	/**
	 * Returns the number of ratings for that object.
	 * 
	 * @param filter One of the filtering components defined by this class.
	 * @return See above.
	 */
	int getRatingCount(int filter)
	{
		Map<DataObject, StructuredDataResults> 
		data = parent.getAllStructuredData();
		if (data == null) return 0;
		Entry<DataObject, StructuredDataResults> e;
		Iterator<Entry<DataObject, StructuredDataResults>> 
		i = data.entrySet().iterator();
		Collection<RatingAnnotationData> ratings;
		StructuredDataResults results;
		Iterator<RatingAnnotationData> j;
		int n = 0;
		long userID = getUserID();
		while (i.hasNext()) {
			e = i.next();
			results = e.getValue();
			ratings = results.getRatings();
			if (ratings != null) {
				
				switch (filter) {
				case ALL:
					n += ratings.size();
					break;
				case ME:
					j = ratings.iterator();
					while (j.hasNext()) {
						if (j.next().getOwner().getId() == userID)
							n++;
					}
				case OTHER:
				default:
					j = ratings.iterator();
					while (j.hasNext()) {
						if (j.next().getOwner().getId() != userID)
							n++;
					}
				}
			}
		}
		return n;
	}
	
	/**
	 * Returns the rating annotation related to the logged in user,
	 * or <code>null</code> if no annotation.
	 * 
	 * @return See above.
	 */
	RatingAnnotationData getUserRatingAnnotation()
	{
		StructuredDataResults data = parent.getStructuredData();
		if (data == null) return null;
		Collection<RatingAnnotationData> ratings = data.getRatings();
		if (CollectionUtils.isEmpty(ratings)) return null;
		Iterator<RatingAnnotationData> i = ratings.iterator();
		RatingAnnotationData rate;
		long id = getCurrentUser().getId();
		while (i.hasNext()) {
			rate = i.next();
			if (rate.getOwner().getId() == id)
				return rate;
		}
		return null;
	}
	
	/**
	 * Returns the rating annotation related to the logged in user,
	 * or <code>null</code> if no annotation.
	 * 
	 * @return See above.
	 */
	Map<DataObject, RatingAnnotationData> getAllUserRatingAnnotation()
	{
		Map<DataObject, StructuredDataResults> 
		data = parent.getAllStructuredData();
		if (data == null) return null;
		Entry<DataObject, StructuredDataResults> e;
		Iterator<Entry<DataObject, StructuredDataResults>> 
		i = data.entrySet().iterator();
		Collection<RatingAnnotationData> ratings;
		StructuredDataResults results;
		Iterator<RatingAnnotationData> j;
		RatingAnnotationData rating;
		Map<DataObject, RatingAnnotationData> 
		map = new HashMap<DataObject, RatingAnnotationData>();
		long id = getUserID();
		while (i.hasNext()) {
			e = i.next();
			results = e.getValue();
			if (results != null) {
				ratings = results.getRatings();
				if (ratings != null) {
					j = ratings.iterator();
					while (j.hasNext()) {
						rating = j.next();
						if (rating.getOwner().getId() == id) {
							map.put(e.getKey(), rating);
						}
					}
				}
			}
		}
		
		return map;
	}
	
	/**
	 * Returns the published annotation.
	 * 
	 * @return See above.
	 */
	BooleanAnnotationData getPublishedAnnotation()
	{
		StructuredDataResults data = parent.getStructuredData();
		if (data == null) return null;
		return null;
	}
	
	/**
	 * Returns the rating done by the current user.
	 * 
	 * @return See above
	 */
	int getUserRating()
	{
		RatingAnnotationData data  = getUserRatingAnnotation();
		if (data == null) return 0;
		return data.getRating();
	}
	
	/**
	 * Returns the rating done by the current user.
	 * 
	 * @return See above
	 */
	int getAllUserRating()
	{
		Map<DataObject, RatingAnnotationData>
		map = getAllUserRatingAnnotation();
		if (map == null) return 0;
		Collection<RatingAnnotationData> ratings = map.values();
		Iterator<RatingAnnotationData> i = ratings.iterator();
		int n = 0;
		while (i.hasNext()) {
			n += i.next().getRating();
		}
		return n;
	}
	
	/** 
	 * Returns the average rating value.
	 * 
	 * @param filter One of the filtering components defined by this class.
	 * @return See above.
	 */
	int getRatingAverage(int filter) 
	{
		Map<DataObject, StructuredDataResults> 
		data = parent.getAllStructuredData();
		if (data == null) return 0;
		Entry<DataObject, StructuredDataResults> e;
		Iterator<Entry<DataObject, StructuredDataResults>> 
		i = data.entrySet().iterator();
		Collection<RatingAnnotationData> ratings;
		StructuredDataResults results;
		Iterator<RatingAnnotationData> j;
		RatingAnnotationData rating;
		int n = 0;
		int value = 0;
		long userID = getUserID();
		while (i.hasNext()) {
			e = i.next();
			results = e.getValue();
			ratings = results.getRatings();
			if (ratings != null) {
				
				j = ratings.iterator();
				switch (filter) {
					case ALL:
						while (j.hasNext()) {
							rating = j.next();
							value += rating.getRating();
							n++;
						}
						break;
					case ME:
						
						while (j.hasNext()) {
							rating = j.next();
							if (rating.getOwner().getId() == userID) {
								value += rating.getRating();
								n++;
							}
						}
					case OTHER:
					default:
						j = ratings.iterator();
						while (j.hasNext()) {
							rating = j.next();
							if (rating.getOwner().getId() != userID) {
								value += rating.getRating();
								n++;
							}
						}
				}
			}
		}
		if (n == 0) return 0;
		return value/n;
	}
	
	/**
	 * Returns the number of textual annotations for that object.
	 * 
	 * @return See above.
	 */
	int getTextualAnnotationCount()
	{
		Collection<TextualAnnotationData> annotations = getTextualAnnotations();
		if (annotations == null) return 0;
		return annotations.size();
	}
	
	/**
	 * Returns the collection of textual annotations.
	 * 
	 * @return See above.
	 */
	Collection<TextualAnnotationData> getTextualAnnotations()
	{
		StructuredDataResults data = parent.getStructuredData();
		if (data == null) return null;
		return data.getTextualAnnotations();
	}
	
	/**
	 * Returns the annotations ordered by date.
	 * 
	 * @return See above.
	 */
	List getTextualAnnotationsByDate()
	{
		if (textualAnnotationsByDate != null)
			return textualAnnotationsByDate;
		textualAnnotationsByDate = (List) getTextualAnnotations();
		sortAnnotationByDate(textualAnnotationsByDate);
		return textualAnnotationsByDate;
	}
	
	/**
	 * Returns the annotation made by the currently selected user.
	 * 
	 * @return See above.
	 */
	TextualAnnotationData getLastUserAnnotation()
	{
		Map<Long, List> m = getTextualAnnotationByOwner();
		long userID = MetadataViewerAgent.getUserDetails().getId();
		List l = m.get(userID);
		if (l == null || l.size() == 0) return null;
		return (TextualAnnotationData) l.get(0);
	}
	
	/**
	 * Returns the annotations organized by users.
	 * 
	 * @return See above.
	 */
	Map<Long, List> getTextualAnnotationByOwner()
	{
		if (textualAnnotationsByUsers != null 
			&& textualAnnotationsByUsers.size() > 0)
			return textualAnnotationsByUsers;
		textualAnnotationsByUsers = new HashMap<Long, List>();
		Collection<TextualAnnotationData> original = getTextualAnnotations();
		if (original == null) return textualAnnotationsByUsers;
        Iterator i = original.iterator();
        AnnotationData annotation;
        Long ownerID;
        List<AnnotationData> userAnnos;
        while (i.hasNext()) {
            annotation = (AnnotationData) i.next();
            ownerID = Long.valueOf(annotation.getOwner().getId());
            userAnnos = textualAnnotationsByUsers.get(ownerID);
            if (userAnnos == null) {
                userAnnos = new ArrayList<AnnotationData>();
                textualAnnotationsByUsers.put(ownerID, userAnnos);
            }
            userAnnos.add(annotation);
        }
        i = textualAnnotationsByUsers.keySet().iterator();
        
        while (i.hasNext()) {
            ownerID = (Long) i.next();
            sortAnnotationByDate(textualAnnotationsByUsers.get(ownerID));
        }
        //sort users by name.
        /* REMOVE Comments when user is not root
        Map groups = (Map) MetadataViewerAgent.getRegistry().lookup(
								LookupNames.USER_GROUP_DETAILS);
        GroupData g;
        i = groups.keySet().iterator();
        ViewerSorter sorter = new ViewerSorter();
        List orderedUsers = new ArrayList();
		while (i.hasNext()) {
			g = (GroupData) i.next();
			orderedUsers.addAll(sorter.sort((Set) groups.get(g)));
		}
		Map<Long, List> orderedAnnotationsByUser = new HashMap<Long, List>();
		i = orderedUsers.iterator();
		ExperimenterData exp;
		List ann;
		while (i.hasNext()) {
			exp = (ExperimenterData) i.next();
			ann = textualAnnotationsByUsers.get(exp.getId());
			if (ann != null)
				orderedAnnotationsByUser.put(exp.getId(), ann);
			
		}
		textualAnnotationsByUsers = orderedAnnotationsByUser;
		*/
        return textualAnnotationsByUsers;
	}

	/**
	 * Returns <code>true</code> if it is the same object,
	 * <code>false</code> otherwise.
	 * 
	 * @param object The object to handle.
	 * @return See above.
	 */
	boolean isSameObject(Object object)
	{
		return parent.isSameObject(object);
	}
	
	/** 
	 * Sets the object of reference.
	 * 
	 * @param refObject The value to set.
	 */
	void setRootObject(Object refObject)
	{ 
		boolean b = isSameObject(this.refObject);
		set = null;
		largeImage = false;
		this.refObject = refObject;
		if (existingTags != null) existingTags.clear();
		existingTags = null;
		if (textualAnnotationsByUsers != null) 
			textualAnnotationsByUsers.clear();
		textualAnnotationsByUsers = null;
		if (textualAnnotationsByDate != null) 
			textualAnnotationsByDate.clear();
		textualAnnotationsByDate = null;
		if (existingAttachments != null) 
			existingAttachments.clear();
	    existingAttachments = null;
	   
	    if (resultsLoader != null) resultsLoader.clear();
	    resultsLoader = null;
	    if (!b) {
			parentRefObject = null;
			gpRefObject = null;
	    	if (emissionsWavelengths != null) 
	    		emissionsWavelengths.clear();
	    	emissionsWavelengths = null;
	    	if (channelAcquisitionDataMap != null)
	    		channelAcquisitionDataMap.clear();
	    	if (channelPlaneInfoMap != null)
	    		channelPlaneInfoMap.clear();
	    	imageAcquisitionData = null;
	    	instrumentData = null;
	    	originalMetadata = null;
	    	if (refObject instanceof ImageData || 
	    			refObject instanceof WellSampleData) {
	    		fireChannelEnumerationsLoading();
	    		fireImageEnumerationsLoading();
	    		fireLargeImageLoading();
	    	} else if (refObject instanceof ExperimenterData) {
	    		fireExperimenterPhotoLoading();
	    	}
	    	if (renderer != null) {
	    		renderer.discard();
	    		renderer = null;
	    	}
	    }
	} 

	/**
	 * Sets the parent of the object of reference.
	 * 
	 * @param parentRefObject The value to set.
	 * @param gpRefObject The value to set.
	 */
	void setParentRootObject(Object parentRefObject, Object gpRefObject)
	{
		this.parentRefObject = parentRefObject;
		this.gpRefObject = gpRefObject;
	}
	
	/**
	 * Returns the parent of the object of reference.
	 * 
	 * @return See above.
	 */
	Object getParentRootObject() { return parentRefObject; }
	
	/**
	 * Returns the parent of the object of reference.
	 * 
	 * @return See above.
	 */
	Object getGrandParentRootObject() { return gpRefObject; }
	
	/**
	 * Returns the owner of the reference object or <code>null</code>
	 * if the object is not a <code>DataObject</code>.
	 * 
	 * @return See above.
	 */
	ExperimenterData getRefObjectOwner()
	{
		Object ref = getRefObject();
		if (ref instanceof DataObject)
			return getOwner((DataObject) ref);
		return null;
	}

	/** Fires an asynchronous retrieval of existing tags. */
	void loadExistingTags()
	{
		EditorLoader l;
		boolean exist = false;
		Iterator i = loaders.iterator();
		while (i.hasNext()) {
			l = (EditorLoader) i.next();
			if (l instanceof TagsLoader) {
				exist = true;
				break;
			}
		}
		if (exist) return;
		TagsLoader loader = new TagsLoader(component,
				parent.getSecurityContext(), canRetrieveAll());
		loader.load();
		loaders.add(loader);
	}
	
	/** 
	 * Fires an asynchronous retrieval of existing attachments 
	 * for the currently logged in user. 
	 */
	void loadExistingAttachments()
	{
		EditorLoader l;
		boolean exist = false;
		Iterator i = loaders.iterator();
		while (i.hasNext()) {
			l = (EditorLoader) i.next();
			if (l instanceof AttachmentsLoader) {
				exist = true;
				break;
			}
		}
		if (exist) return;
		AttachmentsLoader loader = new AttachmentsLoader(component,
				parent.getSecurityContext(), canRetrieveAll());
		loader.load();
		loaders.add(loader);
	}
	
	/** Cancels any ongoing tags retrieval. */
	void cancelExistingTagsLoading()
	{
		Iterator i = loaders.iterator();
		EditorLoader loader;
		List<EditorLoader> toKeep = new ArrayList<EditorLoader>();
		while (i.hasNext()) {
			loader = (EditorLoader) i.next();
			if (loader instanceof TagsLoader) loader.cancel();
			else toKeep.add(loader);
		}
		loaders.clear();
		loaders.addAll(toKeep);
	}
	
	/** Fires an asynchronous retrieval of the channel data. */
	void loadChannelData()
	{
		Object refObject = getRefObject();
		ImageData data = null;
		if (refObject instanceof ImageData)
			data = (ImageData) refObject;
		else if (refObject instanceof WellSampleData) {
			WellSampleData wsd = (WellSampleData) refObject;
			data = wsd.getImage();
			if (data != null && data.getId() < 0) data = null;
		}
		if (data == null) return;
		try {
			PixelsData pixs = data.getDefaultPixels();
			ChannelDataLoader loader = new ChannelDataLoader(component, 
					parent.getSecurityContext(),
					pixs.getId(), parent.getUserID());
			loader.load();
			loaders.add(loader);
		} catch (Exception e) {}
	}

	/** Cancels any ongoing tags retrieval. */
	void cancelChannelDataLoading()
	{
		Iterator i = loaders.iterator();
		EditorLoader loader;
		List<EditorLoader> toKeep = new ArrayList<EditorLoader>();
		while (i.hasNext()) {
			loader = (EditorLoader) i.next();
			if (loader instanceof ChannelDataLoader) loader.cancel();
			else toKeep.add(loader);
		}
		loaders.clear();
		loaders.addAll(toKeep);
	}
	
	/**
	 * Sets the collection of existing tags.
	 * 
	 * @param tags The value to set.
	 */
	void setExistingTags(Collection tags)
	{
		if (tags != null) existingTags = sorter.sort(tags);
		Iterator i = loaders.iterator();
		EditorLoader loader;
		EditorLoader toRemove = null;
		while (i.hasNext()) {
			loader = (EditorLoader) i.next();
			if (loader instanceof TagsLoader) {
				toRemove = loader;
				break;
			}
		}
		if (toRemove != null) loaders.remove(toRemove);
	}
	
	/**
	 * Returns the collection of existing tags.
	 * 
	 * @return See above.
	 */
	Collection getExistingTags() { return existingTags; }
	
	/**
	 * Sets the collection of existing attachments.
	 * 
	 * @param attachments The value to set.
	 */
	void setExistingAttachments(Collection attachments)
	{
		if (attachments != null)
			existingAttachments = sorter.sort(attachments);
		Iterator i = loaders.iterator();
		EditorLoader loader;
		EditorLoader toRemove = null;
		while (i.hasNext()) {
			loader = (EditorLoader) i.next();
			if (loader instanceof AttachmentsLoader) {
				toRemove = loader;
				break;
			}
		}
		if (toRemove != null) loaders.remove(toRemove);
	}
	
	/**
	 * Returns the collection of existing attachments.
	 * 
	 * @return See above.
	 */
	Collection getExistingAttachments() { return existingAttachments; }

	/**
	 * Sets the channel data.
	 * 
	 * @param data The value to set.
	 */
	void setChannelData(Map data)
	{ 
		List l = sorter.sort(data.keySet()); 
		emissionsWavelengths = new LinkedHashMap();
		Iterator i = l.iterator();
		Object channel;
		while (i.hasNext()) {
			channel = i.next();
			emissionsWavelengths.put(channel, data.get(channel));
		}
	}
	
	/**
	 * Returns the channels data related to the image.
	 * 
	 * @return See above.
	 */
	Map getChannelData()
	{ 
		if (getRndIndex() == MetadataViewer.RND_SPECIFIC) {
			if (renderer != null) {
				List<ChannelData> l = renderer.getChannelData();
				Map m = new LinkedHashMap();
				Iterator<ChannelData> i = l.iterator();
				ChannelData data;
				while (i.hasNext()) {
					data = i.next();
					m.put(data, renderer.getChannelColor(data.getIndex()));
				}
				return m;
			}
		}
		return emissionsWavelengths; 
	}

	/**
	 * Starts an asynchronous call to save the annotations.
	 * 
	 * @param object The annotation/link to add or remove.
	 * @param metadata	The metadata to save.
	 * @param asynch 	Pass <code>true</code> to save data asynchronously,
     * 				 	<code>false</code> otherwise.
	 */
	void fireAnnotationSaving(DataToSave object, List<Object> metadata,
			boolean asynch)
	{
		Object ref = getRefObject();
		if (ref instanceof DataObject) {
			DataObject data = (DataObject) ref;
			if (data instanceof WellSampleData) {
				data = ((WellSampleData) ref).getImage();
			}
			List<AnnotationData> list = null;
			if (CollectionUtils.isNotEmpty(toDelete)) {
				list = new ArrayList<AnnotationData>();
				Iterator<AnnotationData> i = toDelete.iterator();
				while (i.hasNext())
					list.add(i.next());
				toDelete.clear();
			}
			parent.saveData(object, list, metadata, data, asynch);
		}
	}
	
	/**
	 * Updates the specified channels. If <code>applyToAll</code> is
	 * <code>true</code>, the channels of all the images in the dataset or
	 * the plate will be updated,
	 * 
	 * @param channels The channels to update.
	 * @param applyToAll Pass <code>true</code> to update the channels of all
	 *                   the images in the dataset or the plate.
	 *                   Pass <code>false</code> to only update the image.
	 */
	void fireChannelSaving(List<ChannelData> channels, boolean applyToAll)
	{
		DataObject object = null;
		if (applyToAll) {
			if (parentRefObject instanceof DatasetData)
				object = (DataObject) parentRefObject;
			else if (gpRefObject instanceof PlateData)
				object = (DataObject) gpRefObject;
			else return;
		} else object = (DataObject) refObject;
		ChannelDataSaver loader = new ChannelDataSaver(component,
				getSecurityContext(), channels, object);
		loader.load();
	}
	
	/**
	 * Starts an asynchronous call to delete the annotations.
	 * 
	 * @param annotations The annotations to delete.
	 */
	void fireAnnotationsDeletion(List<AnnotationData> annotations)
	{
		Object ref = getRefObject();
		if (ref instanceof DataObject) {
			DataObject data = (DataObject) ref;
			if (data instanceof WellSampleData) {
				data = ((WellSampleData) ref).getImage();
			}
			parent.saveData(null, annotations, new ArrayList<Object>(), data,
					true);
		}
	}
	
	/**
	 * Starts an asynchronous call to check the removal of the given FileAnnotations.
	 * 
	 * @param data 	 The annotations to check
	 */
	void fireFileAnnotationRemoveCheck(List<FileAnnotationData> annotations) {
	    FileAnnotationChecker check = new FileAnnotationChecker(component, getSecurityContext(), annotations, getSelectedObjects());
	    check.load();
	}
	
	/**
	 * Starts an asynchronous call to update the experimenter or the group.
	 * 
	 * @param data 	 The experimenter or the group to save.
	 * @param asynch Pass <code>true</code> to save data asynchronously,
     * 				 <code>false</code> otherwise.
	 */
	void fireAdminSaving(Object data, boolean asynch)
	{
		if (data instanceof ExperimenterData || data instanceof AdminObject ||
		    data instanceof GroupData)
			parent.updateAdminObject(data, asynch);
	}
	
	/**
	 * Returns <code>true</code> if the imported set of pixels has been 
	 * archived, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isArchived() { return isArchived(getImage()); }

    /**
     * Returns <code>true</code> if the imported set of pixels has been 
     * archived, <code>false</code> otherwise.
     * 
     * @param ho The object to handle.
     * @return See above.
     */
	boolean isArchived(DataObject ho)
	{
	    ImageData img = null;
        if (ho instanceof ImageData)
            img = (ImageData) ho;
        if (img == null) return false;
        return img.isArchived();
	}

	/** 
	 * Starts an asynchronous loading. 
	 * 
	 * @param file The file where to download the content.
	 * If it is a multi-images file a zip will be created.
	 * @param override Flag indicating to override the existing file if it
     * exists, <code>false</code> otherwise.
	 */
	void download(File file, boolean override)
	{
	    if (refObject instanceof ImageData) {
	        downloadImages(file, override);
	    } else if (refObject instanceof FileAnnotationData) {
	        downloadFiles(file, override);
	    }
	}
	
    /**
     * Starts an asynchronous loading; preserving the original folder structure
     * 
     * @param path
     *            The folder where to download the content.
     * @param override
     *            Flag indicating to override the existing file if it exists,
     *            <code>false</code> otherwise.
     */
    void downloadOriginal(String path, boolean override) {
        if (!(refObject instanceof ImageData))
            return;

        List<ImageData> images = new ArrayList<ImageData>();
        List<DataObject> l = getSelectedObjects();
        if (!CollectionUtils.isEmpty(l)) {
            Iterator<DataObject> i = l.iterator();
            DataObject o;
            List<Long> filesetIds = new ArrayList<Long>();
            long id;
            ImageData image;
            while (i.hasNext()) {
                o = i.next();
                if (isArchived(o)) {
                    image = (ImageData) o;
                    id = image.getFilesetId();
                    if (id < 0)
                        images.add(image);
                    else if (!filesetIds.contains(id)) {
                        images.add(image);
                        filesetIds.add(id);
                    }
                }
            }
        }
        if (!CollectionUtils.isEmpty(images)) {
            DownloadArchivedActivityParam p;
            UserNotifier un = MetadataViewerAgent.getRegistry()
                    .getUserNotifier();
            IconManager icons = IconManager.getInstance();
            Icon icon = icons.getIcon(IconManager.DOWNLOAD_22);
            SecurityContext ctx = getSecurityContext();
            p = new DownloadArchivedActivityParam(new File(path), images, icon);
            p.setOverride(override);
            p.setZip(false);
            p.setKeepOriginalPaths(true);
            un.notifyActivity(ctx, p);
        }
    }

	/** 
	 * Starts an asynchronous call to retrieve disk space information. 
	 * 
	 * @param type 	Either <code>ExperimenterData</code> or
	 * 				<code>GroupData</code>.
	 * @param id 	The identifier of the user or group.
	 */
	void loadDiskSpace(Class type, long id)
	{
		DiskSpaceLoader loader = new DiskSpaceLoader(component,
				parent.getSecurityContext(), type, id);
		loader.load();
		loaders.add(loader);
	}

	/** Cancels the disk space loading. */
	void cancelDiskSpaceLoading()
	{
		Iterator i = loaders.iterator();
		EditorLoader loader;
		List<EditorLoader> toKeep = new ArrayList<EditorLoader>();
		while (i.hasNext()) {
			loader = (EditorLoader) i.next();
			if (loader instanceof DiskSpaceLoader) {
				loader.cancel();
			} else toKeep.add(loader);
		}
		loaders.clear();
		loaders.addAll(toKeep);
	}
	
	/**
	 * Fires an asynchronous call to modify the password.
	 * 
	 * @param old The old password.
	 * @param confirm The new password.
	 */
	void changePassword(String old, String confirm)
	{
		EditorLoader loader = new PasswordEditor(component,
				parent.getSecurityContext(), old, confirm);
		loader.load();
		loaders.add(loader);
	}
	
	/**
	 * Returns <code>true</code> if the object of reference is tag of another
	 * tag, <code>false</code> if it is tag linked to images, without any links
	 * or if it is not a tag.
	 * 
	 * @return See above.
	 */
	boolean hasTagsAsChildren()
	{
		Object ref = getRefObject();
		if (!(ref instanceof TagAnnotationData)) return false;
		TagAnnotationData tag = (TagAnnotationData) ref;
		Set tags = tag.getTags();
		if (tags != null && tags.size() > 0) return true;
		return false;
	}
	
	/**
	 * Returns the owner of the passed annotation.
	 * 
	 * @param data The annotation to handle.
	 * @return  See above.
	 */
	ExperimenterData getOwner(DataObject data)
	{
		if (data == null) return null;
		long id = data.getOwner().getId();
		return MetadataViewerAgent.getExperimenter(id);
	}

	/**
	 * Fires an asynchronous retrieval of containers hosting the currently
	 * edited object. 
	 */
	void loadParents() { parent.loadParents(); }
	
	/** Cancels any ongoing parents retrieval. */
	void cancelParentsLoading() {  }
	
	/**
	 * Returns <code>true</code> if the image has been viewed by other users,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasBeenViewedBy()
	{
		if (!(getRefObject() instanceof ImageData)) return false;
		return getViewedByCount() != 0;
	}
	
	/** Loads the image metadata enumerations. */
	void fireImageEnumerationsLoading()
	{
		EnumerationLoader loader = new EnumerationLoader(component, 
				parent.getSecurityContext(), EnumerationLoader.IMAGE);
		loader.load();
	}
	
	/** Loads the channel metadata enumerations. */
	void fireChannelEnumerationsLoading()
	{
		EnumerationLoader loader = new EnumerationLoader(component, 
				parent.getSecurityContext(), EnumerationLoader.CHANNEL);
		loader.load();
	}
	
	/** Loads the image acquisition data. */
	void fireImagAcquisitionDataLoading()
	{
		Object ref = getRefObject();
		ImageData data = null;
		if (ref instanceof WellSampleData) {
			data = ((WellSampleData) ref).getImage();
		} else if (ref instanceof ImageData) {
			data = (ImageData) ref;
		}
		if (data == null) return;
		AcquisitionDataLoader 
			loader = new AcquisitionDataLoader(component, 
					parent.getSecurityContext(), data); 
		loader.load();
	}
	
	/** 
	 * Loads the image acquisition data. 
	 * 
	 * @param channel The channel to handle.
	 */
	void  fireChannelAcquisitionDataLoading(ChannelData channel)
	{
		AcquisitionDataLoader 
			loader = new AcquisitionDataLoader(component, 
					parent.getSecurityContext(), channel); 
		loader.load();
	}
	
	/**
	 * Starts an asynchronous call to load the instrument linked to the image.
	 * 
	 * @param instrumentID The id of the instrument.
	 */
	void fireInstrumentDataLoading(long instrumentID)
	{
		InstrumentDataLoader loader = new InstrumentDataLoader(component, 
				parent.getSecurityContext(), instrumentID);
		loader.load();
	}
	
	/**
	 * Sets the image acquisition data.
	 * 
	 * @param data The value to set.
	 */
	void setImageAcquisitionData(ImageAcquisitionData data)
	{
		imageAcquisitionData = data;
	}
	
	/**
	 * Returns the image acquisition data.
	 * 
	 * @return See above.
	 */
	ImageAcquisitionData getImageAcquisitionData()
	{ 
		return imageAcquisitionData;
	}

    /**
     * Sets the acquisition data for the specified channel.
     * 
     * @param index The index of the channel.
     * @param data  The value to set.
     */
	void setChannelAcquisitionData(int index, ChannelAcquisitionData data)
	{
		if (channelAcquisitionDataMap == null)
			channelAcquisitionDataMap = new HashMap<Integer, 
										ChannelAcquisitionData>();
		channelAcquisitionDataMap.put(index, data);
	}
	
	/**
	 * Returns the channel acquisition data.
	 * 
	 * @param index The index of the channels.
	 * @return See above.
	 */
	ChannelAcquisitionData getChannelAcquisitionData(int index)
	{ 
		if (channelAcquisitionDataMap == null) return null;
		return channelAcquisitionDataMap.get(index);
	}
	
    /**
     * Sets the collection of plane info for the specified channel.
     * 
     * @param index The index of the channel.
     * @param data  The value to set.
     */
	void setPlaneInfo(int index, Collection data)
	{
		if (channelPlaneInfoMap == null)
			channelPlaneInfoMap = new HashMap<Integer, Collection>();
		channelPlaneInfoMap.put(index, sortPlane(data));
	}
	
	/**
	 * Returns the channel acquisition data.
	 * 
	 * @param index The index of the channel.
	 * @return See above.
	 */
	Collection getChannelPlaneInfo(int index)
	{ 
		if (channelPlaneInfoMap == null) return null;
		return channelPlaneInfoMap.get(index);
	}
	
	/**
	 * Returns the collection of objects corresponding to the passed name.
	 * 
	 * @param name The name of the enumeration.
	 * @return See above.
	 */
	List<EnumerationObject> getImageEnumerations(String name)
	{
		if (imageEnumerations != null)
			return (List<EnumerationObject>) imageEnumerations.get(name);
		return new ArrayList<EnumerationObject>();
	}

	/**
	 * Returns the collection of objects corresponding to the passed name.
	 * 
	 * @param name The name of the enumeration.
	 * @return See above.
	 */
	List<EnumerationObject> getChannelEnumerations(String name)
	{
		if (channelEnumerations != null)
			return (List<EnumerationObject>) channelEnumerations.get(name);
		return new ArrayList<EnumerationObject>();
	}
	
	/**
	 * Returns the enumeration object corresponding to the passed enumeration 
	 * name and value for the image metadata.
	 * 
	 * @param name	The type of enumeration.
	 * @param value	The value to select.
	 * @return See above.
	 */
	Object getImageEnumerationSelected(String name, String value)
	{
		List<EnumerationObject> l = getImageEnumerations(name);
		if (l.size() == 0) return null;
		EnumerationObject o;
		Iterator i = l.iterator();
		String v;
		while (i.hasNext()) {
			o = (EnumerationObject) i.next();
			v = o.getValue();
			v = v.trim();
			if (v.equals(value)) return o;
		}
		return null;
	}
	
	/**
	 * Returns the enumeration object corresponding to the passed enumeration 
	 * name and value for the channel metadata.
	 * 
	 * @param name	The type of enumeration.
	 * @param value	The value to select.
	 * @return See above.
	 */
	Object getChannelEnumerationSelected(String name, String value)
	{
		if (value == null) return null;
		List<EnumerationObject> l = getChannelEnumerations(name);
		if (l.size() == 0) return null;
		EnumerationObject o;
		Iterator i = l.iterator();
		value = value.trim();
		String v;
		while (i.hasNext()) {
			o = (EnumerationObject) i.next();
			v = o.getValue();
			v = v.trim();
			if (v.equals(value)) return o;
		}
		return null;
	}
	
	/**
	 * Sets the enumerations related to channel metadata.
	 * 
	 * @param enumerations The value to set.
	 */
	void setChannelEnumerations(Map enumerations)
	{
		channelEnumerations = new HashMap<String, List<EnumerationObject>>();
		Set set = enumerations.entrySet();
		Entry entry;
		Iterator i = set.iterator();
		String key;
		List<EnumerationObject> values;
		while (i.hasNext()) {
			entry = (Entry) i.next();
			key = (String) entry.getKey();
			values = (List<EnumerationObject>) entry.getValue();
			sortEnumerations(values);
			channelEnumerations.put(key, values);
		}
	}

	/**
	 * Sets the enumerations related to image metadata.
	 * 
	 * @param enumerations The value to set.
	 */
	void setImageEnumerations(Map enumerations)
	{
		imageEnumerations = new HashMap<String, List<EnumerationObject>>();
		Set set = enumerations.entrySet();
		Entry entry;
		Iterator i = set.iterator();
		String key;
		List<EnumerationObject> values;
		while (i.hasNext()) {
			entry = (Entry) i.next();
			key = (String) entry.getKey();
			values = (List<EnumerationObject>) entry.getValue();
			sortEnumerations(values);
			imageEnumerations.put(key, values);
		}
	}
	
	/**
	 * Returns the description of the passed annotation.
	 * 
	 * @param annotation The annotation to handle.
	 * @return See above.
	 */
	String getAnnotationDescription(AnnotationData annotation)
	{
		if (annotation == null) return "";
		if (annotation instanceof TagAnnotationData)
			return ((TagAnnotationData) annotation).getTagDescription();
		else if (annotation instanceof TermAnnotationData)
			return ((TermAnnotationData) annotation).getTermDescription();
		else if (annotation instanceof XMLAnnotationData)
			return ((XMLAnnotationData) annotation).getDescription();
		return "";
	}
	
	/**
	 * Returns <code>true</code> if the display is for a single 
	 * object, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isSingleMode() { return parent.isSingleMode(); }

	/** 
	 * Returns the object path i.e. if a dataset is selected,
	 * the name of the project_name of the dataset.
	 * 
	 * @return See above.
	 */
	String getObjectPath() { return parent.getObjectPath(); }
	
	/**
	 * Starts asynchronous load of the plane info.
	 * 
	 * @param channel 	The selected channel.
	 * @param z 		The selected z-section.
	 */
	void firePlaneInfoLoading(int channel, int z)
	{
		Object ref = getRefObject();
		if (ref instanceof WellSampleData) {
			WellSampleData wsd = (WellSampleData) ref;
			ref = wsd.getImage();
		}
		if (!(ref instanceof ImageData)) return;
		ImageData img = (ImageData) ref;
		PlaneInfoLoader loader = new PlaneInfoLoader(component, 
				parent.getSecurityContext(), img.getDefaultPixels().getId(),
				channel, z);
		loader.load();
	}

	/**
	 * Brings up the dialog to create a movie.
	 * 
	 * @param scaleBar 	   The value of the scale bar. 
	 * 					   If not greater than <code>0</code>, the value is not 
	 * 					   taken into account.
	 * @param overlayColor The color of the scale bar and text. 
	 */
	void makeMovie(int scaleBar, Color overlayColor)
	{
		parent.makeMovie(scaleBar, overlayColor);
	}
	
	/**
	 * Analyzes the data. 
	 * 
	 * @param index The index identifying the analysis to perform.
	 */
	void analyse(int index) { parent.analyse(index); }
	
	/**
	 * Returns <code>true</code> if the renderer has been loaded,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isRendererLoaded() { return renderer != null; }
	
	/** 
	 * Starts an asynchronous call to load the rendering control. 
	 * Returns <code>false</code> if already loaded, <code>true</code>
	 * otherwise.
	 * 
	 * @param pixelsID The id of the pixels set.
	 * @param index One of the constants defined by the RenderingControlLoader
	 * class
	 * @return 
	 */
	boolean fireRenderingControlLoading(long pixelsID, int index)
	{
		if (isRendererLoaded() && index == RenderingControlLoader.LOAD) 
			return false;
		RenderingControlLoader loader = new RenderingControlLoader(component, 
		        new SecurityContext(((DataObject)refObject).getGroupId()), pixelsID, index);
		loader.load();
		return true;
	}
	
	/**
         * Reloads the {@link RenderingControl} for the given pixelsID
         * (Note: This is a blocking method, for asynchronous call use 
         *   {@link fireRenderingControlLoading(long, int)} instead
         * @param pixelsID The id of the pixels set.
         */
        void loadRenderingControl(long pixelsID) {
            OmeroImageService rds = MetadataViewerAgent.getRegistry()
                    .getImageService();
            try {
                RenderingControl ctrl = rds.loadRenderingControl(
                        new SecurityContext(((DataObject)refObject).getGroupId()), pixelsID);
                setRenderingControl(ctrl);
            } catch (Throwable e) {
                LogMessage msg = new LogMessage();
                msg.print("Could not reload RenderingControl for pixelsId="+pixelsID);
                msg.print(e);
                MetadataViewerAgent.getRegistry().getLogger().warn(this, msg);
            }
    
        }
	
	/**
	 * Sets the rendering control.
	 * 
	 * @param rndControl The value to set.
	 */
	void setRenderingControl(RenderingControl rndControl)
	{
		if (renderer != null) {
			renderer.onSettingsApplied(rndControl);
		} else {
		    renderer = RendererFactory.createRenderer(getSecurityContext(),
                    rndControl, getImage(), getRndIndex(), getXMLAnnotations());
		}
	}

	/**
	 * Returns the renderer.
	 * 
	 * @return See above.
	 */
	Renderer getRenderer() { return renderer; }
	
	/**
	 * Returns the rendering constants. 
	 * 
	 * @return See above.
	 */
	int getRndIndex() { return parent.getRndIndex(); }
	
	/**
	 * Returns <code>true </code> if the object e.g. image has been published,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasBeenPublished()
	{
		StructuredDataResults data = parent.getStructuredData();
		if (data == null) return false;
		return getPublishedAnnotation() != null;
	}

	/**
	 * Loads the original metadata file for the image currently selected.
	 * 
	 * @param uiView The view to notify.
	 */
	void loadOriginalMetadata(Object uiView)
	{
		ImageData img = getImage();
		OriginalMetadataLoader loader = new OriginalMetadataLoader(component,
				parent.getSecurityContext(), img.getId(), uiView);
		loader.load();
	}
	
	/**
	 * Loads the specified files.
	 * 
	 * @param files  The file to load.
	 */
	void loadFiles(Map<FileAnnotationData, Object> files)
	{
		if (!MetadataViewerAgent.isBinaryAvailable()) return;
		FileLoader loader = new FileLoader(component,
				parent.getSecurityContext(), files);
		loader.load();
	}
	
	/** 
	 * Notifies that the rendering control has been loaded. 
	 * 
	 * @param reload Pass <code>true</code> if the rendering control has been
	 * 				 reloaded following an exception, <code>false</code> if 
	 * 				 it is an initial load.
	 */
	void onRndLoaded(boolean reload)
	{
		if (renderer == null) return;
		parent.onRndLoaded(reload);
	}

	/**
	 * Sets the instrument used to capture the image.
	 * 
	 * @param data The value to set.
	 */
	void setInstrumentData(InstrumentData data) { instrumentData = data; }
	
	/**
	 * Returns the instrument data.
	 * 
	 * @return See above.
	 */
	InstrumentData getInstrumentData() { return instrumentData; }
	
	/** 
     * Sorts the passed collection of data objects by ID.
     * 
     * @param values  Collection of <code>DataObject</code>s to sort.
     */
    void sortDataObjectByID(List values)
    {
        if (values == null || values.size() == 0) return;
        Comparator c = new Comparator() {
            public int compare(Object o1, Object o2)
            {
                long n1 = ((DataObject) o1).getId(),
                     n2 = ((DataObject) o2).getId();
                int v = 0;
                if (n1 < n2) v = -1;
                else if (n1 > n2) v = 1;
                return -v;
            }
        };
        Collections.sort(values, c);
    }

    /** Updates the permissions. */
	void upgradePermissions() {}
    
	/**
	 * Invokes when the color of the channel has been modified using the 
	 * renderer.
	 * 
	 * @param index The index of the channel.
	 * @return See above.
	 */
	Color getChannelColor(int index)
	{
		if (renderer == null) return null;
		return renderer.getChannelColor(index);
	}

	/**
	 * Returns <code>true</code> if it is an image with a lot of channels.
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isNumerousChannel()
	{
		if (!(refObject instanceof ImageData || 
				refObject instanceof WellSampleData))
			return false;
		if (renderer != null) 
			return renderer.getPixelsDimensionsC() >= Renderer.MAX_CHANNELS;
		ImageData img = null;
		if (refObject instanceof WellSampleData)
			img = ((WellSampleData) refObject).getImage();
		if (refObject instanceof ImageData)
			img = (ImageData) refObject;
		PixelsData pixels = null;
		try {
			pixels = img.getDefaultPixels();
		} catch (Exception e) {
			//ignore
		}
		if (pixels == null) return false;
		return pixels.getSizeC() >= Renderer.MAX_CHANNELS;
	}

	/**
	 * Returns <code>true</code> if the preview is available, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isPreviewAvailable()
	{
		if (!(refObject instanceof ImageData || 
				refObject instanceof WellSampleData))
			return false;
		if (!MetadataViewerAgent.isBinaryAvailable()) return false;

		ImageData img = null;
		if (refObject instanceof WellSampleData)
			img = ((WellSampleData) refObject).getImage();
		if (refObject instanceof ImageData)
			img = (ImageData) refObject;
		if (img.getId() < 0) return false;
		PixelsData pixels = null;
		try {
			pixels = img.getDefaultPixels();
		} catch (Exception e) {
			//ignore
		}
		return pixels != null && !isLargeImage();
	}
	
	/**
	 * Creates a figure.
	 * 
	 * @param value The value containing the parameters for the figure.
	 */
	void createFigure(Object value)
	{ 
		//reset the rendering settings.
		parent.createFigure(value); 
	}

	/**
	 * Runs the passed script.
	 * 
	 * @param script The script to run.
	 * @param index  Indicated to run, download or view.
	 */
	void manageScript(ScriptObject script, int index)
	{ 
		parent.manageScript(script, index);
	}
	
	/**
	 * Returns the pixels data objects.
	 * 
	 * @return See above.
	 */
	PixelsData getPixels()
	{
		ImageData img = getImage();
		if (img == null) return null;
		return img.getDefaultPixels();
	}

	/**
	 * Returns the image or <code>null</code> if the primary select
	 * node is an image or a well.
	 * 
	 * @return See above.
	 */
	ImageData getImage()
	{
		Object object = getPrimarySelect();
		ImageData img = null;
		if (object instanceof WellSampleData)
			img = ((WellSampleData) object).getImage();
		if (object instanceof ImageData)
			img = (ImageData) object;
		return img;
	}
	
	/** 
	 * Loads the ROI for the selected image. 
	 * 
	 * @param index The index of the figure to create.
	 */
	void fireROILoading(int index)
	{
		ImageData img = getImage();
		if (img == null) return;
		long userID = MetadataViewerAgent.getUserDetails().getId();
		ROILoader loader = new ROILoader(component, parent.getSecurityContext(),
				img.getId(), userID, index);
		loader.load();
	}
	
	/** Discards the renderer. */
	void discardRenderer()
	{
		if (renderer != null) renderer.discard();
		renderer = null;
	}
	
	/** Refreshes the view. */
	void refresh() { parent.refresh(); }
	
	/**
	 * Sets the collection of scripts.
	 * 
	 * @param scripts The value to set.
	 */
	void setScripts(List scripts)
	{ 
		if (scripts == null) {
			this.scripts = null;
			return;
		}
		//sort the scripts.
		Map<Long, ScriptObject> map = new LinkedHashMap<Long, ScriptObject>();
		List l = sorter.sort(scripts);
		Iterator i = l.iterator();
		ScriptObject s;
		while (i.hasNext()) {
			s = (ScriptObject) i.next();
			map.put(s.getScriptID(), s);
		}
		this.scripts = map; 
	}
	
	/**
	 * Returns the collection of scripts.
	 * 
	 * @return See above.
	 */
	Collection<ScriptObject> getScripts()
	{ 
		if (scripts == null) return null;
		return scripts.values(); 
	}
	
	/** 
	 * Loads the specified script.
	 * 
	 * @param scriptID The identifier of the script to load.
	 */
	void loadScript(long scriptID)
	{
		ScriptLoader loader = new ScriptLoader(component,
				parent.getSecurityContext(), scriptID);
		loader.load();
		loaders.add(loader);
	}
	
	/**
	 * Sets the specified script.
	 * 
	 * @param script The loaded script.
	 */
	void setScript(ScriptObject script)
	{
		if (scripts == null || scripts.size() == 0) return;
		ScriptObject sc = scripts.get(script.getScriptID());
		if (sc != null) sc.setJobParams(script.getParameters());
	}
	
	/**
	 * Returns the script corresponding to the passed identifier.
	 * 
	 * @param scriptID The identifier of the script.
	 * @return See above.
	 */
	ScriptObject getScript(long scriptID)
	{
		return scripts.get(scriptID);
	}
	
	/** Loads the scripts. */
	void loadScripts()
	{
		ScriptsLoader loader = new ScriptsLoader(component,
				parent.getSecurityContext(), false);
		loader.load();
		loaders.add(loader);
	}
	
	/**
	 * Returns the id of the possible owner. This should only be used to 
	 * handle <code>FileData</code> objects.
	 * 
	 * @return See above.
	 */
	long getUserID() { return parent.getUserID(); }

	/**
	 * Returns the user currently logged in.
	 * 
	 * @return See above.
	 */
	ExperimenterData getCurrentUser()
	{
		return MetadataViewerAgent.getUserDetails();
	}
	
	/**
	 * Returns the experimenter corresponding to the specified identifier.
	 * or <code>null</code>.
	 * 
	 * @param expID The identifier of the experimenter.
	 * @return see above.
	 */
	ExperimenterData getExperimenter(long expID)
	{
		List l = (List) MetadataViewerAgent.getRegistry().lookup(
				LookupNames.USERS_DETAILS);
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
	 * Returns the name of the owner or <code>null</code> if the current owner
	 * is the user currently logged in.
	 * @return
	 */
	String getOwnerName()
	{
		Object o = getRefObject();
		if (o == null) return null;
		if (o instanceof ExperimenterData || o instanceof GroupData)
			return null;
		if (o instanceof DataObject) {
			ExperimenterData user = getCurrentUser();
			DataObject data = (DataObject) o;
			long id = user.getId();
			if (data.getId() < 0) return null;
			if (!((DataObject) o).isLoaded()) return null;
			try {
				ExperimenterData owner = data.getOwner();
				if (owner.getId() == id)
					return EditorUtil.formatExperimenter(user);
				if (owner.isLoaded())
					return EditorUtil.formatExperimenter(owner);
				owner = getExperimenter(owner.getId());
				if (owner != null)
					return EditorUtil.formatExperimenter(owner);
			} catch (Exception e) {
			}
		}
		return null;
	}

	/**
	 * Resets the password of the user currently edited.
	 * 
	 * @param newPass The value to set.
	 */
	void resetPassword(String newPass)
	{
		if (refObject instanceof ExperimenterData 
				&& MetadataViewerAgent.isAdministrator()) {
			parent.resetPassword(newPass);
		}
	}
	
    /** Uploads the user's photo. 
     * 
     * @param photo  The photo to upload.
     * @param format The format of the photo.
     */
    void uploadPicture(File photo, String format)
    { 
    	if (refObject instanceof ExperimenterData) {
    		//ExperimenterData exp = (ExperimenterData) refObject;
    		ExperimenterData exp = MetadataViewerAgent.getUserDetails();
    		UserPhotoUploader loader = new UserPhotoUploader(component,
    				parent.getSecurityContext(), exp, photo, format);
    		loader.load();
    	}
    }
    
    /** Deletes the user's photos.*/
    void deletePicture()
    {
    	if (refObject instanceof ExperimenterData) {
    		try {
    			ExperimenterData exp = MetadataViewerAgent.getUserDetails();
    			if (usersPhoto != null) usersPhoto.remove(exp.getId());
    			OmeroMetadataService svc = 
    				MetadataViewerAgent.getRegistry().getMetadataService();
    			Collection photos = svc.loadAnnotations(
    					parent.getSecurityContext(), 
    					FileAnnotationData.class,
    					FileAnnotationData.EXPERIMENTER_PHOTO_NS, exp.getId());
    			if (photos == null || photos.size() == 0) return;
    			List<DeletableObject> l = new ArrayList<DeletableObject>();
	    		Iterator<AnnotationData> j = photos.iterator();
	    		while (j.hasNext())
	    			l.add(new DeletableObject(j.next()));
	    		IconManager icons = IconManager.getInstance();
	    		DeleteActivityParam p = new DeleteActivityParam(
	    				icons.getIcon(IconManager.APPLY_22), l);
	    		p.setUIRegister(false);
	    		p.setFailureIcon(icons.getIcon(IconManager.DELETE_22));
	    		UserNotifier un =
	    		        MetadataViewerAgent.getRegistry().getUserNotifier();
	    		un.notifyActivity(getSecurityContext(), p);
			} catch (Exception e) {
				// TODO: handle exception
			}
    	}
    }
    
    /** Notifies the parent to upload the script. */
    void uploadScript()
    {
    	parent.uploadScript();
    }
    
    /**
     * Returns the collection of scripts with a UI, mainly the figure scripts.
     * 
     * @return See above.
     */
    private List<ScriptObject> getScriptsWithUI()
    {
    	if (scriptsWithUI != null) return scriptsWithUI;
    	try {
    		OmeroImageService svc = 
    			MetadataViewerAgent.getRegistry().getImageService();
    		scriptsWithUI = svc.loadAvailableScriptsWithUI(getSecurityContext());
    		return scriptsWithUI;
		} catch (Exception e) {
			LogMessage msg = new LogMessage();
			msg.print("Scripts with UI");
			msg.print(e);
			MetadataViewerAgent.getRegistry().getLogger().error(this, msg);
		}
    	return new ArrayList<ScriptObject>();
    }
    
    /**
     * Returns the script corresponding to the specified name.
     * 
     * @return See above.
     */
    ScriptObject getScriptFromName(String name)
    { 
    	List<ScriptObject> scripts = getScriptsWithUI();
    	Iterator<ScriptObject> i = scripts.iterator();
    	ScriptObject script;
    	while (i.hasNext()) {
    		script = i.next();
			if (name.contains(script.getName()))
				return script;
		}
    	return null;
    }
    
    /**
     * Loads the attachments.
     * 
     * @param analysis The object hosting the results.
     */
    void loadAnalysisResults(AnalysisResultsItem analysis)
    {
    	if (resultsLoader == null)
    		resultsLoader = new HashMap<AnalysisResultsItem, EditorLoader>();
    	AnalysisResultsFileLoader loader = new AnalysisResultsFileLoader(
    			component, parent.getSecurityContext(), analysis);
    	resultsLoader.put(analysis, loader);
    	loader.load();
    }
    
    /**
     * Cancels on-going loading of results.
     * 
     * @param item The object hosting the results.
     */
    void cancelAnalysisResultsLoading(AnalysisResultsItem item)
    {
    	if (resultsLoader == null) return;
    	EditorLoader loader = resultsLoader.get(item);
    	if (loader != null) loader.cancel();
    }
    
    /**
     * Cancels on-going loading of results.
     * 
     * @param item The object hosting the results.
     */
    void removeAnalysisResultsLoading(AnalysisResultsItem item)
    {
    	if (resultsLoader == null) return;
    	resultsLoader.remove(item);
    }
    
    /**
     * Returns <code>true</code> if the passed annotation has to be 
     * deleted, <code>false</code> otherwise.
     * 
     * @param annotation The annotation to handle.
     * @return See above.
     */
    boolean isAnnotationToDelete(AnnotationData annotation)
    {
    	if (toDelete == null || toDelete.size() == 0) return false;
    	Iterator<AnnotationData> i = toDelete.iterator();
    	AnnotationData data;
    	while (i.hasNext()) {
			data = i.next();
			if (data.getId() == annotation.getId())
				return true;
		}
    	return false;
    }
    
	/**
	 * Returns the collection of the attachments linked to the 
	 * <code>DataObject</code>.
	 * 
	 * @return See above.
	 */
	List<FileAnnotationData> getTabularData()
	{ 
		StructuredDataResults data = parent.getStructuredData();
		List<FileAnnotationData> l = new ArrayList<FileAnnotationData>();
		if (data == null) return l;
		Collection<FileAnnotationData> attachments = data.getAttachments(); 
		if (attachments == null) return l;
		Iterator<FileAnnotationData> i = attachments.iterator();
		FileAnnotationData f;
		String ns;
		while (i.hasNext()) {
			f = i.next();
			ns = f.getNameSpace();
			if (FileAnnotationData.BULK_ANNOTATIONS_NS.equals(ns)) {
				l.add(f);
			}
		}
		return l;
	}
	
	/**
	 * Returns the object to save as.
	 * 
	 * @param ho The data object.
	 * @return See above.
	 */
	private DataObject saveAsObject(Object ho)
	{
	    if (ho instanceof ImageData || ho instanceof DatasetData) {
            return (DataObject) ho;
        }
	    if (ho instanceof WellSampleData) {
	        return ((WellSampleData) ho).getImage();
	    }
	    return null;
	}

	/** 
	 * Saves locally the images as <code>JPEG</code>, <code>PNG</code>
	 * or <code>TIFF</code>.
	 * 
	 * @param folder The folder where to save the images.
	 * @param format The format to use.
	 */
	void saveAs(File folder, int format, String filename)
	{
	    Collection l = parent.getRelatedNodes();
	    List<DataObject> objects = new ArrayList<DataObject>();
	    Object o;
	    DataObject data;
	    if (l != null) {
	        Iterator i = l.iterator();
	        while (i.hasNext()) {
	            data = saveAsObject(i.next());
	            if (data != null)
	                objects.add(data);
	        }
	    }
	    data = saveAsObject(getRefObject());
	    if (data != null)
	        objects.add(data);
	    if (objects.size() > 0) {
	        IconManager icons = IconManager.getInstance();
	        SaveAsParam p = new SaveAsParam(folder, objects);
	        p.setIndex(format);
	        p.setIcon(icons.getIcon(IconManager.SAVE_AS_22));
	        p.setBatchExportFilename(filename);
	        p.setDeleteWhenFinished(true);
	        UserNotifier un =
	                MetadataViewerAgent.getRegistry().getUserNotifier();
	        un.notifyActivity(getSecurityContext(), p);
	    }
	}
	
	/**
	 * Returns the collection of selected objects.
	 * 
	 * @return See above.
	 */
	List<DataObject> getSelectedObjects()
	{
	    List<DataObject> objects = new ArrayList<DataObject>();
	    Collection l = parent.getRelatedNodes();
	    if (CollectionUtils.isEmpty(l)) {
	        if (getRefObject() instanceof DataObject)
	            objects.add((DataObject) getRefObject());
	        return objects;
	    }
	    Iterator i = l.iterator();
	    Object o;
	    while (i.hasNext()) {
	        o = i.next();
	        if (o instanceof DataObject)
	            objects.add((DataObject) o);
	    }

	    return objects;
	}

	/**
	 * Returns <code>true</code> if the image is a large image,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isLargeImage() { return largeImage; }

	/**
	 * Indicates if the image is a big image or not.
	 * 
	 * @param value The value to set.
	 */
	void setLargeImage(Boolean value)
	{
		if (value == null) largeImage = false;
		largeImage = value.booleanValue();
	}
	
	/**
	 * Returns the parent UI.
	 * 
	 * @return See above.
	 */
	JFrame getRefFrame() { return parent.getParentUI(); }

	/**
	 * Sets the photo associated to the current user.
	 * 
	 * @param photo The photo to set.
	 * @param expId The identifier of the experimenter.
	 */
	void setUserPhoto(BufferedImage photo, long expId)
	{ 
		if (usersPhoto == null)
			usersPhoto = new HashMap<Long, BufferedImage>();
		usersPhoto.put(expId, photo) ;
	}
	
	/**
	 * Returns the photo associated to the current user.
	 * 
	 * @param expId The identifier of the experimenter.
	 * @return See above.
	 */
	BufferedImage getUserPhoto(long expId)
	{
		if (usersPhoto == null) return null;
		return usersPhoto.get(expId);
	}
	
	/**
	 * Returns the security context.
	 * 
	 * @return See above.
	 */
	SecurityContext getSecurityContext() { return parent.getSecurityContext(); }
	
	/**
	 * Returns <code>true</code> if the name of the group already exists,
	 * <code>false</code> otherwise.
	 * 
	 * @param data The group to handle.
	 * @param name The name to check.
	 * @return
	 */
	boolean doesGroupExist(GroupData data, String name)
	{
		if (data == null) return false;
		AdminService svc = MetadataViewerAgent.getRegistry().getAdminService();
		try {
			GroupData g = svc.lookupGroup(getSecurityContext(), name);
			if (g != null && data.getId() != g.getId()) {
				return true;
			}
		} catch (Exception e) {}
		return false;
	}

	/**
	 * Returns the annotations associated to the selected objects.
	 * 
	 * @return See above.
	 */
	Map<DataObject, StructuredDataResults> getAllStructuredData()
	{
		return parent.getAllStructuredData();
	}
	
	/**
	 * Returns <code>true</code> if the specified namespace is excluded.
	 * <code>false</code> otherwise.
	 * 
	 * @param ns The namespace to handle.
	 * @return See above.
	 */
	boolean isNameSpaceExcluded(String ns)
	{
		if (ns == null) return false;
		return EXCLUDED_FILE_NS.contains(ns);
	}
	
	/**
	 * Update the channels.
	 * 
	 * @param channels The value to set.
	 */
	void updateChannels(List<ChannelData> channels)
	{
	    if (channelAcquisitionDataMap != null) {
	        channelAcquisitionDataMap.clear();
	    }
		List l = sorter.sort(channels); 
		emissionsWavelengths = new LinkedHashMap();
		Iterator i = l.iterator();
		Object channel;
		while (i.hasNext()) {
			channel = i.next();
			emissionsWavelengths.put(channel,emissionsWavelengths.get(channel));
		}
	}

	/**
	 * Returns the file set associated to the image if any.
	 * 
	 * @return See above.
	 */
	Set<FilesetData> getFileset() { return set; }
	
	/**
	 * Sets the file set associated to the image if any.
	 * 
	 * @param set The value to set.
	 */
	void setFileset(Set<FilesetData> set) { this.set = set; }
	
	/** Loads the file set if the specified object is an image.*/
	void fireFilesetLoading()
	{
		ImageData img = getImage();
		if (img == null) return;
		FilesetLoader loader = new FilesetLoader(component,
				getSecurityContext(), img.getId());
		loader.load();
	}
	
	/*** Returns the display mode. One of the constants defined by 
	 * {@link LookupNames}.
	 * 
	 * @return See above.
	 */
	int getDisplayMode()
	{
		Integer value = (Integer) MetadataViewerAgent.getRegistry().lookup(
    			LookupNames.DATA_DISPLAY);
		if (value == null) return LookupNames.EXPERIMENTER_DISPLAY;
		switch (value.intValue()) {
			case LookupNames.EXPERIMENTER_DISPLAY:
			case LookupNames.GROUP_DISPLAY:
			return value.intValue();
		}
		return LookupNames.EXPERIMENTER_DISPLAY;
	}

	/**
	 * Returns <code>true</code> if the annotations are loaded,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isAnnotationLoaded()
	{
		StructuredDataResults data = parent.getStructuredData();
		if (data == null) return false;
		return data.isLoaded();
	}
	
	/**
	 * Returns <code>true</code> if the image has an original metadata file
	 * linked to it, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasOriginalMetadata()
	{
		//FileAnnotationData fa = getOriginalMetadata();
		//return fa != null;
		return true;
	}

	/**
	 * Returns the groups the user is member of.
	 * 
	 * @return See above.
	 */
	Collection<GroupData> getAvailableGroups()
	{
	    return MetadataViewerAgent.getAvailableUserGroups();
	}

    /**
     * Returns <code>true</code> if the user is a system user e.g. root
     * <code>false</code> otherwise.
     *
     * @param id The identifier of the user.
     * @return See above.
     */
    boolean isSystemUser(long id)
    {
        return MetadataViewerAgent.getRegistry().getAdminService().isSystemUser(id);
    }

    /**
     * Returns <code>true</code> if the group is a system group e.g. System
     * <code>false</code> otherwise.
     *
     * @param id The identifier of the group.
     * @return See above.
     */
    boolean isSystemGroup(long id)
    {
        return MetadataViewerAgent.getRegistry().getAdminService().isSecuritySystemGroup(id);
    }

    /**
     * Returns <code>true</code> if the group is a system group e.g. System
     * <code>false</code> otherwise.
     *
     * @param id The identifier of the group.
     * @param key The type of group to check.
     * @return See above.
     */
    boolean isSystemGroup(long id, String key)
    {
        return MetadataViewerAgent.getRegistry().getAdminService().isSecuritySystemGroup(id, key);
    }
    
    /**
     * Returns the {@link ImportType}
     * @return See above.
     */
    ImportType getImportType() {
        StructuredDataResults data = parent.getStructuredData();
        if (data != null) {
            Collection<AnnotationData> tfl = data.getTransferLinks();
            if (tfl != null) {
                for (AnnotationData an : tfl) {
                    if (AnnotationData.FILE_TRANSFER_NS.equals(an
                            .getNameSpace())) {
                        String content = an.getContent().toString();
                        return ImportType.getImportType(content);
                    }
                }
            }
        }
        // if nothing's specified it's the default UPLOAD import type
        return ImportType.UPLOAD;
    }

    /**
     * Fires an asynchronous call to retrieve the LDAP details.
     */
    void fireLDAPDetailsLoading()
    {
        if (!(getRefObject() instanceof ExperimenterData)) return;
        ExperimenterData exp = (ExperimenterData) getRefObject();
        EditorLoader l = new LDAPLoader(component, getSecurityContext(),
                exp.getId());
        l.load();
    }

    /**
     * Returns <code>true</code> if the user is connected via LDAP,
     * <code>false</code> otherwise.
     *
     * @return See above.
     */
    boolean isLDAP()
    {
        if (getRefObject() instanceof ExperimenterData) {
            ExperimenterData exp = (ExperimenterData) getRefObject();
            return exp.isLDAP();
        }
        return false;
    }
}
