/*
 * org.openmicroscopy.shoola.agents.measurement.util.ui.FigureTable 
 *
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
 */
package org.openmicroscopy.shoola.agents.measurement.util.ui;


//Java imports
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.util.model.AttributeField;
import org.openmicroscopy.shoola.agents.measurement.util.model.FigureTableModel;
import org.openmicroscopy.shoola.agents.measurement.util.model.ValueType;
import org.openmicroscopy.shoola.util.ui.NumericalTextField;

/** 
 * Displays the figures in a table.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class FigureTable
	extends JTable
	implements DocumentListener
{
	
	/** Indicates that a value has been modified in text field.*/
	public static final String VALUE_CHANGED_PROPERTY = "valueChanged";
	
	/** The model for the table. */
	private FigureTableModel tableModel;
	
	/** The renderer.*/
	private TableCellRenderer renderer;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param model The model used by this table.
	 */
	public FigureTable(FigureTableModel model)
	{
		super(model);
		tableModel = model;
	}
	
	/**
	 * Returns the Field at the specified row.
	 * 
	 * @param row The selected row.
	 * @return See above.
	 */
	public AttributeField getFieldAt(int row)
	{
		return tableModel.getFieldAt(row);
	}
	
	/**
	 * Overridden to return a customized cell renderer.
	 * @see JTable#getCellRenderer(int, int)
	 */
	public TableCellRenderer getCellRenderer(int row, int column) 
	{
		if (renderer == null) renderer = new InspectorCellRenderer();
        return renderer;
    }
	
	/**
	 * Overridden to return the editor corresponding to the specified cell.
	 * @see JTable#getCellEditor(int, int)
	 */
	public TableCellEditor getCellEditor(int row, int col)
	{
		AttributeField field = tableModel.getFieldAt(row);
		InspectorCellRenderer 
			renderer = (InspectorCellRenderer) getCellRenderer(row, col);
		Object v = tableModel.getValueAt(row, col);
		if (field.getValueType() == ValueType.ENUM)
		{
			return new DefaultCellEditor((JComboBox)
				renderer.getTableCellRendererComponent(this,
					getValueAt(row, col), false, false, row, col));
		} else if (v instanceof Double || v instanceof Integer || 
				v instanceof Long || v instanceof String) {
			DefaultCellEditor editor =
				new DefaultCellEditor((JTextField) renderer.
				getTableCellRendererComponent(this,
					getValueAt(row, col), false, false, row, col));
			JTextField f = (JTextField) editor.getComponent();
			f.getDocument().addDocumentListener(this);
			return editor;
		} else if (v instanceof Boolean) {
			return new DefaultCellEditor((JCheckBox) renderer.
				getTableCellRendererComponent(this,
					getValueAt(row, col), false, false, row, col));
		} 
		return super.getCellEditor(row, col);
	}
	
	/**
	 * Indicates that the text has been modified
	 * @see DocumentListener#removeUpdate(DocumentEvent)
	 */
	public void removeUpdate(DocumentEvent e) {
		try {
			String text = e.getDocument().getText(0, 
					e.getDocument().getLength());
			firePropertyChange(VALUE_CHANGED_PROPERTY, null, text);
		} catch (Exception ex) {
		}
	}
	
	/**
	 * Indicates that the text has been modified
	 * @see DocumentListener#removeUpdate(DocumentEvent)
	 */
	public void insertUpdate(DocumentEvent e) {
		try {
			String text = e.getDocument().getText(0, 
					e.getDocument().getLength());
			firePropertyChange(VALUE_CHANGED_PROPERTY, null, text);
		} catch (Exception ex) {
		}
	}
	
	/**
	 * Required by the {@link DocumentListener} I/F but no-operation
	 * implementation in this case.
	 * @see DocumentListener#removeUpdate(DocumentEvent)
	 */
	public void changedUpdate(DocumentEvent e) {}
	
}