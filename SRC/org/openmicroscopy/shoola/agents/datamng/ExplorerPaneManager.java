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
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.DatasetSummary;
import org.openmicroscopy.shoola.env.data.model.ImageData;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.env.data.model.ProjectSummary;

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
	
	
	private Map						cDNodes;
	private Map						imagesInDataset;
	
	/** store all the treeExpansion listeners in an array. */
	private TreeExpansionListener[] listeners;
	
	ExplorerPaneManager(ExplorerPane view, DataManagerCtrl agentCtrl)
	{
		this.view = view;
		this.agentCtrl = agentCtrl;
		cDNodes = new TreeMap();
		imagesInDataset = new TreeMap();
		initListeners();
	}
	
	/** 
	 * Builds the tree model to represent the project-dataset
	 * hierarchy.
	 *
	 * @return  A tree model containing the project-dataset hierarchy.
	 */
	DefaultMutableTreeNode getUserTreeModel()
	{
		root = new DefaultMutableTreeNode("My OME");
		DefaultMutableTreeNode pNode; 	
		List pSummaries = agentCtrl.getAbstraction().getUserProjects();
		DefaultTreeModel treeModel = (DefaultTreeModel) view.tree.getModel();
		if (pSummaries != null) {
			Iterator i = pSummaries.iterator();
			ProjectSummary ps;
			while (i.hasNext()) {
				ps = (ProjectSummary) i.next();
				pNode = new DefaultMutableTreeNode(ps);
				root.add(pNode);
				addDatasetsToProject(ps, pNode, treeModel);
			}
		}
		return root;
	}
	
	
	
	/** Update a project already displayed in the tree. */
	void updateProjectInTree()
	{
		DefaultTreeModel treeModel = (DefaultTreeModel) view.tree.getModel();
		DefaultMutableTreeNode pNode;
		root.removeAllChildren();
												
		List pSummaries = agentCtrl.getAbstraction().getUserProjects();
		if (pSummaries != null) {
			Iterator j = pSummaries.iterator();
			ProjectSummary ps;
			while (j.hasNext()) {
				ps = (ProjectSummary) j.next();
				pNode = new DefaultMutableTreeNode(ps);
				treeModel.insertNodeInto(pNode, root, root.getChildCount());
				setNodeVisible(pNode, root);
				addDatasetsToProject(ps, pNode, treeModel);
			}
		}
		treeModel.reload();
	}
	
	/** Update a dataset already displayed in the tree. */
	void updateDatasetInTree()
	{	
		DefaultTreeModel treeModel = (DefaultTreeModel) view.tree.getModel();
		DefaultMutableTreeNode pNode;
		root.removeAllChildren();
															
		List pSummaries = agentCtrl.getAbstraction().getUserProjects();
		if (pSummaries != null) {
			Iterator j = pSummaries.iterator();
			ProjectSummary ps;
			while (j.hasNext()) {
				ps = (ProjectSummary) j.next();
				pNode = new DefaultMutableTreeNode(ps);
				treeModel.insertNodeInto(pNode, root, root.getChildCount());
				setNodeVisible(pNode, root);
				addDatasetsToProject(ps, pNode, treeModel);
			}
		}
		treeModel.reload();
	}
	
	/** 
	 * Update image data in the Tree.
	 * If the image is displayed in several expanded datasets, the image data
	 * node will also be updated.
	 */
	void updateImageInTree(ImageData id)
	{
		DefaultTreeModel treeModel = (DefaultTreeModel) view.tree.getModel();
		Iterator i = cDNodes.keySet().iterator();
		Iterator j;
		DefaultMutableTreeNode dNode;
		List dNodes, images;
		Integer datasetID;
		//First update the images in list of expanded datasets.
		updateImagesInDataset(id);
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
	
	/** 
	 * Update the map of expanded datasets.
	 */
	private void updateImagesInDataset(ImageData id)
	{
		Iterator i = imagesInDataset.keySet().iterator();
		Iterator j;
		List images;
		ImageSummary is;
		while (i.hasNext()) {
			images = (List) imagesInDataset.get((Integer) i.next());
			j = images.iterator();
			while (j.hasNext()) {
				is = (ImageSummary) j.next();
				if (is.getID() == id.getID()) {
					is.setName(id.getName());
					break;
				}
			}
		}
	}
	
	/** 
	 * Add a project node to the Tree. 
	 * 
	 * @param ps	Project summary to display.
	 */
	void addNewProjectToTree(ProjectSummary ps)
	{
		DefaultTreeModel treeModel = (DefaultTreeModel) view.tree.getModel();
		
		DefaultMutableTreeNode pNode = new DefaultMutableTreeNode(ps);
		treeModel.insertNodeInto(pNode, root, pNode.getChildCount());
		
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
	 * Add a new dataset.
	 */
	void addNewDatasetToTree() 
	{
		DefaultTreeModel treeModel = (DefaultTreeModel) view.tree.getModel();
		DefaultMutableTreeNode pNode;
		root.removeAllChildren();														
		List pSummaries = agentCtrl.getAbstraction().getUserProjects();
		if (pSummaries != null) {
			Iterator j = pSummaries.iterator();
			ProjectSummary ps;
			while (j.hasNext()) {
				ps = (ProjectSummary) j.next();
				pNode = new DefaultMutableTreeNode(ps);
				treeModel.insertNodeInto(pNode, root, root.getChildCount());
				setNodeVisible(pNode, root);
				addDatasetsToProject(ps, pNode, treeModel);
				Object[] o = pNode.getUserObjectPath();
				System.out.println(o.length);
			}
		}
		treeModel.reload();
	}
	
	/** 
	 * Used to build the tree model.
	 * Makes a tree node for every dataset in <code>p</code> and adds 
	 * each of those nodes to the node representing <code>p</code>, that is, 
	 * <code>pNode</code>. 
	 *
	 * @param   p   	The project summary.
	 * @param   pNode   The node for project <code>p</code>.
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
	
	/**
	 * Create and add image's node to the dataset node.
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
			setNodeVisible(iNode, dNode);
		}
	}
	
	/**
	 * Make sure the user can see the new node.
	 * 
	 * @param	child	Node to display.
	 * @param	parent	Parent's node of the child.
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
	 * @param   e   The mouse event.
	 */
	private void onClick(MouseEvent e)
	{
		int selRow = view.tree.getRowForLocation(e.getX(), e.getY());
		if (selRow != -1) {
	   		view.tree.setSelectionRow(selRow);
	   		Object  target = view.getCurrentOMEObject();
			// remove tree expansion listener
			for (int i = 0; i < listeners.length; i++) 
				view.tree.removeTreeExpansionListener(listeners[i]);
	   		if (target != null) {
				if (e.isPopupTrigger()) {
				//TODO: pop up menu
				} else {
					if (e.getClickCount() == 2)    {
						agentCtrl.showProperties(target);
						for (int i = 0; i < listeners.length; i++) 
							view.tree.addTreeExpansionListener(listeners[i]);
						
					}	
				}
	   		}
		}
	}

	/**
	 * 
	 * @param e
	 * @param isExpanding
	 */
	private void onNodeNavigation(TreeExpansionEvent e, boolean isExpanding)
	{
		TreePath path = e.getPath();
		DefaultTreeModel treeModel = (DefaultTreeModel) view.tree.getModel();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) 
										path.getLastPathComponent();
		Object  usrObject = node.getUserObject();
		
		//dataset summary node
		if (usrObject instanceof DatasetSummary) {
			DatasetSummary ds = (DatasetSummary) usrObject;
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
				removeNodesFromDatasetMaps(datasetID, node);
				DefaultMutableTreeNode childNode = 
						new DefaultMutableTreeNode(LOADING);
				treeModel.insertNodeInto(childNode, node, node.getChildCount());		
			}
			treeModel.reload((TreeNode) node);
		}
	}

	/** Remove the specified node from the dataset maps. */
	private void removeNodesFromDatasetMaps(Integer datasetID,
											DefaultMutableTreeNode node)
	{
		List nodes = (List) cDNodes.get(datasetID);
		if (nodes != null) {
			nodes.remove(node);
			if (nodes.size() == 0) {
				imagesInDataset.remove(datasetID);
				cDNodes.remove(datasetID);
			} else cDNodes.put(datasetID, nodes);
		}						
	}
	
	/** Add the specified node to the dataset maps. */
	private void addNodesToDatasetMaps(Integer datasetID, 
										DefaultMutableTreeNode node,
										List images)
	{
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
			public void mousePressed(MouseEvent e) {
				onClick(e);
			}
			public void mouseReleased(MouseEvent e) {
				onClick(e);
			}
		});
		view.tree.addTreeExpansionListener(new TreeExpansionListener() {
			public void treeCollapsed(TreeExpansionEvent e) {
				onNodeNavigation(e, false);
			}
			public void treeExpanded(TreeExpansionEvent e) {
				onNodeNavigation(e, true);	
			}	
		});
		
		listeners = view.tree.getTreeExpansionListeners();
	}
	
}
