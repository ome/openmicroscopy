/*
 * ome.logic.PojosImpl
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//Application-internal dependencies

import ome.api.OMEModel;
import ome.api.HierarchyBrowsing;
import ome.api.Pojos;
import ome.dao.AnnotationDao;
import ome.dao.ContainerDao;
import ome.dao.DaoFactory;
import ome.model.Category;
import ome.model.CategoryGroup;
import ome.model.Classification;
import ome.model.Dataset;
import ome.model.DatasetAnnotation;
import ome.model.Image;
import ome.model.ImageAnnotation;
import ome.model.Project;
import ome.util.builders.PojoOptions;


/**
 * implementation of the Pojos service interface
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since OMERO 2.0
 */
public class PojosImpl extends HierarchyBrowsingImpl implements Pojos {

    private static Log log = LogFactory.getLog(PojosImpl.class);

    private DaoFactory daos;
    
    public PojosImpl(DaoFactory daoFactory){
    	this.daos = daoFactory;
    }

    public Set loadContainerHierary(Class rootNodeType, Set rootNodeIds, Map options) {

        PojoOptions po = new PojoOptions(options);
        
        if (null==rootNodeIds && po.getExperimenter()==null) 
        	throw new IllegalArgumentException(
        			"Set of ids for loadContainerHierarchy() may not be null if experimenter option is null.");

        if (Project.class.equals(rootNodeType) ||
        		Dataset.class.equals(rootNodeType) ) {
        	return null; // FIXME containerDao.loadHierarchy(rootNodeType,);
        }
        		
        else if (CategoryGroup.class.equals(rootNodeType) || 
        		Category.class.equals(rootNodeType)) {
        	return null; // FIXME loadCGCI(rootNodeType,);	
        }
        
        else {
        	throw new IllegalArgumentException(
                "Class parameter for loadContainerIHierarchy() must be in {Project,Dataset,Category,CategoryGroup}, not "
                        + rootNodeType);
        }
        
        
	}

	public Set findContainerHierarchies(Class rootNodeType, Set imageIds, Map options) {
		
		if (null == imageIds)
			throw new IllegalArgumentException(
					"Set of ids for findContainerHierarcheies() may not be null.");

		PojoOptions po = new PojoOptions(options);
		
		if (Project.class.equals(rootNodeType)) {
			return null; // FIXME findPDI();
		}

		else if (CategoryGroup.class.equals(rootNodeType)){
			return null; // FIXME findCGCI();
		}
		
		else {throw new IllegalArgumentException(
	                "Class parameter for findContainerHierarchies() must be in {Project,CategoryGroup}, not "
	                        + rootNodeType);
		}
		
	}

	public Map findAnnotations(Class rootNodeType, Set rootNodeIds, Map options) {
		
		if (null == rootNodeIds)
			throw new IllegalArgumentException(
					"Set of ids for findAnnotation() may not be null.");
		
		PojoOptions po = new PojoOptions(options);
		
		if (Dataset.class.equals(rootNodeType)){
			List result;
			if (po.isAnnotation() && po.getAnnotator()!=null) {
				result = daos.annotation().findDataListAnnotationForExperimenter(rootNodeIds,po.getAnnotator().intValue());
			} else {
				result = daos.annotation().findDataListAnnotations(rootNodeIds);
			}
			return sortDatasetAnnotations(result);
		} 
		
		else if (Image.class.equals(rootNodeType)){
			List result;
			if (po.isAnnotation() && po.getAnnotator()!=null) {
				result = daos.annotation().findImageAnnotationsForExperimenter(rootNodeIds,po.getAnnotator().intValue());
			} else {
				result = daos.annotation().findImageAnnotations(rootNodeIds);
			}
			return sortImageAnnotations(result);
		}

		else { 
			throw new IllegalArgumentException(
                "Class parameter for findAnnotation() must be in {Dataset,Image}, not "
                        + rootNodeType);
		}

	}

	public Set findCGCPaths(Set imgIds, Map options) {
		return null; // FIXME 
	}

	public Set getImages(Class rootNodeType, Set rootNotIds, Map options) {
		// TODO Auto-generated method stub
		//return null;
		throw new RuntimeException("Not implemented yet.");
	}

	public Set getUserImages(Map options) {
		
		PojoOptions po = new PojoOptions(options);
		
		if (po.getExperimenter()==null){
			throw new IllegalArgumentException(
					"experimenter option is required for getUserImages().");
		}
	
		String queryA = " from Image i ";
		String queryB = " left outer join fetch i.experimenter e ";
		String queryC = " left outer fetch join i.imageAnnotations a";
		String queryD = " where e.id = :exp_id ";
		String queryE = " and a.experimenterId = :ann_id";
		String query;
		Object[] objects = new Object[1];
		if (po.isAnnotation()){
			query = queryA + queryB + queryC + queryD;
			if (po.getAnnotator()!=null){
				objects = new Object[2];
				objects[1]=po.getAnnotator();
				
			}
		} else {
			query = queryA + queryB + queryD;
		}

		objects[0]=po.getExperimenter();
		return new HashSet(daos.generic().queryList(query,objects));
		
	}

}