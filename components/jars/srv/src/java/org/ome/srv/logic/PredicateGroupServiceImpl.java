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
import org.ome.interfaces.PredicateGroupService;
import org.ome.interfaces.ServiceFactory;
import org.ome.srv.db.TemporaryDBFactoryFactory;
import org.ome.texen.interfaces.IPredicateGroup;
import org.ome.texen.srv.PredicateGroupWrapper;
import org.ome.texen.srv.ProjectWrapper;

/**
 * @author josh
 */
public class PredicateGroupServiceImpl implements PredicateGroupService {

	protected ServiceFactory dbFactory = TemporaryDBFactoryFactory
			.getServiceFactory();

	protected CacheFactory cacheFactory = TemporaryCacheFactoryFactory
			.getCacheFactory();

	/* (non-Javadoc)
	 * @see org.ome.interfaces.PredicateGroupService#getPredicateGroup(org.ome.LSID)
	 */
	public IPredicateGroup getPredicateGroup(LSID lsid) throws RemoteException {
		PredicateGroupService pgService = dbFactory.getPredicateGroupService();
		Cache cache = cacheFactory.getCache();
		
		ILSObject obj=null;//FIXME = pgService.getPredicateGroup(lsid);
		
		IPredicateGroup pg = PredicateGroupWrapper.wrap(obj);

		return pg;
	}

	
}
