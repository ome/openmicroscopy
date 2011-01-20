/*
 * org.openmicroscopy.shoola.util.ui.filechooser.FileSaverDialog 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.ui.filechooser;


//Java imports
import javax.swing.Icon;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.OptionsDialog;

/** 
 * Dialog used to ask a question.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class FileSaverDialog
    extends OptionsDialog
{
    
    /** The message when an image with the name and extension aleady exists. */
    private static final String MESSAGE = "A file with the same name and " +
                                "extension already exists in this directory. " +
                                "Do you really want to save the image?";
    
    /** Reference to the model. */
    private FileChooser    model;
        
    /**
     * Overridden to bring up the preview image widget.
     * @see OptionsDialog#onYesSelection()
     */
    protected void onYesSelection()
    { 
    	setVisible(false);
    	model.acceptSelection();
    	dispose();
    }
    
    /**
     * Creates a new instance.
     * 
     * @param parent    The parent of this dialog.
     * @param icon      The icon displayed next to the message.
     */
    FileSaverDialog(FileChooser parent, Icon icon)
    {
        super(parent, "Save Image", MESSAGE, icon);
        if (parent == null) throw new IllegalArgumentException("No model.");
        model = parent;
    }

}

