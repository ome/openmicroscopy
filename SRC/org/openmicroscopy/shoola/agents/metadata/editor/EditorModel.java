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
import java.awt.image.BufferedImage;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.iviewer.ViewImage;
import org.openmicroscopy.shoola.agents.metadata.AcquisitionDataLoader;
import org.openmicroscopy.shoola.agents.metadata.AttachmentsLoader;
import org.openmicroscopy.shoola.agents.metadata.ChannelDataLoader;
import org.openmicroscopy.shoola.agents.metadata.DiskSpaceLoader;
import org.openmicroscopy.shoola.agents.metadata.EditorLoader;
import org.openmicroscopy.shoola.agents.metadata.EnumerationLoader;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.metadata.OriginalFileLoader;
import org.openmicroscopy.shoola.agents.metadata.PasswordEditor;
import org.openmicroscopy.shoola.agents.metadata.TagsLoader;
import org.openmicroscopy.shoola.agents.metadata.ThumbnailLoader;
import org.openmicroscopy.shoola.agents.metadata.URLsLoader;
import org.openmicroscopy.shoola.agents.metadata.browser.Browser;
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewer;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import org.openmicroscopy.shoola.env.data.model.EnumerationObject;
import org.openmicroscopy.shoola.env.data.util.StructuredDataResults;
import org.openmicroscopy.shoola.env.data.util.ViewedByDef;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.component.ObservableComponent;
import pojos.AnnotationData;
import pojos.ChannelAcquisitionData;
import pojos.ChannelData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.GroupData;
import pojos.ImageAcquisitionData;
import pojos.ImageData;
import pojos.PermissionData;
import pojos.PixelsData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.RatingAnnotationData;
import pojos.ScreenData;
import pojos.TagAnnotationData;
import pojos.TextualAnnotationData;

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
	
	/** The parent of this editor. */
	private  MetadataViewer			parent;
	
	/** Reference to the component that embeds this model. */
	private Editor					component;
	
	/** The object this editor later. */
	private Object					refObject;
	
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
    
    /** Collection of existing attachments if any. */
    private Collection				existingURLs;
    
    /** The list of emissions wavelengths for a given set of pixels. */
    private List					emissionsWavelengths;
    
    /** Used to sort the various collection. */
    private ViewerSorter			sorter;

	/** Flag indicating to load the thumbnail. */
	private boolean					thumbnailRequired;
	
	/** Reference to the browser. */
	private Browser					browser;
	
	/** The image acquisition data. */
	private ImageAcquisitionData	imageAcquisitionData;
	
	/** The enumerations related to channel metadata. */
	private Map						channelEnumerations;
	
	/** The enumerations related to image metadata. */
	private Map						imageEnumerations;
	
	/** The map hosting the channels acquisition data. */
	private Map<Integer, ChannelAcquisitionData> channelAcquisitionDatMap;
	
    /** 
     * Sorts the passed collection of annotations by date starting with the
     * most recent.
     * 
     * @param annotations   Collection of {@link AnnotationData} linked to 
     *                      the currently edited <code>Dataset</code> or
     *                      <code>Image</code>.
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
	 * Creates a new instance.
	 * 
	 * @param refObject			The object this editor is for.
	 * @param parent			The parent of this browser.
	 * @param thumbnailRequired Pass <code>true</code> to indicate to load the
	 * 							thumbnail, <code>false</code> otherwise.
	 */
	EditorModel(Object refObject, MetadataViewer parent,
				boolean thumbnailRequired) 
	{
		if (refObject == null)
			throw new IllegalArgumentException("No object set.");
		this.parent = parent;
		this.refObject = refObject;
		this.thumbnailRequired = thumbnailRequired;
		loaders = new ArrayList<EditorLoader>();
		sorter = new ViewerSorter();
		channelAcquisitionDatMap = 
			new HashMap<Integer, ChannelAcquisitionData>();
	}
	
	/**
	 * Returns <code>true</code> if multi selection is on, 
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
		if (refObject == null) return false;
    	return ((refObject instanceof ProjectData) || 
    			(refObject instanceof DatasetData));
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
		if (refObject instanceof DataObject)
			return ((DataObject) refObject).getPermissions();
		return null;
	}
	
	/**
	 * Returns the name of the object if any.
	 * 
	 * @return See above.
	 */
	String getRefObjectName() 
	{
		if (refObject instanceof ImageData)
			return ((ImageData) refObject).getName();
		else if (refObject instanceof DatasetData)
			return ((DatasetData) refObject).getName();
		else if (refObject instanceof ProjectData)
			return ((ProjectData) refObject).getName();
		else if (refObject instanceof TagAnnotationData)
			return ((TagAnnotationData) refObject).getTagValue();
		else if (refObject instanceof ScreenData)
			return ((ScreenData) refObject).getName();
		else if (refObject instanceof PlateData)
			return ((PlateData) refObject).getName();
		else if (refObject instanceof FileAnnotationData)
			return ((FileAnnotationData) refObject).getFileName();
		return "";
	}
	
	/**
	 * Returns the description of the object if any.
	 * 
	 * @return See above.
	 */
	String getRefObjectDescription() 
	{
		if (refObject instanceof ImageData)
			return ((ImageData) refObject).getDescription();
		else if (refObject instanceof DatasetData)
			return ((DatasetData) refObject).getDescription();
		else if (refObject instanceof ProjectData)
			return ((ProjectData) refObject).getDescription();
		else if (refObject instanceof ScreenData)
			return ((ScreenData) refObject).getDescription();
		else if (refObject instanceof PlateData)
			return ((PlateData) refObject).getDescription();
		return "";
	}
	
	/**
	 * Returns the id of the ref object if it is an instance of 
	 * <code>DataObject</code> or <code>-1</code> otherwise.
	 * 
	 * @return See above.
	 */
	long getRefObjectID()
	{
		if (refObject instanceof DataObject)
			return ((DataObject) refObject).getId();
		return -1;
	}
	
	/**
	 * Returns the <code>DataObject</code> this editor is for.
	 * 
	 * @return See above.
	 */
	Object getRefObject() { return refObject; }
	
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
		if (data == null) return null;
		Collection tags = data.getTags();
		if (tags == null || tags.size() == 0) return tags;
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
	
	/**
	 * Returns the collection of the tags linked to the <code>DataObject</code>.
	 * 
	 * @return See above.
	 */
	Collection getAttachments()
	{ 
		StructuredDataResults data = parent.getStructuredData();
		if (data == null) return null;
		return data.getAttachments(); 
	}

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
			//if (exp.getId() != userID) {
				value = formatOwner(exp);
				names.add(value);
				m.put(value, def);
				def.setFormattedExperimenter(value);
			//}
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
		return (TextualAnnotationData) l.get(l.size()-1);
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
            ownerID = new Long(annotation.getOwner().getId());
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
	 * Sets the object of reference.
	 * 
	 * @param refObject The value to set.
	 */
	void setRootObject(Object refObject)
	{ 
		this.refObject = refObject; 
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
	    if (existingURLs != null) 
	    	existingURLs.clear();
	    existingURLs = null;
	    if (emissionsWavelengths != null) 
	    	emissionsWavelengths.clear();
	    emissionsWavelengths = null;
	    channelAcquisitionDatMap.clear();
	    imageAcquisitionData = null;
	    if (refObject instanceof ImageData) {
	    	fireChannelEnumerationsLoading();
	    	fireImageEnumerationsLoading();
	    }
	}

	/**
	 * Returns the owner of the ref object or <code>null</code>
	 * if the object is not a <code>DataObject</code>.
	 * 
	 * @return See above.
	 */
	ExperimenterData getRefObjectOwner()
	{
		if (refObject instanceof DataObject)
			return getOwner((DataObject) refObject);
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
								(ImageData) refObject, ids);
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
	
	/** Loads the thumbnail for the currently logged in user. */
	void loadUserThumbnail()
	{
		if ((refObject instanceof ImageData) && thumbnailRequired) {
			Set<Long> l = new HashSet<Long>();
			l.add(MetadataViewerAgent.getUserDetails().getId());
			ThumbnailLoader loader = new ThumbnailLoader(component, 
					(ImageData) refObject, l, true);
			loader.load();
			loaders.add(loader);
		}
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
		int level = TagsLoader.LEVEL_TAG;
		if (getRefObject() instanceof TagAnnotationData)
			level = TagsLoader.LEVEL_TAG_SET;
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
		TagsLoader loader = new TagsLoader(component, level);
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
	
	/** 
	 * Fires an asynchronous retrieval of existing urls 
	 * for the currently logged in user. 
	 */
	void loadExistingUrls()
	{
		EditorLoader l;
		boolean exist = false;
		Iterator i = loaders.iterator();
		while (i.hasNext()) {
			l = (EditorLoader) i.next();
			if (l instanceof URLsLoader) {
				exist = true;
				break;
			}
		}
		if (exist) return;
		URLsLoader loader = new URLsLoader(component);
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
		if (!(refObject instanceof ImageData)) return;
		PixelsData data = ((ImageData) refObject).getDefaultPixels();
		ChannelDataLoader loader = new ChannelDataLoader(component, 
									data.getId());
		loader.load();
		loaders.add(loader);
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
	 * Sets the collection of existing urls.
	 * 
	 * @param urls The value to set.
	 */
	void setExistingURLs(Collection urls)
	{
		if (urls != null)
			existingURLs = sorter.sort(urls);
		Iterator i = loaders.iterator();
		EditorLoader loader;
		EditorLoader toRemove = null;
		while (i.hasNext()) {
			loader = (EditorLoader) i.next();
			if (loader instanceof URLsLoader) {
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
	 * Returns the collection of existing urls.
	 * 
	 * @return See above.
	 */
	Collection getExistingURLs() { return existingURLs; }
	
	/**
	 * Sets the channel data.
	 * 
	 * @param data The value to set.
	 */
	void setChannelData(List data) { emissionsWavelengths = data; }
	
	/**
	 * Returns the channels data related to the image.
	 * 
	 * @return See above.
	 */
	List getChannelData() { return emissionsWavelengths; }

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
		if (refObject instanceof DataObject)
			parent.saveData(toAdd, toRemove, metadata, (DataObject) refObject);
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
		StructuredDataResults data = parent.getStructuredData();
		if (data == null) return false;
		return data.isArchived(); 
	}

	/** Starts an asynchronous loading. */
	void download()
	{
		PixelsData data = ((ImageData) refObject).getDefaultPixels();
		OriginalFileLoader loader = new OriginalFileLoader(component, 
											data.getId());
		loader.load();
		loaders.add(loader);
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
		if (!(refObject instanceof TagAnnotationData)) return false;
		TagAnnotationData tag = (TagAnnotationData) refObject;
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
	void  fireImagAcquisitionDataLoading()
	{
		AcquisitionDataLoader 
			loader = new AcquisitionDataLoader(component, getRefObject()); 
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
		channelAcquisitionDatMap.put(index, data);
	}
	/**
	 * Returns the channel acquisition data.
	 * 
	 * @param index The index of the channels.
	 * @return See above.
	 */
	ChannelAcquisitionData getChannelAcquisitionData(int index)
	{ 
		return channelAcquisitionDatMap.get(index);
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
		List<EnumerationObject> l = getChannelEnumerations(name);
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
	 * Sets the enumerations related to channel metadata.
	 * 
	 * @param channelEnumerations The value to set.
	 */
	void setChannelEnumerations(Map channelEnumerations)
	{
		this.channelEnumerations = channelEnumerations;
	}

	/**
	 * Sets the enumerations related to image metadata.
	 * 
	 * @param imageEnumerations The value to set.
	 */
	void setImageEnumerations(Map imageEnumerations)
	{
		this.imageEnumerations = imageEnumerations;
	}
	
}
