/*
 * org.openmicroscopy.shoola.agents.treeviewer.browser.NodeSelectionVisitor 
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
package org.openmicroscopy.shoola.agents.treeviewer.browser;


//Java imports

//Third-party libraries

//Application-internal dependencies
import pojos.DataObject;

/** 
 * Finds the {@link TreeImageDisplay} hosting the selected 
 * <code>DataObject</code>.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class NodeSelectionVisitor 
	implements TreeImageDisplayVisitor
{

	/** The parent of the selected object if any. */
	private Object 				parent;
	
	/** The selected object. */
	private DataObject 			selected;
	
	/** The node corresponding to the selected object. */
	private TreeImageDisplay	selectedNode;
	
	/**
	 * Finds the node hosting the selected <code>DataObject</code>.
	 * 
	 * @param node The node to check.
	 */
	private void findNode(TreeImageDisplay node)
	{
		if (node == null) return;
		if (parent == null) {
			Object uo = node.getUserObject();
			if (selected.getClass().equals(uo.getClass()) &&
				selected.getId() == node.getUserObjectId()) {
				selectedNode = node;
			}
		} else {
			Object uo = node.getUserObject();
			
			if (selected.getClass().equals(uo.getClass()) &&
				selected.getId() == node.getUserObjectId()) {
				TreeImageDisplay pN = node.getParentDisplay();
				Object po = pN.getUserObject();
				if (po.getClass().equals(parent.getClass()))
					if (po instanceof DataObject 
						&& parent instanceof DataObject) {
						if (((DataObject) po).getId() == 
							((DataObject) parent).getId())
							selectedNode = node;
					}
			}
		}
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parent	The parent of the selected node.
	 * @param selected	The selected node.
	 */
	NodeSelectionVisitor(Object parent, DataObject selected)
	{
		if (selected == null)
			throw new IllegalArgumentException("No node selected.");
		this.parent = parent;
		this.selected = selected;
	}
	
	/**
	 * Returns the node corresponding to the selected object. 
	 * 
	 * @return See above.
	 */
	TreeImageDisplay getSelectedNode() { return selectedNode; }
	
	/**
	 * Retrieves the {@link TreeImageDisplay} corresponding to the 
	 * the selected node.
	 * @see TreeImageDisplayVisitor#visit(TreeImageSet)
	 */
	public void visit(TreeImageSet node) { findNode(node); }
	
	/** 
	 * Retrieves the {@link TreeImageDisplay} corresponding to the 
	 * the selected node.
	 * @see TreeImageDisplayVisitor#visit(TreeImageNode)
	 */
	public void visit(TreeImageNode node) { findNode(node); }
	
}
