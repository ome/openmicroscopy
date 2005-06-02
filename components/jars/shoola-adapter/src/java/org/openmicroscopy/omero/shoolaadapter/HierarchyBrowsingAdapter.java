/*
 * Created on May 31, 2005
*/
package org.openmicroscopy.omero.shoolaadapter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.openmicroscopy.omero.client.ServiceFactory;
import org.openmicroscopy.omero.interfaces.HierarchyBrowsing;
import org.openmicroscopy.omero.model.Category;
import org.openmicroscopy.omero.model.CategoryGroup;
import org.openmicroscopy.omero.model.Dataset;
import org.openmicroscopy.omero.model.DatasetAnnotation;
import org.openmicroscopy.omero.model.Image;
import org.openmicroscopy.omero.model.ImageAnnotation;
import org.openmicroscopy.omero.model.Project;

import org.openmicroscopy.shoola.env.data.model.DataObject;

/**
 * @author josh
 */
public class HierarchyBrowsingAdapter implements HierarchyBrowsingView {

    private ServiceFactory services = new ServiceFactory();
    private HierarchyBrowsing proxiedInterface = services.getHierarchyBrowsingService();
    
    public HierarchyBrowsingAdapter(){
        proxiedInterface = services.getHierarchyBrowsingService();
    }
    
    /* (non-Javadoc)
     * @see org.openmicroscopy.omero.shoolaadapter.HierarchyBrowsing#loadPDIHierarchy(java.lang.Class, int)
     */
    public DataObject loadPDIHierarchy(Class rootNodeType, int rootNodeID) {

        Object result = proxiedInterface.loadPDIHierarchy(rootNodeType, rootNodeID);
        if (rootNodeType.equals(Project.class)){
            return AdapterUtils.go((Project) result);
        } else if (rootNodeType.equals(Dataset.class)){
            return AdapterUtils.go((Dataset) result);
        } else {
            throw new IllegalArgumentException("Method only takes Project and Dataset as argument.");
        }
        
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.omero.shoolaadapter.HierarchyBrowsing#loadCGCIHierarchy(java.lang.Class, int)
     */
    public DataObject loadCGCIHierarchy(Class rootNodeType, int rootNodeID) {
    
        Object result = proxiedInterface.loadCGCIHierarchy(rootNodeType, rootNodeID);
        if (rootNodeType.equals(CategoryGroup.class)){
            return AdapterUtils.go((CategoryGroup) result);
        } else if (rootNodeType.equals(Category.class)){
            return AdapterUtils.go((Category) result);
        } else {
            throw new IllegalArgumentException("Method only takes CategoryGroup and Category as argument.");
        }

    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.omero.shoolaadapter.HierarchyBrowsing#findPDIHierarchies(java.util.Set)
     */
    public Set findPDIHierarchies(Set imgIDs) {
        Set result = proxiedInterface.findPDIHierarchies(imgIDs);
        Set dataObjects = new HashSet();
        for (Iterator i = result.iterator(); i.hasNext();) {
            Object obj = i.next();
            if (obj instanceof Project) {
                Project prj = (Project) obj;
                dataObjects.add(AdapterUtils.go(prj));
            } else if (obj instanceof Dataset) {
                Dataset ds = (Dataset) obj;
                dataObjects.add(AdapterUtils.go(ds)); 
            } else if (obj instanceof Image) {
                Image img = (Image) obj;
                dataObjects.add(AdapterUtils.go(img));
            } else {
                throw new RuntimeException("Method returned unexpected value type:" + obj.getClass()	);
            }
        }
        return dataObjects;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.omero.shoolaadapter.HierarchyBrowsing#findCGCIHierarchies(java.util.Set)
     */
    public Set findCGCIHierarchies(Set imgIDs) {
        Set result = proxiedInterface.findCGCIHierarchies(imgIDs);
        Set dataObjects = new HashSet();
        for (Iterator i = result.iterator(); i.hasNext();) {
            Object obj = i.next();
            if (obj instanceof CategoryGroup) {
                CategoryGroup cg = (CategoryGroup) obj;
                dataObjects.add(AdapterUtils.go(cg));
            } else if (obj instanceof Category) {
                Category ca = (Category) obj;
                dataObjects.add(AdapterUtils.go(ca)); 
            } else if (obj instanceof Image) {
                Image img = (Image) obj;
                dataObjects.add(AdapterUtils.go(img));
            } else {
                throw new RuntimeException("Method returned unexpected value type:" + obj.getClass()	);
            }
        }
        return dataObjects;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.omero.shoolaadapter.HierarchyBrowsing#findImageAnnotations(java.util.Set)
     */
    public Map findImageAnnotations(Set imgIDs) {
        Map result = proxiedInterface.findImageAnnotations(imgIDs);
        return goImageAnnotations(result);
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.omero.shoolaadapter.HierarchyBrowsing#findImageAnnotationsForExperimenter(java.util.Set, int)
     */
    public Map findImageAnnotationsForExperimenter(Set imgIDs,
            int experimenterID) {
        Map result = proxiedInterface.findImageAnnotationsForExperimenter(imgIDs,experimenterID);
        return goImageAnnotations(result);
    }

    
    protected Map goImageAnnotations(Map result) {
        Map dataObjects = new HashMap();
        for (Iterator i = result.keySet().iterator(); i.hasNext();) {
            Object key = i.next();
            Set value = (Set) result.get(key);
            dataObjects.put(key,new HashSet());
            for (Iterator j = value.iterator(); j.hasNext();) {
                ImageAnnotation ann = (ImageAnnotation) j.next();
                ((Set) dataObjects.get(key)).add(AdapterUtils.go(ann));
            }
        }
        return dataObjects; 
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.omero.shoolaadapter.HierarchyBrowsing#findDatasetAnnotations(java.util.Set)
     */
    public Map findDatasetAnnotations(Set datasetIDs) {
        Map result = proxiedInterface.findDatasetAnnotations(datasetIDs);
        return goDatasetAnnotations(result);
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.omero.shoolaadapter.HierarchyBrowsing#findDatasetAnnotationsForExperimenter(java.util.Set, int)
     */
    public Map findDatasetAnnotationsForExperimenter(Set datasetIDs,
            int experimenterID) {
        Map result = proxiedInterface.findDatasetAnnotationsForExperimenter(datasetIDs,experimenterID);
        return goDatasetAnnotations(result);

    }

    protected Map goDatasetAnnotations(Map result) {
        Map dataObjects = new HashMap();
        for (Iterator i = result.keySet().iterator(); i.hasNext();) {
            Object key = i.next();
            Set value = (Set) result.get(key);
            dataObjects.put(key,new HashSet());
            for (Iterator j = value.iterator(); j.hasNext();) {
                DatasetAnnotation ann = (DatasetAnnotation) j.next();
                ((Set) dataObjects.get(key)).add(AdapterUtils.go(ann));
            }
        }
        return dataObjects; 
    }

    
}
