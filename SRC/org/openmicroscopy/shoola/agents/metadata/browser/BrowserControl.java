/*
 * org.openmicroscopy.shoola.agents.metadata.browser.BrowserControl 
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JTree;
import javax.swing.tree.TreePath;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.metadata.actions.BrowserAction;
import org.openmicroscopy.shoola.agents.metadata.actions.CollapseAction;
import org.openmicroscopy.shoola.env.ui.UserNotifier;

/** 
 * The Browser's Controller.
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
class BrowserControl
{
	
	/** Identifies the <code>Collapse</code> action. */
	static final Integer    COLLAPSE = new Integer(0);

	/** Identifies the <code>Close</code> action. */
	static final Integer    CLOSE = new Integer(1);

	/** Identifies the <code>Sort</code> action. */
	static final Integer    SORT = new Integer(2);

	/** Identifies the <code>Sort by Date</code> action. */
	static final Integer    SORT_DATE = new Integer(3);
    
    /** Identifies the <code>Partial Name</code> action.*/
    static final Integer    PARTIAL_NAME = new Integer(4);
    
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
    	ArrayList<TreePath> pathsToRemove = new ArrayList<TreePath>();
    	TreeBrowserDisplay no;
    	Object o;
    	ArrayList<TreeBrowserDisplay> l = new ArrayList<TreeBrowserDisplay>();
    	l.add(node);
    	
    	/*
    	Object uo;
    	Class nodeClass = node.getUserObject().getClass();
    	for (int i = 1; i < n; i++) {
    		o = paths[i].getLastPathComponent();
    		if (o instanceof MetadataDisplay) {
    			no = (MetadataDisplay) o;
    			uo = no.getUserObject();
    			if (uo instanceof ExperimenterData) {
    				pathsToRemove.add(paths[i]);
    			} else {
    				if (uo.getClass().equals(nodeClass)) {
    					l.add(no);
    				} else {
    					pathsToRemove.add(paths[i]);
    				}
    			}
    		}
    	}
		*/

    	if (l.size() != n) {
    		UserNotifier un = 
    			MetadataViewerAgent.getRegistry().getUserNotifier();
    		un.notifyInfo("Node selection", "You can only select " +
    				"node of the same type e.g. images.");
    		//view.removeTreePaths(pathsToRemove);
    		//model.setSelectedDisplay(null);
    		//return;
    	}
    	if (l.size() == 0) return;
    	model.setSelectedNodes(l);
    }

    /**
     * Reacts to tree expansion events.
     * 
     * @param node   	The selected node.
     * @param expanded  Pass <code>true</code> if the node is expanded,
     * 					<code>false</code> otherwise.
     */
	void onNodeNavigation(TreeBrowserDisplay node, boolean expanded)
	{
		if (!expanded) {
    		model.cancel(node);
    		return;
    	}
		model.setSelectedNode(node);
		if (node.isChildrenLoaded()) return;
		view.addDefaultNode(node, BrowserUI.LOADING_MSG);
		model.loadMetadata(node);
	}
    
}
