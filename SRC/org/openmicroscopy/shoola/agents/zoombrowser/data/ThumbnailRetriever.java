/*
 * org.openmicroscopy.shoola.agents.zoombrowser.data.ThumbnailRetriever
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

package org.openmicroscopy.shoola.agents.zoombrowser.data;

//Java imports

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.ds.st.Pixels;
import org.openmicroscopy.is.ImageServerException;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.PixelsService;
import org.openmicroscopy.shoola.env.Environment;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.ui.UserNotifier;

/** 
 * A class for managing cached retrieval of thumbnails.
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class ThumbnailRetriever {
	
	private static final String CACHE_DIR="/.ome-vis-cache/";
	private static final String IMAGE_DIR="/images";
	
	/** the registry that we'll use */
	private Registry registry;
	
	/** The Pixels Service */
	private PixelsService ps;
	/** path to cache */
	String cachePath=null;
	
	public ThumbnailRetriever(Registry registry) {
		this.registry = registry;
		Environment env = (Environment) registry.lookup(LookupNames.ENV);
		ps = registry.getPixelsService();
		if (env != null) {
			URL url = env.getOMEDSAddress();
			String host = url.getHost();
			String hostpath = CACHE_DIR+host+IMAGE_DIR;
			cachePath = env.resolvePathName(hostpath);
			File cacheFile = new File(cachePath);
			try {
				cacheFile.mkdirs();
			}
			catch (Exception e) {
			} 
		}
	}
	
	private File getImageFile(BrowserImageSummary image) {
		// nothing if no cache..
		if (cachePath == null)
			return null;
		
		long omeisID = image.getDefaultPixels().getImageServerID();

		String imageFileName = new String("thumb-"+omeisID+".png");
		File imageFile = new File(cachePath,imageFileName);
		return imageFile;
	}
	
	private BufferedImage getCachedImage(File imageFile) {
				BufferedImage bufImage=null;
		//if (imageFile.exists()) {
			try {
				bufImage = ImageIO.read(imageFile);
			} catch(Exception e) {
					// don't need to check to see if it exists - will throw 
				     // an exception if it doesn't.
					bufImage = null;
			}
		//}
		return bufImage;
	
	}
		
	long cacheElapsed=0;
	long getFileElapsed = 0;
	long isElapsed;
	long cacheWriteElapsed;
	int count;
	public BufferedImage getImage(BrowserImageSummary image) {
		
		BufferedImage im=null;
		long getFileStart = System.currentTimeMillis();
		File imageFile = getImageFile(image);
		getFileElapsed += System.currentTimeMillis()-getFileStart;
		if (imageFile != null) {
			long start = System.currentTimeMillis();
			im = getCachedImage(imageFile);
			cacheElapsed += System.currentTimeMillis()-start;
			count++;
			if (im != null) {
				return im;
			}
		}
			
		// else, not in cache
		try {
			Pixels pix = image.getDefaultPixels().getPixels();
			
			long start = System.currentTimeMillis();
			im = ps.getThumbnail(pix);
			isElapsed += System.currentTimeMillis()-start;
			count++;
			
			if (imageFile != null) {
				// write to cache
				start = System.currentTimeMillis();
				if (im != null)
					ImageIO.write(im,"png",imageFile);
				cacheWriteElapsed += System.currentTimeMillis()-start;
			}
			return im;
		}
		catch(ImageServerException ise) {
			UserNotifier un = registry.getUserNotifier();
			un.notifyError("ImageServer Error",ise.getMessage(),ise);
			return null;
		}
		catch(IOException ioe) {
			UserNotifier un = registry.getUserNotifier();
			un.notifyError("Thumbnail Cache Error","Can't cache thumbnail");
			return im;
		}
	}
	
	public void dumpTime() {
		System.err.println("get file time .."+getFileElapsed);
		System.err.println("thumbnail cache read time .."+cacheElapsed);
		System.err.println("image server time "+isElapsed);
		System.err.println("cache write elapsed "+cacheWriteElapsed);
	}
	
}
