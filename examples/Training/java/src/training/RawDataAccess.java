/*
 * training.RawDataAccess 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package training;


//Java imports
import java.util.ArrayList;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import omero.api.RawPixelsStorePrx;
import pojos.ImageData;
import pojos.PixelsData;
import training.util.DataSink;
import training.util.Plane2D;

/** 
 * Sample code showing how to access raw data.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.3.2
 */
public class RawDataAccess
	extends ConnectToOMERO
{
	
	/** Information to edit.*/
	private long imageId = 27544;
	
	/** The image.*/
	private ImageData image;

	/** Load the image.*/
	private void loadImage()
		throws Exception
	{
		image = loadImage(imageId);
		if (image == null)
			throw new Exception("Image does not exist. Check ID.");
	}
	
	/**
	 * Retrieve a given plane. 
	 * 
	 * This is useful when you need the pixels intensity.
	 */
	private void retrievePlane()
		throws Exception
	{
		//To retrieve the image, see above.
		PixelsData pixels = image.getDefaultPixels();
		int sizeZ = pixels.getSizeZ();
		int sizeT = pixels.getSizeT();
		int sizeC = pixels.getSizeC();
		int sizeX = pixels.getSizeX();
		int sizeY = pixels.getSizeY();
		long pixelsId = pixels.getId();
		RawPixelsStorePrx store = entryUnencrypted.createRawPixelsStore(); 
		store.setPixelsId(pixelsId, false);
		DataSink data = new DataSink(pixels);
		Plane2D p;
		for (int z = 0; z < sizeZ; z++) {
			for (int t = 0; t < sizeT; t++) {
				for (int c = 0; c < sizeC; c++) {
					 byte[] plane = store.getPlane(z, c, t);
					 p = data.getPlane(plane);
					 for (int x = 0; x < sizeX; x++) {
						for (int y = 0; y < sizeY; y++) {
							System.err.println(p.getPixelValue(x, y));
						}
					}
				}
			}
		}
		store.close();
	}
	
	/**
	 * Retrieve a given tile.. 
	 * 
	 * This is useful when you need the pixels intensity.
	 */
	private void retrieveTile()
		throws Exception
	{
		//To retrieve the image, see above.
		PixelsData pixels = image.getDefaultPixels();
		int sizeZ = pixels.getSizeZ();
		int sizeT = pixels.getSizeT();
		int sizeC = pixels.getSizeC();
		long pixelsId = pixels.getId();
		RawPixelsStorePrx store = entryUnencrypted.createRawPixelsStore(); 
		store.setPixelsId(pixelsId, false);
		//tile = (50, 50, 10, 10)  x, y, width, height of tile
		int x = 0;
		int y = 0;
		int width = pixels.getSizeX()/2;
		int height = pixels.getSizeY()/2;
		for (int z = 0; z < sizeZ; z++) {
			for (int t = 0; t < sizeT; t++) {
				for (int c = 0; c < sizeC; c++) {
					 byte[] plane = store.getTile(z, c, t, x, y, width, height);
				}
			}
		}
		store.close();
	}
	/**
	 * Retrieve a given stack.
	 * 
	 * This is useful when you need the pixels intensity.
	 */
	private void retrieveStack()
		throws Exception
	{
		//To retrieve the image, see above.
		PixelsData pixels = image.getDefaultPixels();
		int sizeT = pixels.getSizeT();
		int sizeC = pixels.getSizeC();
		long pixelsId = pixels.getId();
		RawPixelsStorePrx store = entryUnencrypted.createRawPixelsStore(); 
		store.setPixelsId(pixelsId, false);
		for (int t = 0; t < sizeT; t++) {
			for (int c = 0; c < sizeC; c++) {
				 byte[] plane = store.getStack(c, t);
			}
		}
		store.close();
	}
	
	/**
	 * Retrieve a given hypercube. 
	 * 
	 * This is useful when you need the pixels intensity.
	 */
	private void retrieveHypercube()
		throws Exception
	{
		//To retrieve the image, see above.
		PixelsData pixels = image.getDefaultPixels();
		long pixelsId = pixels.getId();
		RawPixelsStorePrx store = entryUnencrypted.createRawPixelsStore();
		store.setPixelsId(pixelsId, false);
		// offset values in each dimension XYZCT
		List<Integer> offset = new ArrayList<Integer>();
		offset.add(0);
		offset.add(0);
		offset.add(0);
		offset.add(0);
		offset.add(0);

		List<Integer> size = new ArrayList<Integer>();
		size.add(pixels.getSizeX());
		size.add(pixels.getSizeY());
		size.add(pixels.getSizeZ());
		size.add(pixels.getSizeC());
		size.add(pixels.getSizeT());

		// indicate the step in each direction, step = 1, 
		//will return values at index 0, 1, 2.
		//step = 2, values at index 0, 2, 4 etc.
		List<Integer> step = new ArrayList<Integer>();
		step.add(1);
		step.add(1);
		step.add(1);
		step.add(1);
		step.add(1);
		byte[] values = store.getHypercube(offset, size, step);
		//Do something
		store.close();
	}
	
	/**
	 * Connects and invokes the various methods.
	 */
	RawDataAccess()
	{
		try {
			connect();
			loadImage();
			retrievePlane();
			retrieveTile();
			retrieveStack();
			retrieveHypercube();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				disconnect(); // Be sure to disconnect
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args)
	{
		new RawDataAccess();
		System.exit(0);
	}

}
