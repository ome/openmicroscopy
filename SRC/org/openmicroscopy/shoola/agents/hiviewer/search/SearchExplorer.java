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
import java.awt.Frame;
import java.util.Set;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.twindow.TinyWindow;

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
    extends TinyWindow
{
    
    private static final Dimension DEFAULT_DIMENSION = new Dimension(150, 150);
    
    private SearchExplorerMng   manager;
    
    /** The tree used to display the results. */
    JTree                       tree;
    
    public SearchExplorer(Frame owner, String title, Set nodes)
    {
        super(owner, title);
        tree = new JTree();
        manager = new SearchExplorerMng(this);
        manager.buildTree(nodes);
        buildUI(); 
    }
    
    /** Build and Layout the GUI. */
    private void buildUI()
    {
        uiDelegate.setCanvas(buildTreeUI());
        setSize(DEFAULT_DIMENSION);
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
    
}
