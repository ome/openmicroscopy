 /*
 * org.openmicroscopy.shoola.agents.editor.uiComponents.TableEditUI 
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
package org.openmicroscopy.shoola.agents.editor.uiComponents;

//Java imports

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.IconManager;
import org.openmicroscopy.shoola.agents.editor.browser.FieldPanel;
import org.openmicroscopy.shoola.agents.editor.model.tables.IMutableTableModel;

/** 
 * A UI for displaying a table, with buttons for adding and removing rows. 
 * These will be disabled unless the tableModel is mutable. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class TableEditUI 
	extends JPanel 
	implements ActionListener {
	
	/**
	 * The JTable used to display the table data.
	 */
	private JTable 				table;
	
	/**
	 * The tableModel. Obtained from the Table Parameter object. 
	 */
	private TableModel 			tableModel;
	
	/**
	 * Button for adding a row. 
	 */
	private JButton 			addRowButton;
	
	/**
	 * Button for removing selected rows. 
	 */
	private JButton 			removeRowsButton;
	
	/**
	 * Action Command for addRowButton.
	 */
	public static final String 	ADD_ROW = "addRow";
	
	/**
	 * Action Command for removeRowsButton
	 */
	public static final String 	REMOVE_ROW = "removeRow";

	/**
	 * A bound property to indicate that the table needs to resize
	 */
	public static final String 	SIZE_CHANGED = "sizeChanged";
	
	
	/**
	 * Initialises the UI components
	 */
	private void initialise()
	{
		if (table == null) {
			table = new JTable(tableModel);
		}
		
		//Buttons for adding or removing rows.
		IconManager iM = IconManager.getInstance();
		Icon addRowIcon = iM.getIcon(IconManager.NEW_ROW_ICON);
        Icon clearRowIcon = iM.getIcon(IconManager.CLEAR_ROW_ICON);
        addRowButton = new CustomButton(addRowIcon);
        addRowButton.setToolTipText("Add a new row");
        addRowButton.setActionCommand(ADD_ROW);
        addRowButton.addActionListener(this);
        removeRowsButton = new CustomButton(clearRowIcon);
        removeRowsButton.setToolTipText("Remove the highlighted rows");
        removeRowsButton.setActionCommand(REMOVE_ROW);
        removeRowsButton.addActionListener(this);
        
        // If the tableModel is not mutable, disable the edit buttons.
        if (! (tableModel instanceof IMutableTableModel)) {
        	addRowButton.setEnabled(false);
        	removeRowsButton.setEnabled(false);
        }
	}
	
	/**
	 * Builds the UI.
	 * Puts the JTable in a ScrollPane, so that the column names are displayed.
	 * Adds buttons for adding/removing rows. 
	 */
	private void buildUI() 
	{ 
		setLayout(new BorderLayout());
		setBackground(null);
		
		// Scrollbars are never shown because the scroll port always resizes
		// to show the whole table.
		JScrollPane tableScroller = new JScrollPane(table, 
        		JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
        		JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
       
        Box verticalBox = Box.createVerticalBox();
        verticalBox.add(addRowButton);
        verticalBox.add(removeRowsButton);
		
        this.add(verticalBox, BorderLayout.WEST);
		this.add(tableScroller, BorderLayout.CENTER);
		
		// Resize the viewport to show the whole table.
		refreshViewportSize();
	}
	
	/**
	 * Sets the size of the scrollPane's view-port, based on the number of
	 * rows in the table. 
	 */
	private void refreshViewportSize() 
	{
		int rows = table.getRowCount();
		int cols = table.getColumnCount();
		int height = rows * table.getRowHeight();
		int width = cols * 120;
		table.setPreferredScrollableViewportSize(new Dimension(width, height));
	}
	
	/**
	 * Fires a PropertyChange for FieldPanel.UPDATE_EDITING_PROPERTY
	 * so that the size of this panel is refreshed, and editing continues...
	 */
	private void refreshEditingSize() 
	{
		// Need to resize...
		firePropertyChange(SIZE_CHANGED, null, null);
	}
	
	public TableEditUI(TableModel tableModel) 
	{
		this.tableModel = tableModel;
		
		initialise();
		buildUI();
	}
	
	public TableEditUI(JTable table) 
	{
		this.table = table;
		tableModel = table.getModel();
		
		initialise();
		buildUI();
	}
	
	/**
	 * Need to override this method for the JPanel to fix a bug with 
	 * resizing. 
	 * Otherwise when the JTable gains focus, it makes this panel bigger.
	 * This is AFTER the JTree that displays this field has called 
	 * getPreferredSize(). 
	 * Therefore, if the field is selected, this panel is too big for 
	 * the field, and overlaps surrounding components. 
	 * 
	 * @see JComponent#getPreferredSize()
	 */
	public Dimension getPreferredSize() 
	{	
		Dimension size = super.getPreferredSize();
		
		int width = (int)size.getWidth();
		int height = (table.getRowCount() + 2) * table.getRowHeight();
		
		size.setSize(width, height);
		return size;
	}
	
	/**
	 * The handler for the Add-Row and Remove-Rows buttons. 
	 * 
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) 
	{
		String com = e.getActionCommand();
		
		/*
		 * Handle the Add-Row Action.
		 */
		if (ADD_ROW.equals(com)) {
			/*
			 * Add a row after the last selected row. 
			 */
			/* if it's a mutable table model, can edit... */
			if (tableModel instanceof IMutableTableModel) {
				IMutableTableModel mtm = (IMutableTableModel)tableModel;
			
				int selected = table.getSelectedColumnCount();
				if (selected == 0) {
					mtm.addEmptyRow();
				} else {
					int rows[] = table.getSelectedRows();
					int lastSelectedIndex = rows[selected -1];
					mtm.addEmptyRow(lastSelectedIndex +1);
				}
				// refresh new size - causes new panel to be created. 
				refreshEditingSize();
			}
			
		/*
		 * Handle the Remove-Row Action.
		 */
		} else if (REMOVE_ROW.equals(com)) {
			
			/* if it's a mutable table model, can edit... */
			if (tableModel instanceof IMutableTableModel) {
				IMutableTableModel mtm = (IMutableTableModel)tableModel;
			
			
				int delete = JOptionPane.showConfirmDialog(table,
					"Are you sure you want to delete rows?\n" +
					"This cannot be undone.", "Really delete rows?", 
					JOptionPane.OK_CANCEL_OPTION);
				if (delete == JOptionPane.OK_OPTION) {
					int[] highlightedRows = table.getSelectedRows();
					mtm.removeRows(highlightedRows);
					// refresh new size - causes new panel to be created. 
					refreshEditingSize();
				}
			}
		}
		// used to notify parents that the panel needs different size. 
		firePropertyChange(FieldPanel.UPDATE_EDITING_PROPERTY, 
				null, getPreferredSize());
	}
	
	/**
	 * Returns the table. 
	 * 
	 * @return
	 */
	public JTable getTable()
	{
		return table;
	}
}
