/*
 * Created on Feb 10, 2005
 */
package org.ome.srv.logic;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;

import org.ome.model.IExperimenter;
import org.ome.model.LSID;
import org.ome.model.LSObject;
import org.ome.cache.Cache;
import org.ome.cache.CacheFactory;
import org.ome.cache.TemporaryCacheFactoryFactory;
import org.ome.interfaces.AdministrationService;
import org.ome.interfaces.ContainerService;
import org.ome.interfaces.ServiceFactory;
import org.ome.srv.db.TemporaryDBFactoryFactory;
import org.ome.model.ProjectWrapper;

/**
 * @author josh
 */
public class AdministrationServiceImpl implements AdministrationService {

	protected ServiceFactory dbFactory = TemporaryDBFactoryFactory
			.getServiceFactory();

	protected CacheFactory cacheFactory = TemporaryCacheFactoryFactory
			.getCacheFactory();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ome.srv.RemoteAdministrationService#retrieveProjectsByExperimenter(int)
	 */
	public List retrieveProjectsByExperimenter(LSID experimenterId)
			throws RemoteException {

		ContainerService containerService = dbFactory.getContainerService();
		Cache cache = cacheFactory.getCache();
		
		List lsObjects = containerService
				.retrieveProjectsByExperimenter(experimenterId);

		for (Iterator iter = lsObjects.iterator(); iter.hasNext();) {
			LSObject obj = (LSObject) iter.next();
			
		}
		
		
		List domainObjects = ProjectWrapper.wrap(lsObjects);

		return domainObjects;
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.AdministrationService#getSessionKey()
	 */
	public String getSessionKey() throws RemoteException {
		// TODO Auto-generated method stub
		/* return null; */
		throw new RuntimeException("implement me");
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.AdministrationService#createExperimenter()
	 */
	public IExperimenter createExperimenter() throws RemoteException {
		// TODO Auto-generated method stub
		/* return null; */
		throw new RuntimeException("implement me");
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.AdministrationService#createExperimenter(org.ome.model.IExperimenter)
	 */
	public int createExperimenter(IExperimenter arg0) throws RemoteException {
		// TODO Auto-generated method stub
		/* return 0; */
		throw new RuntimeException("implement me");
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.AdministrationService#getExperimenter(org.ome.model.LSID)
	 */
	public IExperimenter getExperimenter(LSID arg0) throws RemoteException {
		// TODO Auto-generated method stub
		/* return null; */
		throw new RuntimeException("implement me");
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.AdministrationService#getExperimenter(org.ome.model.LSID, org.ome.model.LSID)
	 */
	public IExperimenter getExperimenter(LSID arg0, LSID arg1) throws RemoteException {
		// TODO Auto-generated method stub
		/* return null; */
		throw new RuntimeException("implement me");
	}

	
}
