/*
 * Created on Feb 19, 2005
*/
package org.ome.srv.logic;

import java.rmi.RemoteException;
import java.util.List;

import org.ome.interfaces.ContainerService;
import org.ome.model.IDataset;
import org.ome.model.IProject;
import org.ome.model.LSID;

/**
 * @author josh
 */
public class ContainerServiceImpl implements ContainerService {

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ContainerService#retrieveProjectsByExperimenter(org.ome.model.LSID)
	 */
	public List retrieveProjectsByExperimenter(LSID arg0) throws RemoteException {
		// TODO Auto-generated method stub
		/* return null; */
		throw new RuntimeException("implement me");
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ContainerService#retrieveProjectsByExperimenter(int, int)
	 */
	public List retrieveProjectsByExperimenter(int arg0, int arg1) throws RemoteException {
		// TODO Auto-generated method stub
		/* return null; */
		throw new RuntimeException("implement me");
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ContainerService#retrieveProject(int)
	 */
	public IProject retrieveProject(int arg0) throws RemoteException {
		// TODO Auto-generated method stub
		/* return null; */
		throw new RuntimeException("implement me");
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ContainerService#retrieveProject(int, int)
	 */
	public IProject retrieveProject(int arg0, int arg1) throws RemoteException {
		// TODO Auto-generated method stub
		/* return null; */
		throw new RuntimeException("implement me");
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ContainerService#retrieveDatasetsByExperimenter(int)
	 */
	public List retrieveDatasetsByExperimenter(int arg0) throws RemoteException {
		// TODO Auto-generated method stub
		/* return null; */
		throw new RuntimeException("implement me");
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ContainerService#retrieveDatasetsByExperimenter(int, int)
	 */
	public List retrieveDatasetsByExperimenter(int arg0, int arg1) throws RemoteException {
		// TODO Auto-generated method stub
		/* return null; */
		throw new RuntimeException("implement me");
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ContainerService#retrieveDataset(int)
	 */
	public IDataset retrieveDataset(int arg0) throws RemoteException {
		// TODO Auto-generated method stub
		/* return null; */
		throw new RuntimeException("implement me");
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ContainerService#retrieveDataset(int, int)
	 */
	public IDataset retrieveDataset(int arg0, int arg1) throws RemoteException {
		// TODO Auto-generated method stub
		/* return null; */
		throw new RuntimeException("implement me");
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ContainerService#createProject()
	 */
	public IProject createProject() throws RemoteException {
		// TODO Auto-generated method stub
		/* return null; */
		throw new RuntimeException("implement me");
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ContainerService#createProject(org.ome.model.IProject)
	 */
	public int createProject(IProject arg0) throws RemoteException {
		// TODO Auto-generated method stub
		/* return 0; */
		throw new RuntimeException("implement me");
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ContainerService#updateProject(org.ome.model.IProject)
	 */
	public void updateProject(IProject arg0) throws RemoteException {
		// TODO Auto-generated method stub
		/*  */
		throw new RuntimeException("implement me");
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ContainerService#setProject(org.ome.model.IProject)
	 */
	public void setProject(IProject arg0) throws RemoteException {
		// TODO Auto-generated method stub
		/*  */
		throw new RuntimeException("implement me");
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ContainerService#createDataset()
	 */
	public IDataset createDataset() throws RemoteException {
		// TODO Auto-generated method stub
		/* return null; */
		throw new RuntimeException("implement me");
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ContainerService#createDataset(org.ome.model.IDataset)
	 */
	public int createDataset(IDataset arg0) throws RemoteException {
		// TODO Auto-generated method stub
		/* return 0; */
		throw new RuntimeException("implement me");
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ContainerService#updateDataset(org.ome.model.IDataset)
	 */
	public void updateDataset(IDataset arg0) throws RemoteException {
		// TODO Auto-generated method stub
		/*  */
		throw new RuntimeException("implement me");
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ContainerService#setDataset(org.ome.model.IDataset)
	 */
	public void setDataset(IDataset arg0) throws RemoteException {
		// TODO Auto-generated method stub
		/*  */
		throw new RuntimeException("implement me");
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ContainerService#retrieveIDPHierarchy(int[])
	 */
	public List retrieveIDPHierarchy(int[] arg0) throws RemoteException {
		// TODO Auto-generated method stub
		/* return null; */
		throw new RuntimeException("implement me");
	}

}
