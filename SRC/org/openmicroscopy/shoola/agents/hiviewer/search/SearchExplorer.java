/*
 * org.openmicroscopy.shoola.agents.hiviewer.search.SearchExplorer
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

package org.openmicroscopy.shoola.agents.hiviewer.search;




//Java imports
import java.awt.Dimension;
import java.awt.Point;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.util.TreeCellRenderer;
import org.openmicroscopy.shoola.env.data.model.DataObject;
import org.openmicroscopy.shoola.env.data.model.DatasetSummary;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.env.data.model.ProjectSummary;
import org.openmicroscopy.shoola.env.ui.tdialog.TinyDialog;
import org.openmicroscopy.shoola.env.ui.tdialog.TinyDialogUI;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class SearchExplorer
    extends TinyDialog
{
    
    private static final int    DEFAULT_WIDTH = 150;
    
    /** The point at which the last popup event occurred. */
    private Point               popupPoint;
    
    /** Reference to this component manager. */
    private SearchExplorerMng   manager;
    
    /** The tree used to display the results. */
    JTree                       tree;
    
    JFrame                      owner;
    
    public SearchExplorer(JFrame owner, String title, Set nodes)
    {
        super(owner, title);
        this.owner = owner;
        tree = new JTree();
        manager = new SearchExplorerMng(this);
        manager.buildTree(nodes);
        buildUI(); 
    }
    
    /** Build and Layout the GUI. */
    private void buildUI()
    {
        uiDelegate.setCanvas(buildTreeUI());
        Dimension d = tree.getPreferredScrollableViewportSize();
        Dimension dC = getContentPane().getPreferredSize();
        int w = dC.width, h = d.height;
        if (w > TinyDialogUI.MAX_WIDTH) w = TinyDialogUI.MAX_WIDTH;
        if (w < DEFAULT_WIDTH) w = DEFAULT_WIDTH;
        if (h > TinyDialogUI.MAX_HEIGHT) h = TinyDialogUI.MAX_HEIGHT;
        setSize(w, h);
    }
    
    /** Display the Tree in a JScrollPane. */
    private JScrollPane buildTreeUI()
    {
        JScrollPane pane = new JScrollPane();
        tree.putClientProperty("JTree.lineStyle", "Angled");
        tree.setCellRenderer(new TreeCellRenderer());
        tree.getSelectionModel().setSelectionMode(
                                    TreeSelectionModel.SINGLE_TREE_SELECTION);
        pane.setViewportView(tree);
        return pane;
    }
    
    /** Set the treeModel when the root node is created. */
    void setTreeModel(DefaultMutableTreeNode root)
    {
        DefaultTreeModel dtm = new DefaultTreeModel(root);
        tree.setModel(dtm);
        tree.setShowsRootHandles(true);
        tree.expandPath(new TreePath(root.getPath()));
    }
    
    /**
     * Returns the selected data object.
     * @return See above.
     */
    DataObject getDataObject()
    {
        DataObject target = null;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                                            tree.getLastSelectedPathComponent();
        if (node != null) {
            Object  usrObj = node.getUserObject();
            if (usrObj instanceof ProjectSummary || 
                usrObj instanceof DatasetSummary ||
                usrObj instanceof ImageSummary)
                target = (DataObject) usrObj;
        }
        return target;
    }
    
    /**
     * The point at which the last popup event occurred.
     * 
     * @return See above.
     */
    Point getPopupPoint() { return popupPoint; }
    
    /** Brings up the menu for the current window. */
    void showMenu(Point p)
    {
        popupPoint = p;
        DataObject target = getDataObject();
        String txt = SearchExplorerPopupMenu.BROWSE;
        boolean b = false;
        if (target instanceof ImageSummary) {
            txt = SearchExplorerPopupMenu.VIEW;
            b = true;
        }
        SearchExplorerPopupMenu.setViewText(txt);
        SearchExplorerPopupMenu.setClassifyEnabled(b);
        SearchExplorerPopupMenu.showMenuFor(this);
    }
    
    /** Hides the popup menu. */
    void hideMenu() { SearchExplorerPopupMenu.hideMenu(); }
    
    /** Overrides the {@link #closeWindow()} method. */
    public void closeWindow()
    {
        super.closeWindow();
        hideMenu();
    }
    
}
