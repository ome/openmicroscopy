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
import javax.swing.JFileChooser;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.IconManager;
import org.openmicroscopy.shoola.agents.viewer.ViewerCtrl;
import org.openmicroscopy.shoola.util.filter.file.JPEGFilter;
import org.openmicroscopy.shoola.util.filter.file.PNGFilter;
import org.openmicroscopy.shoola.util.filter.file.TIFFFilter;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

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
	
	static String 					MESSAGE = "A file with the same name and " +
												"extension already exists in " +
												"this directory. Do you " +
												"still want to save the image?";
	
	static String					TITLE = "Save Image";
	
	/** Default extension format. */
	private static final String		DEFAULT_FORMAT = TIFFFilter.TIF;
	
	/** Reference to the {@link ViewerCtrl controller}. */
	private ViewerCtrl				controller;
	
	private IconManager				im;
	
	/** 
	 * Control used to display or not the fileChooser, when the dialog is shown.
	 */
	private boolean					display;
	
	public ImageSaver(ViewerCtrl controller)
	{
		this.controller = controller;
		im = IconManager.getInstance(controller.getRegistry());
		display = false;
		createChooser();
	}

	void isDisplay(boolean b) { display = b; }
	
	ViewerCtrl getController() { return controller; }
	
	BufferedImage getBufferedImage() { return controller.getBufferedImage(); }
	
	/** Build the file chooser. */
	private void createChooser()
	{ 
		setFileSelectionMode(FILES_ONLY);
		JPEGFilter jpegFilter = new JPEGFilter();
		setFileFilter(jpegFilter);
		addChoosableFileFilter(jpegFilter); 
		PNGFilter pngFilter = new PNGFilter();
		addChoosableFileFilter(pngFilter); 
		setFileFilter(pngFilter);
		TIFFFilter tiffFilter = new TIFFFilter();
		setFileFilter(tiffFilter);
		addChoosableFileFilter(tiffFilter); 
		setAcceptAllFileFilterUsed(false);
		showDialog(controller.getReferenceFrame(), "Save Image");
	}
	
	/** Override the approveSelection method. */
	public void approveSelection()
	{
		File file = getSelectedFile();
		if (file != null) {
			String format = DEFAULT_FORMAT;
			if (getFileFilter() instanceof JPEGFilter) 
				format = JPEGFilter.JPG;
			else if (getFileFilter() instanceof TIFFFilter) 
				format = TIFFFilter.TIF;
			else if (getFileFilter() instanceof PNGFilter) 
				format = PNGFilter.PNG;
				
			String  fileName = file.getAbsolutePath()+"."+format, 
					name = file.getName()+"."+format;
			String message = "The image "+name+", has been saved in \n"
							+getCurrentDirectory();
			setSelection(format, fileName, message, 
								getCurrentDirectory().listFiles());
			setSelectedFile(null);
			if (display) return;	
		}      
		// No file selected, or file can be written - let OK action continue
		super.approveSelection();
	}
	
	/** 
	 * Check if the fileName specified already exists if not the image is saved
	 * in the specified format.
	 * 
	 * @param format		format selected <code>jpeg<code>, 
	 * 						<code>png<code> or <code>tif<code>.
	 * @param fileName		image's name.
	 * @param message		message displayed after the image has been created.
	 * @param list			lis of files in the current directory.
	 */
	private void setSelection(String format, String fileName, String message,
								File[] list)
	{
		boolean exist = false;
		for (int i = 0; i < list.length; i++)
			if ((list[i].getAbsolutePath()).equals(fileName)) exist = true;
			
		if (exist) {
			SelectionDialog dialog = new SelectionDialog(this, format, fileName,
									 message, im.getIcon(IconManager.QUESTION));
			dialog.pack();	
			UIUtilities.centerAndShow(dialog);
		} else {
			display = false;
			new SaveImage(controller.getRegistry(), format, 
						controller.getBufferedImage(), fileName, message);
		}				
	}
	
}
