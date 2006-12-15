/*
 * org.openmicroscopy.shoola.agents.util.classifier.view.ClassifierUI 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.util.classifier.view;


//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.clsf.TreeCheck;
import org.openmicroscopy.shoola.util.ui.clsf.TreeCheckNode;
import pojos.CategoryData;

/** 
 * Component displaying the available CategoryGroup/Category paths.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class ClassifierUI 
	extends JScrollPane
{

    /** Message displayed if the image is unclassified. */
    private static final String     ADD_UNCLASSIFIED_TEXT = "The image " +
            "cannot be categorised. Please first create a category.";

    /** Message displayed if the image is unclassified. */
    private static final String     REMOVE_UNCLASSIFIED_TEXT = "The selected " +
                                    "image hasn't been categorised.";
    
    /** Reference to the <code>Model</code>. */
    private ClassifierModel 		model;
    
    /** Reference to the <code>Control</code>. */
    private ClassifierControl		controller;
    
    /** The tree hosting the hierarchical structure. */
    private TreeCheck             	tree;
    
    /** Component used to sort the nodes. */
    private ViewerSorter          	sorter;
    
    /** Initializes the UI components. */
    private void initComponents()
    {
    	sorter = new ViewerSorter();
    	tree = new TreeCheck("", null);
    	tree.setRootVisible(false);
    	tree.addPropertyChangeListener(TreeCheck.NODE_SELECTED_PROPERTY, 
										controller);
    }
    
    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
    	setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
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
            tree.expandPath(new TreePath(display.getPath()));
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
        Set paths = model.getClassificationPaths();
        if (paths.size() == 0) {
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
        return tree;
        //return new JScrollPane(tree);
    }
    
    /**
     * Returns the note displaying the unclassified message.
     * 
     * @return See above.
     */
    private String getUnclassifiedNote()
    {
        switch (model.getMode()) {
            case Classifier.CLASSIFY_MODE: return ADD_UNCLASSIFIED_TEXT;
            case Classifier.DECLASSIFY_MODE: return REMOVE_UNCLASSIFIED_TEXT;
        }
        return "";
    }
    
    /** Displays the classifications. */
    void showClassifications()
    {
    	getViewport().add(getClassificationComponent());
    }
    
    /**
     * Returns the collection of selected paths or <code>null</code>
     * if no path selected.
     * 
     * @return See above.
     */
    Set getSelectedPaths()
    {
    	Set nodes = tree.getSelectedNodes(); 
    	if (nodes == null || nodes.size() == 0) return null;
    	Set paths = new HashSet(nodes.size()); 
        Iterator i = nodes.iterator();
        Object object; 
        while (i.hasNext()) { 
            object = ((TreeCheckNode) i.next()).getUserObject(); 
            if (object instanceof CategoryData) paths.add(object); 
        } 
        return paths;
    }
    
	/**
	 * Creates a new instance.
	 * 
	 * @param model 	 Reference to the model. Mustn't be <code>null</code>.
	 * @param controller Reference to the control. Mustn't be <code>null</code>.
	 */
    ClassifierUI(ClassifierModel model, ClassifierControl controller)
	{
		if (model == null) 
			throw new IllegalArgumentException("No model.");
		if (controller == null) 
			throw new IllegalArgumentException("No control.");
		this.model = model;
		this.controller = controller;
		initComponents();
		buildGUI();
	}
	
}
