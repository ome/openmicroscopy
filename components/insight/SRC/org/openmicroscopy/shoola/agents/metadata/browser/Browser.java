/*
 * org.openmicroscopy.shoola.agents.metadata.browser.Browser 
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
package org.openmicroscopy.shoola.agents.metadata.browser;


//Java imports
import java.util.Collection;
import java.util.List;
import javax.swing.JComponent;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.component.ObservableComponent;

/** 
 * Defines the interface provided by the viewer component. 
 * The Viewer provides a component allowing user to browse metadata in 
 * a Tree manner.
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
public interface Browser
	extends ObservableComponent
{

	/** The title related to this component. */
	public static final String 		TITLE = "Located in";
	
	/** Bound property indicating that nodes are selected. */
	public static final String		SELECTED_NODES_PROPERTY = "selectedNodes";

	/** Identifies the <code>Datasets</code> node menu item. */
	public static final String		DATASETS = "Datasets";
	
	/** Identifies the <code>Projects</code> node menu item. */
	public static final String		PROJECTS = "Projects";

	/** 
	 * Returns the view.
	 * 
	 * @return See above.
	 */
	public JComponent getUI();
	
	/**
	 * Sets the selected node while navigating the tree.
	 * 
	 * @param node The value to set.
	 */
	public void setSelectedNode(TreeBrowserDisplay node);
	
	/**
	 * Sets the collection of selected nodes.
	 * 
	 * @param nodes The value to set.
	 */
	public void setSelectedNodes(List<TreeBrowserDisplay> nodes);
	
	/**
	 * Returns the last selected node
	 * 
	 * @return See above.
	 */
	public TreeBrowserDisplay getLastSelectedNode();
	
	/**
	 * Sets the root of the tree.
	 * 
	 * @param refObject The object hosted by the root node of the tree.
	 * 					Mustn't be <code>null</code>.
	 */
	public void setRootObject(Object refObject);

	/**
	 * Loads the data for the passed node. The passed node is a 
	 * <code>Menu node</code>
	 * 
	 * @param node The node to handle. Mustn't be <code>null</code>.
	 */
	public void loadMetadata(TreeBrowserDisplay node);

	/**
	 * Converts the collection of parents into their corresponding UI 
	 * components and adds them to the passed node.
	 * 
	 * @param node		The node to handle. Mustn't be <code>null</code>.
	 * @param parents	Collection of parents to display.
	 */
	public void setParents(TreeBrowserDisplay node, Collection parents);
	
}
