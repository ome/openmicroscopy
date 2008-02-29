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
import org.openmicroscopy.shoola.agents.metadata.EditorLoader;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.metadata.ThumbnailLoader;
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewer;
import org.openmicroscopy.shoola.env.data.util.StructuredDataResults;
import org.openmicroscopy.shoola.env.data.util.ViewedByDef;

import pojos.AnnotationData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.ImageData;
import pojos.PermissionData;
import pojos.ProjectData;
import pojos.RatingAnnotationData;
import pojos.URLAnnotationData;

/** 
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

	private StructuredDataResults	data;
	
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
	}
	
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
	 * Returns <code>true</code> if the currently logged in user is the 
	 * owner of the object, <code>false</code> otherwise.
	 * 
	 * @param object The object to handle.
	 * @return See above.
	 */
	boolean isCurrentUserOwner(Object object)
	{
		return true;
		/*
		if (object == null) return false;
		if (object instanceof DataObject) {
			
			long userID = MetadataViewerAgent.getUserDetails().getId();
			return userID == object.getOwner().getId();
		}
		return false;
		*/
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
			ExperimenterData exp = (ExperimenterData) object;
			return exp.getFirstName()+" "+exp.getLastName();
		}
		return "Jean-marie";
	}
	
	/**
	 * Returns the first name and the last name of the owner.
	 * 
	 * @param object The object to handle.
	 * @return See above.
	 */
	String formatDate(DataObject object)
	{
		if (object == null) return "";
		if (object instanceof AnnotationData)
			return "";
			//return UIUtilities.formatDateTime(
			//		((AnnotationData) object).getLastModified());
		return "";
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
	Collection getUrls() { 
		ArrayList l = new ArrayList();
		l.add(new URLAnnotationData("http://www.google.co.uk/"));
		return l;
		//return data.getUrls(); 
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
	Collection getTags() { return data.getTags(); }
	
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
	Collection getAttachments() { return data.getAttachments(); }

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
	Collection getViewedBy() { return data.getViewedBy(); }
	
	/**
	 * Returns the number of ratings for that object.
	 * 
	 * @return See above.
	 */
	int getRatingCount()
	{
		Collection ratings = data.getRatings();
		if (ratings == null) return 0;
		return ratings.size();
	}
	
	/** 
	 * Returns the average rating value.
	 * 
	 * @return See above.
	 */
	int getRatingAverage() 
	{
		Collection ratings = data.getRatings();
		if (ratings == null) return 0;
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
		return data.getTextualAnnotations();
	}
	
	List getTextualAnnotationsByDate()
	{
		if (textualAnnotationsByDate != null)
			return textualAnnotationsByDate;
		textualAnnotationsByDate = (List) getTextualAnnotations();
		sortAnnotationByDate(textualAnnotationsByDate);
		return textualAnnotationsByDate;
	}
	
	String formatToolTip(DataObject object)
	{
		return formatOwner(object)+", "+formatDate(object);
	}
	
	/**
	 * Returns the annotations organized by users.
	 * 
	 * @return See above.
	 */
	Map getTextualAnnotationByOwner()
	{
		if (textualAnnotationsByUsers != null)
			return textualAnnotationsByUsers;
		textualAnnotationsByUsers = new HashMap<Long, List>();
        Iterator i = getTextualAnnotations().iterator();
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
        List annotations;
        while (i.hasNext()) {
            ownerID = (Long) i.next();
            annotations = textualAnnotationsByUsers.get(ownerID);
            sortAnnotationByDate(annotations);
        }
        return textualAnnotationsByUsers;
	}
	
	/**
	 * Sets the structured data.
	 * 
	 * @param data The value to set.
	 */
	void setStructuredDataResults(StructuredDataResults data)
	{
		this.data = data;
	}

	/** 
	 * Sets the object of reference.
	 * 
	 * @param refObject The value to set.
	 */
	void setRootObject(Object refObject) { this.refObject = refObject; }

	/**
	 * Returns the owner of the ref object or <code>null</code>
	 * if the object is not a <code>DataObject</code>.
	 * 
	 * @return See above.
	 */
	ExperimenterData getRefObjectOwner()
	{
		if (refObject instanceof DataObject)
			return ((DataObject) refObject).getOwner();
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
		Set<Long> l = new HashSet<Long>();
		l.add(MetadataViewerAgent.getUserDetails().getId());
		ThumbnailLoader loader = new ThumbnailLoader(component, 
								(ImageData) refObject, l);
		loader.load();
		loaders.add(loader);
		
	}

	/** Cancels any ongoing thumbnails retrieval. */
	void cancelThumbnailsLoading()
	{
		Iterator i = loaders.iterator();
		EditorLoader loader;
		while (i.hasNext()) {
			loader = (EditorLoader) i.next();
			if (loader instanceof ThumbnailLoader)
				loader.cancel();
		}
	}
	
	ViewedByDef getViewedDef(long id)
	{
		Collection l = getViewedBy();
		Iterator i = l.iterator();
		ViewedByDef def = null;
		while (i.hasNext()) {
			def = (ViewedByDef) i.next();
			if (def.getExperimenter().getId() == id)
				return def;
		}
		return null;
	}
	
}
