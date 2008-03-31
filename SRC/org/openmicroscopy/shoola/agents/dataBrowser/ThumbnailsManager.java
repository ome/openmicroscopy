/*
 * org.openmicroscopy.shoola.agents.dataBrowser.ThumbnailsManager 
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
package org.openmicroscopy.shoola.agents.dataBrowser;

//Java imports
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.Thumbnail;

//Third-party libraries

//Application-internal dependencies
import pojos.ImageData;

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
 * @since OME3.0
 */
public class ThumbnailsManager
{
	
	/** How many different Images we have. */
    private int     			totalIDs;
    
    /** Ids of the images whose thumbnails have already been set. */
    private Set<Long>     		processedIDs;
    
    /** 
     * Maps an Image id onto all the {@link ThumbnailProvider}s in the
     * visualization tree that have to provide a thumbnail for that Image.
     * Note that {@link ThumbnailProvider}s are not shared, so there's one
     * for each {@link ImageNode} that represents the given Image. 
     */
    private Map<Long, Set>     	thumbProviders;

    /**
     * Creates a new instance.
     * 
     * @param imageNodes All the {@link ImageNode}s in a given visualization
     *                   tree.  Mustn't be <code>null</code>.
     */
    public ThumbnailsManager(Collection<ImageData> imageNodes)
    {
        if (imageNodes == null) 
            throw new NullPointerException("No image nodes.");
        totalIDs = 0;
        processedIDs = new HashSet<Long>();
        thumbProviders = new HashMap<Long, Set>();
        Iterator i = imageNodes.iterator();
        ImageNode node;
        ImageData is;
        Long id;
        Set<Thumbnail> providers;
        while (i.hasNext()) {
            node = (ImageNode) i.next();
            is = (ImageData) node.getHierarchyObject();
            id = new Long(is.getId());
            providers = thumbProviders.get(id);
            if (providers == null) {
                totalIDs++;
                providers = new HashSet<Thumbnail>();
                thumbProviders.put(id, providers);
            }
            providers.add(node.getThumbnail());
        }
    }
    
    /**
     * Sets the specified pixels to be the thumbnail for the specified Image.
     * 
     * @param imageID The id of the Image.
     * @param thumb   The thumbnail pixels. Mustn't be <code>null</code>.
     */
    public void setThumbnail(long imageID, BufferedImage thumb)
    {
        if (thumb == null) throw new NullPointerException("No thumbnail.");
        Long id = new Long(imageID);
        Set providers = thumbProviders.get(id);
        if (providers != null) {
            Iterator p = providers.iterator();
            while (p.hasNext())
                ((ThumbnailProvider) p.next()).setFullScaleThumb(thumb);
            processedIDs.add(id);
        }
    }
    
    /**
     * Tells if every {@link ImageNode} in the visualization tree has been
     * assigned a thumbnail.
     * 
     * @return <code>true</code> for yes, <code>false</code> for no.
     */
    public boolean isDone() { return (processedIDs.size() == totalIDs); }
    
}
