/*
 * org.openmicroscopy.shoola.agents.util.flim.resultstable.CSVReader 
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
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
public class CSVReader 
{
	/** The filename to get the CSV data from. */
	String fileName;
	
	/** The map of the line No and data. */
	Map<Integer, List<String>> data;
	
	/** The number of columns in the file. */
	private int numColumns;
	
	/**
	 * Instatiate the class.
	 */
	public CSVReader()
	{

	}
	
	/**
	 * Parse the file. 
	 * @param fileName The file to parse.
	 * @throws IOException Thrown if the file cannot be read, closed.
	 */
	public void parseFile(String fileName) throws IOException
	{
		initData();
		this.fileName = fileName;
		FileInputStream fileInputStream = new FileInputStream(fileName);
		DataInputStream dataInputStream = new DataInputStream(fileInputStream);
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(dataInputStream));
		String buff;
		int lineCount = 0;
		while((buff=bufferedReader.readLine())!=null)
		{
			parseLine(buff, lineCount);
			lineCount = lineCount++;
		}
		bufferedReader.close();
	}
	
	/**
	 * Parse a line of the file into a List.
	 * @param line The string to parse.
	 * @param lineNo The current line no.
	 */
	private void parseLine(String line, int lineNo)
	{
		data.put(lineNo, stringToList(line));
		numColumns = 0;
	}
	
	/**
	 * Split a csv string into a list.
	 * @param line The line of text.
	 * @return The list.
	 */
	private List<String> stringToList(String line)
	{
		String[] splitList = line.split(".");
		List<String> row = new ArrayList<String>();
		for(String element: splitList)
			row.add(element);
		numColumns = Math.max(numColumns, row.size());
		return row;
	}
	
	/** 
	 * Initialise the data structures.
	 */
	private void initData()
	{
		data = new LinkedHashMap<Integer, List<String>>();
	}
	
	/**
	 * Read the parsed file into a tableModel.
	 * @param table See above.
	 */
	public void readToTable(TableModel table)
	{
		for(int i = 0 ; i < data.size(); i++)
			for(int j = 0 ; j < numColumns; j++)
				table.setValueAt(getValue(i,j), i, j);
	}
	
	/**
	 * Get the value in the file at location, row, col.
	 * @param rowNum See above.
	 * @param col See above.
	 * @return See above.
	 */
	public String getValue(int rowNum, int col)
	{
		List<String> row = getRow(rowNum);
		return row.get(col);
	}
	
	/**
	 * The numer of rows in the file.
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
	public int getNumColunns()
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
