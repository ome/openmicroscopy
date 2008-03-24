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
import java.awt.Component;
import java.awt.Point;
import java.util.List;
import javax.swing.JComponent;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.browser.TreeBrowserDisplay;
import org.openmicroscopy.shoola.util.ui.component.ObservableComponent;
import pojos.AnnotationData;
import pojos.DataObject;

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
	
	/** Inidicates to layout all the components vertically. */
	public static final int		VERTICAL_LAYOUT = 0;
	
	/** Inidicates to layout all the components vertically. */
	public static final int		GRID_LAYOUT = 1;
	
	/** 
	 * Constrain indicating to add an external component to the 
	 * top left corner.
	 */
	public static final int 	TOP_LEFT = 0;
	
	/** Bound property indicating to save the data. */
	public static final String	SAVE_DATA_PROPERTY = "saveData";
	
	/** Bound property indicating to clear the data to save. */
	public static final String	CLEAR_SAVE_DATA_PROPERTY = "clearSaveData";
	
	/** Flag to denote the <i>New</i> state. */
	public static final int     NEW = 1;

	/** Flag to denote the <i>Loading Metadata</i> state. */
	public static final int     LOADING_METADATA = 2;

	/** Flag to denote the <i>Ready</i> state. */
	public static final int     READY = 3;

	/** Flag to denote the <i>Discarded</i> state. */
	public static final int     DISCARDED = 4;
	
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

	/**
	 * Queries the current state.
	 * 
	 * @return One of the state flags defined by this interface.
	 */
	public int getState();

	/**
	 * Brings up the menu.
	 * 
	 * @param invoker	The component that requested the popup menu.
	 * @param loc		The location of mouse click.
	 */
	public void showMenu(Component invoker, Point loc);

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
	 * @param data		The data object to annotate.
	 */
	public void saveData(List<AnnotationData> toAdd, 
						List<AnnotationData> toRemove, DataObject data);
	
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
	 * Adds the external passed component to the specified location.
	 * 
	 * @param external	The component to add. Mustn't be <code>null</code>.
	 * @param location	One of the location constrains defined by this class.
	 */
	public void addExternalComponent(JComponent external, int location);
	
	/** Brings up on screen the image's information. */
	public void showImageInfo();

	/**
	 * Refreshes the view when the metadata has been saved.
	 * 
	 * @param dataObject The updated object.
	 */
	public void onDataSave(DataObject dataObject);
	
}
