/*
 * Created on Feb 9, 2005
 */
package org.ome.interfaces;

import org.ome.LSID;
import org.ome.texen.interfaces.*;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * responsible for all user and group adminisistration as well as security
 * privielges and ownership.
 * 
 * @author josh
 */
public interface AdministrationService extends Remote {
/*
	public String getSessionKey() throws RemoteException;

	public IExperimenter createExperimenter() throws RemoteException; // password
																	  // TODO

	public int createExperimenter(IExperimenter data) throws RemoteException;

	public IExperimenter getExperimenter(int experimenterId)
			throws RemoteException;

	public IExperimenter getExperimenter(int experimenterId,
			int predicateGroupId) throws RemoteException; // TODO use enums
*/
	public List retrieveProjectsByExperimenter(LSID experimenterId)
			throws RemoteException;//TODO use generics
/*
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

	public List retrieveImagesByExperimenter(int experimenterId)
			throws RemoteException;

	public List retrieveImagesByExperimenter(int experimenterId,
			int predicateGroupId) throws RemoteException;

	public IImage retrieveImage(int imageId) throws RemoteException;

	public IImage retrieveImage(int imageId, int predicateGroupId)
			throws RemoteException;

	// The next could be generated as (?container :contains ?image)

	public IImage retrieveImagesByDataset(int datasetId) throws RemoteException;

	public IImage retrieveImagesByProject(int projectId) throws RemoteException;

	//

	public IProject createProject() throws RemoteException;

	public int createProject(IProject data) throws RemoteException;

	public void updateProject(IProject data) throws RemoteException;

	public void setProject(IProject data) throws RemoteException;

	public IDataset createDataset() throws RemoteException;

	public int createDataset(IDataset data) throws RemoteException;

	public void updateDataset(IDataset data) throws RemoteException;

	public void setDataset(IDataset data) throws RemoteException;

	public void updateImage(IImage data) throws RemoteException;

	public void setImage(IImage data) throws RemoteException;

	public List retrieveIDPHierarchy(int[] images) throws RemoteException;
*/
}