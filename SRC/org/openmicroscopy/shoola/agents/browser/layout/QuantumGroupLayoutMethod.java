/*
 * org.openmicroscopy.shoola.agents.browser.layout.QuantumGroupLayoutMethod
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

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmicroscopy.shoola.agents.browser.images.Thumbnail;

/**
 * Organizes the images by grouping method into a quantum treemap configuration.
 * For more information on quantum treemaps, check out the University of
 * Maryland's PhotoMesa image browser, at http://www.cs.umd.edu/hcil/photomesa.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class QuantumGroupLayoutMethod extends AbstractOrderedLayoutMethod
{
    private GroupingMethod groupMethod;
    private int thumbnailWidth;
    private int thumbnailHeight;
    
    private Map rectangleMap;
    public static int DEFAULT_HMARGIN = 5;
    public static int DEFAULT_VMARGIN = 5;
    
    public static int DEFAULT_WIDTH = 64;
    public static int DEFAULT_HEIGHT = 48;

    private int hMargin = DEFAULT_HMARGIN;
    private int vMargin = DEFAULT_VMARGIN;
    
    public QuantumGroupLayoutMethod(GroupingMethod groupMethod,
                                    int thumbnailWidth,
                                    int thumbnailHeight)
    {
        super(new ImageIDComparator());
        if(groupMethod == null)
        {
            throw new IllegalArgumentException("Null grouing method");
        }
        else if(thumbnailWidth < 16 || thumbnailHeight < 16)
        {
            throw new IllegalArgumentException("Thumbnails too small");
        }
        rectangleMap = new HashMap();
        this.groupMethod = groupMethod;
        this.thumbnailWidth = thumbnailWidth;
        this.thumbnailHeight = thumbnailHeight;
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.layout.LayoutMethod#getAnchorPoint(org.openmicroscopy.shoola.agents.browser.images.Thumbnail)
     */
    public Point2D getAnchorPoint(Thumbnail t)
        throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException("Position state not retained");
    }
    
    /**
     * Gets the anchor points in a default near-square configuration.
     * 
     * @see org.openmicroscopy.shoola.agents.browser.layout.LayoutMethod#getAnchorPoints(org.openmicroscopy.shoola.agents.browser.images.Thumbnail[])
     */
    public Map getAnchorPoints(Thumbnail[] ts)
    {
        GroupModel[] models = groupMethod.getGroups();
        int[] sizes = new int[models.length];
        for(int i=0;i<sizes.length;i++)
        {
            sizes[i] = models[i].getThumbnails().size();
        }
        // TODO make this more adaptive, OK for now
        Rectangle rect = new Rectangle(0,0,560,525);
        Map pointMap = new HashMap();
        QuantumTreemap qt = new QuantumTreemap(sizes,1.0,rect);
        qt.setPivotIndexType(QuantumTreemap.INDEX_BY_MIDDLE);
        Rectangle[] results = qt.quantumLayout();
        for(int i=0;i<results.length;i++)
        {
            Rectangle2D region =
                new Rectangle2D.Double(results[i].x*thumbnailWidth,
                                       results[i].y*thumbnailHeight,
                                       (results[i].width*thumbnailWidth),
                                       (results[i].height*thumbnailHeight));
            rectangleMap.put(models[i],region);
        }
        
        for(int i=0;i<models.length;i++)
        {
            GroupModel model = models[i];
            Rectangle result = results[i];
            Set thumbnails = model.getThumbnails();
            Thumbnail[] array = new Thumbnail[thumbnails.size()];
            thumbnails.toArray(array);
            List orderedThumbnails = getThumbnailOrder(array);
            Map subMap = layoutWithinGroup(orderedThumbnails,result);
            pointMap.putAll(subMap);
        }
        return pointMap;
        
    }
    
    public Rectangle2D getRegionBounds(GroupModel group)
    {
        if(group == null) return null;
        else return (Rectangle2D)rectangleMap.get(group);
    }
    
    private Map layoutWithinGroup(List thumbnails, Rectangle region)
    {
        if(thumbnails == null || region == null)
        {
            return new HashMap();
        }
        int index = 0;
        Map pointMap = new HashMap();
        for(int i=region.y;i<region.y+region.height;i++)
        {
            for(int j=region.x;j<region.x+region.width;j++)
            {
                if(index >= thumbnails.size())
                {
                    return pointMap;
                }
                Thumbnail t = (Thumbnail)thumbnails.get(index++);
                pointMap.put(t,new Point2D.Double(DEFAULT_HMARGIN+thumbnailWidth*j,
                                                  DEFAULT_VMARGIN+thumbnailHeight*i));
            }
        }
        return pointMap;
    }


}
