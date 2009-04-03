/*
 * rg.openmicroscopy.shoola.agents.treeviewer.util.FilterWindow
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

package org.openmicroscopy.shoola.agents.treeviewer.util;


//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
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
import javax.swing.tree.TreePath;



//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.env.ui.ViewerSorter;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.clsf.TreeCheck;
import org.openmicroscopy.shoola.util.ui.clsf.TreeCheckNode;
import pojos.CategoryData;
import pojos.DatasetData;

/** 
 * This Component is used to select the containers from which the images are
 * retrieved. The containers are either <code>Dataset</code> or 
 * <code>Category</code>.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class FilterWindow
    extends JDialog
{

    /** The <code>Dataset</code> container type. */
    public static final int         DATASET = 0;
    
    /** The <code>Category</code> container type. */
    public static final int         CATEGORY = 1;
    
    /** Bound property name indicating the close the window. */
    public static final String      CLOSE_PROPERTY = "close";
    
    /** Text corresponding to the {@link #DATASET} type. */
    private static final String     DATASET_MSG = "datasets";
    
    /** Text corresponding to the {@link #CATEGORY} type. */
    private static final String     CATEGORY_MSG = "categories";
    
    /** The title of the window. */
    private static final String     TITLE = "Filter images retrieval";
    
    /** The subtitle of the window. */
    private static final String     NOTE = "Select items in the following " +
                                            "list.";
    
    /** The subtitle of the window. */
    private static final String     MESSAGE = "No items available.";
    
    /** The default size of the window. */
    private static final Dimension  WINDOW_SIZE = new Dimension(500, 500);

    /** 
     * The size of the invisible components used to separate buttons
     * horizontally.
     */
    private static final Dimension  H_SPACER_SIZE = new Dimension(5, 10);
    
    /** The specified type of container. */
    private int             containerType;
    
    /** Button to close the window. */
    private JButton         cancelButton;
    
    /** Button to set the containers' selection. */ 
    private JButton         setButton;
    
    /** Button to select all items of the tree. */
    private JButton         selectAll;
    
    /** Button to clear the selection. */
    private JButton         clearAll;
    
    /** The tree hosting the hierarchical structure. */
    private TreeCheck       tree;
    
    /** The parent requesting this component. */
    private Object          parent;
    
    /** 
     * Controls if the supported type is supported.
     * 
     * @param type The type to control.
     */
    private void checkType(int type)
    {
        switch (type) {
            case DATASET:
            case CATEGORY:    
                break;
                default:
                    throw new IllegalArgumentException("Container not " +
                                                        "supported");
        }
    }
    
    /**
     * Returns the string corresponding to the {@link #containerType}.
     * 
     * @return See above.
     */
    private String getContainerString()
    {
        switch (containerType) {
            case DATASET: return DATASET_MSG;
            case CATEGORY: return CATEGORY_MSG;
        }
        return "";
    }
    
    /** Initializes the UI components. */
    private void initComponents()
    {
        IconManager im = IconManager.getInstance();
        tree = new TreeCheck("", im.getIcon(IconManager.ROOT)); 
        
        selectAll = new JButton("Select All");
        selectAll.setToolTipText(
                UIUtilities.formatToolTipText("Select all items."));
        selectAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            { 
                tree.selectAllNodes(); 
            }
        });
        clearAll = new JButton("Deselect All");
        clearAll.setToolTipText(
                UIUtilities.formatToolTipText("Clear selection."));
        clearAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                tree.deselectAllNodes();
            }
        });
        cancelButton = new JButton("Cancel");
        cancelButton.setToolTipText(
                UIUtilities.formatToolTipText("Close the window."));
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { close(); }
        });
        setButton = new JButton("Apply");
        setButton.setToolTipText(
                UIUtilities.formatToolTipText("Apply the selection."));
        setButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { setValues(); }
        });
    }
    
    /**
     * Builds the tool bar hosting the {@link #cancelButton} and
     * {@link #setButton}.
     * 
     * @return See above;
     */
    private JToolBar buildRightToolBar()
    {
        JToolBar bar = new JToolBar();
        bar.setRollover(true);
        bar.setBorder(null);
        bar.setFloatable(false);
        bar.add(setButton);
        bar.add(Box.createRigidArea(H_SPACER_SIZE));
        bar.add(cancelButton);
        return bar;
    }
    
    /**
     * Builds the tool bar hosting the {@link #selectAll} and {@link #clearAll}.
     * 
     * @return See above;
     */
    private JToolBar buildLeftToolBar()
    {
        JToolBar bar = new JToolBar();
        bar.setRollover(true);
        bar.setBorder(null);
        bar.setFloatable(false);
        bar.add(selectAll);
        bar.add(Box.createRigidArea(H_SPACER_SIZE));
        bar.add(clearAll);
        return bar;
    }

    /**
     * Creates the message.
     * 
     * @return See above.
     */
    private String createMessage()
    {
        String msg = "Limit the images retrieval by selecting items in the " +
                        "following list of "+getContainerString()+".";
        return msg;
    }
    
    /**
     * Builds the component hosting the tree. If the specified collection
     * is empty then a message is displayed.
     * 
     * @param nodes The nodes to add to the tree.
     * @return See above.
     */
    private JComponent getFilterComponent(Set nodes)
    {
        if (nodes.size() == 0) {
            setButton.setEnabled(false);
            JPanel p = new JPanel();
            p.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 10));
            p.add(UIUtilities.setTextFont(MESSAGE), BorderLayout.CENTER);
            return p;
        }
        //      populates the tree
        DefaultTreeModel dtm = (DefaultTreeModel) tree.getModel();
        TreeCheckNode root = (TreeCheckNode) dtm.getRoot();
        Iterator i = nodes.iterator();
        while (i.hasNext())
            root.addChildDisplay((TreeCheckNode) i.next()) ;
        ViewerSorter sorter = new ViewerSorter();
        List sortedNodes = sorter.sort(nodes);
        i = sortedNodes.iterator();
        TreeCheckNode display;
        while (i.hasNext()) {
            display = (TreeCheckNode) i.next();
            dtm.insertNodeInto(display, root, root.getChildCount());
        }  
        dtm.reload();
        tree.expandPath(new TreePath(root.getPath()));
        return new JScrollPane(tree);
    }
    
    /** 
     * Builds and lays out the GUI. 
     * 
     * @param filterComponent The component hosting the tree.
     */
    private void buildGUI(JComponent filterComponent)
    {
        JPanel contentPanel = new JPanel();
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(5, 5, 15, 10)));
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        IconManager im = IconManager.getInstance();
        TitlePanel tp = new TitlePanel(TITLE, NOTE, createMessage(), 
                                    im.getIcon(IconManager.FILTER_BIG));
        //Set the layout and add components
        Container c = getContentPane();
        c.setLayout(new BorderLayout(0, 0));
        c.add(tp, BorderLayout.NORTH);
        c.add(filterComponent, BorderLayout.CENTER);
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.setBorder(BorderFactory.createEtchedBorder());
        p.add(UIUtilities.buildComponentPanel(buildLeftToolBar()));
        p.add(UIUtilities.buildComponentPanelRight(buildRightToolBar()));
        p.setOpaque(true);
        c.add(p, BorderLayout.SOUTH);
    }
    
    /** Binds the {@link #close() close} action to the exit event generated. */
    private void attachListeners()
    {
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) { close(); }
        });
    }
    
    /** Closes and disposes of the window. */
    private void close()
    {
        firePropertyChange(CLOSE_PROPERTY, Boolean.FALSE, Boolean.TRUE);
        setVisible(false);
        dispose();
    }
    
    /** Sets the selected values and closes the window. */
    private void setValues()
    {
        //Need to retrieve the node.
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
            if ((object instanceof CategoryData) || 
                 (object instanceof DatasetData)) paths.add(object); 
        } 
        HashMap map = new HashMap(1);
        map.put(parent, paths);
        firePropertyChange(TreeViewer.FILTER_NODES_PROPERTY, null, map);
        setVisible(false);
        dispose();
    }
    
    /**
     * Creates a new instance. 
     * 
     * @param parent        The parent requesting this window.
     *                      Mustn't be <code>null</code>.
     * @param owner         The owner of the frame.
     * @param containerType The type of container this window is for. One of the
     *                      following constants: {@link #DATASET} or 
     *                      {@link #CATEGORY}.
     * @param nodes         The nodes to display. Mustn't be <code>null</code>.
     */
    public FilterWindow(Object parent, JFrame owner, int containerType, 
                        Set nodes)
    {
        super(owner, "Filter Images Retrieval", true);
        if (nodes == null) 
            throw new IllegalArgumentException("No nodes.");
        if (parent == null)
            throw new IllegalArgumentException("Parent cannot be null.");
        checkType(containerType);
        this.parent = parent;
        this.containerType = containerType;
        initComponents();
        buildGUI(getFilterComponent(nodes));
        attachListeners();
        setSize(WINDOW_SIZE);
    }
    
}
