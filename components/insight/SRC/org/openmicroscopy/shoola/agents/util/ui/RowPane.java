/*
 * org.openmicroscopy.shoola.agents.metadata.util.RowPane 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.util.ui;

//Java imports
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.util.ui.NumericalTextField;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Lays out a row for either a list or a map.
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
class RowPane 
	extends JPanel
	implements ActionListener, DocumentListener
{

	/** Bound property indicating to remove the row. */
	static final String REMOVE_ROW_PROPERTY = "removeRow";
	
	/** Bound property indicating that the content has been modified. */
	static final String MODIFIED_CONTENT_PROPERTY = "modifiedContent";
	
	/** Button used to remove the row. */
	private JButton removeButton;
	
	/** The component used to enter the key. */
	private JComponent keyComponent;
	
	/** The component used to enter the key. */
	private JComponent valueComponent;
	
	/** Initializes the components. */
	private void initialize()
	{
		valueComponent = null;
		IconManager icons = IconManager.getInstance();
		removeButton = new JButton(icons.getIcon(IconManager.MINUS_9));
		removeButton.setToolTipText("Remove the row.");
		removeButton.addActionListener(this);
		UIUtilities.unifiedButtonLookAndFeel(removeButton);
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.add(removeButton);
		p.add(keyComponent);
		if (valueComponent != null) {
			p.add(Box.createHorizontalStrut(2));
			p.add(valueComponent);
		}
		setLayout(new FlowLayout(FlowLayout.LEFT));
		add(p);
	}
	
	/**
	 * Creates a field for the key.
	 * 
	 * @param keyType The type of key to handle.
	 * @return See above.
	 */
	private JComponent createKeyField(Class keyType)
	{
		JTextField field;
		if (Double.class.equals(keyType) || Integer.class.equals(keyType) ||
				Long.class.equals(keyType) || Float.class.equals(keyType)) {
			field = new NumericalTextField();
			((NumericalTextField) field).setNumberType(keyType);
		} else field = new JTextField();
		field.getDocument().addDocumentListener(this);
		field.setColumns(ScriptComponent.COLUMNS);
		return field;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param keyType The type of the key.
	 */
	RowPane(Class keyType)
	{
		initialize();
		keyComponent = createKeyField(keyType);
		buildGUI();
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param values The values to use as keys.
	 */
	RowPane(Object[] values)
	{
		this(null, values);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param keyType The type of the key.
	 * @param valueType The type of the value.
	 */
	RowPane(Class keyType, Class valueType)
	{
		initialize();
		keyComponent = createKeyField(keyType);
		if (valueType != null) valueComponent = createKeyField(valueType);
		buildGUI();
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param values The values to use as keys.
	 */
	RowPane(Class keyType, Object[] values)
	{
		initialize();
		if (keyType == null) keyComponent = new JComboBox(values);
		else {
			keyComponent = createKeyField(keyType);
			valueComponent = new JComboBox(values);
		}
		buildGUI();
	}

	/**
	 * Sets the default value.
	 * 
	 * @param index The selected index.
	 */
	void setDefaultValue(int index)
	{
		if (valueComponent instanceof JComboBox) {
			JComboBox box = (JComboBox) valueComponent;
			box.setSelectedIndex(index);
		}
		if (keyComponent instanceof JComboBox) {
			JComboBox box = (JComboBox) keyComponent;
			box.setSelectedIndex(index);
		}
	}
	
	/**
	 * Returns the value filled as a key.
	 * 
	 * @return See above.
	 */
	Object getKeyResult()
	{
		return ScriptComponent.getComponentValue(keyComponent);
	}
	
	/**
	 * Returns the value filled as a key.
	 * 
	 * @return See above.
	 */
	Object getValueResult()
	{
		return ScriptComponent.getComponentValue(valueComponent);
	}
	
	/** Hides the {@link #removeButton}. */
	void disableRemove() { removeButton.setEnabled(false); }
	
	/**
	 * Fires a property indicating to remove the row from the display.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		firePropertyChange(REMOVE_ROW_PROPERTY, null, this);
	}
	
	/**
	 * Allows the user to run or not the script.
	 * @see DocumentListener#insertUpdate(DocumentEvent)
	 */
	public void insertUpdate(DocumentEvent e)
	{ 
		firePropertyChange(MODIFIED_CONTENT_PROPERTY, null, this); 
	}

	/**
	 * Allows the user to run or not the script.
	 * @see DocumentListener#removeUpdate(DocumentEvent)
	 */
	public void removeUpdate(DocumentEvent e)
	{ 
		firePropertyChange(MODIFIED_CONTENT_PROPERTY, null, this); 
	}
	
	/**
	 * Overridden to set the text for all components.
	 * @see JPanel#setToolTipText(String)
	 */
	public void setToolTipText(String text)
	{
		super.setToolTipText(text);
		if (keyComponent != null) keyComponent.setToolTipText(text);
		if (valueComponent != null) valueComponent.setToolTipText(text);
	}
	
	/**
	 * Overridden to handle grouping.
	 * @see JPanel#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		if (removeButton != null) removeButton.setEnabled(enabled);
		if (keyComponent != null) keyComponent.setEnabled(enabled);
		if (valueComponent != null) valueComponent.setEnabled(enabled);
	}
	
	/**
	 * Required by the {@link DocumentListener} I/F but no-operation 
	 * implementation in our case.
	 * @see DocumentListener#changedUpdate(DocumentEvent)
	 */
	public void changedUpdate(DocumentEvent e) {}
	
}
