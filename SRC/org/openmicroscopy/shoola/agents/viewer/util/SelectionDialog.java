/*
 * org.openmicroscopy.shoola.agents.viewer.util.SelectionDialog
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
import javax.swing.Icon;
import javax.swing.JDialog;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.OptionsDialog;

/** 
 * Dialog widget to give the user the choice to save or not the image with the
 * specified name. Note that this dialog only pops up if a file with the same 
 * name and extension already exists in the current directory.
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
class SelectionDialog
	extends OptionsDialog
{
	
	/** Reference to the parent. */							
	private ImageSaver 				parentDialog;

	protected String				format, fileName, message;
	
	SelectionDialog (ImageSaver parentDialog, String format, String fileName, 
					 String message, Icon messageIcon) 
	{
		super((JDialog) parentDialog, ImageSaver.TITLE, ImageSaver.MESSAGE, 
		messageIcon);
		this.parentDialog = parentDialog;
		this.format = format;
		this.fileName = fileName;
		this.message = message;
	}

	/** overrides the {@link #onNoSelection() onNoSelection} method. */
	protected void onNoSelection() { parentDialog.isDisplay(true); }
	
	/** overrides the {@link #onYesSelection() onYesSelection} method. */
	protected void onYesSelection()
	{
		parentDialog.isDisplay(false);
		new SaveImage(parentDialog.getController().getRegistry(), format, 
				parentDialog.getBufferedImage(), fileName, message);
		parentDialog.setVisible(false);
		parentDialog.dispose();
	}
	
}
