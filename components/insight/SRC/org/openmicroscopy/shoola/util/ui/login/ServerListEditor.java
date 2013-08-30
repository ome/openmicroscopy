/*
 * org.openmicroscopy.shoola.util.ui.login.ServerListEditor 
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
package org.openmicroscopy.shoola.util.ui.login;


//Java imports
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableCellEditor;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.NumericalTextField;

/** 
 * Customized editor to indent the text when a new row is added to the 
 * table.
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
public class ServerListEditor 
	extends AbstractCellEditor 
	implements ActionListener, DocumentListener, TableCellEditor
{

	/** The component handling the editing of the cell value. */
	private JTextField 		component;

	/** The component handling the editing of the cell value. */
	private JTextField 		textComponent;
	
	/** 
	 * The component handling the editing of the cell value for numerical value.
	 */
	private JTextField 		numericalcomponent;
	
	/** The table this editor is for. */
	private ServerTable		table;
	
	/**
	 * Returns the text of the component
	 * 
	 * @return See above.
	 */
	private String getComponentValue()
	{
		String s = component.getText();
		if (s == null) return "";
		return s.trim();
	}
	
	/** 
	 * Invokes when a new server name is entered or
	 * an existing one is edited.
	 */
	private void handleKeyEnter() { table.handleKeyEnter(); }
	
	/** 
	 * Creates a new instance. 
	 * 
	 * @param table The table this editor is for. Mustn't be <code>null</code>.
	 */
	public ServerListEditor(ServerTable table)
	{
		if (table == null)
			throw new IllegalArgumentException("No table.");
		this.table = table;
		numericalcomponent = new NumericalTextField(ServerEditor.MIN_PORT, 
				ServerEditor.MAX_PORT);
		textComponent = new JTextField();
		textComponent.setName("server name field");
		textComponent.addActionListener(this);
		textComponent.getDocument().addDocumentListener(this);
		textComponent.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					handleKeyEnter();
			}
		});
		component = textComponent;
	}
	
    /**
     * Implements as specified by the {@link TableCellEditor} Interface.
     * @see TableCellEditor#getTableCellEditorComponent(JTable, Object, boolean,
     * 													 int, int)
     */
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int rowIndex, int colIndex) 
    {
    	//Invokes when a cell value is edited by the user.
    	if (colIndex == 2) component = numericalcomponent;
    	else component = textComponent;
        if (value != null) {
        	String v = (String) value;
        	if (v == null || v.trim().length() == 0) v = " ";
        	component.setText(v);
        	//component.requestFocus();
        }
        return component;
    }

    /**
     * Returns the edited text. This method is invoked when the editing is
     * completed
     * 
     * @return The edited text. 
     */
    public Object getCellEditorValue() { return getComponentValue(); }

    /**
     * Handles text insert only when the selected table cell is edited.
     * @see DocumentListener#insertUpdate(DocumentEvent)
     */
	public void insertUpdate(DocumentEvent e)
	{
		table.handleTextModification(getComponentValue());
	}

    /**
 	 * Required by {@link DocumentListener} interface but no-operation
 	 * implementation in our case.
     * @see DocumentListener#removeUpdate(DocumentEvent)
     */
	public void removeUpdate(DocumentEvent e)
	{
		table.handleTextModification(getComponentValue());
	}
    
	/** 
     * Required by {@link DocumentListener} interface but no-operation
 	 * implementation in our case.
     * @see DocumentListener#changedUpdate(DocumentEvent)
     */
	public void changedUpdate(DocumentEvent e) {}

	/**
	 * Stops the edition when the user entered the pressed key.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) 
	{
		stopCellEditing();
		table.finishEdition(getComponentValue());
	}

}
