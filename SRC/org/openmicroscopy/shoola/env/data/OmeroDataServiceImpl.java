/*
 * org.openmicroscopy.shoola.env.data.OmeroDataServiceImpl
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

//Application-internal dependencies
import omero.RString;
import omero.model.Channel;
import omero.model.Dataset;
import omero.model.DatasetImageLink;
import omero.model.Event;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.LongAnnotation;
import omero.model.OriginalFile;
import omero.model.Pixels;
import omero.model.Project;
import omero.model.ProjectDatasetLink;
import omero.model.Screen;
import omero.model.ScreenPlateLink;
import omero.sys.PojoOptions;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.AgentInfo;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.login.UserCredentials;
import org.openmicroscopy.shoola.env.data.model.DeletableObject;
import org.openmicroscopy.shoola.env.data.util.ModelMapper;
import org.openmicroscopy.shoola.env.data.util.PojoMapper;
import org.openmicroscopy.shoola.env.data.util.SearchDataContext;
import pojos.AnnotationData;
import pojos.ChannelData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.ImageData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.RatingAnnotationData;
import pojos.ScreenData;
import pojos.TagAnnotationData;

/** 
 * Implementation of the {@link OmeroDataService} I/F.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class OmeroDataServiceImpl
	implements OmeroDataService
{
  
	/** Uses it to gain access to the container's services. */
	private Registry                context;

	/** Reference to the entry point to access the <i>OMERO</i> services. */
	private OMEROGateway            gateway;

	/**
	 * Unlinks the collection of children from the specified parent.
	 * 
	 * @param parent    The parent of the children.
	 * @param children  The children to unlink
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMEDS service. 
	 */
	private void cut(DataObject parent, Set children)
		throws DSOutOfServiceException, DSAccessException
	{
		IObject mParent = parent.asIObject();
		Iterator i = children.iterator();
		List<Long> ids = new ArrayList<Long>(children.size());
		while (i.hasNext()) {  
			ids.add(new Long(((DataObject) i.next()).getId())); 
		}
		List links = gateway.findLinks(mParent, ids);
		if (links != null) 
			gateway.deleteObjects(links);
	}

	/**
	 * Returns the list of objects owned by other users.
	 * 
	 * @param objects The list to manipulate.
	 * @return See above
	 */
	private List<IObject> isRelatedToOther(List<IObject> objects)
	{
		List<IObject> others = new ArrayList<IObject>();
		if (objects == null) return others;
		ExperimenterData exp = 
			(ExperimenterData) context.lookup(LookupNames.CURRENT_USER_DETAILS);
		long id = exp.getId();
		Iterator i = objects.iterator();
		IObject obj;
		long ownerID;
		
		while (i.hasNext()) {
			obj = (IObject) i.next();
			ownerID = obj.getDetails().getOwner().getId().getValue();
			if (ownerID != id)
				others.add(obj);
		}
		return others;
	}

	/**
	 * Deletes the passed object. Returns the objects that cannot be deleted.
	 * 
	 * @param object The object to delete.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	private List<DeletableObject> delete(DeletableObject object) 
		throws DSOutOfServiceException, DSAccessException
	{
		if (object == null) return null;
		DataObject data = object.getObjectToDelete();
		boolean attachment = object.deleteAttachment();
		boolean content = object.deleteContent();
		long id = data.getId();
		List<IObject> list;
		List<IObject> annotations;
		List<IObject> annotatedByOthers;
		List<IObject> usedByOthers;
		List<DeletableObject> result = new ArrayList<DeletableObject>();
		List<DeletableObject> notDeleted;
		if (data instanceof ImageData) {
			//Can I delete, only check datasets: TODO: more
			list = gateway.checkImage(data.asImage());
			if (list != null && list.size() > 0) {
				object.setBlocker(list);
				result.add(object);
				return result;
			} 
			annotations = gateway.findAnnotationLinks(
					ImageData.class.getName(), id, null);
			annotatedByOthers = isRelatedToOther(annotations);
			
			if (annotatedByOthers.size() != 0) {
				//cannot delete. Notify user.
				object.setBlocker(annotatedByOthers);
				result.add(object);
				return result;
			} 
			//Nobody else annotated it.
			if (!attachment) //want to keep the annotation.
				gateway.deleteObjects(annotations);
			else { //check if we need to keep some object
				List<IObject> toDelete = 
					annotationsLinkToDelete(object.getAttachmentTypes(), 
							annotations);
				if (toDelete.size() > 0)
					gateway.deleteObjects(toDelete);
			} 
			gateway.deleteImage(data.asImage());
			return result;
		} else if (data instanceof DatasetData) {
			if (!content) return deleteDataset(object);
			//retrieve all the images
			ExperimenterData exp = 
				(ExperimenterData) context.lookup(
						LookupNames.CURRENT_USER_DETAILS);
			long userID = exp.getId();
			List<Long> ids = new ArrayList<Long>(1);
			ids.add(id);
			Set l = loadContainerHierarchy(DatasetData.class, ids, true, userID);
			Iterator j = l.iterator();
			DatasetData d;
			Set images;
			Iterator k;
			ImageData img;
			while (j.hasNext()) {
				d = (DatasetData) j.next();
				images = d.getImages();
				k = images.iterator();
				while (k.hasNext()) {
					img = (ImageData) k.next();
					notDeleted = delete(new DeletableObject(img, false, 
										attachment));
					if (notDeleted.size() > 0)
						result.addAll(notDeleted);
				}
			}
			notDeleted = deleteDataset(object);
			if (notDeleted.size() > 0)
				result.addAll(notDeleted);
			return result;
		} else if (data instanceof ProjectData) {
			if (!content)
				return deleteProject(object);
			//retrieve all the images
			ExperimenterData exp = 
				(ExperimenterData) context.lookup(
						LookupNames.CURRENT_USER_DETAILS);
			long userID = exp.getId();
			List<Long> ids = new ArrayList<Long>(1);
			ids.add(id);
			Set l = loadContainerHierarchy(ProjectData.class, ids, false, 
											userID);
			Iterator j = l.iterator();
			ProjectData p;
			Set datasets;
			Iterator k;
			DatasetData d;
			while (j.hasNext()) {
				p = (ProjectData) j.next();
				datasets = p.getDatasets();
				k = datasets.iterator();
				while (k.hasNext()) {
					d = (DatasetData) k.next();
					notDeleted = deleteDataset(
							new DeletableObject(d, false, attachment));
					if (notDeleted.size() > 0)
						result.addAll(notDeleted);
				}
			}
			notDeleted = deleteProject(object);
			if (notDeleted.size() > 0)
				result.addAll(notDeleted);
			return result;
		} else if (data instanceof FileAnnotationData) {
			List<Long> ids = new ArrayList<Long>();
			ids.add(data.getId());
			List l = gateway.findAnnotationLinks(ImageData.class.getName(), -1, 
					ids);
			if (l != null && l.size() > 0) {
				object.setBlocker(l);
				result.add(object);
				return result;
			}
			l = gateway.findAnnotationLinks(DatasetData.class.getName(), -1, 
					ids);
			if (l != null && l.size() > 0) {
				object.setBlocker(l);
				result.add(object);
				return result;
			}
			l = gateway.findAnnotationLinks(ProjectData.class.getName(), -1, 
					ids);
			if (l != null && l.size() > 0) {
				object.setBlocker(l);
				result.add(object);
				return result;
			}
			//not link, we can delete
			long originalFileID = ((FileAnnotationData) data).getFileID();
			gateway.deleteObject(
					gateway.findIObject(data.asIObject()));
			gateway.deleteObject(
					gateway.findIObject(OriginalFile.class.getName(), 
							originalFileID));
		}
		return result;
	}
	
	/**
	 * Returns the collection of annotations to delete before deleting
	 * the object.
	 * @param types
	 * @param annotations
	 * @return See above.
	 */
	private List<IObject> annotationsLinkToDelete(List<Class> types, 
								List<IObject> annotations)
	{
		List<Class> annoTypes = convert(types);
		List<IObject> toDelete = new ArrayList<IObject>();
		if (annoTypes.size() == 0) return toDelete;
		Iterator k = annotations.iterator();
		ImageAnnotationLink ann;
		IObject child;
		while (k.hasNext()) {
			ann = (ImageAnnotationLink) k.next();
			child = ann.getChild();
			if (child != null) {
				if (child instanceof LongAnnotation) {
					
					LongAnnotation longA = (LongAnnotation) child;
					RString name = longA.getNs();
					if (name != null) {
						if (name.getValue().equals(
						RatingAnnotationData.INSIGHT_RATING_NS) &&
							!annoTypes.contains(child.getClass())){
							toDelete.add(ann);
						}
					}
				} else {
					if (!annoTypes.contains(child.getClass())) {
						toDelete.add(ann);
					}
				}
			}
		}
		return toDelete;
	}
	
	/**
	 * Converts the list of pojos into the corresponding class.
	 * 
	 * @param list The list to handle.
	 * @return See above.
	 */
	private List<Class> convert(List<Class> list)
	{
		List<Class> newList = new ArrayList<Class>();
		Iterator<Class> i = list.iterator();
		Class klass, convertedClass;
		while (i.hasNext()) {
			klass = i.next();
			convertedClass = gateway.convertPojos(klass);
			if (convertedClass != null)
				newList.add(convertedClass);
		}
		return newList;
	}
	/**
	 * Deletes the specified project.
	 * 
	 * @param object  The object to handle.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	private List<DeletableObject> deleteProject(DeletableObject object)
		throws DSOutOfServiceException, DSAccessException
	{
		DataObject data = object.getObjectToDelete();
		boolean attachment = object.deleteAttachment();
		boolean content = object.deleteContent();
		long id = data.getId();
		List<IObject> list;
		List<IObject> annotations;
		List<IObject> annotatedByOthers;
		List<IObject> usedByOthers;
		List<DeletableObject> resuls = new ArrayList<DeletableObject>();
		annotations = gateway.findAnnotationLinks(
				ProjectData.class.getName(), id, null);
		annotatedByOthers = isRelatedToOther(annotations);
		if (annotatedByOthers.size() != 0) {
			//cannot delete. Notify user.
			object.setBlocker(annotatedByOthers);
			resuls.add(object);
			return resuls;
		} 
		if (!attachment) //want to keep the annotation.
			gateway.deleteObjects(annotations);
		else //remove the annotation from the object.
			context.getMetadataService().clearAnnotation(data);
		
		//delete all the links
		List toDelete = new ArrayList();
		list = gateway.findLinks(data.asIObject(), null);
		if (list != null) toDelete.addAll(list);
		if (toDelete.size() > 0) gateway.deleteObjects(toDelete);
		//delete the dataset
		gateway.deleteObject(gateway.findIObject(data.asIObject()));
		return resuls;	
	}
	
	/**
	 * Deletes the dataset.
	 * 
	 * @param object
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	private List<DeletableObject> deleteDataset(DeletableObject object)
		throws DSOutOfServiceException, DSAccessException
	{
		DataObject data = object.getObjectToDelete();
		boolean attachment = object.deleteAttachment();
		long id = data.getId();
		List<IObject> list;
		List<IObject> annotations;
		List<IObject> annotatedByOthers;
		List<IObject> usedByOthers;
		List<DeletableObject> results = new ArrayList<DeletableObject>();
		//check if dataset used by other.
		list = gateway.findLinks(ProjectData.class, id, -1);
		usedByOthers = isRelatedToOther(list);
		if (usedByOthers.size() > 0) { //somebody is using it.
			object.setBlocker(usedByOthers);
			results.add(object);
			return results;
		} 
		annotations = gateway.findAnnotationLinks(
				DatasetData.class.getName(), id, null);
		annotatedByOthers = isRelatedToOther(annotations);
		if (annotatedByOthers.size() != 0) {
			//cannot delete. Notify user.
			object.setBlocker(annotatedByOthers);
			results.add(object);
			return results;
		} 
		if (!attachment) //want to keep the annotation.
			gateway.deleteObjects(annotations);
		else {
			//remove the annotation from the object.
			List<IObject> toDelete = 
				annotationsLinkToDelete(object.getAttachmentTypes(), 
						annotations);
			if (toDelete.size() > 0)
				gateway.deleteObjects(toDelete);
		}
		context.getMetadataService().clearAnnotation(data);
		
		//delete all the links
		List toDelete = new ArrayList();
		if (list != null) toDelete.addAll(list); //project links
		list = gateway.findLinks(data.asIObject(), null);
		if (list != null)toDelete.addAll(list);
		if (toDelete.size() > 0) gateway.deleteObjects(toDelete);
		//delete the dataset
		gateway.deleteObject(gateway.findIObject(data.asIObject()));
		return results;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param gateway   Reference to the OMERO entry point.
	 *                  Mustn't be <code>null</code>.
	 * @param registry  Reference to the registry. Mustn't be <code>null</code>.
	 */
	OmeroDataServiceImpl(OMEROGateway gateway, Registry registry)
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
	 * @see OmeroDataService#loadContainerHierarchy(Class, List, boolean, long)
	 */
	public Set loadContainerHierarchy(Class rootNodeType, List rootNodeIDs,
			boolean withLeaves, long userID)
		throws DSOutOfServiceException, DSAccessException 
	{
		if (ScreenData.class.equals(rootNodeType)) {
			return loadScreenPlates(rootNodeType, rootNodeIDs, userID);
		}
		PojoOptions po = new PojoOptions();
		if (rootNodeIDs == null) po.exp(omero.rtypes.rlong(userID));
		if (withLeaves) po.leaves();
		else po.noLeaves();
		Set parents = gateway.loadContainerHierarchy(rootNodeType, rootNodeIDs,
				po.map()); 
		if (rootNodeIDs == null && parents != null) {
			Class klass = null;
			if (rootNodeType.equals(ProjectData.class))
				klass = DatasetData.class;

			if (klass != null) {
				po = new PojoOptions(); 
				po.exp(omero.rtypes.rlong(userID));
				po.noLeaves();
				//Set r = gateway.loadContainerHierarchy(klass, null, po.map());
				Set r = gateway.fetchContainers(klass, userID);
				Iterator i = parents.iterator();
				DataObject parent;
				Set children = new HashSet();
				while (i.hasNext()) {
					parent = (DataObject) i.next();
					if (klass.equals(DatasetData.class))
						children.addAll(((ProjectData) parent).getDatasets());
				}
				Set<Long> childrenIds = new HashSet<Long>();

				Iterator j = children.iterator();
				DataObject child;
				while (j.hasNext()) {
					child = (DataObject) j.next();
					childrenIds.add(new Long(child.getId()));
				}
				Set orphans = new HashSet();
				if (r != null) orphans.addAll(r);
				j = r.iterator();
				while (j.hasNext()) {
					child = (DataObject) j.next();
					if (childrenIds.contains(new Long(child.getId())))
						orphans.remove(child);
				}
				if (orphans.size() > 0) parents.addAll(orphans);
			}
		}
		return parents;                            
	}

	/** 
	 * Implemented as specified by {@link OmeroDataService}. 
	 * @see OmeroDataService#loadTopContainerHierarchy(Class, long)
	 */
	public Set loadTopContainerHierarchy(Class rootNodeType, long userID)
		throws DSOutOfServiceException, DSAccessException 
	{
		PojoOptions po = new PojoOptions();
		po.exp(omero.rtypes.rlong(userID));
		//po.noLeaves();
		return gateway.loadContainerHierarchy(rootNodeType, null, po.map());                         
	}

	/** 
	 * Implemented as specified by {@link OmeroDataService}. 
	 * @see OmeroDataService#findContainerHierarchy(Class, List, long)
	 */
	public Set findContainerHierarchy(Class rootNodeType, List leavesIDs, 
			long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		try {
			PojoOptions po = new PojoOptions();
			po.leaves();
			po.exp(omero.rtypes.rlong(userID));
			return gateway.findContainerHierarchy(rootNodeType, leavesIDs,
					po.map());
		} catch (Exception e) {
			throw new DSAccessException(e.getMessage());
		}
	}

	/** 
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#findAnnotations(Class, List, List, boolean)
	 */
	public Map findAnnotations(Class nodeType, List nodeIDs, List annotatorIDs,
			boolean forUser)
		throws DSOutOfServiceException, DSAccessException
	{
		//PojoOptionsI po = new PojoOptionsI();
		//po.noLeaves();
		//if (forUser) po.exp(new Long(getUserDetails().getId()));
		return gateway.findAnnotations(nodeType, nodeIDs, annotatorIDs, 
				new PojoOptions().map());
	}
	
	/** 
	 * Implemented as specified by {@link OmeroDataService}. 
	 * @see OmeroDataService#getImages(Class, List, long)
	 */
	public Set getImages(Class nodeType, List nodeIDs, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		return gateway.getContainerImages(nodeType, nodeIDs, 
				new PojoOptions().map());
	}

	/** 
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#getExperimenterImages(long)
	 */
	public Set getExperimenterImages(long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		PojoOptions po = new PojoOptions();
		po.exp(omero.rtypes.rlong(userID));
		return gateway.getUserImages(po.map());
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#getCollectionCount(Class, String, List)
	 */
	public Map getCollectionCount(Class rootNodeType, String property, List 
			rootNodeIDs)
		throws DSOutOfServiceException, DSAccessException
	{
		if (!(property.equals(IMAGES_PROPERTY)))
			throw new IllegalArgumentException("Property not supported.");
		if (rootNodeType.equals(TagAnnotationData.class))
			return gateway.getDataObjectsTaggedCount(rootNodeIDs);
		return gateway.getCollectionCount(rootNodeType, property, rootNodeIDs, 
				new PojoOptions().map());
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#createAnnotationFor(DataObject, AnnotationData)
	 */
	public DataObject createAnnotationFor(DataObject annotatedObject,
			AnnotationData data)
		throws DSOutOfServiceException, DSAccessException
	{
		if (data == null) 
			throw new IllegalArgumentException("No annotation to create.");
		if (annotatedObject == null) 
			throw new IllegalArgumentException("No DataObject to annotate."); 
		//First make sure the annotated object is current.
		IObject ho = gateway.findIObject(annotatedObject.asIObject());
		if (ho == null) return null;
		IObject object = gateway.createObject(
				ModelMapper.createAnnotationAndLink(ho, data), 
				(new PojoOptions()).map());
		return PojoMapper.asDataObject(ModelMapper.getAnnotatedObject(object));
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#createDataObject(DataObject, DataObject, 
	 * 										Collection)
	 */
	public DataObject createDataObject(DataObject child, DataObject parent,
										Collection children)
		throws DSOutOfServiceException, DSAccessException
	{
		if (child == null) 
			throw new IllegalArgumentException("The child cannot be null.");
		//Make sure parent is current

		IObject obj = ModelMapper.createIObject(child, parent);
		if (obj == null) 
			throw new NullPointerException("Cannot convert the object.");
		Map options = (new PojoOptions()).map();

		IObject created = gateway.createObject(obj, options);
		if (parent != null)
			ModelMapper.linkParentToChild(created, parent.asIObject());
		if (children != null && children.size() > 0) {
			Iterator i = children.iterator();
			Object node;
			List<IObject> links = new ArrayList<IObject>();
			while (i.hasNext()) {
				node = i.next();
				if (node instanceof DataObject)
					links.add(ModelMapper.linkParentToChild(
							((DataObject) node).asIObject(), created));
			}
			if (links.size() > 0)
				gateway.createObjects(links, options);
		}
		return  PojoMapper.asDataObject(created);
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#removeDataObjects(Set, DataObject)
	 */
	public Set removeDataObjects(Set children, DataObject parent)
		throws DSOutOfServiceException, DSAccessException
	{
		if (children == null) 
			throw new IllegalArgumentException("The children cannot be null.");
		if (children.size() == 0) 
			throw new IllegalArgumentException("No children to remove.");
		Iterator i = children.iterator();
		if (parent == null) {
			while (i.hasNext()) {
				deleteContainer((DataObject) i.next(), false);
			}
		} else {
			cut(parent, children);
		}
		return children;
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#updateDataObject(DataObject)
	 */
	public DataObject updateDataObject(DataObject object)
		throws DSOutOfServiceException, DSAccessException
	{
		if (object == null) 
			throw new DSAccessException("No object to update.");
		if (object instanceof ExperimenterData) 
			return updateExperimenter((ExperimenterData) object);
		IObject ho = null;
		IObject oldObject = null;
		oldObject = object.asIObject();
		ho = gateway.findIObject(oldObject);

		if (ho == null) return null;
		ModelMapper.fillIObject(oldObject, ho);
		ModelMapper.unloadCollections(ho);
		IObject updated = gateway.updateObject(ho,
				(new PojoOptions()).map());
		return PojoMapper.asDataObject(updated);
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#loadExistingObjects(Class, List, long)
	 */
	public Set loadExistingObjects(Class nodeType, List nodeIDs, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		Set all = null;
		Set<Long> objects = new HashSet<Long>();
		if (nodeType.equals(ProjectData.class)) {
			Set in = loadContainerHierarchy(nodeType, nodeIDs, true, userID);
			all = loadContainerHierarchy(DatasetData.class, null, true, userID);
			Iterator i = in.iterator();
			Iterator j; 
			while (i.hasNext()) {
				j = (((ProjectData) i.next()).getDatasets()).iterator();
				while (j.hasNext()) {
					objects.add(new Long(((DatasetData) j.next()).getId()));
				} 
			}
		}  else if (nodeType.equals(DatasetData.class)) {
			Set in = getImages(nodeType, nodeIDs, userID);
			all = getExperimenterImages(userID);
			Iterator i = in.iterator();
			while (i.hasNext()) {
				objects.add(new Long(((ImageData) i.next()).getId()));
			}

		}
		if (all == null) return new HashSet(1);
		Iterator k = all.iterator();
		Set<DataObject> toRemove = new HashSet<DataObject>();
		DataObject ho;
		Long id;
		while (k.hasNext()) {
			ho = (DataObject) k.next();
			id = new Long(ho.getId());
			if (objects.contains(id)) toRemove.add(ho);
		}
		all.removeAll(toRemove);
		return all;
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#addExistingObjects(DataObject, Set)
	 */
	public void addExistingObjects(DataObject parent, Set children)
		throws DSOutOfServiceException, DSAccessException
	{
		if (parent instanceof ProjectData) {
			try {
				children.toArray(new DatasetData[] {});
			} catch (ArrayStoreException ase) {
				throw new IllegalArgumentException(
						"items can only be datasets.");
			}
		} else if (parent instanceof DatasetData) {
			try {
				children.toArray(new ImageData[] {});
			} catch (ArrayStoreException ase) {
				throw new IllegalArgumentException(
						"items can only be images.");
			}
		} else if (parent instanceof ScreenData) {
			try {
				children.toArray(new PlateData[] {});
			} catch (ArrayStoreException ase) {
				throw new IllegalArgumentException(
						"items can only be plate.");
			}
		} else
			throw new IllegalArgumentException("parent object not supported");

		List<IObject> objects = new ArrayList<IObject>();
		IObject ioParent = parent.asIObject();
		IObject ioChild;
		Iterator child = children.iterator();
		while (child.hasNext()) {
			ioChild = ((DataObject) child.next()).asIObject();
			//First make sure that the child is not linked to the parent.
			if (gateway.findLink(ioParent, ioChild) == null)
				objects.add(ModelMapper.linkParentToChild(ioChild, ioParent));
		}
		if (objects.size() != 0) {
			gateway.createObjects(objects, (new PojoOptions()).map());
		} 
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#cutAndPaste(Map, Map)
	 */
	public void cutAndPaste(Map toPaste, Map toCut)
		throws DSOutOfServiceException, DSAccessException
	{
		if ((toPaste == null || toCut == null) || 
				(toPaste.size() == 0 || toCut.size() == 0)) {
			throw new IllegalArgumentException("No data to cut and Paste.");
		}
		Iterator i;
		Object parent;
		i = toCut.keySet().iterator();
		while (i.hasNext()) {
			parent = i.next();
			if (parent instanceof DataObject) //b/c of orphaned container
				cut((DataObject) parent, (Set) toCut.get(parent));
		}

		i = toPaste.keySet().iterator();

		while (i.hasNext()) {
			parent = i.next();
			if (parent instanceof DataObject)
				addExistingObjects((DataObject) parent, 
						(Set) toPaste.get(parent));
		}
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#getChannelsMetadata(long)
	 */
	public List getChannelsMetadata(long pixelsID)
		throws DSOutOfServiceException, DSAccessException
	{
		Pixels pixels = gateway.getPixels(pixelsID);
		
		Collection l = pixels.copyChannels();
		Iterator i = l.iterator();
		List<ChannelData> m = new ArrayList<ChannelData>(l.size());
		int index = 0;
		while (i.hasNext()) {
			m.add(new ChannelData(index, (Channel) i.next()));
			index++;
		}
		return m;
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#createAnnotationFor(Set, AnnotationData)
	 */
	public List createAnnotationFor(Set toAnnotate, AnnotationData d) 
		throws DSOutOfServiceException, DSAccessException 
	{
		if (d == null) 
			throw new IllegalArgumentException("No annotation to create.");
		if (toAnnotate == null || toAnnotate.size() == 0) 
			throw new IllegalArgumentException("No DataObject to annotate."); 
		Iterator i = toAnnotate.iterator();
		DataObject annotatedObject;
		IObject[] toCreate = new IObject[toAnnotate.size()];
		IObject[] objects = new IObject[toAnnotate.size()];
		int index = 0;
		IObject ho;
		while (i.hasNext()) {
			annotatedObject = (DataObject) i.next();
			ho = gateway.findIObject(annotatedObject.asIObject());
			if (ho != null) {
				toCreate[index] = ModelMapper.createAnnotationAndLink(ho, d);
				
				objects[index] = gateway.createObject(toCreate[index], 
						(new PojoOptions()).map());
				index++;
			}

		}
		List<DataObject> results = new ArrayList<DataObject>(objects.length);
		//TODO review that code.
		DataObject data;
		for (int j = 0; j < objects.length; j++) {
			data = PojoMapper.asDataObject(
					ModelMapper.getAnnotatedObject(objects[j]));
			results.add(data);
		}
		return results;
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#updateAnnotationFor(Map)
	 */
	public List updateAnnotationFor(Map nodes) 
		throws DSOutOfServiceException, DSAccessException 
	{
		if (nodes == null || nodes.size() == 0) 
			throw new IllegalArgumentException("No DataObject to annotate."); 

		Map options = (new PojoOptions()).map();
		Iterator i = nodes.keySet().iterator();
		DataObject annotatedObject;
		IObject object, updated, toUpdate;
		List<DataObject> results = new ArrayList<DataObject>(nodes.size());
		while (i.hasNext()) {
			annotatedObject = (DataObject) i.next();
			object = gateway.findIObject(annotatedObject.asIObject());
			if (object != null) {
				ModelMapper.unloadCollections(object);
				updated = gateway.updateObject(object, options);
				toUpdate = ((AnnotationData) 
						nodes.get(annotatedObject)).asIObject();
				ModelMapper.setAnnotatedObject(updated, toUpdate);
				gateway.updateObject(toUpdate, options);
				results.add(PojoMapper.asDataObject(updated));
			}
		}
		return results;
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#getArchivedFiles(String, long)
	 */
	public Map<Integer, List> getArchivedFiles(String path, long pixelsID) 
		throws DSOutOfServiceException, DSAccessException
	{
		context.getLogger().debug(this, path);
		return gateway.getArchivedFiles(path, pixelsID);
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#annotateChildren(Set, AnnotationData)
	 */
	public List annotateChildren(Set folders, AnnotationData data) 
		throws DSOutOfServiceException, DSAccessException 
	{
		if (folders == null || folders.size() == 0)
			throw new IllegalArgumentException("No DataObject to annotate."); 
		if (data == null)
			throw new IllegalArgumentException("No annotation."); 
		Iterator i = folders.iterator(), j;
		DataObject object;
		Set images = null;
		Class klass = null;
		List<Long> ids;
		DataObject image;
		List<DataObject> results = new ArrayList<DataObject>();
		PojoOptions po = new PojoOptions();
		po.allExps();
		Map map = po.map();
		while (i.hasNext()) {
			object = (DataObject) i.next();
			ids = new ArrayList<Long>(1);
			ids.add(new Long(object.getId()));
			if (object instanceof DatasetData) {
				klass = DatasetData.class;
			} 
			if (klass != null) 
				images = gateway.getContainerImages(klass, ids, map);

			if (images != null) {
				j = images.iterator();
				while (j.hasNext()) {
					image = createAnnotationFor((DataObject) j.next(), data);
					results.add(image);
				}
			}
		}
		return results;
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#changePassword(String, String)
	 */
	public Boolean changePassword(String oldPassword, String newPassword) 
		throws DSOutOfServiceException, DSAccessException 
	{
		if (newPassword == null || newPassword.trim().length() == 0)
			throw new IllegalArgumentException("Password not valid.");
		UserCredentials uc = (UserCredentials) 
		context.lookup(LookupNames.USER_CREDENTIALS);
		if (!uc.getPassword().equals(oldPassword)) return Boolean.FALSE;

		gateway.changePassword(newPassword);
		uc.resetPassword(newPassword);
		return Boolean.TRUE;
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#updateExperimenter(ExperimenterData)
	 */
	public ExperimenterData updateExperimenter(ExperimenterData exp) 
		throws DSOutOfServiceException, DSAccessException 
	{
		//ADD control
		if (exp == null) 
			throw new DSAccessException("No object to update.");
		UserCredentials uc = (UserCredentials) 
		context.lookup(LookupNames.USER_CREDENTIALS);
		gateway.updateExperimenter(exp.asExperimenter());
		//oldObject.setOmeName(uc.getUserName());
		//DEfault group issue.
		//TODO invoke server when method is updated server side.
		//PojoMapper.asDataObject(updated);
		ExperimenterData data = gateway.getUserDetails(uc.getUserName());
		context.bind(LookupNames.CURRENT_USER_DETAILS, exp);
//		Bind user details to all agents' registry.
		List agents = (List) context.lookup(LookupNames.AGENTS);
		Iterator i = agents.iterator();
		AgentInfo agentInfo;
		while (i.hasNext()) {
			agentInfo = (AgentInfo) i.next();
			agentInfo.getRegistry().bind(
					LookupNames.CURRENT_USER_DETAILS, exp);
		}
		return data;
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#getServerName()
	 */
	public String getServerName() 
	{
		UserCredentials uc = (UserCredentials) 
		context.lookup(LookupNames.USER_CREDENTIALS);
		return uc.getHostName();
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#getLoggingName()
	 */
	public String getLoggingName() 
	{
		UserCredentials uc = (UserCredentials) 
		context.lookup(LookupNames.USER_CREDENTIALS);
		return uc.getUserName();
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#getSpace(int)
	 */
	public long getSpace(int index)
		throws DSOutOfServiceException, DSAccessException
	{
		switch (index) {
		case OmeroDataService.USED:
			return gateway.getUsedSpace();
		case OmeroDataService.FREE:
			return gateway.getFreeSpace();
		}
		return -1;
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#getImagesPeriod(Timestamp, Timestamp, long)
	 */
	public Set getImagesPeriod(Timestamp startTime, Timestamp endTime, 
								long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		if (startTime == null && endTime == null)
			throw new NullPointerException("Time not specified.");
		
		PojoOptions po = new PojoOptions();
		po.leaves();
		po.exp(omero.rtypes.rlong(userID));
		if (startTime != null) 
			po.startTime(omero.rtypes.rtime(startTime.getTime()));
		if (endTime != null) 
			po.endTime(omero.rtypes.rtime(endTime.getTime()));
		return gateway.getImages(po.map());
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#getImagesPeriodIObject(Timestamp, Timestamp, long)
	 */
	public List getImagesPeriodIObject(Timestamp startTime, Timestamp endTime, 
			long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		if (startTime == null && endTime == null)
			throw new NullPointerException("Time not specified.");
		return gateway.getImagesDuring(startTime, endTime, userID);
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#getImagesAllPeriodCount(Timestamp, Timestamp, long)
	 */
	public List getImagesAllPeriodCount(Timestamp startTime, 
			Timestamp endTime, long userID) 
		throws DSOutOfServiceException, DSAccessException
	{
		if (startTime == null || endTime == null)
			throw new NullPointerException("Time not specified.");
		List imgs = gateway.getImagesDuring(startTime, endTime, userID);
		Iterator i = imgs.iterator();
		Image object;
		List<Timestamp> times = new ArrayList<Timestamp>(imgs.size());
		Event evt;
		while (i.hasNext()) {
			object = (Image) i.next();
			evt = object.getDetails().getCreationEvent();
			if (evt != null)
				times.add(new Timestamp(evt.getTime().getValue()));
		}
		return times;
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#advancedSearchFor(SearchDataContext)
	 */
	public Object advancedSearchFor(SearchDataContext context) 
		throws DSOutOfServiceException, DSAccessException
	{
		if (context == null)
			throw new IllegalArgumentException("No search context defined.");
		if (!context.isValid())
			throw new IllegalArgumentException("Search context not valid.");
		
		
		Object result = gateway.performSearch(context); 
		//Should returns a search context for the moment.
		//collection of images only.
		Map m = (Map) result;
		Iterator i = m.keySet().iterator();
		Integer key;
		List value;
		Iterator k;
		Set<Long> imageIDs = new HashSet<Long>();
		Set images;
		DataObject img;
		List owners = context.getOwners();
		Set<Long> ownerIDs = new HashSet<Long>(owners.size());
		k = owners.iterator();
		while (k.hasNext()) {
			ownerIDs.add(((DataObject) k.next()).getId());
		}
		Map<Integer, Object> results = new HashMap<Integer, Object>();
		Set<DataObject> nodes;
		Map pojoMap = new PojoOptions().map();
		Object v;
		
		while (i.hasNext()) {
			key = (Integer) i.next();
			v =  m.get(key);
			if (v instanceof Integer) {
				results.put(key, v);
			} else {
				value = (List) v;
				nodes = new HashSet<DataObject>(); 
				results.put(key, nodes);
				if (value.size() > 0) {
					switch (key) {
						case SearchDataContext.NAME:
						case SearchDataContext.DESCRIPTION:
						case SearchDataContext.TAGS:
						case SearchDataContext.TEXT_ANNOTATION:
						case SearchDataContext.FILE_ANNOTATION:
						case SearchDataContext.URL_ANNOTATION:
							images = gateway.getContainerImages(ImageData.class, 
									value, pojoMap);
							k = images.iterator();
							while (k.hasNext()) {
								img = (DataObject) k.next();
								if (!imageIDs.contains(img.getId())) {
									imageIDs.add(img.getId());
									nodes.add(img);
								}
							}
							break;
					}
				}
			}
			
		}
		return results; 
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#findContainerPaths(Class, long, long)
	 */
	public Collection findContainerPaths(Class type, long id, long userID) 
		throws DSOutOfServiceException, DSAccessException
	{
		try {
			Class parentClass = null;
			if (DatasetData.class.equals(type))
				parentClass = Project.class;
			else if (ImageData.class.equals(type))
				parentClass = Dataset.class;
			else if (PlateData.class.equals(type))
				parentClass = Screen.class;
			if (parentClass == null) return new HashSet();
			List links = gateway.findLinks(parentClass, id, userID);
			if (links == null) return new HashSet();
			Iterator i = links.iterator();
			Set<DataObject> nodes = new HashSet<DataObject>();
			IObject object, parent = null;
			DataObject data;
			while (i.hasNext()) {
				if (parentClass.equals(Project.class))
					parent = ((ProjectDatasetLink) i.next()).getParent();
				else if (parentClass.equals(Dataset.class))
					parent = ((DatasetImageLink) i.next()).getParent();
				else if (parentClass.equals(Screen.class))
					parent = ((ScreenPlateLink) i.next()).getParent();
				object = gateway.findIObject(parent.getClass().getName(), 
						parent.getId().getValue());
				data = PojoMapper.asDataObject(object);
				nodes.add(data);
			}
			return nodes;
		} catch (Exception e) {
			throw new DSAccessException(e.getMessage());
		}
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#getOriginalFiles(long)
	 */
	public Collection getOriginalFiles(long pixelsID) 
		throws DSOutOfServiceException, DSAccessException
	{
		if (pixelsID < 0)
			throw new IllegalArgumentException("Pixels set ID not valid.");
		return gateway.getOriginalFiles(pixelsID);
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#deleteContainer(DataObject, DataObject, boolean)
	 */
	public void deleteContainer(DataObject child, boolean content)
		throws DSOutOfServiceException, DSAccessException
	{
		if (child == null) return;
		IObject object = gateway.findIObject(child.asIObject());
		//TODO: implement content later 
		//remove Annotation linked to the object.
		List<IObject> remove;
		Iterator i;
		//First remove annotation linked to the object
		context.getMetadataService().clearAnnotation(child);
		List l;
		List children;
		List<Long> ids = new ArrayList<Long>(1);
		if (child instanceof DatasetData) {
			//Find all dataset-image links.
			l = gateway.findLinks(child.asIObject(), null);
			if (l != null && l.size() > 0) {
				children = new ArrayList();
				i = l.iterator();
				remove = new ArrayList<IObject>(l.size());
				ProjectDatasetLink link;
				while (i.hasNext()) {
					link = (ProjectDatasetLink) i.next();
					remove.add(link);
					children.add(link.getChild().getId());
				}
				//delete the links
				gateway.deleteObjects(remove);
				if (content) {
					//remove images.
					i = children.iterator();
					/*
					while (i.hasNext()) {
						id = (Long) i.next();
						//gateway.removeObject(Image, objectID)
					}
					*/
				}
			}
			ids.add(child.getId());
			l = gateway.findLinks(ProjectData.class, ids, -1);
			if (l != null && l.size() > 0) {
				i = l.iterator();
				remove = new ArrayList<IObject>(l.size());
				while (i.hasNext()) 
					remove.add((IObject) i.next());

				gateway.deleteObjects(remove);
			}
		} else if (child instanceof ProjectData) {
			l = gateway.findLinks(child.asIObject(), null);
			if (l != null && l.size() > 0) {
				i = l.iterator();
				remove = new ArrayList<IObject>(l.size());
				while (i.hasNext()) 
					remove.add((IObject) i.next());
				
				gateway.deleteObjects(remove);
			}
		}
		gateway.deleteObject(child.asIObject());
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#loadScreenPlates(Class, List, long)
	 */
	public Set loadScreenPlates(Class rootNodeType, List rootNodeIDs, 
									long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		//Add controls
		Set all = new HashSet();
		Collection r = gateway.loadScreenPlate(rootNodeType, userID);
		all.addAll(r);
		if (ScreenData.class.equals(rootNodeType)) {
			Iterator i = r.iterator();
			ScreenData screen;
			Set<Long> plateIDs = new HashSet<Long>();
			Set<PlateData> plates;
			Iterator j;
			PlateData plate;
			while (i.hasNext()) {
				screen = (ScreenData) i.next();
				plates = screen.getPlates();
				j = plates.iterator();
				while (j.hasNext()) {
					plate = (PlateData) j.next();
					plateIDs.add(plate.getId());
				}
			}
			r = gateway.loadScreenPlate(PlateData.class, userID);
			i = r.iterator();
			while (i.hasNext()) {
				plate = (PlateData) i.next();
				if (!plateIDs.contains(plate.getId()))
					all.add(plate);
			}
		} 
		return all;
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#loadPlateWells(long, long)
	 */
	public Collection loadPlateWells(long plateID, long userID) 
		throws DSOutOfServiceException, DSAccessException
	{
		
		return gateway.loadPlateWells(plateID, userID);
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#delete(Collection)
	 */
	public Collection<DeletableObject> delete(
			Collection<DeletableObject> objects) 
		throws DSOutOfServiceException, DSAccessException
	{
		if (objects == null) return null;
		List<DeletableObject> l = new ArrayList<DeletableObject>();
		Iterator<DeletableObject> i = objects.iterator();
		List<DeletableObject> r; 
		while (i.hasNext()) {
			r = delete(i.next());
			if (r.size() != 0)
				l.addAll(r);
		}
		
		//Clean repository.
		return l;
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#getImage(long, long)
	 */
	public ImageData getImage(long imageID, long userID) 
		throws DSOutOfServiceException, DSAccessException
	{
		return gateway.getImage(imageID);
	}
	
}
