/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.treeviewer.util;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.tree.DefaultTreeModel;

import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.clsf.TreeCheck;
import org.openmicroscopy.shoola.util.ui.clsf.TreeCheckNode;
import omero.gateway.model.DataObject;



/** 
 * A modal dialog displaying the existing objects that can be added 
 * to the currently selected node.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME2.2
 */
public class AddExistingObjectsDialog
    extends JDialog
{
    
    /** 
     * Bound property indicating to add the selected items to 
     * the currently selected node.
     */
    public static final String  EXISTING_ADD_PROPERTY = "existingAdd";
    
    /** Bound property indicating that the window has to be closed. */
    public static final String  CLOSE_PROPERTY = "close";
    
    
    /** The default title. */
    private static final String TITLE = "Add existing items";
    
    /** 
     * The size of the invisible components used to separate buttons
     * horizontally.
     */
    private static final Dimension  H_SPACER_SIZE = new Dimension(5, 10);
    
    /** Text displayed in the title panel. */
    private static final String     TEXT = "Select items";
    
    /** Text displayed in the title panel. */
    private static final String     NOTE = "Select the existing items to add " +
            "to the current node";
    
    /** Message displayed when no node to be added. */
    private static final String     EMPTY_NOTE = "No existing objects.";
  
    /** Button to finish the operation. */
    private JButton             finishButton;
    
    /** Button to cancel the object creation. */
    private JButton             cancelButton;
    
    /** Button to select all items. */
    private JButton             selectAllButton;
    
    /** Button to de-select all items. */
    private JButton             deselectAllButton;
    
    /** The tree hosting the hierarchical structure. */
    private TreeCheck           tree;
    
    /** Component used to sort the nodes. */
    private ViewerSorter        sorter;
    
    /** Sets the properties of the dialog. */
    private void setWinProperties()
    {
        setModal(true);
        setTitle(TITLE);
    }

    /** Fires an event to add selected nodes to the currently selected node. */
    private void finish()
    {
        Set nodes = tree.getSelectedNodes();
        if (nodes == null || nodes.size() == 0) {
            UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
            un.notifyInfo("Add existing items", "No item selected.");
            return;
        }
        Set<Object> paths = new HashSet<Object>();
        Iterator i = nodes.iterator();
        Object object;
        while (i.hasNext()) {
            object = ((TreeCheckNode) i.next()).getUserObject();
            if (object instanceof DataObject) paths.add(object);
        } 
        firePropertyChange(EXISTING_ADD_PROPERTY, null, paths);
        close();
    }
    
    /** Closes and disposes. */
    private void close()
    {
        setVisible(false);
        dispose();
    }

    /** Initializes the components composing the display. */
    private void initComponents()
    {
        sorter = new ViewerSorter();
        IconManager icons = IconManager.getInstance();
        tree = new TreeCheck("", icons.getIcon(IconManager.ROOT)); 
        selectAllButton = new JButton("Select All");
        selectAllButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            { 
                tree.selectAllNodes();
            }
        });
        deselectAllButton = new JButton("Deselect All");
        deselectAllButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            { 
                tree.deselectAllNodes();
           }
        });
        finishButton = new JButton("Finish");
        finishButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { finish(); }
        });
        cancelButton = new JButton("Close");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {  
                close();
            }
        });
    }

    /**
     * Adds the nodes to the specified parent.
     * 
     * @param parent    The parent node.
     * @param nodes     The list of nodes to add.
     */
    private void buildTreeNode(TreeCheckNode parent, List nodes)
    {
        DefaultTreeModel tm = (DefaultTreeModel) tree.getModel();
        Iterator i = nodes.iterator();
        TreeCheckNode display;
        Set children;
        while (i.hasNext()) {
            display = (TreeCheckNode) i.next();
            tm.insertNodeInto(display, parent, parent.getChildCount());
            children = display.getChildrenDisplay();
            if (children.size() != 0)
                buildTreeNode(display, sorter.sort(children));
        }  
    }
    
    /**
     * Builds the tool bar hosting the {@link #cancelButton} and
     * {@link #finishButton}.
     * 
     * @return See above;
     */
    private JToolBar buildRightToolBar()
    {
        JToolBar bar = new JToolBar();
        bar.setRollover(true);
        bar.setBorder(null);
        bar.setFloatable(false);
        bar.add(finishButton);
        bar.add(Box.createRigidArea(H_SPACER_SIZE));
        bar.add(cancelButton);
        return bar;
    }
    
    /**
     * Builds the tool bar hosting the {@link #cancelButton} and
     * {@link #finishButton}.
     * 
     * @return See above;
     */
    private JToolBar buildLeftToolBar()
    {
        JToolBar bar = new JToolBar();
        bar.setRollover(true);
        bar.setBorder(null);
        bar.setFloatable(false);
        bar.add(selectAllButton);
        bar.add(Box.createRigidArea(H_SPACER_SIZE));
        bar.add(deselectAllButton);
        return bar;
    }
    
    /**
     * Builds and lays out the panel hosting the tool bars.
     * 
     * @return See above.
     */
    private JPanel buildToolBars()
    {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(UIUtilities.buildComponentPanel(buildLeftToolBar()));
        p.add(UIUtilities.buildComponentPanelRight(buildRightToolBar()));
        return p;
    }
    
    /** 
     * Returns the main component displaying the available nodes
     * 
     * @param nodes The existing nodes.
     * @return See above.
     */
    private JComponent getExistingnodes(Set nodes)
    {
        if (nodes.size() == 0) {
            finishButton.setEnabled(false);
            JPanel p = new JPanel();
            p.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 10));
            p.add(UIUtilities.setTextFont(EMPTY_NOTE), BorderLayout.CENTER);
            return p;
        }
        //populates the tree
        DefaultTreeModel dtm = (DefaultTreeModel) tree.getModel();
        TreeCheckNode root = (TreeCheckNode) dtm.getRoot();
        Iterator i = nodes.iterator();
        while (i.hasNext())
            root.addChildDisplay((TreeCheckNode) i.next()) ;
        buildTreeNode(root, sorter.sort(nodes));
        dtm.reload();
        return new JScrollPane(tree);
    }
    
    /** Builds and lays out the UI. 
     * 
     * @param nodes The existing nodes.
     */
    private void buildGUI(Set nodes)
    {
        IconManager icons = IconManager.getInstance();
        TitlePanel tp = new TitlePanel(TEXT, NOTE, 
                    icons.getIcon(IconManager.CATEGORY_48));
        Container c = getContentPane();
        c.setLayout(new BorderLayout(0, 0));
        c.add(tp, BorderLayout.NORTH);
        c.add(getExistingnodes(nodes), BorderLayout.CENTER);
        c.add(buildToolBars(), BorderLayout.SOUTH);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param owner The owner of this dialog.
     * @param nodes The nodes to display.
     */
    public AddExistingObjectsDialog(JFrame owner, Set nodes)
    {
        super(owner);
        if (nodes == null) 
            throw new IllegalArgumentException("No nodes to display.");
        setWinProperties();
        initComponents();
        buildGUI(nodes);
        pack();
    }
    
}
