/*
 * org.openmicroscopy.shoola.agents.metadata.view.MetadataViewer 
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
package org.openmicroscopy.shoola.agents.metadata.view;


//Java imports
import java.util.Collection;
import java.util.List;
import javax.swing.JComponent;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.browser.TreeBrowserDisplay;
import org.openmicroscopy.shoola.env.data.util.StructuredDataResults;
import org.openmicroscopy.shoola.util.ui.component.ObservableComponent;
import pojos.AnnotationData;
import pojos.DataObject;
import pojos.ExperimenterData;

/** 
 * Defines the interface provided by the viewer component. 
 * The Viewer provides a component hosting a browser for metadata.
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
 * @since OME3.0
 */
public interface MetadataViewer
	extends ObservableComponent
{
	
	/** Bound property indicating that the data have been saved. */
	public static final String	ON_DATA_SAVE_PROPERTY = "onDataSave";
	
	/** Bound property indicating to save the data. */
	public static final String	SAVE_DATA_PROPERTY = "saveData";
	
	/** Bound property indicating to clear the data to save. */
	public static final String	CLEAR_SAVE_DATA_PROPERTY = "clearSaveData";
	
	/** Bound property indicating that the experimenter has been updated. */
	public static final String	EXPERIMENTER_UPDATED_PROPERTY = 
		                                                "experimenterUpdated";
	
	/**
	 * Bound property indicating that parents of the currently edited objects
	 * are loaded.
	 */
	public static final String	LOADING_PARENTS_PROPERTY = "loadingParents";

	/** Flag to denote the <i>New</i> state. */
	public static final int     NEW = 1;

	/** Flag to denote the <i>Loading Metadata</i> state. */
	public static final int     LOADING_METADATA = 2;

	/** Flag to denote the <i>Ready</i> state. */
	public static final int     READY = 3;

	/** Flag to denote the <i>Discarded</i> state. */
	public static final int     DISCARDED = 4;
	
	/** Flag to denote the <i>Batch Saving</i> state. */
	public static final int 	BATCH_SAVING = 5;
	
	/** Flag to denote the <i>Batch Saving</i> state. */
	public static final int 	SAVING = 6;
	
	/**
	 * Starts the data loading process when the current state is {@link #NEW} 
	 * and puts the window on screen.
	 * If the state is not {@link #NEW}, then this method simply moves the
	 * window to front.
	 * 
	 * @param channelData 	The channel data if already loaded, 
	 * 						<code>null</code> otherwise.
	 * @throws IllegalStateException If the current state is {@link #DISCARDED}.  
	 */
	public void activate(List channelData);

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
	 * Cancels any ongoing data loading. 
	 * 
	 * @param refNode The node of reference
	 */
	public void cancel(TreeBrowserDisplay refNode);

	/**
	 * Loads the metadata related to the passed node.
	 * 
	 * @param node The node to handle.
	 */
	public void loadMetadata(TreeBrowserDisplay node);
	
	/**
	 * Feeds the metadata back to the viewer.
	 * 
	 * @param node		The node to add the data to.
	 * @param result	The result to feed back.
	 */
	public void setMetadata(TreeBrowserDisplay node, Object result);
	
	/**
	 * Returns the UI used to select the metadata.
	 * 
	 * @return See above.
	 */
	public JComponent getSelectionUI();
	
	/**
	 * Returns the UI used to select the metadata.
	 * 
	 * @return See above.
	 */
	public JComponent getEditorUI();
	
	/**
	 * Returns the component hosted by the view.
	 * 
	 * @return See above.
	 */
	public JComponent getUI();
	
	/**
	 * Sets the root of the metadata browser.
	 * 
	 * @param root The objec to set.
	 */
	public void setRootObject(Object root);

	/**
	 * Loads the parent containers of the object hosted by the passed node.
	 * 
	 * @param node The node to handle.
	 */
	public void loadContainers(TreeBrowserDisplay node);
	
	/**
	 * Sets the containers.
	 * 
	 * @param node		The node to handle.
	 * @param result	The value to set.
	 * @see #loadContainers(TreeBrowserDisplay)
	 */
	public void setContainers(TreeBrowserDisplay node, Object result);
	
	/**
	 * Saves the annotations back to the server.
	 * 
	 * @param toAdd		The annotations to add or update.
	 * @param toRemove	The annotations to remove.
	 * @param metadata	The metadata to save.
	 * @param data		The data object to annotate.
	 */
	public void saveData(List<AnnotationData> toAdd, 
						List<AnnotationData> toRemove, List<Object> metadata,
						DataObject data);
	
	/**
	 * Returns <code>true</code> if data to save, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	public boolean hasDataToSave();

	/** Saves the data if any to save. */
	public void saveData();
	
	/** Clears the data to save. */
	public void clearDataToSave();

	/**
	 * Refreshes the view when the metadata has been saved.
	 * 
	 * @param dataObject The updated object.
	 */
	public void onDataSave(List<DataObject> dataObject);
	
	/**
	 * Sets to <code>true</code> if single selection, 
	 * to <code>false</code> if multi selection.
	 * 
	 * @param single Pass <code>true</code> when single selection, 
	 * 				 <code>false</code> otherwise.
	 */
	public void setSelectionMode(boolean single);
	
	/** 
	 * Returns <code>true</code> if the model is in single mode,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isSingleMode();
	
	/**
	 * Sets the nodes related to the root nodes when multi selection is
	 * on.
	 * 
	 * @param nodes The nodes to set.
	 */
	public void setRelatedNodes(Collection nodes);

	/**
	 * Updates the view when the experimented details have been modified.
	 * 
	 * @param data The data to update.
	 */
	public void onExperimenterUpdated(ExperimenterData data);

	/**
	 * Loads the containers hosting the currently edited object.
	 */
	public void loadParents();
	
	/**
	 * Returns the metadata linked to the currently edited object
	 * or <code>null</code> if not loaded.
	 * 
	 * @return See above.
	 */
	public StructuredDataResults getStructuredData();
	
    /**
     * Sets to <code>true</code> if loading data, to <code>false</code>
     * otherwise.
     * 
     * @param busy 	Pass <code>true</code> while loading data, 
     * 				<code>false</code> otherwise.
     */
	public void setStatus(boolean busy);

	/** Displays the tag wizard. */
	public void showTagWizard();
	
}
