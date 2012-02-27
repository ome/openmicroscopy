/*
 * org.openmicroscopy.shoola.agents.editor.view.Editor 
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
import java.awt.Component;
import java.awt.Point;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.editor.browser.Browser;
import org.openmicroscopy.shoola.agents.events.editor.ShowEditorEvent;
import org.openmicroscopy.shoola.util.ui.component.ObservableComponent;
import pojos.FileAnnotationData;
import pojos.GroupData;

/** 
 * Defines the interface provided by the editor component.
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
public interface Editor
	extends ObservableComponent
{

	/** Indicates to save the file locally. */
	public static final int SAVE_LOCALLY = 0;
	
	/** Indicates to save the file locally. */
	public static final int SAVE_SERVER = 1;
	
	/** Possible extension of the experiment. */
	public static final String EXPERIMENT_EXTENSION = 
		ShowEditorEvent.EXPERIMENT_EXTENSION;
	
	/** Indicates to create a blank protocol. */
	public static final int 	PROTOCOL = Browser.PROTOCOL;
	
	/** Indicates to create a blank experiment. */
	public static final int 	EXPERIMENT = Browser.EXPERIMENT;
	
	/** Flag to denote the <i>New</i> state. */
	public static final int     NEW = 1;

	/** Flag to denote the <i>Loading</i> state. */
	public static final int     LOADING = 2;

	/** Flag to denote the <i>Ready</i> state. */
	public static final int     READY = 3;

	/** Flag to denote the <i>Discarded</i> state. */
	public static final int     DISCARDED = 4;

	/** Flag to denote the <i>Saving</i> state. */
	public static final int     SAVING = 5;
	
	/** Identifies the group menu. */
	public static final int     GROUP_MENU = 100;
	
	/**
	 * Starts the data loading process when the current state is {@link #NEW} 
	 * and puts the window on screen.
	 * If the state is not {@link #NEW}, then this method simply moves the
	 * window to front.
	 * 
	 * @throws IllegalStateException If the current state is {@link #DISCARDED}.  
	 */
	public void activate();

	/**
	 * Transitions the viewer to the {@link #DISCARDED} state.
	 * Any ongoing data loading is cancelled.
	 */
	public void discard();

	/** Closes the editor. */
	public void close();
	
	/**
	 * Queries the current state.
	 * 
	 * @return One of the state flags defined by this interface.
	 */
	public int getState();

	/**
	 * Returns true if the file has been edited (and not yet saved)
	 * 
	 * @return		see above. 
	 */
	public boolean hasDataToSave();
	
	/**
	 * Callback used by data loaders to provide the viewer with feedback about
	 * the data retrieval.
	 * 
	 * @param description   Textual description of the ongoing operation.
	 * @param hide          Pass <code>true</code> to indicate tha the work 
	 * 						is done, <code>false</otherwise> otherwise.
	 */
	public void setStatus(String description, boolean hide);

	/** Cancels any ongoing data loading. */
	public void cancel();

	/**
	 * Sets the loaded file.
	 * 
	 * @param fa    The file annotation corresponding to the file or 
	 * 				<code>null</code>.
	 * @param file  The loaded file.
	 */
	public void setFileToEdit(FileAnnotationData fa, File file);

	/**
	 * Returns the title of this editor.
	 * 
	 * @return See above.
	 */
	public String getEditorTitle();
	
	/**
	 * Registers the listener.
	 * 
	 * @param listener The listener to register.
	 */
	public void registerBrowserListener(PropertyChangeListener listener);
	
	/**
	 * Opens a file that exists on the local machine.
	 * (The file is "in hand", not on the server)
	 * 
	 * @param file		The file to open in a new Editor window. 
	 */
	public void openLocalFile(File file);
	
	/**
	 * Creates and opens a new editor with a blank file for user to start editing
	 */
	public void newBlankFile();
	
	/**
	 * Tells this editor to display a new blank file for user to start editing
	 */
	public void setBlankFile();
	
	/**
	 * Invokes when the file has been saved to the server.
	 * 
	 * @param data The annotation.
	 */
	public void onFileSave(FileAnnotationData data);
	
	/**
     * Sets the Edited state of the Browser. 
     * Delegates to this method on the Browser
     * 
     * @param editable	Pass to <code>true</code> if the file has been edited,
     * 					<code>false</code> otherwise.
     */
    public void setEdited(boolean editable);
    
    /**
     * Returns <code>true</code> if the file is an experiment,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isExperiment();
    
    /** Deletes the Experiment Info of the file in the Browser. */
	public void deleteExperimentInfo();
	
	/**
	 * Returns <code>true</code> if the currently logged in user is the
	 * owner of the file.
	 * 
	 * @return See above.
	 */
	public boolean isUserOwner();
	
	/**
	 * Saves the currently-edited file to the location it came from (as defined
	 * in the model). 
	 * If the current file is local, saves there, overwriting the old file. 
	 * If file is on server...
	 * 
	 * @return See above.
	 */
	public boolean saveCurrentFile();
	
	/**
	 * Saves the file locally or server side depending on the passed index.
	 * Returns <code>true</code> if the save was successful, <code>false</code>
	 * otherwise.
	 * 
	 * @param object The object to handle.
	 * @param index  One of the <code>Save</code> constants defined by this 
	 * 				 class.
	 * @return 
	 */
	public boolean save(Object object, int index);
	
	/**
	 * Saves the file either asynchronously or not.
	 * 
	 * @param asynch Passed <code>true</code> to save asynchronously, 
	 * 				<code>false</code> otherwise.
	 */
	public void save(boolean asynch);
	
	/**
	 * Returns the view.
	 * 
	 * @return See above.
	 */
    public JFrame getUI();

    /**
     * Returns the group corresponding to the security context.
     * 
     * @return See above.
     */
	public GroupData getSelectedGroup();
    
	/**
	 * Brings up the menu on top of the specified component at 
	 * the specified location.
	 * 
	 * @param menuID The id of the menu.
	 * @param invoker The component that requested the pop-up menu.
	 * @param loc The point at which to display the menu, relative to the
	 *            <code>component</code>'s coordinates.
	 */
	public void showMenu(int menuID, Component c, Point p);

	/**
	 * Sets the group to used if the file is saved back to the server.
	 * 
	 * @param groupID The id of the group.
	 */
	void setUserGroup(long groupID);

}
