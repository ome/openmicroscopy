/*
 * ome.logic.AnalysisImpl
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

/*------------------------------------------------------------------------------
 *
 * Written by:    Josh Moore <josh.moore@gmx.de>
 *
 *------------------------------------------------------------------------------
 */

package ome.logic;

//Java imports
import java.util.HashSet;
import java.util.Set;

import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

//Third-party libraries
import org.springframework.transaction.annotation.Transactional;

//Application-internal dependencies
import ome.api.IAnalysis;
import ome.api.ServiceInterface;
import ome.model.containers.Dataset;
import ome.model.meta.Experimenter;
import ome.model.containers.Project;
import ome.parameters.Parameters;

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
@TransactionManagement(TransactionManagementType.BEAN)
@Transactional(readOnly=true)
public class AnalysisImpl extends AbstractLevel2Service implements IAnalysis {

    @Override
    protected Class<? extends ServiceInterface> getServiceInterface()
    {
        return IAnalysis.class;
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
		
		return new HashSet(iQuery.findAllByExample(prj,null)); 
        // FIXME does this work. if not:
        // "from Project p where p.experimenter = :expId"
	}

	public Set getAllDatasets() {
		return new HashSet(iQuery.findAllByExample(new Dataset(),null));
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
//		return new HashSet(iQuery.getListByExample(ace));
//	}
// 
	// Criteria is a set ~~~~~~~~~~~~~~~~~~~~~~~`
	
	public Set getDatasetsForProject(long projectId) {
		
		StringBuilder sb = new StringBuilder();
		sb.append(" select d from Dataset d ");
		sb.append("  left outer join fetch d.projectLinks pdl ");
        sb.append("  left outer join fetch pdl.parent p ");
		sb.append("  where p.id = :id");
		
		return new HashSet(iQuery.findAllByQuery(
                sb.toString(),new Parameters().addId( projectId )));
		
	}

	public Set getProjectsForDataset(long datasetId) {
		StringBuilder sb = new StringBuilder();
		sb.append(" select p from Project p ");
		sb.append("  left outer join fetch p.datasetLinks pdl ");
        sb.append("  left outer join fetch pdl.child d ");
		sb.append("  where d.id = :id");
		
		return new HashSet(iQuery.findAllByQuery(
                sb.toString(),new Parameters().addId( datasetId )));
	}

	public Set getImagesForDataset(long datasetId) {
		StringBuilder sb = new StringBuilder();
		sb.append(" select i from Image i ");
		sb.append("  left outer join fetch i.datasetLinks dil ");
        sb.append("  left outer join fetch dil.parent d ");
		sb.append("  where d.id = :id");
		
		return new HashSet(iQuery.findAllByQuery(
                sb.toString(),new Parameters().addId( datasetId )));
	}

	

}