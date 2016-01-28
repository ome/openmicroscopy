/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.dataBrowser;

import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.Thumbnail;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.WellImageSet;

import omero.gateway.model.ExperimenterData;
import omero.gateway.model.ImageData;

/** 
 * Manages the process of assigning thumbnails to {@link ImageNode}s in a
 * visualization tree.
 * This class encapsulates the strategy used to share thumbnail pixels.
 * A given <i>OME</i> Image can be contained in more than one hierarchy
 * container &#151; like a Dataset, in which case it would
 * end up being contained in more than one {@link ImageNode} in the
 * visualization tree &#151; this is because {@link ImageNode}s can't be
 * shared among <code>ImageSet</code>s.  Even if we can have more than one
 * {@link ImageNode} to represent the same Image in a visualization tree,
 * usually just one thumbnail is required to display all the nodes.  This
 * class enforces the sharing strategy. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class ThumbnailsManager
{
	
	/** How many different Images we have. */
    private int     			totalIDs;
    
    /** Ids of the images whose thumbnails have already been set. */
    private Set<Object>     	processedIDs;
    
    /** 
     * Maps an Image id onto all the {@link ThumbnailProvider}s in the
     * visualization tree that have to provide a thumbnail for that Image.
     * Note that {@link ThumbnailProvider}s are not shared, so there's one
     * for each {@link ImageNode} that represents the given Image. 
     */
    private Map<Object, Set>     thumbProviders;
    
    /**
     * Creates a new instance.
     * 
     * @param nodes 	All the {@link ImageDisplay}s in a given visualization
     *                  tree.  Mustn't be <code>null</code>.
     * @param totalIDs	The total number of images to load.
     */
    public ThumbnailsManager(Collection nodes, int totalIDs)
    {
        if (nodes == null) 
            throw new NullPointerException("No image nodes.");
        //totalIDs = 0;
        this.totalIDs = totalIDs;
        processedIDs = new HashSet<Object>();
        thumbProviders = new HashMap<Object, Set>();
        Iterator<ImageDisplay> i = nodes.iterator();
        ImageDisplay node;
        ImageData is = null;
        Long id;
        Set<Thumbnail> providers;
        Thumbnail thumb = null;
        Object ho = null;
        while (i.hasNext()) {
            node = i.next();
            if (node instanceof WellImageSet) {
        		is = ((WellImageSet) node).getSelectedImage();
        		thumb = 
        			((WellImageSet) node).getSelectedWellSample().getThumbnail();
            } else if (node instanceof ImageNode) {
            	ho = node.getHierarchyObject();
            	if (ho instanceof ImageData) {
            		is = (ImageData) ho;
            		if (is.getId() < 0) is = null;
            	}
            		
            	thumb = ((ImageNode) node).getThumbnail();
            } 
            if (is != null) {
            	 id = is.getId();
                 providers = thumbProviders.get(id);
                 if (providers == null) {
                     providers = new HashSet<Thumbnail>();
                     thumbProviders.put(id, providers);
                 }
                 providers.add(thumb);
            } else {
            	if (ho instanceof ImageData) {
            		providers = thumbProviders.get(ho);
                    if (providers == null) {
                        providers = new HashSet<Thumbnail>();
                        thumbProviders.put(ho, providers);
                    }
                    providers.add(thumb);
            	} else if (ho instanceof ExperimenterData) {
            		ExperimenterData exp = (ExperimenterData) ho;
            		providers = thumbProviders.get(exp.getId());
                    if (providers == null) {
                        providers = new HashSet<Thumbnail>();
                        thumbProviders.put(exp.getId(), providers);
                    }
                    providers.add(thumb);
            	}
            }
        }
    }
    
    /**
     * Sets the specified pixels to be the thumbnail for the specified Image.
     * 
     * @param ref The id of the image or to the object of reference
     * 				  which the thumbnail belongs.
     * @param thumb   The thumbnail pixels. Mustn't be <code>null</code>.
     * @param valid   Pass <code>true</code> if it is a valid thumbnail,
     * 					 <code>false</code> otherwise.
     */
    public void setThumbnail(Object ref, BufferedImage thumb, boolean valid)
    {
        if (thumb == null) throw new NullPointerException("No thumbnail.");
        if (ref instanceof Long || ref instanceof ImageData) {
             Set providers = thumbProviders.get(ref);
             if (providers != null) {
                 Iterator p = providers.iterator();
                 ThumbnailProvider tp;
                 while (p.hasNext()) {
                 	tp = (ThumbnailProvider) p.next();
                 	tp.setValid(valid);
                 	tp.setFullScaleThumb(thumb);
                 }
                     
                 processedIDs.add(ref);
             }
        }
    }
    
    /**
     * Sets the specified pixels to be the thumbnail for the specified Image.
     * 
     * @param imageID The id of the Image.
     * @param thumb   The thumbnail pixels. Mustn't be <code>null</code>.
     */
    public void setFullSizeImage(long imageID, BufferedImage thumb)
    {
        if (thumb == null) throw new NullPointerException("No thumbnail.");
        Long id = Long.valueOf(imageID);
        Set providers = thumbProviders.get(id);
        if (providers != null) {
            Iterator p = providers.iterator();
            while (p.hasNext())
                ((ThumbnailProvider) p.next()).setFullSizeImage(thumb);
            processedIDs.add(id);
        }
    }
    
    /**
     * Returns the percentage of processed thumbnails.
     * 
     * @return See above.
     */
    public int getPercentDone()
    {
    	return (int) ((double) processedIDs.size()/totalIDs*100);
    }
    
    /**
     * Tells if every {@link ImageNode} in the visualization tree has been
     * assigned a thumbnail.
     * 
     * @return <code>true</code> for yes, <code>false</code> for no.
     */
    public boolean isDone() { return (processedIDs.size() == totalIDs); }
    
}
