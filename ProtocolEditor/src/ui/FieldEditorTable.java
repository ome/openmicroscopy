/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
 *	author Will Moore will@lifesci.dundee.ac.uk
 */

package ui;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JOptionPane;
import javax.swing.text.JTextComponent;

import tree.DataField;

public class FieldEditorTable extends FieldEditor {
	
	AttributeMemoEditor tableColumnsEditor;
	
	public FieldEditorTable (DataField dataField) {
		
		super(dataField);
	
		//	 comma-delimited list of options
		String tableColumns = dataField.getAttribute(DataField.TABLE_COLUMN_NAMES);
		
		// attributeMemoEditor has listener to update dataField - and adds change to undo-redo.
		tableColumnsEditor = new AttributeMemoEditor
			("Columns: (separate with commas)", DataField.TABLE_COLUMN_NAMES, tableColumns);
		//tableColumnsEditor.removeFocusListener();
		//tableColumnsEditor.getTextArea().addFocusListener(new TableColumnsFocusListener());
		
		tableColumnsEditor.setToolTipText("Add columns names, separated by commas");
		tableColumnsEditor.setTextAreaRows(4);
		attributeFieldsPanel.add(tableColumnsEditor);	
	}
	
	public class TableColumnsFocusListener implements FocusListener {
		
		public void focusLost(FocusEvent event) {
			if (textChanged) {
				JTextComponent source = (JTextComponent)event.getSource();
				
				String newColumnNames = source.getText();
				String[] newCols = newColumnNames.split(",");
				
				String oldColumnNames = dataField.getAttribute(DataField.TABLE_COLUMN_NAMES);
				if (oldColumnNames != null) {
					String[] oldCols = oldColumnNames.split(",");
				
					int deletedCols = oldCols.length - newCols.length;
				
					if (deletedCols > 0) {
						int confirm = JOptionPane.showConfirmDialog(tableColumnsEditor, 
								"You have removed " + deletedCols + (deletedCols == 1? " column":" columns" ) + " from the table.\n" +
								"This will delete all the data in " + (deletedCols == 1? "this column":"these columns.") + "\n" +
								"Are you sure you want to continue?", "Delete Columns?", JOptionPane.OK_CANCEL_OPTION);
						if (confirm != JOptionPane.OK_OPTION) {
							// user has canceled update, simple reset textBox
							source.setText(oldColumnNames);
							textChanged = false;
							return;
						}
					} 
				} 
				// the user didn't get asked, or chose not to cancel, therefore update!
				dataField.setAttribute(source.getName(), source.getText(), true);
				
				textChanged = false;
			}
		}
		public void focusGained(FocusEvent event) {}
	}
	
	
	// overrides this method in the AbstractDataFieldPanel,
	// so as to ask for confirmation before updating dataField with fewer columns (lose data)!
	protected void setDataFieldAttribute(String attributeName, String value, boolean notifyUndoRedo) {
		
		// if not planning to update TABLE_COLUMN_NAME attribute, just setAttribute (eg Name, Description..)
		if (!attributeName.equals(DataField.TABLE_COLUMN_NAMES)) {
			super.setDataFieldAttribute(attributeName, value, true);
			return;
		}
		
		String newColumnNames = value;
		String[] newCols = newColumnNames.split(",");
		
		String oldColumnNames = dataField.getAttribute(DataField.TABLE_COLUMN_NAMES);
		if (oldColumnNames != null) {
			String[] oldCols = oldColumnNames.split(",");
		
			int deletedCols = oldCols.length - newCols.length;
		
			if (deletedCols > 0) {
				int confirm = JOptionPane.showConfirmDialog(tableColumnsEditor, 
						"You have removed " + deletedCols + (deletedCols == 1? " column":" columns" ) + " from the table.\n" +
						"This will delete all the data in " + (deletedCols == 1? "this column":"these columns.") + "\n" +
						"Are you sure you want to continue?", "Delete Columns?", JOptionPane.OK_CANCEL_OPTION);
				if (confirm != JOptionPane.OK_OPTION) {
					// user has canceled update, simply reset textBox
					tableColumnsEditor.getTextArea().setText(oldColumnNames);
					textChanged = false;
					return;
				}
			} 
		} 
		// the user didn't get asked, or chose not to cancel, therefore update!
		dataField.setAttribute(attributeName, value, true);
		
	}

}
