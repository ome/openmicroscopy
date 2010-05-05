/*
 * org.openmicroscopy.shoola.util.ui.NumericalTextFieldLabelled 
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
package org.openmicroscopy.shoola.util.ui;


//Java imports
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentListener;

//Third-party libraries

//Application-internal dependencies

/** 
 * Displays the minimum and/or maximum value of a numerical text field.
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
public class NumericalTextFieldLabelled 
	extends JPanel
{

	/** Initializes the text field. */
	private NumericalTextField field;
	
	/** The label displaying the minimum or maximum. */
	private JLabel	fieldLabel;
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		//setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		add(field);
		add(fieldLabel);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param type The type of number to handle.
	 * @param min  The lower bound of the interval if set.
	 * @param max  The upper bound of the interval if set.
	 */
	public NumericalTextFieldLabelled(Class type, Number min, Number max)
	{
		field = new NumericalTextField();
		fieldLabel = new JLabel();
		String text = "";
		if (min != null) {
			if (type.equals(Integer.class))
				text += "Min: "+min.intValue()+" ";
			else text += "Min: "+min.doubleValue()+" ";
			field.setMinimum(min.doubleValue());
		}
		if (max != null) {
			if (type.equals(Integer.class))
				text += "Max: "+max.intValue();
			else text += "Max: "+max.doubleValue();
			field.setMaximum(max.doubleValue());
		}
		fieldLabel.setText(text);
		field.setNumberType(type);
		buildGUI();
	}
	
	/**
	 * Sets the numerical value. 
	 * 
	 * @param value The value to set.
	 */
	public void setValue(String value)
	{
		if (value != null) field.setText(value);
	}
	
	/**
	 * Returns the value as a number.
	 * 
	 * @return See above.
	 */
	public Number getValueAsNumber()
	{
		return field.getValueAsNumber();
	}
	
	/**
	 * Adds the specified listener to the numerical text field.
	 * 
	 * @param listener The listener to add.
	 */
	public void addDocumentListener(DocumentListener listener)
	{
		field.getDocument().addDocumentListener(listener);
	}
	
}
