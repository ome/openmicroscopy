/*
 * org.openmicroscopy.shoola.env.data.OmeroDataServiceImpl
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.env.data;


//Java imports
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import ome.model.IObject;
import ome.model.core.Channel;
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
            po.exp(new Long(getUserDetails().getId()));
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
        List ids = new ArrayList(children.size());
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
        po.countsFor(new Long(getUserDetails().getId()));
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
            setRootOptions(po, rootLevel, rootLevelID);
            po.countsFor(new Long(getUserDetails().getId()));
            return gateway.findContainerHierarchy(rootNodeType, leavesIDs,
                            po.map());
        } catch (Exception e) {
           throw new DSAccessException(e.getMessage());
        }
    }

    /** 
     * Implemented as specified by {@link OmeroDataService}.
     * @see OmeroDataService#findAnnotations(Class, Set, Set)
     */
    public Map findAnnotations(Class nodeType, Set nodeIDs, Set annotatorIDs)
        throws DSOutOfServiceException, DSAccessException
    {
        PojoOptions po = new PojoOptions();
        po.noCounts();
        po.noLeaves();
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
        po.countsFor(new Long(getUserDetails().getId()));
        return gateway.getContainerImages(nodeType, nodeIDs, po.map());
    }
    
    /** 
     * Implemented as specified by {@link OmeroDataService}.
     * @see OmeroDataService#getUserImages()
     */
    public Set getUserImages()
        throws DSOutOfServiceException, DSAccessException
    {
        PojoOptions po = new PojoOptions();
        po.noCounts();
        po.exp(new Long(getUserDetails().getId()));
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
        IObject object = gateway.createObject(
                ModelMapper.createAnnotation(annotatedObject.asIObject(), 
                                            data), (new PojoOptions()).map());
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
        
        gateway.deleteObject(data.asIObject());
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
        ModelMapper.unloadCollections(object);
        IObject updated = gateway.updateObject(object, options);
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
            while (i.hasNext()) {
                ioObjects[index] = ((DataObject) i.next()).asIObject();
                index++;
            }
            gateway.deleteObjects(ioObjects);
        } else {
            cut(parent, children);
            /*
            IObject p = parent.asIObject();
            IObject ioChild;
            IObject link;
            List links = null;
            List toUpdate = new ArrayList();
            while (i.hasNext()) {
                ioChild = ((DataObject) i.next()).asIObject();
                link = ModelMapper.unlinkChildFromParent(ioChild, p);
                if (links == null) links = new ArrayList();
                if (link != null) links.add(link);
                if (!(toUpdate.contains(p))) toUpdate.add(p);
            }
            if (links != null) {
                gateway.deleteObjects((IObject[]) 
                        links.toArray(new IObject[links.size()]));
            }
            */
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
        IObject ob = object.asIObject();
        ModelMapper.unloadCollections(ob);
        IObject updated = gateway.updateObject(ob,
                                        (new PojoOptions()).map());
        return PojoMapper.asDataObject(updated);
    }

    /**
     * Implemented as specified by {@link OmeroDataService}.
     * @see OmeroDataService#classify(Set, Set)
     */
    public void classify(Set images, Set categories)
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
    }

    /**
     * Implemented as specified by {@link OmeroDataService}.
     * @see OmeroDataService#declassify(Set, Set)
     */
    public void declassify(Set images, Set categories)
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
        Set objects = new HashSet();
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
            all = getUserImages();
            Iterator i = in.iterator();
            while (i.hasNext()) {
                objects.add(new Long(((ImageData) i.next()).getId()));
            }
            
        }
        if (all == null) return new HashSet(1);
        Iterator k = all.iterator();
        Set toRemove = new HashSet();
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
        DataObject parent;
        i = toCut.keySet().iterator();
        while (i.hasNext()) {
            parent = (DataObject) i.next();
            cut(parent, (Set) toCut.get(parent));
        }
        
        i = toPaste.keySet().iterator();
        
        while (i.hasNext()) {
            parent = (DataObject) i.next();
            addExistingObjects(parent, (Set) toPaste.get(parent));
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
    
}
