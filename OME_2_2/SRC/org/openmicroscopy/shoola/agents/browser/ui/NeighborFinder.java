/*
 * org.openmicroscopy.shoola.agents.browser.ui.NeighborFinder
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
package org.openmicroscopy.shoola.agents.browser.ui;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.openmicroscopy.shoola.agents.browser.images.Thumbnail;

/**
 * Brute-force O(n) algorithm for finding neighbors.  Could stand to use some
 * improvement, I suppose.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class NeighborFinder
{
    /**
     * Find the closest neighbor above the selected thumbnail.  If there is
     * no such neighbor, return the selected thumbnail.
     * @param selected The selected thumbnail.
     * @param set The thumbnails to check against.
     * @return The closest neighbor above the selected.
     */
    public static Thumbnail findNorthNeighbor(Thumbnail selected,
                                              Thumbnail[] set)
    {
        if(selected == null)
        {
            return null;
        }
        if(set == null || set.length == 0)
        {
            return selected;
        }
        
        Point2D point = selected.getOffset();
        Rectangle2D rect = new Rectangle2D.Double(point.getX()-5,0,
                                           selected.getWidth()+10,
                                           point.getY()-1);
        
        boolean foundClose = false;
        Thumbnail closest = null;
        double distance = Double.POSITIVE_INFINITY;
        
        for(int i=0;i<set.length;i++)
        {
            if(rect.contains(set[i].getOffset()))
            {
                Point2D target = new Point2D.Double(set[i].getOffset().getX(),
                                                    set[i].getOffset().getY()+
                                                    set[i].getHeight());
                if(point.distance(target) < distance &&
                   closest != selected)
                {
                    distance = point.distance(target);
                    foundClose = true;
                    closest = set[i];
                }
            }
        }
        if(!foundClose) closest = selected;
        return closest;
    }
    
    /**
     * Find the closest neighbor to the right of the selected thumbnail.
     * If there is no such neighbor, return the selected thumbnail.
     * @param selected The selected thumbnail.
     * @param set The thumbnails to check against.
     * @return The closest neighbor above the selected.
     */
    public static Thumbnail findEastNeighbor(Thumbnail selected,
                                             Thumbnail[] set)
    {
        if(selected == null)
        {
            return null;
        }
        if(set == null || set.length == 0)
        {
            return selected;
        }
        
        Point2D point = selected.getOffset();
        Rectangle2D rect =
            new Rectangle2D.Double(point.getX()+selected.getWidth()+1,
                                   point.getY()-5,
                                   selected.getParent().getFullBounds().getWidth(), // camera extent
                                   selected.getHeight()+10);
        
        boolean foundClose = false;
        Thumbnail closest = null;
        double distance = Double.POSITIVE_INFINITY;
        
        for(int i=0;i<set.length;i++)
        {
            if(rect.contains(set[i].getOffset()))
            {
                Point2D target = set[i].getOffset();
                if(point.distance(target) < distance &&
                   closest != selected)
                {
                    distance = point.distance(target);
                    foundClose = true;
                    closest = set[i];
                }
            }
        }
        if(!foundClose) closest = selected;
        return closest;
    }
    
    /**
     * Find the closest neighbor below the selected thumbnail.  If there is
     * no such neighbor, return the selected thumbnail.
     * @param selected The selected thumbnail.
     * @param set The thumbnails to check against.
     * @return The closest neighbor above the selected.
     */
    public static Thumbnail findSouthNeighbor(Thumbnail selected,
                                              Thumbnail[] set)
    {
        if(selected == null)
        {
            return null;
        }
        if(set == null || set.length == 0)
        {
            return selected;
        }
        
        Point2D point = selected.getOffset();
        Rectangle2D rect = new Rectangle2D.Double(point.getX()-5,
                                           point.getY()+1,
                                           selected.getWidth()+10,
                                           selected.getParent().getFullBounds().getHeight());
        
        boolean foundClose = false;
        Thumbnail closest = null;
        double distance = Double.POSITIVE_INFINITY;
        
        for(int i=0;i<set.length;i++)
        {
            if(rect.contains(set[i].getOffset()))
            {
                Point2D target = set[i].getOffset();
                if(point.distance(target) < distance &&
                   closest != selected)
                {
                    distance = point.distance(target);
                    foundClose = true;
                    closest = set[i];
                }
            }
        }
        if(!foundClose) closest = selected;
        return closest;
    }
    
    /**
     * Find the closest neighbor to the left of the selected thumbnail.
     * If there is no such neighbor, return the selected thumbnail.
     * @param selected The selected thumbnail.
     * @param set The thumbnails to check against.
     * @return The closest neighbor above the selected.
     */
    public static Thumbnail findWestNeighbor(Thumbnail selected,
                                             Thumbnail[] set)
    {
        if(selected == null)
        {
            return null;
        }
        if(set == null || set.length == 0)
        {
            return selected;
        }
        
        Point2D point = selected.getOffset();
        Rectangle2D rect = new Rectangle2D.Double(0,point.getY()-5,
                                           point.getX()-1,
                                           selected.getHeight()+10);
        
        boolean foundClose = false;
        Thumbnail closest = null;
        double distance = Double.POSITIVE_INFINITY;
        
        for(int i=0;i<set.length;i++)
        {
            if(rect.contains(set[i].getOffset()))
            {
                Point2D target = new Point2D.Double(set[i].getOffset().getX()+
                                                    set[i].getWidth(),
                                                    set[i].getOffset().getY());
                if(point.distance(target) < distance &&
                   closest != selected)
                {
                    distance = point.distance(target);
                    foundClose = true;
                    closest = set[i];
                }
            }
        }
        if(!foundClose) closest = selected;
        return closest;
    }
}
