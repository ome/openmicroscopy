/*
 * training.RenderImages
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee & Open Microscopy Environment.
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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.imageio.ImageIO;


//Third-party libraries

import omero.api.IContainerPrx;
//Application-internal dependencies
import omero.api.RenderingEnginePrx;
import omero.api.ThumbnailStorePrx;
import omero.model.Image;
import omero.romio.PlaneDef;
import omero.sys.ParametersI;
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
{

	//The value used if the configuration file is not used. To edit*/
	/** The server address.*/
	private String hostName = "serverName";

	/** The username.*/
	private String userName = "userName";
	
	/** The password.*/
	private String password = "password";
	
	/** Information to edit.*/
	private long imageId = 1;
	//end edit
	
	private ImageData image;
	
	/** Reference to the connector.*/
	private Connector connector;
	
	/**
	 * Loads the image.
	 * 
	 * @param imageID The id of the image to load.
	 * @return See above.
	 */
	private ImageData loadImage(long imageID)
		throws Exception
	{
		IContainerPrx proxy = connector.getContainerService();
		List<Image> results = proxy.getImages(Image.class.getName(),
				Arrays.asList(imageID), new ParametersI());
		//You can directly interact with the IObject or the Pojos object.
		//Follow interaction with the Pojos.
		if (results.size() == 0)
			throw new Exception("Image does not exist. Check ID.");
		return new ImageData(results.get(0));
	}
	
	/**
	 * Creates a rendering engine.
	 */
	private void createRenderingEngine()
		throws Exception
	{
		PixelsData pixels = image.getDefaultPixels();
		long pixelsId = pixels.getId();
		RenderingEnginePrx proxy = null;
		try {
			proxy = connector.getRenderingEngine();
			proxy.lookupPixels(pixelsId);
			if (!(proxy.lookupRenderingDef(pixelsId))) {
				proxy.resetDefaultSettings(true);
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
			System.err.println(image.getWidth()+" "+image.getHeight());
		} catch (Exception e) {
			throw new Exception("Cannot render image", e);
		} finally {
			if (proxy != null) proxy.close();
		}
	}
	
	/**
	 * Retrieves the thumbnails
	 * 
	 */
	private void retrieveThumbnails()
		throws Exception
	{
		ThumbnailStorePrx store = null;
		try {
			store = connector.getThumbnailStore();
			PixelsData pixels = image.getDefaultPixels();
			Map<Long, byte[]> map = store.getThumbnailByLongestSideSet(
					omero.rtypes.rint(96), Arrays.asList(pixels.getId()));
			Entry<Long, byte[]> entry;
			Iterator<Entry<Long, byte[]>> i = map.entrySet().iterator();
			ByteArrayInputStream stream;
			//Create a buffered image to display
			Map<Long, BufferedImage> results = new HashMap<Long, BufferedImage>();
			while (i.hasNext()) {
				entry = i.next();
				stream = new ByteArrayInputStream(entry.getValue());
				results.put(entry.getKey(), ImageIO.read(stream));
				
			}
		} catch (Exception e) {
			throw new Exception("Cannot retrieve thumnails", e);
		} finally {
			if (store != null) store.close();
		}
	}
	
	/**
	 * Connects and invokes the various methods.
	 * 
	 * @param info The configuration information.
	 */
	RenderImages(ConfigurationInfo info)
	{
		if (info == null) {
			info = new ConfigurationInfo();
			info.setHostName(hostName);
			info.setPassword(password);
			info.setUserName(userName);
			info.setImageId(imageId);
		}
		connector = new Connector(info);
		try {
			connector.connect();
			image = loadImage(info.getImageId());
			createRenderingEngine();
			retrieveThumbnails();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				connector.disconnect(); // Be sure to disconnect
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Runs the script without configuration options.
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		new RenderImages(null);
		System.exit(0);
	}

}
