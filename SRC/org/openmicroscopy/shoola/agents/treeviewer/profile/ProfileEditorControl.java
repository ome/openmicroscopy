/*
 * org.openmicroscopy.shoola.agents.treeviewer.profile.ProfileEditorControl 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.treeviewer.profile;


//Java imports
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openmicroscopy.shoola.util.ui.login.ServerEditor;

//Third-party libraries

//Application-internal dependencies
import pojos.ExperimenterData;

/** 
 * The {@link ProfileEditor}'s controller. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class ProfileEditorControl
	implements ChangeListener, PropertyChangeListener
{

    /** 
     * Reference to the {@link ProfileEditor} component, which, in this context,
     * is regarded as the Model.
     */
	private ProfileEditor 	model;
	
	/** Reference to the View. */
	private ProfileEditorUI	view;
	
    /**
     * Creates a new instance.
     * The {@link #initialize(ProfileEditorUI) initialize} method 
     * should be called straight 
     * after to link this Controller to the other MVC components.
     * 
     * @param model  Reference to the {@link ProfileEditor} component, which, in 
     *               this context, is regarded as the Model.
     *               Mustn't be <code>null</code>.
     */
	ProfileEditorControl(ProfileEditor model)
	{
		if (model == null)
			throw new IllegalArgumentException("No model.");
		this.model = model;
	}
    /**
     * Links this Controller to its View.
     * 
     * @param view   Reference to the View. Mustn't be <code>null</code>.
     */
	void initialize(ProfileEditorUI view)
	{
		if (view == null)
			throw new IllegalArgumentException("No view.");
		this.view = view;
		model.addChangeListener(this);
	}

	/**
	 * Modifies the password. Mustn't be <code>null</code>.
	 * 
	 * @param oldPassword	The logged-in password.
	 * @param password 		The new password to set.
	 */
	void changePassword(String oldPassword, String password)
	{
		model.changePassword(oldPassword, password);
	}

	/**
	 * Forwards call to update the edited user.
	 * 
	 * @param exp	The object hosting the new value.
	 */
	void save(ExperimenterData exp) { model.save(exp); }

	/**
	 * Forwards call to {@link ProfileEditor} to remove the 
	 * editor from the editor form the display.
	 */
	void close() { model.close(); }
	
	/**
	 * Reacts to state changes in the {@link ProfileEditor}.
     * @see ChangeListener#stateChanged(ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
	{
		switch (model.getState()) {
			case ProfileEditor.READY:
				
				break;
	
			case ProfileEditor.SAVE_EDITION:
				break;
			}
	}
	
	/**
	 * Reacts to property changed fire by the {@link ServerEditor}.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (ServerEditor.REMOVE_MESSAGE_PROPERTY.equals(name)) {
			view.showMessage(false, (JComponent) evt.getNewValue());
		} else if (ServerEditor.ADD_MESSAGE_PROPERTY.equals(name)) {
			view.showMessage(true, (JComponent) evt.getNewValue());
		}
	}

	
}
