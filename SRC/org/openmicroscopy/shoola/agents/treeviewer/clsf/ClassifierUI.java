/*
 * org.openmicroscopy.shoola.agents.treeviewer.clsf.ClassifierUI
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

package org.openmicroscopy.shoola.agents.treeviewer.clsf;


//Java imports
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.tree.DefaultTreeModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.env.ui.ViewerSorter;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.clsf.TreeCheck;
import org.openmicroscopy.shoola.util.ui.clsf.TreeCheckNode;
import pojos.CategoryData;
import pojos.DataObject;


/** 
 * The {@link Classifier}'s view.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class ClassifierUI
    extends JPanel
{
    
    /** Text displayed in the title panel. */
    private static final String     ADD_PANEL_TITLE = "Add To Category";
    
    /** Text displayed in the text panel. */
    private static final String     ADD_PANEL_TEXT = "Classify the following " +
                                                    "image: "; 
    
    /** Text displayed in the note panel. */
    private static final String     ADD_PANEL_NOTE = "The image can be " +
            "classified under the following categories. " +
          "Double click on the name to browse the group or the category.";
    
    /** Message displayed if the image is unclassified. */
    private static final String     ADD_UNCLASSIFIED_TEXT = "The image " +
            "cannot be classified because there is no category available. ";
    
    /** Text displayed in the title panel. */
    private static final String     REMOVE_PANEL_TITLE = "Remove From Category";
    
    /** Text displayed in the text panel. */
    private static final String     REMOVE_PANEL_TEXT = "Declassify the " +
                                                        "following image: ";
    
    /** Text displayed in the note panel. */
    private static final String     REMOVE_PANEL_NOTE = "The image is " +
            "currently classified under the following categories. "+
            "Double click on the name to browse the group or the category.";
    
    /** Message displayed if the image is unclassified. */
    private static final String     REMOVE_UNCLASSIFIED_TEXT = "The selected " +
                                    "image hasn't been classified.";
    
    
    /** Reference to the Model. */
    private ClassifierModel     model;
    
    /** Reference to the Control. */
    private ClassifierControl   controller;
    
    /** Button to finish the operation. */
    private JButton             finishButton;
    
    /** Button to cancel the object creation. */
    private JButton             cancelButton;
    
    /** The UI component hosting the title. */
    private TitlePanel          titlePanel;
    
    /** The panel hosting the classifications. */
    private JPanel              centerPanel;
    
    /** Component used to sort the nodes. */
    private ViewerSorter        sorter;
    
    /** The tree hosting the hierarchical structure. */
    private TreeCheck           tree;
    
    /** Classifies or declassifies the image depending on the mode. */
    private void finish()
    {
        Set nodes = tree.getSelectedNodes(); 
        if (nodes == null || nodes.size() == 0) { 
            UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
            un.notifyInfo("Classification", "No category selected."); 
            return; 
        }
        Set paths = new HashSet(nodes.size()); 
        Iterator i = nodes.iterator();
        Object object; 
        while (i.hasNext()) { 
            object = ((TreeCheckNode) i.next()).getUserObject(); 
            if (object instanceof CategoryData) paths.add(object); 
        } 
        int m = model.getMode();
        if (m == Classifier.CLASSIFY_MODE)
            model.fireClassificationSaving(paths);
        else if (m == Classifier.DECLASSIFY_MODE)
            model.fireDeclassificationSaving(paths);
    }
    
    /** Initializes the GUI components. */
    private void initComponents()
    {
        sorter = new ViewerSorter();
        IconManager im = IconManager.getInstance();
        titlePanel = new TitlePanel(getPanelTitle(), getPanelText(), 
                getPanelNote(), im.getIcon(IconManager.CATEGORY_BIG));
        tree = new TreeCheck("", im.getIcon(IconManager.ROOT)); 
        //Add Listeners
        tree.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { onClick(e); }
            public void mouseReleased(MouseEvent e) { onClick(e); }
        });
        finishButton = new JButton("Finish");
        finishButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { finish(); }
        });
        cancelButton = new JButton("Close");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {  
                controller.closeClassifier(true);
            }
        });
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
        int row = tree.getRowForLocation(p.x, p.y);
        if (row != -1) {
            tree.setSelectionRow(row);
            if (me.getClickCount() != 2) return;
            Object node = tree.getLastSelectedPathComponent();
            if (!(node instanceof TreeCheckNode)) return;
            Object object = ((TreeCheckNode) node).getUserObject();
            if (object instanceof DataObject)
                model.browse((DataObject) object);
        }
    }
    
    /**
     * Builds the tool bar hosting the {@link #cancelButton} and
     * {@link #finishButton}.
     * 
     * @return See above;
     */
    private JToolBar buildToolBar()
    {
        JToolBar bar = new JToolBar();
        bar.setRollover(true);
        bar.setFloatable(false);
        bar.add(finishButton);
        bar.add(cancelButton);
        return bar;
    }
    
    /** Builds and lays out the UI. */
    private void buildGUI()
    {
        setLayout(new BorderLayout(0, 0));
        add(titlePanel, BorderLayout.NORTH);
        centerPanel = new JPanel();
        add(centerPanel, BorderLayout.CENTER);
        JPanel p = UIUtilities.buildComponentPanelRight(buildToolBar());
        p.setBorder(BorderFactory.createEtchedBorder());
        add(p, BorderLayout.SOUTH);
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
     * Returns the component hosting the display.
     * 
     * @return See above.
     */
    private JComponent getClassificationComponent()
    {
        Set paths = model.getPaths();
        if (paths.size() == 0) {
            finishButton.setEnabled(false);
            JPanel p = new JPanel();
            p.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 10));
            p.add(UIUtilities.setTextFont(getUnclassifiedNote()), 
                   BorderLayout.CENTER);
            return p;
        }
        //populates the tree
        DefaultTreeModel dtm = (DefaultTreeModel) tree.getModel();
        TreeCheckNode root = (TreeCheckNode) dtm.getRoot();
        Iterator i = paths.iterator();
        while (i.hasNext())
            root.addChildDisplay((TreeCheckNode) i.next()) ;
        buildTreeNode(root, sorter.sort(paths));
        dtm.reload();
        return new JScrollPane(tree);
    }
    
    /** 
     * Returns the title displayed in the titlePanel.
     * 
     * @return See above.
     */
    private String getPanelTitle()
    {
        switch (model.getMode()) {
            case Classifier.CLASSIFY_MODE:
                return ADD_PANEL_TITLE;
            case Classifier.DECLASSIFY_MODE:
                return REMOVE_PANEL_TITLE;
        }
        return "";
    }
    
    /** 
     * Returns the text displayed in the titlePanel.
     * 
     * @return See above.
     */
    private String getPanelText()
    {
        switch (model.getMode()) {
            case Classifier.CLASSIFY_MODE:
                return ADD_PANEL_TEXT+" "+model.getDataObject().getName();
            case Classifier.DECLASSIFY_MODE:
                return REMOVE_PANEL_TEXT+" "+model.getDataObject().getName();
        }
        return "";
    }
    
    /**
     * Returns the note displayed in the titlePanel.
     * 
     * @return See above.
     */
    private String getPanelNote()
    {
        switch (model.getMode()) {
            case Classifier.CLASSIFY_MODE:
                return ADD_PANEL_NOTE;
            case Classifier.DECLASSIFY_MODE:
                return REMOVE_PANEL_NOTE;
        }
        return "";
    }
    
    /**
     * Returns the note displaying the unclassified message.
     * 
     * @return See above.
     */
    private String getUnclassifiedNote()
    {
        switch (model.getMode()) {
            case Classifier.CLASSIFY_MODE:
                return ADD_UNCLASSIFIED_TEXT;
            case Classifier.DECLASSIFY_MODE:
                return REMOVE_UNCLASSIFIED_TEXT;
        }
        return "";
    }
    
    ClassifierUI()
    {
        
    }
    
    /**
     * Links the View with its Model and Control.
     * 
     * @param controller    Reference to the Control.
     *                      Mustn't be <code>null</code>.
     * @param model         Reference to the Model.
     *                      Mustn't be <code>null</code>.
     */
    void initialize(ClassifierControl controller, ClassifierModel model)
    {
        if (model == null) throw new IllegalArgumentException("No model.");
        if (controller == null) 
            throw new IllegalArgumentException("No control.");
        this.model = model;
        this.controller = controller;
        initComponents();
        buildGUI();
    }
    
    /**
     * Sets the specified thumbnail 
     * 
     * @param thumbnail The thumbnail to set.
     */
    void setThumbnail(BufferedImage thumbnail)
    {
        JLabel label = new JLabel(new ImageIcon(thumbnail));
        label.addMouseListener(new MouseAdapter() {
            
            /**
             * Views the image if the user double-clicks on the thumbnail.
             */
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2) 
                    model.browse(model.getDataObject());
            }
        });
        titlePanel.setIconComponent(label);
    }
    
    /** Displays the classifications. */
    void showClassifications()
    {
        remove(centerPanel);
        add(getClassificationComponent(), BorderLayout.CENTER);
        validate();
        repaint();
    }
    
}
