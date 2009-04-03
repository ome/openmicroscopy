/*
 * org.openmicroscopy.shoola.agents.browser.layout.MaxWidthLayoutMethod
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
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openmicroscopy.shoola.agents.browser.images.Thumbnail;

/**
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class MaxWidthLayoutMethod extends AbstractOrderedLayoutMethod
                                  implements ConstrainedLayoutMethod
{
    private int width;
    
    public static int DEFAULT_HMARGIN = 10;
    public static int DEFAULT_VMARGIN = 10;
    
    private int hMargin = DEFAULT_HMARGIN;
    private int vMargin = DEFAULT_VMARGIN;
    
    public MaxWidthLayoutMethod(int width)
    {
        super(new ImageIDComparator());
        if(width < 97)
        {
            throw new IllegalArgumentException("There must be width > 96.");
        }
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.layout.ConstrainedLayoutMethod#setDimension(int, int)
     */
    public void setDimension(int widthPixels, int heightPixels)
    {
        if(widthPixels < 97) { return; }
        this.width = widthPixels;
    }
    
    /**
     * Sets the horizontal margin between images, in pixels.
     * @param margin See above.
     */
    public void setHorizontalMargin(int margin)
    {
        this.hMargin = margin;
    }
        
    /**
     * Sets the vertical margin between images, in pixels.
     * @param margin See above.
     */
    public void setVerticalMargin(int margin)
    {
        this.vMargin = margin;
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.layout.LayoutMethod#getAnchorPoint(org.openmicroscopy.shoola.agents.browser.images.Thumbnail)
     */
    public Point2D getAnchorPoint(Thumbnail t)
        throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException("Layout method does not" +
            " retain location state.");
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.layout.LayoutMethod#getAnchorPoints(org.openmicroscopy.shoola.agents.browser.images.Thumbnail[])
     */
    public Map getAnchorPoints(Thumbnail[] ts)
    {
        if(ts == null || ts.length == 0)
        {
            return new HashMap();
        }
        List orderList = getThumbnailOrder(ts);
        Map pointMap = new HashMap();
        Iterator iter = orderList.iterator();
        
        int vOffset = 0;
        int maxHeight = 0;
        int hOffset = 0;
        while(iter.hasNext())
        {
            Thumbnail t = (Thumbnail)iter.next();
            BufferedImage bi = (BufferedImage)t.getImage();
            if(hOffset + bi.getWidth() > width)
            {
                vOffset += (maxHeight + vMargin);
                hOffset = 0;
            }
            pointMap.put(t,new Point2D.Double(hOffset,vOffset));
            hOffset += (bi.getWidth() + hMargin);
            if(bi.getHeight() > maxHeight) {
                maxHeight = bi.getHeight();
            }
        }
        return pointMap;
    }

}
