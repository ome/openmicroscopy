/*
 * org.openmicroscopy.shoola.util.ui.login.ServerTable 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.ui.login;


//Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;


//Third-party libraries

//Application-internal dependencies

/** 
 * A customized table displaying the available servers.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class ServerTable 
	extends JTable
{

	/** The default color of the grid. */
	private static final Color LINE_COLOR = Color.LIGHT_GRAY;
	
	/** The height and width added to the icon. */
	private static final int	INDENT = 4;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param servers 	Collection of servers.
	 * @param icon		The icon to display netx to the server's name.
	 */
	ServerTable(List servers, Icon icon)
	{	
		String[] columnNames = {"", ""};
		final Object[][] objects;
		if (servers == null || servers.size() == 0) {
			objects = new Object[1][2];
			objects[0][0] = icon;
			objects[0][1] = "";
		} else {
			objects = new Object[servers.size()][2];
			Iterator i = servers.iterator();
			int j = 0;
			while (i.hasNext()) {
				objects[j][0] = icon;
				objects[j][1] = i.next();
				j++;
			}
		}
		putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setModel(new ServerTableModel(objects, columnNames));
		setDefaultRenderer(Object.class, new ServerListRenderer());
		setRowHeight(icon.getIconHeight()+INDENT);
		setShowHorizontalLines(true);
		setShowVerticalLines(false);
		setTableHeader(null);
		setGridColor(LINE_COLOR);
		Dimension d = getIntercellSpacing();
		setIntercellSpacing(new Dimension(0, d.height));
		TableColumn column = getColumnModel().getColumn(0);
		int n = icon.getIconWidth()+INDENT;
		column.setMaxWidth(n);
		column.setMinWidth(n);
		addMouseListener(new MouseAdapter() {
		
			/**
			 * Edits the cell only when the use double-clicks on a row.
			 * 
			 * @param e The mouse event to digest.
			 */
			public void mousePressed(MouseEvent e)
			{
				TableCellEditor editor = getCellEditor();
				if (editor != null) editor.stopCellEditing();
				if (e.getClickCount() == 2) {
					int r = getSelectedRow();
					int c = getSelectedColumn();
					editCellAt(r, c);
					//setEditingRow(r);
					//setEditingColumn(c);
					repaint();
				}
			}
		});
	}
	
	/** 
	 * Overridden to set the focus on the edited cell.
	 * @see JTable#changeSelection(int, int, boolean, boolean)
	 */
	public void changeSelection(final int row, final int column, boolean toggle, 
								boolean extend)
	{
		super.changeSelection(row, column, toggle, extend);
	 
		if (editCellAt(row, column)) {
			getEditorComponent().requestFocusInWindow();
		}
	}
	
	/**
	 * Inner class used to override the 
	 * {@link DefaultTableModel#isCellEditable(int, int)} method.
	 */
	class ServerTableModel 
		extends DefaultTableModel
	{
		
	    /**
	     *  Constructs a <code>DefaultTableModel</code> and initializes the 
	     *  table by passing <code>data</code> and <code>columnNames</code>
	     *  to the <code>setDataVector</code>
	     *  method. The first index in the <code>Object[][]</code> array is
	     *  the row index and the second is the column index.
	     *
	     * @param data			The data of the table.
	     * @param columnNames	The names of the columns.
	     */
	    public ServerTableModel(Object[][] data, Object[] columnNames)
	    {
	        super(data, columnNames);
	    }
	    
		/**
		 * Overridden so that only the cells displaying the server's name
		 * are editable.
		 * @see DefaultTableModel#isCellEditable(int, int)
		 */
	    public boolean isCellEditable(int row, int column)
	    {
	        return (column != 0);
	    }
	}

}
