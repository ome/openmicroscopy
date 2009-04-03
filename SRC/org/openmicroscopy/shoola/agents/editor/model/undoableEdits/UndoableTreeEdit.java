 /*
 * org.openmicroscopy.shoola.agents.editor.model.undoableEdits.UndoableEdit 
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
package org.openmicroscopy.shoola.agents.editor.model.undoableEdits;

//Java imports

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.undo.AbstractUndoableEdit;

//Third-party libraries

//Application-internal dependencies

/** 
 * The Abstract superclass of UndoableEdit instances used for 
 * editing the TreeModel.
 * Constructor takes and saves a reference to the JTree being edited.
 * This can be null, as long as setTree(JTree) is called before edit is used.
 * In either case, a reference to the TreeModel of the JTree is saved
 * for subclasses to use for editing the model. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public abstract class UndoableTreeEdit 
	extends AbstractUndoableEdit 
	implements TreeEdit
{
	
	/**
	 * The JTree being edited. If this is null, canDo() should return false 
	 */
	protected JTree 			tree;
	
	/**
	 * A reference to the TreeModel that is being edited. 
	 * DefaultTreeModel methods such as insertNodeInto(node, parent, index) are
	 * delegated to.
	 */
	protected DefaultTreeModel 	treeModel;
	
	/**
	 * Creates an instance of this class.
	 * The JTree to edit can be specified here, or can be set later 
	 * using setTree(JTree)
	 * 
	 * @param tree		The JTree to edit
	 */
	public UndoableTreeEdit(JTree tree) 
	{
		setTree(tree);
	}
	
	/**
	 * Sets a reference to the JTree to edit. 
	 * Also stores a reference to the DefaultTreeModel of JTree.
	 * 
	 * @param tree		The JTree that subclasses will edit
	 */
	public void setTree(JTree tree) 
	{
		this.tree = tree;
		if (tree != null) {
			treeModel = (DefaultTreeModel)tree.getModel();
		}
	}
	
	/**
	 * This performs the first occurrence of the Edit, based on
	 * the selected paths of the JTree 
	 */
	public abstract void doEdit();
	
	
}
