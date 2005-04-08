/*
 * org.openmicroscopy.shoola.agents.datamng.ClassifierPane
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

package org.openmicroscopy.shoola.agents.datamng;


//Java imports
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.CategoryData;
import org.openmicroscopy.shoola.env.data.model.CategoryGroupData;
import org.openmicroscopy.shoola.env.data.model.DataObject;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;

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
public class ClassifierPane
    extends JSplitPane
{

    /** This UI component's controller and model. */
    private ClassifierPaneManager       manager;
    
    /** Reference to the Registry. */
    private Registry                    registry;
    
    /** The tree used to represent the categoryGroup-category hierarchy. */
    JTree                               tree;
    
    JScrollPane                         mainPane;
    
    /** 
     * Creates a new instance.
     *
     *@param agentCtrl   The agent's control component.
     */
    ClassifierPane(DataManagerCtrl agentCtrl, Registry registry)
    {
        this.registry = registry;
        tree = new JTree();
        mainPane  = new JScrollPane();
        manager = new ClassifierPaneManager(this, agentCtrl);
        DefaultMutableTreeNode r = manager.getTreeModel();
        DefaultTreeModel dtm = new DefaultTreeModel(r);
        tree.setModel(dtm);
        tree.setShowsRootHandles(true);
        tree.collapsePath(new TreePath(r.getPath()));
        buildGUI();
    }
    
    /** Display the JComponent in the main JScrollPane. */
    void addToMainComponent(JComponent c)
    {
        JViewport port = mainPane.getViewport();
        port.removeAll();
        port.add(c);
    }
    
    /** Remove all components from the main JScrollPane. */
    void removeFromMainComponent()
    {
        mainPane.getViewport().removeAll();
    }
    
    /** 
     * Returns the last selected node on the tree only if that is a 
     * categoryGroup, category or image.
     * If the above condition is not met, <code>null</code> is returned instead.
     *
     * @return  See above.
     */
    DataObject getClassifyObject()
    {
        DataObject target = null;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                                            tree.getLastSelectedPathComponent();
        if (node != null) {
            Object  usrObj = node.getUserObject();
            if (usrObj instanceof CategoryGroupData || 
                usrObj instanceof CategoryData ||
                usrObj instanceof ImageSummary)
                target = (DataObject) usrObj;
        }
        return target;
    }   
    
    /** Return the manager of this component. */
    ClassifierPaneManager getManager() { return manager; }
    
    /** Builds and lay out the GUI. */
    private void buildGUI()
    {
        buildMain();
        setLeftComponent(buildTreeUI());
        setRightComponent(mainPane);
        setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        setContinuousLayout(true); 
        setOneTouchExpandable(true); 
        setDividerLocation(DataManagerUIF.DIVIDER_LOC); 
    }
    
    /** Display the Tree in a JScrollPane. */
    private JScrollPane buildTreeUI()
    {
        JScrollPane pane = new JScrollPane();
        tree.putClientProperty("JTree.lineStyle", "Angled");
        tree.setCellRenderer(new DataTreeCellRenderer(registry));
        tree.getSelectionModel().setSelectionMode(
                                    TreeSelectionModel.SINGLE_TREE_SELECTION);
        pane.setViewportView(tree);
        tree.setMinimumSize(DataManagerUIF.COMPONENT_MIN_DIM);
        return pane;
    }
    
    private void buildMain()
    {
        JPanel p = new JPanel();
        p.setMinimumSize(DataManagerUIF.COMPONENT_MIN_DIM);
        mainPane.getViewport().add(p);
    }
    
}
