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

import javax.swing.table.TableModel;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * 
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
	
	TableModel tableModel;
	
	public static final String TABLE_PARAM = "tableParam";
	
	/**
	 * Creates an instance. 
	 * 
	 * @param fieldType		The String defining the field type
	 */
	public TableParam(String fieldType) {
		super(fieldType);
		
		tableModel = new MutableTableModel();
	}
	
	public TableModel getTableModel() {
		return tableModel;
	}

	@Override
	public String[] getValueAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isParamFilled() {
		return tableModel.getRowCount() > 0;
	}
	
	/**
	 * Returns a comma-delimited list of the column names. 
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
		
		return text;
	}

}
