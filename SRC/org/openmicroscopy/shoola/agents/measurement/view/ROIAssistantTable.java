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
import java.awt.FontMetrics;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.util.ROIAssistantCellRenderer;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;

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
class ROIAssistantTable
	extends JTable
{
	
	/** The default, minimum width of the column in the table. */
	private final static int 	COLUMNWIDTH = 32;
	
	/** The width of the leader column in the table. */
	private final static int 	LEADERCOLUMN_WIDTH = 80;
	
	/** The colour of the grid in the table. */
	private final static Color 	GRIDCOLOUR = new Color(180, 213, 255);
	
	/** The column width finally chosen, determined by the number in the col. */
	private int columnWidth;
	
	/** final width of the leader column. */
	private int leaderColumnWidth;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param model Reference to the model. 
	 */
	ROIAssistantTable(ROIAssistantModel model)
	{
		this.setModel(model);
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		int columnWidth = 0;
		FontMetrics metrics = getFontMetrics(getFont());
		int w;
		for(int i = 0 ; i < getColumnCount(); i++)
		{
			w =  metrics.stringWidth(model.getColumnName(i));
			columnWidth = Math.max(w, COLUMNWIDTH);
		}
		
		w  =  metrics.stringWidth(model.getColumnName(0));

		leaderColumnWidth = Math.max(w, LEADERCOLUMN_WIDTH);
		TableColumn col;
		int width;
		for (int i = 0 ; i < getColumnCount(); i++)
		{
			col = getColumnModel().getColumn(i);
			width = columnWidth;
			if (i == 0) width = leaderColumnWidth;
			col.setMinWidth(width);
			col.setMaxWidth(width);
			col.setPreferredWidth(width);
			col.setResizable(false);
		}
		setColumnSelectionAllowed(true);
		setRowSelectionAllowed(true);
		setRowHeight(columnWidth);
		setGridColor(GRIDCOLOUR);
		getTableHeader().setReorderingAllowed(false);
		setShowGrid(true);
	}
	
	/** 
	 * Returns the width of the column, used in the ROIAssistant.
	 * 
	 * @return See above. 
	 */
	int getColumnWidth() { return columnWidth; }
	
	/** 
	 * Returns the width of the leader column, used in the ROIAssistant.
	 * 
	 * @return See above. 
	 */
	int getLeaderColumnWidth() { return leaderColumnWidth; }
	
	/** 
	 * Returns the shape at the specified column and row.
	 *  
	 * @param row The selected row.
	 * @param col The selected column.
	 * @return See above.
	 */
	ROIShape getShapeAt(int row, int col)
	{
		return ((ROIAssistantModel) getModel()).getShapeAt(row, col); 
	}
	
	/**
	 * Overridden to return a customized cell renderer.
	 * @see JTable#getCellRenderer(int, int)
	 */
	public TableCellRenderer getCellRenderer(int row, int column) 
	{
        return new ROIAssistantCellRenderer();
    }

}


