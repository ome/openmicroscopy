/*
 * org.openmicroscopy.shoola.agents.util.flim.StatsTableModel
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.util.flim;

//Java imports
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.DefaultTableModel;

//Third-party libraries

//Application-internal dependencies

/**
 * Component displaying the histogram.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class StatsTableModel 
	extends DefaultTableModel
{
	/** The default columns in the table. */
	static List<String> DEFAULTCOLUMNS;
	static{
		DEFAULTCOLUMNS = new ArrayList<String>();
		DEFAULTCOLUMNS.add("Colour");
		DEFAULTCOLUMNS.add("Bin Start");
		DEFAULTCOLUMNS.add("Bin End");
		DEFAULTCOLUMNS.add("Min");
		DEFAULTCOLUMNS.add("Max");
		DEFAULTCOLUMNS.add("Mean");
		DEFAULTCOLUMNS.add("Stddev");
		DEFAULTCOLUMNS.add("Freq");
		DEFAULTCOLUMNS.add("Percent");
	}
	
	List<String> columns; 
	/** 
	 * instatiate the table model.
	 * @param rows
	 * @param cols
	 */
	StatsTableModel(int rows)
	{
		buildModel();
	}
	
	/**
	 * Overridden {@see DefaultTableModel#getColumnCount()
	 */
    public int getColumnCount() {
        return columns.size();
    }

	/**
	 * Overridden {@see DefaultTableModel#getColumnName(int)
	 */
	public String getColumnName(int i )
	{
		return columns.get(i);
	}
	
	/**
	 * Build the model.
	 */
	private void buildModel()
	{
		buildColumns();
		this.setColumnCount(columns.size());
	}

	/**
	 * Build the columns, add new columns to the default.
	 */
	private void buildColumns()
	{
		columns = new ArrayList<String>();
		for(String column : DEFAULTCOLUMNS)
			columns.add(column);
	}
	
	/**
	 * Insert data into a new row.
	 * @param data See above.
	 */
	public void insertData(RowData data) 
	{
		Object row[] = new Object[data.size()];
		for(int i = 0 ; i < data.size() ; i++)
			row[i]=data.getElement(i);
		addRow(row);
	}
	
	/**
	 * Clear the table.
	 */
	public void clear()
	{
		while(getRowCount()!=0)
			removeRow(0);
	}
	
}
