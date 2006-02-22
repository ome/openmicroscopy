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
import ome.api.IAnalysis;
import ome.dao.AnalysisDao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//Application-internal dependencies
import ome.dao.AnnotationDao;
import ome.dao.ContainerDao;

import ome.model.containers.Dataset;
import ome.model.meta.Experimenter;
import ome.model.containers.Project;


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
public class AnalysisImpl extends AbstractLevel2Service implements IAnalysis {

    private static Log log = LogFactory.getLog(AnalysisImpl.class);

    @Override
    protected String getName()
    {
        return IAnalysis.class.getName();
    }
    
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
	public Set getProjectsForUser(long experimenterId) {
		//return aDao.getProjectsForUser(experimenterId); TODO remove from Analysis Dao?

		// Criteria
		Experimenter exp = new Experimenter();
		exp.setId(experimenterId);
		
		// Example
		Project prj = new Project();
		prj.getDetails().setOwner(exp);
		
		return new HashSet(_query.getListByExample(prj)); 
        // FIXME does this work. if not:
        // "from Project p where p.experimenter = :expId"
	}

	public Set getAllDatasets() {
		return new HashSet(_query.getListByExample(new Dataset()));
	}

	public Set getAllForImage(long imageId) {
		// TODO Auto-generated method stub
		//return null;
		throw new RuntimeException("Not implemented yet.");
	}

//	public Set getChainExecutionsForDataset(int datasetId) {
//
//		// Criteria
//		Dataset ds = new Dataset();
//		ds.setId(datasetId);
//		
//		// Example
//		AnalysisChainExecution ace = new AnalysisChainExecution();
//		ace.setDataset(ds);
//		
//		return new HashSet(_query.getListByExample(ace));
//	}
// 
	// Criteria is a set ~~~~~~~~~~~~~~~~~~~~~~~`
	
	public Set getDatasetsForProject(long projectId) {
		
		StringBuilder sb = new StringBuilder();
		sb.append(" select d from Dataset d ");
		sb.append("  left outer join fetch d.projects p ");
		sb.append("  where p.projectId = ?");
		
		return new HashSet(_query.queryList(sb.toString(),new Object[]{projectId}));
		
	}

	public Set getProjectsForDataset(long datasetId) {
		StringBuilder sb = new StringBuilder();
		sb.append(" select p from Project p ");
		sb.append("  left outer join fetch p.datasets d ");
		sb.append("  where d.datasetId = ?");
		
		return new HashSet(_query.queryList(sb.toString(),new Object[]{datasetId}));
	}

	public Set getImagesForDataset(long datasetId) {
		StringBuilder sb = new StringBuilder();
		sb.append(" select i from Image i ");
		sb.append("  left outer join fetch i.datasets d ");
		sb.append("  where d.datasetId = ?");
		
		return new HashSet(_query.queryList(sb.toString(),new Object[]{datasetId}));
	}

	

}