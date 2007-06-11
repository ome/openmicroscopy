/*
 * org.openmicroscopy.shoola.agents.measurement.view.roiassistant.ROIAssistantModel 
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
package org.openmicroscopy.shoola.agents.measurement.view.roiassistant;

//Java imports
import java.util.TreeMap;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.roi.exception.NoSuchROIException;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;
import org.openmicroscopy.shoola.util.roi.model.util.FigureType;

/** 
 * 
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
public class ROIAssistantModel
	extends AbstractTableModel
{	
	/** The roi this table model is based on. */
	private ROI							currentROI;
	
	/** The current plane of the viewer. */
	private Coord3D 					currentPlane;
	
	/** The number of columns in the model. */
	private int							numColumns;
	
	/** The number of the rows in the model. */
	private int 						numRows;
	
	private TreeMap<Coord3D, FigureType> shapeMap;
	
	ROIAssistantModel(int numCol, int numRow, Coord3D currentPlane, ROI roi)
	{
		this.setColumnCount(numCol);
		this.setRowCount(numRow);
		this.currentPlane = currentPlane;
		currentROI = roi;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount()
	{
		return numColumns;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount()
	{
		return numRows;
	}

	/** 
	 * Set the value of the model to the object.
	 */
	public void setValueAt(Object value, int col, int row)
	{
		
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int col, int row)
	{
		try
		{
			ROIShape shape = currentROI.getShape(new Coord3D(row, col));
			return shape.getFigure().getType();
		}
		catch (NoSuchROIException e)
		{
			return null;
		}
	}
	
	public void setColumnCount(int col)
	{
		numColumns = col;
	}
	
	public void setRowCount(int row)
	{
		numRows = row;
	}
	
}


