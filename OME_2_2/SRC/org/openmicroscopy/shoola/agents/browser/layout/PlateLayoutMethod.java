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
import java.util.HashMap;
import java.util.IdentityHashMap;
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
public class PlateLayoutMethod implements LayoutMethod
{
    private int numRows; // #rows in the plate
    private int numCols; // #cols in the plate
    
    public static int DEFAULT_HMARGIN = 10;
    public static int DEFAULT_VMARGIN = 10;
    
    public static int DEFAULT_WELLWIDTH = 96;
    public static int DEFAULT_WELLHEIGHT = 96;
    
	private int hMargin = DEFAULT_HMARGIN; // horizontal buffer
	private int vMargin = DEFAULT_VMARGIN; // vertical buffer
    
    private int wellWidth = DEFAULT_WELLWIDTH; // width in pixels of each well in display
    private int wellHeight = DEFAULT_WELLHEIGHT; // height in pixels of each well in display
    
    private Map thumbMap;
    
    /**
     * Construct a layout method that displays a plate of cols x rows.
     * @param rows The number of rows in the plate.
     * @param cols The number of colums on the plate.
     * @throws IllegalArgumentException If the number of rows or columns is
     *                                  less than or equal to zero.
     */
    public PlateLayoutMethod(int rows, int cols)
        throws IllegalArgumentException
    {
        if(rows <= 0 || cols <= 0)
        {
            throw new IllegalArgumentException("All parameters must be positive");
        }
        this.numRows = rows;
        this.numCols = cols;
        thumbMap = new HashMap();
    }
    
    public PlateLayoutMethod(int rows, int cols,
                             int wellWidth, int wellHeight)
    {
        if(rows <= 0 || cols <= 0 ||
           wellWidth <= 0 || wellHeight <= 0)
        {
            throw new IllegalArgumentException("All parameters must be positive");
        }
        this.numRows = rows;
        this.numCols = cols;
        this.wellWidth = wellWidth;
        this.wellHeight = wellHeight;
    }
    
    /**
     * Sets the position in the plate of the specified thumbnail.
     * @param t The thumbnail to place.
     * @param row The row of the thumbnail in the plate.
     * @param column The column of the thumbnail in the plate.
     */
    public void setIndex(Thumbnail t, int row, int column)
    {
        thumbMap.put(t,new Integer(row*numCols+column));
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
     * Sets the well width (at 100%) to the specified pixel width.
     * @param width See above.
     */
    public void setWellWidth(int width)
    {
        this.wellWidth = width;
    }
    
    /**
     * Sets the well height (at 100%) to the specified pixel height.
     * @param height See above.
     */
    public void setWellHeight(int height)
    {
        this.wellHeight = height;
    }
    
    /**
     * Returns the anchor point based on the set well row/column.  Will return
     * null if the position of the thumbnail has not been specified.
     */
    public Point2D getAnchorPoint(Thumbnail t)
    {
        Integer wellNum = (Integer)thumbMap.get(t);
        if(wellNum == null)
        {
            return null;
        }
        
        int number = wellNum.intValue();
        int row = number / numCols;
        int col = number % numCols;
        
        return new Point2D.Double(wellWidth*col+hMargin*col,
                                  wellHeight*row+vMargin*row);
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
        
        // MAJOR ASSUMPTION: all images are of the same size in the
        // plate layout method.  If this is incorrect, this is bad news.
        Map pointMap = new IdentityHashMap();
        for(int i=0;i<ts.length;i++)
        {
            pointMap.put(ts[i],getAnchorPoint(ts[i]));
        }
        
        return pointMap;
    }
}
