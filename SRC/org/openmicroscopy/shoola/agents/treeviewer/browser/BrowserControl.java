/*
 * org.openmicroscopy.shoola.agents.treemng.browser.BrowserControl
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.treeviewer.browser;


//Java imports
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreePath;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.actions.CloseAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.CollapseAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.FilterMenuAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.NavigationAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.SortAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.SortByDateAction;
import org.openmicroscopy.shoola.agents.treeviewer.util.FilterWindow;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import pojos.CategoryData;
import pojos.DatasetData;

/** 
 * The Browser's Controller.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class BrowserControl
    implements ChangeListener, PropertyChangeListener
{

    /** Identifies the <code>Collapse</code> action in the Actions menu. */
    static final Integer     COLLAPSE = new Integer(0);
    
    /** Identifies the <code>Close</code> action in the Actions menu. */
    static final Integer     CLOSE = new Integer(1);
    
    /** Identifies the <code>Sort</code> action in the Actions menu. */
    static final Integer     SORT = new Integer(2);
    
    /** Identifies the <code>Sort by Date</code> action in the Actions menu. */
    static final Integer     SORT_DATE = new Integer(3);
    
    /** Identifies the <code>Filter Menu</code> action in the Actions menu. */
    static final Integer     FILTER_MENU = new Integer(4);
     
    /** Identifies the <code>Forward Nav</code> action in the Actions menu. */
    static final Integer     FORWARD_NAV = new Integer(5);
    
    /** Identifies the <code>Backward Nav</code> action in the Actions menu. */
    static final Integer     BACKWARD_NAV = new Integer(6);
    
    /** 
     * Reference to the {@link Browser} component, which, in this context,
     * is regarded as the Model.
     */
    private Browser     model;
    
    /** Reference to the View. */
    private BrowserUI   view;
    
    /** Maps actions ids onto actual <code>Action</code> object. */
    private Map			actionsMap;
    
    /** Helper method to create all the UI actions. */
    private void createActions()
    {
        actionsMap.put(COLLAPSE, new CollapseAction(model));
        actionsMap.put(CLOSE, new CloseAction(model));
        actionsMap.put(SORT, new SortAction(model));
        actionsMap.put(SORT_DATE, new SortByDateAction(model));
        actionsMap.put(FILTER_MENU, new FilterMenuAction(model));
        actionsMap.put(FORWARD_NAV, new NavigationAction(model, true));
        actionsMap.put(BACKWARD_NAV, new NavigationAction(model, false));
    }
    
    /**
     * Loads the children of nodes contained in the specified in collection.
     * 
     * @param nodes The collection of nodes.
     */
    private void filterNodes(Set nodes)
    {
        if (nodes == null || nodes.size() == 0) return;
        if (model.getBrowserType() != Browser.IMAGES_EXPLORER) return;
        //We should be in the ready state.
        TreeImageDisplay root = view.getTreeRoot();
        root.removeAllChildrenDisplay() ;
        view.loadAction(root);
        model.loadFilteredImageData(nodes);
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
        actionsMap = new HashMap();
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
        model.addPropertyChangeListener(this);
        model.addChangeListener(this);
    }

    /**
     * Reacts to tree expansion events.
     * 
     * @param display The selected node.
     * @param expanded 	<code>true</code> if the node is expanded,
     * 					<code>false</code> otherwise.
     */
    void onNodeNavigation(TreeImageDisplay display, boolean expanded)
    {
        int state = model.getState();
        if ((state == Browser.LOADING_DATA) ||
             (state == Browser.LOADING_LEAVES) || 
             (state == Browser.COUNTING_ITEMS)) return;
        Object ho = display.getUserObject();
        model.setSelectedDisplay(display); 
        if (!expanded) return;
        if ((ho instanceof DatasetData) || (ho instanceof CategoryData)) {
            if (display.getChildrenDisplay().size() == 0) {
                view.loadAction(display);
                model.loadLeaves();
            }    
        } else {
            TreeImageDisplay root = view.getTreeRoot();
            if (root.equals(display) && root.getChildrenDisplay().size() == 0) {
                if (model.getBrowserType() == Browser.IMAGES_EXPLORER) 
                    model.loadFilteredImagesForHierarchy();
                else {
                    view.loadAction(root);
                    model.loadData();
                }
            }
        }
    }
    
    /** Brings up the popup menu. */
    void showPopupMenu() { model.showPopupMenu(); }
    
    /**
     * Reacts to click events in the tree.        
     */
    void onClick()
    {
        Object pathComponent;
        //TreePath[] paths = view.getTreeDisplay().getSelectionPaths();
        TreePath[] paths = view.getSelectedTree().getSelectionPaths();
        if (paths == null) return;
        int n = paths.length;
        if (n == 0) return;
        pathComponent = paths[0].getLastPathComponent();
        //Check if alls node are of the same type.
        if (!(pathComponent instanceof TreeImageDisplay)) return;
        TreeImageDisplay node = (TreeImageDisplay) pathComponent;
        TreeImageDisplay no;
        Object o;
        ArrayList l = new ArrayList();
        l.add(node);
        for (int i = 1; i < n; i++) {
            o = paths[i].getLastPathComponent();
            if (o instanceof TreeImageDisplay) {
                no = (TreeImageDisplay) o;
                if (no.getUserObject().getClass().equals(
                        node.getUserObject().getClass())) {
                    l.add(no);
                }
            }
        }
        if (l.size() != n) {
            UserNotifier un = 
                TreeViewerAgent.getRegistry().getUserNotifier();
            un.notifyInfo("Node selection", "You can only select " +
                    "node of the same type e.g. images.");
            return;
        }
        //Pass TreeImageDisplay array
        TreeImageDisplay[] nodes = (TreeImageDisplay[]) l.toArray(
                                    new TreeImageDisplay[l.size()]);
        model.setSelectedDisplays(nodes);
    }
    
    /**
     * Returns the action corresponding to the specified id.
     * 
     * @param id One of the flags defined by this class.
     * @return The specified action.
     */
    Action getAction(Integer id) { return (Action) actionsMap.get(id); }
    
    /** Forwards event to the {@link Browser} to load the leaves. */
    void loadLeaves() { model.loadLeaves(); }
    
    /** Forwards event to the {@link Browser} to load the hiearchy data. */
    void loadData() { model.loadData(); }
    
    /**
     * Detects when the {@link Browser} is ready and then registers for
     * property change notification.
     * @see ChangeListener#stateChanged(ChangeEvent)
     */
    public void stateChanged(ChangeEvent e)
    {
        view.onStateChanged(model.getState() == Browser.READY);
    }

    /**
     * Reacts to {@link Browser} property changes.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent pce)
    {
        String name = pce.getPropertyName();
        if (name.equals(TreeViewer.FILTER_NODES_PROPERTY)) {
            Map map = (Map) pce.getNewValue();
            if (map.get(model) != null) 
                filterNodes((Set) map.get(model));
        } else if (name.equals(FilterMenu.FILTER_SELECTED_PROPERTY))
            model.setFilterType(((Integer) pce.getNewValue()).intValue());  
        else if (name.equals(FilterWindow.CLOSE_PROPERTY))
            model.collapse(model.getLastSelectedDisplay());
    }
    
}
