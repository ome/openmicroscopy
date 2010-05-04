/*
 * org.openmicroscopy.shoola.agents.metadata.util.ScriptComponent 
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
package org.openmicroscopy.shoola.agents.metadata.util;


//Java imports
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.NumericalTextField;
import org.openmicroscopy.shoola.util.ui.NumericalTextFieldLabelled;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Hosts information related to a parameter for the script.
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
public class ScriptComponent 
{

	/** Indicates how to separate (key, value) pairs for a map. */
	public static final String MAP_SEPARATOR = ":";
	
	/** Indicates how to separate value for a list. */
	public static final String LIST_SEPARATOR = " ";
	
	/** Indicates the required parameter. */
	public static final String REQUIRED = "*";
	
	/** Identifies the map. */
	public static final int		MAP = 1;
	
	/** Identifies the list. */
	public static final int		LIST = 2;
	
	/** Identifies the default index. */
	private static final int	DEFAULT = 0;
	
	/** The component to host. */
	private JComponent component;
	
	/** The text associated to the component. */
	private JLabel label;
	
	/** 
	 * The text explaining the component. It should only be set for
	 * collections and maps.
	 */
	private JLabel info;
	
	/** Indicates if a value is required. */
	private boolean required;
	
	/** Indicate that the component supports map, list etc. */
	private int index;
	
	/**
	 * Converts the value into corresponding map.
	 * 
	 * @param value The value to convert.
	 * @return See above.
	 */
	private Map convertStringToMap(String value)
	{
		Map map = new HashMap();
		String[] values = value.split(LIST_SEPARATOR);
		if (values == null || values.length == 0) return map;
		String[] pair;
		for (int i = 0; i < values.length; i++) {
			pair = values[i].split(MAP_SEPARATOR);
			if (pair != null && pair.length == 2) {
				map.put(pair[0], pair[1]);
			}
		}
		return map;
	}
	
	/**
	 * Converts the value into corresponding list.
	 * 
	 * @param value The value to convert.
	 * @return See above.
	 */
	private List convertStringToList(String value)
	{
		List l = new ArrayList();
		String[] values = value.split(LIST_SEPARATOR);
		if (values == null || values.length == 0) return l;
		for (int i = 0; i < values.length; i++) {
			l.add(values[i].trim());
		}
		return l;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param component The component to host.
	 * @param parameter The 
	 */
	public ScriptComponent(JComponent component, String parameter)
	{
		if (component == null)
			throw new IllegalArgumentException("No component specified.");
		this.component = component;
		label = UIUtilities.setTextFont(parameter);
		label.setToolTipText(component.getToolTipText());
		required = (component instanceof JComboBox);
	}
	
	/**
	 * Sets the text explaining the component when the component is a list
	 * or a map.
	 * 
	 * @param text The value to set.
	 */
	public void setInfo(String text)
	{
		if (text == null || text.trim().length() == 0) return;
		info = new JLabel();
		Font f = info.getFont();
		info.setFont(f.deriveFont(Font.ITALIC, f.getSize()-2));
	}
	
	/**
	 * Sets the index.
	 * 
	 * @param index One of the constants defined by this class.
	 */
	public void setIndex(int index) { this.index = index; }
	
	/**
	 * Sets to <code>true</code> if a value is required for the field,
	 * <code>false</code> otherwise.
	 * 
	 * @param required The value to set.
	 */
	public void setRequired(boolean required)
	{ 
		this.required = required; 
		if (required) label = UIUtilities.setTextFont(label.getText()+" *");
	}
	
	/**
	 * Returns <code>true</code> if a value is required for that component.
	 * 
	 * @return See above.
	 */
	public boolean isRequired() { return required; }
	
	/**
	 * Returns the component hosted.
	 * 
	 * @return See above.
	 */
	public JComponent getComponent() { return component; }
	
	/**
	 * Returns the label associated to the component.
	 * 
	 * @return See above.
	 */
	public JComponent getLabel()
	{ 
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.add(label);
		if (info != null) p.add(info);
		return UIUtilities.buildComponentPanel(p, 0, 0); 
	}
	
	/** 
	 * Returns the value associated to a script.
	 * 
	 * @return See above.
	 */
	public Object getValue()
	{
		if (component instanceof JCheckBox) {
			JCheckBox box = (JCheckBox) component;
			return box.isSelected();
		} else if (component instanceof NumericalTextField) {
			return ((NumericalTextField) component).getValueAsNumber();
		} else if (component instanceof NumericalTextFieldLabelled) {
			return ((NumericalTextFieldLabelled) component).getValueAsNumber();
		} else if (component instanceof JTextField) {
			JTextField field = (JTextField) component;
			String value = field.getText();
			if (value == null) return null;
			value = value.trim();
			switch (index) {
				case DEFAULT:
					return value;
				case MAP:
					return convertStringToMap(value);
				case LIST:
					return convertStringToList(value);
			}
		} else if (component instanceof JComboBox) {
			JComboBox box = (JComboBox) component;
			return box.getSelectedItem();
		}
			
		return null;
	}
	
	
}
