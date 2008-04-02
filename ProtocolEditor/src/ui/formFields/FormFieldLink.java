
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

package ui.formFields;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import tree.DataField;
import tree.DataFieldConstants;
import tree.IDataFieldObservable;
import ui.components.FileChooserReturnFile;
import ui.components.PopupMenuButton;
import util.FilePathMethods;
import util.ImageFactory;
import util.PreferencesManager;


public class FormFieldLink extends FormField {

	/**
	 * A link (stored in the VALUE attribute of the dataField) that is the
	 * URL to a local file or a web-page.
	 */
	String URLlink;
	
	/**
	 * A button that has blue underlined text displaying a link
	 */
	JButton linkButton;
	
	/**
	 * The link font.
	 */
	public static final Font LINK_FONT = new Font("SansSerif", Font.PLAIN, 12);
	
	Icon networkLocalIcon = ImageFactory.getInstance().getIcon(ImageFactory.NETWORK_LOCAL_ICON);
	
	Icon linkLocalIcon = ImageFactory.getInstance().getIcon(ImageFactory.LINK_LOCAL_ICON);
	
	/**
	 * The button that users click to choose a link. 
	 * Provides a pop-up menu with options..
	 */
	JButton getLinkButton;
	
	
	public FormFieldLink(IDataFieldObservable dataFieldObs) {
		super(dataFieldObs);
		
		Border toolBarButtonBorder = new EmptyBorder(2,2,2,2);
		
		
		linkButton = new JButton();
		linkButton.setBorder(toolBarButtonBorder);
		linkButton.setIcon(networkLocalIcon);
		linkButton.setBackground(null);
		linkButton.setCursor(handCursor);
		linkButton.setFont(LINK_FONT);
		linkButton.setForeground(Color.BLUE);
		horizontalBox.add(linkButton);
		
		
		Icon openImageIcon = ImageFactory.getInstance().getIcon(ImageFactory.OPEN_IMAGE_ICON);
		
		Action[] getImageActions = new Action[] {
				new GetURLAction(),
				new GetAbsoluteImagePathAction(),
				new GetRelativeImagePathAction()};
		getLinkButton = new PopupMenuButton("Choose an image to display", 
				openImageIcon, getImageActions);
		
		getLinkButton.setBorder(toolBarButtonBorder);
		getLinkButton.setBackground(null);
		horizontalBox.add(getLinkButton);
	}
	
	
	public class GetURLAction extends AbstractAction {
		
		public GetURLAction() {
			putValue(Action.NAME, "Set URL");
			putValue(Action.SHORT_DESCRIPTION, "Link to a web page");
			putValue(Action.SMALL_ICON, wwwIcon); 
		}
		
		public void actionPerformed(ActionEvent e) {
			String url = (String)JOptionPane.showInputDialog(
                    null, "Enter URL:", "Enter URL", JOptionPane.PLAIN_MESSAGE,
                    wwwIcon, null, "http://");

			if ((url == null) || (url.length() == 0)) // user canceled
				return;
			
			// Save the absolute Path
			// (first overwrite the relative path (if not null). Add to undo queue.
			if (dataField.getAttribute(DataFieldConstants.RELATIVE_IMAGE_PATH) != null)
				dataField.setAttribute(DataFieldConstants.RELATIVE_IMAGE_PATH, null, true);
			// this will cause this field to refresh, displaying the image.
			dataField.setAttribute(DataFieldConstants.IMAGE_PATH, url, true);
		}
	}
	
	public class GetAbsoluteImagePathAction extends AbstractAction {
		
		public GetAbsoluteImagePathAction() {
			putValue(Action.NAME, "Set Absolute link to local file");
			putValue(Action.SHORT_DESCRIPTION, "Link to a local file that will stay in the same absolute file location.");
			putValue(Action.SMALL_ICON, linkLocalIcon); 
		}
		
		public void actionPerformed(ActionEvent e) {
			String imagePath = getImagePath();
			if (imagePath == null) // user canceled
				return;
			
			// Save the absolute Path
			// (first overwrite the relative path (if not null). Add to undo queue.
			if (dataField.getAttribute(DataFieldConstants.RELATIVE_IMAGE_PATH) != null)
				dataField.setAttribute(DataFieldConstants.RELATIVE_IMAGE_PATH, null, true);
			// this will cause this field to refresh, displaying the image.
			dataField.setAttribute(DataFieldConstants.IMAGE_PATH, imagePath, true);
		}
	}
	
