/*
 * org.openmicroscopy.shoola.agents.treeviewer.profile.ProfileEditorModel 
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

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.ExperimenterEditor;
import org.openmicroscopy.shoola.agents.treeviewer.PasswordEditor;
import org.openmicroscopy.shoola.agents.treeviewer.ProfileEditorLoader;

import pojos.ExperimenterData;
import pojos.GroupData;

/** 
 * The Model component in the <code>ProfileEditor</code> MVC triad.
 * This class tracks the <code>ProfileEditor</code>'s state and knows how to
 * initiate data retrievals. It also knows how to store and manipulate
 * the results. The {@link ProfileEditorComponent} intercepts the 
 * results of data loadings, feeds them back to this class and fires state
 * transitions as appropriate.
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
class ProfileEditorModel
{

    /** Holds one of the state flags defined by {@link ProfileEditor}. */
    private int					state;
    
    /** 
     * Will either be a data loader or
     * <code>null</code> depending on the current state. 
     */
    private ProfileEditorLoader	currentLoader;
    
	/** The edited experimenter. */
	private ExperimenterData 	experimenter;
	
	/** Reference to the component that embeds this model. */
	private ProfileEditor		component;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param experimenter The experimenter to edit.
	 */
	ProfileEditorModel(ExperimenterData experimenter)
	{
		this.experimenter = experimenter;
	}
	
    /**
     * Called by the <code>ProfileEditor</code> after creation to allow this
     * object to store a back reference to the embedding component.
     * 
     * @param component The embedding component.
     */
	void initialize(ProfileEditor component) { this.component = component; }
	
	/**
	 * Returns <code>true</code> if the edited experimenter is
	 * the user currently logged in, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isEditable()
	{
		return false;
		/*
		ExperimenterData exp = 
			(ExperimenterData) TreeViewerAgent.getRegistry().lookup(
		        LookupNames.CURRENT_USER_DETAILS);
		return (exp.getId() == experimenter.getId());
		*/
	}
	
	/**
	 * Returns <code>true</code> if the group's name is valid, 
	 * <code>false</code> otherwise.
	 * 
	 * @param g The group to check.
	 * @return See above.
	 */
	boolean isValidGroup(GroupData g)
	{
		if (g == null) return false;
		String name = g.getName();
		if ("user".equals(name) || "default".equals(name)) return false;
		return true;
	}
	
	/** 
	 * Returns the edited experimenter.
	 * 
	 * @return See above.
	 */
	ExperimenterData getUser() { return experimenter; }
	
	/**
	 * Returns the current state.
	 * 
	 * @return One of the flags defined by the {@link ProfileEditor} interface.  
	 */
	int getState() { return state; } 
	 
	/**
	 * Sets the object in the {@link ProfileEditor#DISCARDED} state.
	 * Any ongoing data loading will be cancelled.
	 */
	void discard()
	{
		cancel();
		state = ProfileEditor.DISCARDED;
	}

	/**
	 * Sets the object in the {@link ProfileEditor#READY} state.
	 * Any ongoing data loading will be cancelled.
	 */
	void cancel()
	{
		if (currentLoader != null) {
			currentLoader.cancel();
			currentLoader = null;
		}
		state = ProfileEditor.READY;
	}

	/**
	 * Modifies the password. 
	 * 
	 * @param oldPassword	The logged-in password.
	 * @param password		The new password to set.
	 * 						Mustn't be <code>null</code>.
	 */
	void firePasswordChange(String oldPassword, String password)
	{
		currentLoader = new PasswordEditor(component, oldPassword, password);
		currentLoader.load();
		state = ProfileEditor.SAVE_EDITION;
		
	}

	/**
	 * Fires an asynchonous call to update the user details.
	 * 
	 * @param exp The object holding the changes.
	 */
	void fireEditionSave(ExperimenterData exp)
	{
		currentLoader = new ExperimenterEditor(component, exp);
		currentLoader.load();
		state = ProfileEditor.SAVE_EDITION;
	}
	
	/**
	 * Sets the state.
	 * 
	 * @param state The value to set.
	 */
	void setState(int state) { this.state = state; }

	/**
	 * Sets the currently edited experimenter.
	 * 
	 * @param experimenter The value to set.
	 */
	void setExperimenter(ExperimenterData experimenter)
	{
		this.experimenter = experimenter;
		state = ProfileEditor.READY;
	}

}
