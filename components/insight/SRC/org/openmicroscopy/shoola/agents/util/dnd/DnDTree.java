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
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.SystemColor;
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
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageNode;

import com.sun.java.swing.SwingUtilities2;

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
	
	/** The default color.*/
	private static Color DEFAULT_COLOR = new Color(255, 255, 255, 0);
	
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
	
	/** The node selected as a target.*/
	private TreeNode dropTargetNode;
	
	/** The image representing the dragged object.*/
	private BufferedImage	imgGhost;
	
	/** The point, in the dragged image, the mouse clicked occurred.*/
	//private Point			ptOffset;
	
	/** 
	 * Creates a ghost image.
	 * 
	 * @param path The selected path.
	 * @param p The origin of the dragging.
	 */
	private void createGhostImage(Point p)
	{
		TreePath[] paths = getSelectionPaths();
		if (paths == null || paths.length == 0) return;
		Rectangle rect = new Rectangle();
		rect.x = Integer.MAX_VALUE;
		rect.y = Integer.MIN_VALUE;
		TreePath path;// = paths[0];
		Rectangle r;// = getPathBounds(path);
		int[] values = new int[paths.length];
		int y = 0;
		for (int i = 0; i < paths.length; i++) {
			values[i] = y;
			path = paths[i];
			r = getPathBounds(path);
			if (rect.width < r.width) rect.width = r.width;
			rect.height += r.height;
			rect.x = Math.min(rect.x, r.x);
			rect.y = Math.max(rect.y, r.y);
			y += r.height;
		}
		imgGhost = new BufferedImage((int) rect.getWidth(),
				(int) rect.getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);
		Graphics2D g2 = imgGhost.createGraphics();
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 0.5f));
		JLabel label;
		Icon icon = null;
		int offset = -1;
		int v;
		for (int i = 0; i < paths.length; i++) {
			path = paths[i];
			r = getPathBounds(path);
			label = (JLabel) getCellRenderer().getTreeCellRendererComponent(
					this, path.getLastPathComponent(), false, isExpanded(path),
					getModel().isLeaf(path.getLastPathComponent()), 0, false);
			//label.setSize((int) r.getWidth(), (int) r.getHeight());
			
			//label.paint(g2);
			icon = label.getIcon();
			if (icon != null) {
				icon.paintIcon(label, g2, 0, values[i]);
			}
			v = (icon == null) ? 0 : icon.getIconWidth();
			v += label.getIconTextGap();
			//label.setBounds(v, values[i], rect.width, r.height);
			//label.paint(g2);]
			g2.setColor(label.getForeground());
			g2.setFont(label.getFont());
			FontMetrics fm = g2.getFontMetrics();
			g2.drawString(label.getText(), v, values[i]+fm.getAscent()+1);
			if (offset < 0) {
				offset = (icon == null) ? 0 : icon.getIconWidth();
				offset += label.getIconTextGap();
			}
		}
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OVER, 
				0.5f));
		g2.setPaint(new GradientPaint(offset, 0, 
				SystemColor.controlShadow, getWidth(), 0, DEFAULT_COLOR));
		g2.fillRect(offset, 0, getWidth(), imgGhost.getHeight());
		g2.dispose();
		/*
		// Get the cell renderer (which is a JLabel) for the path being dragged
		JLabel lbl = (JLabel) getCellRenderer().getTreeCellRendererComponent(
		this, path.getLastPathComponent(), false, isExpanded(path),
		getModel().isLeaf(path.getLastPathComponent()), 0, false);
		
		lbl.setSize((int) r.getWidth(), (int) r.getHeight()); 
		// Get a buffered image of the selection for dragging a ghost image
		imgGhost = new BufferedImage((int) r.getWidth(), (int) r.getHeight(), 
				BufferedImage.TYPE_INT_ARGB_PRE);
		Graphics2D g2 = imgGhost.createGraphics();

		// Ask the cell renderer to paint itself into the BufferedImage
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 0.5f));
		lbl.paint(g2);

		Icon icon = lbl.getIcon();
		int nStartOfText = (icon == null) ? 0 : 
			icon.getIconWidth()+lbl.getIconTextGap();
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OVER, 
				0.5f));
		g2.setPaint(new GradientPaint(nStartOfText, 0, 
				SystemColor.controlShadow, getWidth(), 0, DEFAULT_COLOR));
		g2.fillRect(nStartOfText, 0, getWidth(), imgGhost.getHeight());
		g2.dispose();
		*/
	}
	
	/** Creates a new instance.*/
	public DnDTree()
	{
		super();
		setDragEnabled(true);
		//ptOffset = new Point();
		dropTargetNode = null;
		dragSource = new DragSource();
		DropTarget target = new DropTarget(this, this);
		target.setDefaultActions(DnDConstants.ACTION_COPY_OR_MOVE);
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
	public void dragDropEnd(DragSourceDropEvent dsde)
	{
		if (dsde.getDropSuccess() &&
				dsde.getDropAction() == DnDConstants.ACTION_MOVE)
		{
			//pathSource = null;
		}
	}

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
		Transferable transferable = dtde.getTransferable();
		if (!transferable.isDataFlavorSupported(localFlavor)) {
			dtde.rejectDrop();
			return;
		}
		Point dropPoint = dtde.getLocation();
		TreePath path = getPathForLocation(dropPoint.x, dropPoint.y);
		boolean dropped = false;
		try {
			dtde.acceptDrop(DnDConstants.ACTION_MOVE);
			Object droppedObject = transferable.getTransferData(localFlavor);
			TreeImageDisplay droppedNode = null;
			List<TreeImageDisplay> nodes = new ArrayList<TreeImageDisplay>();
			if (droppedObject instanceof List) {
				List l = (List) droppedObject;
				Iterator i = l.iterator();
				Object o;
				while (i.hasNext()) {
					o = i.next();
					if (o instanceof TreeImageDisplay) {
						nodes.add((TreeImageDisplay) o);
					}
				}
			} else if (droppedObject instanceof TreeImageDisplay) {
				nodes.add((TreeImageDisplay) droppedObject);
			}
			
			if (nodes.size() == 0) {
				dropped = true;
				dtde.dropComplete(dropped);
				return;
			}
			TreeImageDisplay parent;
			DefaultMutableTreeNode dropNode =
				(DefaultMutableTreeNode) path.getLastPathComponent();
			if (dropNode instanceof TreeImageDisplay) {
				parent = (TreeImageDisplay) dropNode;
				if (dropNode.isLeaf() && dropNode instanceof TreeImageNode) {
					parent = (TreeImageDisplay) dropNode.getParent();
				}
				ObjectToTransfer transfer = new ObjectToTransfer(parent, nodes);
				firePropertyChange(DRAGGED_PROPERTY, null, transfer);
			}
			dropped = true;
		} catch (Exception e) {}
		dtde.dropComplete(dropped);
		repaint();
	}
	
	/**
	 * Starts dragging the node.
	 * @see DragGestureListener#dragGestureRecognized(DragGestureEvent)
	 */
	public void dragGestureRecognized(DragGestureEvent e)
	{
		Point p = e.getDragOrigin();
		TreePath path = getPathForLocation(p.x, p.y);
		if (path == null) return;
		TreeNode draggedNode = (TreeNode) path.getLastPathComponent();
		if (draggedNode == null) return;
		createGhostImage(p);
		//setSelectionPath(path);
		//pathSource = path;
		TreePath[] paths = getSelectionPaths();
		List<TreeNode> nodes = new ArrayList<TreeNode>();
		for (int i = 0; i < paths.length; i++) {
			nodes.add((TreeNode) paths[i].getLastPathComponent());
		}
		Transferable trans = new TransferableNode(nodes);
		//dragSource.startDrag(e, getCursor(e.getDragAction()), trans, this);
		try {
			e.startDrag(null, imgGhost, new Point(5, 5), trans, this);
		} catch (Exception ex) {
			//already an on-going dragging action.
		}
	}

	/**
	 * Implemented as specified by {@link DropTargetListener} I/F but
	 * no-operation in our case.
	 * {@link DropTargetListener#dragExit(DropTargetDragEvent)}
	 */
	public void dropActionChanged(DropTargetDragEvent dtde) {}
}
