/*
 * Created on Feb 19, 2005
*/
package org.ome.srv.logic;

import org.ome.cache.CacheFactory;
import org.ome.cache.TemporaryCacheFactoryFactory;
import org.ome.interfaces.ServiceFactory;
import org.ome.srv.db.TemporaryDBFactoryFactory;

/**
 * @author josh
 */
public abstract class AbstractServiceImpl {

	protected ServiceFactory dbFactory = TemporaryDBFactoryFactory
		.getServiceFactory();

	protected CacheFactory cacheFactory = TemporaryCacheFactoryFactory
		.getCacheFactory();
	
}
