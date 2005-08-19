/*
 * ome.logic.AnalysisImpl
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
import ome.api.Analysis;
import ome.dao.AnalysisDao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//Application-internal dependencies

import org.openmicroscopy.omero.OMEModel;
import org.openmicroscopy.omero.interfaces.HierarchyBrowsing;
import org.openmicroscopy.omero.logic.AnnotationDao;
import org.openmicroscopy.omero.logic.ContainerDao;
import org.openmicroscopy.omero.logic.GenericDao;
import org.openmicroscopy.omero.model.AnalysisChainExecution;
import org.openmicroscopy.omero.model.Category;
import org.openmicroscopy.omero.model.CategoryGroup;
import org.openmicroscopy.omero.model.Classification;
import org.openmicroscopy.omero.model.Dataset;
import org.openmicroscopy.omero.model.DatasetAnnotation;
import org.openmicroscopy.omero.model.Experimenter;
import org.openmicroscopy.omero.model.Image;
import org.openmicroscopy.omero.model.ImageAnnotation;
import org.openmicroscopy.omero.model.Project;


/**
 * implementation of the Analysis service. 
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since OMERO 1.0
 */
public class AnalysisImpl implements Analysis {

    private static Log log = LogFactory.getLog(AnalysisImpl.class);

    AnalysisDao aDao;
    
    AnnotationDao annDao;

    ContainerDao cDao;

    GenericDao gDao;
    
    public void setAnalysisDao(AnalysisDao analysisDao){
    	this.aDao = analysisDao;
    }
    
    public void setAnnotationDao(AnnotationDao annotationDao) {
        this.annDao = annotationDao; 
    }

    public void setContainerDao(ContainerDao containerDao) {
        this.cDao = containerDao;
    }

    public void setGenericDao(GenericDao genericDao){
    	this.gDao = genericDao;
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
	public Set getProjectsForUser(int experimenterId) {
		//return aDao.getProjectsForUser(experimenterId); TODO remove from Analysis Dao?

		// Criteria
		Experimenter exp = new Experimenter();
		exp.setAttributeId(experimenterId);
		
		// Example
		Project prj = new Project();
		prj.setExperimenter(exp);
		
		return new HashSet(gDao.getListByExample(prj));
	}

	public Set getAllDatasets() {
		return new HashSet(gDao.getListByExample(new Dataset()));
	}

	public Set getAllForImage(int imageId) {
		// TODO Auto-generated method stub
		//return null;
		throw new RuntimeException("Not implemented yet.");
	}

	public Set getChainExecutionsForDataset(int datasetId) {

		// Criteria
		Dataset ds = new Dataset();
		ds.setDatasetId(datasetId);
		
		// Example
		AnalysisChainExecution ace = new AnalysisChainExecution();
		ace.setDataset(ds);
		
		return new HashSet(gDao.getListByExample(ace));
	}
 
	// Criteria is a set ~~~~~~~~~~~~~~~~~~~~~~~`
	
	public Set getDatasetsForProject(int projectId) {
		
		StringBuilder sb = new StringBuilder();
		sb.append(" select d from Dataset d ");
		sb.append("  left outer join fetch d.projects p ");
		sb.append("  where p.projectId = ?");
		
		return new HashSet(gDao.queryList(sb.toString(),new Object[]{projectId}));
		
	}

	public Set getProjectsForDataset(int datasetId) {
		StringBuilder sb = new StringBuilder();
		sb.append(" select p from Project p ");
		sb.append("  left outer join fetch p.datasets d ");
		sb.append("  where d.datasetId = ?");
		
		return new HashSet(gDao.queryList(sb.toString(),new Object[]{datasetId}));
	}

	public Set getImagesForDataset(int datasetId) {
		StringBuilder sb = new StringBuilder();
		sb.append(" select i from Image i ");
		sb.append("  left outer join fetch i.datasets d ");
		sb.append("  where d.datasetId = ?");
		
		return new HashSet(gDao.queryList(sb.toString(),new Object[]{datasetId}));
	}

	

}