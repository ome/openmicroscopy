/*
* pojos.util.MaskClass
*
*------------------------------------------------------------------------------
*  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
package pojos.util;


//Java imports
import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import pojos.MaskData;

/**
 * MaskClass will store all the points associated with the mask object and
 * when complete return the MaskData object of the mask.
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class MaskClass
{
	/** The points in the mask. These points are in the image coordinates. */
	private Set<Point> points;
	
	/** The colour of the mask. */
	private int colour;
	
	/** The min(x,y) and max(x,y) coordinates. */
	private Point min, max;
	
	/** The width of the mask. */
	private int width;
	
	/** The height of the mask. */
	private int height;
	 
	
	/**
	 * Instantiate a new mask object with colour value. 
	 * @param value The colour of the mask as packedInt
	 */
    MaskClass(int value)
    {
    	points = new HashSet<Point>();
    	colour = value;
    }
    	
    /**
     * Get the colour of the mask.
     * @return See above.
     */
    public Color getColour()
    {
    	return new Color(colour);
    }
    
    /**
     * Get the Points in the mask as a bytestream that can be used to 
     * make an image.	
     * @return See above.
     * @throws IOException
     */
    public byte[] asBytes() throws IOException
    {
    	   		
    	ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    	DataOutputStream outputStream = new DataOutputStream(byteStream);
   		for(int x = min.x ; x < max.x + 1 ; x++)
		{
   			for(int y = min.y ; y < max.y + 1 ; y++)
   			{
    				if(points.contains(new Point(x,y)))
    				outputStream.writeInt(colour);
    			else
					outputStream.writeInt(0);
    		}
    	}
    	outputStream.close();
    	return byteStream.toByteArray();
    }
    
    /**
     * Add Point p to the list of points in the mask.
     * @param p See above.
     */
    public void add(Point p)
    {
    	if(points.size()==0)
    	{
    		min = new Point(p);
    		max = new Point(p);
    	}
    	else
    	{
    		min.x = Math.min(p.x, min.x);
    		min.y = Math.min(p.y, min.y);
    		max.x = Math.max(p.x, max.x);
    		max.y = Math.max(p.y, max.y);
    	}
   		width = max.x-min.x+1;
		height = max.y-min.y+1;
    	points.add(p);
    }
    
    /**
     * Create a MaskData Object from the mask.
     * @param z The Z section the mask data is on.
     * @param t The T section the mask data is on.
     * @return See above.
     * @throws IOException
     */
    public MaskData asMaskData(int z, int t) throws IOException 
    {
    	MaskData mask = new MaskData();
    	
    	mask.setX(min.x);
    	mask.setY(min.y);
    	mask.setWidth(width);
    	mask.setHeight(height);
    	mask.setReadOnly(true);
    	mask.setT(t);
    	mask.setZ(z);
    	byte[] theseBytes;
    	theseBytes = this.asBytes();
    	mask.setMask(theseBytes);
    	return mask;
    }
}
