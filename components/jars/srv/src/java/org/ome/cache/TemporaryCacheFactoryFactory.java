/*
 * Created on Feb 12, 2005
 */
package org.ome.cache;

import org.ome.cache.oscache.CacheFactoryImpl;

/**
 * this temporary factory factory is simply here to avoid undue complexity.
 * Currently it simply returns the OSCache Service Factory.
 * 
 * @author josh
 */
public class TemporaryCacheFactoryFactory {
	public static CacheFactory getCacheFactory() {
		return new CacheFactoryImpl();
	}
}
