/*
 * Created on Feb 9, 2005
 */
package org.ome.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
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
public interface ContainerService extends Remote{

	public List retrieveProjectsByExperimenter(LSID experimenterId)
			throws RemoteException;//TODO use generics

	public List retrieveProjectsByExperimenter(LSID experimenterId,
			LSID predicateGroupId) throws RemoteException;

	public IProject retrieveProject(LSID projectId) throws RemoteException;

	public IProject retrieveProject(LSID projectId, LSID predicateGroupId)
			throws RemoteException;

	public List retrieveDatasetsByExperimenter(LSID experimenterId)
			throws RemoteException;

	public List retrieveDatasetsByExperimenter(LSID experimenterId,
			LSID predicateGroupId) throws RemoteException;

	public IDataset retrieveDataset(LSID datasetId) throws RemoteException;

	public IDataset retrieveDataset(LSID datasetId, LSID predicateGroupId)
			throws RemoteException;


	//

	public IProject createProject() throws RemoteException;

	public LSID createProject(IProject data) throws RemoteException;

	public void updateProject(IProject data) throws RemoteException;

	public void setProject(IProject data) throws RemoteException;

	public IDataset createDataset() throws RemoteException;

	public LSID createDataset(IDataset data) throws RemoteException;

	public void updateDataset(IDataset data) throws RemoteException;

	public void setDataset(IDataset data) throws RemoteException;

	public List retrieveIDPHierarchy(LSObject[] images) throws RemoteException;

}