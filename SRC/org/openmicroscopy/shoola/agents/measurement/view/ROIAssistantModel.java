/*
 * org.openmicroscopy.shoola.agents.measurement.view.ROIAssistantModel 
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import javax.swing.table.AbstractTableModel;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.roi.exception.NoSuchROIException;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;

/** 
 * The model.
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
class ROIAssistantModel
	extends AbstractTableModel
{	
	
	/** The roi this table model is based on. */
	private ROI							currentROI;
		
	/** The number of columns in the model. */
	private int							numColumns;
	
	/** The number of the rows in the model. */
	private int 						numRows;
	
	/** The map of the shape type to a coord3D object. */
	private TreeMap<Coord3D, String> 	shapeMap;
	
	/** The name of the columns. */
	private List<String> 				columnNames;
	
	/**
	 * Populates the shape Map of the model with all the ROIShapes of the 
	 * current ROI. 
	 */
	private void populateShapeMap()
	{
		shapeMap.clear();
		TreeMap<Coord3D, ROIShape> list = currentROI.getShapes();
		Iterator<ROIShape> shapeIterator = list.values().iterator();
		ROIShape shape;
		Coord3D coord;
		while (shapeIterator.hasNext())
		{
			shape = shapeIterator.next();
			coord = shape.getCoord3D();
			numRows = Math.max(numRows, coord.getZSection());
			shapeMap.put(coord, shape.getFigure().getType());
		}
	}
	
	/**
	 * Model of the ROIAssistant to store the current locations of the ROIs
	 * on the images, and their type.
	 * 
	 * @param numRow 		The number of z sections in the image. 
	 * @param numCol 		The number of time points in the image. 
	 * @param currentPlane 	The current plane of the image.
	 * @param roi			The ROI which will be propagated.
	 */
	ROIAssistantModel(int numCol, int numRow, Coord3D currentPlane, ROI roi)
	{
		this.setColumnCount(numCol);
		this.setRowCount(numRow);
		this.columnNames = new ArrayList<String>();
		currentROI = roi;
		
		shapeMap = new TreeMap<Coord3D, String>(new Coord3D());
		for (int i = 1 ; i < numCol+1 ; i++)
			columnNames.add(i+"");
		populateShapeMap();
	}

	/**
	 * Returns the shape at z-section, timePoint which refers to the column and
	 * row in the model.
	 * 
	 * @param zSection	The row of the table.
	 * @param timePoint The col of the model.
	 * @return see above.
	 */
	ROIShape getShapeAt(int zSection, int timePoint)
	{
		try
		{
			int translateZ = (getRowCount()-zSection);
			return currentROI.getShape(
						new Coord3D(translateZ-1, timePoint));
			
		}
		catch (NoSuchROIException e)
		{
//			TODO; register in logger
			return null;
		}
	}
	
	/**
	 * Overridden to return the number of columns.
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() { return numColumns; }

	/**
	 * Overridden to return the name of the specified column.
	 * @see AbstractTableModel#getColumnName(int)
	 */
	public String getColumnName(int col) { return columnNames.get(col); }

	/**
	 * Overridden to return the number of rows.
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() { return numRows; }

	/** 
	 * Overridden to set the value of the model to the object.
	 * @see javax.swing.table.TableModel#setValueAt(Object, int, int)
	 */
	public void setValueAt(Object value, int col, int row)
	{
		
	}
	
	/**
	 *  Overridden to return the value of the model to the object.
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int zSection, int timePoint)
	{
		int translateZ = (getRowCount()-zSection);
		try
		{
			ROIShape shape = currentROI.getShape(new Coord3D(translateZ-1, 
											timePoint));
			if (shape == null) return null;
			return shape.getFigure().getType();
		}
		catch (NoSuchROIException e)
		{
			//TODO; register in logger
			return null;
		}
	}
	
	/**
	 * Sets the number of columns in the table to col.
	 * 
	 * @param col The value to set.
	 */
	public void setColumnCount(int col) { numColumns = col; }
	
	/**
	 * Sets the number of rows in the table to col.
	 * 
	 * @param row The value to set.
	 */
	public void setRowCount(int row) { numRows = row; }
	
}


