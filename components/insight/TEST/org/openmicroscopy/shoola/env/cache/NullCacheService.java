/*
 * org.openmicroscopy.shoola.env.cache.NullCacheService 
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

import omero.gateway.cache.CacheService;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * 
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
public class NullCacheService 
	implements CacheService
{

	/**
     * No-op implementation
     * @see CacheService#addElement(int, Object, Object)
     */
	public void addElement(int cacheID, Object key, Object element) {}

    /**
     * No-op implementation
     * @see CacheService#clearCache(int)
     */
	public void clearCache(int cacheID) {}

    /**
     * No-op implementation
     * @see CacheService#createCache()
     */
	public int createCache() { return 0; }

    /**
     * No-op implementation
     * @see CacheService#createCache(int)
     */
	public int createCache(int type) { return 0; }

    /**
     * No-op implementation
     * @see CacheService#getElement(int, Object)
     */
	public Object getElement(int cacheID, Object key) { return null; }

    /**
     * No-op implementation
     * @see CacheService#removeCache(int)
     */
	public void removeCache(int cacheID) {}

    /**
     * No-op implementation
     * @see CacheService#clearAllCaches()
     */
	public void clearAllCaches() {}

    /**
     * No-op implementation
     * @see CacheService#createCache(int, int)
     */
	public int createCache(int type, int size) { return 0; }

    /**
     * No-op implementation
     * @see CacheService#setCacheEntries(int, int)
     */
	public void setCacheEntries(int cacheID, int size) {}

    public void shutDown() {}
	
}
