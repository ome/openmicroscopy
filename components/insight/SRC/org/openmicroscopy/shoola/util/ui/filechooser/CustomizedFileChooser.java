/*
 * org.openmicroscopy.shoola.util.ui.filechooser.CustomizedFileChooser 
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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

import org.openmicroscopy.shoola.util.CommonsLangUtils;

import org.openmicroscopy.shoola.util.filter.file.CustomizedFileFilter;
import org.openmicroscopy.shoola.util.filter.file.RegExFileFilter;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * A customized file chooser.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
class CustomizedFileChooser
	extends GenericFileChooser
	implements DocumentListener, KeyListener
{
	
	/** This is the default text for the file name when loading a file. */
	private final static String LOAD_LABEL = "Load:";
		
	/** This is the default text for the file name when selecting a folder. */
	private final static String FOLDER_LABEL = "Save in:";
	
	/** Reference to the model. */
	private FileChooser model;

	/** Reference to the View. */
	private FileSaverUI view;
	
	/** The text area where to enter the name of the file to save. */
	private JTextField nameArea;
	
	/** User defined file filter. */
	private RegExFileFilter filter;
	
	/** The original file name if any. */
	private String originalName; 
	
	/** 
	 * Initializes the components composing the display. 
	 * 
	 * @param accept Determines whether the all files filter is turned
     * 				 on or off. Default value is <code>false</code>.
	 */
	private void initComponents(boolean accept)
	{
		originalName = "";
		setAcceptAllFileFilterUsed(accept);
		nameArea = (JTextField) 
					UIUtilities.findComponent(this, JTextField.class);
		if (nameArea != null) {
			nameArea.setVisible(true);
			nameArea.getDocument().addDocumentListener(this);
		}
	}
		
	/** Builds and lays out the GUI. */
	private void buildGUI()
	{
		setControlButtonsAreShown(nameArea == null);
		JLabel label;
		List<FileFilter> filters = model.getFilters();
		if (filters != null) {
			for (FileFilter filter : filters)
				addChoosableFileFilter(filter);
			if (!isAcceptAllFileFilterUsed()) setFileFilter(filters.get(0));
		}
		if (isAcceptAllFileFilterUsed())
			setFileFilter(getAcceptAllFileFilter());
		try {
			File f = UIUtilities.getDefaultFolder();
			if (f != null) setCurrentDirectory(f);
		} catch (Exception ex) {}
		switch (model.getChooserType()) {
			case FileChooser.SAVE:
				setDialogType(SAVE_DIALOG);
				setFileSelectionMode(FILES_ONLY);
				break;
			case FileChooser.IMPORT:
				setDialogType(SAVE_DIALOG);
				setFileSelectionMode(FILES_AND_DIRECTORIES);
				setMultiSelectionEnabled(true);
				break;
			case FileChooser.LOAD:
				label = (JLabel) UIUtilities.findComponent(this, JLabel.class);
				if (label != null)
					label.setText(LOAD_LABEL);
				setFileSelectionMode(FILES_ONLY);
				break;
			case FileChooser.FOLDER_CHOOSER:
				label = (JLabel) UIUtilities.findComponent(this, JLabel.class);
				if (label != null)
					label.setText(FOLDER_LABEL);
				setFileSelectionMode(DIRECTORIES_ONLY);
				setCurrentDirectory(getFileSystemView().getHomeDirectory());
		}
	}
	
	/**
	 * Returns the format corresponding to the specified filter.
	 * 
	 * @param selectedFilter The filter specified.
	 * @return See above.
	 */
	private String getFormat(FileFilter selectedFilter)
	{
		List<FileFilter> filters = model.getFilters();
		if (filters == null) return "";
		for (FileFilter filter : filters) {
			if (selectedFilter.equals(filter))
				return filter.getDescription();
		}
		return "";
	}

	/**
	 * Returns the extension corresponding to the specified filter.
	 * 
	 * @param selectedFilter The filter specified.
	 * @return See above.
	 */
	private String getExtension(FileFilter selectedFilter)
	{
		List<FileFilter> filters = model.getFilters();
		if (filters == null) return "";
		for (FileFilter filter : filters) {
			if (selectedFilter.equals(filter) && 
					filter instanceof CustomizedFileFilter)
				return ((CustomizedFileFilter) filter).getExtension();
		}
		return "";
	}
		
	/**
	 * Sets the <code>enabled</code> flag of not the <code>Save</code> and
	 * <code>Preview</code> options depending on the length of the text entered
	 * in the {@link #nameArea}.
	 */
	private void handleTextUpdate()
	{
		if (nameArea == null) return; //should happen
		String text = nameArea.getText();
		originalName = text;
		view.setControlsEnabled(CommonsLangUtils.isNotEmpty(text));
	}
	
	/**
	 * Sets the format, the file name and the message to display.
	 * Returns <code>null</code> if the selected file is <code>null</code>
	 * or a <code>Boolean</code> whose value is <code>true</code>
	 * if the file already exists, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	private Object setSelection()
	{
		// Build the file .
		File f = getSelectedFile();
		if (f == null) return null;
		String format = getExtension(getFileFilter());
		String fileName = f.getAbsolutePath();
		//model.setSelectedFile(fileName);
		
		File[] l = getCurrentDirectory().listFiles();
		String n = model.getExtendedName(fileName, format);
		boolean exist = false;
		if (l != null) {
			for (int i = 0; i < l.length; i++) {
				if ((l[i].getAbsolutePath()).equals(n)) {
					exist = true;
					break;
				}
			}
		}
		setSelectedFile(null);
		return Boolean.valueOf(exist);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param model Reference to the model. Mustn't be <code>null</code>.
	 * @param view 	Reference to the view. Mustn't be <code>null</code>.
	 * @param accept Determines whether the all files filter is turned
     * 				 on or off. Default value is <code>false</code>.
	 */
	CustomizedFileChooser(FileChooser model, FileSaverUI view, boolean accept)
	{
		if (model == null) throw new IllegalArgumentException("No model.");
		if (view == null) throw new IllegalArgumentException("No view.");
		this.model = model;
		this.view = view;
		initComponents(accept);
		buildGUI();
	}
	
	/** 
	 * Sets the original name.
	 * 
	 * @param name The value to set.
	 */
	void setOriginalName(String name) { originalName = name; }
	
	/**
	 * Returns <code>true</code> if the control buttons are shown,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean areControlButtonsShown() { return (nameArea == null); }
	
	/**
     * Creates a new folder.
     * 
     * @param name The name of the folder.
     */
    void createFolder(String name)
    {
    	File dir = getCurrentDirectory();
    	new File(dir.getAbsolutePath(), name).mkdir();
    }
    
    /**
	 * Returns the pathname of the current file.
	 *
	 * @return  The file path.
	 */
	File getFormattedSelectedFile()
	{ 
		File f = getSelectedFile();
		if (f != null) {
			String format = getExtension(getFileFilter());
			if (CommonsLangUtils.isEmpty(format))
				return f;
			
			String fileName = f.getAbsolutePath();
			if (fileName.endsWith("."+format)) return new File(fileName);
			return new File(fileName+"."+format);
		}
		return f;
	}
	
	/** 
	 * Enables the <code>ApproveButton</code> if there is text
	 * entered in the {@link #nameArea}.
	 */
	void requestFocusOnName()
	{
		if (nameArea != null) {
			String text = nameArea.getText();
			text = text.trim();
			view.setControlsEnabled(text.length() > 0);
		}
	}
	
	/** Resets the selection. */
	void resetSelection()
	{
		//super.setSelectedFile(new File(originalName));
	}
	
	/**
	 * Enables or not the <code>Save</code> and <code>Preview</code> options
	 * depending on the text entered in the {@link #nameArea}.
	 * @see DocumentListener#insertUpdate(DocumentEvent)
	 */
	public void insertUpdate(DocumentEvent e)
	{
		handleTextUpdate();
	}
	
	/**
	 * Enables or not the <code>Save</code> and <code>Preview</code> options
	 * depending on the text entered in the {@link #nameArea}.
	 * @see DocumentListener#removeUpdate(DocumentEvent)
	 */
	public void removeUpdate(DocumentEvent e)
	{
		handleTextUpdate();
	}
	
	/**
	 * Creates a Regular Expression filter.
	 * @see KeyListener#keyReleased(KeyEvent)
	 */
	public void keyReleased(KeyEvent e)
	{
		String filterString = nameArea.getText();
		try {
			if (filter == null) {
				filter = new RegExFileFilter(filterString, true);
				setFileHidingEnabled(true);
				setFileFilter(filter);
			} else
				filter.setFilter(filterString, true);
		} catch(Exception exception) {
			// Eat the exception as this is just the user part way through the
			// RegEx expression and it being malformed.
		}
		repaint();
	}

	/**
	 * Overridden to close the model when the selection is cancelled.
	 * @see JFileChooser#cancelSelection()
	 */
	public void cancelSelection()
	{
		model.cancelSelection();
		super.cancelSelection();
	}
	
	/**
	 * Overridden to set the format, name and type of images to save.
	 * @see JFileChooser#approveSelection()
	 */
	public void approveSelection()
	{
		if (model.getChooserType() == FileChooser.FOLDER_CHOOSER) {
				File f = getCurrentDirectory();
				if (f != null) {
					setSelectedFile(null);
					model.acceptSelection();
				}
		} else {

			//Boolean exist = setSelection();
			//No file selected, or file can be written - let OK action continue	
			if (setSelection() != null) {
				model.acceptSelection();
			}
		}
		super.approveSelection();
	}
	
	/**
	 * Overridden to create the selected file when 
	 * <code>Save</code> and <code>Preview</code> options are visible,
	 * otherwise the selected file is <code>null</code>.
	 * @see JFileChooser#getSelectedFile()
	 */
	public File getSelectedFile()
	{
		if (model == null) return super.getSelectedFile(); 
		if (model.getChooserType() == FileChooser.FOLDER_CHOOSER)
			return super.getSelectedFile();
		if (nameArea == null) return super.getSelectedFile();
		String name = nameArea.getText();
		if (CommonsLangUtils.isEmpty(name))
			return super.getSelectedFile();
		return new File(getCurrentDirectory().toString(), name);
	}
	
	/**
	 * Required by the {@link DocumentListener} I/F but no-operation
	 * implementation in our case.
	 * @see DocumentListener#changedUpdate(DocumentEvent)
	 */
	public void changedUpdate(DocumentEvent e) {}

	/**
	 * Required by the {@link KeyListener} I/F but no-operation
	 * implementation in our case.
	 * @see KeyListener#keyPressed(KeyEvent)
	 */
	public void keyPressed(KeyEvent e) {}

	/**
	 * Required by the {@link KeyListener} I/F but no-operation
	 * implementation in our case.
	 * @see KeyListener#keyTyped(KeyEvent)
	 */
	public void keyTyped(KeyEvent e) {}
	
}
