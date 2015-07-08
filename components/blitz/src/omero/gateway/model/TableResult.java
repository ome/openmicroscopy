/*
 * org.openmicroscopy.shoola.env.data.model.TableResult
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
package omero.gateway.model;


//Java imports
import java.util.Map;

//Third-party libraries

//Application-internal dependencies

/**
 * Store the element of the py-tables.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class TableResult
{

	/** Identifies the index of the <code>ROI</code> column. */
	public static final Integer ROI_COLUMN_INDEX = 0;
	
	/** Identifies the index of the <code>Image</code> column. */
	public static final Integer IMAGE_COLUMN_INDEX = 1;
	
	/** Identifies the index of the <code>Well</code> column. */
	public static final Integer WELL_COLUMN_INDEX = 2;
		
	/** The id of the table hosting the result. */
	private long tableID;
	
	/** The columns. */
	private String[] columns;
	
	/** The description of the columns. */
	private String[] columnsDescription;
	
	/** The data to display. */
	private Object[][] data;
	
	/** The indexes of the column. */
	private Map<Integer, Integer> indexes;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param data		The data contained in the py-table.
	 * @param columns 	The name of the columns.
	 * @param columnsDescription The description of the columns.
	 */
	public TableResult(Object[][] data, String[] columns)
	{
		this(data, columns, null);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param data		The data contained in the py-table.
	 * @param columns 	The name of the columns.
	 * @param columnsDescription
	 */
	public TableResult(Object[][] data, String[] columns, 
			String[] columnsDescription)
	{
		this.data = data;
		this.columns = columns;
		this.columnsDescription = columnsDescription;
	}
	
	/**
	 * Returns the index of the column corresponding to the specified value.
	 * One of the constants defined by this class.
	 * 
	 * @param index One of the constants defined by this class.
	 * @return See above.
	 */
	public int getColumnIndex(Integer index)
	{ 
		if (indexes == null || index == null) return -1;
		Integer value = indexes.get(index);
		if (value == null) return -1;
		return value.intValue(); 
	}

	/**
	 * Sets the indexes.
	 * 
	 * @param indexes The value to set.
	 */
	public void setIndexes(Map<Integer, Integer> indexes)
	{
		this.indexes = indexes;
	}
	
	/**
	 * Sets the id of the table.
	 * 
	 * @param tableID The value to set.
	 */
	public void setTableID(long tableID) { this.tableID = tableID; }
	
	/**
	 * Returns the id of the table.
	 * 
	 * @return See above.
	 */
	public long getTableID() { return tableID; }
	
	/**
	 * Returns the name of the columns.
	 * 
	 * @return See above.
	 */
	public String[] getHeaders() { return columns; }
	
	/**
	 * Returns the name of the columns.
	 * 
	 * @return See above.
	 */
	public String[] getHeadersDescription() { return columnsDescription; }
	
	/**
	 * Returns the data.
	 * 
	 * @return See above.
	 */
	public Object[][] getData() { return data; }

}
