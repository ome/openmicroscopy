/*
 * Created on May 31, 2005
*/
package org.openmicroscopy.omero.shoolaadapter;

import java.util.Map;
import java.util.Set;

import org.openmicroscopy.omero.client.ServiceFactory;
import org.openmicroscopy.omero.interfaces.HierarchyBrowsing;

import pojos.DataObject;

/**
 * @author josh
 */
public class HierarchyBrowsingAdapter implements HierarchyBrowsingView {

    private ServiceFactory services = new ServiceFactory();
    private HierarchyBrowsing proxiedInterface;
    
    public HierarchyBrowsingAdapter(){
        proxiedInterface = services.getHierarchyBrowsingService();
    }
    
    /* (non-Javadoc)
     * @see org.openmicroscopy.omero.shoolaadapter.HierarchyBrowsing#loadPDIHierarchy(java.lang.Class, int)
     */
    public DataObject loadPDIHierarchy(Class rootNodeType, int rootNodeID) {
        // TODO Auto-generated method stub
        /* return null; */
        throw new RuntimeException("implement me");
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.omero.shoolaadapter.HierarchyBrowsing#loadCGCIHierarchy(java.lang.Class, int)
     */
    public DataObject loadCGCIHierarchy(Class rootNodeType, int rootNodeID) {
        // TODO Auto-generated method stub
        /* return null; */
        throw new RuntimeException("implement me");
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.omero.shoolaadapter.HierarchyBrowsing#findPDIHierarchies(java.util.Set)
     */
    public Set findPDIHierarchies(Set imgIDs) {
        // TODO Auto-generated method stub
        /* return null; */
        throw new RuntimeException("implement me");
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.omero.shoolaadapter.HierarchyBrowsing#findCGCIHierarchies(java.util.Set)
     */
    public Set findCGCIHierarchies(Set imgIDs) {
        // TODO Auto-generated method stub
        /* return null; */
        throw new RuntimeException("implement me");
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.omero.shoolaadapter.HierarchyBrowsing#findImageAnnotations(java.util.Set)
     */
    public Map findImageAnnotations(Set imgIDs) {
        // TODO Auto-generated method stub
        /* return null; */
        throw new RuntimeException("implement me");
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.omero.shoolaadapter.HierarchyBrowsing#findImageAnnotationsForExperimenter(java.util.Set, int)
     */
    public Map findImageAnnotationsForExperimenter(Set imgIDs,
            int experimenterID) {
        // TODO Auto-generated method stub
        /* return null; */
        throw new RuntimeException("implement me");
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.omero.shoolaadapter.HierarchyBrowsing#findDatasetAnnotations(java.util.Set)
     */
    public Map findDatasetAnnotations(Set datasetIDs) {
        // TODO Auto-generated method stub
        /* return null; */
        throw new RuntimeException("implement me");
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.omero.shoolaadapter.HierarchyBrowsing#findDatasetAnnotationsForExperimenter(java.util.Set, int)
     */
    public Map findDatasetAnnotationsForExperimenter(Set datasetIDs,
            int experimenterID) {
        // TODO Auto-generated method stub
        /* return null; */
        throw new RuntimeException("implement me");
    }

}
