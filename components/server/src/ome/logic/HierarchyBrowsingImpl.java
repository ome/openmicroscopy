/*
 * ome.logic.HierarchyBrowsingImpl
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

/*------------------------------------------------------------------------------
 *
 * Written by:    Josh Moore <josh.moore@gmx.de>
 *
 *------------------------------------------------------------------------------
 */

package ome.logic;

//Java imports
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//Application-internal dependencies

import ome.api.OMEModel;
import ome.api.HierarchyBrowsing;
import ome.dao.AnnotationDao;
import ome.dao.ContainerDao;
import ome.model.Category;
import ome.model.CategoryGroup;
import ome.model.Dataset;
import ome.model.Project;
import ome.tools.AnnotationTransformations;
import ome.tools.HierarchyTransformations;


/**
 * implementation of the HierarchyBrowsing service. A single service
 * object is configured through IoC (most likely by Spring) and is
 * available for all calls.
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since OMERO 1.0
 * @DEV.TODO add queries.hbm.xml to pre-processed (cached) files.
 * @DEV.TODO Check the types of parameters coming in. 
 * 			 passing pojo.ImageData rather than Integer
 *  		 lead to a StackOverFlowError!
 *  		BEST solution Set-->int[] !!! (for Python et al. as well)
 */
public class HierarchyBrowsingImpl implements HierarchyBrowsing {

    private static Log log = LogFactory.getLog(HierarchyBrowsingImpl.class);

    AnnotationDao annotationDao;

    ContainerDao containerDao;

    public void setAnnotationDao(AnnotationDao dao) {
        this.annotationDao = dao;
    }

    public void setContainerDao(ContainerDao dao) {
        this.containerDao = dao;
    }

    /**
     * @see ome.interfaces.HierarchyBrowsing#loadPDIHierarchy(java.lang.Class, int)
     */
    public OMEModel loadPDIHierarchy(final Class arg0, final int arg1) {
    	return loadPDI(arg0,arg1,-1,false);
    }
    
    /**
     * @see ome.interfaces.HierarchyBrowsing#loadPDIAnnotatedHierarchy(java.lang.Class, int, int)
     */
    public OMEModel loadPDIAnnotatedHierarchy(Class arg0, int arg1, int arg2) {
        return loadPDI(arg0,arg1,arg2,true);
    }
    	
    protected OMEModel loadPDI(Class arg0, int arg1, int arg2, boolean arg3){

    	// CONTRACT
        if (!Project.class.equals(arg0) && !Dataset.class.equals(arg0)) {
            throw new IllegalArgumentException(
                    "Class parameter for loadPDIHierarchy() must be Project or Dataset, not "
                            + arg0);
        }

        return containerDao.loadHierarchy(arg0, arg1, arg2, arg3);

    }
    

    
    /**
     * @see ome.interfaces.HierarchyBrowsing#loadCGCIHierarchy(java.lang.Class,int)
     */
    public OMEModel loadCGCIHierarchy(final Class arg0, final int arg1) {
       	return loadCGCI(arg0,arg1,-1,false);
    }
    
    /**
     * @see ome.interfaces.HierarchyBrowsing#loadPDIAnnotatedHierarchy(java.lang.Class, int, int)
     */
    public OMEModel loadCGCIAnnotatedHierarchy(Class arg0, int arg1, int arg2) {
        return loadCGCI(arg0,arg1,arg2,true);
    }
    	
    protected OMEModel loadCGCI(Class arg0, int arg1, int arg2, boolean arg3){
 
        // CONTRACT
        if (!CategoryGroup.class.equals(arg0) && !Category.class.equals(arg0)) {
            throw new IllegalArgumentException(
                    "Class parameter for loadCGCIHierarchy() must be CategoryGroup or Category, not "
                            + arg0);
        }

        return containerDao.loadHierarchy(arg0, arg1, arg2, arg3);

    }

    /**
     * @see ome.interfaces.HierarchyBrowsing#findPDIHierarchies(java.util.Set)
     */
    public Set findPDIHierarchies(final Set arg0) {
    	return findPDI(arg0,-1,false);
    }

    /**
     * @see ome.interfaces.HierarchyBrowsing#findPDIHierarchies(java.util.Set)
     */
    public Set findPDIAnnotatedHierarchies(final Set arg0, final int experimenterId) {
    	return findPDI(arg0,experimenterId,true);
    }
    
