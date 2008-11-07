 /*
 * ui.formFields.FormFieldProtocolImporter 
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
package ui.formFields;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import tree.DataField;
import tree.DataFieldConstants;
import tree.IDataFieldObservable;
import ui.components.CustomComboBox;
import ui.components.PopupMenuButton;
import ui.formFields.FormFieldLink.GetLinkPathAction;
import ui.formFields.FormFieldLink.GetURLAction;
import ui.formFields.FormFieldLink.OpenLinkActionListener;
import util.BareBonesBrowserLaunch;
import util.FilePathMethods;
import util.ImageFactory;
import util.PreferencesManager;
import validation.SAXValidator;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class FormFieldProtocolImporter 
extends FormField 
	implements ActionListener {

	/**
	 * A link (stored in the VALUE attribute of the dataField) that is the
	 * file path to a folder containing protocols you want to choose from.
	 */
	private String 			folderLink;
	
	
	Icon brokenLinkIcon = ImageFactory.getInstance().getIcon(ImageFactory.FILE_CLOSE_ICON);
	
	JComboBox protocolChooser;
	
	/**
	 * A custom dialog that contains the fileChooser for getting a link from users. 
	 * Also contains a checkBox for "Relative Link";
	 */
	JDialog customGetLinkDialog;
	

	
	public static final int BROKEN_LINK = 5;
	
	/**
	 * The button that users click to choose a link. 
	 * Provides a pop-up menu with options..
	 */
	JButton getLinkButton;
	
	public FormFieldProtocolImporter(IDataFieldObservable dataFieldObs) {
		super(dataFieldObs);
		
		Border toolBarButtonBorder = new EmptyBorder(2,2,2,2);
		
		// need comboBox for choosing protocol file to import. 
		protocolChooser = new CustomComboBox();
		protocolChooser.setBackground(null);
		horizontalBox.add(protocolChooser);
		
		Icon chooseLinkIcon = ImageFactory.getInstance().getIcon(ImageFactory.WRENCH_ICON);
		
		getLinkButton = new JButton(chooseLinkIcon);
		getLinkButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { getAndSaveLink(); }
		});
		getLinkButton.setBorder(toolBarButtonBorder);
		getLinkButton.setBackground(null);
		horizontalBox.add(getLinkButton);
		
		// Update the link etc. 
		dataFieldUpdated();
	}
	
	// called when dataField changes attributes
	public void dataFieldUpdated() {
		super.dataFieldUpdated();
		
		updateLink();
	} 
	
	/**
	 * To ensure that only one type of link is saved to dataField.
	 * Update several attributes at once, making sure that all apart from 
	 * one are null.
	 * 
	 * @param name
	 * @param value
	 */
	public void saveLinkToDataField(String name, String value) {
		
		if (name.equals(DataFieldConstants.ABSOLUTE_FILE_LINK) ||
				name.equals(DataFieldConstants.RELATIVE_FILE_LINK)) {
			
			/*
			 * Make a map with all values null, then update one.
			 */
			HashMap<String, String> newValues = new HashMap<String, String>();
			newValues.put(DataFieldConstants.ABSOLUTE_FILE_LINK, null);
			newValues.put(DataFieldConstants.RELATIVE_FILE_LINK, null);
			
			newValues.put(name, value);
			
			// Updates new values, and adds to undo queue as one action. 
			dataField.setAttributes("Protocol Link", newValues, true);
		}
	}
	
	
	/**
	 * This method takes an absolute file path, converts and saves it as a relative link. 
	 * Updating dataField with this attribute will notify this field, which will display it.
	 */
	public void setRelativeLink(String linkedFilePath) {
		
		/*
		 * First, check that the user has saved the current editor file somewhere. 
		 */
		File editorFile = ((DataField)dataField).getNode().getTree().getFile();
			
		/*
		 * If the file does not exist, forget about it!!
		 */
		if (!editorFile.exists()) {
			JOptionPane.showMessageDialog(null, "This editor file has not yet been saved. \n" +
					"You must first save the editor file, before a relative " +
					"image file path can be defined.", 
					"Please save file first", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		/*
		 * Calculate what the relative path is, based on the location of the editorFile. 
		 */
		String relativePath = FilePathMethods.getRelativePathFromAbsolutePath(editorFile, linkedFilePath);
		
		/*
		 * Save the relative Path.
		 * This will cause the field to update, and display the new link
		 */ 
		saveLinkToDataField(DataFieldConstants.RELATIVE_FILE_LINK, relativePath);
	}
	
	/**
	 * Opens a fileChooser, for the user to pick a file.
	 * Then this file path is saved to dataField. 
	 * If the user checks "Relative link" then filePath is saved as relative. 
	 */
	public void getAndSaveLink() {
		
		String currentFilePath = PreferencesManager.getPreference(PreferencesManager.CURRENT_IMAGES_FOLDER);
		
		// Create a file chooser
		JFileChooser fc = new JFileChooser();
		fc.setDialogType(JFileChooser.OPEN_DIALOG);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (currentFilePath != null)
			fc.setCurrentDirectory(new File(currentFilePath));
		
		JCheckBox relativeLink = new JCheckBox("Relative link");
		relativeLink.setToolTipText("<html>The link to the chosen file will be 'relative' to the OMERO.editor file you are editing.<br>" +
				"Choose this option if the location of both files is likely to change in a similar way (eg saved in the same folder).");
		
		customGetLinkDialog = new JDialog();
		Container dialogPane = customGetLinkDialog.getContentPane();
		dialogPane.setLayout(new BorderLayout());
		dialogPane.add(fc, BorderLayout.CENTER);
		
		dialogPane.add(relativeLink, BorderLayout.SOUTH);
		
		fc.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				customGetLinkDialog.setVisible(false);
			}});
		customGetLinkDialog.pack();
		customGetLinkDialog.setModal(true);
		customGetLinkDialog.setLocationRelativeTo(this);
		customGetLinkDialog.setVisible(true);
		
		File file = fc.getSelectedFile();
		// remember where folder was
		if (file == null) 
		return;
		
		System.out.println("FormFieldLink getImagePath() " + file.getAbsolutePath() +
				" " + relativeLink.isSelected());
		PreferencesManager.setPreference(PreferencesManager.CURRENT_IMAGES_FOLDER, file.getParent());
		
		String linkedFilePath = file.getAbsolutePath();
		
		/*
		 * If the user checked the "Relative Link" checkBox, save as a relative link.
		 * Otherwise, save as absolute link.
		 * Either will cause the field to refresh, displaying the new link. 
		 */
		if (relativeLink.isSelected()) {
			setRelativeLink(linkedFilePath);
		} else {
			// Save the absolute Path
			saveLinkToDataField(DataFieldConstants.ABSOLUTE_FILE_LINK, linkedFilePath);
		}
		
	}
	
	
	/**
	 * Updates the local value of URLlink with the dataField value.
	 * Then calls refreshLink() to display new link.
	 */
	public void updateLink() {
		
		/*
		 * First check the value of the Absolute file path attribute
		 */
		folderLink = dataField.getAttribute(DataFieldConstants.ABSOLUTE_FILE_LINK);
		
		// if null, try the relative path attribute
		if (folderLink == null) {
			folderLink = dataField.getAttribute(DataFieldConstants.RELATIVE_FILE_LINK);
			if (folderLink != null) {
				// check if file exists (need to get full path first)!
				File editorFile = ((DataField)dataField).getNode().getTree().getFile();
				folderLink = FilePathMethods.getAbsolutePathFromRelativePath(editorFile, folderLink);
			}
		}
		
		// if folderLink isn't null, it should be absolute...
		if (folderLink != null) {
			
			File protocolFolder = new File(folderLink);
			// check it exists and is directory
			if (! protocolFolder.exists()) 		return;
			if (! protocolFolder.isDirectory()) 	return;
			
			String[] protocols = protocolFolder.list();
			
			String protName;
			protocolChooser.removeActionListener(this);
			protocolChooser.removeAllItems();
			for (int i=0; i<protocols.length; i++) {
				protName = protocols[i];
				if (isEditorFileExtension(protName))
					protocolChooser.addItem(protName);
			}
			protocolChooser.addActionListener(this);
			
			return;
		}
	}
	
	/**
	 * This checks the file extension of the file name (with or without the file path)
	 * and returns true if it matches that of Editor files. 
	 * 
	 * @param fileNameOrPath		The name of the file (with or without path)
	 * @return					True if the file extension is same as Editor files.
	 */
	public boolean isEditorFileExtension(String fileNameOrPath) {
		
		return (fileNameOrPath.endsWith(".pro.xml") 
				|| fileNameOrPath.endsWith(".pro")		// older file
				|| fileNameOrPath.endsWith(".exp"));	// older file
	}
	
	/**
	 * This simply enables or disables all the editable components of the 
	 * FormField.
	 * Gets called (via refreshLockedStatus() ) from dataFieldUpdated()
	 * 
	 * @param enabled
	 */
	public void enableEditing(boolean enabled) {
		
		if (getLinkButton != null)	// just in case!
			getLinkButton.setEnabled(enabled);
	}

	/**
	 * Gets the names of the attributes where this field stores its "value"s.
	 * This is used eg. (if a single value is returned)
	 * as the destination to copy the default value when defaults are loaded.
	 * Also used by EditClearFields to set all values back to null. 
	 * Mostly this is DataFieldConstants.VALUE, but this method should be over-ridden by 
	 * subclasses if they want to store their values under a different attributes (eg "seconds" for TimeField)
	 * 
	 * @return	the name of the attribute that holds the "value" of this field
	 */
	public String[] getValueAttributes() {
		return new String[] {DataFieldConstants.ABSOLUTE_FILE_LINK};
	}


	/**
	 * This method tests to see whether the field has been filled out. 
	 * In the case of Link Field, only one of the link attributes has to be not null
	 * 
	 * @see FormField.isFieldFilled()
	 * @return	True if the field has been filled out by user (Required values are not null)
	 */
	@Override
	public boolean isFieldFilled() {
		String[] attributes = getValueAttributes();
		for (int i=0; i<attributes.length; i++) {
			// if any attribute is not null, then this field is filled. 
			if (dataField.getAttribute(attributes[i]) != null)
				return true;
		}
		return false;
	}

	/**
	 * open the file, import the fields, add root as next sibling to this field. 
	 */
	public void actionPerformed(ActionEvent e) {
		
		String fileName = folderLink + File.separator +
							protocolChooser.getSelectedItem().toString();
		
		// set the import file
		model.setImportFile(new File(fileName));
		// make sure this field is selected(clear others)
		panelClicked(true);
		// import from file (will do the whole file by default)
		model.importFieldsFromImportTree();
		// clear the import file (so it is not displayed in UI)
		model.setImportFile(null);
	}
}
