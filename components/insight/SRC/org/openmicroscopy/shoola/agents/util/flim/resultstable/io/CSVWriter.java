/*
 * org.openmicroscopy.shoola.env.ui.resultstable.io.CSVWriter 
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
package org.openmicroscopy.shoola.agents.util.flim.resultstable.io;

//Java imports

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.TableModel;

//Third-party libraries

//Application-internal dependencies

/** 
 * Displays the results stored in the passed file.
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
public class CSVWriter 
{
	/** The map of the line No and data. */
	Map<Integer, List<String>> data;
	
	/** The number of columns in the file. */
	private int numColumns;
	
	
	/**
	 * Instantiate the writer with the table model.
	 */
	CSVWriter()
	{
		
	}
	
	/** 
	 * Initialise the data structures.
	 */
	private void initData()
	{
		numColumns = 0;
		data = new LinkedHashMap<Integer, List<String>>();
	}
	
	/**
	 * Parse the table model.
	 * @param tableModel See above.
	 */
	public void parseTable(TableModel tableModel)
	{
		initData();
		
		for(int i = 0 ; i < tableModel.getRowCount(); i ++)
		{
			data.put(i,createList(tableModel, i));
		}
	}
	
	/**
	 * Create a list from the index row of the table model.
	 * @param model See above.
	 * @param index See above.
	 * @return See above.
	 */
	private List<String> createList(TableModel model, int index)
	{
		List<String> row = new ArrayList<String>();
		for(int i = 0; i < model.getColumnCount() ; i++)
			row.add(model.getValueAt(index, i).toString());
		numColumns = Math.max(numColumns, row.size());
		return row;
	}
	
	/**
	 * Write the table to the file.
	 * @param fileName See above.
	 * @throws IOException See above.
	 */
	public void writeFile(String fileName) throws IOException
	{
		FileOutputStream fileOutputStream = new FileOutputStream(fileName);
		OutputStreamWriter dataOutputStream = new OutputStreamWriter(fileOutputStream);
		BufferedWriter outputWriter = new BufferedWriter(dataOutputStream);
		for(int i = 0 ; i < getNumRows() ; i++)
		{
			List<String> row = getRow(i);
			for(int j = 0 ; j < row.size(); j++)
			{
				outputWriter.write(row.get(j));
				if(j<row.size()-1)
					outputWriter.write(",");
			}
			outputWriter.newLine();
		}
	}
	
	
	/**
	 * The number of rows in the file.
	 * @return
	 */
	public int getNumRows()
	{
		return data.size();
	}
	
	/**
	 * The number of columns in the file.
	 * @return
	 */
	public int getNumColumns()
	{
		return numColumns;
	}
	
	/**
	 * Get the row from the file.
	 * @param row The row.
	 * @return See above.
	 */
	public List<String> getRow(int row)
	{
		return padToSize(data.get(row));
	}
	
	/**
	 * Pad the list to the length of the longest row.
	 * @param list See above.
	 * @return See above.
	 */
	public List<String> padToSize(List<String> list)
	{
		for(int i = list.size(); i < numColumns; i++)
			list.add("");
		return list;
	}
	
}
