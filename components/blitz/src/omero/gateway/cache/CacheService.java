/*
 * org.openmicroscopy.shoola.env.cache.CacheService 
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
package omero.gateway.cache;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Defines the caching service interface.
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
public interface CacheService
{
	
	/** Indicates to create a default cache. */
	public static final int DEFAULT = 0;
	
	/** Indicates to cache data on disk. */
	public static final int PERSISTENCE_ON_DISK = 1;
	
	/** Indicates to cache data in memory only. */
	public static final int IN_MEMORY = 2;
	
	/** The default size of a cache. */
	public static final int CACHE_SIZE = 10;
	
	/**
	 * Creates a default cache.
	 * 
	 * @return See above.
	 */
	public int createCache();
	
	/**
	 * Creates a cache of a given type.
	 * 
	 * @param type The type of cache to create.
	 * @param size The size of the cache.
	 * @return See above.
	 */
	public int createCache(int type, int size);
	
	/**
	 * Creates a cache of a given type.
	 * 
	 * @param type The type of cache to create.
	 * @return See above.
	 */
	public int createCache(int type);
	
	/** 
	 * Removes the cache corresponding to the passed id.
	 * 
	 * @param cacheID The id of the cache.
	 */
	public void removeCache(int cacheID);
	
	/**
	 * Adds the specified element to the selected cache.
	 * 
	 * @param cacheID The id of the cache.
	 * @param key	  The key corresponding to the element to add.
	 * @param element The element to add.
	 */
	public void addElement(int cacheID, Object key, Object element);
	
	/**
	 * Adds the specified element to the selected cache.
	 * 
	 * @param cacheID The id of the cache.
	 * @param key	  The key corresponding to the element to add.
	 * @return See above.
	 */
	public Object getElement(int cacheID, Object key);
	
	/**
	 * Clears the specified cache.
	 * 
	 * @param cacheID The id of the cache to clear.
	 */
	public void clearCache(int cacheID);
	
	/**
	 * Resets the number of items in memory of the cache, 
	 * when a new cache is created or deleted.
	 * 
	 * @param cacheID	The id of the cache.
	 * @param entries	The number of entries.
	 */
	public void setCacheEntries(int cacheID, int entries);
	
	/** Clears all the caches. */
	public void clearAllCaches();
	
	public void shutDown();
	
}
