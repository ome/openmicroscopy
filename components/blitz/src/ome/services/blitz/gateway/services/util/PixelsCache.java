/*
 * org.openmicroscopy.shoola.env.rnd.data.PixelsCache 
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
package ome.services.blitz.gateway.services.util;


//Java imports
import java.util.LinkedHashMap;
import java.util.Map;


//Third-party libraries

//Application-internal dependencies

/** 
 * Caches Plane2D data, within a given pixels set, that have been rendered.
 * <p>The number of entries in the cache at any given time is  
 * <code>maxEntries</code> at most, being <code>maxEntries</code> the greatest
 * integer such that <nobr>
 * <code>maxEntries</code>*<code>planeSize</code> &lt;= 
 * <code>cacheSize</code>
 * </nobr>.</p>
 * <p>If <code>maxEntries</code> is reached and an entry has to be added, we 
 * discard a previous entry to make room for the new one.
 * </p>
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
public class PixelsCache
{

	/** The size, in bytes, of the image cache. */
	private int 					cacheSize;
	
	 /** The size, in bytes, of 2D-plane. */
	private int						planeSize;
	
	/** 
	 * Maximum number of entries allowed in the cache.
	 * This is the greatest integer <code>M</code> such that 
	 * <nobr><code>
	 * M*{@link #planeSize} &lt;= {@link #cacheSize}</code></nobr>.
	 */
	private int						maxEntries;
	
	/**
     * Maps {@link Integer}s onto {@link Plane2D}s.
     * The <code>add</code> method identifies a {@link Integer} object whose 
     * describe the index of the plane.
     */
	private Map<Integer, Plane2D>	cache;
	
	/**
     * Makes enough room in {@link #cache} for a new entry to be added.
     * We remove an existing entry according to the removal algorithm 
     * specified by this class.  It is assumed that this method will
     * only be invoked when the cache size equals {@link #maxEntries}
     * and is one at least.
     * 
     * @param p The key for the new entry that has to be added.
     *          It's assumed the caller will never pass <code>null</code>.
     */
    private void ensureCapacity(final Integer p)
    {
        //First off, build the C' sequence.
        Integer[] orderedCache = cache.keySet().toArray(new Integer[0]);
        
        //Now get the current navigation direction and set the default
        //candidate for removal: the farthest point away from p. 
        Integer candidate = orderedCache[0];  //We assume cache size > 0.
        
        //Finally remove.
        cache.remove(candidate);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param cacheSize The size, in bytes, of the cache. Must be positive.
     * @param planeSize The size, in bytes, of an image. Must be positive.
     */
	public PixelsCache(int cacheSize, int planeSize)
	{
		if (cacheSize <= 0)
            throw new IllegalArgumentException(
                    "Cache size must be positive: "+cacheSize+".");
        if (planeSize <= 0) 
            throw new IllegalArgumentException(
                    "Image size must be positive: "+planeSize+".");
        this.cacheSize = cacheSize;
        this.planeSize = planeSize;
        maxEntries = cacheSize/planeSize;
        cache = new LinkedHashMap<Integer, Plane2D>(maxEntries);
	}
	
	/**
	 * Adds the specified entry to the cache.
	 * 
	 * @param planeIndex	The index of the plane. 
	 * 						Mustn't be <code>null</code>.
	 * @param data			The entry to add. Mustn't be <code>null</code>.
	 */
    void add(Integer planeIndex, Plane2D data)
    {
        if (maxEntries == 0) return;  //Caching disabled.
      
        if (planeIndex == null || planeIndex.intValue() < 0)
        	throw new IllegalArgumentException("Plane index not valid.");
        if (data == null)
            throw new NullPointerException("No data.");
        
        //Will the next entry fit into the cache?
        if (maxEntries <= cache.size())  //Nope, make room for it.
            ensureCapacity(planeIndex);
        
        //Once we're here we have enough room for the new element.
        cache.put(planeIndex, data);
    }
    
    /**
     * Extracts the data (if any) associated to <code>plane index</code>.
     * 
     * @param planeIndex	The key. Mustn't be <code>null</code> and must
     * 						define a plane.
     * @return See above.
     */
    Plane2D extract(Integer planeIndex)
    {
        if (planeIndex == null || planeIndex.intValue() < 0)
            throw new IllegalArgumentException("Plane index not valid.");
        
        return cache.get(planeIndex);
    }
    
    /**
     * Tells whether or not the cache contains an entry for the specified
     * plane definition.
     * 
     * @param planeIndex The key. Mustn't be <code>null</code> and must
     * 					 define a plane.
     * @return <code>true</code> if the cache contains an entry for 
     *         <code>planeIndex</code>, <code>false</code> otherwise.
     * @see #add(planeIndex, Plane2D)
     */
    boolean contains(Integer planeIndex)
    {
    	if (planeIndex == null || planeIndex.intValue() < 0) return false;
        return (cache.get(planeIndex) != null);
    }
    
    /** Removes all the entries from the cache. */
    void clear()
    {
        int oldSize = cache.size();
        cache = new LinkedHashMap<Integer, Plane2D>(oldSize);
    }
 
    /**
     * Resets the size of the cache.
     * 
     * @param size
     */
    public void resetCacheSize(int size)
    {
        if (size < 0)
            throw new IllegalArgumentException(
                    "Cache size must be positive: "+size+".");
        cacheSize = size;
        maxEntries = cacheSize/planeSize;
        cache = new LinkedHashMap<Integer, Plane2D>(maxEntries);
    }
    
}
