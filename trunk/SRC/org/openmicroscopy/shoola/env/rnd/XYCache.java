/*
 * org.openmicroscopy.shoola.env.rnd.XYCache
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.env.rnd;




//Java imports
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import omeis.providers.re.RGBBuffer;
import omeis.providers.re.data.PlaneDef;

import org.openmicroscopy.shoola.util.math.geom2D.Line;
import org.openmicroscopy.shoola.util.math.geom2D.PlanePoint;


//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class XYCache
{

    /** The size, in bytes, of a rendered XY image. */
    private final int           image_size;
    
    /** 
     * The size, in bytes, of the image cache.
     * Rendered images will be cached until we reach this value.  Subsequent
     * requests to cache an image will result in the removal of some already
     * cached images.
     */
    private int                 cache_size;
    /** 
     * Maximum number of entries allowed in the cache.
     * This is the greatest integer <code>M</code> such that 
     * <nobr><code>
     * M*{@link #image_size} &lt;= {@link #cache_size}</code></nobr>.
     */
    private int                 max_entries;
    
    /**
     * Maps {@link PlanePoint}s onto {@link BufferedImage}s.
     * The <code>add</code> method identifies a {@link PlaneDef} object with
     * a point in the <i>zOt</i> cartesian plane, hence with an instance of
     * {@link PlanePoint}. 
     */
    private Map                 cache;
    
    /**
     * Refers to the {@link NavigationHistory} serving the pixels set this 
     * cache was associated to.
     */
    private NavigationHistory   navigHistory;
    
    
    /**
     * Makes enough room in {@link #cache} for a new entry to be added.
     * We remove an existing entry according to the removal algorithm 
     * specified by this class.  It is assumed that this method will
     * only be invoked when the cache size equals {@link #max_entries}
     * and is one at least.
     * 
     * @param p The key for the new entry that has to be added.
     *          It's assumed the caller will never pass <code>null</code>.
     */
    private void ensureCapacity(final PlanePoint p)
    {
        //First off, build the C' sequence.
        PlanePoint[] orderedCache = (PlanePoint[]) 
                                    cache.keySet().toArray(new PlanePoint[0]);
        Arrays.sort(orderedCache, new Comparator() {
            public int compare(Object o1, Object o2) {
                PlanePoint c1 = (PlanePoint) o1, c2 = (PlanePoint) o2;
                return -Double.compare(c1.distance(p), c2.distance(p));
                //Note the minus above: we want descending order.
            }
        });
        
        //Now get the current navigation direction and set the default
        //candidate for removal: the farthest point away from p. 
        Line curDir = navigHistory.currentDirection();
        PlanePoint candidate = orderedCache[0];  //We assume cache size > 0.
        
        //Start the removal algorithm if the navigation direction is defined.
        if (curDir != null) {
            List negativeHalf = new ArrayList(orderedCache.length);
            int i = 0;
            for (; i < orderedCache.length; ++i) {
                //Does candidate lie on curDir at all?
                if (!curDir.lies(orderedCache[i])) { 
                    //No.  This is the farthest point away from p not on curDir.
                    candidate = orderedCache[i];
                    break;
                }
                //Then orderedCache[i] lies on curDir, which half though?
                if (curDir.lies(orderedCache[i], false))
                    //It lies on the negative half.  This means it sits behind
                    //the current move w/r/t movement orientation.  Collect it.
                    //Notice that points in this list are such that:
                    //  d(negativeHalf(i), p) >= d(negativeHalf(i+1), p) 
                    negativeHalf.add(orderedCache[i]);
            }
            if (i == orderedCache.length  //All cached points lie on curDir. 
                    && !negativeHalf.isEmpty())  //But some in neg half.
                //Get farthest point away from p that sits behind current move.
                candidate = (PlanePoint) negativeHalf.get(0);
        }
        
        //Finally remove.
        cache.remove(candidate);
    }
    
    /**
     * Creates a new instance.
     * An <code>ImageCache</code> works with a given pixels set and
     * with the {@link NavigationHistory} serving that pixels set.  The
     * <code>cacheSize</code> and <code>imageSize</code> parameters determine
     * how many {@link #max_entries entries} the cache will allow before
     * purging old entries.  In particular, if the <code>imageSize</code> is
     * greater than the <code>cacheSize</code>, no element will ever be
     * cached.
     * 
     * @param cacheSize The size, in bytes, of the cache. Must be positive.
     * @param imageSize The size, in bytes, of an XY image. Must be positive.
     * @param nh        Reference to the {@link NavigationHistory} serving 
     *                  the pixels set this cache was associated to.  
     *                  Mustn't be <code>null</code>.  
     */
    XYCache(int cacheSize, int imageSize, NavigationHistory nh)
    {
        if (cacheSize < 0)
            throw new IllegalArgumentException(
                    "Cache size must be positive: "+cacheSize+".");
        if (imageSize <= 0) 
            throw new IllegalArgumentException(
                    "Image size must be positive: "+imageSize+".");
        if (nh == null)
            throw new NullPointerException("No navigation history.");
        cache_size = cacheSize;
        image_size = imageSize;
        max_entries = cache_size/image_size;
        cache = new HashMap(max_entries);
        navigHistory = nh;
    }
    
    /**
     * Adds the specified entry to the cache.
     * 
     * @param pd    The key. Mustn't be <code>null</code> and must define
     *              an XY plane.
     * @param img   An XY image. Mustn't be <code>null</code>.
     */
    void add(PlaneDef pd, BufferedImage img)
    {
        if (max_entries == 0) return;  //Caching disabled.
        
        //Sanity checks.
        if (pd == null)
            throw new NullPointerException("No plane def.");
        if (pd.getSlice() != PlaneDef.XY)
            throw new IllegalArgumentException(
                    "Can only accept XY planes: "+pd.getSlice()+".");
        if (img == null)
            throw new NullPointerException("No image.");
        
        //Will the next entry fit into the cache?
        PlanePoint key = new PlanePoint(pd.getZ(), pd.getT());
        if (max_entries <= cache.size())  //Nope, make room for it.
            ensureCapacity(key);
        
        //Once we're here we have enough room for the new element.
        cache.put(key, img);
    }
    
    //tmp
    void addBuffer(PlaneDef pd, RGBBuffer buffer)
    {
        if (max_entries == 0) return;  //Caching disabled.
        
        //Sanity checks.
        if (pd == null)
            throw new NullPointerException("No plane def.");
        if (pd.getSlice() != PlaneDef.XY)
            throw new IllegalArgumentException(
                    "Can only accept XY planes: "+pd.getSlice()+".");
        if (buffer == null)
            throw new NullPointerException("No buffer.");
        
        //Will the next entry fit into the cache?
        PlanePoint key = new PlanePoint(pd.getZ(), pd.getT());
        if (max_entries <= cache.size())  //Nope, make room for it.
            ensureCapacity(key);
        
        //Once we're here we have enough room for the new element.
        cache.put(key, buffer);
    }
    
    /**
     * Extracts the image (if any) associated to <code>pd</code>.
     * 
     * @param pd    The key. Mustn't be <code>null</code> and must define
     *              an XY plane.
     * @return The image associated to <code>pd</code> or <code>null</code>
     *         if the cache doesn't contain such an entry.
     */
    BufferedImage extract(PlaneDef pd)
    {
        if (pd == null)
            throw new NullPointerException("No plane def.");
        if (pd.getSlice() != PlaneDef.XY)
            throw new IllegalArgumentException(
                    "Can only accept XY planes: "+pd.getSlice()+".");
        PlanePoint key = new PlanePoint(pd.getZ(), pd.getT());
        return (BufferedImage) cache.get(key);
    }
    
    //tmp method
    RGBBuffer extractBuffer(PlaneDef pd)
    {
        if (pd == null)
            throw new NullPointerException("No plane def.");
        if (pd.getSlice() != PlaneDef.XY)
            throw new IllegalArgumentException(
                    "Can only accept XY planes: "+pd.getSlice()+".");
        PlanePoint key = new PlanePoint(pd.getZ(), pd.getT());
        return (RGBBuffer) cache.get(key);
    }
    
    /**
     * Tells whether or not the cache contains an entry for the specified
     * plane definition.
     * 
     * @param pd The key.
     * @return <code>true</code> if the cache contains an entry for 
     *         <code>pd</code>, <code>false</code> otherwise.
     * @see #add(PlaneDef, BufferedImage)
     */
    boolean contains(PlaneDef pd)
    {
        if (pd == null) return false;
        PlanePoint key = new PlanePoint(pd.getZ(), pd.getT());
        return (cache.get(key) != null);
    }
    
    /** Removes all the entries from the cache. */
    void clear()
    {
        int oldSize = cache.size();
        cache = new HashMap(oldSize);
    }
    
    /**
     * Resets the size of the cache.
     * 
     * @param size
     */
    void resetCacheSize(int size)
    {
        if (size < 0)
            throw new IllegalArgumentException(
                    "Cache size must be positive: "+size+".");
        cache_size = size;
        max_entries = cache_size/image_size;
        cache = new HashMap(max_entries);
    }
    
/* 
 * ==============================================================
 *              Follows code to enable testing.
 * ==============================================================
 */ 
    
    Map getCache() { return cache; }
    NavigationHistory getNavigHistory() { return navigHistory; }
    
}
