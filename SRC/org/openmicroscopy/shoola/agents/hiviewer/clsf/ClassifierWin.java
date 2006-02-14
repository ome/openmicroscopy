/*
 * org.openmicroscopy.shoola.agents.hiviewer.clsf.ClassifierWin
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

package org.openmicroscopy.shoola.agents.hiviewer.clsf;




//Java imports
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.tree.DefaultTreeModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.env.ui.ViewerSorter;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.clsf.TreeCheck;
import org.openmicroscopy.shoola.util.ui.clsf.TreeCheckNode;

import pojos.CategoryData;

/** 
 * The top container.
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
abstract class ClassifierWin
    extends JDialog
{
    
    /** 
     * Bound property name indicating that a new category has 
     * been selected.
     */
    public final static String      SELECTED_CATEGORY_PROPERTY = 
                                                            "selectedCategory";
    
    /** Bound property name indicating if the window is closed. */
    public final static String      CLOSED_PROPERTY = "closed";
    
    private static final Dimension  WIN_DIMENSION = new Dimension(300, 300);
    
    /** Horizontal space between the cells in the grid. */
    static final int                H_SPACE = 5;
    
    /** Button to finish the operation. */
    private JButton                 finishButton;
    
    /** Button to cancel the object creation. */
    private JButton                 cancelButton;
    
    /** The tree hosting the hierarchical structure. */
    protected TreeCheck             tree;
    
    /** Component used to sort the nodes. */
    protected ViewerSorter          sorter;
    
    /** 
     * The selected category to classify the image into or to remove the
     * classification from.
     */
    private CategoryData            selectedCategory;
    
    /**
     * All the paths in the Category Group trees that
     * are available for classification/declassification.
     */
    protected Set                   availablePaths;
    
    /** 
     * Adds the image to the selected categories or removes the image from
     * the selected categories.
     */
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
        firePropertyChange(SELECTED_CATEGORY_PROPERTY, null, paths);
    }
    
    /** Initializes the GUI components. */
    private void initComponents()
    {
        sorter = new ViewerSorter();
        IconManager im = IconManager.getInstance();
        tree = new TreeCheck("", im.getIcon(IconManager.ROOT)); 
        finishButton = new JButton("Finish");
        finishButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { finish(); }
        });
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {  
                setClosed();
            }
        });
    }
    
    /** Fires a property change event and closes the window. */ 
    private void setClosed()
    {
        firePropertyChange(CLOSED_PROPERTY, Boolean.TRUE, Boolean.FALSE);
        setVisible(false);
        dispose();
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
     * Returns the main panel added to this window.
     * 
     * @return See above.
     */
    private JComponent getClassifPanel()
    {
        if (availablePaths.size() == 0) {
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
        Iterator i = availablePaths.iterator();
        while (i.hasNext())
            root.addChildDisplay((TreeCheckNode) i.next()) ;
        buildTreeNode(root, sorter.sort(availablePaths));
        dtm.reload();
        return new JScrollPane(tree);
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
    
    /** Builds and lays out the GUI. */
    protected void buildGUI() 
    {
        IconManager icons = IconManager.getInstance();
        TitlePanel tp = new TitlePanel(getPanelTitle(), getPanelText(), 
                getPanelNote(), icons.getIcon(IconManager.CATEGORY_BIG));
        //Set layout and add components
        Container c = getContentPane();
        c.setLayout(new BorderLayout(0, 0));
        c.add(tp, BorderLayout.NORTH);
        c.add(getClassifPanel(), BorderLayout.CENTER);
        JPanel p = UIUtilities.buildComponentPanelRight(buildToolBar());
        p.setBorder(BorderFactory.createEtchedBorder());
        c.add(p, BorderLayout.SOUTH);
    }
    
    /** 
     * Returns the title displayed in the titlePanel.
     * 
     * @return See above.
     */
    protected abstract String getPanelTitle();
    
    /** 
     * Returns the text displayed in the titlePanel.
     * 
     * @return See above.
     */
    protected abstract String getPanelText();
    
    /**
     * Returns the note displayed in the titlePanel.
     * 
     * @return See above.
     */
    protected abstract String getPanelNote();
    
    /**
     * Returns the note displaying the unclassified message.
     * 
     * @return See above.
     */
    protected abstract String getUnclassifiedNote();
    
    /**
     * Creates a new instance.
     * 
     * @param availablePaths    The available paths to the images.
     *                          Mustn't be <code>null</code>.
     * @param owner The owner of this frame.
     */
    ClassifierWin(Set availablePaths, JFrame owner)
    {
        super(owner);
        if (availablePaths == null)
            throw new IllegalArgumentException("no paths");
        this.availablePaths = availablePaths;
        initComponents();
        setModal(true);
        setTitle("Classification");
        
        //AttachWindow Listener
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) { setClosed(); }
        });
    }

    /** 
     * The category selected, either to classify or declassify the 
     * selected image.
     * 
     * @param category The selected category.
     */
    void setSelectedCategory(CategoryData category)
    {
        Object oldValue = selectedCategory;
        selectedCategory = category;
        firePropertyChange(SELECTED_CATEGORY_PROPERTY, oldValue, 
                    selectedCategory);
        setClosed();
    }
    
    /** Brings up the window on screen and centers it. */
    void setOnScreen()
    {
        setSize(WIN_DIMENSION);
        UIUtilities.centerAndShow(this);
    }
    
}
