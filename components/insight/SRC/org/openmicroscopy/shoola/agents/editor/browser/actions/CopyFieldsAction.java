 /*
 * org.openmicroscopy.shoola.agents.editor.browser.actions.CopyFieldsAction 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.editor.browser.actions;

//Java imports

import java.awt.event.ActionEvent;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.EditorAgent;
import org.openmicroscopy.shoola.agents.editor.IconManager;
import org.openmicroscopy.shoola.agents.editor.browser.Browser;
import org.openmicroscopy.shoola.agents.editor.model.TreeModelMethods;
import org.openmicroscopy.shoola.agents.editor.model.undoableEdits.TreeEdit;
import org.openmicroscopy.shoola.agents.events.editor.CopyEvent;
import org.openmicroscopy.shoola.env.config.Registry;

/** 
 * This Action allows users to copy the currently selected fields to 
 * 'clipboard' for pasting elsewhere. 
 * Needs a reference to the JTree for obtaining the selected fields. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class CopyFieldsAction 
	extends BrowserAction 
	implements TreeSelectionListener,
	TreeEdit
{
	/** This is used as a source of selection paths.  */
	protected JTree 				treeUI;
	
	/**
	 * Implemented as specified by the {@link BrowserAction} abstract class. 
	 * Refreshes the state by calling {@link #onStateChange()}
	 * 
	 * @see BrowserAction#onStateChange()
	 */
	protected void onStateChange() {
		refreshState();
	}

	/**
	 * Refreshes the enabled state of this action, based on the state of the
	 * {@link #model} and the selection state of {@link #treeUI}
	 */
	private void refreshState() {
		
			if (treeUI != null) {
				setEnabled(treeUI.getSelectionCount() > 0);
			} else {
				setEnabled(false);
			}
		
	}

	/**
	 * Creates an instance of this class.
	 * setTree(JTree tree) needs to be called before this edit can be used.
	 * 
	 * @see	{#link BrowserAction}
	 * 
	 * @param model		Reference to the Model. Mustn't be <code>null</code>.
	 */
	public CopyFieldsAction(Browser model) {
		super(model);
		
		setName("Copy Selected Steps");
		setDescription("Copy the selected steps to the clipboard");
		setIcon(IconManager.COPY_ICON);  
	}
	
	/**
	 * This sets the JTree to be edited and used for getting selected paths. 
	 * This must be set before the Action becomes enabled and can be used
	 * for editing. 
	 * 
	 * @param tree
	 */
	public void setTree(JTree tree) 
	{
		if (tree == null) return;
		
		// If the JTree has previously been set, need to remove this
		// as a listener (don't want to listen to more than one tree)
		if (treeUI != null) {
			treeUI.removeTreeSelectionListener(this);
		}
		
		// Set the tree, and add this as a selectionListener
		treeUI = tree;
		treeUI.addTreeSelectionListener(this);
		
		//...and refresh the enabled status of this action. 
		refreshState();
	}

	/**
	 * This copies the selected nodes (makes a duplicate of each node, children
	 * etc) and adds it to a list, which is passed to the 
	 * {@link Browser#copySelectedFields(Object)} method. 
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		
		if (treeUI == null) return;
		
		TreePath[] paths = treeUI.getSelectionPaths();
		Object[] nodes = new Object[paths.length];
		
		// copy (duplicate) the selected nodes and add them to the list
		TreePath path;
		DefaultMutableTreeNode node;
		for (int i = 0; i < paths.length; i++) {
			path = paths[i];
			node = (DefaultMutableTreeNode)path.getLastPathComponent();
			nodes[i] = TreeModelMethods.duplicateNode(node);
		}
		
		// pass this list to the model. Event will be posted with this object
		Registry reg = EditorAgent.getRegistry();
		reg.getEventBus().post(new CopyEvent(nodes));
	}
	
	/**
	 * This method is called when the selection changes on the JTree.
	 * It refreshes the enabled status of this Action based on the
	 * currently selected paths of the JTree.
	 * 
	 * @see TreeSelectionListener#valueChanged(TreeSelectionEvent)
	 */
	public void valueChanged(TreeSelectionEvent e) { refreshState(); }

}