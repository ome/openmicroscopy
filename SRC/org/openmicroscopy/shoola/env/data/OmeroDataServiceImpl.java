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
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import ome.model.IObject;
import ome.model.core.Channel;
import ome.model.core.OriginalFile;
import ome.util.builders.PojoOptions;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.ChannelMetadata;
import org.openmicroscopy.shoola.env.data.util.ModelMapper;
import org.openmicroscopy.shoola.env.data.util.PojoMapper;
import pojos.AnnotationData;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.ProjectData;

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
     * Creates a <code>File</code> from the passed array of bytes.
     * 
     * @param name		The name of the file.
     * @param values    The array of bytes.
     * @return See above.
     * @throws DSAccessException If we cannot create the file.
     */
    private File createFile(String name, byte[] values) 
        throws DSAccessException
    {
    	File f = new File(name);
    	try {
    		FileOutputStream stream = new FileOutputStream(f);
    		stream.write(values);
    		stream.close();
    		return f;
    	} catch (Exception e) {
    		f.delete();
    		throw new DSAccessException("Cannot create the file", e);
    	}
    }
    
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
     * Sets the root context of the options depending on the specified
     * level and ID.
     * 
     * @param po		The {@link PojoOptions} to handle.
     * @param rootLevel	The level of the root, either {@link GroupData}, 
     *                  {@link ExperimenterData}.
     * @param rootID	The ID of the root if needed.
     */
    private void setRootOptions(PojoOptions po, Class rootLevel, long rootID)
    {
        if (rootLevel.equals(GroupData.class))
            po.grp(new Long(rootID));
        else if (rootLevel.equals(ExperimenterData.class))
            po.exp(new Long(rootID));
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
     * @see OmeroDataService#loadContainerHierarchy(Class, Set, boolean, Class, 
     * 												long)
     */
    public Set loadContainerHierarchy(Class rootNodeType, Set rootNodeIDs,
                                    boolean withLeaves, Class rootLevel, 
                                    long rootLevelID)
        throws DSOutOfServiceException, DSAccessException 
    {
        PojoOptions po = new PojoOptions();
        po.allCounts();
        setRootOptions(po, rootLevel, rootLevelID);
        if (withLeaves) po.leaves();
        else po.noLeaves();
        if (rootLevel.equals(GroupData.class))
        	po.countsFor(new Long(getUserDetails().getId()));
        else po.countsFor(new Long(rootLevelID));
        return gateway.loadContainerHierarchy(rootNodeType, rootNodeIDs,
                                            po.map());                              
    }

    /** 
     * Implemented as specified by {@link OmeroDataService}. 
     * @see OmeroDataService#findContainerHierarchy(Class, Set, Class, long)
     */
    public Set findContainerHierarchy(Class rootNodeType, Set leavesIDs, 
                                    Class rootLevel, long rootLevelID)
        throws DSOutOfServiceException, DSAccessException
    {
    	
        try {
            PojoOptions po = new PojoOptions();
            po.leaves();
            setRootOptions(po, rootLevel, rootLevelID);
            if (rootLevel.equals(GroupData.class))
            	po.countsFor(new Long(getUserDetails().getId()));
            else po.countsFor(new Long(rootLevelID));
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
        po.noCounts();
        po.noLeaves();
        if (forUser) po.exp(new Long(getUserDetails().getId()));
        return gateway.findAnnotations(nodeType, nodeIDs, annotatorIDs, 
                new PojoOptions().map());
    }

    /** 
     * Implemented as specified by {@link OmeroDataService}. 
     * @see OmeroDataService#findCGCPaths(Set, int, Class, long)
     */
    public Set findCGCPaths(Set imgIDs, int algorithm, Class rootLevel,
                            long rootLevelID)
        throws DSOutOfServiceException, DSAccessException
    {
        if (!checkAlgorithm(algorithm)) 
            throw new IllegalArgumentException("Find CGCPaths algorithm not " +
                    "supported.");
        PojoOptions po = new PojoOptions();
        po.noCounts();
        po.exp(new Long(getUserDetails().getId()));
        return gateway.findCGCPaths(imgIDs, algorithm, po.map());
    }
    
    /** 
     * Implemented as specified by {@link OmeroDataService}. 
     * @see OmeroDataService#getImages(Class, Set, Class, long)
     */
    public Set getImages(Class nodeType, Set nodeIDs, Class rootLevel, 
            			long rootLevelID)
        throws DSOutOfServiceException, DSAccessException
    {
        PojoOptions po = new PojoOptions();
        po.allCounts();
        setRootOptions(po, rootLevel, rootLevelID);
        if (rootLevel.equals(GroupData.class))
        	po.countsFor(new Long(getUserDetails().getId()));
        else po.countsFor(new Long(rootLevelID));
        return gateway.getContainerImages(nodeType, nodeIDs, po.map());
    }
    
    /** 
     * Implemented as specified by {@link OmeroDataService}.
     * @see OmeroDataService#getImagesFor(long)
     */
    public Set getImagesFor(long userID)
        throws DSOutOfServiceException, DSAccessException
    {
        PojoOptions po = new PojoOptions();
        po.noCounts();
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
        PojoOptions po = new PojoOptions();
        po.noCounts();
        if (!(property.equals(IMAGES_PROPERTY)))
            throw new IllegalArgumentException("Property not supported.");
        Map m = gateway.getCollectionCount(rootNodeType, property, rootNodeIDs, 
                po.map());
        return m;
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
        if (!(annotatedObject instanceof ImageData) && 
                !(annotatedObject instanceof DatasetData))
            throw new IllegalArgumentException("This method only supports " +
                    "ImageData and DatasetData objects.");
        //First make sure the annotated object is current.
        IObject ho = gateway.findIObject(annotatedObject.asIObject());
        if (ho == null) return null;
        IObject object = gateway.createObject(
                				ModelMapper.createAnnotation(ho, data), 
                				(new PojoOptions()).map());
        return PojoMapper.asDataObject(ModelMapper.getAnnotatedObject(object));
    }

    /**
     * Implemented as specified by {@link OmeroDataService}.
     * @see OmeroDataService#removeAnnotationFrom(DataObject, AnnotationData)
     */
    public DataObject removeAnnotationFrom(DataObject annotatedObject, 
                                            AnnotationData data)
            throws DSOutOfServiceException, DSAccessException
    {
        if (data == null) 
            throw new IllegalArgumentException("No annotation to delete.");
        if (annotatedObject == null) 
            throw new IllegalArgumentException("No annotated DataObject."); 
        if (!(annotatedObject instanceof ImageData) && 
                !(annotatedObject instanceof DatasetData))
            throw new IllegalArgumentException("This method only supports " +
                    "ImageData and DatasetData objects.");
        //First make sure the data object is current;
        IObject ho = gateway.findIObject(data.asIObject());
        if (ho != null) gateway.deleteObject(ho);
        
        return updateDataObject(annotatedObject);
    }

    /**
     * Implemented as specified by {@link OmeroDataService}.
     * @see OmeroDataService#updateAnnotationFor(DataObject, AnnotationData)
     */
    public DataObject updateAnnotationFor(DataObject annotatedObject,
                                          AnnotationData data)
            throws DSOutOfServiceException, DSAccessException
    {
        if (data == null) 
            throw new IllegalArgumentException("No annotation to update.");
        if (annotatedObject == null) 
            throw new IllegalArgumentException("No annotated DataObject.");
        if (!(annotatedObject instanceof ImageData) && 
                !(annotatedObject instanceof DatasetData))
            throw new IllegalArgumentException("This method only supports " +
                    "ImageData and DatasetData objects.");
        Map options = (new PojoOptions()).map();
        IObject object = annotatedObject.asIObject();
        IObject ho = gateway.findIObject(object);
        if (ho == null)  return null;
        
        ModelMapper.fillIObject(object, ho);
        ModelMapper.unloadCollections(ho);
        IObject updated = gateway.updateObject(ho, options);
        IObject toUpdate = data.asIObject();
        ModelMapper.setAnnotatedObject(updated, toUpdate);
        gateway.updateObject(toUpdate, options);
        return PojoMapper.asDataObject(updated);
    }

    /**
     * Implemented as specified by {@link OmeroDataService}.
     * @see OmeroDataService#createDataObject(DataObject, DataObject)
     */
    public DataObject createDataObject(DataObject child, DataObject parent)
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
        if (parent != null) {
            ModelMapper.linkParentToNewChild(created, parent.asIObject());
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
        IObject oldObject = object.asIObject();
        IObject ho = gateway.findIObject(object.asIObject());
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
    public Set classify(Set images, Set categories)
            throws DSOutOfServiceException, DSAccessException
    {
        try {
            images.toArray(new ImageData[] {});
        } catch (ArrayStoreException ase) {
            throw new IllegalArgumentException(
                    "images only contains ImageData elements.");
        }
        try {
            categories.toArray(new CategoryData[] {});
        } catch (ArrayStoreException ase) {
            throw new IllegalArgumentException(
                    "categories only contains CategoryData elements.");
        }
        
        Iterator category = categories.iterator();
        Iterator image;
        List objects = new ArrayList();
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
        Set ids = new HashSet(images.size());
        while (i.hasNext()) {
        	ids.add(new Long(((DataObject) i.next()).getId()));
		}
        return getImages(ImageData.class, ids, ExperimenterData.class, -1);
    }

    /**
     * Implemented as specified by {@link OmeroDataService}.
     * @see OmeroDataService#declassify(Set, Set)
     */
    public Set declassify(Set images, Set categories)
            throws DSOutOfServiceException, DSAccessException
    {
        try {
            images.toArray(new ImageData[] {});
        } catch (ArrayStoreException ase) {
            throw new IllegalArgumentException(
                    "The images set only contains ImageData elements.");
        }
        try {
            categories.toArray(new CategoryData[] {});
        } catch (ArrayStoreException ase) {
            throw new IllegalArgumentException(
                    "The categories set only contains CategoryData elements.");
        }
        Iterator category = categories.iterator();
        while (category.hasNext())
            cut((DataObject) category.next(), images);
        Iterator i = images.iterator();
        Set<Long> ids = new HashSet<Long>(images.size());
        while (i.hasNext()) 
        	ids.add(new Long(((DataObject) i.next()).getId()));
		
        return getImages(ImageData.class, ids, ExperimenterData.class, -1);
    }

    /**
     * Implemented as specified by {@link OmeroDataService}.
     * @see OmeroDataService#loadExistingObjects(Class, Set, Class, long)
     */
    public Set loadExistingObjects(Class nodeType, Set nodeIDs, Class rootLevel,
                                long rootID)
            throws DSOutOfServiceException, DSAccessException
    {
        Set all = null;
        Set<Long> objects = new HashSet<Long>();
        if (nodeType.equals(ProjectData.class)) {
            Set in = loadContainerHierarchy(nodeType, nodeIDs, true, rootLevel, 
                                            rootID);
            all = loadContainerHierarchy(DatasetData.class, null, true, 
                                            rootLevel, rootID);
            Iterator i = in.iterator();
            Iterator j; 
            while (i.hasNext()) {
                j = (((ProjectData) i.next()).getDatasets()).iterator();
                while (j.hasNext()) {
                    objects.add(new Long(((DatasetData) j.next()).getId()));
                } 
            }
        } else if (nodeType.equals(CategoryGroupData.class)) {
            Set in = loadContainerHierarchy(nodeType, nodeIDs, true, rootLevel, 
                    rootID);
            all = loadContainerHierarchy(CategoryData.class, null, true, 
                                rootLevel, rootID);
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
            Set in = getImages(nodeType, nodeIDs, rootLevel, rootID);
            all = getImagesFor(rootID);
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
        
        List objects = new ArrayList();
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
        List l = gateway.getChannelsData(pixelsID);
        Iterator i = l.iterator();
        List metadata = new ArrayList(l.size());
        int index = 0;
        while (i.hasNext()) {
            metadata.add(new ChannelMetadata(index, (Channel) i.next()));
            index++;
        }
        return metadata;
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
        		toCreate[index] = ModelMapper.createAnnotation(ho, d);
            	objects[index] = gateway.createObject(toCreate[index], 
            	        					(new PojoOptions()).map());
            	index++;
        	}
        	
		}
        //IObject[] objects = gateway.createObjects(toCreate, 
        //					(new PojoOptions()).map());
        List results = new ArrayList(objects.length);
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
		List results = new ArrayList(nodes.size());
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
     * @see OmeroDataService#getAvailableGroups()
     */
	public Set getOrphanContainers(Class nodeType, boolean withLeaves, 
									Class rootLevel, long rootID)
		throws DSOutOfServiceException, DSAccessException
	{
		if (nodeType == null) return null;
		if (!(nodeType.equals(DatasetData.class) || 
				nodeType.equals(CategoryData.class)))
			return null;
		Set r = loadContainerHierarchy(nodeType, null, withLeaves, rootLevel, 
										rootID);
		Set parents = null;
		if (nodeType.equals(DatasetData.class)) {
			parents = loadContainerHierarchy(ProjectData.class, null, false, 
											rootLevel, rootID);
		} else if (nodeType.equals(CategoryData.class)) {
			parents = loadContainerHierarchy(CategoryGroupData.class, null, 
											false, rootLevel, rootID);
		}
		if (parents == null) return null;
		Iterator i = parents.iterator();
    	DataObject parent;
    	Set children = new HashSet();
    	while (i.hasNext()) {
    		parent = (DataObject) i.next();
    		if (nodeType.equals(DatasetData.class))
    			children.addAll(((ProjectData) parent).getDatasets());
    		else 
    			children.addAll(((CategoryGroupData) parent).getCategories());
    	}
    	Set childrenIds = new HashSet();

    	Iterator j = children.iterator();
    	DataObject child;
    	while (j.hasNext()) {
    		child = (DataObject) j.next();
    		childrenIds.add(new Long(child.getId()));
    	}
    	Set orphans = new HashSet();
    	orphans.addAll(r);
    	j = r.iterator();
    	while (j.hasNext()) {
    		child = (DataObject) j.next();
    		if (childrenIds.contains(new Long(child.getId())))
    			orphans.remove(child);
    	}
		return orphans;
	}

    /**
     * Implemented as specified by {@link OmeroDataService}.
     * @see OmeroDataService#getArchivedFiles(String, long)
     */
	public Map<Integer, List> getArchivedFiles(String path, long pixelsID) 
		throws DSOutOfServiceException, DSAccessException
	{
		Map<OriginalFile, byte[]> map = gateway.getOriginalFiles(pixelsID);
		Map<Integer, List> result = new HashMap<Integer, List>();
		if (map == null || map.size() == 0) return result;
		Iterator i = map.keySet().iterator();
		OriginalFile of;
		List<String> l = new ArrayList<String>();
		while (i.hasNext()) {
			of = (OriginalFile) i.next();
			try {
				createFile(path+of.getName(), map.get(of));
			} catch (Exception e) {
				l.add(of.getName());
			}
		}
		result.put(new Integer(map.size()), l);
		return result;
	}
	
}
