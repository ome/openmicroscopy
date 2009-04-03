package ui.components;

/*
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
 *	author Will Moore will@lifesci.dundee.ac.uk
 */

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ui.AbstractComponent;
import ui.IModel;

/**
 * This class is a JComboBox that displays a list of the currently-opened files from <code>IModel</code>
 * Changing the selection of the ComboBox will choose a different file to be 
 * the "currently-displayed" file.
 * This class registers as a changeListener of the model, then updates it's file list
 * upon each changeEvent. 
 * 
 * @author will
 *
 */

public class FileListSelector 
	extends JComboBox 
	implements ChangeListener {

	IModel model;
	
	/**
	 *	The listener for the comboBox.
	 */
	FileListSelectionListener fileListSelectionListener;
	
	/**
	 * creates an instance of this class.
	 * IModel provides a list of current files, and the current file is selected by index.
	 * If model extends <code>AbstractComponent</code>, this class registers as a changeListener.
	 * 
	 * @param model		needs this to get a current file list, and change the current file
	 */
	public FileListSelector(IModel model) {
		super(model.getOpenFileList());
		
		this.model = model;
		
		if (model instanceof AbstractComponent) {
			((AbstractComponent)model).addChangeListener(this);
		}
		
		fileListSelectionListener = new FileListSelectionListener();
		
		addActionListener(fileListSelectionListener);
	}

	/** 
	 * called when the model changes (ie the file list needs updating)
	 */
	public void stateChanged(ChangeEvent e) {
		updateFileList();
	}
	
	/**
	 *  called by stateChanged(). Updates the list of current files from the model
	 */
	public void updateFileList() {
		removeActionListener(fileListSelectionListener);
		
		removeAllItems();
		
		String[] fileList = model.getOpenFileList();
		
		if (fileList.length > 0){
		
			for (int i=0; i<fileList.length; i++) {
				addItem(fileList[i]);
			}
			setSelectedIndex(model.getCurrentFileIndex());
		}
		
		addActionListener(fileListSelectionListener);
	}
	
	
	/**
	 * Responds to a new selection from the comboBox. 
	 * Changes the current file by index (selecting from the list of current files)
	 * @author will
	 *
	 */
	public class FileListSelectionListener implements ActionListener {
		
		public void actionPerformed (ActionEvent event) {
			JComboBox source = (JComboBox)event.getSource();
			int selectedIndex = source.getSelectedIndex();
			System.out.println("FileListSelector changeCurrentFile to index " + selectedIndex);
			model.changeCurrentFile(selectedIndex);
		}
	}	
}
