/*
 * org.openmicroscopy.xdoc.navig.NavMenuUI
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

package org.openmicroscopy.xdoc.navig;


//Java imports
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

//Third-party libraries

//Application-internal dependencies

/** 
 * UI component to display the navigation tree.
 * This class does the layout and allows to set various display options.
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
public class NavMenuUI
    extends JScrollPane
{

    /** The widget to navigate through the table of contents. */
    JTree   navTree;
    
    
    /**
     * Creates a new instance.
     * 
     * @param toc The tree model representing the table of contents.
     *              Mustn't be <code>null</code>.
     */
    public NavMenuUI(DefaultMutableTreeNode toc)
    {
        super();
        if (toc == null) throw new NullPointerException("No toc.");
        navTree = new JTree(toc);
        buildGUI();
    }
    
    /** Builds and lay out the GUI. */
    private void buildGUI()
    {
        navTree.putClientProperty("JTree.lineStyle", "None");
        navTree.setCellRenderer(new NavMenuCellRenderer());
        navTree.setShowsRootHandles(true);
        navTree.getSelectionModel().setSelectionMode(
                                    TreeSelectionModel.SINGLE_TREE_SELECTION);
        setViewportView(navTree);
    }
    /**
     * Adds the specified listener to the tree.
     * 
     * @param tsl The listener to add.
     */
    void addTreeSelectionListener(TreeSelectionListener tsl) 
    {
        navTree.addTreeSelectionListener(tsl);
    }
    
    /**
     * Forwards the call to the tree widget. 
     * 
     * @return The last path component in the first node of the current 
     *          selection in the tree widget.  Will be <code>null</code>
     *          if nothing is selected. 
     */
    DefaultMutableTreeNode getLastSelectedPathComponent()
    {
        return (DefaultMutableTreeNode) navTree.getLastSelectedPathComponent();
    }

}
