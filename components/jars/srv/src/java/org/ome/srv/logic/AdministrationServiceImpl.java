/*
 * Created on Feb 10, 2005
 */
package org.ome.srv.logic;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;

import org.ome.ILSObject;
import org.ome.LSID;
import org.ome.LSObject;
import org.ome.cache.Cache;
import org.ome.cache.CacheFactory;
import org.ome.cache.TemporaryCacheFactoryFactory;
import org.ome.interfaces.AdministrationService;
import org.ome.interfaces.ServiceFactory;
import org.ome.srv.db.TemporaryDBFactoryFactory;
import org.ome.texen.srv.ProjectWrapper;

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

		AdministrationService adminService = dbFactory.getAdministrationService();
		Cache cache = cacheFactory.getCache();
		
		List lsObjects = adminService
				.retrieveProjectsByExperimenter(experimenterId);

		for (Iterator iter = lsObjects.iterator(); iter.hasNext();) {
			ILSObject obj = (ILSObject) iter.next();
			
		}
		
		
		List domainObjects = ProjectWrapper.wrap(lsObjects);

		return domainObjects;
	}

	
}
