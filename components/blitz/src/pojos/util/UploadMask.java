/*
 * pojos.util.UploadMask
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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

package pojos.util;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import pojos.MaskData;
import pojos.ROIData;
import omero.model.RoiI;

/** 
 * Uploaded for masks.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @since OME3.0
 */

public class UploadMask
{
	
	/** The default color. */
	private static final int DEFAUL_COLOR = Color.black.getRGB();
	
	/** The ROIComponent. */
	private ROIComponent component;
	
	/**
	 * Maps the <integer, MaskClass> map to an MaskData object. 
	 * 
	 * @param map See above.
	 * @param z The z section of the image.
	 * @param t The t point of the image.
	 * @param c The channel of the image.
	 * @return See above.
	 * @throws IOException
	 */
	private Map<Integer, MaskData> mapToMaskData(Map<Integer, MaskClass> map, 
						int z, int t, int c) throws IOException
	{
		Map<Integer, MaskData> maskDataMap = new HashMap<Integer, MaskData>();
		Iterator<Integer> iterator = map.keySet().iterator();
		int value;
		MaskData mask;
		while (iterator.hasNext())
		{
			value = iterator.next();
			mask = map.get(value).asMaskData(z, t, c);
			maskDataMap.put(value, mask);
		}
		return maskDataMap;
	}
	
	/**
	 * Creates a new instance.
	 * 
 	 * @param imageId The ImageId where the uploaded images are from. 
	 */
	public UploadMask(long imageId)
	{
		component = new ROIComponent(imageId);
	}
	
	/**
	 * Adds a Mask Shape to the appropriate ROIClass, creating one if necessary.
	 * 
	 * @param image The Image containing the mask data.
	 * @param z The Z Section of the image.
	 * @param t The Time point of the image.
	 * @throws IOException Thrown if an error occurred while creating the mask.
	 */
	public void addImage(byte[] image, int z, int t, int c) 
		throws IOException
	{
		Map<Integer, MaskClass> classMap = createMasks(image);
		Map<Integer, MaskData> maskMap = mapToMaskData(classMap, z, t, c);
		component.addMasks(maskMap);
	}
	
	/**
	 * Adds a Mask Shape to the appropriate ROIClass, creating one if necessary.
	 * 
	 * @param image The Image containing the mask data.
	 * @param z The Z Section of the image.
	 * @param t The Time point of the image.
	 * @throws IOException Thrown if an error occurred while creating the mask.
	 */
	public void addArray(int[][] image, int z, int t, int c) 
		throws IOException
	{
		Map<Integer, MaskClass> classMap = createMasks(image);
		Map<Integer, MaskData> maskMap = mapToMaskData(classMap, z, t, c);
		component.addMasks(maskMap);
	}
	
	/**
	 * Creates Mask Class objects from the bytes stream, This will create a 
	 * Mask object for each unique colour value in the image.  
	 * 
	 * @param bytes The bytes representing the image.
	 * @return A map of <Integer, MaskClass>
	 * @throws IOException Thrown if an error occurred while creating the mask.
	 */
	private Map<Integer, MaskClass> createMasks(byte[] bytes) 
		throws IOException
	{
		Map<Integer, MaskClass> maskMap = new HashMap<Integer, MaskClass>();
		ByteArrayInputStream imageStream = new ByteArrayInputStream(bytes);
		BufferedImage inputImage = ImageIO.read(imageStream);
		int value;
		MaskClass mask;
		for (int x = 0; x < inputImage.getWidth(); x++)
			for (int y = 0; y < inputImage.getHeight(); y++)
			{
				value = inputImage.getRGB(x, y);
				if (value == DEFAUL_COLOR)
					continue;
				if (!maskMap.containsKey(value))
				{
					mask = new MaskClass(value);
					maskMap.put(value, mask);
				}
				else
					mask = maskMap.get(value);
				mask.add(new Point(x, y));
			}
		return maskMap;
	}
	
	/**
	 * Creates Mask Class objects from the bytes stream, This will create a 
	 * Mask object for each unique colour value in the image.  
	 * 
	 * @param bytes The bytes representing the image.
	 * @return A map of <Integer, MaskClass>
	 * @throws IOException Thrown if an error occurred while creating the mask.
	 */
	private Map<Integer, MaskClass> createMasks(int[][] data) 
		throws IOException
	{
		Map<Integer, MaskClass> maskMap = new HashMap<Integer, MaskClass>();
	
		int value;
		MaskClass mask;
		for (int y = 0; y < data.length; y++)
			for (int x = 0; x < data[y].length; x++)
			{
				value = data[x][y];
				if (value == DEFAUL_COLOR || value == 0)
					continue;
				if (!maskMap.containsKey(value))
				{
					mask = new MaskClass(value);
					maskMap.put(value, mask);
				}
				else
					mask = maskMap.get(value);
				mask.add(new Point(x, y));
			}
		return maskMap;
	}
	
	/**
	 * Returns the ROIs created from uploading the images.
	 * 
	 * @return See above.
	 */
	public List<RoiI> getROI()
	{
		List<ROIData> roiList =  component.getROI();
		List<RoiI> rList = new ArrayList<RoiI>();
		for (ROIData roi : roiList)
			rList.add((RoiI)roi.asIObject());
		return rList;
	}

}
