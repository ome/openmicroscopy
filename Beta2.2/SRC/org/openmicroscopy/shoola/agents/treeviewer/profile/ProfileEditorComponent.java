/*
 * org.openmicroscopy.shoola.agents.treeviewer.profile.ProfileEditorComponent 
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
import javax.swing.JComponent;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;
import pojos.ExperimenterData;

/** 
 * Implements the {@link ProfileEditor} interface to provide the functionality
 * required of the tree viewer component.
 * This class is the component hub and embeds the component's MVC triad.
 * It manages the component's state machine and fires state change 
 * notifications as appropriate, but delegates actual functionality to the
 * MVC sub-components.
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
class ProfileEditorComponent
	extends AbstractComponent
	implements ProfileEditor
{

    /** The Model sub-component. */
    private ProfileEditorModel     model;
    
    /** The Controller sub-component. */
    private ProfileEditorControl   controller;
    
    /** The View sub-component. */
    private ProfileEditorUI        view;
    
    /**
     * Creates a new instance.
     * The {@link #initialize() initialize} method should be called straight 
     * after to complete the MVC set up.
     * 
     * @param model The Model sub-component.
     */
    ProfileEditorComponent(ProfileEditorModel model)
    {
        if (model == null) throw new NullPointerException("No model."); 
        this.model = model;
        controller = new ProfileEditorControl(this);
        view = new ProfileEditorUI();
    }
    
    /** Links up the MVC triad. */
    void initialize()
    {
        controller.initialize(view);
        view.initialize(model, controller);
    }
    
    /**
     * Implemented as specified by the {@link ProfileEditor} interface.
     * @see ProfileEditor#activate()
     */
	public void activate() {}

    /**
     * Implemented as specified by the {@link ProfileEditor} interface.
     * @see ProfileEditor#cancel()
     */
	public void cancel()
	{
		if (model.getState() != DISCARDED) {
			model.cancel();
			fireStateChange(); 
		}
	}

    /**
     * Implemented as specified by the {@link ProfileEditor} interface.
     * @see ProfileEditor#close()
     */
	public void close() {
		if (model.getState() == DISCARDED)
            throw new IllegalStateException(
                    "This method cannot be invoked in the DISCARDED state.");
        firePropertyChange(CLOSE_PROFILE_EDITOR_PROPERTY, Boolean.FALSE, 
        				Boolean.TRUE);
	}

    /**
     * Implemented as specified by the {@link ProfileEditor} interface.
     * @see ProfileEditor#discard()
     */
	public void discard()
	{
		if (model.getState() != DISCARDED) {
			model.discard();
			fireStateChange(); 
		}
	}

    /**
     * Implemented as specified by the {@link ProfileEditor} interface.
     * @see ProfileEditor#getState()
     */
	public int getState() { return model.getState(); }

    /**
     * Implemented as specified by the {@link ProfileEditor} interface.
     * @see ProfileEditor#getUI()
     */
	public JComponent getUI() { return view; }

    /**
     * Implemented as specified by the {@link ProfileEditor} interface.
     * @see ProfileEditor#changePassword(String, String)
     */
	public void changePassword(String oldPassword, String password)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
                    "This method cannot be invoked in the DISCARDED state.");
		model.firePasswordChange(oldPassword, password);
		fireStateChange();
	}

    /**
     * Implemented as specified by the {@link ProfileEditor} interface.
     * @see ProfileEditor#save(ExperimenterData)
     */
	public void save(ExperimenterData exp)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
                    "This method cannot be invoked in the DISCARDED state.");
		model.fireEditionSave(exp);
		fireStateChange();
	}

    /**
     * Implemented as specified by the {@link ProfileEditor} interface.
     * @see ProfileEditor#passwordChanged(Boolean)
     */
	public void passwordChanged(Boolean result)
	{
		if (model.getState() != SAVE_EDITION) return;
		UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
		if (result.booleanValue()) {
			un.notifyInfo(ProfileEditorUI.DIALOG_TITLE, "The password " +
					"has been successfully modified.");
		} else {
			un.notifyInfo(ProfileEditorUI.DIALOG_TITLE, "The password " +
			"couldn't be modified. Please try again.");
		}
		model.setState(READY);
		view.passwordChanged();
		fireStateChange();
	}

    /**
     * Implemented as specified by the {@link ProfileEditor} interface.
     * @see ProfileEditor#experimenterChanged(ExperimenterData)
     */
	public void experimenterChanged(ExperimenterData exp)
	{
		if (model.getState() != SAVE_EDITION) return;
		model.setExperimenter(exp);
		fireStateChange();
	}

	/**
     * Implemented as specified by the {@link ProfileEditor} interface.
     * @see ProfileEditor#setDiskSpace(long, long)
     */
	public void setDiskSpace(long free, long used)
	{
		if (model.getState() != LOADING) return;
		model.setState(READY);
		view.setDiskSpace(free, used);
		fireStateChange();
	}

	/**
     * Implemented as specified by the {@link ProfileEditor} interface.
     * @see ProfileEditor#setDiskSpace(long, long)
     */
	public void getDiskSpace()
	{
		switch (model.getState()) {
			case DISCARDED:
			case SAVE_EDITION:
				return;
		}
		model.fireSpaceRetrieval();
		fireStateChange();
	}
	
}
