/*
 * Created on Feb 19, 2005
 */
package org.ome.srv.logic;

import java.rmi.RemoteException;

import java.util.List;

import org.ome.interfaces.ContainerService;
import org.ome.model.Dataset;
import org.ome.model.IDataset;
import org.ome.model.IProject;
import org.ome.model.LSID;
import org.ome.model.LSObject;
import org.ome.model.Project;
import org.ome.model.ProjectWrapper;
import org.ome.srv.db.NamedQuery;
import org.ome.srv.db.queries.DatasetsByExperimenterQuery;
import org.ome.srv.db.queries.ProjectsByExperimenterQuery;

/**
 * @author josh
 */
public class ContainerServiceImpl extends AbstractService implements ContainerService {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ome.srv.RemoteAdministrationService#retrieveProjectsByExperimenter(int)
	 */
	public List retrieveProjectsByExperimenter(LSID experimenterId)
			throws RemoteException {

		NamedQuery nq = new ProjectsByExperimenterQuery(experimenterId);
		List lsObjects = db.evaluateNamedQuery(nq);
		List domainObjects = ProjectWrapper.wrap(lsObjects);
				
		return domainObjects;
	}
	
	/* (non-Javadoc)
	 * @see org.ome.interfaces.ContainerService#retrieveDatasetsByExperimenter(org.ome.model.LSID)
	 */
	public List retrieveDatasetsByExperimenter(LSID experimenterId) throws RemoteException {
		NamedQuery nq = new DatasetsByExperimenterQuery(experimenterId);
		List lsObjects = db.evaluateNamedQuery(nq);
		List domainObjects = ProjectWrapper.wrap(lsObjects);
				
		return domainObjects;
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ContainerService#retrieveProject(org.ome.model.LSID)
	 */
	public IProject retrieveProject(LSID lsid) throws RemoteException {
		return new Project(db.getLSObject(lsid));
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ContainerService#retrieveDataset(org.ome.model.LSID)
	 */
	public IDataset retrieveDataset(LSID lsid) throws RemoteException {
		return new Dataset(db.getLSObject(lsid));
	}

	//TODO ==============================================
	
	/* (non-Javadoc)
	 * @see org.ome.interfaces.ContainerService#retrieveProjectsByExperimenter(org.ome.model.LSID, org.ome.model.LSID)
	 */
	public List retrieveProjectsByExperimenter(LSID arg0, LSID arg1) throws RemoteException {
		// TODO Auto-generated method stub
		/* return null; */
		throw new RuntimeException("implement me");
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ContainerService#retrieveProject(org.ome.model.LSID, org.ome.model.LSID)
	 */
	public IProject retrieveProject(LSID arg0, LSID arg1) throws RemoteException {
		// TODO Auto-generated method stub
		/* return null; */
		throw new RuntimeException("implement me");
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ContainerService#retrieveDatasetsByExperimenter(org.ome.model.LSID, org.ome.model.LSID)
	 */
	public List retrieveDatasetsByExperimenter(LSID arg0, LSID arg1) throws RemoteException {
		// TODO Auto-generated method stub
		/* return null; */
		throw new RuntimeException("implement me");
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ContainerService#retrieveDataset(org.ome.model.LSID, org.ome.model.LSID)
	 */
	public IDataset retrieveDataset(LSID arg0, LSID arg1) throws RemoteException {
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
	public LSID createProject(IProject arg0) throws RemoteException {
		// TODO Auto-generated method stub
		/* return null; */
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
	public LSID createDataset(IDataset arg0) throws RemoteException {
		// TODO Auto-generated method stub
		/* return null; */
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
	 * @see org.ome.interfaces.ContainerService#retrieveIDPHierarchy(org.ome.model.LSObject[])
	 */
	public List retrieveIDPHierarchy(LSObject[] arg0) throws RemoteException {
		// TODO Auto-generated method stub
		/* return null; */
		throw new RuntimeException("implement me");
	}

	
}