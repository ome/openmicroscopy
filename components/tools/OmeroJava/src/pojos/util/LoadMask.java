/*
* pojos.util.LoadMask
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import pojos.ImageData;
import pojos.ROIData;

/**
 * Script to create MaskData objects from uploaded Images.
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
public class LoadMask
{

	/** Map of colour, roiclass this will store all the masks in the image, and
	 * all the roi with a particular colour. */
	private HashMap<Color, ROIClass> roiMap;
	
	/**
	 * Instantiate the LoadMask Object.
	 */
	public LoadMask()
	{
		roiMap = new HashMap<Color, ROIClass>();
	}
	
	
	/**
	 * Add a Mask Shape to the appropriate ROIClass, creating one if neccessary.
	 * @param image The Image containing the mask dat.
	 * @param z The Z Section of the image.
	 * @param t The Time point of the image.
	 */
	public void addMaskShape(BufferedImage image, int z, int t)
	{
		Map<Integer, MaskClass> maskMap = createMasks(image, z, t);
		
		Iterator<Integer> maskIterator = maskMap.keySet().iterator();
		while(maskIterator.hasNext())
		{
			MaskClass mask = maskMap.get(maskIterator.next());
			ROIClass roiClass;
			if(roiMap.containsKey(mask.getColour()))
				roiClass = roiMap.get(mask.getColour());
			else
			{
				roiClass = new ROIClass();
				roiMap.put(mask.getColour(), roiClass);
			}
			roiClass.addMask(mask, z, t);
		}
	}
	
	/**
	 * Get all the masks for the image.
	 * @param image The Image containing the mask dat.
	 * @param z The Z Section of the image.
	 * @param t The Time point of the image.
	 * @return See above.
	 */
	private Map<Integer, MaskClass> createMasks(BufferedImage inputImage, int z, int t)
	{
		int value;
		MaskClass mask;
		Map<Integer, MaskClass> map = new HashMap<Integer, MaskClass>();
		for (int x = 0; x < inputImage.getWidth(); x++)
			for (int y = 0; y < inputImage.getHeight(); y++)
			{
				value = inputImage.getRGB(x, y);
				if(value==Color.black.getRGB())
					continue;
				if (!map.containsKey(value))
				{
					mask = new MaskClass(value);
					map.put(value, mask);
				}
				else
					mask = map.get(value);
				mask.add(new Point(x, y));
			}
		return map;
	}
	
	/**
	 * Return all the roi for the image. 
	 * @param image See above.
	 * @return See above.
	 * @throws IOException
	 */
	public List<ROIData> getROIForImage(ImageData image) throws IOException
	{
		List<ROIData> roiList = new ArrayList<ROIData>();
		Iterator<Color> roiIterator = roiMap.keySet().iterator();
		while(roiIterator.hasNext())
		{
			ROIClass roi = roiMap.get(roiIterator.next());
			roiList.add(roi.getROI(image));
		}
		return roiList;
	}
}
