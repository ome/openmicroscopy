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
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DragSourceMotionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.Map.Entry;

import javax.swing.DropMode;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.basic.BasicTableUI;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreePath;

//Third-party libraries
import org.jdesktop.swingx.JXTreeTable;
import org.jhotdraw.draw.Figure;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.util.roimenu.ROIPopupMenu;
import org.openmicroscopy.shoola.agents.measurement.util.roitable.ROIActionController;
import org.openmicroscopy.shoola.agents.measurement.util.roitable.ROINode;
import org.openmicroscopy.shoola.agents.measurement.util.roitable.ROITableCellRenderer;
import org.openmicroscopy.shoola.agents.measurement.util.roitable.ROITableModel;
import org.openmicroscopy.shoola.agents.measurement.util.roitable.TableRowTransferHandler;
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
import omero.gateway.util.Pojos;

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
    private ListMultimap<String, ROINode> nodesMap;
    
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
	
	/** Only Folders which contain this String will be displayed */
	private String folderNameFilter= "";
	
	/** If set, only Folders with the given Ids will be displayed */
	private Collection<Long> onlyShowFolderIds = new HashSet<Long>();
	
	/** Overrides the filtering mechanisms */
	private boolean ignoreFilters = false;
	
    /**
     * Reference to folders which have been recently modified (ROIs
     * added/removed)
     */
    private Collection<FolderData> recentlyModifiedFolders = new ArrayList<FolderData>();
	
	/**
	 * The type of objects selected
	 */
	enum SelectionType {
	    ROIS, SHAPES, FOLDERS, MIXED
	}
	
	// DnD Scroll
	
	/** DnD autoscroll insets (this defines the scroll sensitive area) */
    private static final int AUTOSCROLL_INSET = 10;
    
	/** DnD autoscroll timer */
    private Timer timer;
	
	/** Track the last mouse drag position */
    private Point lastPosition;
    
	/** outer DnD autoscroll rectable */
    private Rectangle outer;

    /** inner DnD autoscroll rectable */
    private Rectangle inner;
    
	/**
     * Autoscroll to position
     */
    private void autoscroll(Point position) {
        System.out.println("autoscroll "+position);
        Scrollable s = (Scrollable) this;
        if (position.y < inner.y) {
            // scroll upwards
            int dy = s.getScrollableUnitIncrement(outer,
                    SwingConstants.VERTICAL, -1);
            Rectangle r = new Rectangle(inner.x, outer.y - dy, inner.width, dy);
            scrollRectToVisible(r);
        } else if (position.y > (inner.y + inner.height)) {
            // scroll downwards
            int dy = s.getScrollableUnitIncrement(outer,
                    SwingConstants.VERTICAL, 1);
            Rectangle r = new Rectangle(inner.x, outer.y + outer.height,
                    inner.width, dy);
            scrollRectToVisible(r);
        }

        if (position.x < inner.x) {
            // scroll left
            int dx = s.getScrollableUnitIncrement(outer,
                    SwingConstants.HORIZONTAL, -1);
            Rectangle r = new Rectangle(outer.x - dx, inner.y, dx, inner.height);
            scrollRectToVisible(r);
        } else if (position.x > (inner.x + inner.width)) {
            // scroll right
            int dx = s.getScrollableUnitIncrement(outer,
                    SwingConstants.HORIZONTAL, 1);
            Rectangle r = new Rectangle(outer.x + outer.width, inner.y, dx,
                    inner.height);
            scrollRectToVisible(r);
        }
    }
    
    /**
     * Updates inner/outer autoscroll regions
     */
    private void updateRegion() {
        // compute the outer
        Rectangle visible = getVisibleRect();
        outer.setBounds(visible.x, visible.y, visible.width, visible.height);

        // compute the insets
        Insets i = new Insets(0, 0, 0, 0);
        if (this instanceof Scrollable) {
            int minSize = 2 * AUTOSCROLL_INSET;

            if (visible.width >= minSize) {
                i.left = i.right = AUTOSCROLL_INSET;
            }

            if (visible.height >= minSize) {
                i.top = i.bottom = AUTOSCROLL_INSET;
            }
        }

        // set the inner from the insets
        inner.setBounds(visible.x + i.left, visible.y + i.top, visible.width
                - (i.left + i.right), visible.height - (i.top + i.bottom));
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
		this.nodesMap = ArrayListMultimap.create();
		for (int i = 0 ; i < model.getColumnCount() ; i++)
			getColumn(i).setResizable(true);
		
		setDefaultRenderer(ShapeType.class, new ShapeRenderer());
		setTreeCellRenderer(new ROITableCellRenderer());
		popupMenu = new ROIPopupMenu(this);
		reset = false;
		
	    setColumnSelectionAllowed(false);
	    setRowSelectionAllowed(true);
		
	    // enable DnD
	    setUI(new CustomTableUI());
		setDragEnabled(true);
        setDropMode(DropMode.ON);
        setTransferHandler(new TableRowTransferHandler(this));

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
        
        // DnD Scroll
        
        // after exportAsDrag is called (see CustomTableUI), events are no longer
        // propagated to MouseListeners, so we'd have to listen to the DragSource
        DragSource.getDefaultDragSource().addDragSourceMotionListener(
                new DragSourceMotionListener() {
                    @Override
                    public void dragMouseMoved(DragSourceDragEvent e) {
                        lastPosition = MouseInfo.getPointerInfo().getLocation();
                        if (!timer.isRunning())
                            timer.start();
                    }

                });
        DragSource.getDefaultDragSource().addDragSourceListener(
                new DragSourceListener() {

                    @Override
                    public void dropActionChanged(DragSourceDragEvent dsde) {
                        // ignore
                    }

                    @Override
                    public void dragOver(DragSourceDragEvent dsde) {
                        // ignore
                    }

                    @Override
                    public void dragExit(DragSourceEvent dse) {
                        // ignore
                    }

                    @Override
                    public void dragEnter(DragSourceDragEvent dsde) {
                        // ignore
                    }

                    @Override
                    public void dragDropEnd(DragSourceDropEvent dsde) {
                        if (timer.isRunning())
                            timer.stop();
                    }
                });
        
        outer = new Rectangle();
        inner = new Rectangle();

        Toolkit t = Toolkit.getDefaultToolkit();
        Integer prop;

        ActionListener al = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                updateRegion();
                Point componentPosition = new Point(lastPosition);
                SwingUtilities.convertPointFromScreen(componentPosition,
                        ROITable.this);
                if (outer.contains(componentPosition)
                        && !inner.contains(componentPosition)) {
                    autoscroll(componentPosition);
                }
            }
        };

        prop =  (Integer) t.getDesktopProperty("DnD.Autoscroll.interval");
        timer = new Timer(prop == null ? 100 : prop.intValue(), al);

        prop = (Integer) t.getDesktopProperty("DnD.Autoscroll.initialDelay");
        timer.setInitialDelay(prop == null ? 100 : prop.intValue());
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

    /**
     * Get the IDs of all expanded (leaf) folders
     * 
     * @return See above
     */
    Set<Long> getExpandedFolders() {
        Set<Long> result = new HashSet<Long>();
        for (ROINode node : nodesMap.values()) {
            if (node.isExpanded() && node.isFolderNode())
                result.add(((FolderData) node.getUserObject()).getId());
        }
        return result;
    }

    /**
     * Expand the Folder with the given IDs
     * 
     * @param ids
     *            The folder IDs
     */
    void expandFolders(Collection<Long> ids) {
        for (ROINode node : nodesMap.values()) {
            if (node.isFolderNode()
                    && ids.contains(((FolderData) node.getUserObject()).getId()))
                expandPath(node.getPath());
        }
    }
    
	/** Clears the table. */
	void clear()
	{
		int childCount = root.getChildCount();
		for (int i = 0 ; i < childCount ; i++ )
			root.remove(0);
		this.setTreeTableModel(new ROITableModel(root, columnNames));
		this.nodesMap.clear();
		this.recentlyModifiedFolders.clear();
		this.invalidate();
		this.repaint();
	}

    /**
     * Set the name filter
     * 
     * @param filter
     *            Only show folders which contain this String
     */
    public void setNameFilter(String filter) {
        this.folderNameFilter = filter.toLowerCase();
    }
    
    /**
     * Reference to the list of folder ids which should be displayed.
     * Can be empty (ie all folders shown).
     * @return See above.
     */
    public Collection<Long> getIDFilter() {
        return this.onlyShowFolderIds;
    }

    /**
     * Enable/Disable filtering
     * 
     * @param b
     *            Pass <code>false</code> to disable filtering
     */
    public void setIgnoreFilters(boolean b) {
        this.ignoreFilters = b;
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
		if (node == null)
		    return;
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
                expandedNodeIds.add(n.getUUID());
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
	                this.nodesMap.put(parent.getUUID(), parent);
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
	            this.nodesMap.put(newNode.getUUID(), newNode);
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
                        this.nodesMap.put(parent.getUUID(), parent);
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
		            this.nodesMap.put(newNode.getUUID(), newNode);
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
            if (expandedNodeIds.contains(n.getUUID()))
                expandNode(n);
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
    		if (child != null) {
        		node.remove(child);
        		if (node.getChildCount() == 0)
    			node.getParent().remove(node);
    		}
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
	    return nodesMap.get(getUUID(roi));
	}
	
    /**
     * Determines if the given Folder should be displayed or not, taking the
     * different filtering options into account
     * 
     * @param folder
     *            The Folder to check
     * @return See above.
     */
    private boolean displayFolder(FolderData folder) {
        return ignoreFilters
                || ((onlyShowFolderIds == null || onlyShowFolderIds
                        .contains(folder.getId())) && folder.getName()
                        .toLowerCase().contains(folderNameFilter));
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
            if (displayFolder(f)) {
                ROINode node = findFolderNode(f);
                if (node == null) {
                    node = new ROINode(f);
                    nodesMap.put(node.getUUID(), node);
                    handleParentFolderNodes(node);
                }
                insertInto.add(node);
            }
        }
        
        return insertInto;
    }
    
    /**
     * Adds the node to the parent; will create the parent hierarchy if it
     * doesn't exist yet.
     * 
     * @param node
     *            The Node
     */
    void handleParentFolderNodes(ROINode node) {
        FolderData parentFolder = ((FolderData) node.getUserObject())
                .getParentFolder();
        if (parentFolder == null) {
            root.insert(node,
                    root.getInsertionPoint((FolderData) node.getUserObject()));
            return;
        }

        ROINode parent = findFolderNode(parentFolder);
        if (parent == null) {
            parent = new ROINode(parentFolder);
            nodesMap.put(parent.getUUID(), parent);
            parent.insert(node, 0);
            handleParentFolderNodes(parent);
        } else if (parent.findChild((FolderData) node.getUserObject()) == null) {
            parent.insert(node,
                    parent.getInsertionPoint((FolderData) node.getUserObject()));
        }
    }
    
    private ROINode findFolderNode(FolderData folder) {
        Collection<ROINode> tmp = nodesMap.get(getUUID(folder));
        switch (tmp.size()) {
        case 0:
            return null;
        case 1:
            return tmp.iterator().next();
        default:
            throw new RuntimeException("Multiple ROINodes found for "
                    + getUUID(folder));
        }
    }

    /**
     * See {@link ROINode#getUUID()}
     * 
     * @param roi
     *            The ROI
     * @return See above
     */
    private String getUUID(ROI roi) {
        return "ROI_" + roi.getID();
    }

    /**
     * See {@link ROINode#getUUID()}
     * 
     * @param folder
     *            The Folder
     * @return See above
     */
    private String getUUID(FolderData folder) {
        return "FolderData_" + folder.getId();
    }
    
    /**
     * Initializes the Folder nodes, independently from the ROIs
     * 
     * @param folders
     *            The folders
     */
    public void initFolders(Collection<FolderData> folders) {
        for (FolderData f : folders) {
            if (displayFolder(f)) {
                ROINode node = findFolderNode(f);
                if (node == null) {
                    node = new ROINode(f);
                    nodesMap.put(node.getUUID(), node);
                    handleParentFolderNodes(node);
                }
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
            ROINode node = (ROINode) getNodeAtRow(selectedRows[i]);
            if (node == null)
                continue;
			nodeObject = node.getUserObject();
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
		    ROINode node = (ROINode) getNodeAtRow(selectedRows[i]);
		    if (node == null)
                continue;
			nodeObject = node.getUserObject();
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
		    ROINode node = (ROINode) this.getNodeAtRow(selectedRows[i]);
            if (node == null)
                continue;
            nodeObject = node.getUserObject();
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
		    ROINode node = (ROINode) this.getNodeAtRow(selectedRows[i]);
            if (node == null)
                continue;
            nodeObject = node.getUserObject();
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
            ROINode node = (ROINode) this.getNodeAtRow(selectedRows[i]);
            if (node == null)
                continue;
            Object nodeObject = node.getUserObject();
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
            if (n == null)
                continue;
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
     * @param e
     *            mouse event.
     */
    protected void onMousePressed(MouseEvent e) {
        if (MeasurementViewerControl.isRightClick(e)) {
            // consider right click also as selection click before handling the
            // popup menu
            int row = rowAtPoint(e.getPoint());
            ListSelectionModel m = getSelectionModel();
            if (!m.isSelectedIndex(row)) {
                if (e.isControlDown())
                    m.addSelectionInterval(row, row);
                else if (e.isShiftDown())
                    m.addSelectionInterval(m.getAnchorSelectionIndex(), row);
                else {
                    getSelectionModel().clearSelection();
                    m.addSelectionInterval(row, row);
                }
            }

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
		    return "";
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
            if(folder.canLink() && displayFolder(folder))    
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
            ROINode fnode = findFolderNode(f);
            Collection<ROINode> subNodes = new ArrayList<ROINode>();
            fnode.getAllDecendants(subNodes);
            for (ROINode subNode : subNodes)
                if (subNode.isFolderNode())
                    excludeIds.add(((FolderData) subNode.getUserObject())
                            .getId());
        }

        Collection<DataObject> tmp = new ArrayList<DataObject>();
        for (FolderData folder : manager.getFolders()) {
            if (!excludeIds.contains(folder.getId()) && folder.canLink())
                tmp.add(folder);
        }

        SelectionDialog d = new SelectionDialog(tmp, "Destination Folder",
                "Move to selected Folder:", true);
        d.addPropertyChangeListener(this);
        UIUtilities.centerAndShow(d);
    }
    
    /**
     * Get the Folders which have been modified recently (by an add to/remove
     * from folder action)
     */
    Collection<FolderData> getRecentlyModifiedFolders() {
        return recentlyModifiedFolders;
    }

    private void addRecentlyModifiedFolder(FolderData f) {
        this.recentlyModifiedFolders.add(f);
        this.onlyShowFolderIds.add(f.getId());
    }

    private void addRecentlyModifiedFolder(Collection<FolderData> f) {
        this.recentlyModifiedFolders.addAll(f);
        this.onlyShowFolderIds.addAll(Pojos.extractIds(f));
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

            addRecentlyModifiedFolder(folders);
            
            if (action==CreationActionType.ADD_TO_FOLDER) {
                manager.addRoisToFolder(selectedObjects, folders);
            } else if(action==CreationActionType.REMOVE_FROM_FOLDER){
                manager.removeRoisFromFolder(selectedObjects, folders);
            }
        }
        
        if (EditorDialog.CREATE_NO_PARENT_PROPERTY.equals(name)
                || EditorDialog.CREATE_PROPERTY.equals(name)
                && action == CreationActionType.CREATE_FOLDER) {
            FolderData parentFolder = null;
            List<FolderData> sel = getSelectedFolders();
            if (sel.size() == 1 && action != CreationActionType.EDIT_FOLDER)
                parentFolder = sel.get(0);

            FolderData folder = (FolderData) evt.getNewValue();
            if (parentFolder != null) {
                folder.setParentFolder(parentFolder.asFolder());
                addRecentlyModifiedFolder(parentFolder);
            }

            if (getSelectedROIShapes().isEmpty())
                manager.saveROIFolders(Collections.singleton(folder));
            else
                manager.addRoisToFolder(getSelectedROIShapes(),
                        Collections.singleton(folder));
        }
        
        if (SelectionDialog.OBJECT_SELECTION_PROPERTY.equals(name)) {
            FolderData folder = getSelectedFolders().get(0);
            FolderData target = (FolderData) evt.getNewValue();
            folder.setParentFolder(target.asFolder());
            addRecentlyModifiedFolder(target);
            manager.saveROIFolders(Collections.singleton(folder));
        }

        if (SelectionDialog.NONE_SELECTION_PROPERTY.equals(name)) {
            FolderData folder = getSelectedFolders().get(0);
            folder.setParentFolder(null);
            manager.saveROIFolders(Collections.singleton(folder));
        }
    }

    /**
     * Handles drag and drop actions
     * 
     * @param rows
     *            The dragged rows
     * @param destination
     *            The location where the rows had been dragged to
     */
    public void handleDragAndDrop(int[] rows, int destination) {
        if (manager.getState() != MeasurementViewer.READY)
            return;
        
        List<ROINode> objects = new ArrayList<ROINode>();
        for (int i = 0; i < rows.length; i++) {
            if (rows[i] == destination)
                return;
            
            ROINode n = (ROINode) getNodeAtRow(rows[i]);
            if (n != null)
                objects.add(n);
        }

        ROINode target = (ROINode) getNodeAtRow(destination);
        if (target == null) {
            if (objects.iterator().next().isFolderNode()) {
                Collection<FolderData> toSave = new ArrayList<FolderData>();
                for (ROINode n : dropChildFolderNodes(objects)) {
                    FolderData folder = (FolderData) n.getUserObject();
                    folder.setParentFolder(null);
                    toSave.add(folder);
                }
                manager.saveROIFolders(toSave);
            } else if (objects.iterator().next().isShapeNode()) {
                List<ROIShape> rois = new ArrayList<ROIShape>();
                for (ROINode n : objects) {
                    rois.add((ROIShape) n.getUserObject());
                }
                manager.moveROIsToFolder(rois, Collections.EMPTY_LIST);
            } else if (objects.iterator().next().isROINode()) {
                List<ROIShape> rois = new ArrayList<ROIShape>();
                for (ROINode n : objects) {
                    ROI r = (ROI) n.getUserObject();
                    rois.addAll(r.getShapes().values());
                }
                manager.moveROIsToFolder(rois, Collections.EMPTY_LIST);
            }
            return;
        }
        
        if (!target.isFolderNode())
            return;

        // remove the nodes from the selection which are already part of
        // the target node
        Iterator<ROINode> it = objects.iterator();
        while (it.hasNext()) {
            ROINode n = it.next();
            for (int i = 0; i < target.getChildCount(); i++) {
                if (n == target.getChildAt(i)) {
                    it.remove();
                    break;
                }
            }
        }
        
        FolderData targetFolder = (FolderData) target.getUserObject();

        if (objects.iterator().next().isFolderNode()) {

            List<FolderData> folders = new ArrayList<FolderData>(objects.size());
            for (ROINode n : dropChildFolderNodes(objects)) {
                FolderData f = (FolderData) n.getUserObject();
                f.setParentFolder(targetFolder.asFolder());
                folders.add(f);
            }
            addRecentlyModifiedFolder(targetFolder);
            manager.saveROIFolders(folders);
        } else if (objects.iterator().next().isShapeNode()) {
            List<ROIShape> rois = new ArrayList<ROIShape>();
            for (ROINode n : objects) {
                rois.add((ROIShape) n.getUserObject());
            }
            addRecentlyModifiedFolder(targetFolder);
            manager.moveROIsToFolder(rois,
                    Collections.singletonList(targetFolder));
        } else if (objects.iterator().next().isROINode()) {
            List<ROIShape> rois = new ArrayList<ROIShape>();
            for (ROINode n : objects) {
                ROI r = (ROI) n.getUserObject();
                rois.addAll(r.getShapes().values());
            }
            addRecentlyModifiedFolder(targetFolder);
            manager.moveROIsToFolder(rois,
                    Collections.singletonList(targetFolder));
        }
    }
    
    /**
     * Returns a List of ROINodes with top level nodes only. E. g. Consider a
     * branch A -> B -> C, if objects contains node B and C, this method will
     * return node B only (as C is a child of B).
     * 
     * @param objects
     *            The nodes to check
     * @return See above
     */
    private List<ROINode> dropChildFolderNodes(List<ROINode> objects) {

        List<ROINode> sorted = new ArrayList<ROINode>();
        for (ROINode n : objects) {
            if (n.isFolderNode())
                sorted.add(n);
        }

        Collections.sort(sorted, new Comparator<ROINode>() {
            @Override
            public int compare(ROINode o1, ROINode o2) {
                return o1.getPath().getPathCount()
                        - o2.getPath().getPathCount();
            }
        });

        List<ROINode> result = new ArrayList<ROINode>();
        for (ROINode n1 : sorted) {

            boolean contains = false;
            for (ROINode n2 : sorted) {
                if (n2 == n1)
                    continue;

                if (n2.getPath().isDescendant(n1.getPath())) {
                    contains = true;
                    break;
                }
            }

            if (!contains)
                result.add(n1);
        }

        return result;
    }
    
    /**
     * The default drag behavior - if multiselection is enabled - is to extend
     * the selection. To prevent that, we have to override the TableUI. This
     * workaround is based on the forum post:
     * https://community.oracle.com/thread/1361004
     */
    class CustomTableUI extends BasicTableUI {
        
        @Override
        protected MouseInputListener createMouseInputListener() {
            return new MouseInputHandler() {

                public void mousePressed(MouseEvent e) {
                    Point origin = e.getPoint();
                    int row = table.rowAtPoint(origin);
                    int column = table.columnAtPoint(origin);
                    if (row != -1 && column != -1) {
                        if (!table.isCellSelected(row, column)) {
                            super.mousePressed(e);
                        }
                    }
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    super.mouseDragged(e);
                    table.getTransferHandler().exportAsDrag(table, e,
                            DnDConstants.ACTION_MOVE);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    super.mouseReleased(e);
                    timer.stop();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    super.mouseEntered(e);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    super.mouseExited(e);
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    super.mouseMoved(e);
                }
                
            };
        }

    }
    
}

