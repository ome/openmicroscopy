 /*
 * treeModel.fields.TableParam 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.editor.model.params;

//Java imports

import javax.swing.table.TableModel;

import org.openmicroscopy.shoola.agents.editor.model.tables.MutableTableModel;

//Third-party libraries

//Application-internal dependencies

/** 
 * This is a Parameter that holds table data.
 * It delegates the table data itself to a {@link TableModel}. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class TableParam 
	extends AbstractParam {
	
	/**
	 * The table model that holds the data. 
	 */
	private TableModel 				tableModel;
	
	/**
	 * A String to define the table parameter
	 */
	public static final String 		TABLE_PARAM = "TABLE";
	
	/**
	 * Creates an instance. 
	 * Instantiates the table model.
	 * 
	 * @param fieldType		The String defining the field type
	 */
	public TableParam(String fieldType) 
	{
		super(fieldType);
		
		tableModel = new MutableTableModel();
	}
	
	/**
	 * Gets a reference to the table model.
	 * 
	 * @return		see above.
	 */
	public TableModel getTableModel() { return tableModel; }

	/**
	 * Returns an empty array. No value attributes. 
	 * 
	 * @see AbstractParam#getParamAttributes()
	 */
	public String[] getParamAttributes() { return new String[0]; }

	/**
	 * Returns true if there is at least one row of table data.
	 * 
	 * @see AbstractParam#isParamFilled()
	 */
	public boolean isParamFilled() {
		return tableModel.getRowCount() > 0;
	}
	
	/**
	 * Returns a comma-delimited list of the column names. 
	 * 
	 * @see Object#toString()
	 */
	public String toString() 
	{
		String text = "";
		int cols = tableModel.getColumnCount();
		
		for (int c=0; c<cols; c++) 
		{
			if (c >0) text = text + ", ";
			text = text + tableModel.getColumnName(c);
		}
		
		return super.toString() + " " + text;
	}

	/**
	 * Overridden to include copying of the {@link #tableModel};
	 * 
	 * @see AbstractParam#cloneParam();
	 */
	public IParam cloneParam() 
	{
		IParam param = super.cloneParam();
		
		TableModel newTM = new MutableTableModel(tableModel);
		((TableParam)param).tableModel = newTM;
		
		return param;
	}
}
