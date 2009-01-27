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

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.editor.EditorAgent;
import org.openmicroscopy.shoola.agents.editor.actions.SaveNewCmd;
import org.openmicroscopy.shoola.agents.editor.browser.Browser;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.MessageBox;
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
	 * The {@link #initialize() initialize} method should be called straight 
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
	 * @see Editor#hasDataToSave()
	 */
	public boolean hasDataToSave() {  return model.hasDataToSave(); }

	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#discard()
	 */
	public void discard()
	{
		// if the file has been edited, ask the user if they want to save...
		if (model.hasDataToSave()) {
			
			MessageBox msg = new MessageBox(view, "Save Data", 
			"Before closing the Editor, do you want to save?");
			msg.addCancelButton();
			
			int option = msg.centerMsgBox();
			if (option == MessageBox.YES_OPTION) {
				
				// if Save, need to try save current file. 
				boolean saved = saveCurrentFile();
				
				if (saved) {
					model.discard();
				}
				// If that doesn't work, save as new file.. 
				else {
					SaveNewCmd save = new SaveNewCmd(this);
					save.execute();
					// don't discard in case user cancelled. 
				}
			
			}
			else if (option == MessageBox.NO_OPTION) {
				model.discard();
			}
		}
		else {
			// no data to save 
			model.discard();
		}
		
		// the EditorControl will handle view.close() if discard has been 
		// called. Otherwise, window will remain open. 
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
			view.setTitle(model.getFileName());
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
		
		EditorAgent.openLocalFile(file);
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
	public boolean saveCurrentFile()
	{
		long fileID = model.getFileID();
		// If no fileID, file is not saved on server. 
		if (fileID <= 0) {
			// Try to save locally... return success. 
			boolean b = model.saveLocalFile();
			if (b) {
				UserNotifier un = EditorAgent.getRegistry().getUserNotifier();
				un.notifyInfo("File Saved", "The File has been saved locally.");
			}
			return b;
		} 
	
		//This assumes that we are editing an existing file.
		File toEdit = model.getFileToEdit();
		if (toEdit != null) {
			model.fireFileSaving(toEdit);
			return true;
		} 
		
		return false;
	}
	
	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#saveFileLocally(File)
	 */
	public boolean saveFileLocally(File file)
	{
		if (model.saveFileAs(file)) {
			view.setTitle(model.getFileName());
			return true;
		}
		return false;
	}
	
	/** 
	 * Implemented as specified by the {@link Editor} interface.
	 * @see Editor#saveFileServer(String)
	 */
	public void saveFileServer(String fileName) 
	{
		model.fireFileSaving(new File(fileName));
		fireStateChange();
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
	
	/**
	 * This method allows the {@link EditorModel} to call 
	 * {@link #fireStateChange()} so that it can pass on any state changes
	 * from the browser
	 */
	void stateChanged() { fireStateChange(); }
	
}
