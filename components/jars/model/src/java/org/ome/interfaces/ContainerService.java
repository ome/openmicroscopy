/*
 * Created on Feb 9, 2005
 */
package org.ome.interfaces;

import java.util.List;

import org.ome.model.IDataset;
import org.ome.model.IProject;
import org.ome.model.LSID;
import org.ome.model.LSObject;

/**
 * the container service interface represents the top level functionality of all
 * OWL Container Classes.
 * 
 * @author josh
 */
public interface ContainerService {

	public List retrieveProjectsByExperimenter(LSID experimenterId);//TODO use generics

	public List retrieveProjectsByExperimenter(LSID experimenterId,
			LSID predicateGroupId) ;

	public IProject retrieveProject(LSID projectId) ;

	public IProject retrieveProject(LSID projectId, LSID predicateGroupId);

	public List retrieveDatasetsByExperimenter(LSID experimenterId);

	public List retrieveDatasetsByExperimenter(LSID experimenterId,
			LSID predicateGroupId) ;

	public IDataset retrieveDataset(LSID datasetId) ;

	public IDataset retrieveDataset(LSID datasetId, LSID predicateGroupId);


	//

	//FIXME should these exist (in temporary models) or must one pass in the data
	public IProject createProject() ;

	public LSID createProject(IProject data) ;

	public void updateProject(IProject data) ;

	public void setProject(IProject data) ;

	public IDataset createDataset() ;

	public LSID createDataset(IDataset data) ;

	public void updateDataset(IDataset data) ;

	public void setDataset(IDataset data) ;

	public List retrieveIDPHierarchy(LSObject[] images) ;

}