	/**
	 * The action that allows users to choose a Relative image path. 
	 * @author will
	 *
	 */
	public class GetRelativeImagePathAction extends AbstractAction {
		
		public GetRelativeImagePathAction() {
			putValue(Action.NAME, "Set Relative link to local file");
			putValue(Action.SHORT_DESCRIPTION, "Link to a local file that will stay in the same file location, relative to this file");
			putValue(Action.SMALL_ICON, linkLocalIcon); 
		}
		
		public void actionPerformed(ActionEvent e) {
			
			/*
			 * First, check that the user has saved the current editor file somewhere. 
			 */
			File editorFile = ((DataField)dataField).getNode().getTree().getFile();
			String editorFilePath = editorFile.getAbsolutePath();
				
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
			 * If the file exists, but contains "untitled", it may have been saved in a 
			 * temporary location (not in a correct folder). 
			 * Need to check that the user has saved it where they want. 
			 */
			if (editorFile.getName().contains("untitled")) {
				
				Object[] options = {"Cancel", "Continue anyway"};
				
				int yesNo = JOptionPane.showOptionDialog(null, "The current file is called 'untitled' and \n" +
						"may therefore be in a temporary file location. \n" +
						"If so, please cancel and save the file in its 'proper' place.", 
						"Temporary file?", JOptionPane.YES_NO_OPTION, 
						JOptionPane.WARNING_MESSAGE, null, options, options[1]);
				
				if (yesNo == 0) {
					return;
				}
				
			}
			
			String imagePath = getImagePath();
			if (imagePath == null) // user canceled
				return;
			

			/*
			 * Calculate what the relative path is, based on the location of the editorFile. 
			 */
			String relativePath = FilePathMethods.getRelativePathFromAbsolutePath(editorFile, imagePath);
			
			
			// Save the relative Path
			// (first overwrite the absolute path (if not null). Add to undo queue.
			if (dataField.getAttribute(DataFieldConstants.IMAGE_PATH) != null)
				dataField.setAttribute(DataFieldConstants.IMAGE_PATH, null, true);
			// This will cause this field to Refresh, displaying the image.
			dataField.setAttribute(DataFieldConstants.RELATIVE_IMAGE_PATH, relativePath, true);
		}
	}
	
	/**
	 * Opens a fileChooser, for the user to pick a file.
	 * Returns the absolute path for the chosen file. 
	 * 
	 * @return	Absolute file path for chosen file.
	 */
	public static String getImagePath() {
		
		// open any file?
		String[] fileExtensions = {""};
		String currentFilePath = PreferencesManager.getPreference(PreferencesManager.CURRENT_IMAGES_FOLDER);
		
		// Create a file chooser
		FileChooserReturnFile fc = new FileChooserReturnFile(fileExtensions, currentFilePath);
		File file = fc.getFileFromUser();
		
		// remember where folder was
		if (file != null) {
			PreferencesManager.setPreference(PreferencesManager.CURRENT_IMAGES_FOLDER, file.getParent());
			return file.getAbsolutePath();
		}
		
		return null;
		
	}
	
	
	/**
	 * Updates the local value of URLlink with the dataField value.
	 * Then calls refreshLink() to display new link.
	 */
	public void updateLink() {
		URLlink = dataField.getAttribute(DataFieldConstants.VALUE);
		refreshLink();
	}

	/**
	 * Displays the current value of URLlink. 
	 */
	public void refreshLink() {
		
		if (URLlink == null) {
			linkButton.setText("");
			linkButton.setToolTipText("No link");
			return;
		} 
		
		int len = URLlink.length();
		linkButton.setText(len < 20 ? URLlink : "..." + URLlink.substring(len-19, len));
		linkButton.setToolTipText("Open the link to: " + URLlink);
	}
	
	/**
	 * This simply enables or disables all the editable components of the 
	 * FormField.
	 * Gets called (via refreshLockedStatus() ) from dataFieldUpdated()
	 * 
	 * @param enabled
	 */
	public void enableEditing(boolean enabled) {
		super.enableEditing(enabled);	
		
		if (getLinkButton != null)	// just in case!
			getLinkButton.setEnabled(enabled);
	}
}
