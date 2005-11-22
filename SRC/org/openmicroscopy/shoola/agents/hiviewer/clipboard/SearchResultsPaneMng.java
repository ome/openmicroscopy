/*
 * org.openmicroscopy.shoola.agents.hiviewer.clipboard.SearchResultsPaneMng
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

package org.openmicroscopy.shoola.agents.hiviewer.clipboard;





//Java imports
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.env.ui.ViewerSorter;

/** 
 * The {@link SearchResultsPane}'s controller.
 * 
 * @author  Barry Anderson &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:banderson@computing.dundee.ac.uk">
 *              banderson@comnputing.dundee.ac.uk
 *              </a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class SearchResultsPaneMng
{
    
    /** The default root's text. */
    private static final String     ROOT = "Results";
    
    /** Reference to the view, this class controls. */
    private SearchResultsPane       view;
    
    /** The root node. */
    private DefaultMutableTreeNode  root;
    
    /** The map where the tree nodes are stored. */
    private Map                     identityMap;
    
    /** 
     * Attaches a {@link MouseAdapter} to the tree in the view to get notified 
     * of mouse events on the tree.
     */
    private void initListeners()
    {
        view.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { onClick(e); }
            public void mouseReleased(MouseEvent e) { onClick(e); }
        });
    }
    
    /** Pops up the menu. */
    private void onClick(MouseEvent me)
    {
        int row = view.getRowForLocation(me.getX(), me.getY());
        if (row != -1) {
            view.setSelectionRow(row);
            view.setSelectedNode();
            if (me.isPopupTrigger() && view.getDataObject() != null)
                view.showMenu(me.getX(), me.getY());
        }
    }
    
    /** 
     * Builds a node corresponding to the specified {@link ImageDisplay}.
     * 
     * @param node  The specified {@link ImageDisplay}.
     */
    private void buildTreeNode(ImageDisplay node)
    {
        DefaultMutableTreeNode dtn;
        DefaultTreeModel tm = (DefaultTreeModel) view.getModel();
        dtn = (DefaultMutableTreeNode) identityMap.get(node);
        if (dtn == null) { // create a tree node
            //dtn = new DefaultMutableTreeNode(node.getHierarchyObject());
            dtn = new DefaultMutableTreeNode(node);
            identityMap.put(node, dtn);
        }
        ImageDisplay parent = node.getParentDisplay();
        if (parent.getParentDisplay() == null) { //i.e. the parent is root
            if (root == null) getTreeModel();
            if (!(hasBeenAdded(tm, dtn, root)))
                tm.insertNodeInto(dtn, root, root.getChildCount());
        } else {
            DefaultMutableTreeNode 
                pdtn = (DefaultMutableTreeNode) identityMap.get(parent);
            if (pdtn == null) { // create a tree node
                //pdtn = new DefaultMutableTreeNode(parent.getHierarchyObject());
                pdtn = new DefaultMutableTreeNode(parent);
                identityMap.put(parent, pdtn);
            }
            if (!(hasBeenAdded(tm, dtn, pdtn)))
                tm.insertNodeInto(dtn, pdtn, pdtn.getChildCount());
            buildTreeNode(parent);
        }   
    }
    
    /** 
     * Checkes if the specified child as already been added to the 
     * specified parent. 
     * 
     * @param tm The tree model
     * @param child The specified {@link DefaultMutableTreeNode child}.
     * @param parent The specified {@link DefaultMutableTreeNode parent}.
     * 
     * */
    private boolean hasBeenAdded(DefaultTreeModel tm, 
                    DefaultMutableTreeNode child, DefaultMutableTreeNode parent)
    {
        int n = tm.getChildCount(parent);
        for (int i = 0; i < n; i++)
            if (tm.getChild(parent, i) == child) return true;
        return false;
    }
    
    
    /**
     * Creates a new instance. 
     * 
     * @param view The <code>SearchResultsPane</code> view.
     */
    SearchResultsPaneMng(SearchResultsPane view)
    {
        this.view = view;
        identityMap = new HashMap();
        initListeners();
    }
    
    /** Builds the tree model. */
    void getTreeModel()
    {
        root = new DefaultMutableTreeNode(ROOT);
        view.setTreeModel(root);
    }
    
    /** Builds the results tree. */
    void buildTree(Set nodes)
    {
        if (nodes.size() == 0) {
            getTreeModel();
            DefaultMutableTreeNode childNode = 
                new DefaultMutableTreeNode("Empty");
            DefaultTreeModel tm= (DefaultTreeModel) view.getModel();
            tm.insertNodeInto(childNode, root, root.getChildCount());
        } else {
            ViewerSorter sorter = new ViewerSorter(nodes);
            List list = sorter.sort();
            Iterator i = list.iterator();
            while (i.hasNext())
                buildTreeNode((ImageDisplay) i.next());
        }
    }
    
}
