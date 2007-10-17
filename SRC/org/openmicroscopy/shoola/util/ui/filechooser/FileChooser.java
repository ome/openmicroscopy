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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.RegExFactory;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Default Dialog used to display a file chooser.
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

	
	/** Bound property indicating that the cancel button is pressed. */
	public static final String	APPROVE_SELECTION_PROPERTY = "approveSelection";
	
	/** Bound property indicating that the cancel button is pressed. */
	public static final String	CANCEL_SELECTION_PROPERTY = "cancelSelection";
	
	/** 
	 * Bound property indicating the directory where to save the original files.
	 */
	public static final String	LOCATION_PROPERTY = "location";
	
	/** The user has selected to see the load dialog. */
	public static final int 	LOAD = 0;
	
	/** The user has selected to see the save dialog. */
	public static final int 	SAVE = 1;
	
	/** The user has selected to see the load dialog. */
	public static final int 	FOLDER_CHOOSER = 2;
	
	/** Indicates to add the button to the left of the controls. */
	public static final int 	LEFT = 100;
	
	/** Indicates to add the button to the center of the controls. */
	public static final int 	CENTER = 101;
	
	/** Indicates to add the button to the right of the controls. */
	public static final int 	RIGHT = 102;
	
	/** which approval option the user chose. */
	private int option;
	
    /** The UI delegate. */
    private FileSaverUI      	uiDelegate;
       
    /** One of the constants defined by this class. */
    private int					dialogType;
    
    /** Title for the save dialog. */
    private String 				title;
    
    /** Message at top of dialog window. */
    private String 				message;
    
    /** 
     * The filterList containing a list of all the filters to use in the file
     * chooser. 
     */
    private List<FileFilter>	filterList;
    
    
    /** Sets the properties of the dialog. */
    private void setProperties()
    {
    	setTitle(title);
        setModal(true);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
		
			/** 
			 * Cancels the selection.
			 * @see WindowAdapter#windowClosing(WindowEvent)
			 */
			public void windowClosing(WindowEvent e) {
				cancelSelection();
			}
		});
    }
    
    /**
     * Controls if the passed window type is supported.
     * 
     * @param v The value to check.
     */
    private void checkType(int v)
    {
    	switch (v) {
			case SAVE:
			case LOAD:
			case FOLDER_CHOOSER:
				return;
			default:
				throw new IllegalArgumentException("Type not supported");
		}
    }
    
    /**
     * Returns the name of the image.
     * 
     * @param originalName The name to handle.
     * @return See above.
     */
    private String getPartialName(String originalName)
    { 
    	String sep = File.separator;
    	String name = originalName;
    	String[] l;
    	if (Pattern.compile(sep).matcher(originalName).find()) {
            l = originalName.split(sep, 0);
            int n = l.length;
            if (n >= 1) name = l[n-1]; 
        }
    	if (Pattern.compile(".").matcher(name).find()) {
    		l = name.split("\\.");
    		if (l.length >= 1) {
    			name = "";
    			int n = l.length-1;
        		for (int i = 0; i < n; i++) {
    				name += l[i];
    				if (i < (n-1)) name += ".";
    			}
    		}
    	}
        return name;
    }
    
    /**
     * Creates a new instance.
     * 
     * @param owner 		The owner of this dialog.
     * @param dialogType	One of the constants defined by this class.
     * @param title 		Title of the dialog.
     * @param message 		Message of the dialog.
     * @param filterList 	The list of filters.
     */
    public FileChooser(JFrame owner, int dialogType, String title, 
    					String message, List<FileFilter> filterList)
    {
        super(owner);
        checkType(dialogType);
        this.dialogType = dialogType;
        this.title = title;
        this.message = message;
        this.filterList = filterList;
        setProperties();
       	uiDelegate = new FileSaverUI(this);
        pack();
    }
    
    /**
     * Creates a new instance.
     * 
     * @param owner 		The owner of this dialog.
     * @param dialogType	One of the constants defined by this class.
     * @param title 		Title of the dialog.
     * @param message 		Message of the dialog.
     */
    public FileChooser(JFrame owner, int dialogType, String title, 
    					String message)
    {
        this(owner, dialogType, title, message, null);
    }
    
    /**
     * Returns the type.
     * 
     * @return See above.
     */
    int getDialogType() { return dialogType; }
    
    /** 
     * Returns the message to the dialog.
     * 
     * @return See above.
     */
    String getNote() { return message; }
    
    /**
     * Returns the list of filters.
     * 
     * @return See above.
     */
    List<FileFilter> getFilterList() { return filterList; }
   
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
    
    /** Closes the window and disposes. */
    void cancelSelection()
    {
    	firePropertyChange(CANCEL_SELECTION_PROPERTY, Boolean.FALSE, 
    						Boolean.TRUE);
    	option = JFileChooser.CANCEL_OPTION;
    	setVisible(false);
    	dispose();
    }
    
    /** Saves the file. */
    void acceptSelection()
    {
    	firePropertyChange(APPROVE_SELECTION_PROPERTY, Boolean.FALSE, 
							Boolean.TRUE);
        if (uiDelegate.isSetDefaultFolder())
        	UIUtilities.setDefaultFolder(
        			uiDelegate.getCurrentDirectory().toString());
    	option = JFileChooser.APPROVE_OPTION;
    	setVisible(false);
    }
    
    /**
	 * Fires a property indicating where to save the archived files.
	 * 
	 * @param path	The path to the directory.
	 */
	void setFolderPath(String path)
	{
		if (path == null) return;
		char separator = File.separatorChar;
		firePropertyChange(LOCATION_PROPERTY, null, path+separator);
		if (uiDelegate.isSetDefaultFolder()) 
			UIUtilities.setDefaultFolder(path);
		setVisible(false);
    	dispose();	
	}
	
    /** Brings up on screen the dialog asking a <code>Yes/No</code>. */
    void setSelection()
    {
    	IconManager im = IconManager.getInstance();
        FileSaverDialog d = new FileSaverDialog(this, 
                					im.getIcon(IconManager.QUESTION));
        UIUtilities.centerAndShow(d);
    }
    
    /**
     * Sets the name of the file to save.
     * 
     * @param name The name to set.
     */
    public void setSelectedFile(String name) 
    { 
    	if (name == null || name.trim().length() == 0)
    		throw new IllegalArgumentException("File name not valid.");
    	uiDelegate.setSelectedFile(new File(getPartialName(name)));
    }
    
    /**
     * Sets the name of the file to save.
     * 
     * @param name The name to set.
     */
    public void setSelectedFile(File name) 
    { 
    	if (name == null)
    		throw new IllegalArgumentException("File cannot be null.");
    	System.err.println(name);
    	uiDelegate.setSelectedFile(name);
    }
    
    /**
     * Sets the name of the directory  to save.
     * 
     * @param dir The name to set.
     */
    public void setCurrentDirectory(String dir) 
    { 
    	if (dir == null || dir.trim().length() == 0)
    		throw new IllegalArgumentException("Folder name not valid.");
    	uiDelegate.setCurrentDirectory(new File(dir));
    }

    /**
     * Sets the name of the directory  to save.
     * 
     * @param dir The name to set.
     */
    public void setCurrentDirectory(File dir) 
    { 
    	if (dir == null)
    		throw new IllegalArgumentException("Folder cannot be null.");
    	uiDelegate.setCurrentDirectory(dir);
    }
    
    /**
     * Returns the selected file for the chooser.
     * 
     * @return See above,
     */
    public File getSelectedFile()
    {
    	return uiDelegate.getSelectedFile();
    }

    /**
     * Shows the chooser dialog. 
     * 
     * @return The option selected.
     */
    public int showDialog()
    {
	    UIUtilities.setLocationRelativeToAndShow(this.getParent(), this);
	    return option;
    }

    /**
     * Sets the text of the <code>Approve</code> button.
     * 
     * @param text The value to set.
     */
    public void setApproveButtonText(String text)
    {
    	if (text == null || text.trim().length() == 0) return;
    	uiDelegate.setApproveButtonText(text);
    }

    /**
     * Sets the tooltip text of the <code>Approve</code> button.
     * 
     * @param text The value to set.
     */
    public void setApproveButtonToolTipText(String text)
    {
    	if (text == null || text.trim().length() == 0) return;
    	uiDelegate.setApproveButtonToolTipText(text);
    }
    
    /**
     * Adds the passed button to add to the control.
     * 
     * @param button	The button to add.
     * @param location	The location of the button.
     */
    public void addControlButton(JButton button, int location)
    {
    	if (button == null) 
    		throw new IllegalArgumentException("Button cannot be null.");
    	switch (location) {
			case LEFT:
			case RIGHT:
			case CENTER:
				break;
			default:
				throw new IllegalArgumentException("Location not supported.");
		}
    	uiDelegate.addControlButton(button, location);
    }
    
    /**
     * Adds the passed component to add to the control.
     * 
     * @param component	The component to add.
     */
    public void addComponentToControls(JComponent component)
    {
    	if (component == null) 
    		throw new IllegalArgumentException("The component cannot be null.");
    	uiDelegate.addComponentToControls(component);
    }
    
}
