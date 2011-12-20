/*
 * org.openmicroscopy.shoola.agents.util.dnd.DnDTree 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.util.dnd;


//Java imports
import java.awt.Cursor;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;

/** 
 * Adds Drag and Drop facility to the tree.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class DnDTree 
	extends JTree
	implements DragSourceListener, DropTargetListener, DragGestureListener
{

	/** Bound property indicating that the D&D is completed.*/
	public static final String DRAGGED_PROPERTY = "dragged";
	
	/** The supported flavors.*/
	static DataFlavor[] supportedFlavors;
	
	/** The flavor.*/
	static DataFlavor localFlavor;
	
	static {
		try {
			localFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType);
		} catch (Exception e) {
			// TODO: handle exception
		}
		if (localFlavor != null) {
			supportedFlavors = new DataFlavor[1];
			supportedFlavors[0] = localFlavor;
		}
	}
	
	/** The dragging source.*/
	private DragSource dragSource;
	
	/** The target.*/
	private DropTarget dropTarget;
	
	/** The node selected as a target.*/
	private TreeNode dropTargetNode;
	
	/** The node currently dragged.*/
	private TreeNode draggedNode;
	
	/** Creates a new instance.*/
	public DnDTree()
	{
		super();
		dropTargetNode = null;
		draggedNode = null;
		dragSource = new DragSource();
		dropTarget = new DropTarget(this, this);
		dragSource.createDefaultDragGestureRecognizer(this,
			DnDConstants.ACTION_MOVE, this);
	}

	/**
	 * Returns the drop target node.
	 * 
	 * @return See above.
	 */
	public TreeNode getDropTargetNode() { return dropTargetNode; }
	
	/**
	 * Implemented as specified by {@link DragSourceListener} I/F but
	 * no-operation in our case.
	 * {@link DragSourceListener#dragDropEnd(DragSourceDragEvent)}
	 */
	public void dragDropEnd(DragSourceDropEvent dsde) {}

	/**
	 * Implemented as specified by {@link DragSourceListener} I/F but
	 * no-operation in our case.
	 * {@link DragSourceListener#dragEnter(DragSourceDragEvent)}
	 */
	public void dragEnter(DragSourceDragEvent dsde) {}

	/**
	 * Implemented as specified by {@link DragSourceListener} I/F but
	 * no-operation in our case.
	 * {@link DragSourceListener#dragExit(DropTargetEvent)}
	 */
	public void dragExit(DragSourceEvent dse) {}

	/**
	 * Implemented as specified by {@link DropTargetListener} I/F but
	 * no-operation in our case.
	 * {@link DragSourceListener#dragOver(DragSourceDragEvent)}
	 */
	public void dragOver(DragSourceDragEvent dsde) {}

	/**
	 * Implemented as specified by {@link DragSourceListener} I/F but
	 * no-operation in our case.
	 * {@link DragSourceListener#dragOver(DragSourceDragEvent)}
	 */
	public void dropActionChanged(DragSourceDragEvent dsde) {}

	/**
	 * Implemented as specified by {@link DropTargetListener} I/F but
	 * no-operation in our case.
	 * {@link DropTargetListener#dragOver(DropTargetDragEvent)}
	 */
	public void dragEnter(DropTargetDragEvent dtde) {}

	/**
	 * Implemented as specified by {@link DropTargetListener} I/F but
	 * no-operation in our case.
	 * {@link DropTargetListener#dragExit(DropTargetDragEvent)}
	 */
	public void dragExit(DropTargetEvent dte) {}

	/**
	 * Sets the target node.
	 * {@link DropTargetListener#dragOver(DropTargetDragEvent)}
	 */
	public void dragOver(DropTargetDragEvent dtde)
	{
		// figure out which cell it's over, no drag to self
		Point dragPoint = dtde.getLocation();
		TreePath path = getPathForLocation(dragPoint.x, dragPoint.y);
		if (path == null) dropTargetNode = null; 
		else dropTargetNode = (TreeNode) path.getLastPathComponent();
		repaint();
	}
	
	/**
	 * Drops the node and it to its destination.
	 * {@link DropTargetListener#dragOver(DropTargetDragEvent)}
	 */
	public void drop(DropTargetDropEvent dtde)
	{
		Point dropPoint = dtde.getLocation();
		TreePath path = getPathForLocation(dropPoint.x, dropPoint.y);
		DefaultTreeModel dtm = (DefaultTreeModel) getModel();
		boolean dropped = false;
		try {
			dtde.acceptDrop (DnDConstants.ACTION_MOVE);
			Object droppedObject = dtde.getTransferable().getTransferData(
					localFlavor);
			TreeImageDisplay droppedNode = null; 
			if (droppedObject instanceof TreeImageDisplay) {
				// remove from old location
				droppedNode = (TreeImageDisplay) droppedObject;
			}
			if (droppedNode == null) {
				dropped = true;
				dtde.dropComplete (dropped);
				return;
			}
			// insert into spec'd path. if dropped into a parent 
			// make it last child of that parent 
			TreeImageDisplay parent;
			DefaultMutableTreeNode dropNode =
				(DefaultMutableTreeNode) path.getLastPathComponent();
			if (dropNode instanceof TreeImageDisplay) {
				parent = (TreeImageDisplay) dropNode;
				if (dropNode.isLeaf()) {
					parent = (TreeImageDisplay) dropNode.getParent();
				}
				//First check that we can insert.
				/*
				if (canBeMoved(droppedNode, parent)) {
					dtm.removeNodeFromParent(droppedNode);
					dtm.insertNodeInto(droppedNode, parent,
							parent.getChildCount());
				}
				*/
				ObjectToTransfer transfer = new ObjectToTransfer(parent, 
						droppedNode);
				firePropertyChange(DRAGGED_PROPERTY, null, transfer);
			}
			
			dropped = true;
		} catch (Exception e) {
			//handle error.
			e.printStackTrace();
		}
		dtde.dropComplete (dropped);
		repaint();
	}

	/**
	 * Starts dragging the node.
	 * @see DragGestureListener#dragGestureRecognized(DragGestureEvent)
	 */
	public void dragGestureRecognized(DragGestureEvent dge)
	{
		Point clickPoint = dge.getDragOrigin();
		TreePath path = getPathForLocation(clickPoint.x, clickPoint.y);
		if (path == null) return;
		draggedNode = (TreeNode) path.getLastPathComponent();
		if (draggedNode == null) return;
		Transferable trans = new TransferableNode(draggedNode);
		dragSource.startDrag (dge, Cursor.getDefaultCursor(), trans, this);
	}

	/**
	 * Implemented as specified by {@link DropTargetListener} I/F but
	 * no-operation in our case.
	 * {@link DropTargetListener#dragExit(DropTargetDragEvent)}
	 */
	public void dropActionChanged(DropTargetDragEvent dtde) {}
}
