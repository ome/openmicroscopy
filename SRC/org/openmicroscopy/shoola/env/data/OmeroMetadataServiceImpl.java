/*
 * org.openmicroscopy.shoola.env.data.OmeroMetadataServiceImpl 
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
package org.openmicroscopy.shoola.env.data;



//Java imports
import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries
import org.apache.commons.collections.ListUtils;

//Application-internal dependencies
import ome.model.ILink;
import ome.model.IObject;
import ome.model.annotations.Annotation;
import ome.model.annotations.AnnotationAnnotationLink;
import ome.model.annotations.FileAnnotation;
import ome.model.annotations.TagAnnotation;
import ome.model.core.OriginalFile;
import ome.util.builders.PojoOptions;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.util.FilterContext;
import org.openmicroscopy.shoola.env.data.util.ModelMapper;
import org.openmicroscopy.shoola.env.data.util.PojoMapper;
import org.openmicroscopy.shoola.env.data.util.StructuredDataResults;
import org.openmicroscopy.shoola.env.data.util.ViewedByDef;
import org.openmicroscopy.shoola.env.log.LogMessage;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;
import pojos.AnnotationData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.ImageData;
import pojos.RatingAnnotationData;
import pojos.TagAnnotationData;
import pojos.TextualAnnotationData;
import pojos.URLAnnotationData;

/** 
 * Implementation of the {@link OmeroMetadataService} I/F.
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
class OmeroMetadataServiceImpl 
	implements OmeroMetadataService
{

	/** Uses it to gain access to the container's services. */
	private Registry                context;

	/** Reference to the entry point to access the <i>OMERO</i> services. */
	private OMEROGateway            gateway;

	/**
	 * Returns the current user's details.
	 * 
	 * @return See above.
	 */
	private ExperimenterData getUserDetails()
	{ 
		return (ExperimenterData) context.lookup(
									LookupNames.CURRENT_USER_DETAILS);
	}
	
	/**
	 * Updates the passed annotation.
	 * 
	 * @param ann The annotation to update.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMEDS service.
	 */
	private void updateAnnotationData(DataObject ann)
		throws DSOutOfServiceException, DSAccessException
	{
		long id;
		Class ioType;
		IObject ho;
		IObject link = null;
		
		if (ann instanceof TagAnnotationData) {
			TagAnnotationData tag = (TagAnnotationData) ann;
			TextualAnnotationData description = tag.getTagDescription();
			if (description != null) {
				id = tag.getId();
				if (id >= 0) {
					/*
					List links = gateway.findAnnotationLinks(Annotation.class, 
							                      id, null);
					if (links != null) {
						Iterator i = links.iterator();
						long userID = getUserDetails().getId();
						AnnotationAnnotationLink aaLink;
						List<IObject> toRemove = new ArrayList<IObject>(); 
						while (i.hasNext()) {
							aaLink = (AnnotationAnnotationLink) i.next();
							ho = aaLink.getChild();
							if (ho.getDetails().getOwner().getId() == userID) {
								toRemove.add(ho);
								gateway.deleteObject(aaLink);
							}
						}
						i = toRemove.iterator();
						while (i.hasNext()) {
							gateway.deleteObject((IObject) i.next());
						}
					}
					*/
					gateway.removeTagDescription(id, getUserDetails().getId());
				}	
				ioType = gateway.convertPojos(TagAnnotationData.class);
				ho = gateway.findIObject(ioType, id);
				link = ModelMapper.createAnnotation(ho, description);
				if (link != null) 
					gateway.createObject(link, (new PojoOptions()).map());
			}
		}
		
	}
	
	/**
	 * Clears the passed annotations linked to the annotation 
	 * specifed by the id.
	 * 
	 * @param parentID				The id of the parent annotation.
	 * @param childrenAnnotations	The collection of linked annotations.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMEDS service. 
	 */
	private void clearAnnotationAnnotationLink(long parentID, 
			                                 List childrenAnnotations)
		throws DSOutOfServiceException, DSAccessException 
	{
		if (childrenAnnotations == null || 
				childrenAnnotations.size() == 0) return;
		Iterator i = childrenAnnotations.iterator();
		List<Long> ids = new ArrayList<Long>();
		List<IObject> toRemove = new ArrayList<IObject>(); 
		long userID = getUserDetails().getId();
		AnnotationData data;
		while (i.hasNext()) {
			data = (AnnotationData) i.next();
			if (data.getOwner().getId() == userID) {
				ids.add(data.getId());
				toRemove.add(data.asIObject());
			}
		}
		List l = null;
		if (ids.size() != 0)
			l = gateway.findAnnotationLinks(Annotation.class, parentID, ids);
		if (l != null) {
			i = l.iterator();
			while (i.hasNext()) {
				gateway.deleteObject((IObject) i.next());
			}
			i = toRemove.iterator();
			while (i.hasNext()) {
				gateway.deleteObject((IObject) i.next());
			}
		}
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param gateway   Reference to the OMERO entry point.
	 *                  Mustn't be <code>null</code>.
	 * @param registry  Reference to the registry. Mustn't be <code>null</code>.
	 */
	OmeroMetadataServiceImpl(OMEROGateway gateway, Registry registry)
	{
		if (registry == null)
			throw new IllegalArgumentException("No registry.");
		if (gateway == null)
			throw new IllegalArgumentException("No gateway.");
		context = registry;
		this.gateway = gateway;
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#loadAttachments(Class, long, long)
	 */
	public Collection loadAttachments(Class type, long id, long userID) 
		throws DSOutOfServiceException, DSAccessException
	{
		Collection annotations = loadStructuredAnnotations(type, id, userID);
		List<AnnotationData> result = new ArrayList<AnnotationData>();
		if (annotations == null || annotations.size() == 0)
			return result;
		Iterator i = annotations.iterator();
		AnnotationData data;
		FileAnnotation fa;
		long fileID;
		while (i.hasNext()) {
			data = (AnnotationData) i.next();
			if (data instanceof FileAnnotationData) {
					fa = (FileAnnotation) data.asAnnotation();
					fileID = fa.getFile().getId();
					((FileAnnotationData) data).setContent(
							gateway.getOriginalFile(fileID));
					result.add(data);
			}
		}
		return result;
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#loadTags(Class, long, long)
	 */
	public Collection loadTags(Class type, long id, long userID) 
		throws DSOutOfServiceException, DSAccessException
	{
		Collection annotations = loadStructuredAnnotations(type, id, userID);
		List<AnnotationData> result = new ArrayList<AnnotationData>();
		if (annotations == null || annotations.size() == 0)
			return result;
		Iterator i = annotations.iterator();
		AnnotationData data;
		while (i.hasNext()) {
			data = (AnnotationData) i.next();
			if (data instanceof TagAnnotationData)
				result.add(data);
		}
		return result;
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#loadRatings(Class, long, long)
	 */
	public Collection loadRatings(Class type, long id, long userID) 
		throws DSOutOfServiceException, DSAccessException
	{
		Collection annotations = loadStructuredAnnotations(type, id, userID);
		List<AnnotationData> result = new ArrayList<AnnotationData>();
		if (annotations == null || annotations.size() == 0)
			return result;
		Iterator i = annotations.iterator();
		AnnotationData data;
		while (i.hasNext()) {
			data = (AnnotationData) i.next();
			if (data instanceof RatingAnnotationData)
				result.add(data);
		}
		return result;
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#loadUrls(Class, long, long)
	 */
	public Collection loadUrls(Class type, long id, long userID) 
		throws DSOutOfServiceException, DSAccessException
	{
		Collection annotations = loadStructuredAnnotations(type, id, userID);
		List<AnnotationData> result = new ArrayList<AnnotationData>();
		if (annotations == null || annotations.size() == 0)
			return result;
		Iterator i = annotations.iterator();
		AnnotationData data;
		while (i.hasNext()) {
			data = (AnnotationData) i.next();
			if (data instanceof URLAnnotationData)
				result.add(data);
		}
		return result;
	}

	private TagAnnotationData loadTagDescription(TagAnnotationData tag, 
			                                    long userID)
	    throws DSOutOfServiceException, DSAccessException 
	{
		Collection descriptions;
		descriptions = loadTextualAnnotations(TagAnnotationData.class, 
				                              tag.getId(), userID);
		if (descriptions != null && descriptions.size() > 0)
		tag.setTagDescriptions((List) descriptions);
		return tag;
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#loadStructuredData(DataObject, long)
	 */
	public StructuredDataResults loadStructuredData(DataObject object, 
			                                        long userID) 
	    throws DSOutOfServiceException, DSAccessException 
	{
		if (object == null)
			throw new IllegalArgumentException("Object not valid.");
		StructuredDataResults results = new StructuredDataResults(object);
		Collection annotations = loadStructuredAnnotations(object.getClass(),
													object.getId(), userID);
		if (annotations != null && annotations.size() > 0) {
			List<AnnotationData> texts = new ArrayList<AnnotationData>();
			List<AnnotationData> tags = new ArrayList<AnnotationData>();
			List<AnnotationData> urls = new ArrayList<AnnotationData>();
			List<AnnotationData> attachments = new ArrayList<AnnotationData>();
			List<AnnotationData> ratings = new ArrayList<AnnotationData>();
			Iterator i = annotations.iterator();
			AnnotationData data;
			long id;
			TagAnnotationData tag;
			FileAnnotation fa;
			boolean isChild = true;
			while (i.hasNext()) {
				data = (AnnotationData) i.next();
				if (data instanceof URLAnnotationData)
					urls.add(data);
				else if (data instanceof TextualAnnotationData)
					texts.add(data);
				else if ((data instanceof TagAnnotationData)) {
					System.err.println(data.getContentAsString());
					if (!(object instanceof TagAnnotationData)) {
						tag = loadTagDescription((TagAnnotationData) data, 
				                userID);
						tags.add(tag);
					} else isChild = false;
					
					//need to load the description
				} else if (data instanceof RatingAnnotationData)
					ratings.add(data);
				else if (data instanceof FileAnnotationData) {
					fa = (FileAnnotation) data.asAnnotation();
					id = fa.getFile().getId();
					((FileAnnotationData) data).setContent(
							gateway.getOriginalFile(id));
					attachments.add(data);
				}
			}
			if ((object instanceof TagAnnotationData) && isChild) {
				List<Long> ids = new ArrayList<Long>();
				ids.add(object.getId());
				Collection r = gateway.fetchAnnotations(ids, userID, true);
				if (r != null) {
					i = r.iterator();
					while (i.hasNext()) {
						data = (AnnotationData) i.next();
						if (data instanceof TagAnnotationData) {
							tag = loadTagDescription((TagAnnotationData) data, 
					                userID);
			                tags.add(tag);
						}
					}
				}
			}
			results.setTextualAnnotations(texts);
			results.setUrls(urls);
			results.setTags(tags);
			results.setRatings(ratings);
			results.setAttachments(attachments);
		}
		OmeroDataService os = context.getDataService();
		results.setParents(os.findContainerPaths(object.getClass(),
				object.getId(), userID));
		if (object instanceof ImageData) {
			ImageData img = (ImageData) object;
			try {
				results.setArchived(gateway.hasArchivedFiles(
						img.getDefaultPixels().getId()));
			} catch (Exception e) {
				String s = "Data Retrieval Failure: ";
				LogMessage msg = new LogMessage();
		        msg.print(s);
		        msg.print(e);
		        context.getLogger().error(this, msg);
		        results.setArchived(false);
			}
			
			results.setViewedBy(loadViewedBy(img.getId(), 
								img.getDefaultPixels().getId()));
		}
		return results;
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#annotate(DataObject, AnnotationData)
	 */
	public DataObject annotate(DataObject toAnnotate, AnnotationData annotation)
		throws DSOutOfServiceException, DSAccessException
	{
		if (toAnnotate == null)
			throw new IllegalArgumentException("DataObject cannot be null");
		return annotate(toAnnotate.getClass(), toAnnotate.getId(), annotation);
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#annotate(Class, long, AnnotationData)
	 */
	public DataObject annotate(Class type, long id, AnnotationData annotation) 
		throws DSOutOfServiceException, DSAccessException 
	{
		if (annotation == null)
			throw new IllegalArgumentException("DataObject cannot be null");
		long userID = getUserDetails().getId();
		Class ioType = gateway.convertPojos(type);
		IObject ho = gateway.findIObject(ioType, id);
		ILink link = null;
		boolean exist = false;
		if (annotation instanceof TagAnnotationData) {
			TagAnnotationData tag = (TagAnnotationData) annotation;
			//tag a tag
			if (TagAnnotationData.class.equals(type)) {
				if (tag.getId() < 0) {
					TagAnnotation ann = new TagAnnotation();
		    		ann.setTextValue(tag.getContentAsString());
		    		link = ModelMapper.linkAnnotation(ann, ho);
				} else {
					link = (ILink) gateway.findAnnotationLink(
							AnnotationData.class, tag.getId(), ho.getId());
					if (link == null) 
						link = ModelMapper.linkAnnotation(tag.asIObject(), ho);
				}
				
			} else {
				if (tag.getId() < 0) 
					link = ModelMapper.createAnnotation(ho, annotation);
				else {
					link = (ILink) gateway.findAnnotationLink(ho.getClass(), 
														ho.getId(), tag.getId());
					if (link == null)
						link = ModelMapper.linkAnnotation(ho, tag.asIObject());
					else exist = true;
				}
			}
			
				
		} else if (annotation instanceof RatingAnnotationData) {
			//only one annotation of type rating.
			//Remove the previous ones.
			clearAnnotation(type, id, RatingAnnotationData.class);
			int rate = ((RatingAnnotationData) annotation).getRating();
			if (rate != RatingAnnotationData.LEVEL_ZERO)
				link = ModelMapper.createAnnotation(ho, annotation);
		} else if (annotation instanceof FileAnnotationData) {
			FileAnnotationData ann = (FileAnnotationData) annotation;
			if (ann.getId() < 0) {
				OriginalFile of = gateway.uploadFile(ann.getAttachedFile(), 
						ann.getServerFileFormat());
				FileAnnotation fa = new FileAnnotation();
				fa.setFile(of);
				link = ModelMapper.linkAnnotation(ho, fa);
			} else {
				link = ModelMapper.linkAnnotation(ho, ann.asIObject());
			}
			
		} else
			link = ModelMapper.createAnnotation(ho, annotation);
		if (link != null) {
			Map map = (new PojoOptions()).map();
			IObject object;
			if (exist) object = link;
			else object = gateway.createObject(link, map);
			if (annotation instanceof TagAnnotationData) {
				//Add description to the object.
				TagAnnotationData tag = (TagAnnotationData) annotation;
				TextualAnnotationData description = tag.getTagDescription();
				if (description != null) {
					id = tag.getId();
					if (id >= 0) gateway.removeTagDescription(id, userID);
					 	
					link = ModelMapper.createAnnotation(
							ModelMapper.getAnnotationObject(object), 
													description);
				
					if (link != null) gateway.createObject(link, map);
				}
			}

			return PojoMapper.asDataObject(
								ModelMapper.getAnnotatedObject(object));
		}
		
		return null;
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#annotate(Set, AnnotationData)
	 */
	public List<DataObject> annotate(Set<DataObject> toAnnotate, 
									AnnotationData annotation) 
			throws DSOutOfServiceException, DSAccessException
	{
		if (annotation == null)
			throw new IllegalArgumentException("Annotation cannot be null");
		return null;
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#clearAnnotation(Class, long, Class)
	 */
	public void clearAnnotation(Class type, long id, Class annotationType)
		throws DSOutOfServiceException, DSAccessException
	{
		if (type == null)
			throw new IllegalArgumentException("No object specified.");
		long userID = getUserDetails().getId();
		Collection annotations = loadStructuredAnnotations(type, id, userID);
		if (annotations == null || annotations.size() == 0)
			return;
		List<IObject> toRemove = new ArrayList<IObject>(); 
		List<Long> ids = new ArrayList<Long>(); 
		Iterator i = annotations.iterator();
		AnnotationData data;
		while (i.hasNext()) {
			data = (AnnotationData) i.next();
			if (annotationType == null || 
					data.getClass().equals(annotationType)) {
				toRemove.add(data.asIObject());
				ids.add(data.getId());
			} 
		}
		List l = null;
		if (ids.size() != 0)
			l = gateway.findAnnotationLinks(gateway.convertPojos(type), id, 
											ids);
		if (l != null) {
			i = l.iterator();
			while (i.hasNext()) {
				gateway.deleteObject((IObject) i.next());
			}
			i = toRemove.iterator();
			while (i.hasNext()) {
				gateway.deleteObject((IObject) i.next());
			}
		}
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#clearAnnotation(DataObject, Class)
	 */
	public void clearAnnotation(DataObject object, Class annotationType) 
		throws DSOutOfServiceException, DSAccessException
	{
		if (object == null)
			throw new IllegalArgumentException("No object specified.");
		clearAnnotation(object.getClass(), object.getId(), annotationType);
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#clearAnnotation(DataObject)
	 */
	public void clearAnnotation(DataObject object) 
		throws DSOutOfServiceException, DSAccessException
	{
		if (object == null)
			throw new IllegalArgumentException("No object specified.");
		clearAnnotation(object.getClass(), object.getId(), null);
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#removeAnnotation(AnnotationData, DataObject)
	 */
	public DataObject removeAnnotation(AnnotationData annotation, 
										DataObject object) 
		throws DSOutOfServiceException, DSAccessException 
	{
		if (annotation == null)
			throw new IllegalArgumentException("No annotation to remove.");
		if (object == null)
			throw new IllegalArgumentException("No objec to handle.");
		IObject ho = gateway.findIObject(annotation.asIObject());
		IObject link = gateway.findAnnotationLink(object.getClass(), 
				                         object.getId(), ho.getId());
		if (ho != null && link != null) {
			gateway.deleteObject(link);
			gateway.deleteObject(ho);
		}
		return PojoMapper.asDataObject(gateway.findIObject(object.asIObject()));
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#removeAnnotation(AnnotationData, Set)
	 */
	public List<DataObject> removeAnnotation(AnnotationData annotation, 
											Set<DataObject> objects) 
			throws DSOutOfServiceException, DSAccessException
	{
		if (annotation == null)
			throw new IllegalArgumentException("No annotation to remove.");
		if (objects == null || objects.size() == 0)
			throw new IllegalArgumentException("No object to handle.");
		IObject ho = gateway.findIObject(annotation.asIObject());
		List<IObject> links = new ArrayList<IObject>();
		Iterator i = objects.iterator();
		DataObject object;
		while (i.hasNext()) {
			object = (DataObject) i.next();
			links.add(gateway.findAnnotationLink(object.getClass(), 
					                   object.getId(), ho.getId()));
		}
		i = links.iterator();
		while (i.hasNext()) {
			gateway.deleteObject((IObject) i.next());
		}
		gateway.deleteObject(ho);
		return null;
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#loadTextualAnnotations(Class, long, long)
	 */
	public Collection loadTextualAnnotations(Class type, long id, long userID) 
		throws DSOutOfServiceException, DSAccessException
	{
		Collection annotations = loadStructuredAnnotations(type, id, userID);
		if (annotations == null || annotations.size() == 0)
			return annotations;
		Iterator i = annotations.iterator();
		List<AnnotationData> result = new ArrayList<AnnotationData>();
		AnnotationData data;
		while (i.hasNext()) {
			data = (AnnotationData) i.next();
			if (data instanceof TextualAnnotationData)
				result.add(data);
		}
		return result;
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#loadStructuredAnnotations(Class, long, long)
	 */
	public Collection loadStructuredAnnotations(Class type, long id, 
			                                    long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		if (id < 0)
			throw new IllegalArgumentException("Object id not valid.");
		PojoOptions po = new PojoOptions();
		po.noLeaves();
		Set<Long> ids = null;
		if (userID != -1) {
			ids = new HashSet<Long>(1);
			ids.add(userID);
		}
		Set<Long> objects = new HashSet<Long>(1);
		objects.add(id);
		Map map = gateway.findAnnotations(type, objects, ids, 
										new PojoOptions().map());
		return (Collection) map.get(id);
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#loadViewedBy(long, long)
	 */
	public Collection loadViewedBy(long imageID, long pixelsID) 
		throws DSOutOfServiceException, DSAccessException
	{
		OmeroImageService rds = context.getImageService();
		Map settings = rds.getRenderingSettings(pixelsID);
		List<ViewedByDef> list = new ArrayList<ViewedByDef>();
		if (settings != null) {
			 Iterator i = settings.keySet().iterator();
			 OmeroMetadataService os = context.getMetadataService();
			 ExperimenterData exp;
			 ViewedByDef def;
			 while (i.hasNext()) {
				 exp = (ExperimenterData) i.next();
				 def = new ViewedByDef(exp, 
						 	(RndProxyDef) settings.get(exp), 
						 	os.loadRatings(ImageData.class, 
			 				imageID, exp.getId()));
				 def.setIds(imageID, pixelsID);
				 list.add(def);
			}
		}
		return list;
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#loadAnnotations(Class, Class, long)
	 */
	public Collection loadAnnotations(Class annotationType, Class objectType, 
			                         long objectID, long userID) 
		throws DSOutOfServiceException, DSAccessException
	{
		Collection c = gateway.fetchAnnotation(objectID, userID);
		List<AnnotationData> annotations = new ArrayList<AnnotationData>();
		if (c == null || c.size() == 0) return annotations;
		Iterator i = c.iterator();
		AnnotationData data;
		long id;
		FileAnnotation fa;
		while (i.hasNext()) {
			data = (AnnotationData) i.next();
			if (annotationType.equals(data.getClass())) {
				if (data instanceof FileAnnotationData) {
					fa = (FileAnnotation) data.asAnnotation();
					id = fa.getFile().getId();
					((FileAnnotationData) data).setContent(
							gateway.getOriginalFile(id));
				}
				annotations.add(data);
			}
		}	
		return annotations;
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#saveData(Collection, List, List, long)
	 */
	public Object saveData(Collection<DataObject> data, 
			        List<AnnotationData> toAdd, List<AnnotationData> toRemove, 
			        long userID) 
			throws DSOutOfServiceException, DSAccessException
	{
		if (data == null)
			throw new IllegalArgumentException("No data to save");
		OmeroDataService service = context.getDataService();
		Iterator i;
		Iterator<DataObject> j = data.iterator();
		DataObject object;
		while (j.hasNext()) {
			object = j.next();
			if (object instanceof AnnotationData) {
				updateAnnotationData(object);
			} else service.updateDataObject(object);
			if (toAdd != null) {
				i = toAdd.iterator();
				while (i.hasNext())
					annotate(object, (AnnotationData) i.next());
			}
			if (toRemove != null) {
				i = toRemove.iterator();
				while (i.hasNext())
					removeAnnotation((AnnotationData) i.next(), object);
			}
		}
		
		return data;
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#saveBatchData(Collection, List, List, long)
	 */
	public Object saveBatchData(Collection<DataObject> data, 
				List<AnnotationData> toAdd, List<AnnotationData> toRemove, 
				long userID) 
			throws DSOutOfServiceException, DSAccessException
	{
		if (data == null)
			throw new IllegalArgumentException("No data to save");
		OmeroDataService service = context.getDataService();
		Iterator i;
		Iterator<DataObject> j = data.iterator();
		DataObject object, child;
		Set<Long> ids;
		Set images = null;
		PojoOptions po = new PojoOptions();
		po.allExps();
		Map m = po.map();
		Iterator k;
		List result = null;
		while (j.hasNext()) {
			object = j.next();
			if (result == null) result = new ArrayList();
			if (object instanceof DatasetData) {
				//retrieve all images in the dataset.
				//Tmp solution, this code should be pushed server side.
				ids = new HashSet<Long>(1);
				ids.add(object.getId());
				images = gateway.getContainerImages(DatasetData.class, ids, m);
				if (images != null) {
					k = images.iterator();
					while (k.hasNext()) {
						child = (DataObject) k.next();
						result.add(child);
						if (toAdd != null) {
							i = toAdd.iterator();
							while (i.hasNext())
								annotate(child, (AnnotationData) i.next());
						}
						if (toRemove != null) {
							i = toRemove.iterator();
							while (i.hasNext())
								removeAnnotation((AnnotationData) i.next(), 
													child);
						}
					}
				}
			} else if (object instanceof ImageData) {
				service.updateDataObject(object);
				if (toAdd != null) {
					i = toAdd.iterator();
					while (i.hasNext())
						annotate(object, (AnnotationData) i.next());
				}
				if (toRemove != null) {
					i = toRemove.iterator();
					while (i.hasNext())
						removeAnnotation((AnnotationData) i.next(), object);
				}
			}
		}
		if (result == null) return data;
		return result;
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#downloadFile(String, long, int)
	 */
	public File downloadFile(File file, long fileID, long size) 
		throws DSOutOfServiceException, DSAccessException
	{
		if (fileID < 0)
			throw new IllegalArgumentException("File ID not valid");
		if (file == null)
			throw new IllegalArgumentException("File path not valid");
		return gateway.downloadFile(file, fileID, size);
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#loadRatings(Class, Set, long)
	 */
	public Map<Long, Collection> loadRatings(Class nodeType, 
											Set<Long> nodeIds, long userID) 
			throws DSOutOfServiceException, DSAccessException
	{
		PojoOptions po = new PojoOptions();
		po.noLeaves();
		Set<Long> ids = null;
		if (userID != -1) {
			ids = new HashSet<Long>(1);
			ids.add(userID);
		}
		Map map = gateway.findAnnotations(nodeType, nodeIds, ids, 
				new PojoOptions().map());
		Map<Long, Collection> results = new HashMap<Long, Collection>();
		if (map == null) return results;
		
		Iterator<Long> i = map.keySet().iterator();
		Long id;
		AnnotationData data;
		Iterator j;
		List<AnnotationData> result;
		Collection l;
		while (i.hasNext()) {
			id = i.next();
			l = (Collection) map.get(id);
			result = new ArrayList<AnnotationData>();
			j = l.iterator();
			while (j.hasNext()) {
				data = (AnnotationData) j.next();
				if (data instanceof RatingAnnotationData)
					result.add(data);
			}
			if (result.size() > 0)
				results.put(id, result);
		}
		return results;
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#filterByAnnotation(Class, Set, Class, List, 
	 * 												long)
	 */
	public Collection filterByAnnotation(Class nodeType, Set<Long> nodeIds, 
		Class annotationType, List<String> terms, long userID) 
		throws DSOutOfServiceException, DSAccessException
	{
		List<Long> results = new ArrayList<Long>();
		
		PojoOptions po = new PojoOptions();
		po.noLeaves();
		Set<Long> ids = null;
		if (userID != -1) {
			ids = new HashSet<Long>(1);
			ids.add(userID);
		}
		
		Map map = gateway.findAnnotations(nodeType, nodeIds, ids, 
				new PojoOptions().map());
		if (map == null || map.size() == 0) return results;
		ExperimenterData exp = getUserDetails();
		long id;
		Collection l;
		AnnotationData data;
		Iterator i, j;
		if (terms != null && terms.size() > 0) {
			List annotations = gateway.filterBy(annotationType, terms, null, 
												null, exp);
			List<Long> annotationsIds = new ArrayList<Long>();
			
			i = annotations.iterator();
			while (i.hasNext())
				annotationsIds.add(((Annotation) i.next()).getId());
			
			i = map.keySet().iterator();
			while (i.hasNext()) {
				id = (Long) i.next();
				l = (Collection) map.get(id);
				j = l.iterator();
				while (j.hasNext()) {
					data = (AnnotationData) j.next();
					if (data.getClass().equals(annotationType) && 
					annotationsIds.contains(data.getId()))
						results.add(id);
				}
			}
		} else { 
			// retrieve the objects not annotated by the specifed type.
			i = map.keySet().iterator();
			results.addAll(nodeIds);
			while (i.hasNext()) {
				id = (Long) i.next();
				l = (Collection) map.get(id);
				j = l.iterator();
				while (j.hasNext()) {
					data = (AnnotationData) j.next();
					if (data.getClass().equals(annotationType))
						results.remove(id);
				}
			}
		}
		
		return results;
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#filterByAnnotation(Class, Set, FilterContext, 
	 * 												long)
	 */
	public Collection filterByAnnotation(Class nodeType, Set<Long> ids, 
			FilterContext filter, long userID) 
		throws DSOutOfServiceException, DSAccessException
	{
		if (filter == null)
			throw new IllegalArgumentException("No filtering context.");
		int rateIndex = filter.getIndex();
		List<Long> filteredNodes = new ArrayList<Long>();

		PojoOptions po = new PojoOptions();
		po.noLeaves();
		Set<Long> userIDs = null;
		if (userID != -1) {
			userIDs = new HashSet<Long>(1);
			userIDs.add(userID);
		}
		
		Map map = gateway.findAnnotations(nodeType, ids, userIDs, 
				new PojoOptions().map());
		if (map == null || map.size() == 0) {
			if (rateIndex == FilterContext.EQUAL && filter.getRate() == 0)
				return ids;
		}
		
		//TODO: retrieve the experimenter corresponding to the passed id.
		ExperimenterData exp = getUserDetails();
		
		Timestamp start = filter.getFromDate();
		Timestamp end = filter.getToDate();
		Set<Long> annotationsIds = new HashSet<Long>();
		Iterator i, j;
		Long id;
		Collection l;
		List annotations;
		int resultType = filter.getResultType();
		Map<Class, List<String>> types = filter.getAnnotationType();
		
		Map<Class, List<Long>> r = new HashMap<Class, List<Long>>();
		List<Long> found;
		Class type;
		if (types != null && types.size() > 0) {
			i = types.keySet().iterator();
			
			AnnotationData data;
			if (resultType == FilterContext.INTERSECTION) {
				
				while (i.hasNext()) {
					type = (Class) i.next();
					found = new ArrayList<Long>();
					annotations = gateway.filterBy(type, types.get(type), 
												start, end, exp);
					i = annotations.iterator();
					while (i.hasNext())
						annotationsIds.add(((Annotation) i.next()).getId());
					
					i = map.keySet().iterator();
					
					while (i.hasNext()) {
						id = (Long) i.next();
						l = (Collection) map.get(id);
						j = l.iterator();
						while (j.hasNext()) {
							data = (AnnotationData) j.next();
							if (annotationsIds.contains(data.getId())) {
								found.add(id);
							}
						}
					}
					r.put(type, found);
				}
			} else if (resultType == FilterContext.UNION) {
				while (i.hasNext()) {
					type = (Class) i.next();
					annotations = gateway.filterBy(type, types.get(type), 
												start, end, exp);
					i = annotations.iterator();
					while (i.hasNext())
						annotationsIds.add(((Annotation) i.next()).getId());
				}

				i = map.keySet().iterator();
				
				while (i.hasNext()) {
					id = (Long) i.next();
					l = (Collection) map.get(id);
					j = l.iterator();
					while (j.hasNext()) {
						data = (AnnotationData) j.next();
						if (annotationsIds.contains(data.getId()))
							filteredNodes.add(id);
					}
				}
			}
		}

		if (rateIndex != -1) {
			int rate = filter.getRate();
			int value;
			i = map.keySet().iterator();
			AnnotationData data;
			found = new ArrayList<Long>();
			switch (rateIndex) {
				case FilterContext.EQUAL:
					if (rate == 0) { //unrated element.
						found.addAll(ids);
						while (i.hasNext()) 
							found.remove(i.next());
					} else {
						
						while (i.hasNext()) {
			    			id = (Long) i.next();
			    			l = (Collection) map.get(id);
			    			j = l.iterator();
			    			while (j.hasNext()) {
								data = (AnnotationData) j.next();
								if (data instanceof RatingAnnotationData) {
									value = ((RatingAnnotationData) 
			    							data).getRating();
									if (rate == value) 
										found.add(id);
								}
							}
			    		}
					}
					break;
				case FilterContext.LOWER:
					if (rate == 0) { //unrated element.
						found.addAll(ids);
						
						while (i.hasNext()) 
							found.remove(i.next());
					} else {
						while (i.hasNext()) {
			    			id = (Long) i.next();
			    			l = (Collection) map.get(id);
			    			j = l.iterator();
			    			while (j.hasNext()) {
								data = (AnnotationData) j.next();
								if (data instanceof RatingAnnotationData) {
									value = ((RatingAnnotationData) 
			    							data).getRating();
									if (value <= rate) found.add(id);
								}
							}
			    		}
					}
					break;
				case FilterContext.HIGHER:
					while (i.hasNext()) {
		    			id = (Long) i.next();
		    			l = (Collection) map.get(id);
		    			j = l.iterator();
		    			while (j.hasNext()) {
							data = (AnnotationData) j.next();
							if (data instanceof RatingAnnotationData) {
								value = ((RatingAnnotationData) 
		    							data).getRating();
								if (value >= rate) found.add(id);
							}
						}
		    		}
			}
			if (resultType == FilterContext.UNION)
				filteredNodes.addAll(found);
			else if (resultType == FilterContext.INTERSECTION)
				r.put(RatingAnnotationData.class, found);
		}
		if (resultType == FilterContext.UNION)
			return filteredNodes;
		
		//Intersection.
		filteredNodes.clear();
		
		if (r.size() == 0) return filteredNodes;
		
		i = r.keySet().iterator();
		int index = 0;
		type = null;
		while (i.hasNext()) {
			type = (Class) i.next();
			if (index == 0) {
				filteredNodes.addAll(r.get(type));
				break;
			}
			
			index++;
		}
		r.remove(type);
		i = r.keySet().iterator();
		while (i.hasNext()) {
			type = (Class) i.next();
			filteredNodes = ListUtils.intersection(filteredNodes, r.get(type));
		}
		//r.put(RatingAnnotationData.class, found);
		return filteredNodes;
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#loadTagSetsContainer(Long, boolean, long)
	 */
	public Collection loadTagSetsContainer(Long id, boolean images, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		//Collection of tags linked to tags.
		Collection c = gateway.loagTagSets(userID);
		List<Long> ids = new ArrayList<Long>();
		Iterator i = c.iterator();
		TagAnnotationData tag, child;
		Set children;
		Iterator j;
		while (i.hasNext()) {
			tag = (TagAnnotationData) i.next();
			ids.add(tag.getId());
			children = tag.getTags();
			if (children != null) {
				j = children.iterator();
				while (j.hasNext()) {
					child = (TagAnnotationData) j.next();
					if (!ids.contains(child.getId()))
						ids.add(child.getId());
				}
			}
		}
		Collection allTags = loadAnnotations(TagAnnotationData.class, null, -1, 
				                             userID);
		i = allTags.iterator();
		while (i.hasNext()) {
			tag = (TagAnnotationData) i.next();
			if (!ids.contains(tag.getId()))
				c.add(tag);
		}
		return c;
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#loadTagsContainer(Long, boolean, long)
	 */
	public Collection loadTagsContainer(Long id, boolean images, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		if (images) return gateway.loadTagAndImages(id, images);
		return loadAnnotations(TagAnnotationData.class, null, id, userID);
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#loadTags(int, long)
	 */
	public Collection loadTags(int tagLevel, long userID) 
		throws DSOutOfServiceException, DSAccessException
	{
		List<AnnotationData> annotations = new ArrayList<AnnotationData>();
		Collection c = loadAnnotations(TagAnnotationData.class, null, -1, 
										userID);
//		All the tags.
		Iterator i;
		Long id;
		List links;
		ILink link;
		TagAnnotationData tag;
		List<Long> ids = new ArrayList<Long>();
		Map<Long, TagAnnotationData> 
		     map = new HashMap<Long, TagAnnotationData>(); 
		i = c.iterator();
		while (i.hasNext()) {
			tag = (TagAnnotationData) i.next();
			ids.add(tag.getId());
			map.put(tag.getId(), tag);
		}
		switch (tagLevel) {
			case OmeroMetadataService.LEVEL_TAG:
				links = gateway.findAnnotationLinks(ImageData.class, -1, ids);
				if (links != null) {
					i = links.iterator();
					while (i.hasNext()) {
						link = (ILink) i.next();
						id = link.getChild().getId();
						tag = map.get(id);
						if (tag != null) {
							map.remove(id);
							ids.remove(id);
							annotations.add(tag);
						}
					}
				}
				//Need to retrieve the one not linked.
				if (map.size() > 0) {
					links = gateway.findlinkedTags(ids, true);
					if (links != null) {
						i = links.iterator();
						while (i.hasNext()) {
							link = (ILink) i.next();
							id = link.getChild().getId();
							tag = map.get(id);
							if (tag != null) {
								map.remove(id);
							} else {
								id = link.getParent().getId();
								tag = map.get(id);
								if (tag != null) map.remove(id);
							}
						}
					}
					if (map.size() > 0) {
						i = map.keySet().iterator();
						while (i.hasNext()) {
							tag = map.get(i.next());
							annotations.add(tag);
						}
					}
				}
				break;
			case OmeroMetadataService.LEVEL_TAG_SET:
				//Need to review that code 
				links = gateway.findlinkedTags(ids, false);
				//find the tag containing tags.
				if (links != null) {
					i = links.iterator();
					while (i.hasNext()) {
						link = (ILink) i.next();
						id = link.getParent().getId();
						tag = map.get(id);
						if (tag != null) {
							map.remove(id);
							ids.remove(id);
							annotations.add(tag);
						}
					}
				}
				//find tag linked to images.
				links = gateway.findAnnotationLinks(ImageData.class, -1, ids);
				if (links != null) {
					i = links.iterator();
					while (i.hasNext()) {
						link = (ILink) i.next();
						id = link.getChild().getId();
						tag = map.get(id);
						if (tag != null) 
							map.remove(id);
					}
				}
				if (map.size() > 0) {
					i = map.keySet().iterator();
					while (i.hasNext()) {
						tag = map.get(i.next());
						annotations.add(tag);
					}
				}
				break;
		}
		return annotations;
	}
	
}
