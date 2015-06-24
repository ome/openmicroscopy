/*
 * org.openmicroscopy.shoola.util.ui.filechooser.FileSaver 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.openmicroscopy.shoola.util.CommonsLangUtils;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.filter.file.CustomizedFileFilter;
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.MessageBox;
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
 * @since OME3.0
 */
public class FileChooser
    extends JDialog
{

    /** The text corresponding to the download action.*/
    public static final String DOWNLOAD_TEXT = "Download";

    /** The text corresponding to the download action.*/
    public static final String DOWNLOAD_DESCRIPTION =
            "Select where to download the file(s).";

    /** Bound property indicating that the cancel button is pressed. */
    public static final String	APPROVE_SELECTION_PROPERTY = "approveSelection";

    /** Bound property indicating that the cancel button is pressed. */
    public static final String	CANCEL_SELECTION_PROPERTY = "cancelSelection";

    /** 
     * Bound property indicating the directory where to save the original files.
     */
    public static final String	LOCATION_PROPERTY = "location";

    /** Identifies the <code>Load</code> dialog. */
    public static final int LOAD = 0;

    /** Identifies the <code>Save</code> dialog. */
    public static final int SAVE = 1;

    /** Identifies the <code>Folder Chooser</code> dialog. */
    public static final int FOLDER_CHOOSER = 2;

    /** Identifies the <code>Import</code> dialog. */
    public static final int IMPORT = 3;

    /** Indicates to add the button to the left of the controls. */
    public static final int LEFT = 100;

    /** Indicates to add the button to the center of the controls. */
    public static final int CENTER = 101;

    /** Indicates to add the button to the right of the controls. */
    public static final int RIGHT = 102;

    /** The approval option the user chose. */
    private int option;

    /** The UI delegate. */
    private FileSaverUI uiDelegate;

    /** One of the constants defined by this class. */
    private int dialogType;

    /** Title for the save dialog. */
    private String title;

    /** Message at top of dialog window. */
    private String message;

    /** Collection of supported filters. */
    private List<FileFilter> filters;

    /** The list of selected paths.*/
    private List<String> paths;

    /** 
     * Path to the folder.
     * Only used when the type is {@link #FOLDER_CHOOSER}.
     */
    private String folderPath;

    /** Ask if a file should be overridden. */
    private boolean checkOverride;

    /** Flag indicate to override the names when saving.*/
    private boolean override;
    
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

            /**
             * Requests focus on name to enable the <code>Approve button</code>.
             * @see WindowAdapter#windowOpened(WindowEvent)
             */
            public void windowOpened(WindowEvent e) {
                uiDelegate.requestFocusOnName();
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
        case IMPORT:
            return;
        default:
            throw new IllegalArgumentException("Type not supported");
        }
    }

    /**
     * Creates a new instance.
     * 
     * @param owner The owner of this dialog.
     * @param dialogType One of the constants defined by this class.
     * @param title Title of the dialog.
     * @param message Message of the dialog.
     * @param filters The list of filters.
     * @param accept Determines whether the all files filter is
     * turned on or off. Default value is <code>false</code>.
     * @param checkOverride Ask for confirmation if the user selects a file
     * that already exists.
     */
    public FileChooser(JFrame owner, int dialogType, String title,
            String message, List<FileFilter> filters, boolean accept,
            boolean checkOverride)
    {
        super(owner);
        checkType(dialogType);
        this.dialogType = dialogType;
        this.title = title;
        this.message = message;
        this.filters = filters;
        this.checkOverride = checkOverride;
        setProperties();
        folderPath = null;
        uiDelegate = new FileSaverUI(this, accept);
        pack();
    }

    /**
     * Creates a new instance.
     * 
     * @param owner The owner of this dialog.
     * @param dialogType One of the constants defined by this class.
     * @param title Title of the dialog.
     * @param message Message of the dialog.
     * @param filters The list of filters.
     * @param accept Determines whether the all files filter is turned.
     */
    public FileChooser(JFrame owner, int dialogType, String title,
            String message, List<FileFilter> filters, boolean accept)
    {
        this(owner, dialogType, title, message, filters, accept, false);
    }

    /**
     * Creates a new instance.
     * 
     * @param owner The owner of this dialog.
     * @param dialogType One of the constants defined by this class.
     * @param title Title of the dialog.
     * @param message Message of the dialog.
     * @param filters The list of filters.
     */
    public FileChooser(JFrame owner, int dialogType, String title,
            String message, List<FileFilter> filters)
    {
        this(owner, dialogType, title, message, filters, false, false);
    }

    /**
     * Creates a new instance.
     * 
     * @param owner The owner of this dialog.
     * @param dialogType One of the constants defined by this class.
     * @param title Title of the dialog.
     * @param message Message of the dialog.
     */
    public FileChooser(JFrame owner, int dialogType, String title,
            String message)
    {
        this(owner, dialogType, title, message, null, false);
    }

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
    List<FileFilter> getFilters() { return filters; }
   
    /**
     * Adds the extension to the passed name if necessary.
     * 
     * @param name The name to handle.
     * @param format The selected file format.
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
        firePropertyChange(CANCEL_SELECTION_PROPERTY, Boolean.valueOf(false),
                Boolean.valueOf(true));
        option = JFileChooser.CANCEL_OPTION;
        setVisible(false);
        dispose();
    }

    /** Saves the file. */
    void acceptSelection()
    {
        option = JFileChooser.APPROVE_OPTION;
        File[] files;
        override = false;
        if (getChooserType() == FOLDER_CHOOSER) {
            File f = uiDelegate.getCurrentDirectory();
            if (f != null) {
                if (!CollectionUtils.isEmpty(paths) && checkOverride) {
                    File[] entries = f.listFiles();
                    boolean exist = false;
                    for (int i = 0; i < entries.length; i++) {
                        if (paths.contains(entries[i].getName())) {
                            exist = true;
                        }
                    }
                    if (exist) {
                        MessageBox msg = new MessageBox(this,
                                "Overwrite existing files.",
                                "Do you wish to overwrite the existing files?");
                        int option = msg.centerMsgBox();
                        override = option == MessageBox.YES_OPTION;
                    }
                }
                String path = f.getAbsolutePath();
                if (!path.endsWith(File.separator))
                    path += File.separator;
                firePropertyChange(APPROVE_SELECTION_PROPERTY, null, path);
                setVisible(false);
                dispose();
            }
            return;
        } else {
            if (uiDelegate.isMultisSelectionEnabled()) {
                files = getSelectedFiles();
            } else {
                files = new File[1];
                files[0] = getSelectedFile();
            }
        }

        if (uiDelegate.isSetDefaultFolder()
                && getChooserType() != FileChooser.FOLDER_CHOOSER)
            UIUtilities.setDefaultFolder(
                    uiDelegate.getCurrentDirectory().toString());
        File f = getSelectedFile();
        String extension = FilenameUtils.getExtension(f.getName());
        if (CommonsLangUtils.isBlank(extension)) {
            FileFilter filter = getSelectedFilter();
            if (filter instanceof CustomizedFileFilter) {
                extension = ((CustomizedFileFilter) filter).getExtension();
                f = new File(f.getAbsolutePath()+"."+extension);
                files[0] = f;
            }
        }
        if (getChooserType() != FileChooser.FOLDER_CHOOSER) {
            if (f.exists() && checkOverride)
            {
                MessageBox msg = new MessageBox(this,
                        "Overwrite existing file.",
                        "Do you wish to overwrite the existing file?");
                int option = msg.centerMsgBox();
                if (option == MessageBox.NO_OPTION) 
                    return;
                String path = f.getAbsolutePath();
                f.delete();
                files[0] = new File(path);
            }
        }
        firePropertyChange(APPROVE_SELECTION_PROPERTY, Boolean.valueOf(false),
                files);
        setVisible(false);
        dispose();
    }

    /**
	 * Fires a property indicating where to save the archived files.
	 * 
	 * @param path The path to the directory.
	 */
	void setFolderPath(String path)
	{
	    if (path == null) return;
	    folderPath = path;
	    firePropertyChange(LOCATION_PROPERTY, null, path+File.separatorChar);
	    setVisible(false);
	    dispose();	
	}

    /** Brings up on screen the dialog asking a <code>Yes/No</code>. */
    void setSelection()
    {
        IconManager im = IconManager.getInstance();
        FileSaverDialog d = new FileSaverDialog(this,
                im.getIcon(IconManager.QUESTION_32));
        UIUtilities.centerAndShow(d);
    }

    /** 
     * Returns the path to the folder or <code>null</code>.
     * 
     * @return See above.
     */
    public File getFolderPath()
    {
        if (folderPath != null) return new File(folderPath);
        return null;
    }

    /**
     * Returns the type.
     * 
     * @return See above.
     */
    public int getChooserType() { return dialogType; }

    /**
     * Sets the name of the file to save.
     * 
     * @param name The name to set.
     */
    public void setSelectedFile(String name)
    {
        if (CommonsLangUtils.isBlank(name))
            throw new IllegalArgumentException("File name not valid.");
        String s = FilenameUtils.getBaseName(name);
        if (CommonsLangUtils.isBlank(s)) s = name;
        uiDelegate.setSelectedFile(new File(s));
    }

    /**
     * Sets the name of the file to save.
     * 
     * @param name The name to set.
     */
    public void setSelectedFileFull(String name)
    {
        if (CommonsLangUtils.isBlank(name)) return;
        uiDelegate.setSelectedFile(new File(name));
    }

    /**
     * Sets the name of the file to save.
     * 
     * @param name The name to set.
     */
    public void setSelectedFile(File name)
    {
        if (name == null) return;
        uiDelegate.setSelectedFile(name);
    }

    /**
     * Sets the name of the directory  to save.
     * 
     * @param dir The name to set.
     */
    public void setCurrentDirectory(String dir)
    {
        if (CommonsLangUtils.isBlank(dir))
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
	 * Returns the selected files.
	 * 
	 * @return See above.
	 */
	public File[] getSelectedFiles()
	{
	    return uiDelegate.getSelectedFiles();
	}

    /**
     * Returns the selected file with the file format extension added 
     * to it e.g. myfile.csv if the CVS filter is selected.
     * If no filter selected, the method returns the selected file
     * 
     * @return See above.
     * @see #getSelectedFile()
     */
    public File getFormattedSelectedFile()
    {
        return uiDelegate.getFormattedSelectedFile();
    }

    /**
     * Returns the currently selected filter.
     * 
     * @return See above.
     */
    public FileFilter getSelectedFilter()
    {
        return uiDelegate.getSelectedFilter();
    }

    /**
     * Shows the chooser dialog.
     * 
     * @return The option selected.
     */
    public int showDialog()
    {
        UIUtilities.setLocationRelativeToAndShow(getParent(), this);
        return option;
    }

    /**
     * Shows the chooser dialog.
     * 
     * @return The option selected.
     */
    public int centerDialog()
    {
        UIUtilities.centerAndShow(this);
        return option;
    }

    /**
     * Sets the text of the <code>Approve</code> button.
     * 
     * @param text The value to set.
     */
    public void setApproveButtonText(String text)
    {
        if (CommonsLangUtils.isBlank(text)) return;
        uiDelegate.setApproveButtonText(text);
    }

    /**
     * Sets the text displayed in the tool tip of the <code>Approve</code>
     * button.
     * 
     * @param text The value to set.
     */
    public void setApproveButtonToolTipText(String text)
    {
        if (CommonsLangUtils.isBlank(text)) return;
        uiDelegate.setApproveButtonToolTipText(text);
    }

    /**
     * Adds the passed button to add to the control.
     * 
     * @param button The button to add.
     * @param location he location of the button.
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

    /**
     * Sets the icon displayed in the title panel.
     * 
     * @param icon the value to set.
     */
    public void setTitleIcon(Icon icon)
    {
        if (icon == null) return;
        uiDelegate.setTitleIcon(icon);
    }

    /**
     * Sets the value indicating to allow for multiple selection if
     * <code>true</code>.
     * 
     * @param enabled Pass <code>true</code> to allow multiple selections,
     *                <code>false</code> otherwise.
     */
    public void setMultiSelectionEnabled(boolean enabled)
    {
        uiDelegate.setMultiSelectionEnabled(enabled);
    }

    /**
     * Sets the value indicating to check or not if the file can be overridden.
     * 
     * @param checkOverwrite Pass <code>true</code> to override,
     * <code>false</code> otherwise.
     */
    public void setCheckOverride(boolean checkOverride)
    {
        this.checkOverride = checkOverride;
    }

    /**
     * Sets the list of selected files. This should only be used when
     * several files are selected.
     * 
     * @param paths The collection of selected paths.
     */
    public void setSelectedFiles(List<String> paths)
    {
        this.paths = paths;
    }

    /**
     * Returns <code>true</code> to override the files when saving,
     * <code>false</code> otherwise. Default is <code>false</code>.
     *
     * @return
     */
    public boolean isOverride() { return override; }
}
