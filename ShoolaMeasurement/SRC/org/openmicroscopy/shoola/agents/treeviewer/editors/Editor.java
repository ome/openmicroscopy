/*
 * org.openmicroscopy.shoola.agents.treeviewer.editors.Editor
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.agents.treeviewer.editors;




//Java imports
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JRootPane;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.util.ui.component.ObservableComponent;
import pojos.DataObject;


/** 
 * The component hosting the editor view.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public interface Editor
    extends ObservableComponent
{
    
    /** Flag to denote the <i>New</i> state. */
    public static final int         NEW = 1;
    
    /** Flag to denote the <i>Discarded</i> state. */
    public static final int         DISCARDED = 2;
    
    /** Flag to denote the <i>Loading Classification</i> state. */
    public static final int         LOADING_CLASSIFICATION = 4;
    
    /** Flag to denote the <i>Loading CHANNEL DATA</i> state. */
    public static final int         LOADING_CHANNEL_DATA = 5;

    /** Flag to denote the <i>Save Edition</i> state. */
    public static final int         SAVE_EDITION = 6;
    
    /** Flag to denote the <i>Loading Archived</i> state. */
    public static final int         LOADING_ARCHIVED = 7;
    
    /** Flag to denote the <i>Ready</i> state. */
    public static final int         READY = 8;
    
    /** Identifies the <code>Create</code> type for the editor. */
    public static final int         CREATE_EDITOR = TreeViewer.CREATE_EDITOR;
    
    /** Identifies the <code>Edit</code> type for the editor. */
    public static final int         PROPERTIES_EDITOR = 
                                                TreeViewer.PROPERTIES_EDITOR;
    
	/** The index of the <code>Properties</code> pane. */
	public static final int			PROPERTIES_INDEX = 0;
    
	/** The index of the <code>Permissions</code> pane. */
	public static final int			PERMISSIONS_INDEX = 1;
	
	/** The index of the <code>Info</code> pane. */
	public static final int			INFO_INDEX = 2;
	
	/** The index of the <code>Annotation sub</code> pane. */
	public static final int			ANNOTATION_INDEX = 0;
	
	/** The index of the <code>classification sub</code> pane. */
	public static final int			CLASSIFICATION_INDEX = 1;
	
    /** Bounds property to indicate to close the {@link Editor}. */
    public static final String      CLOSE_EDITOR_PROPERTY = "closeEditor";
    
    /** 
     * Closes the {@link Editor}. 
     * 
     * @throws IllegalStateException If the current state is
     *                               {@link #DISCARDED}.
     */
    void close();
    
    /**
     * Creates or updates the specified <code>DataObject</code> depending
     * on the specified operation.
     * 
     * @param object    The object to handle.
     * @param operation The type of operation. 
     */
    void saveObject(DataObject object, int operation);

    /** Reloads the classifications. */
    void loadClassifications();
    
    /** Retrieves the emission wavelengths for the set of pixels. */
    void retrieveChannelsData();   
    
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
    
    /**
     * Sets the thumbnail associated to the currently edited Image.
     * 
     * @param thumbnail The thumbnail to set.
     */
    public void setThumbnail(BufferedImage thumbnail);
    
    /**
     * Sets the retrieved classfication paths.
     * 
     * @param paths The paths to set.
     */
    public void setRetrievedClassification(Set paths);

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
     * Sets the size of the UI component. This method should be invoked
     * rather than directly invoking setSize(Dimension) on the returned value
     * of {@link #getUI()}.
     * 
     * @param d The dimension to set.
     */
    public void setSize(Dimension d);
    
    /**
     * Sets the result of the save operation.
     * 
     * @param object    The <code>DataObject</code> saved.
     * @param operation The type of save i.e. <code>CREATE</code>, 
     *                  <code>UPDATE</code> or <code>DELETE</code>.
     */
    public void setSaveResult(DataObject object, int operation);
    
    /**
     * Returns <code>true</code> if the editor has some data to save 
     * before closing, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean hasDataToSave();

    /** 
     * Saves the data before closing the editor. This method
     * should only be invoked after asking a question to the user.
     */
    public void saveData();

    /**
     * Sets the values of the emission wavelengths.
     * 
     * @param emissionWaves The emission wavelengths.
     */
    public void setChannelsData(List emissionWaves);
    
    /** Retrieves the annotations for the edited <code>DataObject</code>. */
    public void retrieveAnnotations();
    
    /** 
     * Returns the index of the selected sub pane.
     * 
     * @return See above.
     */
    public int getSelectedSubPane();
    
    /** Retrieves the thumbnail when the state is <code>READY</code>. */
    public void retrieveThumbnail();

    /** Indicates to set the focus on the name area. */
	public void setFocusOnName();

	/** 
	 * Sets the default button.
	 * 
	 * @param rootPane The root pane of the frame hosting the editor.
	 */
	public void setDefaultButton(JRootPane rootPane);
    
	/**
	 * Adds the selected nodes along the edited one.
	 * 
	 * @param nodes The nodes to add.
	 */
	public void addSiblings(List nodes);
	
}
