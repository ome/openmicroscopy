/*
 * org.openmicroscopy.shoola.agents.hiviewer.clipboard.clsf.ClassificationPaneUI
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

package org.openmicroscopy.shoola.agents.hiviewer.clipboard.clsf;




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
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.IconManager;
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.clsf.TreeCheck;
import org.openmicroscopy.shoola.util.ui.clsf.TreeCheckNode;
import pojos.DataObject;

/** 
 * The component hosting the classifications. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class ClassificationPaneUI
    extends JPanel
{

    /** The text displayed in the note panel. */
    private static final String     PANEL_NOTE = "Classification for: ";
    
    /** The text displayed in the note panel. */
    private static final String     PANEL_SUBNOTE = "Double click on the " +
            "name to browse the group or the category.";
    
    /** The text displayed when the image has been annotated. */
    private static final String     NO_CLASSIFICATION_TEXT = "The selected " +
            "image has not been classified. ";
    
    /** Reference to the Model. */
    private ClassificationPane  model;
    
    /** 
     * A {@link ViewerSorter sorter} to order nodes in ascending 
     * alphabetical order.
     */
    private ViewerSorter        sorter;
    
    /** The tree hosting the classification. */
    private TreeCheck           treeDisplay;
    
    /** The label displaying the classification context. */
    private JLabel              titleLabel;
    
    /** The label displaying extra information. */
    private JLabel              noteLabel;
    
    /** The declassify button. */
    private JButton             declassifyButton;
    
    /**
     * Adds the nodes to the specified parent.
     * 
     * @param parent    The parent node.
     * @param nodes     The list of nodes to add.
     */
    private void buildTreeNode(TreeCheckNode parent, List nodes)
    {
        DefaultTreeModel tm = (DefaultTreeModel) treeDisplay.getModel();
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
            if (!(node instanceof TreeCheckNode)) return;
            Object userObject = ((TreeCheckNode) node).getUserObject();
            if (userObject instanceof DataObject) 
                model.browse((DataObject) userObject);
        }
    }
    
    /** Initializes the UI components. */
    private void initComponents()
    {
        declassifyButton = new JButton("Declassify");
        declassifyButton.addActionListener(new ActionListener() {
        
            public void actionPerformed(ActionEvent e)
            { 
                model.declassify(treeDisplay.getSelectedNodes()); 
            }
        
        });
        declassifyButton.setVisible(false);
        titleLabel = new JLabel(NO_CLASSIFICATION_TEXT);
        noteLabel = new JLabel();

        
        //tree hosting the classification
        IconManager im = IconManager.getInstance();
        treeDisplay = new TreeCheck("", im.getIcon(IconManager.ROOT));
        treeDisplay.setRootVisible(false);
        treeDisplay.setShowsRootHandles(true);
        treeDisplay.putClientProperty("JTree.lineStyle", "Angled");
        treeDisplay.getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);
        //Add Listeners
        treeDisplay.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { onClick(e); }
            public void mouseReleased(MouseEvent e) { onClick(e); }
        });
    }
    
    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.add(titleLabel);
        titlePanel.add(noteLabel);
        p.add(UIUtilities.buildComponentPanel(titlePanel));
        //p.add(UIUtilities.buildComponentPanel(declassifyButton));
        add(UIUtilities.buildComponentPanel(p), BorderLayout.NORTH);
        add(new JScrollPane(treeDisplay), BorderLayout.CENTER);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>..
     */
    ClassificationPaneUI(ClassificationPane model)
    {
        if (model == null)  throw new IllegalArgumentException("No Model.");
        this.model = model;
        sorter = new ViewerSorter();
        initComponents();
        buildGUI();
    }
    
    /**
     * Reacts to a new selection in the browser.
     * 
     * @param title The context of the classification.
     */
    void onSelectedDisplay(String title)
    { 
        if (title == null) {
            declassifyButton.setVisible(false);
            titleLabel.setText(NO_CLASSIFICATION_TEXT);
            noteLabel.setText("");
        } else {
            declassifyButton.setVisible(true);
            titleLabel.setText(PANEL_NOTE+title);
            noteLabel.setText(PANEL_SUBNOTE);
        }
        DefaultTreeModel dtm = (DefaultTreeModel) treeDisplay.getModel();
        TreeCheckNode root = (TreeCheckNode) dtm.getRoot();
        root.removeAllChildren();
        root.removeAllChildrenDisplay();
        dtm.reload();
        repaint();
    }
    
    /** 
     * Adds the classifications nodes to the tree. 
     * 
     * @param nodes The nodes to add.
     */
    void showClassifications(Set nodes)
    {
        DefaultTreeModel dtm = (DefaultTreeModel) treeDisplay.getModel();
        TreeCheckNode root = (TreeCheckNode) dtm.getRoot();
        Iterator i = nodes.iterator();
        while (i.hasNext())
            root.addChildDisplay((TreeCheckNode) i.next()) ;
        buildTreeNode(root, sorter.sort(nodes));
        dtm.reload();
        dtm.reload();
    }
    
}
