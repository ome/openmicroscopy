/*
 * org.openmicroscopy.shoola.agents.browser.layout.GraphLayout
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
import java.util.HashMap;
import java.util.Map;

import org.openmicroscopy.shoola.agents.browser.datamodel.ThumbnailDataPair;
import org.openmicroscopy.shoola.agents.browser.heatmap.LinearScale;
import org.openmicroscopy.shoola.agents.browser.images.Thumbnail;

/**
 * A method that organizes thumbnails in a 2D scatter plot.  Both axes are linear.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2.1
 * @since OME2.2.1
 */
public class GraphLayoutMethod implements LayoutMethod
{
    /**
     * The vertical minimum value (bottom)
     */
    protected double min;
    
    /**
     * The vertical maximum value (top)
     */
    protected double max;
    
    protected Map positionMap;
    
    /**
     * Creates a graph layout, establishing only a minimum and maximum value.  It
     * is a stateful layout, so both methods defined by LayoutMethod work.
     * 
     * @param minimumValue The minimum value of the compared elements.
     * @param maximumValue The maximum value of the compared elements.
     * @param thumbnails The list of thumbnails to layout.
     */
    public GraphLayoutMethod(double minimumValue,
                             double maximumValue,
                             ThumbnailDataPair[] thumbnails)
    {
       this.min = minimumValue;
       this.max = maximumValue;
       
       int xOffset = 0;
       int yMax = 16*thumbnails.length;
       
       LinearScale scale = new LinearScale(minimumValue,maximumValue);
       
       for(int i=0;i<thumbnails.length;i++)
       {
           Thumbnail t = thumbnails[i].getThumbnail();
           double num = thumbnails[i].getValue().doubleValue();
           positionMap.put(t,new Point2D.Double(xOffset,
                                                yMax-scale.getScalar(num,0,yMax)));
       }
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.layout.LayoutMethod#getAnchorPoint(org.openmicroscopy.shoola.agents.browser.images.Thumbnail)
     */
    public Point2D getAnchorPoint(Thumbnail t)
        throws UnsupportedOperationException
    {
        return (Point2D)positionMap.get(t);
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.layout.LayoutMethod#getAnchorPoints(org.openmicroscopy.shoola.agents.browser.images.Thumbnail[])
     */
    public Map getAnchorPoints(Thumbnail[] ts)
    {
        if(ts == null) return null;
        
        Map returnMap = new HashMap();
        for(int i=0;i<ts.length;i++)
        {
            returnMap.put(ts[i],positionMap.get(ts[i]));
        }
        return returnMap;
    }
}
