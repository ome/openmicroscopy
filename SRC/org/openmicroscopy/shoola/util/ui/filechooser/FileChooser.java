/*
 * org.openmicroscopy.shoola.util.ui.filechooser.FileSaver 
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
import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.imviewer.IconManager;
import org.openmicroscopy.shoola.util.ui.RegExFactory;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * 
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
public class FileChooser
    extends JDialog
{
	
	/** The user has selected to see the save dialog. */
	public static final int 	SAVE = 1;

	/** The user has selected to see the load dialog. */
	public static final int 	LOAD = 0;
	
	/** which approval option the user chose. */
	private int option;
	
    /** The UI delegate. */
    private FileSaverUI      uiDelegate;
        
    
    /** Title for the save dialog. */
    private String 			title;
    
    /** Message at top of dialog window. */
    private String 			message;
    
    /** The filterList containing a list of all the filters to use in the file
     * chooser. 
     */
    private ArrayList<FileFilter> filterList;
    
    
    /** Sets the properties of the dialog. */
    private void setProperties()
    {
    	setTitle(title);
        setModal(true);
    }
    
    /** 
     * Return the message to the dialog.
     * @return see above.
     */
    public String getNote()
    {
    	return message;
    }
    
    /**
     * Get the filter list.
     * @return see above.
     */
    public ArrayList<FileFilter> getFilterList()
    {
    	return filterList;
    }
    
    /**
     * Creates a new instance.
     * 
     * @param owner 		The owner of this dialog.
     * @param save			is this a save dialog.
     * @param title 		Title of the dialog.
     * @param message 		Message of the dialog.
     * @param filterList 	One of the constants defined by this class.
     */
    public FileChooser(JFrame owner, int save, String title, String message, ArrayList<FileFilter> filterList)
    {
        super(owner);
        this.title = title;
        this.message = message;
        this.filterList = filterList;
        setProperties();
        if(save!= SAVE && save != LOAD)
        	throw new IllegalArgumentException("Save param must be SAVE or LOAD.");
       	uiDelegate = new FileSaverUI(save==SAVE, this);
        pack();
    }
    
    /**
     * Adds the extension to the passed name if necessary.
     * 
     * @param name		The name to handle.
     * @param format	The selected file format.
     * @return See above.
     */
    String getExtendedName(String name, String format)
    {
    	String extension = "."+format;
    	Pattern pattern = RegExFactory.createPattern(extension);
    	String n;
    	if (RegExFactory.find(pattern, name)) {
    		n = name;
    	} else {
    		pattern = RegExFactory.createCaseInsensitivePattern(extension);
    		if (RegExFactory.find(pattern, name)) n = name;
    		else n = name + "." + format;
    	}
    	return n;
    }
    
    /**
     * Sets the name of the file to save.
     * 
     * @param name The name to set.
     */
    public void setSelectedFile(String name) 
    { 
    	uiDelegate.setSelectedFile(new File(name));
    }
    
    /**
     * Sets the name of the file to save.
     * 
     * @param name The name to set.
     */
    public void setSelectedFile(File name) 
    { 
    	uiDelegate.setSelectedFile(name);
    }
    /**
     * Sets the name of the directory  to save.
     * 
     * @param dir The name to set.
     */
    public void setCurrentDirectory(String dir) 
    { 
    	uiDelegate.setCurrentDirectory(new File(dir));
    }

    /**
     * Sets the name of the directory  to save.
     * 
     * @param dir The name to set.
     */
    public void setCurrentDirectory(File dir) 
    { 
    	uiDelegate.setCurrentDirectory(dir);
    }
       
    /**
     * Brings up on screen the dialog asking a <code>Yes/No</code>.
     * 
     * @param index One of the constants defined by this class.
     */
    void setSelection(int index)
    {
    	IconManager im = IconManager.getInstance();
        FileSaverDialog d = new FileSaverDialog(this, 
                im.getIcon(IconManager.QUESTION));
        UIUtilities.centerAndShow(d);
    }
    
    /**
     * Get the selcted file for the chooser.
     * @return see above,
     */
    public File getSelectedFile()
    {
    	return uiDelegate.getSelectedFile();
    }
    
    /** Closes the window and disposes. */
    void cancelSelection()
    {
    	option = JFileChooser.CANCEL_OPTION;
    	setVisible(false);
    }

    /**
     * Show the chooser dialog. 
     * @return the option selected.
     */
    public int showDialog()
    {
	    UIUtilities.setLocationRelativeToAndShow(this.getParent(), this);
	    return option;
    }
    
    /** 
     * Saves the file. 
     * 
     */
    void acceptSelection()
    {
    	//TODO: Do stuff
    	
        if (uiDelegate.isSetDefaultFolder())
        	UIUtilities.setDefaultFolder(uiDelegate.getCurrentDirectory().toString());
    	option = JFileChooser.APPROVE_OPTION;
    	setVisible(false);
    }

   
}
