/*
 * Created on Feb 14, 2005
*/
package org.ome.srv.logic;

import org.ome.ILSObject;
import org.ome.LSID;
import org.ome.LSObject;
import org.ome.cache.CacheFactory;
import org.ome.cache.TemporaryCacheFactoryFactory;
import org.ome.interfaces.GenericService;
import org.ome.interfaces.ServiceFactory;
import org.ome.srv.db.TemporaryDBFactoryFactory;
import org.ome.texen.srv.PredicateGroup;

/**
 * @author josh
 */
public class GenericServiceImpl implements GenericService {

	protected ServiceFactory dbFactory = TemporaryDBFactoryFactory
	.getServiceFactory();

	protected CacheFactory cacheFactory = TemporaryCacheFactoryFactory
	.getCacheFactory();
	
	/* (non-Javadoc)
	 * @see org.ome.interfaces.GenericService#setLSOjbect(org.ome.LSObject)
	 */
	public void setLSOjbect(ILSObject obj) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.GenericService#updateLSObject(org.ome.LSObject)
	 */
	public void updateLSObject(ILSObject obj) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.GenericService#getLSObject(org.ome.LSID)
	 */
	public ILSObject getLSObject(LSID lsid) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.GenericService#getLSObject(org.ome.LSID, org.ome.texen.srv.PredicateGroup)
	 */
	public ILSObject getLSObject(LSID lsid, PredicateGroup pg) {
		// TODO Auto-generated method stub
		return null;
	}

}
