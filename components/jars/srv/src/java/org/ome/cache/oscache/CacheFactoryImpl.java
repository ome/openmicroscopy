/*
 * Created on Feb 12, 2005
 */
package org.ome.cache.oscache;

import org.ome.cache.CacheFactory;
import org.ome.cache.Cache;

/**
 * @author josh
 */
public class CacheFactoryImpl implements CacheFactory {

	protected Cache cache;
	
	public Cache getCache() {
		if (null == cache) {
			cache = new OSCache();
		}
		return cache;
	}

}
