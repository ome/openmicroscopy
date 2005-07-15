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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.omero.client.ServiceFactory;
import org.openmicroscopy.omero.interfaces.HierarchyBrowsing;
import org.openmicroscopy.omero.model.Category;
import org.openmicroscopy.omero.model.CategoryGroup;
import org.openmicroscopy.omero.model.Dataset;
import org.openmicroscopy.omero.model.DatasetAnnotation;
import org.openmicroscopy.omero.model.ImageAnnotation;
import org.openmicroscopy.omero.model.Project;

import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ProjectData;

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
public class PojoHierarchyBrowsingAdapter implements PojoOmeroService {

    private ServiceFactory services = new ServiceFactory();
    private HierarchyBrowsing proxiedInterface = services.getHierarchyBrowsingService();    
    static private Map classMap;

    static {
    		classMap = new HashMap();
    		classMap.put(ProjectData.class,Project.class);
    		classMap.put(DatasetData.class,Dataset.class);
    		classMap.put(CategoryGroupData.class,CategoryGroup.class);
    		classMap.put(CategoryData.class,Category.class);
    	}
    

    /* (non-Javadoc)
     * @see org.openmicroscopy.omero.shoolaadapter.PojoOmeroService#loadPDIHierarchy(java.lang.Class, int)
     */
    public DataObject loadPDIHierarchy(Class rootNodeType, int rootNodeID) {
    		rootNodeType=(Class) classMap.get(rootNodeType);
        Object result = proxiedInterface.loadPDIHierarchy(rootNodeType, rootNodeID);
        return PojoAdapterUtils.adaptLoadedPDIHierarchy(rootNodeType,result);
        
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.omero.shoolaadapter.PojoOmeroService#loadCGCIHierarchy(java.lang.Class, int)
     */
    public DataObject loadCGCIHierarchy(Class rootNodeType, int rootNodeID) {
    	rootNodeType=(Class) classMap.get(rootNodeType);
        Object result = proxiedInterface.loadCGCIHierarchy(rootNodeType, rootNodeID);
        return PojoAdapterUtils.adaptLoadedCGCIHierarchy(rootNodeType,result);

    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.omero.shoolaadapter.PojoOmeroService#findPDIHierarchies(java.util.Set)
     */
    public Set findPDIHierarchies(Set imgIDs) {
        Set result = proxiedInterface.findPDIHierarchies(imgIDs);
        return PojoAdapterUtils.adaptFoundPDIHierarchies(result);
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.omero.shoolaadapter.PojoOmeroService#findCGCIHierarchies(java.util.Set)
     */
    public Set findCGCIHierarchies(Set imgIDs) {
        Set result = proxiedInterface.findCGCIHierarchies(imgIDs);
        return PojoAdapterUtils.adaptFoundCGCIHierarchies(result);
    }

	public DataObject loadPDIAnnotatedHierarchy(Class rootNodeType, int rootNodeID, int experimenterID) {
		rootNodeType=(Class) classMap.get(rootNodeType);
		Object result = proxiedInterface.loadPDIAnnotatedHierarchy(rootNodeType,rootNodeID, experimenterID);
		return PojoAdapterUtils.adaptLoadedPDIHierarchy(rootNodeType,result);
	}

	public DataObject loadCGCIAnnotatedHierarchy(Class rootNodeType, int rootNodeID, int experimenterID) {
		rootNodeType=(Class) classMap.get(rootNodeType);
		Object result = proxiedInterface.loadCGCIAnnotatedHierarchy(rootNodeType,rootNodeID,experimenterID);
		return PojoAdapterUtils.adaptLoadedCGCIHierarchy(rootNodeType,result);
	}

	public Set findPDIAnnotatedHierarchies(Set imgIDs, int experimenterID) {
		Set result = proxiedInterface.findPDIAnnotatedHierarchies(imgIDs,experimenterID);
		return PojoAdapterUtils.adaptFoundPDIHierarchies(result);
	}

	public Set findCGCIAnnotatedHierarchies(Set imgIDs, int experimenterID) {
		Set result = proxiedInterface.findCGCIAnnotatedHierarchies(imgIDs,experimenterID);
		return PojoAdapterUtils.adaptFoundCGCIHierarchies(result);
	}

	public Set findCGCPaths(Set imgIDs, boolean contained) {
		Set result = proxiedInterface.findCGCPaths(imgIDs, contained);
		return PojoAdapterUtils.adaptFoundCGCIHierarchies(result);//TODO Does this work as expected?
	}

    /* (non-Javadoc)
     * @see org.openmicroscopy.omero.shoolaadapter.PojoOmeroService#findImageAnnotations(java.util.Set)
     */
    public Map findImageAnnotations(Set imgIDs) {
        Map result = proxiedInterface.findImageAnnotations(imgIDs);
        return PojoAdapterUtils.adaptFoundImageAnnotations(result);
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.omero.shoolaadapter.PojoOmeroService#findAnnotations(java.lang.Class, java.util.Set, int)
     */
    public Map findAnnotations(Class clazz, Set iDs, int experimenterID) {
    		
    		if (clazz==null){
    			throw new IllegalArgumentException("Class parameter to method cannot be null");
    		}
    	
    		if (clazz.equals(ImageAnnotation.class)){
    			Map result;
    			if (experimenterID == -1){
    				result = proxiedInterface.findImageAnnotations(iDs);
    			} else {
    				result = proxiedInterface.findImageAnnotationsForExperimenter(iDs,experimenterID);    				
    			}
    			return PojoAdapterUtils.adaptFoundImageAnnotations(result);
    		} else if (clazz.equals(DatasetAnnotation.class)){
    			Map result;
    			if (experimenterID == -1){
    				result = proxiedInterface.findDatasetAnnotations(iDs);
    			} else {
    				result = proxiedInterface.findDatasetAnnotationsForExperimenter(iDs,experimenterID);    				
    			}
    			return PojoAdapterUtils.adaptFoundDatasetAnnotations(result);    			
    		} else {
    			throw new IllegalArgumentException("Method cannot handle calls for type "+clazz.getName());
    		}
    }
	
}
