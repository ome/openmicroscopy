/*
 * org.openmicroscopy.shoola.util.ui.filechooser.CustomizedFileChooser 
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
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.filter.file.Filter;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * A customized file chooser.
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
class CustomizedFileChooser
	extends JFileChooser 
	implements DocumentListener
{
	
	/** This is the default text for the file name when loading a file. */
	private final static String LOAD_LABEL	= "Load:";
		
	/** This is the default text for the file name when selecting a folder. */
	private final static String FOLDER_LABEL	= "Save in:";
	
	/** Reference to the model. */
	private FileChooser			model;

	/** Reference to the View. */
	private FileSaverUI			view;
	
	/** The text area where to enter the name of the file to save. */
	private JTextField			nameArea;
	
	/** Initiliazes the components composing the display. */
	private void initComponents()
	{
		nameArea = (JTextField) 
					UIUtilities.findComponent(this, JTextField.class);
		if (nameArea != null) nameArea.getDocument().addDocumentListener(this);
	}
		
	/** Builds and lays out the GUI. */
	private void buildGUI()
	{
		setAcceptAllFileFilterUsed(false);
		setDialogType(SAVE_DIALOG);
		setControlButtonsAreShown(nameArea == null);
		JLabel label;
		List<FileFilter> filters = model.getFilterList();
		if (filters != null) {
			for (FileFilter filter : filters)
				addChoosableFileFilter(filter);
			setFileFilter(filters.get(0));
		}
		File f = UIUtilities.getDefaultFolder();
		if (f != null) setCurrentDirectory(f);
		switch (model.getDialogType()) {
			case FileChooser.SAVE:
				setFileSelectionMode(FILES_ONLY);
				break;
			case FileChooser.LOAD:
				label = (JLabel) UIUtilities.findComponent(this, JLabel.class);
				if (label != null)
					label.setText(LOAD_LABEL);
				setFileSelectionMode(FILES_ONLY);
				break;
			case FileChooser.FOLDER_CHOOSER:
				/*
				List boxes = UIUtilities.findComponents(this, JComboBox.class);
				if (boxes != null) {
					JComboBox box = (JComboBox) boxes.get(boxes.size()-1);
					if (box.getParent() != null) 
						box.getParent().setVisible(false);
				}
				*/
				label = (JLabel) UIUtilities.findComponent(this, JLabel.class);
				if (label != null)
					label.setText(FOLDER_LABEL);
				setFileSelectionMode(DIRECTORIES_ONLY);
				String s = UIUtilities.getDefaultFolderAsString();
		        if (s == null) return;
		        if (s == null || s.equals("") || !(new File(s).exists()))
		            setCurrentDirectory(getFileSystemView().getHomeDirectory());
		        else setCurrentDirectory(new File(s));  
		       	return;
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
		List<FileFilter> filters = model.getFilterList();
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
		List<FileFilter> filters = model.getFilterList();
		if (filters == null) return "";
		for (FileFilter filter : filters) {
			if (selectedFilter.equals(filter) && filter instanceof Filter)
				return ((Filter) filter).getExtension();
		}
		return "";
	}
		
	/**
	 * Sets the <code>enabled</code> flag of not the <code>Save</code> and
	 * <code>Preview</code> options depending on the lenght of the text entered
	 * in the {@link #nameArea}.
	 */
	private void handleTextUpdate()
	{
		if (nameArea == null) return; //should happen
		String text = nameArea.getText();
		boolean b = (text == null || text.trim().length() == 0);
		view.setControlsEnabled(!b);
	}
	
	/**
	 * Sets the format, the file name and the message to display.
	 * Returns <code>null</code> if the selected file is <code>null</code>
	 * or a <code>Boolean</code> whose value is <code>true</code>
	 * if the file already exists, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	private Boolean setSelection()
	{
		// Build the file .
		File f = getSelectedFile();
		if (f != null)
		{
			String format = getExtension(getFileFilter());
			String fileName = f.getAbsolutePath();
			//model.setSelectedFile(fileName);
			
			File[] l = getCurrentDirectory().listFiles();
			String n = model.getExtendedName(fileName, format);
			boolean exist = false;
			for (int i = 0; i < l.length; i++) {
				if ((l[i].getAbsolutePath()).equals(n)) {
					exist = true;
					break;
				}
			}
			setSelectedFile(null);
			return new Boolean(exist);
			//if (display) return;    // to check
		}
		return null;
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param model Reference to the model. Mustn't be <code>null</code>.
	 * @param view 	Reference to the view. Mustn't be <code>null</code>.
	 */
	CustomizedFileChooser(FileChooser model, FileSaverUI view)
	{
		if (model == null) throw new IllegalArgumentException("No model.");
		if (view == null) throw new IllegalArgumentException("No view.");
		this.model = model;
		this.view = view;
		initComponents();
		buildGUI();
	}
		
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
    	String n = dir.getAbsolutePath()+File.separator+name;
    	new File(n).mkdir();
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
			if (format == null || format.trim().length() == 0)
				return f;
			
			String fileName = f.getAbsolutePath();
			return new File(fileName+"."+format);
		}
		return f;
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
	 * Overridden to close the {@link ImgSaver} when the selection is cancelled.
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
		if (model.getDialogType() == FileChooser.FOLDER_CHOOSER) {
			File file = getSelectedFile();
	        if (file != null) model.setFolderPath(file.getAbsolutePath()); 
		} else {
			
			
			Boolean exist = setSelection();
			//No file selected, or file can be written - let OK action continue	
			if (exist != null) {
				
				model.acceptSelection();
				//super.approveSelection();
			} else {
				//super.approveSelection();
			}
			//model.acceptSelection();
			//super.approveSelection();
		}
		//previewSelection();
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
		if (nameArea == null) return super.getSelectedFile();
		return new File(getCurrentDirectory().toString(), nameArea.getText());
	}
	
	/**
	 * Required by the {@link DocumentListener} I/F but no-op implementation
	 * in our case.
	 * @see DocumentListener#changedUpdate(DocumentEvent)
	 */
	public void changedUpdate(DocumentEvent e) {}
	
}
