/*
 * org.openmicroscopy.shoola.agents.viewer.util.ImageSaver
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
import java.io.File;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.ViewerCtrl;
import org.openmicroscopy.shoola.env.ui.UserNotifier;

/** 
 * Save the current image.
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
public class ImageSaver
	extends JFileChooser
{
	private static final String 	JPEG = "jpeg";
	private static final String 	JPG = "jpg";
	private static final String 	PNG = "png";
	
	private ViewerCtrl				controller;
	public ImageSaver(ViewerCtrl controller)
	{
		this.controller = controller;
		createChooser();
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
		UserNotifier un = controller.getRegistry().getUserNotifier();
		if (img == null) 
			un.notifyError("Save image", "No current image displayed");
		try {
			Iterator writers = ImageIO.getImageWritersByFormatName(format);
			ImageWriter writer = (ImageWriter) writers.next();
			File f = new File(fileName);
			ImageOutputStream ios = ImageIO.createImageOutputStream(f);
			writer.setOutput(ios);
			writer.write(img);
			ios.close();
			un.notifyInfo("Image saved", message);
			//TODO: forward event to server.
		} catch (Exception ex){
			un.notifyError("Save image failure", "Unable to save the image",
								ex);
		}
	}
	
	/** Build the file chooser. */
	private void createChooser()
	{ 
		setFileSelectionMode(FILES_ONLY);
		JpegFilter jpegFilter = new JpegFilter();
		setFileFilter(jpegFilter);
		PngFilter pngFilter = new PngFilter();
		addChoosableFileFilter(pngFilter); 
		setFileFilter(pngFilter);
		setAcceptAllFileFilterUsed(false);
		int returnVal = showDialog(controller.getReferenceFrame(), 
									"Save Image");
		
		//Process the result
		File file = getSelectedFile();
		if (file != null && returnVal == JFileChooser.APPROVE_OPTION) {
			String format = PNG;
			if (getFileFilter() instanceof JpegFilter) format = JPG;
			String  fileName = file.getAbsolutePath()+"."+format, 
					name = file.getName()+"."+format;;
			String message = "The image "+name+", has been saved in \n"
							+getCurrentDirectory();
			saveImageAs(format, controller.getBufferedImage(), fileName, 
						message);
			setSelectedFile(null);
		}      
	}
	
	/** 
	 * Filter the files which extension is <code>jpeg</code> or 
	 * <code>jpg</code>.
	 */
	static class JpegFilter extends FileFilter
	{
		private String description = JPEG;
		
		public String getDescription()
		{
			return description;
		}
		
		public boolean accept(File f)
		{
			if (f.isDirectory()) return true;
			String s = f.getName();
			String extension = null;
			int i = s.lastIndexOf('.');
			if (i > 0 && i < s.length()-1)
				extension = s.substring(i+1).toLowerCase();
			if (extension != null) {
				boolean b = false;
				if (extension.equals(JPEG) || extension.equals(JPG)) b =  true;
				return b;
			}
			return false;
		}
	}
	
	/** 
	 * Filter the files which extension is <code>png</code> or 
	 * <code>jpg</code>.
	 */
	static class PngFilter extends FileFilter
	{
		private String description = PNG;
	
		public String getDescription()
		{
			return description;
		}
		
		public boolean accept(File f)
		{
			if (f.isDirectory()) return true;
			String s = f.getName();
			String extension = null;
			int i = s.lastIndexOf('.');
			if (i > 0 && i < s.length()-1)
				extension = s.substring(i+1).toLowerCase();
			if (extension != null) {
				boolean b = false;
				if (extension.equals(PNG)) b = true;
				return b;
			}
			return false;
		}
	}
	
}
