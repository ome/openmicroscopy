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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

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
	private static final Color	LINE_COLOR = Color.LIGHT_GRAY;
	
	/** The height and width added to the icon. */
	private static final int	INDENT = 4;
	
	/** The default size of an icon. */
	private static final int	DEFAULT_ICON_SIZE = 22;
	
	/** Index of the previously selected row. */
	private int				previousRow;
	
	/** Reference to the parent. */
	private ServerEditor 	parent;

	/** Flag indicating a selection not done by a user. */
	private boolean			manual;
	
	/** Handles the mouse pressed event. */
	private void handleClickCount()
	{
		TableCellEditor editor = getCellEditor();
		if (editor != null) editor.stopCellEditing();
	}
	
	/**
	 * Initializes the table.
	 * 
	 * @param servers   The existing servers.
	 * @param icon		The icon used.
	 */
	private void initComponents(Map<String, String> servers, Icon icon)
	{
		String[] columnNames = {"icon", "host", "port"};
		final Object[][] objects;
		Boolean focus = Boolean.valueOf(true);
		if (servers == null || servers.size() == 0) {
			objects = new Object[1][3];
			objects[0][0] = icon;
			objects[0][1] = "";
			objects[0][2] = parent.getDefaultPort();
			focus = Boolean.valueOf(false);
		} else {
			objects = new Object[servers.size()][3];
			Iterator<Entry<String, String>> i = servers.entrySet().iterator();
			int j = 0;
			String s;
			Entry<String, String> entry;
			while (i.hasNext()) {
				entry = i.next();
				s = entry.getKey();
				objects[j][0] = icon;
				objects[j][1] = s;
				objects[j][2] = entry.getValue();
				j++;
			}
		}
		int w = DEFAULT_ICON_SIZE;
		int h = DEFAULT_ICON_SIZE;
		if (icon != null) {
			w = icon.getIconWidth();
			h = icon.getIconHeight();
		}
		focus = Boolean.valueOf(false);
		putClientProperty("terminateEditOnFocusLost", focus);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setModel(new ServerTableModel(objects, columnNames));
		ServerListRenderer rnd = new ServerListRenderer();
		setDefaultRenderer(Object.class, rnd);
		setRowHeight(h+INDENT);
		setShowHorizontalLines(true);
		setShowVerticalLines(false);
		setTableHeader(null);
		setGridColor(LINE_COLOR);
		Dimension d = getIntercellSpacing();
		setIntercellSpacing(new Dimension(0, d.height));
		TableColumnModel tcm = getColumnModel();
		TableColumn column = tcm.getColumn(0);
		int n = w+INDENT;
		column.setMaxWidth(n);
		column.setMinWidth(n);
		column = tcm.getColumn(1);
		column.setCellEditor(new ServerListEditor(this));
		addMouseListener(new MouseAdapter() {
		
			/**
			 * Edits the cell only when the use double-clicks on a row.
			 * 
			 * @param e The mouse event to digest.
			 */
			public void mousePressed(MouseEvent e) { handleClickCount(); }
		});
		
		int width = rnd.getFontMetrics(rnd.getFont()).stringWidth(
				""+ServerEditor.MAX_PORT);
		width += width/2;
		column = tcm.getColumn(2);
		column.setMaxWidth(width);
		column.setMinWidth(width);
		column.setCellEditor(new ServerListEditor(this));
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parent	Reference to the model. Mustn't be <code>null</code>.
	 * @param servers 	Collection of servers.
	 * @param icon		The icon to display next to the server's name.
	 */
	ServerTable(ServerEditor parent, Map<String, String> servers, Icon icon)
	{	
		if (parent == null)
			throw new IllegalArgumentException("No model");
		this.parent = parent;
		previousRow = -1;
		manual = false;
		setName("server table");
		initComponents(servers, icon);
	}
	
	/** 
	 * Invokes when a new server name is entered or
	 * an existing one is edited.
	 */
	void handleKeyEnter()
	{
		parent.firePropertyChange(ServerEditor.APPLY_SERVER_PROPERTY, 
				Boolean.FALSE, Boolean.TRUE);
	}
	
	/**
	 * Handles the text modification in the edited cell.
	 * 
	 * @param text The textual value to handle.
	 */
	void handleTextModification(String text)
	{
		if (!parent.isEditing()) return;
		int m = getSelectedRow();
		List<String> values = new ArrayList<String>();
		for (int i = 0; i < getRowCount(); i++) {
			if (i != m) values.add((String) getValueAt(i, 1)); 
		}
		String server = parent.getActiveServer();
		if (server != null && !values.contains(server))
			values.add(server);
		Iterator j = values.iterator();
		String name;
		boolean found = false; 
		while (j.hasNext()) {
			name = (String) j.next();
			if (name.equals(text)) {
				found = true;
				break;
			}
		}
		parent.showMessagePanel(found);
	}
	
	/**
	 * Forwards call to the parent.
	 * 
	 * @param text The entered text.
	 */
	void finishEdition(String text) { parent.finishEdition(text); }
	
	/**
	 * Removes the specified row.
	 * 
	 * @param row The row to remove.
	 */
	void removeRow(int row)
	{
		DefaultTableModel model = (DefaultTableModel) getModel();
		int n = model.getRowCount();
		if (row >= n) return;
		model.removeRow(row);
		previousRow = row-1;
	}
	
	/**
	 * Sets the flag indicating that the selection does not 
	 * corresponds to a user's action.
	 * 
	 * @param manual Pass <code>true</code> to indicate that it does not 
	 * 				 correspond to a user's action, <code>false</code>
	 * 				 otherwise.
	 */
	void setManual(boolean manual) { this.manual = manual; }
	
	/** 
	 * Overridden to set the focus on the edited cell.
	 * @see JTable#changeSelection(int, int, boolean, boolean)
	 */
	public void changeSelection(final int row, final int column, boolean toggle, 
								boolean extend)
	{
		super.changeSelection(row, column, toggle, extend);
		if (manual) return;
		String v = null;
		DefaultTableModel model = ((DefaultTableModel) getModel());
		if (row != previousRow && row >= 0 && previousRow != -1) {
			if (model.getColumnCount() < 3) return; 
			if (previousRow < model.getRowCount())
				v = (String) model.getValueAt(previousRow, 1);
			TableCellEditor editor = getCellEditor();
			if (editor != null) editor.stopCellEditing();
			if (v == null || v.trim().length() == 0) v = null;
		}
		if (row >= 0 && model.getColumnCount() == 3) {
			handleTextModification((String) model.getValueAt(row, 1));
		}
		parent.changeSelection(row, previousRow, v);
		previousRow = row;
		//if (parent.isEditing()) {//if (editCellAt(row, column)) {
		if (editCellAt(row, column)) {
			Component comp = getEditorComponent();
			if (comp != null) comp.requestFocusInWindow();
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
		 * can be edited.
		 * @see DefaultTableModel#isCellEditable(int, int)
		 */
	    public boolean isCellEditable(int row, int column)
	    {
	        return (column != 0);
	    }
	}

}
