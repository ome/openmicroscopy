/*
 * ome.ij.dm.browser.BrowserControl 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package ome.ij.dm.browser;



//Java imports
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Action;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreePath;


//Third-party libraries

//Application-internal dependencies
import ome.ij.dm.actions.CollapseAction;
import ome.ij.dm.actions.ShowNameAction;
import ome.ij.dm.actions.SortAction;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.ProjectData;

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
 * @since 3.0-Beta4
 */
public class BrowserControl    
	implements ChangeListener
{

    /** Identifies the <code>Collapse</code> action. */
	static final Integer    COLLAPSE = Integer.valueOf(0);

	/** Identifies the <code>Sort</code> action. */
	static final Integer    SORT = Integer.valueOf(1);

	/** Identifies the <code>Sort by Date</code> action. */
	static final Integer    SORT_DATE = Integer.valueOf(2);
    
    /** Identifies the <code>Partial Name</code> action.*/
    static final Integer    PARTIAL_NAME = Integer.valueOf(3);
   
    /** 
     * Reference to the {@link Browser} component, which, in this context,
     * is regarded as the Model.
     */
    private Browser     			model;
    
    /** Reference to the View. */
    private BrowserUI   			view;
    
    /** Maps actions ids onto actual <code>Action</code> object. */
    private Map<Integer, Action>	actionsMap;
    
    /** Helper method to create all the UI actions. */
    private void createActions()
    {
        actionsMap.put(COLLAPSE, new CollapseAction(model));
        actionsMap.put(SORT, new SortAction(model, SortAction.SORT_BY_NAME));
        actionsMap.put(SORT_DATE, new SortAction(model, 
        				SortAction.SORT_BY_DATE));
        actionsMap.put(PARTIAL_NAME, new ShowNameAction(model));
    }
    
    /**
     * Creates a new instance.
     * The {@link #initialize(BrowserUI) initialize} method 
     * should be called straight after to link this Controller to the other 
     * MVC components.
     * 
     * @param model  Reference to the {@link Browser} component, which, in 
     *               this context, is regarded as the Model.
     *               Mustn't be <code>null</code>.
     */
    BrowserControl(Browser model)
    {
        if (model == null) throw new NullPointerException("No model.");
        this.model = model;
        actionsMap = new HashMap<Integer, Action>();
        createActions();
    }
    
    /**
     * Links this Controller to its Model and its View.
     * 
     * @param view   Reference to the View. Mustn't be <code>null</code>.
     */
    void initialize(BrowserUI view)
    {
        if (view == null) throw new NullPointerException("No view.");
        this.view = view;
        //model.addChangeListener(this);
    }
    
    /**
     * Returns the node hosting the experimenter passing a child node.
     * 
     * @param node The child node.
     * @return See above.
     */
    TreeImageDisplay getDataOwner(TreeImageDisplay node)
    {
    	if (node == null) return null;
    	TreeImageDisplay parent = node.getParentDisplay();
    	Object ho;
    	if (parent == null) {
    		ho = node.getUserObject();
    		if (ho instanceof ExperimenterData)
    			return node;
    		return null;
    	}
    	ho = parent.getUserObject();
    	if (ho instanceof ExperimenterData) return parent;
    	return getDataOwner(parent);
    }

    /**
     * Reacts to tree expansion events.
     * 
     * @param display   The selected node.
     * @param expanded  Pass <code>true</code> if the node is expanded,
     * 					<code>false</code> otherwise.
     */
    void onNodeNavigation(TreeImageDisplay display, boolean expanded)
    {
    	if (!expanded) {
    		model.cancel();
    		return;
    	}
        int state = model.getState();
        if ((state == Browser.LOADING_DATA) ||
             (state == Browser.LOADING_LEAVES))
             return;
        
        Object ho = display.getUserObject();
        model.setSelectedDisplay(display); 
       
        if (display.isChildrenLoaded()) {
        	List l = display.getChildrenDisplay();
			//if (display.getChildCount() != l.size()) {
	
        		//view.setLeavesViews(l, (TreeImageSet) display);
        	//} else {
        		if (view.isFirstChildMessage(display)) {
        			view.setLeavesViews(l, (TreeImageSet) display);
        		}
        	//}
        	return;
        }
        if (ho instanceof ProjectData) {
        	if (display.numberItems == 0) return;
        }
        
        view.loadAction(display);
       
        if (ho instanceof DatasetData) {
        	model.loadExperimenterData(getDataOwner(display), display);
        } else if (ho instanceof ExperimenterData) {
        	model.loadExperimenterData(display, null);
        }
    }
    
    /** 
     * Brings up the popup menu. 
     * 
     * @param index The index of the menu.
     */
    //void showPopupMenu(int index) { model.showPopupMenu(index); }
    
    /** 
     * Reacts to click events in the tree.
     * 
     *  @param added The collection of added paths.
     */
    void onClick(List<TreePath> added)
    {
    	JTree tree = view.getTreeDisplay();
        TreePath[] paths = tree.getSelectionPaths();
        if (paths == null) return;
        TreeImageDisplay node;
        if (paths.length == 1) {
        	Object p = paths[0].getLastPathComponent();
        	if (!(p instanceof TreeImageDisplay))
        		return;
        	node = (TreeImageDisplay) p;
        	model.setSelectedDisplay(node);
    		return;
        }
    }
    
    /**
     * Views the image.
     * 
     * @param node The node to view.
     */
	void viewImage(TreeImageDisplay node) { model.viewImage(node); }
	
    /**
     * Returns the action corresponding to the specified id.
     * 
     * @param id One of the flags defined by this class.
     * @return The specified action.
     */
    Action getAction(Integer id) { return actionsMap.get(id); }
	
    /**
     * Detects when the {@link Browser} is ready and then registers for
     * property change notification.
     * @see ChangeListener#stateChanged(ChangeEvent)
     */
    public void stateChanged(ChangeEvent e)
    {
    	/*
    	int state = model.getState();
    	switch (state) {
			case Browser.BROWSING_DATA:
				
				break;
	
			default:
				break;
		}
		view.onStateChanged(state == Browser.READY);
		*/
    }

}
