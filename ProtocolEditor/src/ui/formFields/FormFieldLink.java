
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
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import tree.DataField;
import tree.DataFieldConstants;
import tree.IDataFieldObservable;
import ui.components.FileChooserReturnFile;
import ui.components.PopupMenuButton;
import util.BareBonesBrowserLaunch;
import util.FilePathMethods;
import util.ImageFactory;
import util.PreferencesManager;
import validation.SAXValidator;


public class FormFieldLink extends FormField {

	/**
	 * A link (stored in the VALUE attribute of the dataField) that is the
	 * URL to a local file or a web-page.
	 * This will be passed to the BareBonesBrowserLauncher when user clicks button. 
	 * This should open a web page, but also it seems to launch an appropriate application
	 * for other files. 
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
	
	Icon linkLocalIcon = ImageFactory.getInstance().getIcon(ImageFactory.LINK_LOCAL_ICON);

	Icon linkRelativeIcon = ImageFactory.getInstance().getIcon(ImageFactory.LINK_RELATIVE_ICON);
	
	Icon editorLinkIcon = ImageFactory.getInstance().getIcon(ImageFactory.LINK_SCIENCE_ICON);

	Icon editorRelativeLinkIcon = ImageFactory.getInstance().getIcon(ImageFactory.LINK_SCIENCE_RELATIVE_ICON);
	
	Icon brokenLinkIcon = ImageFactory.getInstance().getIcon(ImageFactory.FILE_CLOSE_ICON);
	
	
	/**
	 * A custom dialog that contains the fileChooser for getting a link from users. 
	 * Also contains a checkBox for "Relative Link";
	 */
	JDialog customGetLinkDialog;
	
	/**
	 * This is the type of link that is currently set for this field. 
	 * Eg. Local link, Relative link, Url link. 
	 * It is set by updateLink(), depending on the attribute retrieved from dataField. 
	 * Used to set the icon etc. 
	 */
	private int linkType;
	
	public static final int LOCAL_LINK = 0;
	
	public static final int RELATIVE_LINK = 1;
	
	public static final int URL_LINK = 2;
	
	public static final int LOCAL_EDITOR_LINK = 3;
	
	public static final int RELATIVE_EDITOR_LINK = 4;
	
	public static final int BROKEN_LINK = 5;
	
	/**
	 * The button that users click to choose a link. 
	 * Provides a pop-up menu with options..
	 */
	JButton getLinkButton;
	
	public FormFieldLink(IDataFieldObservable dataFieldObs) {
		super(dataFieldObs);
		
		Border toolBarButtonBorder = new EmptyBorder(2,2,2,2);
		
		
		linkButton = new JButton();
		linkButton.addActionListener(new OpenLinkActionListener());
		linkButton.setBorder(toolBarButtonBorder);
		linkButton.setBackground(null);
		linkButton.setCursor(handCursor);
		linkButton.setFont(LINK_FONT);
		linkButton.setForeground(Color.BLUE);
		horizontalBox.add(linkButton);
		
		
		Icon chooseLinkIcon = ImageFactory.getInstance().getIcon(ImageFactory.WRENCH_ICON);
		
		Action[] getLinkActions = new Action[] {
				new GetURLAction(),
				new GetLinkPathAction()};
		getLinkButton = new PopupMenuButton("Choose a link to a URL or local file", 
				chooseLinkIcon, getLinkActions);
		
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
		
		if (name.equals(DataFieldConstants.URL_LINK) || 
				name.equals(DataFieldConstants.ABSOLUTE_FILE_LINK) ||
				name.equals(DataFieldConstants.RELATIVE_FILE_LINK)) {
			
			/*
			 * Make a map with all values null, then update one.
			 */
			HashMap<String, String> newValues = new HashMap<String, String>();
			newValues.put(DataFieldConstants.URL_LINK, null);
			newValues.put(DataFieldConstants.ABSOLUTE_FILE_LINK, null);
			newValues.put(DataFieldConstants.RELATIVE_FILE_LINK, null);
			
			newValues.put(name, value);
			
			// Updates new values, and adds to undo queue as one action. 
			dataField.setAttributes("Link", newValues, true);
		}
			
	}
	
	/**
	 * This listener for the linkButton uses BareBonesBrowserLauncher, passing it
	 * URLlink. 
	 * @author will
	 *
	 */
	public class OpenLinkActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {

