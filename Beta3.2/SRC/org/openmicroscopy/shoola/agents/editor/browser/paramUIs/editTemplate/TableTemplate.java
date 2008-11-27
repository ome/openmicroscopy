 /*
 * org.openmicroscopy.shoola.agents.editor.browser.paramUIs.editTemplate.TableTemplate 
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
package org.openmicroscopy.shoola.agents.editor.browser.paramUIs.editTemplate;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JOptionPane;
import javax.swing.table.TableModel;

import org.openmicroscopy.shoola.agents.editor.browser.paramUIs.AbstractParamEditor;
import org.openmicroscopy.shoola.agents.editor.browser.paramUIs.ITreeEditComp;
import org.openmicroscopy.shoola.agents.editor.model.IAttributes;
import org.openmicroscopy.shoola.agents.editor.model.params.MutableTableModel;
import org.openmicroscopy.shoola.agents.editor.model.params.TableParam;

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
public class TableTemplate 
	extends AbstractParamEditor
	implements PropertyChangeListener
{
	private AttributeEditArea 			colsFieldEditor;
	
	private MutableTableModel 					tableModel;
	
	public static final String 			TABLE_COLS = "tableCols";
	
	private void buildUI()
	{
		IAttributes param = getParameter();
		// A text box to display and edit the list of columns
		colsFieldEditor = new AttributeEditArea(param, 
					TABLE_COLS, 
					"Table columns: separate with commas");
		colsFieldEditor.addPropertyChangeListener
				(ITreeEditComp.VALUE_CHANGED_PROPERTY, this);
		colsFieldEditor.setToolTipText("Table columns, separated by commas");
		
		add(colsFieldEditor);
		
		if (param instanceof TableParam) {
			TableParam tableParam = (TableParam)param;
			TableModel tm = tableParam.getTableModel();
			if (tm instanceof MutableTableModel) {
				tableModel = (MutableTableModel)tm;
			}
			setTableColsText();
		}
	}
	
	/**
	 * Sets the text of the editor, based on the names of columns in ]
	 * the table model. 
	 */
	private void setTableColsText()
	{
		if (tableModel == null) return;
		
		int cols = tableModel.getColumnCount();
		
		String colsString = "";
		for (int c=0; c<cols; c++) {
			if (c > 0) colsString = colsString + ", ";
			colsString = colsString + tableModel.getColumnName(c);
		}
		colsFieldEditor.getTextArea().setText(colsString);
	}
	
	/**
	 * Creates an instance.
	 * 
	 * @param param		The paramter to edit.
	 */
	public TableTemplate(IAttributes param) 
	{
		super(param);
		
		buildUI();
	}

	public void propertyChange(PropertyChangeEvent evt) {
		
		if (evt.getSource().equals(colsFieldEditor)) {
			
			String newC = evt.getNewValue().toString();
			
			String[] newCols = newC.split(",");
			
			// get the number of cols right
			int newColCount = newCols.length;
			int oldColCount = tableModel.getColumnCount();
			
			int deletedCols = oldColCount - newColCount;
			
			if (deletedCols > 0) {
				int confirm = JOptionPane.showConfirmDialog(this, 
						"You have removed " + deletedCols + 
						(deletedCols == 1? " column":" columns" ) + 
						" from the table.\n" +
						"This will delete all the data in " + 
						(deletedCols == 1? "this column":"these columns.") + 
						"\n" +
						"Are you sure you want to continue?", 
						"Delete Columns?", JOptionPane.OK_CANCEL_OPTION);
				if (confirm != JOptionPane.OK_OPTION) {
					// user has canceled update, simply reset textBox
					setTableColsText();
					return;
				}
				// need to delete extra columns
				for (int i=0; i< deletedCols; i++) {
					tableModel.removeLastColumn();
				}
			} 
			
			// Sets the name of existing columns, or adds new ones if needed.
			for (int c=0; c<newColCount; c++) {
				tableModel.setColumnName(c, newCols[c].trim());
			}
		}
	}

	/**
	 * Implemented as specified by the {@link ITreeEditComp} interface. 
	 * 
	 * @see {@link ITreeEditComp#getEditDisplayName()
	 */
	public String getEditDisplayName() { return "Table Columns"; }

}
