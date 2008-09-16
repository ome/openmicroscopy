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
import org.openmicroscopy.shoola.agents.editor.browser.Browser;
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
				model.fireFileLoading();
				fireStateChange();
				//view.setOnScreen();	// called by EditorUI initialize()
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
					
		model.setFileToEdit(file);
		view.displayFile();
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
		
		Editor editor = EditorFactory.getEditor(file);
	
		if (editor != null) {
			if (editor.getState() == Editor.NEW)
				editor.setFileToEdit(file);
			
			editor.activate();
		}
	}
}