			if (linkType==LOCAL_EDITOR_LINK || linkType==RELATIVE_EDITOR_LINK) {
				if (model != null) {
					model.openThisFile(new File(URLlink));
				}
			}
			else
				/*
				 * If not null, Check that the URLlink is a valid URL...
				 */
				if (URLlink != null) {
					try {
						URL validUrl = new URL(URLlink);
						
						// if not, add the file extension...
					} catch (MalformedURLException ex) {
						 if (System.getProperty("os.name").startsWith("Mac OS")) {
							 URLlink = "file://" + URLlink;
					     } else {
					    	 URLlink = "file:///" + URLlink;
					    }
					}
				}
			BareBonesBrowserLaunch.openURL(URLlink);
		}
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
			
			/*
			 * If the user did not input a valid URL, try adding "http://"
			 */
			try {
				URL validUrl = new URL(url);
			} catch (MalformedURLException ex) {
				// This will make a valid URL. BUT may not be valid link.
				url = "http://" + url;
			}
			
			// Save the URL to dataField
			saveLinkToDataField(DataFieldConstants.URL_LINK, url);
		}
	}
	
	public class GetLinkPathAction extends AbstractAction {
		
		public GetLinkPathAction() {
			putValue(Action.NAME, "Set Link to local file");
			putValue(Action.SHORT_DESCRIPTION, "Choose a file, that will be linked from this file");
			putValue(Action.SMALL_ICON, linkLocalIcon); 
		}
		
		public void actionPerformed(ActionEvent e) {
			getAndSaveLink();
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
		URLlink = dataField.getAttribute(DataFieldConstants.ABSOLUTE_FILE_LINK);
		if (URLlink != null) {
			
			File linkedFile = new File(URLlink);
			if (! linkedFile.exists()) {
				linkType = BROKEN_LINK;
			} else {
				
				if ((isEditorFileExtension(URLlink))
						&& (SAXValidator.isFileEditorFile(new File(URLlink))))
							linkType = LOCAL_EDITOR_LINK;
				else
					linkType = LOCAL_LINK;
				
			}

		}
		
		// if null, check the Relative file path attribute..
		else {
			URLlink = dataField.getAttribute(DataFieldConstants.RELATIVE_FILE_LINK);
			// .. and if not null, convert it to absolute path.
			if (URLlink != null) {
				
				File linkedFile = new File(URLlink);
				if (! linkedFile.exists()) {
					linkType = BROKEN_LINK;
				} else {
				
					File editorFile = ((DataField)dataField).getNode().getTree().getFile();
					URLlink = FilePathMethods.getAbsolutePathFromRelativePath(editorFile, URLlink);
					
					if ((isEditorFileExtension(URLlink)) 
						&& (SAXValidator.isFileEditorFile(linkedFile)))
						linkType = RELATIVE_EDITOR_LINK;
					else
						linkType = RELATIVE_LINK;
				}
			}
		}

		
		// if still null, check the URL 
		if (URLlink == null) {
			URLlink = dataField.getAttribute(DataFieldConstants.URL_LINK);
			linkType = URL_LINK;
		}
		
		
		System.out.println("FormFieldLink updateLink() " + URLlink);
		refreshLink();
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
	 * Displays the current value of URLlink. 
	 */
	public void refreshLink() {
		
		if (URLlink == null) {
			linkButton.setText("");
			linkButton.setToolTipText("No link set");
			linkButton.setIcon(linkLocalIcon); 
			linkButton.setEnabled(false);
			return;
		} 
		
		int len = URLlink.length();
		linkButton.setText(len < 30 ? URLlink : "..." + URLlink.substring(len-29, len));
		linkButton.setToolTipText("Open the link to: " + URLlink);
		linkButton.setEnabled(true);
		
		switch (linkType) {
			case LOCAL_LINK: 
				linkButton.setIcon(linkLocalIcon);
				break;
			case RELATIVE_LINK:
				linkButton.setIcon(linkRelativeIcon);
				break;
			case LOCAL_EDITOR_LINK:
				linkButton.setIcon(editorLinkIcon);
				break;
			case RELATIVE_EDITOR_LINK:
				linkButton.setIcon(editorRelativeLinkIcon);
				break;
			case URL_LINK:
				linkButton.setIcon(wwwIcon);
				break;
			case BROKEN_LINK:
				linkButton.setIcon(brokenLinkIcon);
				linkButton.setEnabled(false);
				linkButton.setToolTipText("FILE NOT FOUND: " + URLlink);
				break;
		}

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
		return new String[] {DataFieldConstants.ABSOLUTE_FILE_LINK, 
				DataFieldConstants.RELATIVE_FILE_LINK,
				DataFieldConstants.URL_LINK};
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
}
