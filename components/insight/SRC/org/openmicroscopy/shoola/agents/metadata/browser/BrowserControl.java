/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.metadata.browser;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.openmicroscopy.shoola.agents.events.hiviewer.Browse;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.metadata.actions.BrowserAction;
import org.openmicroscopy.shoola.agents.metadata.actions.CollapseAction;
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewer;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import omero.gateway.model.DataObject;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ProjectData;

/** 
 * The Browser's Controller.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
class BrowserControl
	implements PropertyChangeListener
{
	
	/** Identifies the <code>Collapse</code> action. */
	static final Integer    COLLAPSE = Integer.valueOf(0);

	/** Identifies the <code>Close</code> action. */
	static final Integer    CLOSE = Integer.valueOf(1);

	/** Identifies the <code>Sort</code> action. */
	static final Integer    SORT = Integer.valueOf(2);

	/** Identifies the <code>Sort by Date</code> action. */
	static final Integer    SORT_DATE = Integer.valueOf(3);
    
    /** Identifies the <code>Partial Name</code> action.*/
    static final Integer    PARTIAL_NAME = Integer.valueOf(4);
    
    /** Reference to the Model. */
    private Browser						model;
    
    /** Reference to the View. */
    private BrowserUI					view;
    
    /** Maps actions ids onto actual <code>Action</code> object. */
	private Map<Integer, BrowserAction>	actionsMap;
	
	/** Helper method to create all the UI actions. */
	private void createActions()
	{
		actionsMap.put(COLLAPSE, new CollapseAction(model));
	}
	
	/** Creates a new instance. */
    BrowserControl()
    {
    	actionsMap = new HashMap<Integer, BrowserAction>();
    }
    
    /**
     * Links this Controller to its Model and its View.
     * 
     * @param model	Reference to the Model. Mustn't be <code>null</code>.
     * @param view	Reference to the View. Mustn't be <code>null</code>.
     */
    void initialize(Browser model, BrowserUI view)
    {
        if (view == null) throw new NullPointerException("No view.");
        if (model == null) throw new NullPointerException("No model.");
        this.model = model;
        this.view = view;
        createActions();
    }

    /**
	 * Returns the action corresponding to the specified id.
	 * 
	 * @param id One of the flags defined by this class.
	 * @return The specified action.
	 */
    BrowserAction getAction(Integer id) { return actionsMap.get(id); }
    
    /** Reacts to mouse click in the tree. */
    void onClick()
    {
    	Object pathComponent;
    	JTree tree = view.getTreeDisplay();
    	TreePath[] paths = tree.getSelectionPaths();
    	if (paths == null) return;
    	int n = paths.length;
    	if (n == 0) return;
    	pathComponent = paths[0].getLastPathComponent();
    	//Check if alls node are of the same type.
    	if (!(pathComponent instanceof TreeBrowserDisplay)) return;
    	TreeBrowserDisplay node = (TreeBrowserDisplay) pathComponent;
    	List<TreeBrowserDisplay> l = new ArrayList<TreeBrowserDisplay>();
    	l.add(node);

    	if (l.size() != n) {
    		UserNotifier un = 
    			MetadataViewerAgent.getRegistry().getUserNotifier();
    		un.notifyInfo("Node selection", "You can only select " +
    				"node of the same type e.g. images.");
    	}
    	if (l.size() == 0) return;
    	model.setSelectedNodes(l);
    }

    /**
     * Reacts to tree expansion events.
     * 
     * @param node The selected node.
     */
	void onNodeNavigation(TreeBrowserDisplay node)
	{
		model.setSelectedNode(node);
		if (node.isChildrenLoaded()) return;
		view.addDefaultNode(node, BrowserUI.LOADING_MSG);
		model.loadMetadata(node);
	}
    
	/**
	 * Browses the collection of specified nodes.
	 * 
	 * @param nodes The nodes to browse.
	 */
	void browser(List<TreeBrowserDisplay> nodes)
	{
		if (nodes == null || nodes.size() == 0) return;
		Set<Long> ids = new HashSet<Long>();
		Iterator<TreeBrowserDisplay> i = nodes.iterator();
		TreeBrowserDisplay node;
		Object object;
		while (i.hasNext()) {
			node = i.next();
			object = node.getUserObject();
			if (object instanceof DataObject)
				ids.add(((DataObject) object).getId());
		}
		if (ids.size() == 0) return;
		node = model.getLastSelectedNode();
		object = node.getUserObject();
		int index = -1;
		if (object instanceof DatasetData)
			index = Browse.DATASETS;
		else if (object instanceof ProjectData)
			index = Browse.PROJECTS;
		if (index == -1) return;
	}

	/**
	 * Adds a dummy node to the view when the parents are loaded.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (MetadataViewer.LOADING_PARENTS_PROPERTY.equals(name)) {
			view.addDefaultNode(BrowserUI.LOADING_MSG);
		}
	}
	
}
