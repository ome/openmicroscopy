 /*
 * org.openmicroscopy.shoola.agents.editor.preview.EditorPreview 
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
package org.openmicroscopy.shoola.agents.editor.preview;

//Java imports

import java.io.File;

import javax.swing.JComponent;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.EditorAgent;
import org.openmicroscopy.shoola.agents.editor.FileAnnotationLoader;
import org.openmicroscopy.shoola.agents.editor.browser.BrowserControl;
import org.openmicroscopy.shoola.agents.editor.browser.MetadataPanelsComponent;
import org.openmicroscopy.shoola.agents.util.editorpreview.PreviewPanel;

import pojos.FileAnnotationData;

/** 
 * This is the 'Main' class of the Editor preview component, which provides 
 * a summary view of an Editor file. If this file is on the server, the 
 * preview is generated from the File Annotation description, which is 
 * an XML String. 
 * If the Editor file is local, the whole document is opened in the usual
 * way and the tree Model is used to build the preview. 
 * In both cases, the information needed for the preview is not retrieved
 * until the user expands the preview pane, which calls
 * {@link #loadPreviewData()}.
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class EditorPreview 
	implements AnnotationHandler {
	
	/** A reference to the Browser Control, for getting file annotations etc */
	private BrowserControl 				controller;
	
	/** The ID of the file on the server. Will be null if file is local */
	private long 						fileID;
	
	/** The annotation of the file on the server. Used to retrieve description */
	private FileAnnotationData 			fileAnnotation;
	
	/** The path of the local file. Will be null if file is on the server */
	private String						filePath;
	
	/** 
	 * This panel takes the XML description and builds the preview panel.
	 * Not used if the file is local. 
	 */
	private EditorPreviewUI				view;
	
	/**
	 * This is the File-Annotation description. It is null when this class is
	 * created with fileID, but will be retrieved from the server when user
	 * expands the preview pane 
	 * @see {@link #loadPreviewData()}
	 */
	private String 						annotationDesc;
	
	/** The component hosting the image acquisition data. */
	private PreviewPanel				previewPanel;
	
	/** The panel that parses and displays the tree Model for a local file */
	private MetadataPanelsComponent 	mdc;
	
	/** The model that takes a local file and parses it to a tree model */
	private EditorPreviewModel 			model;
	
	/**
	 * Creates the Preview UI, which is an expandable pane that contains the
	 * appropriate preview panel. 
	 * 
	 * @param preview	The panel to display
	 * @param title		The initial title to display (will change when data loads)
	 */
	private void initialise(JPanel preview, String title)
	{
		view = new EditorPreviewUI(this, preview);
		view.setTitle(title);
	}

	/**
	 * Creates an instance of this preview for displaying a local file.
	 * Builds the un-populated UI.
	 * 
	 * @param filePath		The absolute file path of file to display. 
	 */
	public EditorPreview(String filePath, BrowserControl controller)
	{
		this.filePath = filePath;
		this.controller = controller;
		
		String title = "File not found";
		if (filePath != null) {
			File f = new File(filePath);
			if (f.exists()) {
				title = f.getName();
			} 
		}
		
		mdc = new MetadataPanelsComponent(null);
		initialise(mdc, title);
	}
	
	/**
	 * Makes an instance for viewing a preview of Editor file on the server.
	 * Builds the un-populated UI. 
	 * 
	 * @param fileID
	 */
	public EditorPreview(long fileID, BrowserControl controller) 
	{
		this.fileID = fileID;
		this.controller = controller;
		previewPanel = new PreviewPanel();
		initialise(previewPanel, "File ID: " + fileID);
	}
	
	/**
	 * Called by the UI when the panel expands to show the preview. 
	 * This retrieves the required info, either from the server or the local
	 * file, depending on whether we have an ID for server file. 
	 * When the data has been retrieved, it is used to populate the UI. 
	 */
	void loadPreviewData()
	{
		if (fileID != 0) {
			if (! EditorAgent.isServerAvailable()) {
				annotationDesc = 
					"<protocol><n>File ID: " + fileID + " on server</n>" +
					"<d>Can't show the preview of this file because " +
					"OMERO.editor is not connected to the server.</d>" +
					"</protocol>";
			}
			else if (annotationDesc == null) {
				// need to retrieve file annotation from server
				controller.getFileAnnotation(fileID, this);
			}
		}
		else if (filePath != null && model == null){
			
			File editorFile = new File(filePath);
			if (editorFile.exists()) {
				model = new EditorPreviewModel();
				model.setFileToEdit(editorFile);

				mdc.setTreeModel(model.getModel());
				view.setTitle(mdc.getProtocolTitle());
			}
		}
	}
	
	/**
	 * This allows classes to retrieve the UI for this component. 
	 * 
	 * @return	The UI. 
	 */
	public JComponent getUI()	{ return view; }

	public void handleAnnotation(FileAnnotationData fileAnnotation) {
		annotationDesc = fileAnnotation.getDescription();
		previewPanel.setDescriptionXml(annotationDesc);
		view.setTitle(previewPanel.getTitle());
	}

}