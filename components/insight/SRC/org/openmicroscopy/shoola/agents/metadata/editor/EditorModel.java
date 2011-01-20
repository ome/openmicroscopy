/*
 * org.openmicroscopy.shoola.agents.metadata.editor.EditorModel 
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
package org.openmicroscopy.shoola.agents.metadata.editor;


//Java imports
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

//Third-party libraries

//Application-internal dependencies
import omero.model.OriginalFile;
import omero.model.PlaneInfo;

import org.openmicroscopy.shoola.agents.editor.EditorAgent;
import org.openmicroscopy.shoola.agents.events.iviewer.ViewImage;
import org.openmicroscopy.shoola.agents.metadata.AcquisitionDataLoader;
import org.openmicroscopy.shoola.agents.metadata.AttachmentsLoader;
import org.openmicroscopy.shoola.agents.metadata.ChannelDataLoader;
import org.openmicroscopy.shoola.agents.metadata.DiskSpaceLoader;
import org.openmicroscopy.shoola.agents.metadata.EditorLoader;
import org.openmicroscopy.shoola.agents.metadata.EnumerationLoader;
import org.openmicroscopy.shoola.agents.metadata.FileLoader;
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.InstrumentDataLoader;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.metadata.OriginalFileLoader;
import org.openmicroscopy.shoola.agents.metadata.PasswordEditor;
import org.openmicroscopy.shoola.agents.metadata.PlaneInfoLoader;
import org.openmicroscopy.shoola.agents.metadata.RenderingControlLoader;
import org.openmicroscopy.shoola.agents.metadata.TagsLoader;
import org.openmicroscopy.shoola.agents.metadata.ThumbnailLoader;
import org.openmicroscopy.shoola.agents.metadata.browser.Browser;
import org.openmicroscopy.shoola.agents.metadata.rnd.Renderer;
import org.openmicroscopy.shoola.agents.metadata.rnd.RendererFactory;
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewer;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import org.openmicroscopy.shoola.env.data.model.DownloadActivityParam;
import org.openmicroscopy.shoola.env.data.model.EnumerationObject;
import org.openmicroscopy.shoola.env.data.util.StructuredDataResults;
import org.openmicroscopy.shoola.env.data.util.ViewedByDef;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.component.ObservableComponent;
import pojos.AnnotationData;
import pojos.BooleanAnnotationData;
import pojos.ChannelAcquisitionData;
import pojos.ChannelData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.GroupData;
import pojos.ImageAcquisitionData;
import pojos.ImageData;
import pojos.InstrumentData;
import pojos.PermissionData;
import pojos.PixelsData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.RatingAnnotationData;
import pojos.ScreenAcquisitionData;
import pojos.ScreenData;
import pojos.TagAnnotationData;
import pojos.TextualAnnotationData;
import pojos.URLAnnotationData;
import pojos.WellData;
import pojos.WellSampleData;

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
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class EditorModel 
{
	
	/** The default name for the original metadata file. */
	static final String ORIGINAL_METADATA_NAME = "original_metadata.txt";
	
	/** The parent of this editor. */
	private  MetadataViewer			parent;
	
	/** Reference to the component that embeds this model. */
	private Editor					component;
	
	/** The object this editor is for. */
	private Object					refObject;

	/** The parent of the object this editor is for. */
	private Object					parentRefObject;
	
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
    
    /** The retrieved thumbnails. */
    private Map<Long, BufferedImage> thumbnails;
    
    /** Collection of existing tags if any. */
    private Collection				existingTags;
    
    /** Collection of existing attachments if any. */
    private Collection				existingAttachments;
    
    /** The list of emission wavelengths for a given set of pixels. */
    private Map						emissionsWavelengths;
    
    /** Used to sort the various collection. */
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

	/**
	 * Downloads the files.
	 * 
	 * @param folder The folder to save the file into.
	 */
	private void downloadFiles(File folder)
	{
		UserNotifier un = EditorAgent.getRegistry().getUserNotifier();
		FileAnnotationData fa = (FileAnnotationData) getRefObject();
		OriginalFile f = (OriginalFile) fa.getContent();
		IconManager icons = IconManager.getInstance();
		
		DownloadActivityParam activity = new DownloadActivityParam(f,
				folder, icons.getIcon(IconManager.DOWNLOAD_22));
		un.notifyActivity(activity);
		
		Collection l = parent.getRelatedNodes();
		if (l == null) return;
		Iterator i = l.iterator();
		Object o;
		while (i.hasNext()) {
			o = i.next();
			if (o instanceof FileAnnotationData) {
				fa = (FileAnnotationData) o;
				f = (OriginalFile) fa.getContent();
				activity = new DownloadActivityParam(f,
						folder, icons.getIcon(IconManager.DOWNLOAD_22));
				activity.setFileName(fa.getFileName());
				un.notifyActivity(activity);
			}
		}
	}
	
	/** 
	 * Downloads the archived images. 
	 * 
	 * @param folder The folder to save the file into.
	 */
	private void downloadImages(File folder)
	{
		Set<Long> ids = new HashSet<Long>();
		Collection l = parent.getRelatedNodes();
		ImageData img;
		PixelsData data;
		if (l != null) {
			Iterator i = l.iterator();
			Object o;
			while (i.hasNext()) {
				o = (Object) i.next();
				if (o instanceof ImageData) {
					img = (ImageData) o;
					if (img.isArchived()) {
						data = img.getDefaultPixels();
						ids.add(data.getId());
					}
				}
			}
		}
		img = (ImageData) getRefObject();
		if (img.isArchived()) {
			data = img.getDefaultPixels();
			ids.add(data.getId());
		}
		
		OriginalFileLoader loader = new OriginalFileLoader(component, ids, 
				folder);
		loader.load();
		loaders.add(loader);
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
		String name = "";
		Object ref = getRefObject();
		if (ref instanceof ImageData)
			name = ((ImageData) ref).getName();
		else if (ref instanceof DatasetData)
			name = ((DatasetData) ref).getName();
		else if (ref instanceof ProjectData)
			name = ((ProjectData) ref).getName();
		else if (ref instanceof TagAnnotationData)
			name = ((TagAnnotationData) ref).getTagValue();
		else if (ref instanceof ScreenData)
			name = ((ScreenData) ref).getName();
		else if (ref instanceof PlateData)
			name = ((PlateData) ref).getName();
		else if (ref instanceof ScreenAcquisitionData)
			name = ((ScreenAcquisitionData) ref).getLabel();
		else if (ref instanceof FileAnnotationData)
			name = ((FileAnnotationData) ref).getFileName();
		else if (ref instanceof WellSampleData) {
			WellSampleData ws = (WellSampleData) ref;
			ImageData img = ws.getImage();
			if (img != null && img.getId() >= 0) name = img.getName();
		} //else if (ref instanceof FolderData)
			//name = ((FolderData) ref).getName();
		if (name == null) return "";
		return name.trim();
	}

	/**
	 * Returns the description of the object if any.
	 * 
	 * @return See above.
	 */
	String getRefObjectDescription() 
	{
		String description = "";
		Object ref = getRefObject();
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
		else if (ref instanceof TagAnnotationData) {
			description = ((TagAnnotationData) ref).getTagDescription();
		} else if (ref instanceof WellSampleData) {
			if (parentRefObject instanceof WellData) {
				WellData ws = (WellData) parentRefObject;
				description = ws.getWellType();
			}
		} //else if (ref instanceof FolderData) 
			//description = null;//((FolderData) ref).getDescription();
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
	 * Returns the <code>DataObject</code> this editor is for.
	 * 
	 * @return See above.
	 */
	Object getRefObject()
	{ 
		StructuredDataResults data = parent.getStructuredData();
		if (data == null) return refObject;
		Object o = data.getRelatedObject();
		//if (o != null && o instanceof FolderData) return o;
		return refObject; 
	}
	
	/**
	 * Returns <code>true</code> if the object is readable,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isReadable()
	{
		return false;
	}
	
	/**
	 * Returns <code>true</code> if the object is writable,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isWritable() { return false; }
	
	/**
	 * Returns <code>true</code> if the group's name is valid, 
	 * <code>false</code> otherwise.
	 * 
	 * @param g The group to check.
	 * @return See above.
	 */
	boolean isValidGroup(GroupData g)
	{
		if (g == null) return false;
		String name = g.getName();
		if ("user".equals(name) || "default".equals(name)) return false;
		return true;
	}
	
	/**
	 * Returns <code>true</code> if the currently logged in user is the 
	 * owner of the object, <code>false</code> otherwise.
	 * 
	 * @param object The object to handle.
	 * @return See above.
	 */
	boolean isCurrentUserOwner(Object object)
	{
		long userID = MetadataViewerAgent.getUserDetails().getId();
		if (object == null) return false;
		if (object instanceof ExperimenterData) 
			return (((ExperimenterData) object).getId() == userID);
		if (object instanceof DataObject)  {
			try {
				ExperimenterData exp = ((DataObject) object).getOwner();
				if (exp == null) return false;
				return userID == exp.getId();
			} catch (Exception e) {
				return false;
			}
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
	 * Returns the first name and the last name of the owner.
	 * 
	 * @param object The object to handle.
	 * @return See above.
	 */
	String formatDate(DataObject object)
	{
		String date = "";
		Timestamp time = null;
		if (object == null) return date;
		if (object instanceof AnnotationData)
			time = ((AnnotationData) object).getLastModified();
		else if (object instanceof ImageData) 
			time = EditorUtil.getAcquisitionTime((ImageData) object);
			
		if (time != null) date = UIUtilities.formatShortDateTime(time);
		return date;
	}
	
	/**
	 * Returns the number of URLs linked to the <code>DataObject</code>.
	 * 
	 * @return See above.
	 */
	int getUrlsCount()
	{ 
		Collection urls = getUrls();
		if (urls == null) return 0;
		return urls.size();
	}
	
	/**
	 * Returns the collection of the URLs linked to the <code>DataObject</code>.
	 * 
	 * @return See above.
	 */
	Collection getUrls()
	{ 
		StructuredDataResults data = parent.getStructuredData();
		if (data == null) return null;
		return data.getUrls(); 
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
	Collection getTags()
	{ 
		StructuredDataResults data = parent.getStructuredData();
		if (data == null) return new ArrayList();
		Collection tags = data.getTags();
		if (tags == null || tags.size() == 0) return new ArrayList();
		return sorter.sort(tags);
	}
	
	/**
	 * Returns the number of attachments linked to the <code>DataObject</code>.
	 * 
	 * @return See above.
	 */
	int getAttachmentsCount()
	{
		Collection attachments = getAttachments();
		if (attachments == null) return 0;
		return attachments.size();
	}
	
	private FileAnnotationData originalMetadata;
	
	/**
	 * Returns the collection of the tags linked to the <code>DataObject</code>.
	 * 
	 * @return See above.
	 */
	Collection getAttachments()
	{ 
		StructuredDataResults data = parent.getStructuredData();
		if (data == null) return new ArrayList();
		Collection attachements = data.getAttachments(); 
		if (attachements == null) return new ArrayList();
		Iterator i = attachements.iterator();
		FileAnnotationData f;
		String ns;
		while (i.hasNext()) {
			f = (FileAnnotationData) i.next();
			ns = f.getNameSpace();
			if (FileAnnotationData.COMPANION_FILE_NS.equals(ns)) {
				//tmp
				String name = f.getFileName();
				if (name.contains(ORIGINAL_METADATA_NAME))
					originalMetadata = f;
			}
		}
		if (originalMetadata != null) attachements.remove(originalMetadata);
		return sorter.sort(attachements); 
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
	}
	
	/**
	 * Returns the number of ratings for that object.
	 * 
	 * @return See above.
	 */
	int getRatingCount()
	{
		StructuredDataResults data = parent.getStructuredData();
		Collection ratings = data.getRatings();
		if (ratings == null) return 0;
		return ratings.size();
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
		Collection ratings = data.getRatings();
		if (ratings == null || ratings.size() == 0) return null;
		Iterator i = ratings.iterator();
		RatingAnnotationData rate;
		long id = MetadataViewerAgent.getUserDetails().getId();
		while (i.hasNext()) {
			rate = (RatingAnnotationData) i.next();
			if (rate.getOwner().getId() == id)
				return rate;
		}
		return null;
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
		Collection c = data.getPublished();
		if (c == null || c.size() == 0) return null;
		Iterator i = c.iterator();
		BooleanAnnotationData b;
		long id = MetadataViewerAgent.getUserDetails().getId();
		while (i.hasNext()) {
			b = (BooleanAnnotationData) i.next();
			if (b.getOwner().getId() == id)
				return b;
		}
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
	 * Returns the average rating value.
	 * 
	 * @return See above.
	 */
	int getRatingAverage() 
	{
		StructuredDataResults data = parent.getStructuredData();
		if (data == null) return 0;
		Collection ratings = data.getRatings();
		if (ratings == null || ratings.size() == 0) return 0;
		Iterator i = ratings.iterator();
		RatingAnnotationData rate;
		int value = 0;
		while (i.hasNext()) {
			rate = (RatingAnnotationData) i.next();
			value += rate.getRating();
		}
		return value/ratings.size();
	}
	
	/**
	 * Returns the number of textual annotations for that object.
	 * 
	 * @return See above.
	 */
	int getTextualAnnotationCount()
	{
		Collection annotations = getTextualAnnotations();
		if (annotations == null) return 0;
		return annotations.size();
	}
	
	/**
	 * Returns the collection of textual annotations.
	 * 
	 * @return See above.
	 */
	Collection getTextualAnnotations()
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
	
	URLAnnotationData getLastUserUrlAnnotation()
	{
		Map<Long, List> map = new HashMap<Long, List>();
		Collection original = getUrls();
		if (original == null) return null;
        Iterator i = original.iterator();
        AnnotationData annotation;
        Long ownerID;
        List<AnnotationData> userAnnos;
        while (i.hasNext()) {
            annotation = (AnnotationData) i.next();
            ownerID = Long.valueOf(annotation.getOwner().getId());
            userAnnos = map.get(ownerID);
            if (userAnnos == null) {
                userAnnos = new ArrayList<AnnotationData>();
                map.put(ownerID, userAnnos);
            }
            userAnnos.add(annotation);
        }
        i = map.keySet().iterator();
        
        while (i.hasNext()) {
            ownerID = (Long) i.next();
            sortAnnotationByDate(map.get(ownerID));
        }
		long userID = MetadataViewerAgent.getUserDetails().getId();
		List l = map.get(userID);
		if (l == null || l.size() == 0) return null;
		return (URLAnnotationData) l.get(0);
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
		Collection original = getTextualAnnotations();
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
		if (object == null) return false;
		if (!object.getClass().equals(refObject.getClass())) return false;
		if ((object instanceof DataObject) &&
				(refObject instanceof DataObject)) {
			DataObject d1 = (DataObject) object;
			DataObject d2 = (DataObject) refObject;
			return d1.getId() == d2.getId();
		}
		return false;
	}
	
	/** 
	 * Sets the object of reference.
	 * 
	 * @param refObject The value to set.
	 */
	void setRootObject(Object refObject)
	{ 
		this.refObject = refObject; 
		parentRefObject = null;
		if (thumbnails != null) thumbnails.clear();
		thumbnails = null;
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
	    if (refObject instanceof ImageData) {
	    	fireChannelEnumerationsLoading();
	    	fireImageEnumerationsLoading();
	    }
	    if (renderer != null) {
	    	renderer.discard();
	    	renderer = null;
	    }
	} 

	/**
	 * Sets the parent of the object of reference.
	 * 
	 * @param parentRefObject The value to set.
	 */
	void setParentRootObject(Object parentRefObject)
	{
		this.parentRefObject = parentRefObject;
	}
	
	/**
	 * Returns the parent of the object of reference.
	 * 
	 * @return See above.
	 */
	Object getParentRootObject() { return parentRefObject; }
	
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
	
	/**
	 * Returns <code>true</code> if the thumbnails are loaded, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isThumbnailsLoaded() { return thumbnails != null; }

	/**
	 * Sets the retrieved thumbnails.
	 * 
	 * @param thumbnails The value to set.
	 */
	void setThumbnails(Map<Long, BufferedImage> thumbnails)
	{
		this.thumbnails = thumbnails;
		Iterator i = loaders.iterator();
		EditorLoader loader = null;
		while (i.hasNext()) {
			loader = (EditorLoader) i.next();
			if (loader instanceof ThumbnailLoader)
				break;
		}
		if (loader != null) loaders.remove(loader);
	}
	
	/** 
	 * Returns the thumbnails.
	 * 
	 * @return See above.
	 */
	Map<Long, BufferedImage> getThumbnails() { return thumbnails; }
	
	/** Fires an asynchronous retrieval of thumbnails. */
	void loadThumbnails()
	{
		Set<Long> ids = new HashSet<Long>();
		Collection l = getViewedBy();
		if (l == null) return;
		Iterator i = l.iterator();
		ViewedByDef def = null;
		while (i.hasNext()) {
			def = (ViewedByDef) i.next();
			ids.add(def.getExperimenter().getId());
		}
		ThumbnailLoader loader = new ThumbnailLoader(component, 
								(ImageData) getRefObject(), ids);
		loader.load();
		loaders.add(loader);
	}

	/** Cancels any ongoing thumbnails retrieval. */
	void cancelThumbnailsLoading()
	{
		Iterator i = loaders.iterator();
		EditorLoader loader;
		List<EditorLoader> toKeep = new ArrayList<EditorLoader>();
		while (i.hasNext()) {
			loader = (EditorLoader) i.next();
			if (loader instanceof ThumbnailLoader) {
				loader.cancel();
			} else toKeep.add(loader);
		}
		loaders.clear();
		loaders.addAll(toKeep);
	}
	
	/**
	 * Returns the object hosting rendering settings set 
	 * by the passed user, or <code>null</code> if any
	 * 
	 * @param userID	The id of the user who set the rendering settings.
	 * @return See above.
	 */
	ViewedByDef getViewedDef(long userID)
	{
		Collection l = getViewedBy();
		Iterator i = l.iterator();
		ViewedByDef def = null;
		while (i.hasNext()) {
			def = (ViewedByDef) i.next();
			if (def.getExperimenter().getId() == userID)
				return def;
		}
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
		TagsLoader loader = new TagsLoader(component);
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
		AttachmentsLoader loader = new AttachmentsLoader(component);
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
			if (data == null || data.getId() < 0) data = null;
		}
		try {
			PixelsData pixs = data.getDefaultPixels();
			ChannelDataLoader loader = new ChannelDataLoader(component, 
					pixs.getId());
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
	 * @param toAdd		The annotation to save.
	 * @param toRemove	The annotation to remove.
	 * @param metadata	The metadata to save.
	 */
	void fireAnnotationSaving(List<AnnotationData> toAdd,
			List<AnnotationData> toRemove, List<Object> metadata)
	{
		Object ref = getRefObject();
		if (ref instanceof DataObject) {
			DataObject data = (DataObject) ref;
			if (data instanceof WellSampleData) {
				data = ((WellSampleData) ref).getImage();
			}
			parent.saveData(toAdd, toRemove, metadata, data);
		}
	}
	
	/**
	 * Starts an asynchronous call to update the experimenter.
	 * 
	 * @param exp The experimenter to save.
	 */
	void fireDataObjectSaving(ExperimenterData exp)
	{
		parent.saveData(null, null, null, exp);
	}
	
	/**
	 * Returns <code>true</code> if the imported set of pixels has been 
	 * archived, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isArchived()
	{ 
		Object ref = getRefObject();
		if (!(ref instanceof ImageData)) return false;
		ImageData img = (ImageData) ref;
		if (img.isArchived()) return true;
		Collection l = parent.getRelatedNodes();
		if (l == null || l.size() == 0) return false;
		Iterator i = l.iterator();
		Object o;
		while (i.hasNext()) {
			o = (Object) i.next();
			if (o instanceof ImageData) {
				img = (ImageData) o;
				if (img.isArchived()) return true;
			}
		}
		return false;
	}

	/** 
	 * Starts an asynchronous loading. 
	 * 
	 * @param folder The folder to save the file into.
	 */
	void download(File folder)
	{
		Object ref = getRefObject();
		if (refObject instanceof ImageData) {
			downloadImages(folder);
		} else if (refObject instanceof FileAnnotationData) {
			downloadFiles(folder);
		}
	}
	
	/** Starts an asynchronous call to retrieve disk space information. */
	void loadDiskSpace()
	{
		long id = MetadataViewerAgent.getUserDetails().getId();
		DiskSpaceLoader loader = new DiskSpaceLoader(component, id);
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
	 * @param old		The old password.
	 * @param confirm	The new password.
	 */
	void changePassword(String old, String confirm)
	{
		EditorLoader loader = new PasswordEditor(component, old, confirm);
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

	/**
	 * Views the image and sets the rendering settings.
	 * 
	 * @param def 	The object hosting rendering settings. 
	 */
	void viewImage(ViewedByDef def)
	{
		ViewImage evt;
		ImageData img = (ImageData) getRefObject();
		evt = new ViewImage(img, null);
		if (def != null)
			evt.setSettings(def.getRndSettings(), 
					def.getExperimenter().getId());
		
		EventBus bus = MetadataViewerAgent.getRegistry().getEventBus();
		bus.post(evt);
	}
	
	/** Loads the image metadata enumerations. */
	void fireImageEnumerationsLoading()
	{
		EnumerationLoader loader = new EnumerationLoader(component, 
					EnumerationLoader.IMAGE);
		loader.load();
	}
	
	/** Loads the channel metadata enumerations. */
	void fireChannelEnumerationsLoading()
	{
		EnumerationLoader loader = new EnumerationLoader(component, 
				EnumerationLoader.CHANNEL);
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
			loader = new AcquisitionDataLoader(component, data); 
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
			loader = new AcquisitionDataLoader(component, channel); 
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
				instrumentID);
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
     * Sets the acquisition data for the specifed channel.
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
     * Sets the collection of plane info for the specifed channel.
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
	 * @param index The index of the channels.
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
		while (i.hasNext()) {
			o = (EnumerationObject) i.next();
			if (o.getValue().equals(value)) return o;
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
	 * Returns the description of the passed tag.
	 * 
	 * @param tag The tag to handle.
	 * @return See above.
	 */
	String getTagDescription(TagAnnotationData tag)
	{
		if (tag == null) return "";
		/*
		List l = tag.getTagDescriptions();
		if (l != null && l.size() > 0) {
			long userID = MetadataViewerAgent.getUserDetails().getId();
			Iterator i = l.iterator();
			TextualAnnotationData desc;
			while (i.hasNext()) {
				desc = (TextualAnnotationData) i.next();
				if (desc != null && desc.getOwner().getId() == userID) 
					return desc.getText();
			}
		}
		*/
		return tag.getTagDescription();
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
	 * @param channel The selected channel.
	 */
	void fireLoadPlaneInfo(int channel)
	{
		Object ref = getRefObject();
		if (ref instanceof WellSampleData) {
			WellSampleData wsd = (WellSampleData) ref;
			ref = wsd.getImage();
		}
		if (!(ref instanceof ImageData)) return;
		ImageData img = (ImageData) ref;
		PlaneInfoLoader loader = new PlaneInfoLoader(component, 
				img.getDefaultPixels().getId(), channel);
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
	
	/** Analyzes the data. */
	void analyse() { parent.analyse(); }
	
	/**
	 * Returns <code>true</code> if the renderer has been loaded,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isRendererLoaded() { return renderer != null; }
	
	/** 
	 * Starts an asynchronous call to load the rendering control. 
	 * 
	 * @param pixelsID The id of the pixels set.
	 * @param index One of the constants defined by the RenderingControlLoader
	 * class
	 */
	void fireRenderingControlLoading(long pixelsID, int index)
	{
		if (isRendererLoaded() && index == RenderingControlLoader.LOAD) return;
		RenderingControlLoader loader = new RenderingControlLoader(component, 
				pixelsID, index);
		loader.load();
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
			renderer = RendererFactory.createRenderer(rndControl, getRndIndex());
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

	void loadFile(FileAnnotationData data, Object uiView)
	{
		FileLoader loader = new FileLoader(component, data, uiView);
		loader.load();
	}
	
	/** Notifies that the rendering control has been loaded. 
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
    
    /**
     * Returns <code>true</code> if the current user is a leader of the passed
     * group, <code>false</code> otherwise.
     * 
     * @param groupID The id of the group.
     * @return See above.
     */
    boolean isGroupLeader(long groupID)
    {
    	ExperimenterData exp = MetadataViewerAgent.getUserDetails();
    	Map<Long, Boolean> m = exp.isLeader();
    	Boolean b = m.get(groupID);
    	if (b == null) return false;
    	return b;
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
		if (!(refObject instanceof ImageData)) return false;
		if (renderer != null) 
			return renderer.getPixelsDimensionsC() >= Renderer.MAX_CHANNELS;
		ImageData img = (ImageData) refObject;
		return img.getDefaultPixels().getSizeC() >= Renderer.MAX_CHANNELS;
		//return img.isLifetime();
	}

	/** Discards the renderer. */
	void discardRenderer()
	{
		if (renderer != null) renderer.discard();
		renderer = null;
	}
	
	/** Refreshes the view. */
	void refresh() { parent.setRootObject(getRefObject()); }
	
}
