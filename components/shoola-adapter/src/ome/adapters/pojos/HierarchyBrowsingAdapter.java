/*
 * ome.adapters.pojos.AdapterUtils
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
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
package ome.adapters.pojos;

//Java imports
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import ome.client.ServiceFactory;
import ome.api.HierarchyBrowsing;

import org.openmicroscopy.shoola.env.data.model.DataObject;

/** 
 * calls the Omero interface and returns through the target (Shoola)
 * interface using {@link AdapterUtils}.
 * @DEV.TODO use InvocationHandler to simplifiy this
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 1.0
 */
public class HierarchyBrowsingAdapter implements HierarchyBrowsingView {

    private ServiceFactory services = new ServiceFactory();
    private HierarchyBrowsing proxiedInterface = services.getHierarchyBrowsingService();
    
    public HierarchyBrowsingAdapter(){
        proxiedInterface = services.getHierarchyBrowsingService();
    }
    
    /* (non-Javadoc)
     * @see ome.adapters.pojos.HierarchyBrowsing#loadPDIHierarchy(java.lang.Class, int)
     */
    public DataObject loadPDIHierarchy(Class rootNodeType, int rootNodeID) {

        Object result = proxiedInterface.loadPDIHierarchy(rootNodeType, rootNodeID);
        return AdapterUtils.adaptLoadedPDIHierarchy(rootNodeType,result);
        
    }

    /* (non-Javadoc)
     * @see ome.adapters.pojos.HierarchyBrowsing#loadCGCIHierarchy(java.lang.Class, int)
     */
    public DataObject loadCGCIHierarchy(Class rootNodeType, int rootNodeID) {
    
        Object result = proxiedInterface.loadCGCIHierarchy(rootNodeType, rootNodeID);
        return AdapterUtils.adaptLoadedCGCIHierarchy(rootNodeType,result);

    }

    /* (non-Javadoc)
     * @see ome.adapters.pojos.HierarchyBrowsing#findPDIHierarchies(java.util.Set)
     */
    public Set findPDIHierarchies(Set imgIDs) {
        Set result = proxiedInterface.findPDIHierarchies(imgIDs);
        return AdapterUtils.adaptFoundPDIHierarchies(result);
    }

    /* (non-Javadoc)
     * @see ome.adapters.pojos.HierarchyBrowsing#findCGCIHierarchies(java.util.Set)
     */
    public Set findCGCIHierarchies(Set imgIDs) {
        Set result = proxiedInterface.findCGCIHierarchies(imgIDs);
        return AdapterUtils.adaptFoundCGCIHierarchies(result);
    }

    /* (non-Javadoc)
     * @see ome.adapters.pojos.HierarchyBrowsing#findImageAnnotations(java.util.Set)
     */
    public Map findImageAnnotations(Set imgIDs) {
        Map result = proxiedInterface.findImageAnnotations(imgIDs);
        return AdapterUtils.adaptFoundImageAnnotations(result);
    }

    /* (non-Javadoc)
     * @see ome.adapters.pojos.HierarchyBrowsing#findImageAnnotationsForExperimenter(java.util.Set, int)
     */
    public Map findImageAnnotationsForExperimenter(Set imgIDs,
            int experimenterID) {
        Map result = proxiedInterface.findImageAnnotationsForExperimenter(imgIDs,experimenterID);
        return AdapterUtils.adaptFoundImageAnnotations(result);
    }

    /* (non-Javadoc)
     * @see ome.adapters.pojos.HierarchyBrowsing#findDatasetAnnotations(java.util.Set)
     */
    public Map findDatasetAnnotations(Set datasetIDs) {
        Map result = proxiedInterface.findDatasetAnnotations(datasetIDs);
        return AdapterUtils.adaptFoundDatasetAnnotations(result);
    }

    /* (non-Javadoc)
     * @see ome.adapters.pojos.HierarchyBrowsing#findDatasetAnnotationsForExperimenter(java.util.Set, int)
     */
    public Map findDatasetAnnotationsForExperimenter(Set datasetIDs,
            int experimenterID) {
        Map result = proxiedInterface.findDatasetAnnotationsForExperimenter(datasetIDs,experimenterID);
        return AdapterUtils.adaptFoundDatasetAnnotations(result);

    }
    
}
