/*
 * Created on Feb 19, 2005
*/
package org.ome.srv.logic;

import org.ome.cache.CacheFactory;
import org.ome.cache.TemporaryCacheFactoryFactory;
import org.ome.srv.db.GenericStore;
import org.ome.srv.db.TemporaryDBFactoryFactory;

/**
 * @author josh
 */
public abstract class AbstractService {

	protected GenericStore db = TemporaryDBFactoryFactory
		.getStore();

	protected CacheFactory cacheFactory = TemporaryCacheFactoryFactory
		.getCacheFactory();
	
}
