/*
 * Created on Feb 19, 2005
*/
package org.ome.srv.db.jena;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ome.interfaces.ContainerService;
import org.ome.model.IDataset;
import org.ome.model.IProject;
import org.ome.model.LSID;
import org.ome.model.LSObject;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdql.QueryResults;

/**
 * @author josh
 */
public class JenaContainerStore extends JenaAbstractStore implements ContainerService {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ome.interfaces.RemoteAdministrationService#retrieveProjectsByExperimenter(int)
	 */
	public List retrieveProjectsByExperimenter(LSID experimenterId)
			throws RemoteException {

		Model m = JenaModelFactory.getModel();
		Map map = new HashMap();

		map.put("exp", m.getResource(experimenterId.getURI()));
		QueryResults results = queryWithBinding(m, JenaQueryStrings
				.getProjectsByExperimenterQueryString(), map);

		List l = lsObjectFromQueryResult(results, "project");
		results.close();
		return l;
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ContainerService#retrieveProjectsByExperimenter(org.ome.model.LSID, org.ome.model.LSID)
	 */
	public List retrieveProjectsByExperimenter(LSID arg0, LSID arg1) throws RemoteException {
		// TODO Auto-generated method stub
		/* return null; */
		throw new RuntimeException("implement me");
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ContainerService#retrieveProject(org.ome.model.LSID)
	 */
	public IProject retrieveProject(LSID arg0) throws RemoteException {
		// TODO Auto-generated method stub
		/* return null; */
		throw new RuntimeException("implement me");
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ContainerService#retrieveProject(org.ome.model.LSID, int)
	 */
	public IProject retrieveProject(LSID arg0, int arg1) throws RemoteException {
		// TODO Auto-generated method stub
		/* return null; */
		throw new RuntimeException("implement me");
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.ContainerService#retrieveDatasetsByExperimenter(org.ome.model.LSID)
	 */
	public List retrieveDatasetsByExperimenter(LSID arg0) throws RemoteException {
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
	 * @see org.ome.interfaces.ContainerService#retrieveDataset(org.ome.model.LSID)
	 */
	public IDataset retrieveDataset(LSID arg0) throws RemoteException {
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
	 * @see org.ome.interfaces.ContainerService#retrieveIDPHierarchy(org.ome.model.LSObject[])
	 */
	public List retrieveIDPHierarchy(LSObject[] arg0) throws RemoteException {
		// TODO Auto-generated method stub
		/* return null; */
		throw new RuntimeException("implement me");
	}
}
