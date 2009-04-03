/*
 * org.openmicroscopy.shoola.env.cache.CacheServiceImpl 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.env.cache;



//Java imports
import java.io.InputStream;

//Third-party libraries
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

//Application-internal dependencies

/** 
 * Provides the caching service.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta3
 */
class CacheServiceImpl
	implements CacheService
{
	
	/** Reference to the manager. */
	private CacheManager manager;
	
	/** The id of the last created cache. */
	private int 		cacheID;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param stream
	 */
	CacheServiceImpl(InputStream stream)
	{
		manager = new CacheManager(stream);
		cacheID = -1;
	}

	/** Shuts down the cache manager. */
	void shutDown()
	{
		try {
			manager.shutdown();
		} catch (Exception e) {
			//just it case it has already been shut down.
		}
	}
	
	/** 
	 * Implemented as specified by {@link CacheService}.
	 * @see CacheService#createCache()
	 */
	public int createCache() { return createCache(DEFAULT); }

	/** 
	 * Implemented as specified by {@link CacheService}.
	 * @see CacheService#createCache(int, int)
	 */
	public int createCache(int type, int size) 
	{
		Cache cache;
		if (size < 0) size = 0;
		switch (type) {
			case PERSISTENCE_ON_DISK:
				//TODO: implement.
				return -1;
			case IN_MEMORY:
				cacheID++;
				//name, maximum number of elements, overflow to disk, eternal
				//time to Idle, time to live
				//cache = new Cache(""+cacheID, size, true, false, 300, 600);
				cache = new Cache(""+cacheID, size, 
						MemoryStoreEvictionPolicy.LRU, true, 
						manager.getDiskStorePath(), false, 300, 600,
						false, 300, null, null, 10000000);
				manager.addCache(cache);
				//cache.initialise();
				break;
			case DEFAULT:
				cacheID++;
				manager.addCache(""+cacheID);
				break;
			default:
				return -1;
		}
		return cacheID;
		
	}
	
	/** 
	 * Implemented as specified by {@link CacheService}.
	 * @see CacheService#createCache(int)
	 */
	public int createCache(int type) 
	{
		return createCache(type, CACHE_SIZE);
	}

	/** 
	 * Implemented as specified by {@link CacheService}.
	 * @see CacheService#removeCache(int)
	 */
	public void removeCache(int cacheID)
	{
		Cache cache = manager.getCache(""+cacheID);
		if (cache != null) manager.removeCache(""+cacheID);
	}
	
	/** 
	 * Implemented as specified by {@link CacheService}.
	 * @see CacheService#addElement(int, Object, Object)
	 */
	public void addElement(int cacheID, Object key, Object element) 
	{
		Cache cache = manager.getCache(""+cacheID);
		if (cache == null) return;
		if (cache.getSize() >= 
			cache.getCacheConfiguration().getMaxElementsInMemory())
			cache.flush();
		cache.put(new Element(key, element));
	}
	
	/** 
	 * Implemented as specified by {@link CacheService}.
	 * @see CacheService#getElement(int, Object)
	 */
	public Object getElement(int cacheID, Object key)
	{
		Cache cache = manager.getCache(""+cacheID);
		if (cache == null) return null;
		Element element = cache.get(key);
		if (element == null) return null;
		return element.getObjectValue();
	}
	
	/** 
	 * Implemented as specified by {@link CacheService}.
	 * @see CacheService#clearCache(int)
	 */
	public void clearCache(int cacheID) 
	{
		Cache cache = manager.getCache(""+cacheID);
		if (cache == null) return;
		cache.removeAll();
	}

	/** 
	 * Implemented as specified by {@link CacheService}.
	 * @see CacheService#clearAllCaches()
	 */
	public void clearAllCaches() 
	{
		String[] names = manager.getCacheNames();
		if (names == null) return;
		Cache cache;
		for (int i = 0; i < names.length; i++) {
			cache = manager.getCache(names[i]);
			if (cache != null) cache.removeAll();
		}
	}
	
	/** 
	 * Implemented as specified by {@link CacheService}.
	 * @see CacheService#setCacheEntries(int, int)
	 */
	public void setCacheEntries(int cacheID, int size)
	{
		Cache cache = manager.getCache(""+cacheID);
		if (cache == null) return;
		cache.flush();
		cache.getCacheConfiguration().setMaxElementsInMemory(size);
	}
	
}
