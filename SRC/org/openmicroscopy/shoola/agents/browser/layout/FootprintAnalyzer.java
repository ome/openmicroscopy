/*
 * org.openmicroscopy.shoola.agents.browser.layout.FootprintAnalyzer.java
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

/*------------------------------------------------------------------------------
 *
 * Written by:    Jeff Mellen <jeffm@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */
 
package org.openmicroscopy.shoola.agents.browser.layout;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.openmicroscopy.shoola.agents.browser.images.Thumbnail;

/**
 * A class that determines the space that a particular group (or subgroup)
 * of thumbnails occupies.
 *
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class FootprintAnalyzer
{
    /**
     * Gets the area returned by all the thumbnails in the map.  The map is
     * a map returned by a LayoutMethod.
     * 
     * @param pointThumbnailMap The map retured by a LayoutMethod, containing
     *        all the offsets of the Thumbnails.
     * @return The rectangle encompassing all the thumbnails.
     */
    public static Rectangle2D getArea(Map pointThumbnailMap)
    {
        boolean initialized = false;
        double nOffset = 0;
        double wOffset = 0;
        double eOffset = 0;
        double sOffset = 0;
        
        for(Iterator iter = pointThumbnailMap.keySet().iterator();
            iter.hasNext();)
        {
            Thumbnail t = (Thumbnail)iter.next();
            Point2D offset = (Point2D)pointThumbnailMap.get(t);
            
            if(!initialized)
            {
                wOffset = offset.getX();
                nOffset = offset.getY();
                eOffset = wOffset + t.getImage().getWidth(null);
                sOffset = nOffset + t.getImage().getHeight(null);
                initialized = true;
            }
            else
            {
                if(offset.getX() < wOffset)
                {
                    wOffset = offset.getX();
                }
                if(offset.getY() < nOffset)
                {
                    nOffset = offset.getY();
                }
                double eBound = offset.getX() + t.getImage().getWidth(null);
                double sBound = offset.getY() + t.getImage().getHeight(null);
                
                if(eBound > eOffset)
                {
                    eOffset = eBound;
                }
                if(sBound > sOffset)
                {
                    sOffset = sBound;
                }
            }
            
        }
        
        return new Rectangle2D.Double(wOffset,nOffset,
                                      eOffset-wOffset,
                                      sOffset-nOffset);
    }
    
    /**
     * Gets the area returned by the thumbnails specified in the group model.
     * The map is a map returned by a LayoutMethod.
     * 
     * @param pointThumbnailMap The map returned by a LayoutMethod, containing
     *        all the offsets of the Thumbnails.
     * @param subgroup The subgroup of Thumbnails to select the area of.
     * @return The rectangle encompassing the specified thumbnails.
     */
    public static Rectangle2D getArea(Map pointThumbnailMap,
                                      GroupModel subgroup)
    {
        Map subMap = new HashMap();
        
        for(Iterator iter = subgroup.getThumbnails().iterator();
            iter.hasNext();)
        {
            Thumbnail t = (Thumbnail)iter.next();
            subMap.put(t,pointThumbnailMap.get(t));
        }
        return getArea(subMap);
    }
}
