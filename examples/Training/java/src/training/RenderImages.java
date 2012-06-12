/*
 * training.RenderImages
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
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.imageio.ImageIO;


//Third-party libraries

//Application-internal dependencies
import omero.api.RenderingEnginePrx;
import omero.api.ThumbnailStorePrx;
import omero.romio.PlaneDef;
import pojos.ImageData;
import pojos.PixelsData;


/** 
 * Sample code showing how to start a rendering engine and how to interact
 * with it.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.3.2
 */
public class RenderImages
	extends ConnectToOMERO
{
	
	/** Information to edit.*/
	private long imageId = 27544;
	
	private ImageData image;
	/** 
	 * Retrieve an image if the identifier is known.
	 */
	private void loadImage()
		throws Exception
	{
		image = loadImage(imageId);
		if (image == null)
			throw new Exception("Image does not exist. Check ID.");
	}
	
	/**
	 * Creates a rendering engine.
	 */
	private void createRenderingEngine()
		throws Exception
	{
		PixelsData pixels = image.getDefaultPixels();
		long pixelsId = pixels.getId();
		RenderingEnginePrx proxy = entryUnencrypted.createRenderingEngine();
		proxy.lookupPixels(pixelsId);
		if (!(proxy.lookupRenderingDef(pixelsId))) {
			proxy.resetDefaults();
			proxy.lookupRenderingDef(pixelsId);
		}
		proxy.load();
		// Now can interact with the rendering engine.
		proxy.setActive(0, Boolean.valueOf(false));
		// to render the image uncompressed
		PlaneDef pDef = new PlaneDef();
		pDef.z = 0;
		pDef.t = 0;
		pDef.slice = omero.romio.XY.value;
		//render the data uncompressed.
		int[] uncompressed = proxy.renderAsPackedInt(pDef);
		byte[] compressed = proxy.renderCompressed(pDef);
		//Create a buffered image
		ByteArrayInputStream stream = new ByteArrayInputStream(compressed);
		BufferedImage image = ImageIO.read(stream);
		//pain the image
		proxy.close();
	}
	
	/**
	 * Retrieves the thumbnails
	 * 
	 */
	private void retrieveThumbnails()
		throws Exception
	{
		ThumbnailStorePrx store = entryUnencrypted.createThumbnailStore();
		PixelsData pixels = image.getDefaultPixels();
		Map<Long, byte[]> map = store.getThumbnailByLongestSideSet(
				omero.rtypes.rint(96), Arrays.asList(pixels.getId()));
		Entry entry;
		Iterator i = map.entrySet().iterator();
		ByteArrayInputStream stream;
		//Create a buffered image to display
		Map<Long, BufferedImage> results = new HashMap<Long, BufferedImage>();
		while (i.hasNext()) {
			entry = (Entry) i.next();
			stream = new ByteArrayInputStream((byte[]) entry.getValue());
			results.put((Long) entry.getKey(), ImageIO.read(stream));
			
		}
		//convert the byte array and display.
		
		store.close();
	}
	
	/**
	 * Connects and invokes the various methods.
	 */
	RenderImages()
	{
		try {
			connect();
			loadImage();
			createRenderingEngine();
			retrieveThumbnails();
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
		new RenderImages();
		System.exit(0);
	}

}
