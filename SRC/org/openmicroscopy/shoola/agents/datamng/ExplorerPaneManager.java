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
import org.openmicroscopy.shoola.env.data.model.DataObject;
import org.openmicroscopy.shoola.env.data.model.DatasetSummary;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.env.data.model.ProjectSummary;

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

	private static final String		LOADING = "Loading...";
	
	private static final String		EMPTY = "Empty";
	
	/** This UI component's view. */
	private ExplorerPane        	view; 
	
	/** The agent's control component. */
	private DataManagerCtrl     	agentCtrl;
	
	/** Root of the tree. */
	private DefaultMutableTreeNode  root;
	
	/** 
	 * Boolean value used to control if the project-dataset hierarchy has 
	 * been loaded. 
	 */
	private boolean					treeLoaded;
	
	/** 
	 * Map of expanded dataset nodes.
	 * Key: datasetSummary id, value: list of expanded nodes.
	 * Used to update image node (if visible) when the image's name has been
	 * modified.
	 */
	private Map						cDNodes;
	
	/** List of images displayed in the expanded dataset node. */
	private Map						imagesInDataset;					

	/** 
	 * Map with all project summary nodes displayed.
	 * key: project summary id, value: corresponding node.
	 */
	private Map						pNodes;
	
	ExplorerPaneManager(ExplorerPane view, DataManagerCtrl agentCtrl)
	{
		this.view = view;
		this.agentCtrl = agentCtrl;
		pNodes = new TreeMap();
		initListeners();
		treeLoaded = false;
	}
	
	boolean isTreeLoaded() { return treeLoaded; }
	
	/** 
	 * Builds the tree model to represent the project-dataset
	 * hierarchy.
	 *
     * @param name      user's last name.
	 * @return  A tree model containing the project-dataset hierarchy.
	 */
	DefaultMutableTreeNode getUserTreeModel(String name)
	{
		root = new DefaultMutableTreeNode(name+"'s OME");
		DefaultMutableTreeNode childNode = 
								new DefaultMutableTreeNode("");
		DefaultTreeModel treeModel = (DefaultTreeModel) view.tree.getModel();
		treeModel.insertNodeInto(childNode, root, root.getChildCount());
		return root;
	}
	
	/** 
	 * Makes a tree node for every dataset in <code>p</code> and adds 
	 * each of those nodes to the node representing <code>p</code>, that is, 
	 * <code>pNode</code>. 
	 *
	 * @param p		The project summary.
	 * @param pNode	The node for project <code>p</code>.
	 */
	private void addDatasetsToProject(ProjectSummary p, 
									DefaultMutableTreeNode pNode, 
									DefaultTreeModel treeModel)
	{
		List datasets = p.getDatasets();
		Iterator dIter = datasets.iterator();
		DatasetSummary ds;
		DefaultMutableTreeNode dNode;
		while (dIter.hasNext()) {
			ds = (DatasetSummary) dIter.next();
			dNode = new DefaultMutableTreeNode(ds);
			treeModel.insertNodeInto(dNode, pNode, pNode.getChildCount());
			treeModel.insertNodeInto(new DefaultMutableTreeNode(LOADING),
									dNode, dNode.getChildCount());
		}
	}
	
	/** Update a <code>project</code> node. */
	void updateProjectInTree()
	{
		DefaultTreeModel treeModel = (DefaultTreeModel) view.tree.getModel();
		DefaultMutableTreeNode pNode;
		List pSummaries = agentCtrl.getUserProjects();										
		if (pSummaries != null) {
			Iterator j = pSummaries.iterator();
			ProjectSummary ps;
			Integer projectID;
			while (j.hasNext()) {
				ps = (ProjectSummary) j.next();
				projectID = new Integer(ps.getID());
				pNode = (DefaultMutableTreeNode) pNodes.get(projectID);
				pNode.removeAllChildren();
				addDatasets(ps.getDatasets(), pNode, false);
				treeModel.reload(pNode);
			}
		}
	}
	
	/** Update a <code>dataset</code> node. */
	void updateDatasetInTree()
	{	
		DefaultTreeModel treeModel = (DefaultTreeModel) view.tree.getModel();
		DefaultMutableTreeNode pNode;										
		List pSummaries = agentCtrl.getUserProjects();
		if (pSummaries != null) {
			Iterator j = pSummaries.iterator();
			ProjectSummary ps;
			Integer projectID;
			while (j.hasNext()) {
				ps = (ProjectSummary) j.next();
				projectID = new Integer(ps.getID());
				pNode = (DefaultMutableTreeNode) pNodes.get(projectID);
				pNode.removeAllChildren();
				addDatasets(ps.getDatasets(), pNode, false);
				treeModel.reload(pNode);
			}
		}	
	}
	
	/** 
	 * Called when a <code>project</code> node is modified. 
	 * 
	 * @param datasets		list of datasets in the project.
	 * @param pNode			project node associated to the project 
	 * 						summary object.
	 * @param isVisible		true if the node has to be reloaded false 
	 * 						otherwise.
	 */
	private void addDatasets(List datasets, DefaultMutableTreeNode pNode,
							boolean isVisible)
	{
		Iterator i = datasets.iterator();
		DatasetSummary ds;
		DefaultMutableTreeNode dNode;
		DefaultTreeModel treeModel = (DefaultTreeModel) view.tree.getModel();
		while (i.hasNext()) {
			ds = (DatasetSummary) i.next();
			dNode = new DefaultMutableTreeNode(ds);
			treeModel.insertNodeInto(dNode, pNode, pNode.getChildCount());
			treeModel.insertNodeInto(new DefaultMutableTreeNode(LOADING),
												dNode, dNode.getChildCount());
			if (isVisible) setNodeVisible(dNode, pNode);
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
		if (cDNodes != null) {
			Iterator i = cDNodes.keySet().iterator();
			Iterator j;
			DefaultMutableTreeNode dNode;
			List dNodes, images;
			Integer datasetID;
			//First update the images in list of expanded datasets.
			updateImagesInDataset(is);
			//Then update the tree.
			while (i.hasNext()) {
				datasetID = (Integer) i.next();
				images = (List) imagesInDataset.get(datasetID);
				dNodes = (List) cDNodes.get(datasetID);
				j = dNodes.iterator();
				while (j.hasNext()) {
					dNode = (DefaultMutableTreeNode) j.next();
					dNode.removeAllChildren();
					addImagesToDataset(images, dNode); 
					treeModel.reload(dNode);
				}
			}
		}
	}
	
	/** 
	 * Update the map of expanded datasets.
	 * 
	 * @param retVal	ImageSummary object.
	 */
	private void updateImagesInDataset(ImageSummary retVal)
	{
		Iterator i = imagesInDataset.keySet().iterator();
		Iterator j;
		List images;
		ImageSummary is;
		while (i.hasNext()) {
			images = (List) imagesInDataset.get(i.next());
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
	 * Add a new project to the Tree. 
	 * 
	 * @param ps	Project summary to display.
	 */
	void addNewProjectToTree(ProjectSummary ps)
	{
		DefaultTreeModel treeModel = (DefaultTreeModel) view.tree.getModel();
		DefaultMutableTreeNode pNode = new DefaultMutableTreeNode(ps);
		treeModel.insertNodeInto(pNode, root, root.getChildCount());
		pNodes.put(new Integer(ps.getID()), pNode);
		List datasets = ps.getDatasets();
		if (datasets != null) {
			Iterator i = datasets.iterator();
			DefaultMutableTreeNode dNode;
			DatasetSummary ds;
			while (i.hasNext()) {
				ds = (DatasetSummary) i.next();
				dNode = new DefaultMutableTreeNode(ds);
				dNode.add(new DefaultMutableTreeNode(LOADING));
				pNode.add(dNode);
			}
		}
		setNodeVisible(pNode, root);							
	}
	
	/**
	 * Add a new dataset to the tree.
	 * 
	 * @param projects		list of projects to which the dataset id added.
	 */
	void addNewDatasetToTree(List projects) 
	{
		DefaultTreeModel treeModel = (DefaultTreeModel) view.tree.getModel();
		DefaultMutableTreeNode pNode;													
		List pSummaries = agentCtrl.getUserProjects();
		if (pSummaries != null) {
			Iterator j = pSummaries.iterator();
			ProjectSummary ps;
            Integer projectID;
			while (j.hasNext()) {
				ps = (ProjectSummary) j.next();
				projectID = new Integer(ps.getID());
				pNode = (DefaultMutableTreeNode) pNodes.get(projectID);
				pNode.removeAllChildren();
				if (projects.indexOf(ps) != -1)
					addDatasets(ps.getDatasets(), pNode, true);
				else addDatasets(ps.getDatasets(), pNode, false);
				treeModel.reload(pNode);
			}
		}
	}

	/**
	 * Create and add an image's node to the dataset node.
	 * 
	 * @param images	List of image summary object.
	 * @param dNode		The node for the dataset
	 */
	private void addImagesToDataset(List images, DefaultMutableTreeNode dNode)
	{
		Iterator i = images.iterator();
		ImageSummary is;
		DefaultMutableTreeNode iNode;
		DefaultTreeModel treeModel = (DefaultTreeModel) view.tree.getModel();
		while (i.hasNext()) {
			is = (ImageSummary) i.next();
			iNode = new DefaultMutableTreeNode(is);
			treeModel.insertNodeInto(iNode, dNode, dNode.getChildCount());
		}
	}
	
	/**
	 * Make sure the user can see the new node.
	 * 
	 * @param child		Node to display.
	 * @param parent	Parent's node of the child.
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
	   		DataObject target = view.getCurrentOMEObject();
	   		if (target != null) {
				if (e.isPopupTrigger()) {
					DataManagerUIF presentation = 
								agentCtrl.getAbstraction().getPresentation();
					TreePopupMenu popup = presentation.getPopupMenu();
					popup.setTarget(target); 
					popup.show(view.tree, e.getX(), e.getY());
				} else {
					if (e.getClickCount() == 2)
						agentCtrl.showProperties(target);
				}
	   		} else { //click on the root node.
				if (e.getClickCount() == 2 && !treeLoaded) rebuildTree();
				else if (treeLoaded && e.isPopupTrigger())
				{
					DataManagerUIF presentation = 
					agentCtrl.getAbstraction().getPresentation();
					TreePopupMenu popup = presentation.getPopupMenu();
					popup.setTarget(null);  
					popup.show(view.tree, e.getX(), e.getY());
				}
	   		}
		}
	}
	
	/**
	 * Handle the tree expansion event.
	 * 
	 * @param e				event	
	 * @param isExpanding	true if a treeExpanded event has been fired,
	 * 						false otherwise.
	 */
	private void onNodeNavigation(TreeExpansionEvent e, boolean isExpanding)
	{
		TreePath path = e.getPath();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) 
										path.getLastPathComponent();
		Object usrObject = node.getUserObject();
		//dataset summary node
		if (usrObject instanceof DatasetSummary) {
			DatasetSummary ds = (DatasetSummary) usrObject;
			datasetNodeNavigation(ds, node, isExpanding);
		} else {
			if (node.equals(root) && !treeLoaded && isExpanding) rebuildTree();
		}
	}

	/** Build the tree model to represent the project-dataset hierarchy. */
	void rebuildTree()
	{
		List pSummaries = agentCtrl.getUserProjects();
		DefaultTreeModel treeModel = (DefaultTreeModel) view.tree.getModel();
		root.removeAllChildren();
		if (pSummaries != null) {
			Iterator i = pSummaries.iterator();
			ProjectSummary ps;
			DefaultMutableTreeNode pNode;
			while (i.hasNext()) {
				ps = (ProjectSummary) i.next();
				pNode = new DefaultMutableTreeNode(ps);
				treeModel.insertNodeInto(pNode, root, root.getChildCount());
				pNodes.put(new Integer(ps.getID()), pNode);
				addDatasetsToProject(ps, pNode, treeModel);	
			}
			treeModel.reload(root);
			treeLoaded = true;	
		} else {
			DefaultMutableTreeNode childNode = 
										new DefaultMutableTreeNode("");
			treeModel.insertNodeInto(childNode, root, root.getChildCount());
			treeModel.reload(root);
			view.tree.collapsePath(new TreePath(root.getPath()));
		}	
	}
	
	/** 
	 * Handle the navigation when the node which fired the treeExpansion
	 * event is a dataset summary node.
	 * 
	 * @param ds			DatasetSummary object.
	 * @param node			Node which fired event.
	 * @param isExpanding	True is the node is expanded false otherwise.
	 */
	private void datasetNodeNavigation(DatasetSummary ds, 
										DefaultMutableTreeNode node, 
										boolean isExpanding)
	{
		DefaultTreeModel treeModel = (DefaultTreeModel) view.tree.getModel();
		Integer datasetID = new Integer(ds.getID());
		node.removeAllChildren();
		if (isExpanding) {
			List list = agentCtrl.getAbstraction().getImages(ds.getID());
			//TODO: loading will never be displayed b/c we are in the
			// same thread.
			if (list.size() != 0) {
				addNodesToDatasetMaps(datasetID, node, list);
				addImagesToDataset(list, node); 	
			} else {
				DefaultMutableTreeNode childNode = 
										new DefaultMutableTreeNode(EMPTY);
				treeModel.insertNodeInto(childNode, node, 
										node.getChildCount());						
			}
		} else {
			//removeNodesFromDatasetMaps(datasetID, node);
			DefaultMutableTreeNode childNode = 
					new DefaultMutableTreeNode(LOADING);
			treeModel.insertNodeInto(childNode, node, node.getChildCount());		
		}
		treeModel.reload(node);
	}
	
	/**
	 * Add the specified node to the dataset maps.
	 * @param datasetID		maps' key.
	 * @param node			node to add.
	 * @param images		list of images in the specified dataset.
	 */
	private void addNodesToDatasetMaps(Integer datasetID, 
										DefaultMutableTreeNode node,
										List images)
	{
		if (cDNodes == null) cDNodes = new TreeMap();
		if (imagesInDataset == null) imagesInDataset = new TreeMap();
		List lNodes = (List) cDNodes.get(datasetID);
		if (lNodes == null) lNodes = new ArrayList();
		lNodes.add(node);
		cDNodes.put(datasetID, lNodes);
		imagesInDataset.put(datasetID, images);
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
