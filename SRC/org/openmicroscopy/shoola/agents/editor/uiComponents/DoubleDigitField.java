 /*
 * uiComponents.DoubleDigitField 
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
package org.openmicroscopy.shoola.agents.editor.uiComponents;

//Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.BorderFactory;
import javax.swing.JFormattedTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.JTextComponent;
import javax.swing.text.MaskFormatter;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.editor.EditorAgent;
import org.openmicroscopy.shoola.env.log.LogMessage;

/** 
 * A formatted text field that displays 2 digits, between a specified
 * range. 
 * Fires propertyChanged DIGIT_VALUE_PROPERTY when the fields are edited
 * and focus is lost. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */

public class DoubleDigitField 
	extends JFormattedTextField
{
	
	/**
	 * The colour used to highlight the text when the field gains focus.
	 */
	public static final Color BLUE_HIGHLIGHT = new Color(181,213,255);
	
	/**
	 * A bound property of this class. 
	 * PropertyChangeEvent is fired when focus is lost from this 
	 * field, IF the new value is different from the old value.
	 */
	public static final String DIGIT_VALUE_PROPERTY = "digitValueProperty";
	
	/**
	 * The border displayed when the field has focus. 
	 */
	Border selectedBorder;
	
	/**
	 * The border displayed when the field doesn't have focus.
	 */
	Border unSelectedBorder;
	
	/**
	 * The minimum value allowed for this field. 
	 * Range is checked when this field loses focus. 
	 */
	private int minValue;
	
	/**
	 * The maximum value allowed for this field. 
	 */
	private int maxValue;
	
	/**
	 * A temporary variable that allows checking whether the field has been
	 * edited. 
	 */
	private String oldValue;
	
	/**
	 * Creates an instance of this class.
	 * The minimum value is set to zero. 
	 * 
	 * @param maxValue		The maximum value for this field.
	 */
	public DoubleDigitField(int maxValue) {
		this(0, maxValue);
	}
	
	/**
	 * Creates an instance of this class.
	 * Other constructor feeds into this one.
	 * 
	 * @param minValue	The minimum value for this field.
	 * @param maxValue	The maximum value for this field.
	 */
	public DoubleDigitField(int minValue, int maxValue) {
		
		super(createFormatter("##"));
		
		this.minValue = minValue;
		this.maxValue = maxValue;
		
		/*
		 * Create the borders
		 */
		int padding = 2;
		Border blackBorder = new LineBorder(Color.black);
		Border whiteBorder = new LineBorder(Color.gray);
		Border emptyBorder = new EmptyBorder(padding,padding,padding,padding);
		selectedBorder = BorderFactory.createCompoundBorder(
				   blackBorder, emptyBorder);
		unSelectedBorder = BorderFactory.createCompoundBorder(
				   whiteBorder, emptyBorder);
		
		/*
		 * Get the sizes from the UIsizes registry. 
		 */
		UIUtilities r = UIUtilities.getInstance();
		int width = r.getDimension(UIUtilities.DOUBLE_DIGIT_FIELD_W);
		int height = r.getDimension(UIUtilities.SINGLE_ROW_HEiGHT);
		Dimension fieldSize = new Dimension(width, height);
		this.setPreferredSize(fieldSize);
		this.setMaximumSize(fieldSize);
		
		/*
		 * Sets font, highlight and border.
		 */
		this.setFont(new CustomFont());
		this.setBorder(unSelectedBorder);
		this.setSelectionColor(BLUE_HIGHLIGHT);
		this.setSelectedTextColor(Color.black);
		
		this.addFocusListener(new FieldFocusListener());
	}
	
	/**
	 * A focus listener for this field. 
	 * Manages the change of appearance (border and highlight) when focus
	 * gained and lost.
	 * Also checks range and fires property change event when focus is lost. 
	 * 
	 * @author will
	 *
	 */
	public class FieldFocusListener implements FocusListener {

		public void focusGained(FocusEvent e) {
			JTextComponent source = (JTextComponent)e.getSource();
			source.setBorder(selectedBorder);
			source.setSelectionStart(0);
			source.setSelectionEnd(2);
		}

		public void focusLost(FocusEvent e) {
			
			String oldVal = oldValue;
			String newValue = checkRange(getText().trim());
			setText(newValue);	// make sure the display is within range.
			if (! newValue.equals(oldVal)) {
				DoubleDigitField.this.firePropertyChange(DIGIT_VALUE_PROPERTY, 
						oldVal, newValue);
			}
			
			JTextComponent source = (JTextComponent)e.getSource();
			source.setBorder(unSelectedBorder);
			source.setSelectionStart(0);
			source.setSelectionEnd(0);
		}
	}
	
	/**
	 * Makes sure that the returned value is between the min and max values. 
	 * 
	 * @param value		The value to check. Must be passed to an integer.
	 * @return			The value after is has been brought within the range.
	 */
	public String checkRange(String value) {
		
		int val = new Integer(value);
		val = Math.max(val, minValue);
		val = Math.min(val, maxValue);
		
		return val + "";
	}
	
	/**
	 * Calls super.setText().
	 * But first, it updates the value of oldValue, and formats the string
	 * so that a single digit is converted to a douple digit. 
	 */
	public void setText(String text) {
		oldValue = text;
		
		if (text.length() == 1) {
			text = "0" + text;
		}
		super.setText(text);
	}
	
	/**
	 * A convenience method for creating a MaskFormatter.
	 */
    protected static MaskFormatter createFormatter(String s) {
        MaskFormatter formatter = null;
        try {
            formatter = new MaskFormatter(s);
        } catch (java.text.ParseException exc) {
        	 LogMessage msg = new LogMessage();
             msg.print("createFormatter");
             msg.print(exc);
             EditorAgent.getRegistry().getLogger().error(DoubleDigitField.class, 
            		 msg);
        }
        return formatter;
    }

}
