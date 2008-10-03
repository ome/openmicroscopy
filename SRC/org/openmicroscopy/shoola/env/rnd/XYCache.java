 /*
 * org.openmicroscopy.shoola.env.rnd.XYCache
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


//Third-party libraries

//Application-internal dependencies
import omero.romio.PlaneDef;
import org.openmicroscopy.shoola.util.math.geom2D.Line;
import org.openmicroscopy.shoola.util.math.geom2D.PlanePoint;


/** 
 * Caches XY images, within a given pixels set, that have been rendered.
 * <p>The number of entries in the cache at any given time is  
 * <code>max_entries</code> at most, being <code>max_entries</code> the greatest
 * integer such that <nobr>
 * <code>max_entries</code>*<code>image_size</code> &lt;= 
 * <code>cache_size</code>
 * </nobr>.</p>
 * <p>If <code>max_entries</code> is reached and an entry has to be added, we 
 * discard a previous entry to make room for the new one.  The removal policy
 * is based on the current navigation direction maintained by the 
 * {@link NavigationHistory} and is as follows.  Let <code>C</code> be the set
 * of all entries in the cache and be <code>n</code> its cardinality.  It's a
 * trivial observation that we can identify an element of <code>C</code> with
 * a point in the <i>zOt</i> cartesian plane. Now if a point <code>p</code> is
 * to be added to <code>C</code> and <code>n=max_entries</code>, we consider
 * the set <code>C'</code> of all elements of <code>C</code> ordered such that
 * the first element is the farthest away from <code>p</code> and the last 
 * element is the closest to <code>p</code>.  That is:</p>
 * <p><code>
 * C' = {c<sub>1</sub>, .. , c<sub>n</sub>} <br>
 * d(c<sub>i</sub>, p) &gt;= d(c<sub>i+i</sub>, p) </code><br>
 * <small>(d being the standard distance between two points in the cartesian 
 * plane)</small>
 * </p>
 * <p>Then the removal algorithm is given by the following steps:</p>
 * <ol>
 *  <li>Get the line <code>D</code> representing the current navigation 
 *  direction.</li>
 *  <li>Look for the first element of <code>C'</code> that doesn't lie on
 *  <code>D</code>.  If such an element exists, then remove it.  Otherwise
 *  go on to the next step.</li>
 *  <li>Look for the first element of <code>C'</code> that falls on the 
 *  negative half of <code>D</code> &#151; this half contains the points 
 *  "behind" the current move with respect to the orientation of the
 *  movement. If such an element exists, then remove it.  Otherwise go on
 *  to the next step.</li>
 *  <li>Remove <code>c<sub>1</sub></code>.</li>
 * </ol>
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 			<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class XYCache
{

    /** The size, in bytes, of a rendered XY image. */
    private final int           			image_size;
    
    /** 
     * The size, in bytes, of the image cache.
     * Rendered images will be cached until we reach this value.  Subsequent
     * requests to cache an image will result in the removal of some already
     * cached images.
     */
    private int                 			cache_size;
    
    /** 
     * Maximum number of entries allowed in the cache.
     * This is the greatest integer <code>M</code> such that 
     * <nobr><code>
     * M*{@link #image_size} &lt;= {@link #cache_size}</code></nobr>.
     */
    private int                 			max_entries;
    
    /**
     * Maps {@link PlanePoint}s onto {@link BufferedImage}s or 
     * <code>byte</code> array.
     * The <code>add</code> method identifies a {@link PlaneDef} object with
     * a point in the <i>zOt</i> cartesian plane, hence with an instance of
     * {@link PlanePoint}. 
     */
    private Map<PlanePoint, Object>			cache;
    
    /**
     * Refers to the {@link NavigationHistory} serving the pixels set this 
     * cache was associated to.
     */
    private NavigationHistory   			navigHistory;
    
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
        PlanePoint[] orderedCache = cache.keySet().toArray(new PlanePoint[0]);
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
        cache = new HashMap<PlanePoint, Object>(max_entries);
        navigHistory = nh;
    }
    
    /**
     * Adds the specified entry to the cache.
     * 
     * @param pd    	The key. Mustn't be <code>null</code> and must define
     *              	an XY plane.
     * @param object  	An XY image or a byte array.
     * 					Mustn't be <code>null</code>.
     */
    void add(PlaneDef pd, Object object)
    {
        if (max_entries == 0) return;  //Caching disabled.
        
        //Sanity checks.
        if (pd == null)
            throw new NullPointerException("No plane def.");
        if (pd.slice != omero.romio.XY.value)
            throw new IllegalArgumentException(
                    "Can only accept XY planes: "+pd.slice+".");
        if (object == null)
            throw new NullPointerException("No image.");
        
        //Will the next entry fit into the cache?
        PlanePoint key = new PlanePoint(pd.z, pd.t);
        if (max_entries <= cache.size())  //Nope, make room for it.
            ensureCapacity(key);
        //Once we're here we have enough room for the new element.
        cache.put(key, object);
    }
   
    /**
     * Extracts the image (if any) associated to <code>pd</code>.
     * 
     * @param pd    The key. Mustn't be <code>null</code> and must define
     *              an XY plane.
     * @return 		The image or byte array associated to <code>pd</code> or 
     * 				<code>null</code> if the cache doesn't contain such an
     * 				entry.
     */
    Object extract(PlaneDef pd)
    {
        if (pd == null)
            throw new NullPointerException("No plane def.");
        if (pd.slice != omero.romio.XY.value)
            throw new IllegalArgumentException(
                    "Can only accept XY planes: "+pd.slice+".");
        PlanePoint key = new PlanePoint(pd.z, pd.t);
        return cache.get(key);
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
        PlanePoint key = new PlanePoint(pd.z, pd.t);
        return (cache.get(key) != null);
    }
    
    /** Removes all the entries from the cache. */
    void clear()
    {
        int oldSize = cache.size();
        cache = new HashMap<PlanePoint, Object>(oldSize);
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
        cache = new HashMap<PlanePoint, Object>(max_entries);
    }
    
/* 
 * ==============================================================
 *              Follows code to enable testing.
 * ==============================================================
 */ 
    
    /**
     * Returns the cache.
     * 
     * @return See above.
     */
    Map getCache() { return cache; }
    
    /**
     * Returns the navigation history.
     * 
     * @return See above.
     */
    NavigationHistory getNavigHistory() { return navigHistory; }
    
}
