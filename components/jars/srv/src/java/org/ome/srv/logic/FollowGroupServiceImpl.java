/*
 * Created on Feb 10, 2005
 */
package org.ome.srv.logic;

import java.rmi.RemoteException;

import org.ome.model.FollowGroup;
import org.ome.model.FollowGroupWrapper;
import org.ome.model.IFollowGroup;
import org.ome.model.LSID;
import org.ome.model.LSObject;
import org.ome.cache.Cache;
import org.ome.cache.CacheFactory;
import org.ome.cache.TemporaryCacheFactoryFactory;
import org.ome.interfaces.FollowGroupService;
import org.ome.interfaces.ServiceFactory;
import org.ome.srv.db.TemporaryDBFactoryFactory;

/**
 * @author josh
 */
public class FollowGroupServiceImpl implements FollowGroupService {

	protected ServiceFactory dbFactory = TemporaryDBFactoryFactory
			.getServiceFactory();

	protected CacheFactory cacheFactory = TemporaryCacheFactoryFactory
			.getCacheFactory();

	/* (non-Javadoc)
	 * @see org.ome.interfaces.PredicateGroupService#getPredicateGroup(org.ome.LSID)
	 */
	public IFollowGroup getFollowGroup(LSID lsid) throws RemoteException {
		FollowGroupService pgService = dbFactory.getFollowGroupService();
		Cache cache = cacheFactory.getCache();
		
		LSObject obj=null;//FIXME = pgService.getFollowGroup(lsid);
		
		IFollowGroup pg = new FollowGroup(obj);

		return pg;
	}

	
}
