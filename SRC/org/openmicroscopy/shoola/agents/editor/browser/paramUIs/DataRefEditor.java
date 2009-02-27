 /*
 * org.openmicroscopy.shoola.agents.editor.browser.paramUIs.DataRefEditor 
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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.IconManager;
import org.openmicroscopy.shoola.agents.editor.browser.paramUIs.editTemplate.AttributeEditLine;
import org.openmicroscopy.shoola.agents.editor.browser.paramUIs.editTemplate.AttributeEditNoLabel;
import org.openmicroscopy.shoola.agents.editor.model.DataReference;
import org.openmicroscopy.shoola.agents.editor.model.IAttributes;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomLabel;
import org.openmicroscopy.shoola.agents.editor.uiComponents.ImagePreview;
import org.openmicroscopy.shoola.agents.editor.uiComponents.PopupMenuButton;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.filechooser.FileChooser;

/** 
 * UI component for displaying and editing a {@link DataReference}
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class DataRefEditor 
	extends AbstractParamEditor
	implements PropertyChangeListener
{
	/**
	 * The parent UI that handles editing of name etc. 
	 * Add this class as a propertyChangeListener to the name editor etc. 
	 */
	private PropertyChangeListener parent;
	
	private JLabel				linkLabel;

	/**
	 * The button that users click to choose a link. 
	 * Provides a pop-up menu with options..
	 */
	private JButton 			getLinkButton;
	
	/**
	 * The Icon to indicate that the current link is a URL
	 */
	private Icon 				wwwIcon;
	
	/**
	 * The Icon to indicate that the current link is local
	 */
	private Icon 				linkLocalIcon;
	
	/** Panel for displaying an image if the link is to a local image file */
	private	JPanel 				imagePreview;
	
	/**
	 * Initialises the UI components
	 */
	private void initialise() 
	{
		IconManager imF = IconManager.getInstance();
		Icon chooseLinkIcon = imF.getIcon(IconManager.WRENCH_ICON);
		wwwIcon = imF.getIcon(IconManager.WWW_ICON);
		linkLocalIcon = imF.getIcon(IconManager.LINK_LOCAL_ICON);
		
		IAttributes dataRef = getParameter();
		String ref = dataRef.getAttribute(DataReference.REFERENCE);
		if (ref == null)	ref = "No link set";
		linkLabel = new CustomLabel(ref);
		
		String desc = dataRef.getAttribute(DataReference.DESCRIPTION);
		String size = dataRef.getAttribute(DataReference.SIZE);
		String lastM = dataRef.getAttribute(DataReference.MODIFICATION_TIME);
		String mime = dataRef.getAttribute(DataReference.MIME_TYPE);
		String creat = dataRef.getAttribute(DataReference.CREATION_TIME);
		
		String toolTip = "<html>" + 
			ref + "<br>" +
			"Description: " + (desc == null ? "" : desc) + "<br>" +
			"Size: " + (size == null ? "" : size) + "<br>" +
			"Last Modified: " + (lastM == null ? "" : formatUTC(lastM)) + "<br>"
			+ "Created: " + (creat == null ? "" : formatUTC(creat)) + "<br>"
			+ "Mime type: " + (mime == null ? "" : mime) + "</html>";
		
		linkLabel.setToolTipText(toolTip);
		
		Action[] getLinkActions = new Action[] {
				new GetURLAction(),
				new GetLinkPathAction()};
		getLinkButton = new PopupMenuButton("Choose a link to a URL or local file", 
				chooseLinkIcon, getLinkActions);
		
		if (DataReference.showImage(ref)) {
			imagePreview = new ImagePreview(ref);
		}
	}
	
	/**
	 * Simple method for formatting UTC millisecs (as a string) into 
	 * a date. 
	 * 
	 * @param utcMillis
	 * @return
	 */
	private String formatUTC(String utcMillis)
	{
		SimpleDateFormat fmt = new SimpleDateFormat("d MMM, yyyy");
		if (utcMillis == null) {
			return "";
		}
		long millis = new Long(utcMillis);
		Date date = new Date(millis);
		return fmt.format(date);
	}
	
	/**
	 * Builds the UI. Adds a link and a get-link button.
	 */
	private void buildUI() 
	{	
		setLayout(new BorderLayout());
		// add name field
		AttributeEditLine nameEditor = new AttributeEditNoLabel
			(getParameter(), DataReference.NAME, "Data Link Name");
		nameEditor.addPropertyChangeListener
			(ITreeEditComp.VALUE_CHANGED_PROPERTY, parent);
		
		add(nameEditor, BorderLayout.NORTH);
		
		add(linkLabel, BorderLayout.CENTER);
		
		add(getLinkButton, BorderLayout.WEST);
		
		if (imagePreview != null) {
			add(imagePreview, BorderLayout.SOUTH);
		}
	}
	
	/**
	 * Opens a fileChooser, for the user to pick a file.
	 */
	private void getAndSaveLink() 
	{	
		FileChooser chooser = new FileChooser(null, FileChooser.LOAD, 
				"Open File", "Choose a file to open in the Editor");
		chooser.addPropertyChangeListener(
				FileChooser.APPROVE_SELECTION_PROPERTY, this);
		UIUtilities.centerAndShow(chooser);
	}
	
	/**
	 * Creates an instance of the class. 
	 * Initiates, then builds the UI. 
	 * 
	 * @param param		The parameter to edit by this field
	 */
	public DataRefEditor(IAttributes param, PropertyChangeListener parent) 
	{	
		super(param);
		
		this.parent = parent;
		
		initialise();
		
		buildUI();
	}
	
	/**
	 * An Action that is used in the get-link pop-up menu. 
	 * Allows users to enter a URL in a dialog box. 
	 * 
	 * @author will
	 *
	 */
	private class GetURLAction extends AbstractAction 
	{
		
		/**
		 * Creates an instance. Sets the Name, Description and Icon. 
		 */
		public GetURLAction() 
		{
			putValue(Action.NAME, "Set URL");
			putValue(Action.SHORT_DESCRIPTION, "Link to a web page");
			putValue(Action.SMALL_ICON, wwwIcon); 
		}
		
		/**
		 * Opens a dialog box for users to enter a URL
		 * 
		 * @see ActionListener#actionPerformed(ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) 
		{
			String url = (String)JOptionPane.showInputDialog(
	                null, "Enter URL:", "Enter URL", JOptionPane.PLAIN_MESSAGE,
	                wwwIcon, null, "http://");
	
			if ((url == null) || (url.length() == 0)) // user canceled
				return;
			
			// If the user did not input a valid URL, try adding "http://"
			try {
				new URL(url);
			} catch (MalformedURLException ex) {
				// This will make a valid URL. BUT may not be valid link.
				url = "http://" + url;
			}
			
			// Save the URL to dataField
			attributeEdited(DataReference.REFERENCE, url);
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
			putValue(Action.SMALL_ICON, linkLocalIcon); 
		}
		
		/**
		 * Opens a dialog box for users to choose a local file. 
		 * 
		 * @see ActionListener#actionPerformed(ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) 
		{
			getAndSaveLink();
		}
	}
	
	/**
    * Responds to the user choosing a file to link.
    * 
    * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
    */
	public void propertyChange(PropertyChangeEvent evt) 
	{
		String name = evt.getPropertyName();
		if (FileChooser.APPROVE_SELECTION_PROPERTY.equals(name)) {
			File f = (File) evt.getNewValue();
					
			Map<String, String> edits = new HashMap<String, String>();
			// set reference/path to file, and last modified time. 
			edits.put(DataReference.REFERENCE, f.getAbsolutePath());
			edits.put(DataReference.MODIFICATION_TIME, f.lastModified()+"");
			// set unknown variables to null
			edits.put(DataReference.MIME_TYPE, null);
			edits.put(DataReference.SIZE, null);
			edits.put(DataReference.CREATION_TIME, null);
			
			attributeEdited("Data Link", edits);
		}
	}
	
	/**
	 * @see ITreeEditComp#getEditDisplayName()
	 */
	public String getEditDisplayName() { return "Edit Link"; }
	
}
