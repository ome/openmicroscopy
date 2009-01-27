/*
 * org.openmicroscopy.shoola.agents.metadata.editor.AcquisitionComponent 
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
package org.openmicroscopy.shoola.agents.metadata.editor;


//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import org.openmicroscopy.shoola.util.ui.UIUtilities;

//Third-party libraries

//Application-internal dependencies

/** 
 * Utility class where UI component about acquisition metadata are stored.
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
class AcquisitionComponent
	extends JComponent
{

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
		firePropertyChange(EditorControl.SAVE_PROPERTY, Boolean.FALSE, 
				Boolean.TRUE);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param label	Component displaying the name of the field.
	 * @param area	Component displaying the value of the field.
	 */
	AcquisitionComponent(JLabel label, JComponent area)
	{
		dirty = false;
		this.label = label;
		this.area = area;
		setField = true;
		label.setLabelFor(area);
		value = getValue();
		setBorder(AnnotationUI.EDIT_BORDER);
	}
	
	/**
	 * Sets to <code>true</code> if the value has been set, <code>false</code> 
	 * otherwise.
	 * 
	 * @param setField The value to set.
	 */
	void setSetField(boolean setField)
	{ 
		this.setField = setField;
		if (!setField) {
			if (area instanceof JTextComponent)
				area.setBorder(AnnotationUI.EDIT_BORDER);
		}
	}
	
	/**
	 * Returns <code>true</code> if the field has been set, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isSetField() { return setField; }
	
	/**
	 * Returns the component displaying the name of the field.
	 * 
	 * @return See above.
	 */
	JLabel getLabel() { return label; }
	
	/**
	 * Returns the component displaying the value of the field.
	 * 
	 * @return See above.
	 */
	JComponent getArea()
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
	Object getAreaValue() { return getValue(); }
	
	/**
	 * Returns <code>true</code> if the value of the {@link #area} has been 
	 * modified, <code>false</code> otherwise.
	 *  
	 * @return See above.
	 */
	boolean isDirty() { return dirty; }
	
	/**
	 * Adds the passed property listener.
	 * 
	 * @param listener The listener to add.
	 */
	void attachListener(PropertyChangeListener listener)
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
			c.getDocument().addDocumentListener(new DocumentListener() {
			
				public void removeUpdate(DocumentEvent e) {
					notifyDataToSave();
				}
			
				public void insertUpdate(DocumentEvent e) {
					notifyDataToSave();
				}
			
				public void changedUpdate(DocumentEvent e) {}
			
			});
		} 
	}

}
