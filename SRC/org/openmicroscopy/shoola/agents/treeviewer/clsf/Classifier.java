/*
 * org.openmicroscopy.shoola.agents.treeviewer.clsf.ClassifierWin
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
import java.util.HashMap;
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
import pojos.ImageData;

/** 
 * The component hosting the classification tree.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public abstract class Classifier
    extends JPanel
{

    /** Identifies the classify panel. */
    public static final int     CLASSIFY_MODE = 0;
    
    /** Identifies the classify panel. */
    public static final int     DECLASSIFY_MODE = 1;
    
    /** Bounds property to indicate that the classification is cancelled. */
    public static final String  CANCEL_CLASSIFICATION_PROPERTY = 
                                    "cancelClassification";
    
    /** Bounds property to indicate to classify the currently selected image. */
    public static final String  CLASSIFY_PROPERTY = "classify";
    
    /** 
     * Bounds property to indicate to declassify the currently selected image. 
     */
    public static final String  DECLASSIFY_PROPERTY = "declassify";
    
    /** 
     * Bounds property to indicate to browse the selected category or 
     * categoryGroup. 
     */
    public static final String  BROWSE_PROPERTY = "browse";
    
    /** Button to finish the operation. */
    private JButton         finishButton;
    
    /** Button to cancel the object creation. */
    private JButton         cancelButton;
    
    /** The UI component hosting the title. */
    private TitlePanel      titlePanel;
    
    /** One of the constants defined by this class. */
    private int             mode;
    
    /** The image to classify or declassify. */
    private ImageData       image;
    
    /** The tree hosting the hierarchical structure. */
    protected TreeCheck     tree;
    
    /** Component used to sort the nodes. */
    protected ViewerSorter  sorter;
    
    /**
     * All the paths in the Category Group trees that
     * are available for classification/declassification.
     */
    protected Set           paths;
    
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
        HashMap map = new HashMap(1);
        map.put(image, paths);
        switch (mode) {
            case CLASSIFY_MODE:
                firePropertyChange(CLASSIFY_PROPERTY, null, map);
                break;
            case DECLASSIFY_MODE:
                firePropertyChange(DECLASSIFY_PROPERTY, null, map);
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
        int row = tree.getRowForLocation(p.x, p.y);
        if (row != -1) {
            tree.setSelectionRow(row);
            if (me.getClickCount() != 2) return;
            Object node = tree.getLastSelectedPathComponent();
            if (!(node instanceof TreeCheckNode)) return;
            Object object = ((TreeCheckNode) node).getUserObject();
            if (object instanceof DataObject)
                firePropertyChange(BROWSE_PROPERTY, null, object);
        }
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
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {  
                firePropertyChange(CANCEL_CLASSIFICATION_PROPERTY, 
                                    Boolean.FALSE, Boolean.TRUE);
            }
        });
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
    protected void buildGUI()
    {
        setLayout(new BorderLayout(0, 0));
        add(titlePanel, BorderLayout.NORTH);
        add(getClassificationComponent(), BorderLayout.CENTER);
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
     * @param paths The paths to display. Mustn't be <code>null</code>.
     * @param mode  The type of classifer. One of the constants defined
     *              by this class.
     * @param image The image to classify. Mustn't be <code>null</code>.
     */
    Classifier(Set paths, int mode, ImageData image)
    {
        if (paths == null) throw new IllegalArgumentException("No paths.");
        if (image == null) throw new IllegalArgumentException("No image.");
        this.image = image;
        this.paths = paths;
        this.mode = mode;
        initComponents();
    }
    
    /**
     * Sets the specified thumbnail 
     * 
     * @param thumbnail The thumbnail to set.
     */
    public void setThumbnail(BufferedImage thumbnail)
    {
        if (thumbnail ==  null) return;
        JLabel label = new JLabel(new ImageIcon(thumbnail));
        label.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                    firePropertyChange(BROWSE_PROPERTY, null, image);
            }
        });
        titlePanel.setIconComponent(label);
    }

}
