/*
 * org.openmicroscopy.shoola.agents.fsimporter.view.FSImporter 
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
package org.openmicroscopy.shoola.agents.fsimporter.view;


//Java imports
import java.awt.Component;
import java.awt.Point;
import java.io.File;
import java.util.Collection;
import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.treeviewer.BrowserSelectionEvent;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.env.data.model.DiskQuota;
import org.openmicroscopy.shoola.env.data.model.ImportableObject;
import org.openmicroscopy.shoola.util.ui.component.ObservableComponent;

import pojos.DataObject;
import pojos.GroupData;

/** 
 * Defines the interface provided by the importer component. 
 * The Viewer provides a top-level window hosting UI components to interact 
 * with the instance.
 *
 * When the user quits the window, the {@link #discard() discard} method is
 * invoked and the object transitions to the {@link #DISCARDED} state.
 * At which point, all clients should de-reference the component to allow for
 * garbage collection.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public interface Importer
	extends ObservableComponent
{

	/** Bound property indicating to the group has been modified. */
	public static final String	CHANGED_GROUP_PROPERTY = "changedGroup";
	
	/** Identifies the <code>Personal</code> menu. */
	public static final int     PERSONAL_MENU = 100;
	
	/** Indicates that the type is for project. */
	public static final int		PROJECT_TYPE = 
		BrowserSelectionEvent.PROJECT_TYPE;
	
	/** Indicates that the type is for Screening data. */
	public static final int		SCREEN_TYPE = BrowserSelectionEvent.SCREEN_TYPE;
	
	/** Flag to denote the <i>New</i> state. */
	public static final int     NEW = 1;

	/** Flag to denote the <i>Ready</i> state. */
	public static final int     READY = 2;

	/** Flag to denote the <i>Discarded</i> state. */
	public static final int     DISCARDED = 3;
	
	/** Flag to denote the <i>Importing</i> state. */
	public static final int     IMPORTING = 4;
	
	/** Flag to denote the <i>Loading Container</i> state. */
	public static final int     LOADING_CONTAINER = 5;
	
	/** Flag to denote the <i>Creating Container</i> state. */
	public static final int     CREATING_CONTAINER = 6;
	
	/**
	 * Starts the data loading process when the current state is {@link #NEW} 
	 * and puts the window on screen.
	 * If the state is not {@link #NEW}, then this method simply moves the
	 * window to front.
	 * 
	 * @param type One of the types constants defined by this class. 
	 * @param selectedContainer The default container.
	 * @param objects The available containers.
	 * @throws IllegalStateException If the current state is {@link #DISCARDED}.  
	 */
	public void activate(int type, TreeImageDisplay selectedContainer, 
			Collection<TreeImageDisplay> objects);
	
	/**
	 * Transitions the viewer to the {@link #DISCARDED} state.
	 * Any ongoing data loading is cancelled.
	 */
	public void discard();

	/**
	 * Queries the current state.
	 * 
	 * @return One of the state flags defined by this interface.
	 */
	public int getState();

	/**
	 * Imports the specified files.
	 * 
	 * @param data The data to import.
	 */
	public void importData(ImportableObject data);

	/**
	 * Sets the collection of existing tags if any.
	 * 
	 * @param tags The collection of tags.
	 */
	public void setExistingTags(Collection tags);
	
	/** Invokes to load the existing tags if not already loaded. */
	public void loadExistingTags();
	
	/**
	 * Sets the imported file.
	 * 
	 * @param f The file imported.
	 * @param result Depends on the result, it can be an image, an exception.
	 * @param index The index of the UI components.
	 */
	public void setImportedFile(File f, Object result, int index);

	/**
	 * Returns the view.
	 * 
	 * @return See above.
	 */
	public JFrame getView();

	/** Submits the files that failed to import. */
	public void submitFiles();
	
	/**
	 * Removes the specified import element.
	 * 
	 * @param element The element to remove.
	 */
	void removeImportElement(Object element);

	/** Cancels any on-going import. */
	public void cancel();
	
	/**
	 * Cancels the loading of images.
	 * 
	 * @param id The identifier of the import element.
	 */
	public void cancelImport(int id);

	/** Cancels the on-going import. */
	public void cancelImport();
	
	/**
	 * Returns <code>true</code> if errors to send, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	public boolean hasFailuresToSend();
	
	/**
	 * Returns <code>true</code> if files to re-import, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	public boolean hasFailuresToReimport();
	
	/** 
	 * Sets the used and available disk space.
	 * 
	 * @param qota The value to set.
	 */
	public void setDiskSpace(DiskQuota quota);
	
	/** 
	 * Closes the dialog and cancels the import in the queue if 
	 * selected by user.
	 */
	public void close();

	/**
	 * Moves the window to the front.
	 * @throws IllegalStateException If the current state is not
	 *                               {@link #DISCARDED}.
	 */
	public void moveToFront();
	
	/** Tries to re-import failed import. */
	public void retryImport();

	/**
	 * Returns <code>true</code> if it is the last file to import,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isLastImport();

	/**
	 * Sets the containers.
	 * 
	 * @param result The result to display
	 * @param refreshImport Pass <code>true</code> to refresh the on-going
	 * 						import, <code>false</code> otherwise.
	 * @param type 	The type of location to reload, either {@link #PROJECT_TYPE}
	 * 				or {@link #SCREEN_TYPE}.
	 */
	public void setContainers(Collection result, boolean refreshImport, 
			int type);

	/** 
	 * Reloads the containers where to load the data.
	 * 
	 * @param type 	The type of location to reload, either {@link #PROJECT_TYPE}
	 * 				or {@link #SCREEN_TYPE}.
	 */
	public void refreshContainers(int type);

	/** Cancels all the ongoing imports.*/
	public void cancelAllImports();

	/** 
	 * Notifies that the new object has been created.
	 * 
	 * @param d The newly created object.
	 * @param parent The parent of the object.
	 */
	public void onDataObjectSaved(DataObject d, DataObject parent);

	/**
	 * Creates the data object.
	 * 
	 * @param child The data object to create.
	 * @param parent The parent of the object or <code>null</code>.
	 */
	public void createDataObject(DataObject child, DataObject parent);
	
	/**
	 * Returns <code>true</code> if there are data to import,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean hasOnGoingImport();

	/**
	 * Brings up the menu on top of the specified component at 
	 * the specified location.
	 * 
	 * @param menuID    The id of the menu. One out of the following constants:
	 *                  {@link #PERSONAL_MENU}.
	 * @param invoker   The component that requested the pop-up menu.
	 * @param loc       The point at which to display the menu, relative to the
	 *                  <code>component</code>'s coordinates.
	 */
	public void showMenu(int personalMenu, Component source, Point point);

	/**
	 * Returns the currently selected group.
	 * 
	 * @return See above.
	 */
	GroupData getSelectedGroup();

	/**
	 * Sets the default group for the currently selected user and updates the 
	 * view.
	 * 
	 * @param group The group to set.
	 */
	void setUserGroup(GroupData group);

	/** Logs off from the current server.*/
	void logOff();

}
