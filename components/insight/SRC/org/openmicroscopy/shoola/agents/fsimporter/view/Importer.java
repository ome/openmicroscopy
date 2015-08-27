/*
 * org.openmicroscopy.shoola.agents.fsimporter.view.FSImporter 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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

import java.awt.Component;
import java.awt.Point;
import java.util.Collection;

import javax.swing.JFrame;

import org.openmicroscopy.shoola.agents.events.treeviewer.BrowserSelectionEvent;
import org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponent;
import org.openmicroscopy.shoola.agents.fsimporter.util.ObjectToCreate;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.env.data.model.DiskQuota;
import org.openmicroscopy.shoola.env.data.model.ImportableFile;
import org.openmicroscopy.shoola.env.data.model.ImportableObject;
import org.openmicroscopy.shoola.env.data.model.ResultsObject;
import org.openmicroscopy.shoola.util.ui.component.ObservableComponent;

import pojos.DataObject;
import pojos.FileAnnotationData;
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
 * @since 3.0-Beta4
 */
public interface Importer
	extends ObservableComponent
{

    /** Bound property indicating to the group has been modified. */
    public static final String CHANGED_GROUP_PROPERTY = "changedGroup";

    /** Identifies the <code>Personal</code> menu. */
    public static final int PERSONAL_MENU = 100;

    /** Indicates that the type is for project. */
    public static final int PROJECT_TYPE = BrowserSelectionEvent.PROJECT_TYPE;

    /** Indicates that the type is for Screening data. */
    public static final int SCREEN_TYPE = BrowserSelectionEvent.SCREEN_TYPE;

    /** Flag to denote the <i>New</i> state. */
    public static final int NEW = 1;

    /** Flag to denote the <i>Ready</i> state. */
    public static final int READY = 2;

    /** Flag to denote the <i>Discarded</i> state. */
    public static final int DISCARDED = 3;

    /** Flag to denote the <i>Importing</i> state. */
    public static final int IMPORTING = 4;

    /**
     * Starts the data loading process when the current state is {@link #NEW}
     * and puts the window on screen.
     * If the state is not {@link #NEW}, then this method simply moves the
     * window to front.
     * 
     * @param type One of the types constants defined by this class.
     * @param selectedContainer The default container.
     * @param objects The available containers.
     * @param userId The id of the user to import for.
     * @throws IllegalStateException If the current state is {@link #DISCARDED}.
     */
    public void activate(int type, TreeImageDisplay selectedContainer,
            Collection<TreeImageDisplay> objects, long userId);

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
    public void uploadComplete(ImportableFile f, Object result, int index);

    /**
     * Sets the import log file.
     *
     * @param collection A collection of file annotations linked to the file set.
     * @param fileSetID The id of the file set.
     * @param index The index of the UI component.
     */
    public void setImportLogFile(Collection<FileAnnotationData> collection,
            long fileSetID, int index);

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
    public boolean hasFailuresToReupload();

    /** 
     * Sets the used and available disk space.
     * 
     * @param quota The value to set.
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

    /**
     * Tries to re-upload the failed upload(s).
     * 
     * @param fc The file to upload or <code>null</code>.
     */
    public void retryUpload(FileImportComponent fc);

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
     *                      import, <code>false</code> otherwise.
     * @param changeGroup Flag indicating that the group has been modified
     *                    if <code>true</code>, <code>false</code> otherwise.
     * @param type The type of location to reload, either {@link #PROJECT_TYPE}
     *             or {@link #SCREEN_TYPE}.
     * @param userID The id of the user the data are for.
     */
    public void setContainers(Collection result, boolean refreshImport,
            boolean changeGroup, int type, long userID);

    /** 
     * Reloads the containers where to load the data.
     * 
     * @param details Import location details to reload.
     */
    public void refreshContainers(ImportLocationDetails details);

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
     * @param data The object hosting information about the object to create.
     */
    public void createDataObject(ObjectToCreate data);

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
     * @param personalMenu The id of the menu. One out of the following constants:
     *               {@link #PERSONAL_MENU}.
     * @param source The component that requested the pop-up menu.
     * @param point The point at which to display the menu, relative to the
     *            <code>component</code>'s coordinates.
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

    /**
     * Returns <code>true</code> if the agent is the entry point
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean isMaster();

    /**
     * Returns the display mode.
     * 
     * @return See above.
     */
    int getDisplayMode();

    /**
     * Returns <code>true</code> if there are files to upload,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean hasOnGoingUpload();

    /** 
     * Indicates that the import is complete for the specified component.
     * 
     * @param component The component to handle.
     */
    void onImportComplete(FileImportComponent component);

    /**
     * Indicates that the import is complete for the specified component.
     * 
     * @param component The component to handle.
     */
    void onUploadComplete(FileImportComponent component);

    /**
     * Sets the result e.g. thumbnails corresponding to the images imported.
     * 
     * @param result The result to set.
     * @param component The component hosting the result.
     */
    void setImportResult(Object result, Object component);

    /**
     * Returns the id of the user to import data for.
     *
     * @return See above.
     */
    long getImportFor();

    /**
     * Returns <code>true</code> if the user can import the data for other
     * users, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean canImportAs();

    /**
     * Returns the groups the current user is a member of.
     * 
     * @return See above.
     */
    Collection<GroupData> getAvailableGroups();

    /**
     * Returns <code>true</code> if the group is a system group,
     * <code>false</code> otherwise.
     *
     * @param groupID The identifier of the group.
     * @param key One of the constants defined by GroupData.
     * @return See above.
     */
    boolean isSystemGroup(long groupID, String key);

    /**
     * Imports the results.
     *
     * @param object The object to handle.
     * @param importImage Pass <code>true</code> to import the image first,
     *                    <code>false</code> otherwise.
     */
    void importResults(ResultsObject object, boolean importImage);
}
