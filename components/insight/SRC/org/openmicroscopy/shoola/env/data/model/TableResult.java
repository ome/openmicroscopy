/*
 * org.openmicroscopy.shoola.env.data.model.TableResult
 *
 *------------------------------------------------------------------------------
 * Copyright (C) 2006-2009 University of Dundee. All rights reserved.
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.env.data.model;

//Java imports

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

	/** The id of the table hosting the result. */
	private long tableID;
	
	/** The columns. */
	private String[] columns;
	
	/** The description of the columns. */
	private String[] columnsDescription;
	
	/** The data to display. */
	private Object[][] data;
	
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
