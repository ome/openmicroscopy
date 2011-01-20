/*
 * org.openmicroscopy.shoola.agents.util.DataComponent 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.util;

//Java imports
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Utility class where a editable (key, value) pair is displayed.
 *
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class DataComponent 	
	extends JComponent
	implements DocumentListener
{
	
	/** The border when the field are is editable. */
	public static final Border EDIT_BORDER = BorderFactory.createLineBorder(
			Color.LIGHT_GRAY);
	
	/** Data Object indicating that the value has been modified. */
	public static final String DATA_MODIFIED_PROPERTY = "dataModified";
	
	/** Component displaying the name of the field. */
	private JLabel 		label;
	
	/** Component displaying the value of the field. */
	private JComponent 	area;
	
	/** Flag indicating if the field has been set or not. */
	private boolean		setField;
	
	/** Flag indicating the value of the {@link #area} component 
	 * has been modified.
	 */
	private boolean		dirty;
	
	/** The selected object. */
	private Object		value;
	
	/**
	 * Returns the original value of the {@link #area}.
	 * 
	 * @return See above.
	 */
	private Object getValue()
	{
		if (area instanceof JComboBox) {
			JComboBox c = (JComboBox) area;
			return c.getSelectedItem(); 
		} else if (area instanceof JTextComponent) {
			JTextComponent c = (JTextComponent) area;
			return c.getText();
		} 
		return null;
	}
	
	/** Fires a property to enable the save action. */
	private void notifyDataToSave()
	{
		dirty = true;
		Object v = getValue();
		if (value != null && value.equals(v)) dirty = false;
		firePropertyChange(DATA_MODIFIED_PROPERTY, Boolean.valueOf(!dirty), 
				Boolean.valueOf(dirty));
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param label	Component displaying the name of the field.
	 * @param area	Component displaying the value of the field.
	 */
	public DataComponent(JLabel label, JComponent area)
	{
		dirty = false;
		this.label = label;
		this.area = area;
		setField = true;
		label.setLabelFor(area);
		value = getValue();
		//area.setEnabled(false);
		//setBorder(EDIT_BORDER);
	}
	
	/**
	 * Sets to <code>true</code> if the value has been set, <code>false</code> 
	 * otherwise.
	 * 
	 * @param setField The value to set.
	 */
	public void setSetField(boolean setField)
	{ 
		this.setField = setField;
		if (!setField) {
			if (area instanceof JTextComponent)
				area.setBorder(EDIT_BORDER);
		}
	}
	
	/**
	 * Returns <code>true</code> if the field has been set, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isSetField() { return setField; }
	
	/**
	 * Returns the component displaying the name of the field.
	 * 
	 * @return See above.
	 */
	public JLabel getLabel() { return label; }
	
	/**
	 * Returns the component displaying the value of the field.
	 * 
	 * @return See above.
	 */
	public JComponent getArea()
	{ 
		if (area instanceof JComboBox) {
			JPanel p = UIUtilities.buildComponentPanel(area, 0, 0);
			p.setBackground(area.getBackground());
			return p;
		}
		return area; 
	}
	
	/**
	 * Returns the value of the {@link #area} component.
	 * 
	 * @return See above.
	 */
	public Object getAreaValue() { return getValue(); }
	
	/**
	 * Returns <code>true</code> if the value of the {@link #area} has been 
	 * modified, <code>false</code> otherwise.
	 *  
	 * @return See above.
	 */
	public boolean isDirty() { return dirty; }
	
	/**
	 * Adds the passed property listener.
	 * 
	 * @param listener The listener to add.
	 */
	public void attachListener(PropertyChangeListener listener)
	{
		addPropertyChangeListener(listener);
		if (area instanceof JComboBox) {
			JComboBox c = (JComboBox) area;
			c.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					notifyDataToSave();
				}
			
			});
		} else if (area instanceof JTextComponent) {
			JTextComponent c = (JTextComponent) area;
			c.getDocument().addDocumentListener(this);
		} 
	}
	
	/**
	 * Overridden to set the enabled flag of the area.
	 * @see JComponent#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled)
	{
		area.setEnabled(enabled);
	}

	/**
	 * Implemented as specified by the {@link DocumentListener} I/F.
	 * @see DocumentListener#removeUpdate(DocumentEvent)
	 */
	public void removeUpdate(DocumentEvent e) { notifyDataToSave(); }

	/**
	 * Implemented as specified by the {@link DocumentListener} I/F.
	 * @see DocumentListener#insertUpdate(DocumentEvent)
	 */
	public void insertUpdate(DocumentEvent e) { notifyDataToSave(); }

	/**
	 * Required by the {@link DocumentListener} I/F but no-op implementation
	 * in our case.
	 * @see DocumentListener#removeUpdate(DocumentEvent)
	 */
	public void changedUpdate(DocumentEvent e) {}
	
	
}
