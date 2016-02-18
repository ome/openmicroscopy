/*
 * org.openmicroscopy.shoola.agents.measurement.view.ROITable 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2016 University of Dundee. All rights reserved.
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.Map.Entry;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.ToolTipManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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
import org.openmicroscopy.shoola.agents.util.SelectionWizard;
import org.openmicroscopy.shoola.agents.util.ui.EditorDialog;
import org.openmicroscopy.shoola.agents.util.ui.SelectionDialog;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys;
import org.openmicroscopy.shoola.util.roi.model.annotation.MeasurementAttributes;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.graphutils.ShapeType;
import org.openmicroscopy.shoola.util.ui.treetable.OMETreeTable;
import org.openmicroscopy.shoola.agents.measurement.IconManager;
import org.openmicroscopy.shoola.agents.measurement.MeasurementAgent;
import omero.gateway.model.DataObject;
import omero.gateway.model.FolderData;

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
	implements ROIActionController, PropertyChangeListener
{

	/** The root node of the tree. */
	private ROINode			root;
	
	/** Column names of the table. */
	private Vector<String>	columnNames;
    
	/** References to all ROINodes */
    private Collection<ROINode> nodes;
    
	/** The tree model. */
	private ROITableModel	model;
	
	/** The roi popup menu creation class .*/
	private ROIPopupMenu 	popupMenu;
	
	/** Reference to the object manager. */
	private ObjectManager   manager;

	/** Flag indicating to reset the component when loading locally.*/
	private boolean reset;
	
	/** The type of action currently performed */
	private CreationActionType action;
	
	/** Holds the previously used selection, used for resetting the selection */
	private int[] previousSelectionIndices;
	
	/**
	 * The type of objects selected
	 */
	enum SelectionType {
	    ROIS, SHAPES, FOLDERS, MIXED
	}
	
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
		this.setRowHeight(25);
		this.nodes = new ArrayList<ROINode>();
		for (int i = 0 ; i < model.getColumnCount() ; i++)
			getColumn(i).setResizable(true);
		
		setDefaultRenderer(ShapeType.class, new ShapeRenderer());
		setTreeCellRenderer(new ROITableCellRenderer());
		popupMenu = new ROIPopupMenu(this);
		reset = false;
		
		// make sure either shapes or folders can be selected, not both
        selectionModel.addListSelectionListener(new ListSelectionListener() {
            boolean active = true;

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!active)
                    return;

                if (getSelectionType(getSelectedObjects()) == SelectionType.MIXED
                        && previousSelectionIndices != null) {
                    active = false;
                    selectionModel.clearSelection();
                    for (int i : previousSelectionIndices)
                        selectionModel.addSelectionInterval(i, i);
                    active = true;
                } else
                    previousSelectionIndices = getSelectedRows();
            }
        });
	}
	
    /**
     * Determines which type of objects are selected
     * 
     * @param selection
     *            The objects
     * @return The {@link SelectionType}
     */
    SelectionType getSelectionType(Collection<Object> selection) {
        SelectionType result = null;
        for (Object obj : selection) {
            SelectionType tmp = null;
            if (obj instanceof ROI)
                tmp = SelectionType.ROIS;
            else if (obj instanceof ROIShape)
                tmp = SelectionType.SHAPES;
            else if (obj instanceof FolderData)
                tmp = SelectionType.FOLDERS;

            if (result == null) {
                result = tmp;
            } else {
                if (result != tmp) {
                    return SelectionType.MIXED;
                }
            }
        }
        return result;
    }
	
    /**
     * Invoked when the selection has changed
     * 
     * @param selection
     *            The selected Objects.
     */
    void onSelection(Collection<Object> selection) {
        popupMenu.setActionsEnabled(selection);

        if (popupMenu.isActionEnabled(CreationActionType.REMOVE_FROM_FOLDER)) {
            // disable 'remove from folder action' if rois aren't in any folders
            List<ROIShape> selectedObjects = getSelectedROIShapes();
            Map<Long, Object> inFolders = new HashMap<Long, Object>();
            for (ROIShape shape : selectedObjects) {
                for (FolderData f : shape.getROI().getFolders()) {
                    if (!inFolders.containsKey(f.getId()))
                        inFolders.put(f.getId(), f);
                }
            }
            popupMenu.enableAction(CreationActionType.REMOVE_FROM_FOLDER,
                    !inFolders.isEmpty());
        }
    }
	
	/** 
	 * Select the ROIShape in the TreeTable and move the view port of the 
	 * table to the shape selected. 
	 * @param shape
	 */
	void selectROIShape(ROIShape shape)
	{
		Collection<ROINode> nodes = findNodes(shape.getROI());
		for(ROINode node : nodes) {
    		expandROIRow(node);
    		ROINode child = node.findChild(shape);
    		int row = this.getRowForPath(child.getPath());
    		this.selectionModel.addSelectionInterval(row, row);
		}
	}
	
	/**
	 * Scroll to the selected ROIShape. 
	 * @param shape see above.
	 */
	void scrollToROIShape(ROIShape shape)
	{
	    Collection<ROINode> nodes = findNodes(shape.getROI());
	    ROINode child = null;
		for(ROINode node : nodes) {
    		expandROIRow(node);
    		child = node.findChild(shape);
		}
		if(child!=null)
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
		this.nodes.clear();
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
		if (node.isROINode())
		{
			ROI roi = (ROI) node.getUserObject();
			expandNode = node;
			if (roi.isVisible() && !expandNode.isExpanded())
				expandROIRow(expandNode);
		}
		else if (node.isShapeNode())
		{
			expandNode = (ROINode) node.getParent();
			ROIShape roiShape = (ROIShape) node.getUserObject();
			if (roiShape.getROI().isVisible() && !expandNode.isExpanded())
				expandROIRow(expandNode);
		}
		
		repaint();
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
	    // store the expanded state of the nodes
	    Set<String> expandedNodeIds = new HashSet<String>();
        Collection<ROINode> tmp = new ArrayList<ROINode>();
        root.getAllDecendants(tmp);
        for (ROINode n : tmp) {
            if (n.isExpanded()) {
                Object uo = n.getUserObject();
                if (uo != null) {
                    if (uo instanceof ROI)
                        expandedNodeIds.add("ROI_"+((ROI) uo).getID());
                    else if (uo instanceof ROIShape)
                        expandedNodeIds.add("Shape_"+((ROIShape) uo).getID());
                    else if (uo instanceof FolderData)
                        expandedNodeIds.add("Folder_"+((FolderData) uo).getId());
                }
            }
        }
        
        // rebuild the nodes
		ROINode parent = null;
		int childCount;
		ROINode roiShapeNode;
		ROINode newNode;
		int index;
		for (ROIShape shape : shapeList)
		{
		    if(shape.getROI().getFolders().isEmpty()) {
		        // find the ROI node
		        Collection<ROINode> nodes = findNodes(shape.getROI());
	            if (nodes.isEmpty())
	            {
	                parent = new ROINode(shape.getROI());
	                parent.setExpanded(true);
	                this.nodes.add(parent);
	                childCount = root.getChildCount();
	                root.insert(parent, childCount);
	            }
	            else
	                parent = nodes.iterator().next();
	            
	            // get the shape node, replace if it is exists
	            // or just add new node if it does not exists yet.
	            roiShapeNode = parent.findChild(shape.getCoord3D());
	            newNode = new ROINode(shape);
	            newNode.setExpanded(true);
	            this.nodes.add(newNode);
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
		    
		    else {
		        // if the ROI is organized in folders, we have to do the
		        // same like above but for each folder.
		        Collection<ROINode> nodes = findNodes(shape.getROI());
		        if (nodes.isEmpty())
                {
		            Collection<ROINode> folders = findFolders(shape.getROI());
		            for (ROINode folder : folders) {
                        parent = new ROINode(shape.getROI());
                        parent.setExpanded(true);
                        nodes.add(parent);
                        this.nodes.add(parent);
                        childCount = folder.getChildCount();
                        folder.insert(parent, childCount);
		            }
                }
		        
                Iterator<ROINode> it = nodes.iterator();
                while(it.hasNext()) {
                    parent = it.next();
		            roiShapeNode = parent.findChild(shape.getCoord3D());
		            newNode = new ROINode(shape);
		            newNode.setExpanded(true);
		            this.nodes.add(newNode);
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
		    }			
		}
		model = new ROITableModel(root, columnNames);
		this.setTreeTableModel(model);
		
		// restore the expanded state
        tmp.clear();
        root.getAllDecendants(tmp);
        for (ROINode n : tmp) {
            Object uo = n.getUserObject();
            if (uo != null) {
                String id = "";
                if (uo instanceof ROI)
                    id = "ROI_"+((ROI) uo).getID();
                else if (uo instanceof ROIShape)
                    id = "Shape_"+((ROIShape) uo).getID();
                else if (uo instanceof FolderData)
                    id = "Folder_"+((FolderData) uo).getId();

                if (expandedNodeIds.contains(id))
                    expandNode(n);
            }
        }
		
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
	    expandPath(parent.getPath());
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
		Collection<ROINode> nodes = findNodes(roi);
		for(ROINode node : nodes)
		    this.expandROIRow(node);
	}
	
	/**
	 * Removes the ROIShape from the table, and delete the ROI if the 
	 * ROIShape was the last ROIShape in the ROI.
	 * 
	 * @param shape see above.
	 */
	void removeROIShape(ROIShape shape)
	{
	    Collection<ROINode> nodes = findNodes(shape.getROI());
        for(ROINode node : nodes) {
    		ROINode child = node.findChild(shape);
    		node.remove(child);
    		if (node.getChildCount() == 0)
    			node.getParent().remove(node);
        }
		this.setTreeTableModel(new ROITableModel(root, columnNames));
	}

	/**
	 * Removes the ROI from the table.
	 * 
	 * @param roi see above.
	 */
	void removeROI(ROI roi)
	{
	    Collection<ROINode> nodes = findNodes(roi);
		for(ROINode node: nodes) 
		    node.getParent().remove(node);
		
		this.setTreeTableModel(new ROITableModel(root, columnNames));
	}
	
	/**
	 * Find the nodes representing an ROI
	 * @param roi The Roi
	 * @return See above
	 */
	Collection<ROINode> findNodes(ROI roi) {
	    Collection<ROINode> result = new ArrayList<ROINode>();
	    for(ROINode node: nodes) {
	        if(node.isROINode()) {
	            ROI nodeRoi = (ROI) node.getUserObject();
	            if(nodeRoi.getID() == roi.getID())
	                result.add(node);
	        }
	    }
	    return result;
	}
	
	/**
     * Finds the ROI Folder nodes of the ROI.
     * Folders which don't exist yet, will be created.
     * 
     * @param roi The ROI
     * @return The folder nodes this ROI is part of
     */
    Collection<ROINode> findFolders(ROI roi) {
        if(roi.getFolders().isEmpty())
            return Collections.EMPTY_LIST;
        
        Collection<ROINode> insertInto = new ArrayList<ROINode>();
        
        for (FolderData f : roi.getFolders()) {
            ROINode node = findFolderNode(nodes, f);
            if (node == null) {
                node = new ROINode(f);
                nodes.add(node);
                handleParentFolderNodes(node);
            }
            insertInto.add(node);
        }
        
        return insertInto;
    }
    
    private ROINode getFolderNode(FolderData f) {
        for (ROINode node : nodes) {
            if (node.isFolderNode()) {
                if (((FolderData) node.getUserObject()).getId() == f.getId())
                    return node;
            }
        }
        return null;
    }
    
    /**
     * Adds the node to the parent; will create the parent hierarchy
     * if it doesn't exist yet.
     * @param node The Node
     */
    void handleParentFolderNodes(ROINode node) {
        FolderData parentFolder = ((FolderData) node.getUserObject())
                .getParentFolder();
        if (parentFolder == null) {
            root.insert(node, 0);
            return;
        }

        ROINode parent = findFolderNode(nodes, parentFolder);
        if (parent == null) {
            parent = new ROINode(parentFolder);
            nodes.add(parent);
            parent.insert(node, 0);
            handleParentFolderNodes(parent);
        } else if (parent.findChild((FolderData) node.getUserObject()) == null) {
            parent.insert(node, 0);
        }
    }
    
    /** 
     * Find the {@link ROINode} for a certain {@link FolderData} within a
     * collection of {@link ROINode}s
     * 
     * @param nodes
     *            The collection to search through
     * @param folder
     *            The folder to look for
     * @return The ROINode representing the folder or <code>null</code> if it
     *         can't be found
     */
    private ROINode findFolderNode(Collection<ROINode> nodes, FolderData folder) {
        for (ROINode n : nodes) {
            Object obj = n.getUserObject();
            if (obj instanceof FolderData
                    && ((FolderData) obj).getId() == folder.getId())
                return n;
        }
        return null;
    }
    
    /**
     * Initializes the Folder nodes, independently from the ROIs
     * 
     * @param folder
     *            The folders
     */
    public void initFolders(Collection<FolderData> folders) {
        for (FolderData f : folders) {
            ROINode node = findFolderNode(nodes, f);
            if (node == null) {
                node = new ROINode(f);
                nodes.add(node);
                handleParentFolderNodes(node);
            }
        }
        model = new ROITableModel(root, columnNames);
        this.setTreeTableModel(model);
    }
    
	/**
	 * Invokes when a ROIShape has changed its properties. 
	 * 
	 * @param shape the roiShape which has to be updated.
	 */
	 void setROIAttributesChanged(ROIShape shape)
	{
	     
		Collection<ROINode> nodes = findNodes(shape.getROI());
		for(ROINode node : nodes) {
    		ROINode child = node.findChild(shape);
    		model.nodeUpdated(child);
		}
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
	Collection<Object> getSelectedObjects()
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
		FolderData folder;
		for (int i = 0 ; i < selectedRows.length ; i++)
		{	
			nodeObject = this.getNodeAtRow(selectedRows[i]).getUserObject();
			if (nodeObject instanceof ROIShape)
			{
				roiShape = (ROIShape) nodeObject;
				if (!roiMap.containsKey(roiShape.getID()))
					selectedList.add(roiShape);
			}
			
			else if (nodeObject instanceof FolderData)
            {
			    folder = (FolderData) nodeObject;
                selectedList.add(folder);
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
     * Get the selected Folders
     * @return see above.
     */
    List<FolderData> getSelectedFolders()
    {
        List<FolderData> result = new ArrayList<FolderData>();
        int [] selectedRows = this.getSelectedRows();
        for (int i = 0 ; i < selectedRows.length ; i++)
        {
            Object nodeObject = this.getNodeAtRow(selectedRows[i]).getUserObject();
            if (nodeObject instanceof FolderData)
            {
                result.add((FolderData)nodeObject);
            }
        }
        return result;
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
    public void propagateROI() {
        manager.showReadyMessage();
        ROI roi = null;
        for (int i : getSelectedRows()) {
            ROINode n = (ROINode) this.getNodeAtRow(i);
            Object obj = n.getUserObject();
            if (roi != null) {
                if (n.isROINode()) {
                    if (((ROI) obj).getID() != roi.getID()) {
                        manager.showMessage("Propagate: Only one ROI may be "
                                + "propagated at a time.");
                        return;
                    }
                } else if (n.isShapeNode()) {
                    if (((ROIShape) obj).getROI().getID() != roi.getID()) {
                        manager.showMessage("Propagate: Only one ROI may be "
                                + "propagated at a time.");
                        return;
                    }
                } else {
                    manager.showMessage("Propagate: No ROI selected.");
                    return;
                }
            } else {
                if (n.isROINode())
                    roi = (ROI) obj;
                else if (n.isShapeNode())
                    roi = ((ROIShape) obj).getROI();
                else {
                    manager.showMessage("Propagate: No ROI selected.");
                    return;
                }
            }
        }

        if (roi != null)
            manager.propagateROI(roi);
        else {
            manager.showMessage("Propagate: No ROI selected.");
            return;
        }
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
		    onSelection(getSelectedObjects());
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
		if (node.isROINode()) {
			ROI roi = (ROI) object;
			return AnnotationKeys.TEXT.get(roi);
		} else if (node.isShapeNode()) {
			ROIShape s = (ROIShape) object;
			return ""+s.getFigure().getAttribute(MeasurementAttributes.TEXT);
		}
		else if(node.isFolderNode()) {
		    FolderData f = (FolderData) object;
		    f.getFolderPathString();
		    return "Owner: "+f.getOwner().getUserName()+"; Permissions: "+f.getPermissions();
		}
		return "";
	}
    
	/** 
     * Loads the tags. 
     * 
     *  @see ROIActionController#loadTags()
     */
    public void loadTags() {
        manager.loadTags();
    }

    @Override
    public void addToFolder() {
        action = CreationActionType.ADD_TO_FOLDER;
        Collection<Object> tmp = new ArrayList<Object>();
        for(FolderData folder : manager.getFolders()) {
            ROINode folderNode = getFolderNode(folder);
            if(folderNode.isLeaf() || folderNode.containsROIs())
                tmp.add(folder);
        }
        SelectionWizard wiz = new SelectionWizard(null, tmp, FolderData.class, manager.canEdit(), MeasurementAgent.getUserDetails());
        wiz.setTitle("Add to ROI Folders", "Select the Folders to add the ROI(s) to", IconManager.getInstance().getIcon(IconManager.ROIFOLDER));
        wiz.addPropertyChangeListener(this);
        UIUtilities.centerAndShow(wiz);
    }

    @Override
    public void removeFromFolder() {
        action = CreationActionType.REMOVE_FROM_FOLDER;
        List<ROIShape> selectedObjects = getSelectedROIShapes();
        Map<Long, Object> inFolders = new HashMap<Long, Object>();
        for (ROIShape shape : selectedObjects) {
            for (FolderData f : shape.getROI().getFolders()) {
                if (!inFolders.containsKey(f.getId()))
                    inFolders.put(f.getId(), f);
            }
        }
        
        SelectionWizard wiz = new SelectionWizard(null, inFolders.values(),
                FolderData.class, MeasurementAgent.getUserDetails());
        wiz.setTitle("Remove from ROI Folders",
                "Select the Folders to remove the ROI(s) from", IconManager
                        .getInstance().getIcon(IconManager.ROIFOLDER));
        wiz.addPropertyChangeListener(this);
        UIUtilities.centerAndShow(wiz);
    }
    
    @Override
    public void createFolder() {
        action = CreationActionType.CREATE_FOLDER;
        DataObject obj = new FolderData();
        EditorDialog d = new EditorDialog((JFrame) null, obj, false);
        d.addPropertyChangeListener(this);
        UIUtilities.centerAndShow(d);
    }
    
    @Override
    public void deleteFolder() {
        action = CreationActionType.DELETE_FOLDER;
        List<FolderData> selection = getSelectedFolders();
        manager.deleteFolders(selection);
    }
    
    @Override
    public void editFolder() {
        action = CreationActionType.EDIT_FOLDER;
        
        List<FolderData> selection = getSelectedFolders();
        if(selection.size()==1) {
            DataObject obj = selection.get(0);
            EditorDialog d = new EditorDialog((JFrame) null, obj, false, EditorDialog.EDIT_TYPE);
            d.addPropertyChangeListener(this);
            UIUtilities.centerAndShow(d);
        }
    }
    
    @Override
    public void moveFolder() {
        action = CreationActionType.MOVE_FOLDER;

        // subnodes and direct parent nodes of the selected nodes have to 
        // be excluded from the available nodes
        Set<Long> excludeIds = new HashSet<Long>();
        for (FolderData f : getSelectedFolders()) {
            excludeIds.add(f.getId());
            if (f.getParentFolder() != null)
                excludeIds.add(f.getParentFolder().getId());
            ROINode fnode = getFolderNode(f);
            Collection<ROINode> subNodes = new ArrayList<ROINode>();
            fnode.getAllDecendants(subNodes);
            for (ROINode subNode : subNodes)
                if (subNode.isFolderNode())
                    excludeIds.add(((FolderData) subNode.getUserObject())
                            .getId());
        }

        Collection<DataObject> tmp = new ArrayList<DataObject>();
        for (FolderData folder : manager.getFolders()) {
            ROINode folderNode = getFolderNode(folder);
            if (!excludeIds.contains(folder.getId()) && folder.canLink()
                    && (folderNode.isLeaf() || folderNode.containsFolders()))
                tmp.add(folder);
        }

        SelectionDialog d = new SelectionDialog(tmp, "Destination Folder",
                "Move to selected Folder:", true);
        d.addPropertyChangeListener(this);
        UIUtilities.centerAndShow(d);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String name = evt.getPropertyName();
        if (SelectionWizard.SELECTED_ITEMS_PROPERTY.equals(name)) {

            List<ROIShape> selectedObjects = getSelectedROIShapes();
            Collection<FolderData> folders = null;

            Map m = (Map) evt.getNewValue();
            if (m == null || m.size() != 1)
                return;
            Set set = m.entrySet();
            Entry entry;
            Iterator i = set.iterator();
            Class type;
            while (i.hasNext()) {
                entry = (Entry) i.next();
                type = (Class) entry.getKey();
                if (FolderData.class.getName().equals(type.getName())) {
                    folders = (Collection<FolderData>) entry.getValue();
                    break;
                }
            }

            if (folders == null)
                return;

            if (action==CreationActionType.ADD_TO_FOLDER) {
                manager.addRoisToFolder(selectedObjects, folders);
            } else if(action==CreationActionType.REMOVE_FROM_FOLDER){
                manager.removeRoisFromFolder(selectedObjects, folders);
            }
        }
        
        if (EditorDialog.CREATE_NO_PARENT_PROPERTY.equals(name) ||
                EditorDialog.CREATE_PROPERTY.equals(name)) {
            FolderData parent = null;
            List<FolderData> sel = getSelectedFolders();
            if (sel.size() == 1)
                parent = sel.get(0);
            
            Collection<FolderData> toSave = new ArrayList<FolderData>();
            
            FolderData folder = (FolderData) evt.getNewValue();
            if(action == CreationActionType.CREATE_FOLDER && parent!=null) {
                folder.setParentFolder(parent.asFolder());
            }
            
            toSave.add(folder);
            
            manager.saveROIFolders(Collections.singleton(folder));
        } 
        
        if (SelectionDialog.OBJECT_SELECTION_PROPERTY.equals(name)) {
            FolderData folder = getSelectedFolders().get(0);
            FolderData target = (FolderData) evt.getNewValue();
            folder.setParentFolder(target.asFolder());
            manager.saveROIFolders(Collections.singleton(folder));
        }

        if (SelectionDialog.NONE_SELECTION_PROPERTY.equals(name)) {
            FolderData folder = getSelectedFolders().get(0);
            folder.setParentFolder(null);
            manager.saveROIFolders(Collections.singleton(folder));
        }
    }
    
    
}

