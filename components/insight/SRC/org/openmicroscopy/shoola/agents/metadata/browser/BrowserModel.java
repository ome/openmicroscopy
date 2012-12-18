/*
 * org.openmicroscopy.shoola.agents.metadata.browser.BrowserModel 
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewer;


/** 
 * Implements the {@link Browser} interface to provide the functionality
 * required of the browser viewer component, thus acting as the Model in MVC.
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
class BrowserModel
{

	/** The selected node. */
	private List<TreeBrowserDisplay>	selectedNodes;
	
	/** The parent of this browser. */
	private  MetadataViewer 			parent;
	
	/** The UI object hosting the edited root object. */
	private TreeBrowserSet				root;
	
	/** Reference to the component that embeds this model. */
	private Browser 					component;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param refObject	The object this browser originated from.
	 * @param parent	The parent of this browser.
	 */
	BrowserModel(Object refObject, MetadataViewer parent) 
	{
		selectedNodes = new ArrayList<TreeBrowserDisplay>();
		if (refObject != null)
			selectedNodes.add(new TreeBrowserSet(refObject));
		this.parent = parent;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param refObject	The object this browser originated from.
	 * @param parent	The parent of this browser.
	 * @param ui		Reference to the UI.
	 */
	BrowserModel(MetadataViewer parent) 
	{
		this(null, parent);
	}
	
	/**
	 * Called by the <code>Browser</code> after creation to allow this
	 * object to store a back reference to the embedding component.
	 * 
	 * @param component The embedding component.
	 */
	void initialize(Browser component) { this.component = component; }
	
	/**
	 * Returns the selected node.
	 * 
	 * @return See above.
	 */
	TreeBrowserDisplay getLastSelectedNode()
	{ 
		int n = selectedNodes.size();
		if (n == 0) return null;
		return selectedNodes.get(n-1); 
	}

	/**
	 * Returns the selected nodes.
	 * 
	 * @return See above.
	 */
	List<TreeBrowserDisplay> getSelectedNodes() { return selectedNodes; }
	
	/**
	 * Sets the selected nodes.
	 * 
	 * @param nodes The nodes to set.
	 */
	void setSelectedDisplays(List<TreeBrowserDisplay> nodes)
	{
		selectedNodes.clear();
		if (nodes == null) return;
		Iterator<TreeBrowserDisplay> i = nodes.iterator();
		while (i.hasNext()) 
			selectedNodes.add(i.next());
	}
	
	/**
	 * Sets the root node of the browser.
	 * 
	 * @param refObject The object to set.
	 */
	void setRootObject(Object refObject)
	{
		selectedNodes.clear();
		root = new TreeBrowserSet(refObject);
		selectedNodes.add(root);
	}

	/**
	 * Loads the data for the specified node.
	 * 
	 * @param node The node to handle.
	 */
	void loadMetadata(TreeBrowserDisplay node)
	{
		parent.loadContainers(node);
	}

	/**
	 * Returns the UI object hosting the edited object.
	 * 
	 * @return See above.
	 */
	TreeBrowserSet getRoot() { return root; }

}
