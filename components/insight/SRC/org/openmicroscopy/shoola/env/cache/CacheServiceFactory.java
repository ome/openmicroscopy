/*
 * org.openmicroscopy.shoola.env.cache.CacheServiceFactory 
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

//Application-internal dependencies
import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.util.file.IOUtil;
import omero.gateway.cache.CacheService;

/** 
 * A factory for the {@link CacheService}. 
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
public class CacheServiceFactory
{

	/** The name of the cache configuration file in the config directory. */
	public static final String CACHE_CONFIG_FILE = "ehcache.xml";
	
	/**
	 * Creates a new {@link CacheService}.
	 * 
     * @param container Reference to the container.
	 * @return See above.
	 */
	public static CacheService makeNew(Container container)
	{
		//NB: this can't be called outside of container b/c agents have no refs
		//to the singleton container. So we can be sure this method is going to
		//create services just once.
		if (container == null)	return null;
		//If caching is off, then we return the no-op adapter.
		Registry reg = container.getRegistry();
		Boolean isCachingOn = (Boolean) reg.lookup(LookupNames.CACHE_ON);
		if (!isCachingOn.booleanValue()) return makeNoOpCache();
		
		//Ok we have to cache, so try and read the config file.
		InputStream config = loadConfig(
			container.getConfigFileRelative(CACHE_CONFIG_FILE));
		if (config == null)	return makeNoOpCache();
		
		//We have a config file, set up ehcache.
		CacheService cache = new CacheServiceImpl(config,
				container.getRegistry().getLogger());
		try {
			config.close();
		} catch (Exception e) {}
		return cache;
	}
	
	/**
	 * Shuts downs the caching service.
	 * 
	 * @param container Reference to the container.
	 */
	public static void shutdown(Container container)
	{
		//NB: this can't be called outside of container b/c agents have no refs
		//to the singleton container. So we can be sure this method is going to
		//create services just once.
		if (container == null)	return;
		Registry reg = container.getRegistry();
		((CacheServiceImpl) reg.getCacheService()).shutDown();
	}
	
	/**
	 * Creates a no-operation implementation of {@link CacheService}.
	 * 
	 * @return See above.
	 */
	private static CacheService makeNoOpCache()
	{
		return new CacheService() {
			public void addElement(int cacheID, Object key, Object element) {}
			public int createCache() { return -1; }
			public int createCache(int type) { return -1; }
			public int createCache(int type, int size) { return -1; }
			public Object getElement(int cacheID, Object key) { return null; }
			public void removeCache(int cacheID) {}
			public void clearCache(int cacheID) {}
			public void setCacheEntries(int cacheID, int size) {}
			public void clearAllCaches() {}
            public void shutDown() {}
		};
	}
	
	/**
	 * Reads in the specified file as a property object.
	 * 
	 * @param file	Absolute pathname to the file.
	 * @return	The content of the file as a property object or
	 * 			<code>null</code> if an error occurred.
	 */
	private static InputStream loadConfig(String file)
	{
		InputStream stream;
		try { 
			stream = IOUtil.readConfigFile(file);
		} catch (Exception e) {
			return null;
		}
		return stream;
	}
	
}
