/*
 * org.openmicroscopy.shoola.agents.treeviewer.profile.ProfileEditor 
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
import org.openmicroscopy.shoola.util.ui.component.ObservableComponent;
import pojos.ExperimenterData;

/** 
 * Defines the interface provided by the profile editor component.
 * Allows the logged-in user to edit his/her profile, changes password,
 * manages the available OMERO servers.
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
public interface ProfileEditor
	extends ObservableComponent
{

    /** Bounds property to indicate to close the {@link ProfileEditor}. */
    public static final String      CLOSE_PROFILE_EDITOR_PROPERTY = 
    									"closeProfileEditor";
    
	/** Flag to denote the <i>New</i> state. */
    public static final int         NEW = 1;
    
    /** Flag to denote the <i>Discarded</i> state. */
    public static final int         DISCARDED = 2;
    
    /** Flag to denote the <i>Save Edition</i> state. */
    public static final int         SAVE_EDITION = 3;
    
    /** Flag to denote the <i>Laoading</i> state. */
    public static final int         LOADING = 4;
    
    /** Flag to denote the <i>Ready</i> state. */
    public static final int         READY = 5;
    
    /** 
     * Closes the {@link ProfileEditor}. 
     * 
     * @throws IllegalStateException If the current state is
     *                               {@link #DISCARDED}.
     */
    void close();
    
    /** Retrieves the used and free disk space. */
	void getDiskSpace();
	
    /**
     * Queries the current state.
     * 
     * @return One of the state flags defined by this interface.
     */
    public int getState();
    
    /**
     * Starts the data loading process when the current state is {@link #NEW} 
     * and puts the progress window on screen.
     * 
     * @throws IllegalStateException If the current state is {@link #DISCARDED}.  
     */
    public void activate();
    
    /**
     * Transitions the classifier to the {@link #DISCARDED} state.
     * Any ongoing data loading is cancelled.
     */
    public void discard();
    
    /** Cancels any ongoing data loading. */
    public void cancel();
    
    /** 
     * Returns the UI component. 
     * 
     * @return See above.
     * @throws IllegalStateException If the current state is
     *                               {@link #DISCARDED}.
     */
    public JComponent getUI();

	/**
	 * Modifies the password. 
	 * 
	 * @param oldPassword	The logged-in password.
	 * @param password		The new password to set.
	 * 						Mustn't be <code>null</code>.
	 */
	public void changePassword(String oldPassword, String password);

	/**
	 * Updates the currently edited user.
	 * 
	 * @param exp The object hosting the changes. Mustn't be <code>null</code>.
	 */
	public void save(ExperimenterData exp);

	/**
	 * Notifies that the password has been modified.
	 * 
	 * @param result 	Pass <code>true</code> if the update was successful,
	 * 					<code>false</code> otherwise.
	 */
	public void passwordChanged(Boolean result);
    
	/**
	 * Notifies that the password has been modified.
	 * 
	 * @param data The updated experimenter..
	 */
	public void experimenterChanged(ExperimenterData data);
	
	/**
	 * Sets the free and used disk space on the file system.
	 * 
	 * @param free 	The free space on the file system.
	 * @param used	The used space on the file system.
	 */
	public void setDiskSpace(long free, long used);

}
