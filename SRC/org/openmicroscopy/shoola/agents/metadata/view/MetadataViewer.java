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
import java.awt.image.BufferedImage;
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
	 * Sets the thumbnail associated to the edited object.
	 * 
	 * @param thumbnail The value to set.
	 */
	public void setThumbnail(BufferedImage thumbnail);

	public void loadContainers(TreeBrowserDisplay node);
	
	public void setContainers(TreeBrowserDisplay node, Object result);

	public void saveData(List<AnnotationData> toAdd, 
						List<AnnotationData> toRemove, DataObject data);
	
}
