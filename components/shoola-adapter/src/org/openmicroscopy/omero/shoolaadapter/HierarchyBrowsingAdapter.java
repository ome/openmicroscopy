/*
 * Created on May 31, 2005
*/
package org.openmicroscopy.omero.shoolaadapter;

import java.util.Map;
import java.util.Set;

import org.openmicroscopy.omero.client.ServiceFactory;
import org.openmicroscopy.omero.interfaces.HierarchyBrowsing;

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
        return AdapterUtils.adaptLoadedPDIHierarchy(rootNodeType,result);
        
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.omero.shoolaadapter.HierarchyBrowsing#loadCGCIHierarchy(java.lang.Class, int)
     */
    public DataObject loadCGCIHierarchy(Class rootNodeType, int rootNodeID) {
    
        Object result = proxiedInterface.loadCGCIHierarchy(rootNodeType, rootNodeID);
        return AdapterUtils.adaptLoadedCGCIHierarchy(rootNodeType,result);

    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.omero.shoolaadapter.HierarchyBrowsing#findPDIHierarchies(java.util.Set)
     */
    public Set findPDIHierarchies(Set imgIDs) {
        Set result = proxiedInterface.findPDIHierarchies(imgIDs);
        return AdapterUtils.adaptFoundPDIHierarchies(result);
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.omero.shoolaadapter.HierarchyBrowsing#findCGCIHierarchies(java.util.Set)
     */
    public Set findCGCIHierarchies(Set imgIDs) {
        Set result = proxiedInterface.findCGCIHierarchies(imgIDs);
        return AdapterUtils.adaptFoundCGCIHierarchies(result);
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.omero.shoolaadapter.HierarchyBrowsing#findImageAnnotations(java.util.Set)
     */
    public Map findImageAnnotations(Set imgIDs) {
        Map result = proxiedInterface.findImageAnnotations(imgIDs);
        return AdapterUtils.adaptFoundImageAnnotations(result);
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.omero.shoolaadapter.HierarchyBrowsing#findImageAnnotationsForExperimenter(java.util.Set, int)
     */
    public Map findImageAnnotationsForExperimenter(Set imgIDs,
            int experimenterID) {
        Map result = proxiedInterface.findImageAnnotationsForExperimenter(imgIDs,experimenterID);
        return AdapterUtils.adaptFoundImageAnnotations(result);
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.omero.shoolaadapter.HierarchyBrowsing#findDatasetAnnotations(java.util.Set)
     */
    public Map findDatasetAnnotations(Set datasetIDs) {
        Map result = proxiedInterface.findDatasetAnnotations(datasetIDs);
        return AdapterUtils.adaptFoundDatasetAnnotations(result);
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.omero.shoolaadapter.HierarchyBrowsing#findDatasetAnnotationsForExperimenter(java.util.Set, int)
     */
    public Map findDatasetAnnotationsForExperimenter(Set datasetIDs,
            int experimenterID) {
        Map result = proxiedInterface.findDatasetAnnotationsForExperimenter(datasetIDs,experimenterID);
        return AdapterUtils.adaptFoundDatasetAnnotations(result);

    }
    
}
