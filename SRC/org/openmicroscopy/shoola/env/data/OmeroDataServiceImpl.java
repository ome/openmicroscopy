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
import ome.model.ILink;
import ome.model.IObject;
import ome.model.containers.Category;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Channel;
import ome.model.core.Image;
import ome.model.meta.Event;
import ome.util.builders.PojoOptions;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.AgentInfo;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.login.UserCredentials;
import org.openmicroscopy.shoola.env.data.model.ChannelMetadata;
import org.openmicroscopy.shoola.env.data.model.Mapper;
import org.openmicroscopy.shoola.env.data.util.ModelMapper;
import org.openmicroscopy.shoola.env.data.util.PojoMapper;
import org.openmicroscopy.shoola.env.data.util.SearchDataContext;
import pojos.AnnotationData;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.ProjectData;
import pojos.TagAnnotationData;
import pojos.TextualAnnotationData;
import pojos.URLAnnotationData;

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
	 * Helper method to return the user's details.
	 * 
	 * @return See above.
	 */
	private ExperimenterData getUserDetails()
	{
		return (ExperimenterData) context.lookup(
				LookupNames.CURRENT_USER_DETAILS);
	}

	/**
	 * Checks if the specified classification algorithm is supported.
	 * 
	 * @param algorithm The passed index.
	 * @return <code>true</code> if the algorithm is supported.
	 */
	private boolean checkAlgorithm(int algorithm)
	{
		switch (algorithm) {
			case OmeroDataService.DECLASSIFICATION:
			case OmeroDataService.CLASSIFICATION_ME:
			case OmeroDataService.CLASSIFICATION_NME:    
				return true;
		}
		return false;
	}

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
		if (links != null) {
			gateway.deleteObjects((IObject[]) 
					links.toArray(new IObject[links.size()]));
		} 
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
	 * @see OmeroDataService#loadContainerHierarchy(Class, Set, boolean, long)
	 */
	public Set loadContainerHierarchy(Class rootNodeType, Set rootNodeIDs,
			boolean withLeaves, long userID)
		throws DSOutOfServiceException, DSAccessException 
	{
		PojoOptions po = new PojoOptions();
		if (rootNodeIDs == null) po.exp(new Long(userID));
		if (withLeaves) po.leaves();
		else po.noLeaves();
		//po.countsFor(new Long(userID));
		//If rootNodeIDs, returns the orphaned containers:
		Set parents = gateway.loadContainerHierarchy(rootNodeType, rootNodeIDs,
				po.map()); 
		if (rootNodeIDs == null && parents != null) {
			Class klass = null;
			if (rootNodeType.equals(ProjectData.class))
				klass = DatasetData.class;
			else if (rootNodeType.equals(CategoryGroupData.class))
				klass = CategoryData.class;
			
			if (klass != null) {
				po = new PojoOptions(); 
				po.exp(new Long(userID));
				//options.allCounts();
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
					else 
						children.addAll(
								((CategoryGroupData) parent).getCategories());
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
		//po.allCounts();
		po.exp(new Long(userID));
		po.noLeaves();
		//po.countsFor(new Long(userID));
		return gateway.loadContainerHierarchy(rootNodeType, null,
				po.map());                         
	}

	/** 
	 * Implemented as specified by {@link OmeroDataService}. 
	 * @see OmeroDataService#findContainerHierarchy(Class, Set, long)
	 */
	public Set findContainerHierarchy(Class rootNodeType, Set leavesIDs, 
			long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		try {
			PojoOptions po = new PojoOptions();
			po.leaves();
			po.exp(new Long(userID));
			//po.countsFor(new Long(userID));
			return gateway.findContainerHierarchy(rootNodeType, leavesIDs,
					po.map());
		} catch (Exception e) {
			throw new DSAccessException(e.getMessage());
		}
	}

	/** 
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#findAnnotations(Class, Set, Set, boolean)
	 */
	public Map findAnnotations(Class nodeType, Set nodeIDs, Set annotatorIDs,
			boolean forUser)
		throws DSOutOfServiceException, DSAccessException
	{
		PojoOptions po = new PojoOptions();
		po.noLeaves();
		if (forUser) po.exp(new Long(getUserDetails().getId()));
		return gateway.findAnnotations(nodeType, nodeIDs, annotatorIDs, 
				new PojoOptions().map());
	}

	/** 
	 * Implemented as specified by {@link OmeroDataService}. 
	 * @see OmeroDataService#findCGCPaths(Set, int, long)
	 */
	public Set findCGCPaths(Set imgIDs, int algorithm, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		if (!checkAlgorithm(algorithm)) 
			throw new IllegalArgumentException("Find CGCPaths algorithm not " +
			"supported.");
		PojoOptions po = new PojoOptions();
		po.exp(new Long(userID));
		return gateway.findCGCPaths(imgIDs, algorithm, po.map());
	}

	/** 
	 * Implemented as specified by {@link OmeroDataService}. 
	 * @see OmeroDataService#getImages(Class, Set, long)
	 */
	public Set getImages(Class nodeType, Set nodeIDs, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		PojoOptions po = new PojoOptions();
		//po.leaves();
		//PojoOptions po = new PojoOptions();
		//if (rootNodeIDs == null) po.exp(new Long(userID));
		//if (withLeaves) po.leaves();
		//else 
		
		//po.countsFor(new Long(userID));
		
		return gateway.getContainerImages(nodeType, nodeIDs, po.map());
	}

	/** 
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#getExperimenterImages(long)
	 */
	public Set getExperimenterImages(long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		PojoOptions po = new PojoOptions();
		//po.noCounts();
		po.exp(new Long(userID));
		return gateway.getUserImages(po.map());
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#getCollectionCount(Class, String, Set)
	 */
	public Map getCollectionCount(Class rootNodeType, String property, Set 
			rootNodeIDs)
		throws DSOutOfServiceException, DSAccessException
	{
		if (!(property.equals(IMAGES_PROPERTY)))
			throw new IllegalArgumentException("Property not supported.");
		if (rootNodeType.equals(TagAnnotationData.class))
			return gateway.getImagesTaggedCount(rootNodeIDs);
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
		/*
		if (!(annotatedObject instanceof ImageData) && 
				!(annotatedObject instanceof DatasetData))
			throw new IllegalArgumentException("This method only supports " +
			"ImageData and DatasetData objects.");
			*/
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
			throw new NullPointerException("Cannot convert object.");
		Map options = (new PojoOptions()).map();

		IObject created = gateway.createObject(obj, options);
		if (parent != null)
			ModelMapper.linkParentToChild(created, parent.asIObject());
		if (children != null && children.size() > 0) {
			Iterator i = children.iterator();
			Object node;
			List<ILink> links = new ArrayList<ILink>();
			while (i.hasNext()) {
				node = i.next();
				if (node instanceof DataObject) {
					links.add(ModelMapper.linkParentToChild(
							((DataObject) node).asIObject(), created));
				}
			}
			if (links.size() > 0) {
				IObject[] array = new IObject[links.size()];
				int index = 0;
				i = links.iterator();
				while (i.hasNext()) {
					array[index] = (IObject) i.next();
					index++;
				}
				gateway.createObjects(array, options);
			}
			
			
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
			int index = 0;
			IObject[] ioObjects  = new IObject[children.size()];
			IObject o;
			while (i.hasNext()) {
				o = gateway.findIObject(((DataObject) i.next()).asIObject());
				ioObjects[index] = o;
				index++;
			}
			gateway.deleteObjects(ioObjects);
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
	 * @see OmeroDataService#classify(Set, Set)
	 */
	public Set classify(Set<ImageData> images, Set<CategoryData> categories)
		throws DSOutOfServiceException, DSAccessException
	{
		if (images == null)
			throw new IllegalArgumentException("No images to classify.");
		if (categories == null)
			throw new IllegalArgumentException("No categories specified.");
		Iterator category = categories.iterator();
		Iterator image;
		List<ILink> objects = new ArrayList<ILink>();
		IObject ioParent, ioChild;
		while (category.hasNext()) {
			ioParent = ((DataObject) category.next()).asIObject();
			image = images.iterator();
			while (image.hasNext()) {
				ioChild = ((DataObject) image.next()).asIObject();
				objects.add(ModelMapper.linkParentToChild(ioChild, ioParent));
			}   
		}
		if (objects.size() != 0) {
			Iterator i = objects.iterator();
			IObject[] array = new IObject[objects.size()];
			int index = 0;
			while (i.hasNext()) {
				array[index] = (IObject) i.next();
				index++;
			}
			gateway.createObjects(array, (new PojoOptions()).map());
		}
		Iterator i = images.iterator();
		Set<Long> ids = new HashSet<Long>(images.size());
		while (i.hasNext()) {
			ids.add(new Long(((DataObject) i.next()).getId()));
		}
		return getImages(ImageData.class, ids, -1);
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#declassify(Set, Set)
	 */
	public Set declassify(Set<ImageData> images, Set<CategoryData> categories)
		throws DSOutOfServiceException, DSAccessException
	{
		if (images == null)
			throw new IllegalArgumentException("No images to classify.");
		if (categories == null)
			throw new IllegalArgumentException("No categories specified.");
		Iterator category = categories.iterator();
		while (category.hasNext())
			cut((DataObject) category.next(), images);
		Iterator i = images.iterator();
		Set<Long> ids = new HashSet<Long>(images.size());
		while (i.hasNext()) 
			ids.add(new Long(((DataObject) i.next()).getId()));

		return getImages(ImageData.class, ids, -1);
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#loadExistingObjects(Class, Set, long)
	 */
	public Set loadExistingObjects(Class nodeType, Set nodeIDs, long userID)
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
		} else if (nodeType.equals(CategoryGroupData.class)) {
			Set in = loadContainerHierarchy(nodeType, nodeIDs, true, userID);
			all = loadContainerHierarchy(CategoryData.class, null, true, 
					userID);
			Iterator i = in.iterator();
			Iterator j; 
			while (i.hasNext()) {
				j = (((CategoryGroupData) i.next()).getCategories()).iterator();
				while (j.hasNext()) {
					objects.add(new Long(((CategoryData) j.next()).getId()));
				} 
			}
		} else if ((nodeType.equals(DatasetData.class)) || 
				(nodeType.equals(CategoryData.class))) {
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
		} else if (parent instanceof CategoryGroupData) {
			try {
				children.toArray(new CategoryData[] {});
			} catch (ArrayStoreException ase) {
				throw new IllegalArgumentException(
						"items can only be categories.");
			}
		} else if (parent instanceof CategoryData) {
			try {
				children.toArray(new ImageData[] {});
			} catch (ArrayStoreException ase) {
				throw new IllegalArgumentException(
						"items can only be images.");
			}
		} else
			throw new IllegalArgumentException("parent object not supported");

		List<ILink> objects = new ArrayList<ILink>();
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
			Iterator i = objects.iterator();
			IObject[] array = new IObject[objects.size()];
			int index = 0;
			while (i.hasNext()) {
				array[index] = (IObject) i.next();
				index++;
			}
			gateway.createObjects(array, (new PojoOptions()).map());
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
		Collection l = gateway.getChannelsData(pixelsID);
		Iterator i = l.iterator();
		List<ChannelMetadata> m = new ArrayList<ChannelMetadata>(l.size());
		int index = 0;
		while (i.hasNext()) {
			m.add(Mapper.mapChannel(index, (Channel) i.next()));
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
		for (int j = 0; j < objects.length; j++) {
			results.add(PojoMapper.asDataObject(
					ModelMapper.getAnnotatedObject(objects[j])));
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
	 * @see OmeroDataService#getAvailableGroups()
	 */
	public Map<GroupData, Set> getAvailableGroups() 
		throws DSOutOfServiceException, DSAccessException
	{
		return gateway.getAvailableGroups();
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
	 * @see OmeroDataService#classifyChildren(Set, Set)
	 */
	public Set classifyChildren(Set containers, Set categories) 
		throws DSOutOfServiceException, DSAccessException
	{
		if (containers == null || containers.size() == 0)
			throw new IllegalArgumentException("No containers specified."); 
		if (categories == null)
			throw new IllegalArgumentException("No categories specified."); 
//		Tmp solution
		ExperimenterData exp = getUserDetails();
		Iterator i = containers.iterator();
		Set images = null;
		DataObject object;
		Class klass = null;
		Set<Long> ids;
		Set<DataObject> results = new HashSet<DataObject>();
		while (i.hasNext()) {
			object = (DataObject) i.next();
			ids = new HashSet<Long>(1);
			ids.add(new Long(object.getId()));
			if (object instanceof DatasetData) {
				klass = DatasetData.class;
			} else if (object instanceof CategoryData) {
				klass = CategoryData.class;
			}
			if (klass != null)
				images = getImages(klass, ids, exp.getId());
			if (images != null) {
				results.addAll(classify(images, categories));
			}
		}
		return results;
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#tagImagesIncontainers(Set, Class, Set)
	 */
	public Set tagImagesIncontainers(Set<Long> ids, Class rootType, 
									Set<CategoryData> tags)
		throws DSOutOfServiceException, DSAccessException 
	{
		ExperimenterData exp = getUserDetails();
		Iterator i = ids.iterator();
		Set images = null;
		Set<Long> nodes;
		Set<DataObject> results = new HashSet<DataObject>();
		while (i.hasNext()) {
			nodes = new HashSet<Long>(1);
			nodes.add((Long) i.next());
			images = getImages(rootType, ids, exp.getId());
			if (images != null) {
				results.addAll(classify(images, tags));
			}
		}
		return results;
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
		Set<Long> ids;
		DataObject image;
		List<DataObject> results = new ArrayList<DataObject>();
		PojoOptions po = new PojoOptions();
		po.allExps();
		Map map = po.map();
		while (i.hasNext()) {
			object = (DataObject) i.next();
			ids = new HashSet<Long>(1);
			ids.add(new Long(object.getId()));
			if (object instanceof DatasetData) {
				klass = DatasetData.class;
			} else if (object instanceof CategoryData) {
				klass = CategoryData.class;
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

		gateway.changePassword(uc.getUserName(), newPassword);
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
		po.exp(new Long(userID));
		po.startTime(startTime);
		po.endTime(endTime);
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
			times.add(evt.getTime());
		}
		return times;
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#findCategoryPaths(long, boolean, long)
	 */
	public Set findCategoryPaths(long imageID, boolean leaves, long userID) 
		throws DSOutOfServiceException, DSAccessException
	{
		List links = gateway.findLinks(Category.class, imageID, userID);
		if (links == null || links.size() == 0) return new HashSet();
		Iterator i = links.iterator();
		Set<Long> ids = new HashSet<Long>(links.size());
		long id;
		while (i.hasNext()) {
			id = ((ILink) i.next()).getParent().getId();
			ids.add(id);
		}
		return loadContainerHierarchy(CategoryData.class, ids, leaves, userID);
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#findCategoryPaths(Set, boolean, long)
	 */
	public Set findCategoryPaths(Set<Long> imagesID, boolean leaves, 
			long userID) 
		throws DSOutOfServiceException, DSAccessException
	{
		if (imagesID == null)
			throw new IllegalArgumentException("No images specified.");
		List links = gateway.findLinks(Category.class, imagesID, userID);
		if (links == null || links.size() == 0) return new HashSet();
		Iterator i = links.iterator();
		Set<Long> ids = new HashSet<Long>(links.size());
		while (i.hasNext()) 
			ids.add(((ILink) i.next()).getParent().getId());
		
		return loadContainerHierarchy(CategoryData.class, ids, leaves, userID);
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
		if (result instanceof Integer) return result;
		//Should returns a search context for the moment.
		//collection of images only.
		Map m = (Map) result;
		Iterator i = m.keySet().iterator();
		Class key;
		Set value;
		Iterator k;
		Set<Long> imageIDs = new HashSet<Long>();
		Set images;
		DataObject img;
		List owners = context.getOwners();
		List<Long> ownerIDs = new ArrayList<Long>(owners.size());
		k = owners.iterator();
		while (k.hasNext()) {
			ownerIDs.add(((DataObject) k.next()).getId());
		}
		Map<Class, Object> results = new HashMap<Class, Object>();
		Set<DataObject> nodes;
		while (i.hasNext()) {
			key = (Class) i.next();
			value = (Set) m.get(key);
			nodes = new HashSet<DataObject>(); 
			results.put(key, nodes);
			if (value.size() > 0) {
				if (key.equals(String.class)) {
					images = gateway.getContainerImages(ImageData.class, value, 
							new PojoOptions().map());
					k = images.iterator();
					while (k.hasNext()) {
						img = (DataObject) k.next();
						if (!imageIDs.contains(img.getId())) {
							if (ownerIDs.contains(img.getOwner().getId())) {
								imageIDs.add(img.getId());
								nodes.add(img);
							}
						}
					}
				} else if (key.equals(TagAnnotationData.class) ||
						key.equals(TextualAnnotationData.class) ||
						key.equals(URLAnnotationData.class) ||
						key.equals(FileAnnotationData.class)) {
					//Retrieve all the images linked to the annotation
					if (value.size() > 0) {
						images = gateway.getAnnotatedObjects(ImageData.class, 
								    value);
						k = images.iterator();
						while (k.hasNext()) {
							img = (DataObject) k.next();
							if (!imageIDs.contains(img.getId())) {
								if (ownerIDs.contains(img.getOwner().getId())) {
									imageIDs.add(img.getId());
									nodes.add(img);
								}
							}
						}
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
			if (parentClass == null) return new HashSet();
			List links = gateway.findLinks(parentClass, id, userID);
			if (links == null) return new HashSet();
			Iterator i = links.iterator();
			Set<DataObject> nodes = new HashSet<DataObject>();
			ILink link;
			IObject object, parent;
			while (i.hasNext()) {
				link = (ILink) i.next();
				parent = link.getParent();
				object = gateway.findIObject(parent.getClass(), parent.getId());
				nodes.add(PojoMapper.asDataObject(object));
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

}
