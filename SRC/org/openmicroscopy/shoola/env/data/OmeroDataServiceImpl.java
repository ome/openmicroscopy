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
import omero.model.Annotation;
import omero.model.AnnotationAnnotationLink;
import omero.model.Channel;
import omero.model.Dataset;
import omero.model.DatasetAnnotationLink;
import omero.model.DatasetImageLink;
import omero.model.Event;
import omero.model.FileAnnotation;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.LongAnnotation;
import omero.model.OriginalFile;
import omero.model.Pixels;
import omero.model.PlateAnnotationLink;
import omero.model.Project;
import omero.model.ProjectAnnotationLink;
import omero.model.ProjectDatasetLink;
import omero.model.Screen;
import omero.model.ScreenAnnotationLink;
import omero.model.ScreenPlateLink;
import omero.model.TagAnnotation;
import omero.model.WellAnnotationLink;
import omero.model.WellSampleAnnotationLink;
import omero.sys.Parameters;
import omero.sys.ParametersI;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.AgentInfo;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.login.UserCredentials;
import org.openmicroscopy.shoola.env.data.model.DeletableObject;
import org.openmicroscopy.shoola.env.data.util.ModelMapper;
import org.openmicroscopy.shoola.env.data.util.PojoMapper;
import org.openmicroscopy.shoola.env.data.util.SearchDataContext;
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
import pojos.WellData;
import pojos.WellSampleData;

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
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	private synchronized List<DeletableObject> delete(DeletableObject object) 
		throws DSOutOfServiceException, DSAccessException
	{
		if (object == null) return null;
		ExperimenterData exp = (ExperimenterData) context.lookup(
					LookupNames.CURRENT_USER_DETAILS);

		long userID = exp.getId();
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
		if (data instanceof ImageData) { //Image
			//First check if the object still exists.
			
			if (gateway.findIObject(data.asIObject()) == null) {
				return result;
			}
			
			
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
							annotations, ImageData.class);
				if (toDelete.size() > 0)
					gateway.deleteObjects(toDelete);
			} 
			gateway.deleteImage(data.asImage());
			return result;
		} else if (data instanceof DatasetData) { //Dataset
			if (!content) return deleteDataset(object);
			//retrieve all the images
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
				if (images != null) {
					k = images.iterator();
					while (k.hasNext()) {
						img = (ImageData) k.next();
						notDeleted = delete(new DeletableObject(img, false, 
											attachment));
						if (notDeleted.size() > 0)
							result.addAll(notDeleted);
					}
				}
			}
			notDeleted = deleteDataset(object);
			if (notDeleted.size() > 0)
				result.addAll(notDeleted);
			return result;
		} else if (data instanceof PlateData) {
			if (gateway.findIObject(data.asIObject()) != null) {
				gateway.deleteObject(data.asIObject());
			}
			return result;
		} else if (data instanceof ScreenData) {
			if (!content)
				return deleteTopContainer(object);
			List<Long> ids = new ArrayList<Long>(1);
			ids.add(id);
			Set l = loadContainerHierarchy(ScreenData.class, ids, false, 
											userID);
			//delete all the plates
			if (l != null && l.size() > 0) {
				Iterator i = l.iterator();
				DataObject obj;
				while (i.hasNext()) {
					obj = (DataObject) i.next();
					if (obj instanceof PlateData) {
						gateway.deleteObject(obj.asIObject());
					}
				}
			}
			return deleteTopContainer(object);
		} else if (data instanceof ProjectData) { //project
			if (!content)
				return deleteTopContainer(object);
			//retrieve all the dataset
			List<Long> ids = new ArrayList<Long>(1);
			ids.add(id);
			Set l = loadContainerHierarchy(ProjectData.class, ids, false, 
											userID);
			Iterator j = l.iterator();
			ProjectData p;
			Set datasets;
			Iterator k;
			DatasetData d;
			Set images;
			ImageData img;
			while (j.hasNext()) {
				p = (ProjectData) j.next();
				datasets = p.getDatasets();
				k = datasets.iterator();
				while (k.hasNext()) {
					d = (DatasetData) k.next();
					images = d.getImages();
					if (images != null) {
						k = images.iterator();
						while (k.hasNext()) {
							img = (ImageData) k.next();
							notDeleted = delete(new DeletableObject(img, false, 
												attachment));
							if (notDeleted.size() > 0)
								result.addAll(notDeleted);
						}
					}
					//Now delete the dataset.
					notDeleted = deleteDataset(
							new DeletableObject(d, false, attachment));
					if (notDeleted.size() > 0)
						result.addAll(notDeleted);
					/*
					notDeleted = deleteDataset(
							new DeletableObject(d, false, attachment));
					if (notDeleted.size() > 0)
						result.addAll(notDeleted);
						*/
				}
			}
			notDeleted = deleteTopContainer(object);
			if (notDeleted.size() > 0)
				result.addAll(notDeleted);
			return result;
		} else if (data instanceof FileAnnotationData) {
			if (gateway.findIObject(data.asIObject()) == null)
				return result;
			List<Long> ids = new ArrayList<Long>();
			ids.add(data.getId());
			List l = gateway.findAnnotationLinks(ImageData.class.getName(), -1, 
					ids);
			List links = new ArrayList();
			//remove the links.
			if (l != null && l.size() > 0) links.addAll(l);
			l = gateway.findAnnotationLinks(DatasetData.class.getName(), -1, 
					ids);
			if (l != null && l.size() > 0) links.addAll(l);
			l = gateway.findAnnotationLinks(ProjectData.class.getName(), -1, 
					ids);
			if (l != null && l.size() > 0) links.addAll(l);
			if (links.size() > 0)
				gateway.deleteObjects(links);
			//not link, we can delete
			long originalFileID = ((FileAnnotationData) data).getFileID();
			gateway.deleteObject(
					gateway.findIObject(data.asIObject()));
			gateway.deleteObject(
					gateway.findIObject(OriginalFile.class.getName(), 
							originalFileID));
		} else if (data instanceof TagAnnotationData) {
			if (gateway.findIObject(data.asIObject()) == null)
				return result;
			TagAnnotationData t = (TagAnnotationData) data;
			String ns = t.getNameSpace();
			if (TagAnnotationData.INSIGHT_TAGSET_NS.equals(ns)) {
				deleteTagSet(object);
			} else {
				//For now: don't allow to delete content.
				//deleteTag(object);
				IObject io = gateway.findIObject(data.asIObject());
				deleteTagLinks(io);
				gateway.deleteObject(io);
			}
		}
		return result;
	}
	
	/**
	 * Removed the annotations links.
	 * 
	 * @param object
	 * @return See above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	private List<DeletableObject> deleteTagLinks(IObject object)
		throws DSOutOfServiceException, DSAccessException
	{
		//Delete the Tag-DataObject links
		long id = object.getId().getValue();
		List<Long> ids = new ArrayList<Long>();
		ids.add(id);
		List l = gateway.findAnnotationLinks(ImageData.class.getName(), -1, 
				ids);
		List links = new ArrayList();
		//remove the links.
		if (l != null && l.size() > 0) links.addAll(l);
		l = gateway.findAnnotationLinks(DatasetData.class.getName(), -1, 
				ids);
		if (l != null && l.size() > 0) links.addAll(l);
		l = gateway.findAnnotationLinks(ProjectData.class.getName(), -1, 
				ids);
		if (l != null && l.size() > 0) links.addAll(l);
		l = gateway.findAnnotationLinks(TagAnnotationData.class.getName(), -1, 
				ids);
		if (l != null && l.size() > 0) links.addAll(l);
		
		if (links.size() > 0)
			gateway.deleteObjects(links);
		context.getMetadataService().clearAnnotation(TagAnnotationData.class, 
									id, null);
		return null;
	}
	
	private List<DeletableObject> deleteTagSet(DeletableObject object)
		throws DSOutOfServiceException, DSAccessException
	{
		DataObject data = object.getObjectToDelete();
		long id = data.getId();
		boolean content = object.deleteContent();
		List<DeletableObject> results = new ArrayList<DeletableObject>();
		if (gateway.findIObject(data.asIObject()) == null)
			return results;
		List l = gateway.findAnnotationLinks(Annotation.class.getName(), id,
											null);
		
		List<Long> tagIds = new ArrayList<Long>();
		List<IObject> tags = new ArrayList<IObject>(); 
		if (content) {
			Iterator i = l.iterator();
			AnnotationAnnotationLink link;
			long tagID;
			while (i.hasNext()) {
				link =  (AnnotationAnnotationLink) i.next();
				tagID = link.getChild().getId().getValue();
				if (!tagIds.contains(tagID)) {
					tagIds.add(tagID);
					tags.add(link.getChild());
				}
			}
		}
		//Delete Tag Set-Tag links
		gateway.deleteObjects(l);
		
		//Delete tag links 
		if (tags.size() > 0) {
			Iterator<IObject> i = tags.iterator();
			IObject child;
			while (i.hasNext()) {
				child = i.next();
				deleteTagLinks(child);
			}
			//Delete the tags
			gateway.deleteObjects(tags);
		}
		//Delete Tag Set
		//Clear other type of linkages
		context.getMetadataService().clearAnnotation(TagAnnotationData.class, 
				data.getId(), null);
		gateway.deleteObject(gateway.findIObject(data.asIObject()));
		return results;	
	}
	
	/**
	 * Returns the collection of annotations to delete before deleting
	 * the object.
	 * 
	 * @param types The type of annotations to delete.
	 * @param annotations
	 * @param parentType
	 * @return See above.
	 */
	private List<IObject> annotationsLinkToDelete(List<Class> types, 
								List<IObject> annotations, Class parentType)
	{
		List<IObject> toDelete = new ArrayList<IObject>();
		if (types == null || types.size() == 0) return toDelete;
		List<Class> annoTypes = convert(types);
		
		if (annoTypes.size() == 0) return toDelete;
		Iterator k = annotations.iterator();
		IObject ann;
		IObject child = null;
		while (k.hasNext()) {
			ann = (IObject) k.next();
			if (ImageData.class.equals(parentType)) 
				child = ((ImageAnnotationLink) ann).getChild();
			else if (DatasetData.class.equals(parentType)) 
				child = ((DatasetAnnotationLink) ann).getChild();
			else if (ProjectData.class.equals(parentType))
				child = ((ProjectAnnotationLink) ann).getChild();
			else if (ScreenData.class.equals(parentType))
				child = ((ScreenAnnotationLink) ann).getChild();
			else if (PlateData.class.equals(parentType))
				child = ((PlateAnnotationLink) ann).getChild();
			else if (WellData.class.equals(parentType))
				child = ((WellAnnotationLink) ann).getChild();
			else if (WellSampleData.class.equals(parentType))
				child = ((WellSampleAnnotationLink) ann).getChild();
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
	 * Deletes the specified top container.
	 * 
	 * @param object  The object to handle.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	private List<DeletableObject> deleteTopContainer(DeletableObject object)
		throws DSOutOfServiceException, DSAccessException
	{
		DataObject data = object.getObjectToDelete();
		Class klass = null;
		List<DeletableObject> results = new ArrayList<DeletableObject>();
		if (data instanceof ProjectData) klass = ProjectData.class;
		else if (data instanceof ScreenData) klass = ScreenData.class;
		if (klass == null) 
			throw new omero.IllegalArgumentException("Method can only be" +
					"invoked to delete Project or Screen");
		if (gateway.findIObject(data.asIObject()) == null)
			return results;
		boolean attachment = object.deleteAttachment();
		long id = data.getId();
		List<IObject> list;
		List<IObject> annotations;
		List<IObject> annotatedByOthers;
		
		annotations = gateway.findAnnotationLinks(klass.getName(), id, null);
		annotatedByOthers = isRelatedToOther(annotations);
		if (annotatedByOthers.size() != 0) {
			//cannot delete. Notify user.
			object.setBlocker(annotatedByOthers);
			results.add(object);
			return results;
		} 
		/*
		if (!attachment) //want to keep the annotation.
			gateway.deleteObjects(annotations);
		else //remove the annotation from the object.
			context.getMetadataService().clearAnnotation(data);
		*/
		if (!attachment) //want to keep the annotation.
			gateway.deleteObjects(annotations);
		else {
			//remove the annotation from the object.
			List<IObject> toDelete = 
				annotationsLinkToDelete(object.getAttachmentTypes(), 
						annotations, klass);
			if (toDelete.size() > 0)
				gateway.deleteObjects(toDelete);
		}
		context.getMetadataService().clearAnnotation(data);
		
		//delete all the links
		List toDelete = new ArrayList();
		list = gateway.findLinks(data.asIObject(), null);
		if (list != null) toDelete.addAll(list);
		if (toDelete.size() > 0) gateway.deleteObjects(toDelete);
		//delete the dataset
		gateway.deleteObject(gateway.findIObject(data.asIObject()));
		return results;	
	}
	
	/**
	 * Deletes the dataset.
	 * 
	 * @param object
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
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
		if (gateway.findIObject(data.asIObject()) == null)
			return results;
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
						annotations, DatasetData.class);
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
		ParametersI param = new ParametersI();
		if (rootNodeIDs == null) param.exp(omero.rtypes.rlong(userID));
		if (withLeaves) param.leaves();
		else param.noLeaves();
		if (rootNodeIDs == null || rootNodeIDs.size() == 0) {
			if (ProjectData.class.equals(rootNodeType) || 
					ScreenData.class.equals(rootNodeType))
				param.orphan();
		}
		Set parents = gateway.loadContainerHierarchy(rootNodeType, rootNodeIDs,
				param); 
		return parents;                            
	}

	/** 
	 * Implemented as specified by {@link OmeroDataService}. 
	 * @see OmeroDataService#loadTopContainerHierarchy(Class, long)
	 */
	public Set loadTopContainerHierarchy(Class rootNodeType, long userID)
		throws DSOutOfServiceException, DSAccessException 
	{
		ParametersI param = new ParametersI();
		param.exp(omero.rtypes.rlong(userID));
		return gateway.loadContainerHierarchy(rootNodeType, null, param);                         
	}

	/** 
	 * Implemented as specified by {@link OmeroDataService}. 
	 * @see OmeroDataService#findContainerHierarchy(Class, List, long)
	 */
	public Set findContainerHierarchy(Class rootNodeType, List leavesIDs, 
			long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		ParametersI po = new ParametersI();
		po.leaves();
		po.exp(omero.rtypes.rlong(userID));
		return gateway.findContainerHierarchy(rootNodeType, leavesIDs, po);
	}
	
	/** 
	 * Implemented as specified by {@link OmeroDataService}. 
	 * @see OmeroDataService#getImages(Class, List, long)
	 */
	public Set getImages(Class nodeType, List nodeIDs, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		if (nodeType == null)
			throw new IllegalArgumentException("No type specified.");
		ParametersI po = new ParametersI();
		po.exp(omero.rtypes.rlong(userID));
		return gateway.getContainerImages(nodeType, nodeIDs, po);
	}

	/** 
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#getExperimenterImages(long)
	 */
	public Set getExperimenterImages(long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		ParametersI po = new ParametersI();
		po.exp(omero.rtypes.rlong(userID));
		return gateway.getUserImages(po);
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
		//if (rootNodeType.equals(TagAnnotationData.class))
			//return gateway.getDataObjectsTaggedCount(rootNodeIDs);
		return gateway.getCollectionCount(rootNodeType, property, rootNodeIDs, 
				new Parameters());
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

		IObject created = gateway.createObject(obj);
		IObject link;
		/*
		if (child instanceof TagAnnotationData) {
			//add description.
			TagAnnotationData tag = (TagAnnotationData) child;
			TextualAnnotationData desc = tag.getTagDescription();
			if (desc != null) {
				OmeroMetadataService service = context.getMetadataService(); 
				service.annotate(TagAnnotationData.class, 
						created.getId().getValue(), desc);
			}
		}
		*/
		if (parent != null) {
			link = ModelMapper.linkParentToChild(created, parent.asIObject());
			if ((child instanceof TagAnnotationData) && link != null) {
				gateway.createObject(link);
			}
		}
			
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
				gateway.createObjects(links);
		}
		return PojoMapper.asDataObject(created);
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
		IObject updated = gateway.updateObject(ho, new Parameters());
		return PojoMapper.asDataObject(updated);
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#addExistingObjects(DataObject, Collection)
	 */
	public void addExistingObjects(DataObject parent, Collection children)
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
		} else if (parent instanceof TagAnnotationData) {
			TagAnnotationData tagSet = (TagAnnotationData) parent;
			if (!TagAnnotationData.INSIGHT_TAGSET_NS.equals(
					tagSet.getNameSpace()))
				throw new IllegalArgumentException("Parent not supported");
			Iterator i = children.iterator();
			TagAnnotationData tag;
			Object object;
			while (i.hasNext()) {
				object = i.next();
				if (!(object instanceof TagAnnotationData))
					throw new IllegalArgumentException(
					"items can only be Tag.");
				tag = (TagAnnotationData) object;
				if (tag.getNameSpace() != null)
					throw new IllegalArgumentException(
					"items can only be Tag.");
					
			}
		} else
			throw new IllegalArgumentException("Parent not supported");

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
		if (objects.size() != 0)
			gateway.createObjects(objects);
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#cutAndPaste(Map, Map)
	 */
	public void cutAndPaste(Map toPaste, Map toCut)
		throws DSOutOfServiceException, DSAccessException
	{
		if (toPaste == null) toPaste = new HashMap();
		if (toCut == null) toCut = new HashMap();
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
		if (pixels == null) return new ArrayList<ChannelData>();
		Collection l = pixels.copyChannels();
		if (l == null) return new ArrayList<ChannelData>();
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
	 * @see OmeroDataService#getSpace(int, long)
	 */
	public long getSpace(int index, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		switch (index) {
			case OmeroDataService.USED: return gateway.getUsedSpace();
			case OmeroDataService.FREE: return gateway.getFreeSpace();
		}
		return -1;
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#getImagesPeriod(Timestamp, Timestamp, long, boolean)
	 */
	public Collection getImagesPeriod(Timestamp startTime, Timestamp endTime, 
								long userID, boolean asDataObject)
		throws DSOutOfServiceException, DSAccessException
	{
		if (startTime == null && endTime == null)
			throw new NullPointerException("Time not specified.");
		
		ParametersI po = new ParametersI();
		po.leaves();
		po.exp(omero.rtypes.rlong(userID));
		if (startTime != null) 
			po.startTime(omero.rtypes.rtime(startTime.getTime()));
		if (endTime != null) 
			po.endTime(omero.rtypes.rtime(endTime.getTime()));
		return gateway.getImages(po, asDataObject);
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
		Collection imgs = getImagesPeriod(startTime, endTime, userID, false);
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
									value, new Parameters());
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
			else if (TagAnnotationData.class.equals(type))
				parentClass = TagAnnotation.class;
			else if (FileAnnotationData.class.equals(type))
				parentClass = FileAnnotation.class;
			if (parentClass == null) return new HashSet();
			List links = gateway.findLinks(parentClass, id, userID);
			if (links == null) return new HashSet();
			Iterator i = links.iterator();
			Set<DataObject> nodes = new HashSet<DataObject>();
			IObject object, parent = null;
			DataObject data;
			List<Long> ids = new ArrayList<Long>();
			long parentId;
			IObject link;
			while (i.hasNext()) {
				if (parentClass.equals(Project.class))
					parent = ((ProjectDatasetLink) i.next()).getParent();
				else if (parentClass.equals(Dataset.class))
					parent = ((DatasetImageLink) i.next()).getParent();
				else if (parentClass.equals(Screen.class))
					parent = ((ScreenPlateLink) i.next()).getParent();
				else if (parentClass.equals(TagAnnotation.class)) 
					parent = ((AnnotationAnnotationLink) i.next()).getParent();
				else if (parentClass.equals(FileAnnotation.class)) {
					link = (IObject) i.next();
					if (link instanceof ProjectAnnotationLink)
						parent = ((ProjectAnnotationLink) link).getParent();
					else if (link instanceof DatasetAnnotationLink)
						parent = ((DatasetAnnotationLink) link).getParent();
					else if (link instanceof ImageAnnotationLink)
						parent = ((ImageAnnotationLink) link).getParent();
				}
				parentId = parent.getId().getValue();
				if (!ids.contains(parentId)) {
					object = gateway.findIObject(parent.getClass().getName(), 
							parent.getId().getValue());
					data = PojoMapper.asDataObject(object);
					if (TagAnnotation.class.equals(parentClass)) {
						if (data instanceof TagAnnotationData) {
							TagAnnotationData tag = (TagAnnotationData) data;
							if (TagAnnotationData.INSIGHT_TAGSET_NS.equals(
									tag.getNameSpace())) {
								nodes.add(data);
							}
						}
					} else nodes.add(data);
					ids.add(parentId);
				}
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
	 * @see OmeroDataService#loadPlateWells(long, long, long)
	 */
	public Collection loadPlateWells(long plateID, long acquisitionID,
			long userID) 
		throws DSOutOfServiceException, DSAccessException
	{
		return gateway.loadPlateWells(plateID, acquisitionID, userID);
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
	 * @see OmeroDataService#getServerVersion()
	 */
	public String getServerVersion()
	{
		try {
			return gateway.getServerVersion();
		} catch (Exception e) {
			//ignore it.
		}
		return "";
	}
	
}
