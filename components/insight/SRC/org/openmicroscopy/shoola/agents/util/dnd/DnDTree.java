/*
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


import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.Toolkit;
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
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageNode;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageTimeSet;
import org.openmicroscopy.shoola.util.ui.IconManager;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;

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
	
	/**
	 * Flag indicating if the user currently logged in an administrator or not.
	 */
	private boolean administrator;
	
	/** The identifier of the user currently logged in.*/
	private long userID;
	
	/** Cursor indicating that the drop action is not permitted.*/
	private Cursor cursor;
	
	/** The default cursor.*/
	private Cursor defaultCursor;

	/** Flag indicating if the drop action is allowed or not.*/
	private boolean dropAllowed;
	
	/** The location of the drop.*/
	private int dropLocation;
	
	/**
	 * Auto-scrolls when dragging nodes.
	 * 
	 * @param tree The component to handle.
	 * @param p The location of the cursor.
	 */
	private void autoscroll(JTree tree, Point p)
	{
		Insets insets = getAutoscrollInsets();
		Rectangle outer = tree.getVisibleRect();
		Rectangle inner = new Rectangle(
				outer.x+insets.left,
				outer.y+insets.top,
				outer.width-(insets.left+insets.right),
				outer.height-(insets.top+insets.bottom));
		if (!inner.contains(p)) {
			Rectangle scrollRect = new Rectangle(p.x-insets.left,
					p.y-insets.top, insets.left+insets.right,
					insets.top+insets.bottom);
			tree.scrollRectToVisible(scrollRect);
		}
	}

	/**
	 * Returns the insets when auto-scrolling.
	 * 
	 * @return See above.
	 */
	private Insets getAutoscrollInsets()
	{
		int margin = 12;
		Rectangle outer = getBounds();
		Rectangle inner = getParent().getBounds();
		return new Insets(inner.y-outer.y+margin, inner.x-outer.x+margin,
		outer.height-inner.height-inner.y+outer.y+margin,
		outer.width-inner.width-inner.x+outer.x+margin);
	}
	
	/** 
	 * Sets the cursor depending on the selected node.
	 * 
	 * @param node The destination node.
	 * @param transferable The object hosting the nodes to move.
	 */
	private void handleMouseOver(TreeImageDisplay node,
			Transferable transferable)
	{
		TreeImageDisplay parent = node;
		if (node.isLeaf() && node instanceof TreeImageNode) {
			parent = (TreeImageDisplay) node.getParent();
		}
		Object ot = parent.getUserObject();
		/*
		if (ot instanceof GroupData && !administrator) {
			setCursor(createCursor());
			dropAllowed = false;
			return;
		}
		*/
		if (!canLink(ot) &&
				!(ot instanceof ExperimenterData || ot instanceof GroupData)) {
			dropAllowed = false;
			setCursor(createCursor());
			return;
		}
		//Now check that the src and target are compatible.
		try {
			Object droppedObject = transferable.getTransferData(localFlavor);
			List<TreeImageDisplay> nodes = new ArrayList<TreeImageDisplay>();
			if (droppedObject instanceof List) {
				List<Object> l = (List) droppedObject;
				Iterator<Object> i = l.iterator();
				Object o;
				while (i.hasNext()) {
					o = i.next();
					if (o instanceof TreeImageDisplay)
						nodes.add((TreeImageDisplay) o);
				}
			} else if (droppedObject instanceof TreeImageDisplay) {
				nodes.add((TreeImageDisplay) droppedObject);
			}
			if (nodes.size() == 0) return;
			//Check the first node
			TreeImageDisplay first = nodes.get(0);
			Object child = first.getUserObject();
			if (ot instanceof GroupData && child instanceof ExperimenterData &&
				!administrator) {
				setCursor(createCursor());
				dropAllowed = false;
				return;
			}
			List<TreeImageDisplay> list = new ArrayList<TreeImageDisplay>();
			Iterator<TreeImageDisplay> i = nodes.iterator();
			TreeImageDisplay n;
			
			Object os = null;
			int childCount = 0;
			while (i.hasNext()) {
				n = i.next();
				os = n.getUserObject();
				if (parent.contains(n)) {
					childCount++;
				} else {
					if (EditorUtil.isTransferable(ot, os, userID)) {
						if (ot instanceof GroupData) {
							if (os instanceof ExperimenterData &&
									administrator) list.add(n);
							else {
								if (canLink(os)) list.add(n);
							}
						} else {
							if (canLink(os)) list.add(n);
						}
					}	
				}
			}
			if (childCount == nodes.size() || list.size() == 0 ||
					(list.size() == 1 && parent == list.get(0))) {
				setCursor(createCursor());
				dropAllowed = false;
			}
		} catch (Exception e) {
			dropAllowed = false;
		}
	}
	
	/**
	 * Returns <code>true</code> if the user currently logged in is the owner
	 * of the object, <code>false</code> otherwise.
	 * 
	 * @param ho The object to handle.
	 * @return See above.
	 */
	private boolean canLink(Object ho)
	{
		if (ho instanceof TreeImageTimeSet) {
			TreeImageDisplay n = EditorUtil.getDataOwner((TreeImageDisplay) ho);
			if (n == null) return true;
			ExperimenterData exp = (ExperimenterData) n.getUserObject();
			return (exp.getId() == userID);
		}
		return EditorUtil.isUserOwner(ho, userID);
	}
	
	/**
	 * Returns the cursor corresponding to the dragged action.
	 * 
	 * @param action The action to handle.
	 * @return See above.
	 */
	private Cursor getCursor(int action)
	{
		/*
		switch (action) {
			case DnDConstants.ACTION_COPY:
				return DragSource.DefaultCopyDrop;
			case DnDConstants.ACTION_MOVE:
				default:
				return DragSource.DefaultMoveDrop;
		}
		*/
		return defaultCursor;
	}
	
	/** 
	 * Creates or recycles the image cursor.
	 * 
	 * @return See above.
	 */
	private Cursor createCursor()
	{
		if (cursor != null) return cursor;
		IconManager icons = IconManager.getInstance();
		Image image = icons.getImageIcon(IconManager.NO_ENTRY).getImage();
		Toolkit tk = Toolkit.getDefaultToolkit();
		cursor = tk.createCustomCursor(image , new Point(0, 0), "img");
		return cursor;
	}
	
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
		TreePath path;
		Rectangle r;
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
			icon = label.getIcon();
			if (icon != null) {
				icon.paintIcon(label, g2, 0, values[i]);
			}
			v = (icon == null) ? 0 : icon.getIconWidth();
			v += label.getIconTextGap();
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
	}
	
	/** 
	 * Creates a new instance.
	 * 
	 * @param userID The identifier of the user currently logged in.
	 * @param administrator Pass <code>true</code> to indicate that the user
	 *                      currently logged in an administrator,
	 *                      <code>false</code> otherwise.
	 */
	public DnDTree(long userID, boolean administrator)
	{
		super();
		setName("project tree");
		defaultCursor = getCursor();
		dropLocation = -1;
		reset(userID, administrator);
		setDragEnabled(true);
		dropTargetNode = null;
		dragSource = new DragSource();
		DropTarget target = new DropTarget(this, this);
		target.setDefaultActions(DnDConstants.ACTION_COPY_OR_MOVE);
		dragSource.createDefaultDragGestureRecognizer(this,
			DnDConstants.ACTION_MOVE, this);
		addFocusListener(new FocusAdapter() {
			
			public void focusLost(FocusEvent e) {
				setCursor(defaultCursor);
			}
		});
	}

	/** 
	 * Resets the values.
	 * 
	 * @param userID The identifier of the user currently logged in.
	 * @param administrator Pass <code>true</code> to indicate that the user
	 *                      currently logged in an administrator,
	 *                      <code>false</code> otherwise.
	 */
	public void reset(long userID, boolean administrator)
	{
		this.userID = userID;
		this.administrator = administrator;
	}
	
    /** Resets.*/
    public void reset() { dropTargetNode = null; }
    
	/**
	 * Returns the drop target node.
	 * 
	 * @return See above.
	 */
	public TreeNode getDropTargetNode() { return dropTargetNode; }

	/**
	 * Returns the row where the node was dropped.
	 * 
	 * @return See above.
	 */
	public int getRowDropLocation() { return dropLocation; }
	
	/**
	 * Returns <code>true</code> if the drop action is allowed,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isDropAllowed() { return dropAllowed; }
	
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
		JTree tree = (JTree) dtde.getDropTargetContext().getComponent();
		autoscroll(tree, dragPoint);
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
		dropLocation = getRowForPath(path);
		setCursor(defaultCursor);
		Transferable transferable;
		try {
			if (!dropAllowed) {
				dtde.rejectDrop();
				repaint();
				return;
			}
			transferable = dtde.getTransferable();
			if (!transferable.isDataFlavorSupported(localFlavor)) {
				dtde.rejectDrop();
				repaint();
				return;
			}
		} catch (Exception e) {
			return;
		}
		boolean dropped = false;
		
		try {
			dtde.acceptDrop(DnDConstants.ACTION_MOVE);
			Object droppedObject = transferable.getTransferData(localFlavor);
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
				repaint();
				return;
			}
			TreeImageDisplay parent = null;
			DefaultMutableTreeNode dropNode =
				(DefaultMutableTreeNode) path.getLastPathComponent();
			//if (dropNode instanceof TreeImageDisplay) {
				if (dropNode instanceof TreeImageDisplay)
					parent = (TreeImageDisplay) dropNode;
				if (dropNode.isLeaf() && dropNode instanceof TreeImageNode) {
					parent = (TreeImageDisplay) dropNode.getParent();
				}
				int action = DnDConstants.ACTION_MOVE;
				ObjectToTransfer transfer = new ObjectToTransfer(parent, nodes, 
						action);
				firePropertyChange(DRAGGED_PROPERTY, null, transfer);
			//}
			dropped = true;
		} catch (Exception e) {
			try {
				dtde.rejectDrop();
			} catch (Exception ex) {
				//ignore
			}
			repaint();
		}
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
		TreePath[] paths = getSelectionPaths();
		List<TreeNode> nodes = new ArrayList<TreeNode>();
		TreeNode n;
		if (paths != null) {
			for (int i = 0; i < paths.length; i++) {
				n = (TreeNode) paths[i].getLastPathComponent();
				if (n instanceof TreeImageDisplay)
					nodes.add(n);
			}
		}
		
		if (nodes.size() == 0) return;
		createGhostImage(p);
		Transferable trans = new TransferableNode(nodes);
		try {
			setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			if (DragSource.isDragImageSupported()) {
				e.startDrag(getCursor(e.getDragAction()), imgGhost,
						new Point(5, 5), trans, this);
			} else {
				e.startDrag(getCursor(e.getDragAction()), trans, this);
			}
				
		} catch (Exception ex) {
			//already an on-going dragging action.
		}
	}

	/**
	 * Implemented as specified by {@link DropTargetListener} I/F but
	 * no-operation in our case.
	 * {@link DropTargetListener#dropActionChanged(DropTargetDragEvent)}
	 */
	public void dropActionChanged(DropTargetDragEvent dtde) {}
	
	/**
	 * Implemented as specified by {@link DragSourceListener} I/F but
	 * no-operation in our case.
	 * {@link DragSourceListener#dragDropEnd(DragSourceDropEvent)}
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
	public void dragEnter(DragSourceDragEvent dsde)
	{
		setCursor(defaultCursor);
	}

	/**
	 * Implemented as specified by {@link DragSourceListener} I/F but
	 * no-operation in our case.
	 * {@link DragSourceListener#dragExit(DragSourceEvent)}
	 */
	public void dragExit(DragSourceEvent dse)
	{
		dse.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
	}

	/**
	 * Modifies the cursor if the elements can be dropped in the node mouse over.
	 * {@link DragSourceListener#dragOver(DragSourceDragEvent)}
	 */
	public void dragOver(DragSourceDragEvent dsde)
	{
		Point p = dsde.getLocation();
		SwingUtilities.convertPointFromScreen(p, this);
		TreePath path = getPathForLocation(p.x, p.y);
		setCursor(getCursor(dsde.getDropAction()));
		dropAllowed = true;
		if (path != null) {
			DefaultMutableTreeNode node =
				(DefaultMutableTreeNode) path.getLastPathComponent();
			Transferable trans = dsde.getDragSourceContext().getTransferable();
			if (node instanceof TreeImageDisplay) {
				handleMouseOver((TreeImageDisplay) node, trans);
			}
		}
	}
	
	/**
	 * Implemented as specified by {@link DragSourceListener} I/F but
	 * no-operation in our case.
	 * {@link DragSourceListener#dropActionChanged(DragSourceDragEvent)}
	 */
	public void dropActionChanged(DragSourceDragEvent dsde)
	{
		dsde.getDragSourceContext().setCursor(getCursor(dsde.getDropAction()));
	}

	/**
	 * Implemented as specified by {@link DropTargetListener} I/F but
	 * no-operation in our case.
	 * {@link DropTargetListener#dragEnter(DropTargetDragEvent)}
	 */
	public void dragEnter(DropTargetDragEvent dtde) {}

	/**
	 * Implemented as specified by {@link DropTargetListener} I/F but
	 * no-operation in our case.
	 * {@link DropTargetListener#dragExit(DropTargetEvent)}
	 */
	public void dragExit(DropTargetEvent dte) {}

}
