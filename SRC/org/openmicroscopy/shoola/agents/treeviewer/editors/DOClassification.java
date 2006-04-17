/*
 * org.openmicroscopy.shoola.agents.treeviewer.editors.DOClassification
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

package org.openmicroscopy.shoola.agents.treeviewer.editors;


//Java imports
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageSet;
import org.openmicroscopy.shoola.agents.treeviewer.util.TreeCellRenderer;
import org.openmicroscopy.shoola.env.ui.ViewerSorter;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.DataObject;

/** 
 * The component hosting the classification. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class DOClassification
    extends JPanel
{

    /** The text displayed in the note panel. */
    private static final String     PANEL_NOTE = "The image is currently " +
            "classified under the following categories.";
    
    /** The text displayed in the note panel. */
    private static final String     PANEL_SUBNOTE = "Double click on the " +
            "name to browse the group or the category.";
    
    /** The root object. */
    private static final String     ROOT = "Classification";
    
    /** Reference to the Model. */
    private EditorModel         model;
    
    /** Reference to the Control. */
    private EditorControl       controller;
    
    /** The tree hosting the classification. */
    private JTree               treeDisplay;
    
    /** Button to reload the classification. */
    private JButton             refreshButton;
    
    /** 
     * A {@link ViewerSorter sorter} to order nodes in ascending 
     * alphabetical order.
     */
    private ViewerSorter        sorter;
    
    /**
     * Adds the nodes to the specified parent.
     * 
     * @param parent The parent node.
     * @param nodes The list of nodes to add.
     */
    private void buildTreeNode(DefaultMutableTreeNode parent, List nodes)
    {
        DefaultTreeModel tm = (DefaultTreeModel) treeDisplay.getModel();
        Iterator i = nodes.iterator();
        TreeImageDisplay display;
        Set children;
        while (i.hasNext()) {
            display = (TreeImageDisplay) i.next();
            tm.insertNodeInto(display, parent, parent.getChildCount());
            if (display instanceof TreeImageSet) {
                children = display.getChildrenDisplay();
                if (children.size() != 0)
                    buildTreeNode(display, sorter.sort(children));
            }
        } 
    }
    
    /** 
     * Handles the mouse click event. 
     * Browses the selected <code>CategoryGroup</code> or <code>Category</code>.
     * 
     * @param me The mouse event.
     */
    private void onClick(MouseEvent me)
    {
        Point p = me.getPoint();
        int row = treeDisplay.getRowForLocation(p.x, p.y);
        if (row != -1) {
            treeDisplay.setSelectionRow(row);
            if (me.getClickCount() != 2) return;
            Object node = treeDisplay.getLastSelectedPathComponent();
            if (!(node instanceof TreeImageDisplay)) return;
            Object userObject = ((TreeImageDisplay) node).getUserObject();
            if (userObject instanceof DataObject) 
                model.browse((DataObject) userObject);
        }
    }
    
    /** Initializes the UI components. */
    private void initComponents()
    {
        IconManager im = IconManager.getInstance();
        refreshButton = new JButton(im.getIcon(IconManager.REFRESH));
        refreshButton.setEnabled(false);
        refreshButton.setToolTipText(
                        UIUtilities.formatToolTipText("Reload data.")); 
        refreshButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {  
                refreshButton.setEnabled(false);
                controller.reloadClassifications();
            }
        });
        treeDisplay = new JTree();
        treeDisplay.setCellRenderer(new TreeCellRenderer(false));
        treeDisplay.setShowsRootHandles(true);
        treeDisplay.putClientProperty("JTree.lineStyle", "Angled");
        treeDisplay.getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);
        TreeImageSet root = new TreeImageSet(ROOT);
        treeDisplay.setModel(new DefaultTreeModel(root));
        //Add Listeners
        treeDisplay.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { onClick(e); }
            public void mouseReleased(MouseEvent e) { onClick(e); }
        });
    }
    
    /** 
     * Builds the tool bar hosting the buttons. 
     * 
     * @return See above.
     */
    private JToolBar createToolBar()
    {
        JToolBar bar = new JToolBar();
        bar.setBorder(null);
        bar.setRollover(true);
        bar.setFloatable(false);
        bar.add(refreshButton);
        return bar;
    }
    
    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(new JLabel(PANEL_NOTE));
        p.add(new JLabel(PANEL_SUBNOTE));
        p.add(createToolBar());
        add(UIUtilities.buildComponentPanel(p), BorderLayout.NORTH);
        add(new JScrollPane(treeDisplay), BorderLayout.CENTER);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model         Reference to the Model. 
     *                      Mustn't be <code>null</code>.
     * @param controller    Reference to the Control. 
     *                      Mustn't be <code>null</code>.
     */
    DOClassification(EditorModel model, EditorControl controller)
    {
        if (model == null)  throw new IllegalArgumentException("No Model.");
        if (controller == null) 
            throw new IllegalArgumentException("No Control.");
        this.model = model;
        this.controller = controller;
        sorter = new ViewerSorter();
        initComponents();
        buildGUI();
    }
    
    /** Adds the classifications nodes to the tree. */
    void showClassifications()
    {
        if (!model.isClassificationLoaded()) return;
        refreshButton.setEnabled(true);
        Set nodes = model.getClassifications();
        DefaultTreeModel dtm = (DefaultTreeModel) treeDisplay.getModel();
        TreeImageDisplay root = (TreeImageDisplay) dtm.getRoot();
        root.removeAllChildren();
        root.removeAllChildrenDisplay();
        if (nodes.size() != 0) {
            Iterator i = nodes.iterator();
            while (i.hasNext())
                root.addChildDisplay((TreeImageDisplay) i.next()) ;
            buildTreeNode(root, sorter.sort(nodes));
        }
        dtm.reload();
    }
    
}
