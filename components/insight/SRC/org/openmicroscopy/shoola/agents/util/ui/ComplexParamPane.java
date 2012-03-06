/*
 * org.openmicroscopy.shoola.agents.metadata.util.RowForParam
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;


//Third-party libraries
import info.clearthought.layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Component displaying rows to collect.
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
class ComplexParamPane 
	extends JPanel
	implements PropertyChangeListener
{

	/** Identifies a map. */
	static final int MAP = 0;
	
	/** Identifies a list. */
	static final int LIST = 1;
	
	/** The default value. */
	private int			defaultIndex;
	
	/** The values to use or <code>null</code>. */
	private Object[] 		values;
	
	/** Indicates the type for the key. */
	private Class 			keyType;
	
	/** Indicates the type for the key. */
	private Class 			valueType;
	
	/** One of the constants defined by this class. */
	private int 			index;
	
	/** The collection of rows. */
	private List<RowPane> 	rows;
	
	/** The component hosting the rows. */
	private JPanel			rowsPane;
	
	/** Component used to add new row. */
	private JButton			addButton;
	
	/** Initializes the components. */
	private void initializeComponents()
	{
		IconManager icons = IconManager.getInstance();
		addButton = new JButton(icons.getIcon(IconManager.PLUS_12));
		UIUtilities.unifiedButtonLookAndFeel(addButton);
		addButton.setToolTipText("Add a new row.");
		addButton.addActionListener(new ActionListener() {
			
			/**
			 * Creates a new row and lays it out.
			 * @see ActionListener#actionPerformed(ActionEvent)
			 */
			public void actionPerformed(ActionEvent e) {
				createRow();
				layoutRows();
			}
		});
	}
	
	/**
	 * Checks if the passed value is valid.
	 * 
	 * @param value The value to handle.
	 */
	private void checkIndex(int value)
	{
		switch (value) {
			case MAP:
			case LIST:
				break;
			default:
				throw new IllegalArgumentException("Index not supported.");
		}
	}
	
	/** 
	 * Creates a new row and returns it.
	 * 
	 * @return See above.
	 */
	private RowPane createRow()
	{
		RowPane row = null;
		switch (index) {
			case LIST:
				if (keyType == null && values != null) 
					row = new RowPane(values);
				else if (keyType != null && values == null)
					row = new RowPane(keyType);
				break;
			case MAP:
				if (values == null && valueType != null)
					row = new RowPane(keyType, valueType);
				else if (values != null && valueType == null)
					row = new RowPane(keyType, values);
		}
		if (row != null) {
			row.setDefaultValue(defaultIndex);
			row.setToolTipText(getToolTipText());
			rows.add(row);
			row.addPropertyChangeListener(RowPane.REMOVE_ROW_PROPERTY, this);
		}
		return row;
	}
	
	/** Lays out the rows. */
	private void layoutRows()
	{
 		rowsPane.removeAll();
 		TableLayout layout = (TableLayout) rowsPane.getLayout();
		Iterator<RowPane> i = rows.iterator();
		int index = 0;
		while (i.hasNext()) {
			layout.insertRow(index, TableLayout.PREFERRED);
			rowsPane.add(i.next(), "0,"+index+", LEFT, CENTER");
			index++;
		}
		revalidate();
		repaint();
	}

	/** 
	 * Removes the row from the display. The row will not be removed 
	 * if the parameters is required.
	 * 
	 * @param row The row to remove.
	 */
	private void removeRow(RowPane row)
	{
		if (row == null) return;
		rows.remove(row);
		layoutRows();
		
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		double[] columns = {TableLayout.PREFERRED, 5, TableLayout.FILL};
		rowsPane = new JPanel();
		TableLayout layout = new TableLayout();
		layout.setColumn(columns);
		rowsPane.setLayout(layout);
		RowPane row = createRow();
		row.addPropertyChangeListener(this);
		row.disableRemove();
		layoutRows();
		double[][] size = {{TableLayout.PREFERRED, 2, TableLayout.FILL}, 
				{TableLayout.PREFERRED, TableLayout.PREFERRED}};
		setLayout(new TableLayout(size));
		add(addButton, "0, 0, LEFT, TOP");
		add(rowsPane, "2, 0, 2, 1");
	}
	
	/** 
	 * Creates a new instance.
	 * 
	 * @param values   The values of the enumerations or <code>null</code>.
	 */
	ComplexParamPane(JComboBox values)
	{
		this(LIST, values, null, null);
	}
	
	/** 
	 * Creates a new instance.
	 * 
	 * @param keyType  The type of the key.
	 */
	ComplexParamPane(Class keyType)
	{
		this(LIST, null, keyType, null);
	}
	
	/** 
	 * Creates a new instance.
	 * 
	 * @param keyType  The type of the key.
	 * @param values   The values of the enumerations or <code>null</code>.
	 */
	ComplexParamPane(Class keyType, JComboBox values)
	{
		this(MAP, values, keyType, null);
	}
	
	/** 
	 * Creates a new instance.
	 * 
	 * @param keyType  The type of the key.
	 * @param valueType The type of the value.
	 */
	ComplexParamPane(Class keyType, Class valueType)
	{
		this(MAP, null, keyType, valueType);
	}
	
	/** 
	 * Creates a new instance.
	 * 
	 * @param index    One of the constants defined by this class.
	 * @param values   The values of the enumerations or <code>null</code>.
	 * @param keyType  The type of the key.
	 * @param valueType The type of the value.
	 */
	ComplexParamPane(int index, JComboBox values, Class keyType, 
			Class valueType)
	{
		if (values == null && valueType == null && keyType == null)
			throw new IllegalArgumentException("Value not specified.");
		checkIndex(index);
		if (index == MAP && keyType == null)
			throw new IllegalArgumentException("Key cannot be null for " +
					"Map Type.");
		this.index = index;
		if (values != null) {
			defaultIndex = values.getSelectedIndex();
			Object[] v = new Object[values.getItemCount()];
			for (int i = 0; i < v.length; i++) 
				v[i] = values.getItemAt(i);
			this.values = v;
		}
		this.keyType = keyType;
		this.valueType = valueType;
		rows = new ArrayList<RowPane>();
		initializeComponents();
		buildGUI();
	}
	
	/** 
	 * Returns the filled values.
	 * 
	 * @return See above.
	 */
	Object getValue()
	{
		Iterator<RowPane> i = rows.iterator();
		RowPane row;
		Object key, value;
		switch (index) {
			case LIST:
				List<Object> r = new ArrayList<Object>();
				while (i.hasNext()) {
					row = i.next();
					key = row.getKeyResult();
					if (key != null) r.add(key);
				}
				if (r.size() == 0) return null;
				return r;
			case MAP:
				Map<Object, Object> m = new HashMap<Object, Object>();
				while (i.hasNext()) {
					row = i.next();
					key = row.getKeyResult();
					value = row.getValueResult();
					if (value != null && key != null)
						m.put(key, value);
				}
				if (m.size() == 0) return m;
				return m;
		}
		return null;
	}
	
	/**
	 * Removes a row from the display.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (RowPane.REMOVE_ROW_PROPERTY.equals(name)) 
			removeRow((RowPane) evt.getNewValue());
		else if (RowPane.MODIFIED_CONTENT_PROPERTY.equals(name)) {
			firePropertyChange(name, evt.getOldValue(), evt.getNewValue());
		}
	}
	
	/**
	 * Overridden to set the text for all components.
	 * @see JPanel#setToolTipText(String)
	 */
	public void setToolTipText(String text)
	{
		super.setToolTipText(text);
		Iterator<RowPane> i = rows.iterator();
		while (i.hasNext()) {
			((RowPane) i.next()).setToolTipText(text);
		}
	}
	
	/**
	 * Overridden to handle grouping.
	 * @see JPanel#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		if (addButton != null) addButton.setEnabled(enabled);
		Iterator<RowPane> i = rows.iterator();
		while (i.hasNext()) {
			((RowPane) i.next()).setEnabled(enabled);
		}
	}
	
}
