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
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.ViewerCtrl;
import org.openmicroscopy.shoola.util.filter.file.JPEGFilter;
import org.openmicroscopy.shoola.util.filter.file.PNGFilter;
import org.openmicroscopy.shoola.util.filter.file.TIFFFilter;

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
	/** Default extension format. */
	private static final String		DEFAULT_FORMAT = TIFFFilter.TIF;
	private ViewerCtrl				controller;
	
	/** 
	 * Control to display or not the fileChooser, when we pop up the dialog 
	 * widget.
	 */
	private boolean					display;
	
	public ImageSaver(ViewerCtrl controller)
	{
		this.controller = controller;
		display = false;
		createChooser();
	}

	void isDisplay(boolean b)
	{
		display = b;
	}
	
	ViewerCtrl getController()
	{
		return controller;
	}
	
	BufferedImage getBufferedImage()
	{
		return controller.getBufferedImage();
	}
	
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
			showDialog(new SelectionDialog(this, format, fileName, message));
		} else {
			display = false;
			new SaveImage(controller.getRegistry(), format, 
						controller.getBufferedImage(), fileName, message);
		}				
	}
	
	/** 
	 * Sizes, centers and brings up the specified editor dialog.
	 *
	 * @param editor	The editor dialog.
	 */
	void showDialog(JDialog editor)
	{
		JFrame topFrame = (JFrame) controller.getReferenceFrame();
		Rectangle tfB = topFrame.getBounds(), psB = editor.getBounds();
		int offsetX = (tfB.width-psB.width)/2, 
			offsetY = (tfB.height-psB.height)/2;
		if (offsetX < 0)	offsetX = 0;
		if (offsetY < 0)	offsetY = 0;
		editor.setLocation(tfB.x+offsetX, tfB.y+offsetY);
		editor.setVisible(true);
	}
	
}
