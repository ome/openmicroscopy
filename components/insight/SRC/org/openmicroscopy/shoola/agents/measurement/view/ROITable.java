/*
 * org.openmicroscopy.shoola.agents.measurement.view.ROITable 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.measurement.view;



//Java imports
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.Map.Entry;

import javax.swing.JPopupMenu;
import javax.swing.ToolTipManager;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreePath;

//Third-party libraries
import org.jdesktop.swingx.JXTreeTable;
import org.jhotdraw.draw.Figure;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.util.roimenu.ROIPopupMenu;
import org.openmicroscopy.shoola.agents.measurement.util.roitable.ROIActionController;
import org.openmicroscopy.shoola.agents.measurement.util.roitable.ROINode;
import org.openmicroscopy.shoola.agents.measurement.util.roitable.ROITableCellRenderer;
import org.openmicroscopy.shoola.agents.measurement.util.roitable.ROITableModel;
import org.openmicroscopy.shoola.agents.measurement.util.ui.ShapeRenderer;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys;
import org.openmicroscopy.shoola.util.roi.model.annotation.MeasurementAttributes;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;
import org.openmicroscopy.shoola.util.ui.graphutils.ShapeType;
import org.openmicroscopy.shoola.util.ui.treetable.OMETreeTable;

/**
 * The ROITable is the class extending the JXTreeTable, this shows the 
 * ROI as the parent object and the ROIShapes belonging to the ROI as the
 * child objects. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ROITable 
	extends OMETreeTable
	implements ROIActionController
{

	/** The root node of the tree. */
	private ROINode			root;
	
	/** Column names of the table. */
	private Vector<String>	columnNames;
	
	/** The map to relate ROI to ROINodes. */
	private Map<ROI, ROINode> ROIMap;
	
	/** The tree model. */
	private ROITableModel	model;
	
	/** The roi popup menu creation class .*/
	private ROIPopupMenu 	popupMenu;
	
	/** Reference to the object manager. */
	private ObjectManager   manager;

	/** Flag indicating to reset the component when loading locally.*/
	private boolean reset;
	
	/**
	 * Returns <code>true</code> if all the roishapes in the shapelist 
	 * have the same id, <code>false</code> otherwise.
	 *  
	 * @param shapeList The list to handle.
	 * @return See above.
	 */
	private boolean haveSameID(List<ROIShape> shapeList)
	{
		TreeMap<Long, ROIShape> shapeMap = new TreeMap<Long, ROIShape>();
		for (ROIShape shape : shapeList)
		{
			if (!shapeMap.containsKey(shape.getID()))
			{
				if (shapeMap.size() == 0)
					shapeMap.put(shape.getID(), shape);
				else
					return false;
			}
		}
		return true;
	}
	
	/**
	 * Returns the id that the shapes in the list contain, if they
	 * do not contain the same id return -1;
	 * 
	 * @param shapeList The list to handle.
	 * @return See above.
	 */
	private long getSameID(List<ROIShape> shapeList)
	{
		TreeMap<Long, ROIShape> shapeMap = new TreeMap<Long, ROIShape>();
		if (shapeList.size() == 0) return -1;
		for (ROIShape shape : shapeList)
		{
			if (!shapeMap.containsKey(shape.getID()))
			{
				if (shapeMap.size() == 0)
					shapeMap.put(shape.getID(), shape);
				else
					return -1;
			}
		}
		return shapeList.get(0).getID();
	}

	/**
	 * Are all the roishapes in the shapelist on separate planes. 
	 * 
	 * @param shapeList The list to handle.
	 * @return See above.
	 */
	private boolean onSeparatePlanes(List<ROIShape> shapeList)
	{
		TreeMap<Coord3D, ROIShape> 
		shapeMap = new TreeMap<Coord3D, ROIShape>(new Coord3D());
		for (ROIShape shape : shapeList)
		{
			if (shapeMap.containsKey(shape.getCoord3D()))
				return false;
			else
				shapeMap.put(shape.getCoord3D(), shape);
		}
		return true;
	}
	
	/**
	 * The constructor for the ROITable, taking the root node and
	 * column names as parameters.  
	 * 
	 * @param model the table model.
	 * @param columnNames the column names.
	 * @param manager Reference to the manager.
	 */
	ROITable(ROITableModel model, Vector columnNames, ObjectManager manager)
	{
		super(model);
		this.model = model;
		this.manager = manager;
		this.root = (ROINode) model.getRoot();
		this.columnNames = columnNames;
		ToolTipManager.sharedInstance().registerComponent(this);
		this.setAutoResizeMode(JXTreeTable.AUTO_RESIZE_ALL_COLUMNS);
		ROIMap = new HashMap<ROI, ROINode>();
		for (int i = 0 ; i < model.getColumnCount() ; i++)
			getColumn(i).setResizable(true);
		
		setDefaultRenderer(ShapeType.class, new ShapeRenderer());
		setTreeCellRenderer(new ROITableCellRenderer());
		popupMenu = new ROIPopupMenu(this);
		reset = false;
	}
	
	/** 
	 * Invokes when new figures are selected.
	 * 
	 * @param figures The selected figures.
	 */
	void onSelectedFigures(Collection<Figure> figures)
	{
		popupMenu.setActionsEnabled(figures);
	}
	
	/** 
	 * Select the ROIShape in the TreeTable and move the view port of the 
	 * table to the shape selected. 
	 * @param shape
	 */
	void selectROIShape(ROIShape shape)
	{
		ROINode parent = findParent(shape.getROI());
		if (parent == null)
			return;
		expandROIRow(parent);
		ROINode child = parent.findChild(shape);
		
		int row = this.getRowForPath(child.getPath());
		this.selectionModel.addSelectionInterval(row, row);
	}
	
	/**
	 * Scroll to the selected ROIShape. 
	 * @param shape see above.
	 */
	void scrollToROIShape(ROIShape shape)
	{
		ROINode parent = findParent(shape.getROI());
		if (parent == null)
			return;
		expandROIRow(parent);
		ROINode child = parent.findChild(shape);
		this.scrollPathToVisible(child.getPath());
	}

    /** 
     * Displays the menu at the specified location if not already visible.
     * 
     * @param x The x-coordinate of the mouse click.
     * @param y The y-coordinate of the mouse click.
     */
    void showROIManagementMenu(Component c, int x, int y)
    {
    	JPopupMenu menu = popupMenu.getPopupMenu();
    	if (menu.isVisible()) return;
    	menu.show(c, x, y);
    }
    
	/** Refreshes the data in the table.  */
	void refresh()
	{
		this.setTreeTableModel(new ROITableModel(root, columnNames));
	}
	
	/** Clears the table. */
	void clear()
	{
		int childCount = root.getChildCount();
		for (int i = 0 ; i < childCount ; i++ )
			root.remove(0);
		this.setTreeTableModel(new ROITableModel(root, columnNames));
		ROIMap = new HashMap<ROI, ROINode>();
		this.invalidate();
		this.repaint();
	}

	/**
	 * Set the value of the object at row and column to value object, 
	 * this will also expand the row of the object set, unless the ROI
	 * of the object is not visible it will then collapse the row.
	 * 
	 * @param obj see above.
	 * @param row see above.
	 * @param column see above.
	 */
	public void setValueAt(Object obj, int row, int column)
	{
		ROINode node = (ROINode) getNodeAtRow(row);
		super.setValueAt(obj, row, column);
		ROINode expandNode;
		if (node.getUserObject() instanceof ROI)
		{
			ROI roi = (ROI) node.getUserObject();
			expandNode = node;
			if (roi.isVisible())
				expandROIRow(expandNode);
			else
				collapseROIRow(expandNode);
		}
		else
		{
			expandNode = (ROINode) node.getParent();
			ROIShape roiShape = (ROIShape) node.getUserObject();
			if (roiShape.getROI().isVisible())
				expandROIRow(expandNode);
			else
				collapseROIRow(expandNode);
		}
	}
	
	/**
	 * Adds the ROIShape to the table, placing it under the ROI the ROIShape
	 * belongs to. This method will create the ROI if it does not exist 
	 * already. This method will also collapse all the ROI in the table
	 * and expand the ROI of the ROIShape, moving the viewport to 
	 * the ROIShape.
	 *  
	 * @param shape The shape to add.
	 */
	void addROIShape(ROIShape shape)
	{
		List<ROIShape> shapeList = new ArrayList<ROIShape>();
		shapeList.add(shape);
		addROIShapeList(shapeList);
	}

	/**
	 * Adds the ROIShapes in the shapeList to the table, placing it under the 
	 * ROI the ROIShape belongs to. This method will create the ROI if it does 
	 * not exist already. This method will also collapse all the ROI in the 
	 * table  and expand the ROI of the ROIShape, moving the viewport to 
	 * the ROIShape. 
	 * 
	 * @param shapeList The collection to add.
	 */
	void addROIShapeList(List<ROIShape> shapeList)
	{
		ROINode parent = null;
		int childCount;
		ROINode roiShapeNode;
		ROINode newNode;
		int index;
		for (ROIShape shape : shapeList)
		{
			parent = findParent(shape.getROI());
			if (parent == null)
			{
				parent = new ROINode(shape.getROI());
				parent.setExpanded(true);
				ROIMap.put(shape.getROI(), parent);
				childCount = root.getChildCount();
				root.insert(parent, childCount);
			}
			roiShapeNode = parent.findChild(shape.getCoord3D());
			newNode = new ROINode(shape);
			newNode.setExpanded(true);
			if (roiShapeNode != null)
			{
				index = parent.getIndex(roiShapeNode);
				parent.remove(shape.getCoord3D());
				parent.insert(newNode, index);
			}
			else
				parent.insert(newNode, 
						parent.getInsertionPoint(shape.getCoord3D()));
		}
		model = new ROITableModel(root, columnNames);
		this.setTreeTableModel(model);
		if (parent != null) expandROIRow(parent);
	}

	/** 
	 * Expands the row of the node.
	 * 
	 * @param node The selected node.
	 */
	void expandNode(ROINode node)
	{
		if (node.getUserObject() instanceof ROI)
			expandROIRow((ROI)node.getUserObject());
	}
	
	/**
	 * Returns the ROI Shape at row index.
	 * 
	 * @param index The index of the row.
	 * @return See above.
	 */
	ROIShape getROIShapeAtRow(int index)
	{
		TreePath path = this.getPathForRow(index);
		if (path == null) return null;
		ROINode node = (ROINode)path.getLastPathComponent();
		if (node.getUserObject() instanceof ROIShape)
			return (ROIShape) node.getUserObject();
		return null;
	}
	
	/**
	 * Returns the ROI at row index.
	 * 
	 * @param index see above.
	 * @return see above.
	 */
	ROI getROIAtRow(int index)
	{
		TreePath path = this.getPathForRow(index);
		ROINode node = (ROINode) path.getLastPathComponent();
		if (node.getUserObject() instanceof ROI)
			return (ROI)node.getUserObject();
		return null;
	}
	
	/** 
	 * Expands the row with node parent.
	 * 
	 * @param parent see above.
	 */
	void expandROIRow(ROINode parent)
	{
		int addedNodeIndex = root.getIndex(parent);
		parent.setExpanded(true);
		this.expandRow(addedNodeIndex);
		ROINode node;
		for (int i = 0; i < root.getChildCount(); i++)
		{
			node = (ROINode) root.getChildAt(i);
			if (node.isExpanded()) 
				expandPath(node.getPath());
		}
	}
	
	/** 
	 * Collapses the row with node parent.
	 * @param parent see above.
	 */
	void collapseROIRow(ROINode parent)
	{
		int addedNodeIndex = root.getIndex(parent);
		this.collapseRow(addedNodeIndex);
		parent.setExpanded(false);
		ROINode node;
		for (int i = 0; i < root.getChildCount(); i++)
		{
			node = (ROINode) root.getChildAt(i);
			if (node.isExpanded()) 
				expandROIRow((ROINode) node);
		}
	}
	
	/** 
	 * Expands the row with ROI.
	 * 
	 * @param roi see above.
	 */
	void expandROIRow(ROI roi)
	{
		ROINode selectedNode = findParent(roi);
		this.expandROIRow(selectedNode);
	//	this.scrollCellToVisible(selectedNodeIndex, 0);
	}
	
	/**
	 * Removes the ROIShape from the table, and delete the ROI if the 
	 * ROIShape was the last ROIShape in the ROI.
	 * 
	 * @param shape see above.
	 */
	void removeROIShape(ROIShape shape)
	{
		ROINode parent = findParent(shape.getROI());
		if (parent == null) return;
		ROINode child = parent.findChild(shape);
		parent.remove(child);
		if (parent.getChildCount() == 0)
			root.remove(parent);
		this.setTreeTableModel(new ROITableModel(root, columnNames));
	}

	/**
	 * Removes the ROI from the table.
	 * 
	 * @param roi see above.
	 */
	void removeROI(ROI roi)
	{
		ROINode roiNode = findParent(roi);
		root.remove(roiNode);
		this.setTreeTableModel(new ROITableModel(root, columnNames));
	}
	
	/**
	 * Finds the parent ROINode of the ROI.
	 * @param roi see above.
	 * @return see above.
	 */
	ROINode findParent(ROI roi)
	{
		if (ROIMap.containsKey(roi))
			return ROIMap.get(roi);
		return null;
	}
	
	/**
	 * Invokes when a ROIShape has changed its properties. 
	 * 
	 * @param shape the roiShape which has to be updated.
	 */
	 void setROIAttributesChanged(ROIShape shape)
	{
		ROINode parent = findParent(shape.getROI());
		ROINode child = parent.findChild(shape);
		model.nodeUpdated(child);
	}
	
	
	/** 
	 * Returns <code>true</code> if the column the shapeType column,
	 * <code>false</code> otherwise.
	 * 
	 * @param column see above
	 * @return see above 
	 */
	boolean isShapeTypeColumn(int column)
	{
		TableColumn col = this.getColumn(column);
		return (col.getModelIndex() == (ROITableModel.SHAPE_COLUMN+1));
	}
	
	/**
	 * Create a list of all the roi and roishapes selected in the table.
	 * This will only list an roi even if the roi and roishapes are selected.
	 * @return see above.
	 */
	List getSelectedObjects()
	{
		int [] selectedRows = this.getSelectedRows();
		TreeMap<Long, Object> roiMap = new TreeMap<Long, Object>(); 
		List selectedList = new ArrayList();
		Object nodeObject;
		ROI roi;
		for (int i = 0 ; i < selectedRows.length ; i++)
		{	
			nodeObject = getNodeAtRow(selectedRows[i]).getUserObject();
			if (nodeObject instanceof ROI)
			{
				roi = (ROI) nodeObject;
				roiMap.put(roi.getID(), roi);
				selectedList.add(roi);
			}
		}
		ROIShape roiShape;
		for (int i = 0 ; i < selectedRows.length ; i++)
		{	
			nodeObject = this.getNodeAtRow(selectedRows[i]).getUserObject();
			if (nodeObject instanceof ROIShape)
			{
				roiShape = (ROIShape) nodeObject;
				if (!roiMap.containsKey(roiShape.getID()))
					selectedList.add(roiShape);
			}
		}
		return selectedList;
	}

	/**
	 * Build the plane map from the selected object list. This builds a map
	 * of all the planes that have objects reside on them.
	 * @param objectList see above.
	 * @return see above.
	 */
	TreeMap<Coord3D, ROIShape> buildPlaneMap(ArrayList objectList)
	{
		TreeMap<Coord3D, ROIShape> planeMap = 
			new TreeMap<Coord3D, ROIShape>(new Coord3D());
		ROI roi;
		TreeMap<Coord3D, ROIShape> shapeMap;
		Iterator i;
		Coord3D coord;
		ROIShape shape;
		Entry entry;
		for (Object node : objectList)
		{
			if (node instanceof ROI)
			{
				roi = (ROI) node;
				shapeMap =  roi.getShapes();
				i = shapeMap.entrySet().iterator();
				while (i.hasNext())
				{
					entry = (Entry) i.next();
					coord = (Coord3D) entry.getKey();
					if (planeMap.containsKey(coord))
						return null;
					planeMap.put(coord, (ROIShape) entry.getValue());
				}
			} else if (node instanceof ROIShape)
			{
				shape = (ROIShape)node;
				if (planeMap.containsKey(shape.getCoord3D()))
					return null;
				else
					planeMap.put(shape.getCoord3D(), shape);
			}
		}
		return planeMap;
	}
	
	/**
	 * Returns the id of objects in the selected list. 
	 * 
	 * @param selectedObjects
	 * @return see above.
	 */
	List<Long> getIDList(List selectedObjects)
	{
		TreeMap<Long,ROI> idMap = new TreeMap<Long, ROI>();
		List<Long> idList = new ArrayList<Long>();
		ROI roi;
		for (Object node : selectedObjects)
		{
			if (node instanceof ROI) roi = (ROI) node;
			else roi = ((ROIShape) node).getROI();
			if (!idMap.containsKey(roi.getID()))
			{
				idMap.put(roi.getID(), roi);
				idList.add(roi.getID());
			}
		}
		return idList;
	}
	
	/**
	 * Returns the roishapes of the selected objects, this method will split ROI
	 * into their respective ROIshapes. 
	 * 
	 * @return see above.
	 */
	List<ROIShape> getSelectedROIShapes()
	{
		int [] selectedRows = this.getSelectedRows();
		TreeMap<Long, Object> roiMap = new TreeMap<Long, Object>(); 
		List<ROIShape> selectedList = new ArrayList<ROIShape>();
		Object nodeObject;
		ROI roi;
		Iterator<ROIShape> shapeIterator;
		for (int i = 0 ; i < selectedRows.length ; i++)
		{	
			nodeObject = this.getNodeAtRow(selectedRows[i]).getUserObject();
			if (nodeObject instanceof ROI)
			{
				roi = (ROI) nodeObject;
				if (roi.isClientSide()) reset = true;
				roiMap.put(roi.getID(), roi);
				shapeIterator = roi.getShapes().values().iterator();
				while (shapeIterator.hasNext())
					selectedList.add(shapeIterator.next());
			}
		}
		ROIShape roiShape;
		for (int i = 0 ; i < selectedRows.length ; i++)
		{	
			nodeObject = this.getNodeAtRow(selectedRows[i]).getUserObject();
			if (nodeObject instanceof ROIShape)
			{
				roiShape = (ROIShape) nodeObject;
				roi = roiShape.getROI();
				if (roi.getShapes().size() == 1) reset = true;
				if (!roiMap.containsKey(roiShape.getID()))
					selectedList.add(roiShape);
			}
		}

		if (selectedList.size() == 0) {//check model
			Collection<Figure> figures = manager.getSelectedFigures();
			if (figures != null && figures.size() > 0) {
				Iterator<Figure> i = figures.iterator();
				Figure figure;
				while (i.hasNext()) {
					figure = i.next();
					if (figure instanceof ROIFigure) {
						selectedList.add(((ROIFigure) figure).getROIShape());
					}
				}
			}
		}
		return selectedList;
	}
	
	/**
	 * Duplicates the ROI
	 * @see ROIActionController#duplicateROI()
	 */
	public void duplicateROI()
	{
		manager.showReadyMessage();
		List<ROIShape> selectedObjects = getSelectedROIShapes();
		if (onSeparatePlanes(selectedObjects) && haveSameID(selectedObjects))
			manager.duplicateROI(getSameID(selectedObjects), selectedObjects );
		else
			manager.showMessage("Duplicate: ROIs must be from the same ROI " +
					"and on separate planes.");
	}

	/** 
	 * Merges the ROI.
	 * @see ROIActionController#mergeROI()
	 */
	public void mergeROI()
	{
		manager.showReadyMessage();
		List<ROIShape> selectedObjects = getSelectedROIShapes();
		if (onSeparatePlanes(selectedObjects) && selectedObjects.size() > 1)
		{
			manager.mergeROI(getIDList(selectedObjects), selectedObjects);
		} else
			manager.showMessage("Merge: ROIs must be on separate " +
			"planes and must include more than one.");	
	}

	/** 
	 * Propagates the ROI.
	 * 
	 * @see ROIActionController#propagateROI()
	 */
	public void propagateROI()
	{
		manager.showReadyMessage();
		if (this.getSelectedRows().length != 1)
		{
			manager.showMessage("Propagate: Only one ROI may be " +
					"propagated at a time.");
			return;
		}
		ROINode node = (ROINode) this.getNodeAtRow(this.getSelectedRow());
		Object nodeObject = node.getUserObject(); 
		if (nodeObject instanceof ROI)
			manager.propagateROI(((ROI)nodeObject));
		if (nodeObject instanceof ROIShape)
			manager.propagateROI(((ROIShape)nodeObject).getROI());
	}

	/** 
	 * Splits the ROI.
	 * 
	 * @see ROIActionController#splitROI()
	 */
	public void splitROI()
	{
		manager.showReadyMessage();
		List<ROIShape> selectedObjects = getSelectedROIShapes();
		if (onSeparatePlanes(selectedObjects) && haveSameID(selectedObjects))
			manager.splitROI(getSameID(selectedObjects), selectedObjects);
		else
			manager.showMessage("Split: ROIs must be from the same ROI and " +
			"on separate planes.");
	}
	
	/** 
	 * Deletes the ROIs. 
	 * 
	 *  @see ROIActionController#deleteROI()
	 */
	public void deleteROI()
	{
		List<ROIShape> selectionList = getSelectedROIShapes();
		manager.deleteROIShapes(selectionList);
		if (reset) manager.reset();
		reset = false;
	}
	
	/** 
	 * Calculates statistics.
	 * @see ROIActionController#calculateStats()
	 */
	public void calculateStats()
	{
		manager.showReadyMessage();
		List<ROIShape> selectedObjects = getSelectedROIShapes();
		if (onSeparatePlanes(selectedObjects) && haveSameID(selectedObjects))
			manager.calculateStats(selectedObjects);
		else
			manager.showMessage("Calculate: ROIs must be from the same ROI " +
					"and on separate planes.");
	}
	
	/**
	 * Extending the mouse pressed event to show menu. 
	 * 
	 * @param e mouse event.
	 */
	protected void onMousePressed(MouseEvent e)
	{
		if (MeasurementViewerControl.isRightClick(e)) {
			Collection l = getSelectedObjects();
			if (l == null || l.size() == 0) return;
			Iterator i = l.iterator();
			Object o;
			ROI roi;
			ROIShape shape;
			List<Figure> list = new ArrayList<Figure>();
			while (i.hasNext()) {
				o =  i.next();
				if (o instanceof ROI) {
					roi = (ROI) o;
					list.addAll(roi.getAllFigures());
				} else if (o instanceof ROIShape) {
					shape = (ROIShape) o;
					list.add(shape.getFigure());
				}
			}
			onSelectedFigures(list);
			showROIManagementMenu(this, e.getX(), e.getY());
		}
	}

	/**
	 * Overridden to display the tool tip.
	 * @see JXTreeTable#getToolTipText(MouseEvent)
	 */
	public String getToolTipText(MouseEvent e)
	{
		TreePath path = getPathForLocation(e.getX(), e.getY());
		if (path == null) return "";
		int row = getRowForPath(path);
		if (row < 0) return "";
		ROINode node = (ROINode) getNodeAtRow(row);
		if (node == null) return "";
		Object object = node.getUserObject();
		if (object instanceof ROI) {
			ROI roi = (ROI) object;
			return AnnotationKeys.TEXT.get(roi);
		} else if (object instanceof ROIShape) {
			ROIShape s = (ROIShape) object;
			return ""+s.getFigure().getAttribute(MeasurementAttributes.TEXT);
		}
		return "";
	}

	/** 
     * Loads the tags. 
     * 
     *  @see ROIActionController#loadTag()
     */
    public void loadTags() {
        manager.loadTags();
    }
}

