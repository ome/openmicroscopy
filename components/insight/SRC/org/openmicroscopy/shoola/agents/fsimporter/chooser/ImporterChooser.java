/*
 * org.openmicroscopy.shoola.agents.fsimporter.view.ImporterChooser 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.fsimporter.chooser;


//Java imports
import java.io.File;
import java.util.Iterator;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Customized file chooser.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class ImporterChooser 
	extends JFileChooser
{
	
	/** The text area where to enter the name of the file to save. */
    private JTextField	nameArea;
    
    /** Initiliazes the components composing the display. */
    private void initComponents()
    {
    	nameArea = (JTextField) UIUtilities.findComponent(this, 
    											JTextField.class);
    	if (nameArea != null) {
    		//nameArea.setText(model.getPartialImageName());
    		//nameArea.getDocument().addDocumentListener(this);
    	}
    }
    
	/** 
	 * Initializes the filters. 
	 * @param filters The collection of file filters.
	 */
	private void initFilters(List<FileFilter> filters)
	{
		//setAcceptAllFileFilterUsed(false);
        //setDialogType(SAVE_DIALOG);
        //setFileSelectionMode(FILES_ONLY);
		if (filters != null) {
			ViewerSorter sorter = new ViewerSorter();
			List l = sorter.sort(filters);
			Iterator i = l.iterator();
			while (i.hasNext()) 
				addChoosableFileFilter((FileFilter) i.next());
		}
	}
	
	/** 
	 * Initiliazes the chooser. 
	 * 
	 * @param filters 	The collection of supported filters.
	 */
	private void initialize(List<FileFilter> filters)
	{
		initComponents();
		initFilters(filters);
		setControlButtonsAreShown(false);
		File f = getFileSystemView().getDefaultDirectory();
		if (f != null) setSelectedFile(f);
	}

	private FileSystemView fsv;
	/**
	 * Creates a new instance.
	 * 
	 * @param fsv 		The file system view.
	 * @param filters 	The collection of supported filters.
	 */
	ImporterChooser(FileSystemView fsv, List<FileFilter> filters)
	{
		//super(fsv.getDefaultDirectory(), fsv);
		this.fsv = fsv;
		initialize(filters);
	}

	File getFSDefaultDirectory() { return fsv.getDefaultDirectory(); }
	
	/**
	 * Creates a new instance.
	 * 
	 * @param filters 	The collection of supported filters.
	 */
	ImporterChooser(List<FileFilter> filters)
	{
		super();
		initialize(filters);
	}
	
	/**
	 * Returns the selected directory or <code>null</code> if no directory 
	 * selected.
	 * 
	 * @return See above.
	 */
	File getSelectedDirectory()
	{
		File f = getSelectedFile();
		if (f.isDirectory()) return f;
		return null;
	}
	
}

