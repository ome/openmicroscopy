/*
 * org.openmicroscopy.shoola.env.rnd.CachingService 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.rnd;




//Java imports
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.rnd.data.PixelsCache;

/** 
* Initializes and keeps track of all caches.
*
* @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
* <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
* @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
* <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
* @version 3.0
* <small>
* (<b>Internal version:</b> $Revision: $Date: $)
* </small>
* @since OME3.0
*/
public class CachingService
{
	
	/** The sole instance. */
	private static CachingService	singleton;

	/** Reference to the container registry. */
	private static Registry			registry;

	/** The maximum amount of memory in bytes used for caching. */
	private static int				maxSize;

	/**
	 * Creates a new instance. This can't be called outside of container b/c 
	 * agents have no refs to the singleton container. So we can be sure this
	 * method is going to create services just once.
	 * 
	 * @param c Reference to the container. 
	 * @return The sole instance.
	 * @throws NullPointerException If the reference to the {@link Container}
	 *                              is <code>null</code>.
	 */
	public static CachingService getInstance(Container c)
	{
		if (c == null)
			throw new NullPointerException();  //An agent called this method?
		if (singleton == null) {
			registry = c.getRegistry();
			singleton = new CachingService();
		}
		return singleton;
	}

	/**
	 * Creates a {@link XYCache}.
	 * 
	 * @param pixelsID	The id of the pixels set.	
	 * @param imageSize	The size of the rendered image to cache.
	 * @param sizeZ		The number of z-section.
	 * @param sizeT		The number of timepoints.
	 * @return See above.
	 */
	public static XYCache createXYCache(long pixelsID, int imageSize, int sizeZ,
			int sizeT)
	{
		//Shouldn't happen
		XYCache cache = singleton.imageCache.get(pixelsID);
		if (cache != null) return cache;
		int cacheSize = getCacheSize();
		if (cacheSize <= 0) return null;
		NavigationHistory nh = new NavigationHistory(cacheSize/imageSize, 
				sizeZ, sizeT);
		cache = new XYCache(cacheSize, imageSize, nh);
		singleton.imageCache.put(pixelsID, cache);
		return cache;
	}

	/**
	 * Creates a {@link PixelsCache}.
	 * 
	 * @param pixelsID	The id of the pixels set.
	 * @param planeSize	The size of the plane to cache.
	 * @return See above.
	 */
	public static PixelsCache createPixelsCache(long pixelsID, int planeSize)
	{
		//Shouldn't happen
		PixelsCache cache = singleton.pixelsCache.get(pixelsID);
		if (cache != null) return cache;
		int cacheSize = getCacheSize();
		if (cacheSize <= 0) return null;
		//Initializes a new cache and records it.
		cache = new PixelsCache(cacheSize, planeSize);
		singleton.pixelsCache.put(pixelsID, cache);
		return cache;
	}

	/**
	 * Returns the size of the cache.
	 * 
	 * @return See above.
	 */
	private static int getCacheSize()
	{
		int n = singleton.pixelsCache.size();
		int m = singleton.imageCache.size();
		int sizeCache = 0;
		if (n == 0 && m == 0) return maxSize*1024*1024;
		else if (n == 0 && m > 0) {
			sizeCache = (maxSize/m)*1024*1024;
			//reset all the image caches.
			Iterator i = singleton.imageCache.keySet().iterator();
			XYCache cache;
			while (i.hasNext()) {
				cache = singleton.imageCache.get(i.next());
				cache.resetCacheSize(sizeCache);
			}
			return sizeCache;
		} else if (m == 0 && n > 0) {
			sizeCache = (maxSize/n)*1024*1024;
			//reset all the image caches.
			Iterator i = singleton.pixelsCache.keySet().iterator();
			PixelsCache cache;
			while (i.hasNext()) {
				cache = singleton.pixelsCache.get(i.next());
				cache.resetCacheSize(sizeCache);
			}
			return sizeCache;
		}
		sizeCache = (maxSize/(m+n))*1024*1024;
		//reset all the image caches.
		Iterator i = singleton.pixelsCache.keySet().iterator();
		PixelsCache cache;
		while (i.hasNext()) {
			cache = singleton.pixelsCache.get(i.next());
			cache.resetCacheSize(sizeCache);
		}
		i = singleton.imageCache.keySet().iterator();
		XYCache xyCache;
		while (i.hasNext()) {
			xyCache = singleton.imageCache.get(i.next());
			xyCache.resetCacheSize(sizeCache);
		}
		return sizeCache;
	}

	/**
	 * Collection of pixels cache whose keys are the pixels IDs and the 
	 * values are the corresponding {@link PixelsCache}.
	 */
	private Map<Long, PixelsCache>	pixelsCache;

	/**
	 * Collection of image cache whose keys are the pixels IDs and the 
	 * values are the corresponding {@link XYCache}.
	 */
	private Map<Long, XYCache>		imageCache;

	/** Creates the sole instance. */
	private CachingService()
	{
		//Retrieve the maximum heap size.
		MemoryUsage usage = 
			ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
		String message = "Heap memory usage: max "+usage.getMax();

		//System.err.println(usage);
		registry.getLogger().info(this, message);
		//percentage of memory used for caching.
		maxSize = (int) (0.6*usage.getMax())/(1024*1024); 
		pixelsCache = new HashMap<Long, PixelsCache>();
		imageCache = new HashMap<Long, XYCache>();
	}

}
