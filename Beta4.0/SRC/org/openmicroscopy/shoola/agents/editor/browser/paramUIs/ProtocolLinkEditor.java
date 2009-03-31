 /*
 * org.openmicroscopy.shoola.agents.editor.browser.paramUIs.ProtocolLinkEditor 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.editor.browser.paramUIs;

//Java imports

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.EditorAgent;
import org.openmicroscopy.shoola.agents.editor.IconManager;
import org.openmicroscopy.shoola.agents.editor.browser.FieldPanel;
import org.openmicroscopy.shoola.agents.editor.model.params.EditorLinkParam;
import org.openmicroscopy.shoola.agents.editor.model.params.IParam;
import org.openmicroscopy.shoola.agents.editor.model.params.TextParam;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomButton;
import org.openmicroscopy.shoola.agents.editor.uiComponents.PopupMenuButton;
import org.openmicroscopy.shoola.agents.events.editor.EditFileEvent;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.filter.file.EditorFileFilter;
import org.openmicroscopy.shoola.util.ui.InputDialog;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.filechooser.FileChooser;

/** 
 * UI for viewing and editing a link to an Editor file. Either a local file
 * identified by absolute file path, or a file on the server, identified by Id. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class ProtocolLinkEditor 
	extends AbstractParamEditor 
	implements ActionListener, 
		PropertyChangeListener
{
	
	/**
	 * The Icon to indicate that the current link is to a local editor file
	 */
	private Icon 				editorLinkIcon;
	
	/**
	 * This icon is displayed if the link is broken (file not found)
	 */
	private Icon 				brokenLinkIcon;

	
	/**
	 * A button that has blue underlined text displaying a link
	 */
	private JButton 			linkButton;
	
	/**
	 * The button that users click to choose a link. 
	 * Provides a pop-up menu with options..
	 */
	private JButton 			getLinkButton;
	
	/**
	 * The ID of the file on the server. 
	 */
	private Long				fileID;
	
	private String 				filePath;
	
	/**
	 * Initialises the UI components
	 */
	private void initialise() 
	{
		IconManager imF = IconManager.getInstance();
		editorLinkIcon = imF.getIcon(IconManager.OMERO_EDITOR);
		brokenLinkIcon = imF.getIcon(IconManager.FILE_CLOSE_ICON);
		Icon chooseLinkIcon = imF.getIcon(IconManager.WRENCH_ICON);
		
		linkButton = new CustomButton();
		linkButton.addActionListener(this);
		linkButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		linkButton.setForeground(Color.BLUE);
		
		Action[] getLinkActions = new Action[] {
				new GetIDAction(),
				new GetLinkPathAction()};
		getLinkButton = new PopupMenuButton("Choose a link an Ediot  or local file", 
				chooseLinkIcon, getLinkActions);
	}
	
	/**
	 * Builds the UI. Adds a link and a get-link button.
	 */
	private void buildUI() 
	{	
		add(linkButton);
		
		add(getLinkButton);
	}
	
	/**
	 * This updates the UI with the 'value' of the LINK for this parameter.
	 * If this parameter has multiple values, it will use the first. 
	 */
	private void updateLink() {

		String editorLink = getParameter().getAttribute(TextParam.PARAM_VALUE);
		
		if (editorLink == null) {
			linkButton.setText("");
			linkButton.setToolTipText("No link set");
			//linkButton.setIcon(linkLocalIcon); 
			linkButton.setEnabled(false);
			return;
		} 
		
		if (EditorLinkParam.isLinkValidId(editorLink)) {
			fileID = new Long(editorLink);
		} else {
			filePath = editorLink;
		}
		
		
		if (filePath != null) {
		
			int len = filePath.length();
			linkButton.setText(len < 30 ? filePath : "..." + 
					filePath.substring(len-29, len));
			linkButton.setToolTipText("Open the local Editor file: " + filePath);
			linkButton.setEnabled(true);
		
			File linkedFile = new File(filePath);
			if (! linkedFile.exists()) {
				linkButton.setIcon(brokenLinkIcon);
				linkButton.setEnabled(false);
				linkButton.setToolTipText("FILE NOT FOUND: " + filePath);
			}
			return;
		}
		
		if (fileID > 0) {
			linkButton.setText("File ID: " + fileID);
			linkButton.setToolTipText("Open the Editor file on the server, ID: "
					+ fileID);
			linkButton.setEnabled(true);
		}
	
	}

	/**
	 * An Action that is used in the get-link pop-up menu. 
	 * Allows users to enter an ID of a Protocol file on the server.  
	 * 
	 * @author will
	 *
	 */
	private class GetIDAction extends AbstractAction 
	{
		
		/**
		 * Creates an instance. Sets the Name, Description and Icon. 
		 */
		public GetIDAction() 
		{
			boolean server = EditorAgent.isServerAvailable();
			putValue(Action.NAME, "File on Server");
			putValue(Action.SHORT_DESCRIPTION, 
					server ? "Choose the ID of a file on the server." : 
						"Server not available");
			putValue(Action.SMALL_ICON, editorLinkIcon); 
			setEnabled(server);
		}
		
		/**
		 * Opens a dialog box for users to enter a URL
		 * 
		 * @see ActionListener#actionPerformed(ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) 
		{
			JFrame f = EditorAgent.getRegistry().getTaskBar().getFrame();
			InputDialog dialog = new InputDialog(f, 
					"Enter ID of Editor File on Server", "");
			
			int option = dialog.centerMsgBox();
			if (option == InputDialog.SAVE) {
				String newFileID = dialog.getText();
				
				// could check valid ID (integer) etc? 
				if (EditorLinkParam.isLinkValidId(newFileID)) {
					attributeEdited(TextParam.PARAM_VALUE, newFileID);
				} else {
					UserNotifier un = EditorAgent.getRegistry().getUserNotifier();
					un.notifyInfo("Not valid ID", 
							"Did not enter a valid Editor file ID");
				}
			}
		}
	}

	/**
	 * An Action that is used in the get-link pop-up menu. 
	 * Allows users to choose a link to a  local file using a file chooser. 
	 * 
	 * @author will
	 *
	 */
	private class GetLinkPathAction extends AbstractAction 
	{
		
		/**
		 * Creates an instance. Sets the Name, Description, and Icon
		 */
		public GetLinkPathAction() 
		{
			putValue(Action.NAME, "Set Link to local file");
			putValue(Action.SHORT_DESCRIPTION, "Choose a file," +
					" that will be linked from this file");
			putValue(Action.SMALL_ICON, editorLinkIcon); 
		}
		
		/**
		 * Opens a dialog box for users to choose a local file. 
		 * 
		 * @see ActionListener#actionPerformed(ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) 
		{
			List<FileFilter>		filters;
			filters = new ArrayList<FileFilter>();
		       filters.add(new EditorFileFilter());
			
			FileChooser chooser = new FileChooser(null, FileChooser.LOAD, 
					"Open File", "Choose a file to open in the Editor", 
					filters);
			chooser.addPropertyChangeListener(
					FileChooser.APPROVE_SELECTION_PROPERTY, 
					ProtocolLinkEditor.this);
			UIUtilities.centerAndShow(chooser);
		}
	}

	/**
	 * Creates an instance of the class. 
	 * Initiates, then builds the UI. 
	 * 
	 * @param param		The parameter to edit by this field
	 */
	public ProtocolLinkEditor(IParam param) 
	{	
		super(param);
		
		initialise();
		
		buildUI();
		
		// Set the link, based on the value of the relative attributes
		updateLink();
	}
	
	/**
	 * This listener for the linkButton uses BareBonesBrowserLauncher, passing it
	 * URLlink. 
	 * 
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {

		if (filePath != null) {
			
			File f = new File(filePath);
			// if link to local file that exists, open in Editor. 
			if (f.exists())
				EditorAgent.openLocalFile(f);
			
		}
		else if (fileID > 0) {
			EventBus bus = MetadataViewerAgent.getRegistry().getEventBus();
			bus.post(new EditFileEvent(fileID));
		}
	}

	/**
	 * Calls the super.attributeEdited(), then 
	 * fires propertyChange to refresh link, and size of panel
	 * 
	 * @see AbstractParamEditor#attributeEdited(String, Object)
	 */
	public void attributeEdited(String attributeName, Object newValue) 
	{	 
		super.attributeEdited(attributeName, newValue);
		
		firePropertyChange(FieldPanel.UPDATE_EDITING_PROPERTY, null, null);
	}

	/**
	 * @see ITreeEditComp#getEditDisplayName()
	 */
	public String getEditDisplayName() { return "Edit Link"; }

	public void propertyChange(PropertyChangeEvent evt) {
		
		if (FileChooser.APPROVE_SELECTION_PROPERTY.equals(evt.getPropertyName())) 
		{
			File f = (File) evt.getNewValue();
			
			attributeEdited(TextParam.PARAM_VALUE, f.getAbsolutePath());
		}
	}

}
