/*
 * org.openmicroscopy.shoola.agents.browser.layout.PlateLayoutMethod
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
 * Lays out thumbnails in a plate method-- specifiying ahead of time how
 * many rows and columns there are.  Right now, they are ordered by ID--
 * this can change and be a little more sophisticated by analyzing the well
 * number.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class PlateLayoutMethod extends AbstractOrderedLayoutMethod
{
    private int numRows; // #rows in the plate
    private int numCols; // #cols in the plate
    
    public static int DEFAULT_HMARGIN = 10;
    public static int DEFAULT_VMARGIN = 10;
    
	private int hMargin = DEFAULT_HMARGIN; // horizontal buffer
	private int vMargin = DEFAULT_VMARGIN; // vertical buffer
    
    /**
     * Construct a layout method that displays a plate of cols x rows.
     * @param rows The number of rows in the plate.
     * @param cols The number of colums on the plate.
     */
    public PlateLayoutMethod(int rows, int cols)
    {
        super(new ImageIDComparator());
        this.numRows = rows;
        this.numCols = cols;
    }
    
    /**
     * Sets the horizontal margin (pixels) between thumbnails at 100%.
     * @param margin See above.
     */
    public void setHorizontalMargin(int margin)
    {
        this.hMargin = margin;
    }
    
    /**
     * Sets the vertical margin (pixels) between thumbnails at 100%.
     * @param margin See above.
     */
    public void setVerticalMargin(int margin)
    {
        this.vMargin = margin;
    }
    
    /**
     * Throws null as the method does not maintain any state-- for now.
     * TODO: change such that you can get it by well number/name.
     */
    public Point2D getAnchorPoint(Thumbnail t)
    {
        // TODO support this method via well lookup
        throw new UnsupportedOperationException("Layout method does not" +
            "retain state; use getAnchorPoints(Thumbnail[]) instead.");
    }
    
    
    /**
     * Returns a map mapping all the thumbnails to their anchor points
     * (offsets) in the group.
     */
    public Map getAnchorPoints(Thumbnail[] ts)
    {
        if(ts == null || ts.length == 0)
        {
            // return empty
            return new HashMap();
        }
        List orderList = getThumbnailOrder(ts);
        
        // MAJOR ASSUMPTION: all images are of the same size in the
        // plate layout method.  If this is incorrect, this is bad news.
        Map pointMap = new HashMap();
        Iterator iter = orderList.iterator();
        
        // placement loop
        int vOffset = 0;
        
        for(int i=0;i<numRows;i++)
        {
            int maxHeight = 0;
            int hOffset = 0;
            if(!iter.hasNext())
            {
            	break;
            }
            
            for(int j=0;j<numCols;j++)
            {
            	if(!iter.hasNext())
            	{
            		break;
            	}
                Thumbnail t = (Thumbnail)iter.next();
                BufferedImage bi = (BufferedImage)t.getImage();
                pointMap.put(t,new Point2D.Double(hOffset,vOffset));
                hOffset += (bi.getWidth() + hMargin);
                
                if(bi.getHeight() > maxHeight)
                {
                    maxHeight = bi.getHeight();
                }
            }
            vOffset += (maxHeight + vMargin);
        }
        
        return pointMap;
    }

    
    

}
