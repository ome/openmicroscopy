/*
 * org.openmicroscopy.shoola.agents.imviewer.util.saver.ImgSaverSelectionDialog
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.agents.imviewer.util.saver;



//Java imports
import javax.swing.Icon;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.OptionsDialog;


/** 
 * Brings up the preview image widget if the answer to the question is 
 * <code>yes</code>.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class ImgSaverSelectionDialog
    extends OptionsDialog
{
    
    /** The message when an image with the name and extension aleady exists. */
    private static final String MESSAGE = "A file with the same name and \n" +
                                "extension already exists in this " +
                                "directory.\n " +
                                "Do you really want to save the image?";
    
    /** Reference to the {@link ImgSaver}. */
    private ImgSaver    model;
    
    /** 
     * One of the following constant: 
     * {@link ImgSaver#DIRECT} or 
     * {@link ImgSaver#PREVIEW}.
     */
    private int			index;
    
    /**
     * Overridden to bring up the preview image widget.
     * @see OptionsDialog#onYesSelection()
     */
    protected void onYesSelection()
    { 
    	setVisible(false);
    	switch (index) {
			case ImgSaver.DIRECT:
				model.saveImage(true);
				break;
			case ImgSaver.PREVIEW:
				model.previewImage(); 
				break;
		}
    	
    	dispose();
    }
    
    /**
     * Creates a new instance.
     * 
     * @param parent    The parent of this dialog.
     * @param icon      The icon displayed next to the message.
     * @param index		The index of the saving type.
     */
    ImgSaverSelectionDialog(ImgSaver parent, Icon icon, int index)
    {
        super(parent, "Save Image", MESSAGE, icon);
        if (parent == null) throw new IllegalArgumentException("No model.");
        model = parent;
        this.index = index;
    }

}
