/*
 * org.openmicroscopy.shoola.agents.fsimporter.view.FSChooser 
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
package org.openmicroscopy.shoola.agents.fsimporter.view;


//Java imports
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.filter.file.BMPFilter;
import org.openmicroscopy.shoola.util.filter.file.CustomizedFileFilter;
import org.openmicroscopy.shoola.util.filter.file.DVFilter;
import org.openmicroscopy.shoola.util.filter.file.JPEGFilter;
import org.openmicroscopy.shoola.util.filter.file.PNGFilter;
import org.openmicroscopy.shoola.util.filter.file.TIFFFilter;
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
 * @since 3.0-Beta4
 */
class FSChooser 
	extends JFileChooser
	implements DocumentListener
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
    		nameArea.getDocument().addDocumentListener(this);
    	}
    }
    
	/** Initializes the filters. */
	private void initFilters()
	{
		setAcceptAllFileFilterUsed(false);
        setDialogType(SAVE_DIALOG);
        setFileSelectionMode(FILES_ONLY);
        addChoosableFileFilter(new DVFilter()); 
        addChoosableFileFilter(new BMPFilter()); 
        addChoosableFileFilter(new JPEGFilter()); 
        PNGFilter filter = new PNGFilter();
        addChoosableFileFilter(filter); 
        addChoosableFileFilter(new TIFFFilter());
        setFileFilter(filter);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param fsv The file system view.
	 */
	FSChooser(FileSystemView fsv)
	{
		if (fsv != null) setFileSystemView(fsv);
		initComponents();
		initFilters();
		setControlButtonsAreShown(false);
	}
	
	/**
	 * Returns all the extension supported.
	 * 
	 * @return See above.
	 */
	List<String> getSupportedExtensions()
	{
		FileFilter[] filters = getChoosableFileFilters();
		List<String> extensions = new ArrayList<String>();
		FileFilter filter;
		String[] ext;
		for (int i = 0; i < filters.length; i++) {
			filter = filters[i];
			if (filter instanceof CustomizedFileFilter) {
				ext = ((CustomizedFileFilter) filter).getExtensions();
				for (int j = 0; j < ext.length; j++) {
					extensions.add(ext[j]);
				}
			}
		}
		return extensions;
	}

	public void changedUpdate(DocumentEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void insertUpdate(DocumentEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void removeUpdate(DocumentEvent e) {
		// TODO Auto-generated method stub
		
	}
    
}
