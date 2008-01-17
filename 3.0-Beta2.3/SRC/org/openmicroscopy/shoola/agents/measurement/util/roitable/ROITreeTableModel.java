/*
 * org.openmicroscopy.shoola.agents.measurement.util.roitable.ROITreeTableModel 
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
package org.openmicroscopy.shoola.agents.measurement.util.roitable;



// Java imports
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.tree.TreePath;

// Third-party libraries

// Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.util.model.AnnotationDescription;
import org.openmicroscopy.shoola.agents.measurement.util.model.AnnotationField;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKey;
import org.openmicroscopy.shoola.util.ui.treetable.model.OMETreeTableModel;

/**
 * 
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $Date: $)
 *          </small>
 * @since OME3.0
 */
public class ROITreeTableModel 
	extends OMETreeTableModel
{
	
	/** Collection of column names. */
	private List<AnnotationField>					fields;

	/** Map of annotationkeys to columns. */
	private HashMap<AnnotationKey, Integer>			keyMap;
	
	/**
	 * Set the fields of the Table. Each field represents a column in the table
	 * and will represent an annotation in the ROI, ROIShape. 
	 * @param newFields see above.
	 */
	public void setFields(List<AnnotationField> newFields)
	{
		fields = newFields;
		updateKeyMap();
	}
	
	/**
	 * Set the model to use ROI nodes and columns as a vector.
	 * 
	 * @param node		root node for model.
	 * @param columns	column names.
	 * @param fields	field list.
	 */
	public ROITreeTableModel(ROITreeNode node, Vector<String> columns, 
							List<AnnotationField> fields)
	{
		super(node, columns);
		keyMap = new HashMap<AnnotationKey, Integer>();
		this.fields = fields;
		updateKeyMap();
	}
	
	/** 
	 * Build the keyMap, mapping keys to columns.
	 *
	 */
	private void updateKeyMap()
	{
		keyMap.clear();
		for(int i = 0 ; i < fields.size(); i++)
			keyMap.put(fields.get(i).getKey(), i);
	}
	
	/**
	 * The node has been updated.
	 * 
	 * @param node
	 *            see above.
	 */
	public void nodeUpdated(ROITreeNode node)
	{
		Object[] objects=new Object[2];
		objects[0]=getRoot();
		objects[1]=node.getParent();
		TreePath path=new TreePath(objects);
		modelSupport.fireChildChanged(path, node.getParent().getIndex(node),
			node);
	}
	
	/**
	 * Set the value of column field of the node object to the value param.
	 * 
	 * @param value 	 the new value of the object.
	 * @param nodeObject the node.
	 * @param column     the field.
	 */
	public void setValueAt(Object value, Object nodeObject, int column)
	{
		if (nodeObject instanceof ROITreeNode)
		{
			ROITreeNode node=(ROITreeNode) nodeObject;
			Object object = node.getUserObject();
			AnnotationField objectField = fields.get(column-1);
				
			if(object instanceof ROI)
			{
				ROI roi = (ROI)object;
				roi.setAnnotation(objectField.getKey(), value);
			}
			else if(object instanceof ROIShape)
			{
				ROIShape shape = (ROIShape)object;
				shape.setAnnotation(objectField.getKey(), value);
			}
			nodeUpdated(node);
		}
	}
	
	/**
	 * Get the value of the column field of the node.
	 * 
	 * @param nodeObject the node.
	 * @param column     the field.
	 * @return see above.
	 */
	public Object getValueAt(Object nodeObject, int column)
	{
		if (nodeObject instanceof ROITreeNode)
		{
			ROITreeNode ROITreeNode=(ROITreeNode) nodeObject;
			Object object=ROITreeNode.getUserObject();
			if(column==0)
				return object;
			AnnotationField objectField = fields.get(column-1);
			if(object instanceof ROI)
			{
				ROI roi = (ROI)object;
				return roi.getAnnotation(objectField.getKey());
			}
			else if(object instanceof ROIShape)
			{
				ROIShape shape = (ROIShape)object;
				return shape.getAnnotation(objectField.getKey());
			}
		}
		return null;
	}
	
	/**
	 * Return the column used by the ROITable for the key. Offset by one to 
	 * take into account the tree column.
	 * @param key see above.
	 * @return see above.
	 */
	public int getKeyColumn(AnnotationKey key)
	{
		return keyMap.get(key)+1;
	}
	
	/**
	 * Is the cell editable for this node and column.
	 * 
	 * @param node     the node of the tree.
	 * @param column   the field to edit.
	 * @return see above.
	 */
	public boolean isCellEditable(Object node, int column)
	{
		return fields.get(column-1).isEditable();
	}
	
	/**
	 * Get the class of each column.
	 * 
	 * @param column   the field in the data for the node.
	 * @return see above.
	 */
	public Class<?> getColumnClass(int column)
	{
		switch(column)
		{
			case 0:
				return ROINode.class;
			default:
				return fields.get(column-1).getClass();
		}
	}
	
	/**
	 * Is the column a shape column.
	 * 
	 * @param modelIndex   column index.
	 * @return see above.
	 */
	public boolean isShapeColumn(int modelIndex)
	{
		return fields.get(modelIndex-1).getName().
				equals(AnnotationDescription.SHAPE_STRING);
	}
}
