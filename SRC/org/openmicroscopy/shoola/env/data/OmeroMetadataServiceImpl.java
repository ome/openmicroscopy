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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import ome.model.IObject;
import ome.model.annotations.Annotation;
import ome.model.annotations.FileAnnotation;
import ome.model.core.OriginalFile;
import ome.util.builders.PojoOptions;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.util.ModelMapper;
import org.openmicroscopy.shoola.env.data.util.PojoMapper;
import org.openmicroscopy.shoola.env.data.util.StructuredDataResults;
import org.openmicroscopy.shoola.env.data.util.ViewedByDef;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;
import pojos.AnnotationData;
import pojos.DataObject;
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
			Collection descriptions;
			while (i.hasNext()) {
				data = (AnnotationData) i.next();
				if (data instanceof URLAnnotationData)
					urls.add(data);
				else if (data instanceof TextualAnnotationData)
					texts.add(data);
				else if (data instanceof TagAnnotationData) {
					long id = data.getId();
					descriptions = loadTextualAnnotations(
							TagAnnotationData.class, id, userID);
					TagAnnotationData tag = (TagAnnotationData) data;
					if (descriptions != null && descriptions.size() > 0)
					tag.setTagDescriptions(
									(List<TextualAnnotationData>) descriptions);
					tags.add(tag);
					//need to load the description
				} else if (data instanceof RatingAnnotationData)
					ratings.add(data);
				else if (data instanceof FileAnnotationData) {
					FileAnnotation fa = (FileAnnotation) data.asAnnotation();
					long id = fa.getFile().getId();
					((FileAnnotationData) data).setContent(
							gateway.getOriginalFile(id));
					attachments.add(data);
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
			boolean b = gateway.hasArchivedFiles(
					img.getDefaultPixels().getId());
			System.err.println(b);
			results.setArchived(gateway.hasArchivedFiles(
					img.getDefaultPixels().getId()));
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
		Class ioType = gateway.convertPojos(type);
		IObject ho = gateway.findIObject(ioType, id);
		IObject link = null;
		boolean exist = false;
		if (annotation instanceof TagAnnotationData) {
			TagAnnotationData tag = (TagAnnotationData) annotation;
			if (tag.getId() < 0) 
				link = ModelMapper.createAnnotation(ho, annotation);
			else {
				link = gateway.findAnnotationLink(ho, tag.getId());
				if (link == null)
					link = ModelMapper.linkAnnotation(ho, tag.asIObject());
				else exist = true;
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
				TagAnnotationData tag = (TagAnnotationData) annotation;
				TextualAnnotationData description = tag.getTagDescription();
				if (description != null) {
					id = tag.getId();
					if (id >= 0)
						clearAnnotationAnnotationLink(id, 
										tag.getTagDescriptions());
					 	
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
		IObject link = gateway.findAnnotationLink(object.asIObject(), 
						ho.getId());
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
			throw new IllegalArgumentException("No objec to handle.");
		IObject ho = gateway.findIObject(annotation.asIObject());
		List<IObject> links = new ArrayList<IObject>();
		Iterator i = objects.iterator();
		while (i.hasNext()) {
			links.add(gateway.findAnnotationLink(
					((DataObject) i.next()).asIObject(), ho.getId()));
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
									long objectID) 
		throws DSOutOfServiceException, DSAccessException
	{
		Collection c = gateway.fetchAnnotation(annotationType, objectType, -1);
		List<AnnotationData> annotations = new ArrayList<AnnotationData>();
		if (c == null || c.size() == 0) return annotations;
		Iterator i = c.iterator();
		AnnotationData data;
		while (i.hasNext()) {
			data = (AnnotationData) i.next();
			if (annotationType.equals(data.getClass())) {
				if (data instanceof FileAnnotationData) {
					FileAnnotation fa = (FileAnnotation) data.asAnnotation();
					long id = fa.getFile().getId();
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
	 * @see OmeroMetadataService#saveData(DataObject, List, List, long)
	 */
	public Object saveData(DataObject data, List<AnnotationData> toAdd, 
			List<AnnotationData> toRemove, long userID) 
			throws DSOutOfServiceException, DSAccessException
	{
		if (data == null)
			throw new IllegalArgumentException("No data to save");
		OmeroDataService service = context.getDataService();
		service.updateDataObject(data);
		Iterator i;
		if (toAdd != null) {
			i = toAdd.iterator();
			while (i.hasNext())
				annotate(data, (AnnotationData) i.next());
		}
		if (toRemove != null) {
			i = toRemove.iterator();
			while (i.hasNext())
				removeAnnotation((AnnotationData) i.next(), data);
		}
		return data;
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
	
}
