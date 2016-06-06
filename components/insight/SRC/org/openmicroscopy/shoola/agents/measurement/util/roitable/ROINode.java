/*
 * org.openmicroscopy.shoola.agents.measurement.util.roitable.ROINode 
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
package org.openmicroscopy.shoola.agents.measurement.util.roitable;



//Java imports
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.tree.TreePath;

//Third-party libraries
import org.jdesktop.swingx.treetable.MutableTreeTableNode;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys;
import org.openmicroscopy.shoola.util.roi.model.annotation.MeasurementAttributes;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;
import org.openmicroscopy.shoola.util.ui.treetable.model.OMETreeNode;

import omero.gateway.model.FolderData;

/**
 * The ROINode is an extension of the DefaultMutableTreeTableNode
 * to use in the ROITable, this creates the structure for mapping
 * nodes in the table to ROI and ROIShapes.
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
public class ROINode 
	extends OMETreeNode
{

	/** ROI ID Column no for the wizard. */
	private static final int				ROIID_COLUMN = 
		ROITableModel.ROIID_COLUMN;

	/** Time point Column no for the wizard. */
	private static final int				TIME_COLUMN = 
		ROITableModel.TIME_COLUMN;
	
	/** Z-Section Column no for the wizard. */
	private static final int				Z_COLUMN = ROITableModel.Z_COLUMN;

	/** Type Column no for the wizard. */
	private static final int				SHAPE_COLUMN = 
		ROITableModel.SHAPE_COLUMN;
	
	/** Annotation Column no for the wizard. */
	private static final int				ANNOTATION_COLUMN = 
		ROITableModel.ANNOTATION_COLUMN;

	/** Visible Column no for the wizard. */
	private static final int				VISIBLE_COLUMN = 
		ROITableModel.VISIBLE_COLUMN;
	
	/** The map of the children, ROIShapes belonging to the ROINode. */
	private Map<ROIShape, ROINode>				childMap;

	/** The map of the children, ROIShapes belonging to the ROINode. */
	private TreeMap<Coord3D, ROINode>			childCoordMap;
		
	/** The map of the children, FolderData belonging to the ROINode. */
    private HashMap<FolderData, ROINode>           folderMap;
    
    /**
     * Flag to indicate if this node is marked as visible; only used for folder
     * nodes, which can override shape node visibility
     */
    private boolean visible = true;
    
	/**
	 * Constructor for parent node. 
	 * @param str parent type.
	 */
	public ROINode(String str)
	{
		super(str);
		initMaps();
	}
	
	/**
	 * Construct a node with ROI type.
	 * @param nodeName see above.
	 */
	public ROINode(ROI nodeName)
	{
		super(nodeName);
		initMaps();
	}
	
	/**
	 * Construct ROINode with ROIShape type.
	 * @param nodeName see above.
	 */
	public ROINode(ROIShape nodeName)
	{
		super(nodeName);
		initMaps();
	}
	
	/**
     * Construct a node for a ROI Folder
     * @param folder see above.
     */
    public ROINode(FolderData folder)
    {
        super(folder);
        initMaps();
    }

    /**
     * Checks if this node is a folder node
     * 
     * @return <code>true</code> if the ROINode represents a folder,
     *         <code>false</code> otherwise
     */
    public boolean isFolderNode() {
        return getUserObject() instanceof FolderData;
    }

    /**
     * Checks if this node is a roi node
     * 
     * @return <code>true</code> if the ROINode represents a ROI,
     *         <code>false</code> otherwise
     */
    public boolean isROINode() {
        return getUserObject() instanceof ROI;
    }
    
    /**
     * Checks if this node is a shape node
     * 
     * @return <code>true</code> if the ROINode represents a ROIShape,
     *         <code>false</code> otherwise
     */
    public boolean isShapeNode() {
        return getUserObject() instanceof ROIShape;
    }

    /**
     * Checks if node is an ancestor of this node
     * 
     * @param node
     *            The node to check
     * @return <code>true</code> if node is an ancestor of this node
     */
    public boolean isAncestorOf(ROINode node) {
        for (int i = 0; i < node.getPath().getPathCount(); i++) {
            if (node.getPath().getPathComponent(i) == this)
                return true;
        }
        return false;
    }
    
	/**
	 * Get the point in the parent where a child with coordinate should be 
	 * inserted.
	 * 
	 * @param coord see above.
	 * @return see above.
	 */
	public int getInsertionPoint(Coord3D coord)
	{
		Iterator<Coord3D> i = childCoordMap.keySet().iterator();
		int index = 0;
		Coord3D nodeCoord;
		while (i.hasNext())
		{
			nodeCoord = i.next();
			if (nodeCoord.compare(nodeCoord, coord) != -1)
				return index;
			index++;
		}
		return index;
	}
	
    /**
     * Get the point in the parent where a child should be inserted.
     * 
     * @param f
     *            see above.
     * @return see above.
     */
    public int getInsertionPoint(FolderData f) {
        int index = 0;

        if (f == null)
            return index;

        for (MutableTreeTableNode n : children) {
            ROINode r = (ROINode) n;
            if (r.getUserObject() == null)
                continue;
            if (r.isFolderNode()) {
                if (((FolderData) r.getUserObject()).getName()
                        .compareToIgnoreCase(f.getName()) > 0)
                    break;
            }
            index++;
        }

        return index;
    }
    
	/** Initializes the maps for the child nodes. */
	private void initMaps()
	{
		childMap = new HashMap<ROIShape, ROINode>();
		childCoordMap = new TreeMap<Coord3D, ROINode>(new Coord3D());
		folderMap = new HashMap<FolderData, ROINode>();
	}
	
	/**
	 * Find the shape belonging to the ROINode.
	 * 
	 * @param shape see above.
	 * @return see above.
	 */
	public ROINode findChild(ROIShape shape)
	{
	    return childMap.get(shape);
	}

	/**
	 * Find the shape belonging to the shapeCoord.
	 * 
	 * @param shapeCoord see above.
	 * @return see above.
	 */
	public ROINode findChild(Coord3D shapeCoord)
	{
	    return childCoordMap.get(shapeCoord);
	}
	
    /**
     * Find the child node representing the given folder
     * 
     * @param folder
     *            see above.
     * @return see above.
     */
    public ROINode findChild(FolderData folder) {
        return folderMap.get(folder);
    }
    
	/**
	 * Returns <code>true</code> if the node can be edited, <code>false</code>
	 * otherwise.
	 * 
	 * @param column the column to edit.
	 * @return see above.
	 */
	public boolean isEditable(int column)
	{
		switch (column)
		{
			case VISIBLE_COLUMN+1:
			case ANNOTATION_COLUMN+1:
				return true;
			default:
				return false;
		}
	}
	
	/**
	 * Add a child to the current node.
	 * @param child see above.
	 * @param index the index to place child. 
	 */
	 public void insert(ROINode child, int index) 
	 {
		 super.insert(child, index);
		 Object userObject = child.getUserObject();
		 if (userObject instanceof ROIShape)
		 {
			 ROIShape shape = (ROIShape) userObject;
			 child.setExpanded(true);
			 childMap.put(shape, (ROINode) child);
			 childCoordMap.put(shape.getCoord3D(), (ROINode) child);
		 }
		 else if(child.isFolderNode()) {
		     folderMap.put((FolderData)userObject, (ROINode) child);
		 }
	 }

	 /**
	  * Remove a child to the current node.
	  * @param child see above.
	  */
	 public void remove(ROINode child) 
	 {
		 super.remove(child);
		 Object userObject = child.getUserObject();
		 if (userObject instanceof ROIShape)
		 {
			 ROIShape shape = (ROIShape) userObject;
			 childMap.remove(shape);
			 childCoordMap.remove(shape.getCoord3D());
		 }
		 else if(child.isFolderNode()) {
             folderMap.remove((FolderData)userObject);
         }
	 }
	 
	 /**
	  * Remove a child to the current node.
	  * @param childCoord see above.
	  */
	 public void remove(Coord3D childCoord) 
	 {
		 ROINode childNode = childCoordMap.get(childCoord);
		 
		 super.remove(childNode);
		 Object userObject = childNode.getUserObject();
		 if (userObject instanceof ROIShape)
		 {
			 ROIShape shape = (ROIShape) userObject;
			 childMap.remove(shape);
			 childCoordMap.remove(shape.getCoord3D());
		 }
	 }
	 
	/**
	 * Get the value for the node at column
	 * @param column return the value of the element at column.
	 */
	public Object getValueAt(int column)
	{
		Object userObject = getUserObject();
		if (userObject instanceof ROI)
		{
			ROI roi = (ROI) userObject;
			switch (column)
			{
				case 0:
					return null;
				case ROIID_COLUMN+1:
					return Long.valueOf(roi.getID());
				case TIME_COLUMN+1:
					return roi.getTRange();
				case Z_COLUMN+1:
					return roi.getZRange();
				case SHAPE_COLUMN+1:
					return roi.getShapeTypes();
				case ANNOTATION_COLUMN+1:
					return AnnotationKeys.TEXT.get(roi);
				case VISIBLE_COLUMN+1:
					return isVisible();
				default:
					return null;
			}
		}
		else if (userObject instanceof ROIShape)
		{
			ROIShape roiShape = (ROIShape) userObject;
			int v;
			switch (column)
			{
				case 0:
					return null;
				case ROIID_COLUMN+1:
					if (roiShape.getROI().isClientSide())
						return "--";
					return Long.valueOf(roiShape.getROIShapeID());
				case TIME_COLUMN+1:
					v = roiShape.getT();
					if (v < 0) return "";
					return ((Integer) (v+1)).toString();
				case Z_COLUMN+1:
					v = roiShape.getZ();
					if (v < 0) return "";
					return ((Integer) (v+1)).toString();
				case SHAPE_COLUMN+1:
					return roiShape.getFigure().getType();
				case ANNOTATION_COLUMN+1:
					return roiShape.getFigure().getAttribute(
							MeasurementAttributes.TEXT);
				case VISIBLE_COLUMN+1:
					return isVisible();
				default:
					return null;
			}
        } 
		else if (userObject instanceof FolderData) {
		    FolderData folder = (FolderData)userObject;
            switch (column) {
            case ROIID_COLUMN+1:
                return folder.getId();
            case ANNOTATION_COLUMN+1:
                return folder.getDescription();
            case VISIBLE_COLUMN + 1:
                return isVisible();
            default:
                return "";
            }
        }
		
		return null;
	}

    boolean isVisible() {
        if (isROINode()) {
            return ((ROI) getUserObject()).isVisible();
        } else if (isShapeNode()) {
            return ((ROIShape) getUserObject()).getFigure().isVisible();
        } else if (isFolderNode()) {
            return this.visible;
        }
        return false;
    }
	
	/**
	 * Get the value for the node at column
	 * @param value  the value of the object to set.
	 * @param column the column.
	 */
	public void setValueAt(Object value, int column)
	{
		Object userObject = getUserObject();
		if (userObject instanceof ROI)
		{
			ROI roi = (ROI) userObject;
			switch (column) {
				case 0:
				case ROIID_COLUMN+1:
				case TIME_COLUMN+1:
				case Z_COLUMN+1:
				case SHAPE_COLUMN+1:
					break;
				case ANNOTATION_COLUMN+1:
					if (value instanceof String)
						roi.setAnnotation(AnnotationKeys.TEXT, (String) value);
					break;
				case VISIBLE_COLUMN+1:
					if (value instanceof Boolean)
					{
					    for(MutableTreeTableNode child : getChildList()) {
	                        child.setValueAt(value, column);
	                    }
					}
					break;
					default:
					break;
			}
		} else if (userObject instanceof ROIShape) {
			ROIShape roiShape = (ROIShape) userObject;
			ROIFigure figure = roiShape.getFigure();
			switch (column) {
				case 0:
				case ROIID_COLUMN+1:
				case TIME_COLUMN+1:
				case Z_COLUMN+1:
				case SHAPE_COLUMN+1:
				case ANNOTATION_COLUMN+1:
					if (value instanceof String)
					{
						AnnotationKeys.TEXT.set(roiShape, (String)value);
						MeasurementAttributes.TEXT.set(figure, (String)value);
						MeasurementAttributes.SHOWTEXT.set(figure, 
								!((String) value).equals(""));
					}
					break;
				case VISIBLE_COLUMN+1:
					if(value instanceof Boolean)
						roiShape.getFigure().setVisible((Boolean) value);
					break;
				default:
					break;
			}
		} else if (userObject instanceof FolderData) {
		    
            switch (column) {
            case VISIBLE_COLUMN + 1:
                if(value instanceof Boolean) {
                    this.visible = (Boolean) value;
                    for (int i = 0; i < getChildCount(); i++) {
                        ROINode child = (ROINode) getChildAt(i);
                        propateFolderVisibility(this.visible, child);
                    }
                    updateShapeVisibility();
                }
                break;
            default:
                break;

            }
		}
	}
	
	/**
	 * Runs through all shape nodes and updates their visibility with
	 * respect to the folders' visibility state they are part of.
	 */
    private void updateShapeVisibility() {
        Collection<ROINode> shapes = ROIUtil.getShapeNodes(getRoot());

        for (ROINode shape : shapes) {
            ROIShape s = (ROIShape) shape.getUserObject();
            s.getFigure().setVisible(isShapeVisible(shape));
        }
    }

    /**
     * Propagates the visibility status down to all subfolders of the given node
     * 
     * @param vis
     *            The visibility status
     * @param node
     *            The node
     */
    private void propateFolderVisibility(boolean vis, ROINode node) {
        if (!node.isFolderNode())
            return;
        node.setValueAt((Boolean) vis, VISIBLE_COLUMN + 1);
        for (int i = 0; i < node.getChildCount(); i++) {
            ROINode child = (ROINode) node.getChildAt(i);
            propateFolderVisibility(vis, child);
        }
    }
    
    /**
     * Determines the visibility of a shape node with respect to the folders'
     * visibility the shape is part of.
     * 
     * @param shape
     *            The shape node to check
     * @return Returns <code>true</code> if any folder the shape is part of is
     *         visible, <code>false</code> otherwise. If the shape is not part
     *         of any folder, the shape's visibility itself is returned.
     */
    private boolean isShapeVisible(ROINode shape) {
        Boolean b = null;
        Collection<ROINode> shapeNodes = ROIUtil.getShapeNodes(((ROIShape) shape
                .getUserObject()).getROIShapeID(), getRoot());
        for (ROINode shapeNode : shapeNodes) {
            TreePath path = shapeNode.getPath();
            if (path.getPathCount() > 2) {
                // the direct parent of a shape node is always an roi node
                // therefore take a step of length 2 to get the folder node
                ROINode folder = (ROINode) path.getPathComponent(path
                        .getPathCount() - 3);
                if (folder.isRoot())
                    return shape.isVisible();
                if (b == null)
                    b = folder.isVisible();
                else
                    b = b == true || folder.isVisible();
            }
        }

        if (b == null)
            return shape.isVisible();
        else
            return b;
    }
    
    /**
     * Indicates if the node can be annotated if <code>true</code>,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean canAnnotate() {
        if (isFolderNode())
            return ((FolderData) getUserObject()).canAnnotate();
        if (isROINode())
            return ((ROI) getUserObject()).canAnnotate();
        if (isShapeNode())
            return ((ROIShape) getUserObject()).getROI().canAnnotate();

        return false;
    }

    /**
     * Indicates if the node can be deleted if <code>true</code>,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean canDelete() {
        if (isFolderNode())
            return ((FolderData) getUserObject()).canDelete();
        if (isROINode())
            return ((ROI) getUserObject()).canDelete();
        if (isShapeNode())
            return ((ROIShape) getUserObject()).getROI().canDelete();

        return false;
    }

    /**
     * Indicates if the node can be edited if <code>true</code>,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean canEdit() {
        if (isFolderNode())
            return ((FolderData) getUserObject()).canEdit();
        if (isROINode())
            return ((ROI) getUserObject()).canEdit();
        if (isShapeNode())
            return ((ROIShape) getUserObject()).getROI().canEdit();

        return false;
    }

    /**
     * Get the root node of this branch
     * 
     * @return See above
     */
    private ROINode getRoot() {
        return (ROINode) getPath().getPathComponent(0);
    }

}
