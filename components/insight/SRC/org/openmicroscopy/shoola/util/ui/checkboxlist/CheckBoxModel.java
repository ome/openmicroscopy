/*
* org.openmicroscopy.shoola.util.ui.checkboxlist.CheckBoxModel
*
*------------------------------------------------------------------------------
*  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
*
*
*  This program is free software; you can redistribute it and/or modify
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


//Java imports
package org.openmicroscopy.shoola.util.ui.checkboxlist;

//Java imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.table.AbstractTableModel;

//Third-party libraries

//Application-internal dependencies

/**
 *
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class CheckBoxModel
	extends AbstractTableModel
{

	/** The list of strings associated with the checkbox. */
	List<String> 				list;
	
	/** The map of true false values for the checkbox. */
	HashMap<Integer, Boolean> 	selected;
	
	/**
	 * Constructor for the table model, sets the values for the check boxes to
	 * be equal to the hashmap selected.
	 * @param list See above.
	 * @param selected See above.
	 */
	public CheckBoxModel(List<String> list, HashMap<Integer, Boolean> selected)
	{
		this.list = list;
		this.selected = selected;	
		if (selected == null)
		{
			this.selected = new HashMap<Integer, Boolean>();
			for(int i = 0 ; i < list.size() ; i++)
				this.selected.put(i,false);
		}
	}
	
	/**
	 * Constructor for tableModel, sets the values of the check boxes for the 
	 * list to false by default.
	 * @param list
	 */
	public CheckBoxModel(List<String> list)
	{
		this(list, null);
	}
	
	/**
	 * Default constructor for table model.
	 */
	public CheckBoxModel()
	{
		this(new ArrayList<String>(), null);
	}

	/**
	 * Overridden 
	 * 
	 * @see AbstractTableModel#getValueAt(int ,int)
	 */
	public int getColumnCount()
	{
		return 2;
	}

	/**
	 * Overridden 
	 * 
	 * @see AbstractTableModel#getValueAt(int ,int)
	 */
	public int getRowCount()
	{
		return list.size();
	}

	/**
	 * Overridden 
	 * 
	 * @see AbstractTableModel#getValueAt(int ,int)
	 */
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		switch(columnIndex)
		{
			case 0:
				return list.get(rowIndex);
			case 1:
				return selected.get(rowIndex);
			default:
				throw new IllegalArgumentException("Table does not have Row : " + 
						rowIndex + " and Column : " + columnIndex);
		}
	}

	/**
	 * Overridden 
	 * 
	 * @see AbstractTableModel#setValueAt(Object ,int ,int)
	 */
	public void setValueAt(Object value, int row, int col)
	{
		switch(col)
		{
			case 1:
				selected.put(row, (Boolean)value);
				this.fireTableCellUpdated(row, col);
			default:
				break;
		}
	}

}
