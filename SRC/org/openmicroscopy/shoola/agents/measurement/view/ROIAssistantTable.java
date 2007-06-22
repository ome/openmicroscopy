/*
 * org.openmicroscopy.shoola.agents.measurement.view.ROIAssistantTable 
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
import java.awt.Color;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.openmicroscopy.shoola.agents.measurement.util.ROIAssistantCellRenderer;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;

//Third-party libraries

//Application-internal dependencies

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
public class ROIAssistantTable
	extends JTable
{	
	public final static int 	COLUMNWIDTH = 32;
	public final static int 	LEADERCOLUMN_WIDTH = 80;
	private final static Color 	GRIDCOLOUR = new Color(180, 213, 255);
	
	ROIAssistantTable(ROIAssistantModel model)
	{
		this.setModel(model);
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		for(int i = 0 ; i < getColumnCount(); i++)
		{
			
			TableColumn col = getColumnModel().getColumn(i);
			int width = COLUMNWIDTH;
			if(i==0)
				width = LEADERCOLUMN_WIDTH;
			col.setMinWidth(width);
			col.setMaxWidth(width);
			col.setPreferredWidth(width);
			col.setResizable(false);
		}
		setColumnSelectionAllowed(true);
		setRowSelectionAllowed(true);
		setRowHeight(COLUMNWIDTH);
		setGridColor(GRIDCOLOUR);
		getTableHeader().setReorderingAllowed(false);
		setShowGrid(true);
	}
	
	/**
	 * Overridden to return a customized cell renderer.
	 * @see JTable#getCellRenderer(int, int)
	 */
	public TableCellRenderer getCellRenderer(int row, int column) 
	{
        return new ROIAssistantCellRenderer();
    }
	
	public ROIShape getShapeAt(int row, int col)
	{
		return ((ROIAssistantModel)getModel()).getShapeAt(row, col); 
	}

}


