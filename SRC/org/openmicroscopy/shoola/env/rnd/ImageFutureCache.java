/*
 * org.openmicroscopy.shoola.env.rnd.ImageFutureCache
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

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.rnd.data.DataSourceException;
import org.openmicroscopy.shoola.env.rnd.defs.PlaneDef;
import org.openmicroscopy.shoola.env.rnd.quantum.QuantizationException;
import org.openmicroscopy.shoola.util.concur.tasks.Future;
import org.openmicroscopy.shoola.util.math.geom2D.Line;
import org.openmicroscopy.shoola.util.math.geom2D.Point;

/** 
 * Caches XY images, within a given pixels set, that have been rendered or
 * handles to (said) images that are being/have been rendered asynchronously.
 * <p>Because of this, the <code>add</code> method comes in two flavors:
 * {@link #add(PlaneDef, BufferedImage) one} to enter a {@link BufferedImage}
 * directly into the cache, and {@link #add(PlaneDef, Future) another} one to
 * enter a {@link Future} to an asynchronous invocation of the 
 * {@link Renderer#render() render} method on the {@link Renderer} object that
 * is serving the pixels set this cache was associated to.</p>
 * <p>When an image is {@link #extract(PlaneDef) extracted} from the cache, we
 * first check to see whether the cached item is a {@link Future} object.  In
 * that case, we attempt to retrieve the actual image the {@link Future} is 
 * representing, possibly blocking until the image has been rendered.  If the
 * {@link Future} returns a {@link BufferedImage}, then we cache it and discard
 * the {@link Future}.  Otheriwise, if the image couldn't be rendered, we just
 * throw the cause of the error and discard the cache entry altogether.</p>
 * <p>The number of entries in the cache at any given time is  
 * {@link #MAX_ENTRIES} at most, being {@link #MAX_ENTRIES} the greatest integer
 * such that <nobr>
 * {@link #MAX_ENTRIES}*{@link #IMAGE_SIZE} &lt;= {@link #CACHE_SIZE}</nobr>.
 * Note that, in general, if <code>n</code> is the current number of entries, 
 * then the amount of bytes in memory could be less than 
 * <nobr><code>n*IMAGE_SIZE</code></nobr> &#151; in fact, the cache might 
 * contain handles to images that have not been rendered yet.</p>
 * <p>If {@link #MAX_ENTRIES} is reached and an entry has to be added, we 
 * discard a previous entry to make room for the new one.  The removal policy
 * is based on the current navigation direction maintained by the 
 * {@link NavigationHistory} and is as follows.  Let <code>C</code> be the set
 * of all entries in the cache and be <code>n</code> its cardinality.  It's a
 * trivial observation that we can identify an element of <code>C</code> with
 * a point in the <i>zOt</i> cartesian plane.  Now if a point <code>p</code> is
 * to be added to <code>C</code> and <code>n=MAX_ENTRIES</code>, we consider
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
 *  movement.  If such an element exists, then remove it.  Otherwise go on
 *  to the next step.</li>
 *  <li>Remove <code>c<sub>1</sub></code>.</li>
 * </ol>
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class ImageFutureCache
{

    /** 
     * The size, in bytes, of the image cache.
     * Rendered images will be cached until we reach this value.  Subsequent
     * requests to cache an image will result in the removal of some already
     * cached images.
     */
    final int   CACHE_SIZE;
    
    /** The size, in bytes, of a rendered XY image. */
    final int   IMAGE_SIZE;
    
    /** 
     * Maximum number of entries allowed in the cache.
     * This is the greatest integer <code>M</code> such that 
     * <nobr><code>
     * M*{@link #IMAGE_SIZE} &lt;= {@link #CACHE_SIZE}</code></nobr>.
     */
    final int   MAX_ENTRIES;
    
    /**
     * Maps {@link Point}s onto {@link BufferedImage}s or {@link Future}s.
     * The <code>add</code> methods identify a {@link PlaneDef} object with
     * a point in the <i>zOt</i> cartesian plane, hence with an instance of
     * {@link Point}. 
     */
    private Map                 cache;
    
    /**
     * Refers to the {@link NavigationHistory} serving the pixels set this 
     * cache was associated to.
     */
    private NavigationHistory   navigHistory;
    
    
    /**
     * Removes the cache entry (if any) associated to <code>key</code>.
     * If the entry is a {@link Future}, then we also cancel execution to
     * tell the associated service its result is no longer needed &#151; 
     * this is useful in the case the asynchronous rendering is still 
     * going on.
     * 
     * @param key   Identifies the entry.  It's assumed the caller will never
     *              pass <code>null</code>.
     */
    private void removeEntry(Point key)
    {
        Object entry = cache.remove(key);
        if (entry != null && entry instanceof Future) {
            Future f = (Future) entry;
            f.cancelExecution();
        }
    }
    
    /**
     * Makes enough room in {@link #cache} for a new entry to be added.
     * We remove an existing entry according to the removal algorithm 
     * specified by this class.  It is assumed that this method will
     * only be invoked when the cache size equals {@link #MAX_ENTRIES}
     * and is one at least.
     * 
     * @param p The key to the new entry that has to be added.
     *          It's assumed the caller will never pass <code>null</code>.
     */
    private void ensureCapacity(final Point p)
    {
        //First off, build the C' sequence.
        Point[] orderedCache = (Point[]) cache.keySet().toArray(new Point[0]);
        Arrays.sort(orderedCache, new Comparator() {
            public int compare(Object o1, Object o2) {
                Point c1 = (Point) o1, c2 = (Point) o2;
                return -Double.compare(c1.distance(p), c2.distance(p));
                //Note the minus above: we want descending order.
            }
        });
        
        //Now get the current navigation direction and set the default
        //candidate for removal: the farthest point away from p. 
        Line curDir = navigHistory.currentDirection();
        Point candidate = orderedCache[0];  //We assume cache size > 0.
        
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
                candidate = (Point) negativeHalf.get(0);
        }
        
        //Finally remove.
        removeEntry(candidate);
    }
    
    /**
     * Adds the specified entry to the cache.
     * 
     * @param p     The key.  Assumed to be not <code>null</code>.
     * @param img   Either an instance of {@link BufferedImage} or 
     *              {@link Future}.  Assumed to be not <code>null</code>.
     */
    private void addEntry(Point key, Object img)
    {
        if (MAX_ENTRIES == 0) return;  //Caching disabled.
        
        //Will the next entry fit into the cache?
        if (MAX_ENTRIES <= cache.size())  //Nope, make room for it.
            ensureCapacity(key);
        
        //Once we're here we have enough room for the new element.
        cache.put(key, img);
    }
    
    /**
     * Creates a new instance.
     * An <code>ImageFutureCache</code> works with a given pixels set and
     * with the {@link NavigationHistory} serving that pixels set.  The
     * <code>cacheSize</code> and <code>imageSize</code> parameters determine
     * how many {@link #MAX_ENTRIES entries} the cache will allow before
     * purging old entries.  In particular, if the <code>imageSize</code> is
     * greater than the <code>cacheSize</code>, no element will ever be
     * cached.
     * 
     * @param cacheSize The size, in bytes, of the cache.  Must be positive.
     * @param imageSize The size, in bytes, of an XY image.  Must be positive.
     * @param nh  Reference to the {@link NavigationHistory} serving the pixels
     *              set this cache was associated to.  
     *              Mustn't be <code>null</code>.  
     */
    ImageFutureCache(int cacheSize, int imageSize, NavigationHistory nh)
    {
        if (cacheSize <= 0)
            throw new IllegalArgumentException(
                    "Cache size must be positive: "+cacheSize+".");
        if (imageSize <= 0) 
            throw new IllegalArgumentException(
                    "Image size must be positive: "+imageSize+".");
        if (nh == null)
            throw new NullPointerException("No navigation history.");
        CACHE_SIZE = cacheSize;
        IMAGE_SIZE = imageSize;
        MAX_ENTRIES = CACHE_SIZE/IMAGE_SIZE;
        cache = new HashMap(MAX_ENTRIES);
        navigHistory = nh;
    }
    
    /**
     * Adds the specified entry to the cache.
     * 
     * @param p     The key.  Mustn't be <code>null</code> and must define
     *              an XY plane.
     * @param img   Handle to an XY image that is being/has been rendered 
     *              asynchronously.  Mustn't be <code>null</code>.
     */
    void add(PlaneDef pd, Future img)
    {
        if (pd == null)
            throw new NullPointerException("No plane def.");
        if (pd.getSlice() != PlaneDef.XY)
            throw new IllegalArgumentException(
                    "Can only accept XY planes: "+pd.getSlice()+".");
        if (img == null)
            throw new NullPointerException("No image future.");
        addEntry(new Point(pd.getZ(), pd.getT()), img);
    }
    
    /**
     * Adds the specified entry to the cache.
     * 
     * @param p     The key.  Mustn't be <code>null</code> and must define
     *              an XY plane.
     * @param img   An XY image.  Mustn't be <code>null</code>.
     */
    void add(PlaneDef pd, BufferedImage img)
    {
        if (pd == null)
            throw new NullPointerException("No plane def.");
        if (pd.getSlice() != PlaneDef.XY)
            throw new IllegalArgumentException(
                    "Can only accept XY planes: "+pd.getSlice()+".");
        if (img == null)
            throw new NullPointerException("No image.");
        addEntry(new Point(pd.getZ(), pd.getT()), img);
    }
    
    /**
     * Extracts the image (if any) associated to <code>pd</code>.
     * We first check to see whether the cached item is a {@link BufferedImage}.
     * In that case, we just return it.  If the item is a {@link Future} object,
     * we attempt to retrieve the actual image the {@link Future} is 
     * representing, possibly blocking until the image has been rendered.  
     * If the {@link Future} returns a {@link BufferedImage}, then we cache it
     * and discard the {@link Future}.  Otheriwise, if the image couldn't be 
     * rendered, we just throw the cause of the error and discard the cache 
     * entry altogether.
     * 
     * @param pd    The key.  Mustn't be <code>null</code>.
     * @return The image associated to <code>pd</code> or <code>null</code>
     *          if the cache doesn't contain such an entry.
     * @throws DataSourceException If an error occurred while fetching image 
     *                              data.
     * @throws QuantizationException If an error occurred while rendering.
     */
    BufferedImage extract(PlaneDef pd)
        throws DataSourceException, QuantizationException
    {
        if (pd == null)
            throw new NullPointerException("No plane def.");
        BufferedImage img = null;
        Point key = new Point(pd.getZ(), pd.getT());
        Object entry = cache.get(key);
        if (entry != null) {
            if (entry instanceof BufferedImage)
                img = (BufferedImage) entry;
            else  //We've got a Future.  Block until img rendered or error.
                try {
                    img = RenderingManager.extractXYImage((Future) entry);
                } finally {
                    removeEntry(key);  //Discard this entry in any case.
                    //If no error, then replace it with the actual image.
                    if (img != null) addEntry(key, img);
                }
        }
        return img;
    }
    
    /**
     * Tells whether or not the cache contains an entry for the specified
     * plane definition.
     * 
     * @param pd    The key.
     * @return  <code>true</code> if the cache contains a mapping from 
     *          <code>pd</code> into a {@link BufferedImage} or 
     *          {@link Future}, <code>false</code> otherwise.
     * @see #add(PlaneDef, BufferedImage)
     * @see #add(PlaneDef, Future)
     */
    boolean contains(PlaneDef pd)
    {
        if (pd == null) return false;
        Point key = new Point(pd.getZ(), pd.getT());
        return (cache.get(key) != null);
    }
    
    /**
     * Removes all entries from the cache.
     */
    void clear()
    {
        int oldSize = cache.size();
        Point[] keys = (Point[]) cache.keySet().toArray(new Point[0]);
        for (int i = 0; i < keys.length; ++i) removeEntry(keys[i]);
        cache = new HashMap(oldSize);
    }
    
    
/* 
 * ==============================================================
 *              Follows code to enable testing.
 * ==============================================================
 */ 
    
    Map getCache() { return cache; }
    NavigationHistory getNavigHistory() { return navigHistory; }
    
}
