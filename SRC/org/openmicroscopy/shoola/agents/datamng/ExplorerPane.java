/*
 * org.openmicroscopy.shoola.agents.datamng.ExplorerPane
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
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.DataObject;
import org.openmicroscopy.shoola.env.data.model.DatasetSummary;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.env.data.model.ProjectSummary;

/** 
 * UI component to browse the whole project-dataset
 * hierarchy for the current user.
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
class ExplorerPane
	extends JScrollPane
{
	
	/** This UI component's controller and model. */
	private ExplorerPaneManager     manager;
	
	/** Reference to the Registry. */
	private Registry				registry;
	
	/** The tree used to represent the project-dataset hierarchy. */
	JTree       					tree;
	
	/** 
	 * Creates a new instance.
	 *
	 *@param    agentCtrl   The agent's control component.
	 */
	ExplorerPane(DataManagerCtrl agentCtrl, Registry registry)
	{
		this.registry = registry;
		tree = new JTree();
		manager = new ExplorerPaneManager(this, agentCtrl);
		DefaultMutableTreeNode r = manager.getUserTreeModel(
                agentCtrl.getUserLastName());
		DefaultTreeModel dtm = new DefaultTreeModel(r);
		tree.setModel(dtm);
		tree.setShowsRootHandles(true);
		tree.collapsePath(new TreePath(r.getPath()));
		buildGUI();
	}
	
	/** 
	 * Returns the last selected node on the tree only if that is a project, 
	 * dataset or image.
	 * If the above condition is not met, <code>null</code> is returned instead.
	 *
	 * @return  See above.
	 */
	DataObject getCurrentOMEObject()
	{
		DataObject target = null;
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)
											tree.getLastSelectedPathComponent();
		if (node != null) {
			Object  usrObj = node.getUserObject();
			if (usrObj instanceof ProjectSummary || 
				usrObj instanceof DatasetSummary ||
				usrObj instanceof ImageSummary)
				target = (DataObject) usrObj;
		}
		return target;
	}	
	
	/** Return the manager of this component. */
	ExplorerPaneManager getManager() { return manager; }
	
	/** Builds and lay out the GUI. */
	private void buildGUI()
	{
		tree.putClientProperty("JTree.lineStyle", "Angled");
		tree.setCellRenderer(new DataTreeCellRenderer(registry));
		tree.getSelectionModel().setSelectionMode(
									TreeSelectionModel.SINGLE_TREE_SELECTION);
		setViewportView(tree);
	}
		
}
