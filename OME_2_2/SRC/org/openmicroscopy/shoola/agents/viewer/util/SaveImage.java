/*
 * org.openmicroscopy.shoola.agents.viewer.util.SaveImage
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

package org.openmicroscopy.shoola.agents.viewer.util;


//Java imports
import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.filter.file.TIFFFilter;
import org.openmicroscopy.shoola.util.image.io.TIFFEncoder;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class SaveImage
{
	
	private Registry	registry;
	
	SaveImage(Registry registry, String format, BufferedImage image, 
				String fileName, String message)
	{
		this.registry = registry;
		if (format.equals(TIFFFilter.TIF))
			saveImageAsTIFF(image, fileName, message);
		else
			saveImageAs(format, image, fileName, message);
	}
	
	/**
	 * Save a specified buffered image as a <code>jpeg</code> or 
	 * <code>png</code>.
	 * 
	 * @param format	one the format defined above.
	 * @param img		buffered image to save.
	 * @param fileName	image's name.
	 * @param message	message to display when the image has been saved.
	 */
	private void saveImageAs(String format, BufferedImage img, String fileName, 
							String message)
	{
		UserNotifier un = registry.getUserNotifier();
		if (img == null) 
			un.notifyError("Save image", "No current image displayed");
		File f = new File(fileName);
		try {
			Iterator writers = ImageIO.getImageWritersByFormatName(format);
			ImageWriter writer = (ImageWriter) writers.next();
			ImageOutputStream ios = ImageIO.createImageOutputStream(f);
			writer.setOutput(ios);
			writer.write(img);
			ios.close();
			un.notifyInfo("Image saved", message);
			//TODO: forward event to server.
		} catch (Exception ex) {
			f.delete();
			un.notifyError("Save image failure", "Unable to save the image",
								ex);
		}
	}
	
	/**
	 * Save a specified buffered image as a <code>tif</code>.
	 * 
	 * @param img		buffered image to save.
	 * @param fileName	image's name.
	 * @param message	message to display when the image has been saved.
	 */
	private void saveImageAsTIFF(BufferedImage img, String fileName, 
								String message)
	{
		UserNotifier un = registry.getUserNotifier();
		if (img == null) 
			un.notifyError("Save image", "No current image displayed");
		File f = new File(fileName);	
		try {
			DataOutputStream dos = 
							new DataOutputStream(new FileOutputStream(f));
			TIFFEncoder encoder = new TIFFEncoder(img, dos);
			encoder.write();
			dos.close();
			un.notifyInfo("Image saved", message);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println(f.delete());
			un.notifyError("Save image failure", "Unable to save the image", 
							ex);					
		}
	}	
	
}
