/*
 * org.openmicroscopy.omero.shoolaadapter.PojoHierarchyBrowsingAdapter
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
package org.openmicroscopy.omero.shoolaadapter;

//Java imports
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.omero.client.ServiceFactory;
import org.openmicroscopy.omero.interfaces.HierarchyBrowsing;

import pojos.DataObject;

/** 
 * calls the Omero interface and returns through the target (Shoola)
 * interface using {@link PojoAdapterUtils}.
 * 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 1.0
 */
public class PojoHierarchyBrowsingAdapter implements PojoHierarchyBrowsingView {

    private ServiceFactory services = new ServiceFactory();
    private HierarchyBrowsing proxiedInterface = services.getHierarchyBrowsingService();
    
    public PojoHierarchyBrowsingAdapter(){
        proxiedInterface = services.getHierarchyBrowsingService();
    }
    
    /* (non-Javadoc)
     * @see org.openmicroscopy.omero.shoolaadapter.HierarchyBrowsing#loadPDIHierarchy(java.lang.Class, int)
     */
    public DataObject loadPDIHierarchy(Class rootNodeType, int rootNodeID) {

        Object result = proxiedInterface.loadPDIHierarchy(rootNodeType, rootNodeID);
        return PojoAdapterUtils.adaptLoadedPDIHierarchy(rootNodeType,result);
        
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.omero.shoolaadapter.HierarchyBrowsing#loadCGCIHierarchy(java.lang.Class, int)
     */
    public DataObject loadCGCIHierarchy(Class rootNodeType, int rootNodeID) {
    
        Object result = proxiedInterface.loadCGCIHierarchy(rootNodeType, rootNodeID);
        return PojoAdapterUtils.adaptLoadedCGCIHierarchy(rootNodeType,result);

    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.omero.shoolaadapter.HierarchyBrowsing#findPDIHierarchies(java.util.Set)
     */
    public Set findPDIHierarchies(Set imgIDs) {
        Set result = proxiedInterface.findPDIHierarchies(imgIDs);
        return PojoAdapterUtils.adaptFoundPDIHierarchies(result);
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.omero.shoolaadapter.HierarchyBrowsing#findCGCIHierarchies(java.util.Set)
     */
    public Set findCGCIHierarchies(Set imgIDs) {
        Set result = proxiedInterface.findCGCIHierarchies(imgIDs);
        return PojoAdapterUtils.adaptFoundCGCIHierarchies(result);
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.omero.shoolaadapter.HierarchyBrowsing#findImageAnnotations(java.util.Set)
     */
    public Map findImageAnnotations(Set imgIDs) {
        Map result = proxiedInterface.findImageAnnotations(imgIDs);
        return PojoAdapterUtils.adaptFoundImageAnnotations(result);
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.omero.shoolaadapter.HierarchyBrowsing#findImageAnnotationsForExperimenter(java.util.Set, int)
     */
    public Map findImageAnnotationsForExperimenter(Set imgIDs,
            int experimenterID) {
        Map result = proxiedInterface.findImageAnnotationsForExperimenter(imgIDs,experimenterID);
        return PojoAdapterUtils.adaptFoundImageAnnotations(result);
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.omero.shoolaadapter.HierarchyBrowsing#findDatasetAnnotations(java.util.Set)
     */
    public Map findDatasetAnnotations(Set datasetIDs) {
        Map result = proxiedInterface.findDatasetAnnotations(datasetIDs);
        return PojoAdapterUtils.adaptFoundDatasetAnnotations(result);
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.omero.shoolaadapter.HierarchyBrowsing#findDatasetAnnotationsForExperimenter(java.util.Set, int)
     */
    public Map findDatasetAnnotationsForExperimenter(Set datasetIDs,
            int experimenterID) {
        Map result = proxiedInterface.findDatasetAnnotationsForExperimenter(datasetIDs,experimenterID);
        return PojoAdapterUtils.adaptFoundDatasetAnnotations(result);

    }
    
}
