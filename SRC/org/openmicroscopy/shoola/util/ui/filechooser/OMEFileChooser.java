/*
 * org.openmicroscopy.shoola.util.ui.filechooser.FileChooser 
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.util.saver.ImgSaver;
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
public class OMEFileChooser
	extends JFileChooser 
	implements DocumentListener
{
	
	/** This is the default text for the file name when loading a file. */
	private final static String loadLabel	= "Load :";
		
	/** Reference to the model. */
	private FileChooser			model;
	
	/** is this a save dialog. */
	private boolean 			save;
	
	/** Reference to the View. */
	private FileSaverUI			view;
	
	/** The text area where to enter the name of the file to save. */
	private JTextField			nameArea;
	
	/** Initiliazes the components composing the display. */
	private void initComponents()
	{
		nameArea=(JTextField) UIUtilities.findComponent(this, JTextField.class);
		if (nameArea!=null) nameArea.getDocument().addDocumentListener(this);
	}
		
	/** Builds and lays out the GUI. */
	private void buildGUI()
	{
		setAcceptAllFileFilterUsed(false);
		if(save)
			setDialogType(SAVE_DIALOG);
		else
		{	
			setDialogType(SAVE_DIALOG);
			JLabel label=(JLabel) UIUtilities.findComponent(this, JLabel.class);
			label.setText(loadLabel);
		}
		setFileSelectionMode(FILES_ONLY);
		for(FileFilter filter : model.getFilterList())
			addChoosableFileFilter(filter);
		setFileFilter(model.getFilterList().get(0));
		
		File f=UIUtilities.getDefaultFolder();
		if (f!=null) setCurrentDirectory(f);
		if (nameArea!=null) setControlButtonsAreShown(false);
		else
		{
			if(save)
			{
				setApproveButtonToolTipText(UIUtilities
				.formatToolTipText(FileSaverUI.SAVE_AS));
				setApproveButtonText("Save as");
			}
			else
			{
				setApproveButtonToolTipText(UIUtilities
				.formatToolTipText(FileSaverUI.LOAD));
				setApproveButtonText("Load");
			}
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
		for(FileFilter filter : model.getFilterList())
		{
			if(selectedFilter.equals(filter))
				return filter.getDescription();
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
		if (nameArea==null) return; //should happen
		String text=nameArea.getText();
		boolean b=(text==null||text.trim().length()==0);
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
		File f=getSelectedFile();
		
		if (f!=null)
		{
			String format=getFormat(getFileFilter());
			String fileName=f.getAbsolutePath();
			model.setSelectedFile(fileName);
			
			File[] l=getCurrentDirectory().listFiles();
			String n=model.getExtendedName(fileName, format);
			boolean exist=false;
			for (int i=0; i<l.length; i++)
			{
				if ((l[i].getAbsolutePath()).equals(n))
				{
					exist=true;
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
	 * @param saveDialog is this a save dialog.
	 * @param model Reference to the model. Mustn't be <code>null</code>.
	 * @param view 	Reference to the view. Mustn't be <code>null</code>.
	 */
	OMEFileChooser(boolean saveDialog, FileChooser model, FileSaverUI view)
	{
		if (model==null) throw new IllegalArgumentException("No model.");
		if (view==null) throw new IllegalArgumentException("No view.");
		this.model=model;
		this.view=view;
		this.save = saveDialog;
		initComponents();
		buildGUI();
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
	 * Required by the {@link DocumentListener} I/F but no-op implementation
	 * in our case.
	 * @see DocumentListener#changedUpdate(DocumentEvent)
	 */
	public void changedUpdate(DocumentEvent e)
	{}
	
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
		Boolean exist=setSelection();
		if (exist==null)
		// No file selected, or file can be written - let OK action continue
		super.approveSelection();
		else
		{
			model.acceptSelection();
			super.approveSelection();
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
		if (nameArea==null) return super.getSelectedFile();
		return new File(getCurrentDirectory().toString(), nameArea.getText());
	}
	
}
