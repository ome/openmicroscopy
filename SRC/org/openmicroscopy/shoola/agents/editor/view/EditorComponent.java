/*
 * org.openmicroscopy.shoola.agents.editor.view.EditorComponent 
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
package org.openmicroscopy.shoola.agents.editor.view;

//Java imports
import java.io.File;

import javax.swing.JOptionPane;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.editor.EditorAgent;
import org.openmicroscopy.shoola.agents.editor.browser.Browser;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;

/** 
 * Implements the {@link Editor} interface to provide the functionality
 * required of the editor component.
 * This class is the component hub and embeds the component's MVC triad.
 * It manages the component's state machine and fires state change 
 * notifications as appropriate, but delegates actual functionality to the
 * MVC sub-components.
 *
 * @see org.openmicroscopy.shoola.agents.editor.view.EditorModel
 * @see org.openmicroscopy.shoola.agents.editor.view.EditorUI
 * @see org.openmicroscopy.shoola.agents.editor.view.EditorControl
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta3
 */
class EditorComponent
	extends AbstractComponent
	implements Editor
{

	/** The Model sub-component. */
	private EditorModel     model;

	/** The Controller sub-component. */
	private EditorControl	controller;

	/** The View sub-component. */
	private EditorUI       	view;
	
	/**
	 * Creates a new instance.
	 * The {@link #initialize() initialize} method should be called straigh 
	 * after to complete the MVC set up.
	 * 
	 * @param model The Model sub-component. Mustn't be <code>null</code>.
	 */
	EditorComponent(EditorModel model)
	{
		if (model == null) throw new NullPointerException("No model.");
		this.model = model;
		controller = new EditorControl(this);
		view = new EditorUI(model.getFileName());
	}

	/** Links up the MVC triad. */
	void initialize()
	{
		model.initialize(this);
		controller.initialize(view);
		view.initialize(controller, model);
	}
	
	/**
	 * Returns the Model sub-component.
	 * 
	 * @return See above.
	 */
	EditorModel getModel() { return model; }
	
	/** 
	 * Implemented as specified by the {@link ImViewer} interface.
	 * @see Editor#activate()
	 */
	public void activate()
	{
		int state = model.getState();
		switch (state) {
			case NEW:
				if (model.getFileID() != 0) {
					model.fireFileLoading();
					fireStateChange();
					//view.setOnScreen();	// called by EditorUI initialize()
				}
				else {
					view.deIconify();
				}
				break;
			case DISCARDED:
				throw new IllegalStateException(
						"This method can't be invoked in the DISCARDED state.");
			default:
				view.deIconify();
		}
	}

	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#discard()
	 */
	public void discard()
	{
		model.discard();
		view.close();
		fireStateChange();
	}

	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#getState()
	 */
	public int getState()
	{
		return model.getState();
	}

	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#setStatus(String, boolean)
	 */
	public void setStatus(String description, boolean hide)
	{
		view.setStatus(description, hide);
	}

	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#cancel()
	 */
	public void cancel()
	{
		if (model.getState() == DISCARDED) return;
		model.cancel();
	}

	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#setFileToEdit(File)
	 */
	public void setFileToEdit(File file)
	{
		if ((model.getState() != LOADING) && (model.getState() != NEW))
			throw new IllegalStateException("This method should only be " +
					"invoked in the LOADING or NEW states.");
					
		if (model.setFileToEdit(file)) {
			view.displayFile();
		}
		fireStateChange();
	}

	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#getEditorTitle()
	 */
	public String getEditorTitle()
	{
		if (model.getState() == DISCARDED) return "";
		return model.getFileName();
	}

	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#getBrowser()
	 */
	public Browser getBrowser() 
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException("This method should not be " +
			"invoked in the DISCARDED state.");
		return model.getBrowser();
	}

	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#openLocalFile(File file)
	 */
	public void openLocalFile(File file) {
		
		// gets a blank editor (one that has been created with a 'blank' model), 
		// OR an existing editor if one has the same 
		// file ID (will be 0 if editor file is local) and same file name, OR
		// creates a new editor model and editor with this new file. 
		Editor editor = EditorFactory.getEditor(file);
	
		// activates the editor
		// if the editor is 'blank' or has just been created (above), 
		// need to set the file
		if (editor != null) {
			if (editor.getState() == Editor.NEW)
				editor.setFileToEdit(file);
			
			// this simply brings the editor to the front / de-iconifies it.
			editor.activate();
		}
	}

	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * Creates and opens a new blank file in the browser, for a user to 
	 * begin editing. 
	 * 
	 * @see Editor#newBlankFile()
	 */
	public void newBlankFile() {
		// gets a new editor
		Editor editor = EditorFactory.getNewBlankEditor();
		
		// primes the editor to open new file and activates the editor
		if (editor != null) {
			editor.setBlankFile();
			
			// this simply brings the editor to the front / deiconifies it.
			editor.activate();
		}
	}
	
	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * Primes the model to display a blank file, and updates the view. 
	 * 
	 * @see Editor#setBlankFile()
	 */
	public void setBlankFile() {
		model.setBlankFile();
		view.displayFile();
		fireStateChange();
	}

	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * Saves the file in model. 
	 * @see Editor#saveCurrentFile()
	 */
	public void saveCurrentFile()
	{
		long fileID = model.getFileID();
		//Try to save it locally.
		if (fileID <= 0) {
			boolean b = model.saveLocalFile();
			if (b) {
				UserNotifier un = EditorAgent.getRegistry().getUserNotifier();
				un.notifyInfo("File Saved", "The File has been saved locally.");
			} else {
				//This assumes that we are editing an existing file.
				File toEdit = model.getFileToEdit();
				if (toEdit != null) {
					model.fireFileSaving(toEdit);
				} else {
					//Saves as...
					// Temporary fix to allow user to enter name...
					String fileName = JOptionPane.showInputDialog(null, 
							"Please enter a file name",
							"Save File to Server", JOptionPane.QUESTION_MESSAGE);
					if (! fileName.endsWith(".pro.xml")) {
						fileName = fileName + ".pro.xml";
					}
					File test = new File(fileName);
					model.fireFileSaving(test);
					fireStateChange();
				}
			}
		} else {
			//This assumes that we are editing an existing file.
			File toEdit = model.getFileToEdit();
			if (toEdit != null) {
				model.fireFileSaving(toEdit);
			} else {
				//Saves as
				File test = new File("testSaveServer.pro.xml");
				model.fireFileSaving(test);
				fireStateChange();
			}
		}
	}
	
	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#saveFileAs(File)
	 */
	public boolean saveFileAs(File file) {
		
		if (model.saveFileAs(file)) {
			view.setTitle(model.getFileName());
			return true;
		}
		return false;
	}

	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#onFileSave(boolean)
	 */
	public void onFileSave(boolean result)
	{
		UserNotifier un = EditorAgent.getRegistry().getUserNotifier();
		String message = 
			"An error occured while saving the file to the server.";
		if (result) message = "The File has been saved to the server.";
		un.notifyInfo("File Saved", message);
		model.setState(READY);
		fireStateChange();
	}
	
}
