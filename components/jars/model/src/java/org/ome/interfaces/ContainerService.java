/*
 * Created on Feb 9, 2005
 */
package org.ome.interfaces;

import java.rmi.RemoteException;
import java.util.List;

import org.ome.model.IDataset;
import org.ome.model.IProject;
import org.ome.model.LSID;

/**
 * the container service interface represents the top level functionality of all
 * OWL Container Classes.
 * 
 * @author josh
 */
public interface ContainerService {

	public List retrieveProjectsByExperimenter(LSID experimenterId)
			throws RemoteException;//TODO use generics

	public List retrieveProjectsByExperimenter(int experimenterId,
			int predicateGroupId) throws RemoteException;

	public IProject retrieveProject(int projectId) throws RemoteException;

	public IProject retrieveProject(int projectId, int predicateGroupId)
			throws RemoteException;

	public List retrieveDatasetsByExperimenter(int experimenterId)
			throws RemoteException;

	public List retrieveDatasetsByExperimenter(int experimenterId,
			int predicateGroupId) throws RemoteException;

	public IDataset retrieveDataset(int datasetId) throws RemoteException;

	public IDataset retrieveDataset(int datasetId, int predicateGroupId)
			throws RemoteException;


	//

	public IProject createProject() throws RemoteException;

	public int createProject(IProject data) throws RemoteException;

	public void updateProject(IProject data) throws RemoteException;

	public void setProject(IProject data) throws RemoteException;

	public IDataset createDataset() throws RemoteException;

	public int createDataset(IDataset data) throws RemoteException;

	public void updateDataset(IDataset data) throws RemoteException;

	public void setDataset(IDataset data) throws RemoteException;

	public List retrieveIDPHierarchy(int[] images) throws RemoteException;

}