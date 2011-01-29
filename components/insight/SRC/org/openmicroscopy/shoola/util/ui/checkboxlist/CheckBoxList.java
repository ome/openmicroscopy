/*
* org.openmicroscopy.shoola.util.ui.checkboxlist.CheckBoxList
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
package org.openmicroscopy.shoola.util.ui.checkboxlist;


//Java imports
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

//Third-party libraries

//Application-internal dependencies

/**
 * Table with 2 columns, the second one is the check box.
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
public class CheckBoxList
	extends JTable
{
	
	/** Reference to the renderer. */
	private CheckBoxRenderer renderer;
	
	/** Installs the listeners. */
	private void installListeners()
	{
		addMouseListener(new MouseAdapter() 
		{
			public void mousePressed(MouseEvent e) 
			{
				if (getSelectedColumn() == 1)	
					toggleValue();
			}
		});
	}

	/** Toggles the value of the boolean under the current selection. */
	private void toggleValue()
	{
		int col = getSelectedColumn();
		int row = getSelectedRow();
		Object v = getModel().getValueAt(row, col);;
		boolean value = Boolean.valueOf(false);
		if (v != null) value = (Boolean) v;
		getModel().setValueAt(!value, row, col);
		repaint();
	}
	
	/**
	 * Creates a new instance.
	 *  
	 * @param model See above.
	 */
	public CheckBoxList(CheckBoxModel model)
	{
		super(model);
		installListeners();
	}
	
	/**
	 * Default constructor.
	 */
	CheckBoxList()
	{
		this(new CheckBoxModel());
	}
	
	/**
	 * Default Cell editor, This takes the row and column for the default editor.
	 * We require this as it stops the disconnection between Renderer and Editor.
	 * 
	 * @param row The row of the cell.
	 * @param col The column of the cell.
	 */
	public DefaultCellEditor getCellEditor(int row, int col)
	{
		CheckBoxRenderer renderer = (CheckBoxRenderer) 
			getCellRenderer(row, col);
		return new DefaultCellEditor((JCheckBox) renderer.
				getTableCellRendererComponent(this,
					getValueAt(row, col), false, false, row, col));
	}

	/**
	 * Overridden to return a customized cell renderer.
	 * @see JTable#getCellRenderer(int, int)
	 */
	public TableCellRenderer getCellRenderer(int row, int column) 
	{
		if (renderer == null) renderer = new CheckBoxRenderer();
        return renderer;
    }

	/**
	 * Returns all the values that have been selected as <code>true</code>.
	 * 
	 * @return See above.
	 */
	public List<String> getTrueValues()
	{
		List<String> values = new ArrayList<String>();
		for (int row = 0 ;row < getModel().getRowCount() ; row++)
			if ((Boolean) getValueAt(row, 1))
				values.add((String) getValueAt(row, 0));
		return values;
	}
	
	/**
	 * Sets to <code>true</code> the values in the list.
	 * 
	 * @param values See above.
	 */
	public void setTrueValues(List<String> values)
	{
		int row;
		for (String value : values)
		{
			row = findValue(value);
			if (row == -1)
				continue;
			getModel().setValueAt(true, row, 1);
		}
	}
	
	/**
	 * Finds the index corresponding to the specified value.
	 * 
	 * @param value See above.
	 * @return See above.
	 */
	public int findValue(String value)
	{
		for (int i = 0 ; i < getRowCount(); i++)
			if (value.equals(getModel().getValueAt(i, 0)))
				return i;
		return -1;
	}
	
}
