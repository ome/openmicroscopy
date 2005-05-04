/*
 * org.openmicroscopy.shoola.agents.datamng.ClassifierPaneManager
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.CategoryData;
import org.openmicroscopy.shoola.env.data.model.CategoryGroupData;
import org.openmicroscopy.shoola.env.data.model.DataObject;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.env.ui.UserNotifier;

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
public class ClassifierPaneManager
{

    private static final String         LOADING = "Loading...";
    
    private static final String         EMPTY = "Empty";
    
    private static final String         TITLE = "CategoryGroup/Category/Image";
    
    /** This UI component's view. */
    private ClassifierPane              view; 
    
    /** The agent's control component. */
    private DataManagerCtrl             agentCtrl;
    
    /** Root of the tree. */
    private DefaultMutableTreeNode      root;
    
    /** 
     * Map of expanded category nodes.
     * Used to update image node (if visible) when the image's name has been
     * modified.
     */
    private Map                     cNodes;
    
    /** List of images displayed in the expanded category node. */
    private Map                     imagesInCategory;                    

    /** 
     * Map with all categoryGroupd nodes displayed.
     */
    private Map                     gNodes;
    
    private boolean                 treeLoaded;
    
    /** Creates a new instance. */
    public ClassifierPaneManager(ClassifierPane view, DataManagerCtrl control)
    {
        this.view = view;
        agentCtrl = control;
        gNodes = new TreeMap();
        cNodes = new TreeMap();
        initListeners();
    }

    /** 
     * Builds the tree model to represent the project-dataset
     * hierarchy.
     *
     * @param name      user's last name.
     * @return  A tree model containing the categoryGroup/category hierarchy.
     */
    DefaultMutableTreeNode getTreeModel()
    {
        root = new DefaultMutableTreeNode(TITLE);
        DefaultMutableTreeNode childNode = 
                                new DefaultMutableTreeNode("");
        DefaultTreeModel treeModel = (DefaultTreeModel) view.tree.getModel();
        treeModel.insertNodeInto(childNode, root, root.getChildCount());
        return root;
    }
    
    /** 
     * Makes a tree node for every category in <code>group</code> and adds 
     * each of those nodes to the node representing <code>g</code>, that is, 
     * <code>gNode</code>. 
     *
     * @param g     CategoryGroupData.
     * @param gNode The node for CategoryGroupData <code>g</code>.
     */
    private void addCategoriesToGroup(CategoryGroupData g, 
                                    DefaultMutableTreeNode gNode, 
                                    DefaultTreeModel treeModel)
    {
        List l = g.getCategories();
        Iterator dIter = l.iterator();
        DefaultMutableTreeNode cNode;
        while (dIter.hasNext()) {
            cNode = new DefaultMutableTreeNode(dIter.next());
            treeModel.insertNodeInto(cNode, gNode, gNode.getChildCount());
            treeModel.insertNodeInto(new DefaultMutableTreeNode(LOADING),
                                    cNode, cNode.getChildCount());
        }
    }
    
    /** Update a <code>CategoryGroup</code> node. */
    void updateGroupInTree()
    {
        DefaultTreeModel treeModel = (DefaultTreeModel) view.tree.getModel();
        DefaultMutableTreeNode gNode;
        List l = agentCtrl.getCategoryGroups();     
        if (l == null) return;
        Iterator j = l.iterator();
        CategoryGroupData cg;
        Integer id;
        while (j.hasNext()) {
            cg = (CategoryGroupData) j.next();
            id = new Integer(cg.getID());
            gNode = (DefaultMutableTreeNode) gNodes.get(id);
            gNode.removeAllChildren();
            addCategories(cg.getCategories(), gNode, false);
            treeModel.reload(gNode);
        }
    }
    
    /** Update a <code>dataset</code> node. */
    void updateCategoryInTree()
    {   
        DefaultTreeModel treeModel = (DefaultTreeModel) view.tree.getModel();
        DefaultMutableTreeNode gNode;                                       
        List l = agentCtrl.getCategoryGroups(); 
        if (l == null) return;
        Iterator j = l.iterator();
        CategoryGroupData cg;
        Integer id;
        while (j.hasNext()) {
            cg = (CategoryGroupData) j.next();
            id = new Integer(cg.getID());
            gNode = (DefaultMutableTreeNode) gNodes.get(id);
            gNode.removeAllChildren();
            addCategories(cg.getCategories(), gNode, false);
            treeModel.reload(gNode);
        }  
    }
    
    /** 
     * Called when a <code>group</code> node is modified. 
     * 
     * @param l             list of categories in the group.
     * @param gNode         group node associated to the group object.
     * @param isVisible     true if the node has to be reloaded false 
     *                      otherwise.
     */
    private void addCategories(List l, DefaultMutableTreeNode gNode,
                            boolean isVisible)
    {
        Iterator i = l.iterator();
        //CategorySummary cd;
        DefaultMutableTreeNode cNode;
        DefaultTreeModel treeModel = (DefaultTreeModel) view.tree.getModel();
        while (i.hasNext()) {
            cNode = new DefaultMutableTreeNode(i.next());
            treeModel.insertNodeInto(cNode, gNode, gNode.getChildCount());
            treeModel.insertNodeInto(new DefaultMutableTreeNode(LOADING),
                                                cNode, cNode.getChildCount());
            if (isVisible) setNodeVisible(cNode, gNode);
        }   
    }
    
    /** 
     * Update image data in the Tree.
     * If the image is displayed in several expanded datasets, the image data
     * node will also be updated.
     * 
     * @param is ImageSummary object.
     */
    void updateImageInTree(ImageSummary is)
    {
        DefaultTreeModel treeModel = (DefaultTreeModel) view.tree.getModel();
        if (cNodes == null || cNodes.size() == 0) return;
        Iterator i = cNodes.keySet().iterator();
        Iterator j;
        DefaultMutableTreeNode dNode;
        List dNodes, images;
        Integer id;
        //First update the images in list of expanded categories.
        updateImagesInCategory(is);
        //Then update the tree.
        while (i.hasNext()) {
            id = (Integer) i.next();
            images = (List) imagesInCategory.get(id);
            dNodes = (List) cNodes.get(id);
            j = dNodes.iterator();
            while (j.hasNext()) {
                dNode = (DefaultMutableTreeNode) j.next();
                dNode.removeAllChildren();
                addImagesToCategory(images, dNode); 
                treeModel.reload(dNode);
            }
        }
    }
    
    /** 
     * Update the map of expanded categories.
     * 
     * @param retVal    ImageSummary object.
     */
    private void updateImagesInCategory(ImageSummary retVal)
    {
        Iterator i = imagesInCategory.keySet().iterator();
        Iterator j;
        List images;
        ImageSummary is;
        while (i.hasNext()) {
            images = (List) imagesInCategory.get(i.next());
            j = images.iterator();
            while (j.hasNext()) {
                is = (ImageSummary) j.next();
                if (is.getID() == retVal.getID()) {
                    is.setName(retVal.getName());
                    break;
                }
            }
        }
    }
    
    /** 
     * Add a new group to the Tree. 
     * 
     * @param cgd    CategoryGroupData object to add.
     */
    void addNewGroupToTree(CategoryGroupData cgd)
    {
        DefaultTreeModel treeModel = (DefaultTreeModel) view.tree.getModel();
        DefaultMutableTreeNode gNode = new DefaultMutableTreeNode(cgd);
        treeModel.insertNodeInto(gNode, root, root.getChildCount());
        gNodes.put(new Integer(cgd.getID()), gNode);
        List l = cgd.getCategories();
        if (l != null) {
            Iterator i = l.iterator();
            DefaultMutableTreeNode cNode;
            while (i.hasNext()) {
                cNode = new DefaultMutableTreeNode(i.next());
                cNode.add(new DefaultMutableTreeNode(LOADING));
                gNode.add(cNode);
            }
        }
        setNodeVisible(gNode, root);                            
    }

    /**
     * Create and add an image's node to the category node.
     * 
     * @param images    List of image summary object.
     * @param cNode     The node representing the category.
     */
    private void addImagesToCategory(List images, DefaultMutableTreeNode cNode)
    {
        Iterator i = images.iterator();
        ImageSummary is;
        DefaultMutableTreeNode iNode;
        DefaultTreeModel treeModel = (DefaultTreeModel) view.tree.getModel();
        while (i.hasNext()) {
            is = (ImageSummary) i.next();
            iNode = new DefaultMutableTreeNode(is);
            treeModel.insertNodeInto(iNode, cNode, cNode.getChildCount());
        }
    }
    
    /**
     * Make sure the user can see the new node.
     * 
     * @param child     Node to display.
     * @param parent    Parent's node of the child.
     */
    private void setNodeVisible(DefaultMutableTreeNode child, 
                                DefaultMutableTreeNode parent)
    {
        view.tree.expandPath(new TreePath(parent.getPath()));
        view.tree.scrollPathToVisible(new TreePath(child.getPath()));
    }
    
    /** 
     * Handles mouse clicks within the tree component in the view.
     * If the mouse event is the platform popup trigger event, then the context 
     * popup menu is brought up. Otherwise, double-clicking on a project, 
     * dataset node brings up the corresponding property sheet dialog.
     *
     * @param e   The mouse event.
     */
    private void onClick(MouseEvent e)
    {
        int selRow = view.tree.getRowForLocation(e.getX(), e.getY());
        if (selRow == -1) return;
        view.tree.setSelectionRow(selRow);
        DataObject target = view.getClassifyObject();
        if (target != null) {
            if (e.isPopupTrigger()) showPopupMenu(e, target);
            else {
                if (e.getClickCount() == 2)
                    agentCtrl.showProperties(target, 
                            DataManagerCtrl.FOR_CLASSIFICATION);
            }
        } else { //click on the root node.
            if (e.getClickCount() == 2 && !treeLoaded) rebuildTree();
            else if (treeLoaded && e.isPopupTrigger()) showPopupMenu(e, null);
        }
    }
    
    /** Bring up the popupMenu. */
    private void showPopupMenu(MouseEvent e, DataObject target)
    {
        DataManagerUIF presentation = 
            agentCtrl.getAbstraction().getPresentation();
        TreePopupMenu popup = presentation.getPopupMenu();
        popup.setTarget(target);  
        popup.show(view.tree, e.getX(), e.getY());
    }
    
    /**
     * Handle the tree expansion event.
     * 
     * @param e             event   
     * @param isExpanding   true if a treeExpanded event has been fired,
     *                      false otherwise.
     */
    private void onNodeNavigation(TreeExpansionEvent e, boolean isExpanding)
    {
        TreePath path = e.getPath();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) 
                                        path.getLastPathComponent();
        Object usrObject = node.getUserObject();
        //category node
        //dataset summary node
        if (usrObject instanceof CategoryData) {
            categoryNodeNavigation((CategoryData) usrObject, node, 
                                    isExpanding);
        } else if (usrObject instanceof CategoryGroupData) {
            agentCtrl.setSelectedCategoryGroup(
                    ((CategoryGroupData) usrObject).getID());
        } else {
            if (node.equals(root) && !treeLoaded && isExpanding) rebuildTree();
        }
    }
    
    /** Refresh the specified category. */
    void refreshCategoryInTree(CategoryData data)
    {
        //no category node expanded
        if (cNodes.size() == 0) return;
        List nodes = (List) cNodes.get(new Integer(data.getID()));
        //The category hasn't been expanded, in this case, we do nothing
        if (nodes == null) return;
        Iterator i = nodes.iterator();
        DefaultTreeModel treeModel = (DefaultTreeModel) view.tree.getModel();
        DefaultMutableTreeNode node;   
        List images = data.getImages();
        while (i.hasNext()) {
            node = (DefaultMutableTreeNode) i.next();
            node.removeAllChildren();
            if (images.size() != 0)
                addImagesToCategory(images, node);    
            else 
                treeModel.insertNodeInto(new DefaultMutableTreeNode(EMPTY), node, 
                    node.getChildCount());
            treeModel.reload(node);
        }       
    }
    
    /** 
     * Build the tree model to represent the 
     * CategogyGroup-category hierarchy.
     */
    void rebuildTree()
    {
        List l = agentCtrl.getCategoryGroups();
        DefaultTreeModel treeModel = (DefaultTreeModel) view.tree.getModel();
        root.removeAllChildren();
        if (l == null || l.size() == 0) {
            treeModel.insertNodeInto(new DefaultMutableTreeNode(""), root, 
                    root.getChildCount());
            treeModel.reload(root);
            view.tree.collapsePath(new TreePath(root.getPath()));
            //Bring up the CreateCategoryGroup Panel
            agentCtrl.createGroup();
            UserNotifier un = agentCtrl.getRegistry().getUserNotifier();
            un.notifyInfo("Classifications", "No categoryGroup created");
            return;
        }
        Iterator i = l.iterator();
        CategoryGroupData cgd;
        DefaultMutableTreeNode gNode;
        while (i.hasNext()) {
            cgd = (CategoryGroupData) i.next();
            gNode = new DefaultMutableTreeNode(cgd);
            treeModel.insertNodeInto(gNode, root, root.getChildCount());
            gNodes.put(new Integer(cgd.getID()), gNode);
            addCategoriesToGroup(cgd, gNode, treeModel); 
        }
        treeModel.reload(root);
        treeLoaded = true;  
    }
    
    /** 
     * Handle the navigation when the node which fired the treeExpansion
     * event is a node for a {@link CategorySummary}.
     * 
     * @param ds            CategorySummary object.
     * @param node          Node which fired event.
     * @param isExpanding   True is the node is expanded false otherwise.
     */
    private void categoryNodeNavigation(CategoryData data, 
                                        DefaultMutableTreeNode node, 
                                        boolean isExpanding)
    {
        DefaultTreeModel treeModel = (DefaultTreeModel) view.tree.getModel();
        Integer id = new Integer(data.getID());
        node.removeAllChildren();
        if (isExpanding) {
            List list = data.getImages();
           
            //TODO: loading will never be displayed b/c we are in the
            // same thread.
            if (list.size() != 0) {
                addNodesToCategoryMaps(id, node, list);
                addImagesToCategory(list, node);     
            } else
                treeModel.insertNodeInto(new DefaultMutableTreeNode(EMPTY), 
                                        node, node.getChildCount());                      
        } else
            treeModel.insertNodeInto(new DefaultMutableTreeNode(LOADING), node, 
                                    node.getChildCount());        
        treeModel.reload(node);
    }
    
    /**
     * Add the specified node to the category maps.
     * @param datasetID     maps' key.
     * @param node          node to add.
     * @param images        list of images in the specified dataset.
     */
    private void addNodesToCategoryMaps(Integer id, DefaultMutableTreeNode node,
                                        List images)
    {
        if (imagesInCategory == null) imagesInCategory = new TreeMap();
        List lNodes = (List) cNodes.get(id);
        if (lNodes == null) lNodes = new ArrayList();
        lNodes.add(node);
        cNodes.put(id, lNodes);
        imagesInCategory.put(id, images);
    }
    
    /** 
     * Attach a mouse adapter to the tree in the view to get notified 
     * of mouse events on the tree.
     */
    private void initListeners()
    {
        view.tree.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { onClick(e); }
            public void mouseReleased(MouseEvent e) { onClick(e); }
        });
        view.tree.addTreeExpansionListener(new TreeExpansionListener() {
            public void treeCollapsed(TreeExpansionEvent e) {
                onNodeNavigation(e, false);
            }
            public void treeExpanded(TreeExpansionEvent e) {
                onNodeNavigation(e, true);  
            }   
        });
    }
    
}
