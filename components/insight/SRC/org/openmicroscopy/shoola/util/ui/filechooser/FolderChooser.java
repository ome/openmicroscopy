/*
 * org.openmicroscopy.shoola.util.ui.filechooser.FolderChooser 
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
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Chooser to select a folder
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class FolderChooser 	
	extends GenericFileChooser
{

	/** Reference to the Model. */
	private FolderChooserDialog model;
	
	/** Initializes the folder chooser. */
	private void initialize()
	{
		JLabel label = (JLabel) UIUtilities.findComponent(this, JLabel.class);
		
		if (label != null) {
			label.setText("Save In:");
		}
		JTextField field = (JTextField) UIUtilities.findComponent(this, 
													JTextField.class);
		if (field != null) {
			field.setEditable(false);
		}
		setAcceptAllFileFilterUsed(false);
		setDialogType(CUSTOM_DIALOG);
        setFileSelectionMode(DIRECTORIES_ONLY);
        setApproveButtonText("Download");
        setApproveButtonToolTipText(FolderChooserDialog.TITLE);
        try {
			File f = UIUtilities.getDefaultFolder();
			if (f != null) setCurrentDirectory(f);
		} catch (Exception ex) {}
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param model Reference to the Model. Mustn't be <code>null</code>.
	 */
	FolderChooser(FolderChooserDialog model)
	{
		if (model == null) throw new NullPointerException("No model.");
		this.model = model;
		initialize();
	}
	
	 /**
     * Overridden to close the model when the selection is cancelled.
     * @see JFileChooser#cancelSelection()
     */
    public void cancelSelection()
    { 
    	model.close(); 
    	super.cancelSelection();
    }
    
    /**
     * Overridden to set the directory
     * @see JFileChooser#approveSelection()
     */
    public void approveSelection()
    {
        File file = getSelectedFile();
        if (file != null) model.setFolderPath(file.getAbsolutePath());    
        // No file selected, or file can be written - let OK action continue
        super.approveSelection();
    }
	
}
