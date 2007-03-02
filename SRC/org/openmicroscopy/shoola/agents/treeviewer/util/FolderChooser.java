/*
 * org.openmicroscopy.shoola.agents.treeviewer.util.FolderChooser
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
package org.openmicroscopy.shoola.agents.treeviewer.util;


//Java imports
import java.io.File;
import javax.swing.JFileChooser;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.util.saver.ImgSaver;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * 
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
	extends JFileChooser
{

	/** Reference to the Model. */
	private FolderChooserDialog model;
	
	/** Initializes the folder chooser. */
	private void initialize()
	{
		setAcceptAllFileFilterUsed(false);
		setDialogType(SAVE_DIALOG);
        setFileSelectionMode(DIRECTORIES_ONLY);
        setApproveButtonText("Download");
        String s = UIUtilities.getDefaultFolderAsString();
        if (s == null) return;
        String last;
        String separator = File.separator;
        String[] elements = s.split(separator);
        int n = elements.length-1;

        if (n > 0) {
        	String path = "";
        	last = elements[n];
        	int index = 0;
        	for (int i = 0; i < n; i++) {
        		path += elements[i];
        		if (index != (n-1)) path += separator;
        		index++;
        	}
        	File f = new File(path);
        	if (f != null) setCurrentDirectory(f);
        	File selectedFile = new File(last);
        	if (selectedFile != null) setSelectedFile(selectedFile);

        }
        
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param model Reference to the Model. Mustn't be <code>null</code>.
	 */
	FolderChooser(FolderChooserDialog model)
	{
		if (model == null)
			throw new NullPointerException("No model.");
		this.model = model;
		initialize();
	}
	
	 /**
     * Overridden to close the {@link ImgSaver} when the selection is cancelled.
     * @see JFileChooser#cancelSelection()
     */
    public void cancelSelection() { model.close(); }
    
    /**
     * Overridden to set the directory
     * @see JFileChooser#approveSelection()
     */
    public void approveSelection()
    {
        File file = getSelectedFile();
        if (file != null) {
        	model.setFolderPath(file.getAbsolutePath());
        }      
        // No file selected, or file can be written - let OK action continue
        super.approveSelection();
    }
    
	
}