    protected Set findPDI(Set arg0, int experimenterId, boolean annotated) {
        // CONTRACT
        if (null == arg0 || arg0.size() == 0) {
            return new HashSet();
        }

        List result = containerDao.findPDIHierarchies(arg0, experimenterId, annotated);
        Set imagesAll = new HashSet(result);

        if (null == imagesAll || imagesAll.size() == 0) {
            if (log.isDebugEnabled()) {
                log.debug("findPDIHierarchies() -- no results found:\n"
                        + arg0.toString());
            }
            return new HashSet();
        }

        return HierarchyTransformations.invertPDI(imagesAll);

    }

    /** 
     * @see ome.interfaces.HierarchyBrowsing#findCGCIHierarchies(java.util.Set)
     */
    public Set findCGCIHierarchies(Set arg0) {
    	return findCGCI(arg0,-1,false);
    }

    /** 
     * @see ome.interfaces.HierarchyBrowsing#findCGCIAnnotatedHierarchies(java.util.Set)
     */
    public Set findCGCIAnnotatedHierarchies(Set arg0, int experimenterId) {
    	return findCGCI(arg0,experimenterId,true);
    }

    /** 
     * @see ome.interfaces.HierarchyBrowsing#findCGCPaths(java.util.Set, boolean)
     */
    public Set findCGCPaths(Set imgIds, boolean contained) {
    	
        // CONTRACT
        if (null == imgIds || imgIds.size() == 0) {
            return new HashSet();
        }
    	
    	List l = containerDao.findCGCPaths(imgIds,contained);
    	Set<CategoryGroup> s = new HashSet<CategoryGroup>(l);
    	for (CategoryGroup cg : s){
    		for (Object o : cg.getCategories()){
    			Category c = (Category) o;
    			c.setClassifications(null);
    		}
    	}
    	return s;
    }
    
    protected Set findCGCI(Set arg0, int experimenterId, boolean annotated) {

    	// CONTRACT
        if (null == arg0 || arg0.size() == 0) {
            return new HashSet();
        }

        List result = containerDao.findCGCIHierarchies(arg0,experimenterId,annotated);
        Set imagesAll = new HashSet(result);

        if (null == imagesAll || imagesAll.size() == 0) {
            if (log.isDebugEnabled()) {
                log.debug("findCGCIHierarchies() -- no results found:\n"
                        + arg0.toString());
            }
            return new HashSet();
        }

        return HierarchyTransformations.invertCGCI(imagesAll);
        
    }

	
    /** 
     * @see ome.interfaces.HierarchyBrowsing#findImageAnnotations(java.util.Set)
     */
    public Map findImageAnnotations(Set arg0) {

        // CONTRACT
        if (null == arg0 || arg0.size() == 0) {
            return new HashMap();
        }

        List result = annotationDao.findImageAnnotations(arg0);

        return sortImageAnnotations(result);

    }

    /** 
     * @see ome.interfaces.HierarchyBrowsing#findImageAnnotations(java.util.Set,int)
     */
    public Map findImageAnnotationsForExperimenter(final Set arg0,
            int arg1) {

        // CONTRACT
        if (null == arg0 || arg0.size() == 0) {
            return new HashMap();
        }

        List result = annotationDao.findImageAnnotationsForExperimenter(arg0,
                arg1);
        return sortImageAnnotations(result);

    }

    Map sortImageAnnotations(List l) {
//FIXME refactor all! add all ids to return map
        Set result = new HashSet(l);

        if (null == result || result.size() == 0) {
            return new HashMap();
        }

        return AnnotationTransformations.sortImageAnnotatiosn(result);

    }

    /** 
     * @see ome.interfaces.HierarchyBrowsing#findDatasetAnnotations(java.util.Set)
     */
    public Map findDatasetAnnotations(Set arg0) {
//FIXME
        // CONTRACT
        if (null == arg0 || arg0.size() == 0) {
            return new HashMap();
        }

        List result = annotationDao.findDataListAnnotations(arg0);
        return sortDatasetAnnotations(result);

    }

    /** 
     * @see ome.interfaces.HierarchyBrowsing#findDatasetAnnotations(java.util.Set, int)
     */
    public Map findDatasetAnnotationsForExperimenter(Set arg0,
            int arg1) {

        // CONTRACT
        if (null == arg0 || arg0.size() == 0) {
            return new HashMap();
        }

        List result = annotationDao.findDataListAnnotationForExperimenter(arg0,
                arg1);
        return sortDatasetAnnotations(result);

    }

    Map sortDatasetAnnotations(List l) {

        Set result = new HashSet(l);

        if (null == result || result.size() == 0) {
            return new HashMap();
        }

        return AnnotationTransformations.sortDatasetAnnotatiosn(result);

    }

}