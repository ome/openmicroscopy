/*
 * org.openmicroscopy.shoola.agents.datamng.ExplorerPaneManager
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.ProjectData;

/** 
 * Modify the tree according to the events fired by the editors.
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
class ExplorerPaneManager
{

    private static final String     LOADING = "Loading...";
    
    private static final String     EMPTY = "Empty";
    
    private static final String     TITLE = "Project/Dataset/Image";
    
    /** This UI component's view. */
    private ExplorerPane            view; 
    
    /** The agent's control component. */
    private DataManagerCtrl         agentCtrl;
    
    /** Root of the tree. */
    private DefaultMutableTreeNode  root;
    
    /** 
     * Boolean value used to control if the project-dataset hierarchy has 
     * been loaded. 
     */
    private boolean                 treeLoaded;
    
    /** 
     * Map of expanded dataset nodes.
     * Key: datasetSummary id, value: list of expanded nodes.
     * Used to update image node (if visible) when the image's name has been
     * modified.
     */
    private Map                     cDNodes;
    
    /** List of images displayed in the expanded dataset node. */
    private Map                     imagesInDataset;                    

    /** 
     * Map with all project summary nodes displayed.
     * key: project summary id, value: corresponding node.
     */
    private Map                     pNodes;
    
    
    /** 
     * Makes a tree node for every dataset in <code>p</code> and adds 
     * each of those nodes to the node representing <code>p</code>, that is, 
     * <code>pNode</code>. 
     *
     * @param p     The project summary.
     * @param pNode The node for project <code>p</code>.
     */
    private void addDatasetsToProject(ProjectData p, 
                                    DefaultMutableTreeNode pNode, 
                                    DefaultTreeModel treeModel)
    {
        Set datasets = p.getDatasets();
        Iterator dIter = datasets.iterator();
        DatasetData d;
        DefaultMutableTreeNode dNode;
        while (dIter.hasNext()) {
            d = (DatasetData) dIter.next();
            dNode = new DefaultMutableTreeNode(d);
            treeModel.insertNodeInto(dNode, pNode, pNode.getChildCount());
            treeModel.insertNodeInto(new DefaultMutableTreeNode(LOADING),
                                    dNode, dNode.getChildCount());
        }
    }
    
    /** 
     * Called when a <code>project</code> node is modified. 
     * 
     * @param datasets      list of datasets in the project.
     * @param pNode         project node associated to the project 
     *                      summary object.
     * @param isVisible     true if the node has to be reloaded false 
     *                      otherwise.
     */
    private void addDatasets(Set datasets, DefaultMutableTreeNode pNode,
                            boolean isVisible)
    {
        Iterator i = datasets.iterator();
        DatasetData d;
        DefaultMutableTreeNode dNode;
        DefaultTreeModel treeModel = (DefaultTreeModel) view.tree.getModel();
        while (i.hasNext()) {
            d = (DatasetData) i.next();
            dNode = new DefaultMutableTreeNode(d);
            treeModel.insertNodeInto(dNode, pNode, pNode.getChildCount());
            treeModel.insertNodeInto(new DefaultMutableTreeNode(LOADING),
                                                dNode, dNode.getChildCount());
            if (isVisible) setNodeVisible(dNode, pNode);
        }   
    }
  
    /** 
     * Update the map of expanded datasets.
     * 
     * @param retVal    ImageSummary object.
     */
    private void updateImagesInDataset(ImageData retVal)
    {
        Iterator i = imagesInDataset.keySet().iterator();
        Iterator j;
        Set images;
        ImageData data;
        while (i.hasNext()) {
            images = (Set) imagesInDataset.get(i.next());
            j = images.iterator();
            while (j.hasNext()) {
                data = (ImageData) j.next();
                if (data.getId() == retVal.getId()) {
                    data.setName(retVal.getName());
                    break;
                }
            }
        }
    }
    
    /** 
     * Handle the navigation when the node which fired the treeExpansion
     * event is a dataset summary node.
     * 
     * @param d            DatasetSummary object.
     * @param node          Node which fired event.
     * @param isExpanding   True is the node is expanded false otherwise.
     */
    private void datasetNodeNavigation(DatasetData d, 
                                        DefaultMutableTreeNode node, 
                                        boolean isExpanding)
    {
        DefaultTreeModel treeModel = (DefaultTreeModel) view.tree.getModel();
        Integer datasetID = new Integer(d.getId());
        node.removeAllChildren();
        if (isExpanding) {
            Set images = agentCtrl.getImages(d.getId());
            //TODO: loading will never be displayed b/c we are in the
            // same thread.
            if (images.size() != 0) {
                addNodesToDatasetMaps(datasetID, node, images);
                addImagesToDataset(images, node);     
            } else 
                treeModel.insertNodeInto(new DefaultMutableTreeNode(EMPTY), 
                                         node, node.getChildCount());                       
        } else 
            treeModel.insertNodeInto(new DefaultMutableTreeNode(LOADING), 
                                    node, node.getChildCount());        
        treeModel.reload(node);
    }
    
    /**
     * Add the specified node to the dataset maps.
     * @param datasetID     maps' key.
     * @param node          node to add.
     * @param images        list of images in the specified dataset.
     */
    private void addNodesToDatasetMaps(Integer datasetID, 
                                        DefaultMutableTreeNode node,
                                        Set images)
    {
        if (imagesInDataset == null) imagesInDataset = new TreeMap();
        Set lNodes = (Set) cDNodes.get(datasetID);
        if (lNodes == null) lNodes = new HashSet();
        lNodes.add(node);
        cDNodes.put(datasetID, lNodes);
        imagesInDataset.put(datasetID, images);
    }
    

    /**
     * Create and add an image's node to the dataset node.
     * 
     * @param images    List of image summary object.
     * @param dNode     The node for the dataset
     */
    private void addImagesToDataset(Set images, DefaultMutableTreeNode dNode)
    {
        Iterator i = images.iterator();
        ImageData data;
        DefaultMutableTreeNode iNode;
        DefaultTreeModel treeModel = (DefaultTreeModel) view.tree.getModel();
        while (i.hasNext()) {
            data = (ImageData) i.next();
            iNode = new DefaultMutableTreeNode(data);
            treeModel.insertNodeInto(iNode, dNode, dNode.getChildCount());
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
        if (selRow != -1) {
            view.tree.setSelectionRow(selRow);
            pojos.DataObject target = view.getCurrentOMEObject();
            if (target != null) {
                if (e.isPopupTrigger()) showPopupMenu(e, target);
                else {
                    if (e.getClickCount() == 2)
                        agentCtrl.showProperties(target,
                                DataManagerCtrl.FOR_HIERARCHY);
                }
            } else { //click on the root node.
                if (e.getClickCount() == 2 && !treeLoaded) rebuildTree();
                else if (treeLoaded && e.isPopupTrigger()) 
                    showPopupMenu(e, null);
            }
        }
    }
    
    /** Bring up the popupMenu. */
    private void showPopupMenu(MouseEvent e, DataObject target)
    {
        DataManagerUIF presentation = 
            agentCtrl.getAbstraction().getPresentation();
        TreePopupMenu popup = presentation.getPopupMenu();
        popup.setTarget(target);  
        popup.setIndex(DataManagerCtrl.FOR_HIERARCHY);
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
        //dataset summary node
        if (usrObject instanceof DatasetData)
            datasetNodeNavigation((DatasetData) usrObject, node, isExpanding);
        else {
            if (node.equals(root) && !treeLoaded && isExpanding) rebuildTree();
        }
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
    
    ExplorerPaneManager(ExplorerPane view, DataManagerCtrl agentCtrl)
    {
        this.view = view;
        this.agentCtrl = agentCtrl;
        pNodes = new TreeMap();
        cDNodes = new TreeMap();
        initListeners();
        treeLoaded = false;
    }
    
    boolean isTreeLoaded() { return treeLoaded; }
    
    /** 
     * Builds the tree model to represent the project-dataset
     * hierarchy.
     *
     * @return  A tree model containing the project-dataset hierarchy.
     */
    DefaultMutableTreeNode getUserTreeModel()
    {
        root = new DefaultMutableTreeNode(TITLE);
        DefaultMutableTreeNode childNode = 
                                new DefaultMutableTreeNode("");
        DefaultTreeModel treeModel = (DefaultTreeModel) view.tree.getModel();
        treeModel.insertNodeInto(childNode, root, root.getChildCount());
        return root;
    }

    
    /** Update a <code>project</code> node. */
    void updateProjectInTree()
    {
        DefaultTreeModel treeModel = (DefaultTreeModel) view.tree.getModel();
        DefaultMutableTreeNode pNode;
        Set projects = agentCtrl.getUserProjects();    
        if (projects == null || projects.size() == 0) return;
        Iterator j = projects.iterator();
        ProjectData p;
        Integer projectID;
        while (j.hasNext()) {
            p = (ProjectData) j.next();
            projectID = new Integer(p.getId());
            pNode = (DefaultMutableTreeNode) pNodes.get(projectID);
            pNode.removeAllChildren();
            addDatasets(p.getDatasets(), pNode, false);
            treeModel.reload(pNode);
        }
    }
    
    /** Update a <code>dataset</code> node. */
    void updateDatasetInTree()
    {   
        DefaultTreeModel treeModel = (DefaultTreeModel) view.tree.getModel();
        DefaultMutableTreeNode pNode;                                       
        Set projects = agentCtrl.getUserProjects();
        if (projects == null || projects.size() == 0) return;
        Iterator j = projects.iterator();
        ProjectData p;
        while (j.hasNext()) {
            p = (ProjectData) j.next();
            pNode = (DefaultMutableTreeNode) pNodes.get(new Integer(p.getId()));
            pNode.removeAllChildren();
            addDatasets(p.getDatasets(), pNode, false);
            treeModel.reload(pNode);
        }  
    }
    
    /** Refresh the specified datasetSummary node. */
    void refreshDatasetInTree(DatasetData data)
    {
        //no dataset node expanded
        if (cDNodes.size() == 0) return;
        Set nodes = (Set) cDNodes.get(new Integer(data.getId()));
        //The dataset hasn't been expanded, in this case, we do nothing
        if (nodes == null) return;
        Iterator i = nodes.iterator();
        DefaultTreeModel treeModel = (DefaultTreeModel) view.tree.getModel();
        DefaultMutableTreeNode node;   
        Set images = agentCtrl.getImages(data.getId());
        while (i.hasNext()) {
            node = (DefaultMutableTreeNode) i.next();
            node.removeAllChildren();
            if (images.size() != 0)
                addImagesToDataset(images, node);    
            else 
                treeModel.insertNodeInto(new DefaultMutableTreeNode(EMPTY), 
                                        node, node.getChildCount());            
            treeModel.reload(node);
        }       
    }

    /** 
     * Update image data in the Tree.
     * If the image is displayed in several expanded datasets, the image data
     * node will also be updated.
     * 
     * @param data ImageSummary object.
     */
    void updateImageInTree(ImageData data)
    {
        DefaultTreeModel treeModel = (DefaultTreeModel) view.tree.getModel();
        if (cDNodes.size() == 0) return;
        Iterator i = cDNodes.keySet().iterator();
        Iterator j;
        DefaultMutableTreeNode dNode;
        Set dNodes, images;
        Integer datasetID;
        //First update the images in list of expanded datasets.
        updateImagesInDataset(data);
        //Then update the tree.
        while (i.hasNext()) {
            datasetID = (Integer) i.next();
            images = (Set) imagesInDataset.get(datasetID);
            dNodes = (Set) cDNodes.get(datasetID);
            j = dNodes.iterator();
            while (j.hasNext()) {
                dNode = (DefaultMutableTreeNode) j.next();
                dNode.removeAllChildren();
                addImagesToDataset(images, dNode); 
                treeModel.reload(dNode);
            }
        }
    }
    
    
    /** 
     * Add a new project to the Tree. 
     * 
     * @param data    Project summary to display.
     */
    void addNewProjectToTree(ProjectData data)
    {
        DefaultTreeModel treeModel = (DefaultTreeModel) view.tree.getModel();
        DefaultMutableTreeNode pNode = new DefaultMutableTreeNode(data);
        treeModel.insertNodeInto(pNode, root, root.getChildCount());
        pNodes.put(new Integer(data.getId()), pNode);
        Set datasets = data.getDatasets();
        if (datasets != null && datasets.size() != 0) {
            Iterator i = datasets.iterator();
            DefaultMutableTreeNode dNode;
            DatasetData d;
            while (i.hasNext()) {
                d = (DatasetData) i.next();
                dNode = new DefaultMutableTreeNode(d);
                dNode.add(new DefaultMutableTreeNode(LOADING));
                pNode.add(dNode);
            }
        }
        setNodeVisible(pNode, root);                            
    }
    
    /**
     * Add a new dataset to the tree.
     * 
     * @param projects      list of projects to which the dataset id added.
     */
    void addNewDatasetToTree(Set projects) 
    {
        DefaultTreeModel treeModel = (DefaultTreeModel) view.tree.getModel();
        DefaultMutableTreeNode pNode;                                                   
        Set userProjects = agentCtrl.getUserProjects();
        if (userProjects == null || userProjects.size() == 0) return;
        Iterator j = userProjects.iterator();
        ProjectData p;
        Integer projectID;
        while (j.hasNext()) {
            p = (ProjectData) j.next();
            projectID = new Integer(p.getId());
            pNode = (DefaultMutableTreeNode) pNodes.get(projectID);
            pNode.removeAllChildren();
            addDatasets(p.getDatasets(), pNode, projects.contains(p));
            treeModel.reload(pNode);
        }
    }


    /** Build the tree model to represent the project-dataset hierarchy. */
    void rebuildTree()
    {
        Set projects = agentCtrl.getUserProjects();
        DefaultTreeModel treeModel = (DefaultTreeModel) view.tree.getModel();
        root.removeAllChildren();
        if (projects == null || projects.size() == 0) {
            treeModel.insertNodeInto(new DefaultMutableTreeNode(""), root, 
                    root.getChildCount());
            treeModel.reload(root);
            view.tree.collapsePath(new TreePath(root.getPath()));
            agentCtrl.createProject();
            UserNotifier un = agentCtrl.getRegistry().getUserNotifier();
            un.notifyInfo("Hierarchy", "No project created");
            return;
        }
        Iterator i = projects.iterator();
        ProjectData p;
        DefaultMutableTreeNode pNode;
        while (i.hasNext()) {
            p = (ProjectData) i.next();
            pNode = new DefaultMutableTreeNode(p);
            treeModel.insertNodeInto(pNode, root, root.getChildCount());
            pNodes.put(new Integer(p.getId()), pNode);
            addDatasetsToProject(p, pNode, treeModel); 
        }
        treeModel.reload(root);
        treeLoaded = true;  
    }
    
}
