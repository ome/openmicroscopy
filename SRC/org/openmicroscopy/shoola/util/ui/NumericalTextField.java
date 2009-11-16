/*
 * org.openmicroscopy.shoola.util.ui.NumericalTextField 
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
package org.openmicroscopy.shoola.util.ui;


//Java imports
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.PlainDocument;

//Third-party libraries

//Application-internal dependencies

/** 
 * A text field containing only numerical value.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta3
 */
public class NumericalTextField 
	extends JTextField
	implements DocumentListener, FocusListener
{

	/** Bounds property indicating that the text has been updated. */
	public static final String		TEXT_UPDATED_PROPERTY = "textUpdated";
	
	/** Accepted value if integer. */
	private static final String 	NUMERIC = "0123456789";
	
	/** Accepted value if double or float. */
	private static final String 	FLOAT = NUMERIC+".";
	
	/** The color used for the foreground when the user is editing the value. */
	private Color 					editedColor;
	
	/** The default foreground color. */
	private Color					defaultForeground;
	
	/** Helper reference to the document. */
	private NumericalPlainDocument 	document;

	/** The default Text. */
	private String					originalText;
	
	/** The type of number to handle: integer, double or float. */
	private Class					numberType;
	
	/** Flag indicating if negative values are accepted. */
	private boolean					negativeAccepted;
	
	/** The accepted characters. */
	private String					accepted;
	
	/**
	 * Updates the <code>foreground</code> color depending on the text entered.
	 */
	private void updateForeGround()
	{
		String text = getText();
		if (editedColor != null) {
			if (originalText != null) {
				if (originalText.equals(text)) setForeground(defaultForeground);
				else setForeground(editedColor);
			}
		}
		if (originalText == null) {
			originalText = text;
			defaultForeground = getForeground();
		}
		firePropertyChange(TEXT_UPDATED_PROPERTY, Boolean.valueOf(false),
				Boolean.valueOf(true));
	}
	
	/**
	 * Creates a default instance with {@link Double#MIN_VALUE} as min value
	 * and {@link Double#MAX_VALUE} as max value.
	 */
	public NumericalTextField()
	{
		this(0.0, Double.MAX_VALUE);	
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param min 	The minimum value of the text field.
	 * @param max 	The maximum value of the text field.
	 */
	public NumericalTextField(double min, double max)
	{
		document = new NumericalPlainDocument(min, max);
		setHorizontalAlignment(JTextField.LEFT);
		setDocument(document);
		originalText = null;
		editedColor = null;
		document.addDocumentListener(this);
		addFocusListener(this);
		numberType = Integer.class;
		accepted = NUMERIC;
		setNegativeAccepted(min < 0);
	}
	
	/**
	 * Sets to <code>true</code> if negative values are accepted,
	 * to <code>false</code> otherwise.
	 * 
	 * @param negativeAccepted The value to set.
	 */
	public void setNegativeAccepted(boolean negativeAccepted)
	{
		this.negativeAccepted = negativeAccepted;
		if (negativeAccepted) {
			accepted += "-";
			double min = document.getMinimum();
			if (min >= 0) {
				if (numberType == null || Integer.class.equals(numberType))
					min = Integer.MIN_VALUE;
				else min = Double.MIN_VALUE;
				document.setMinimum(min);
			}
		}
	}
	
	/**
	 * Returns <code>true</code> if negative values are accepted,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isNegativeAccepted() { return negativeAccepted; }
	
	/**
	 * Sets the type of number to handle.
	 * 
	 * @param numberType The value to set.
	 */
	public void setNumberType(Class numberType)
	{ 
		if (numberType == null)
			numberType = Integer.class;
		this.numberType = numberType;
		if (numberType.equals(Integer.class)) {
			accepted = NUMERIC;
		} else accepted = FLOAT;
		setNegativeAccepted(negativeAccepted);	
	}
	
	/**
	 * Sets the minimum value.
	 * 
	 * @param min The value to set.
	 */
	public void setMinimum(double min)
	{ 
		document.setMinimum(min);
		if (min < 0) setNegativeAccepted(true);
	}
	
	/**
	 * Sets the maximum value.
	 * 
	 * @param max The value to set.
	 */
	public void setMaximum(double max) { document.setMaximum(max); }
	
	/**
	 * Returns the maximum value.
	 * 
	 * @return See above.
	 */
	public double getMaximum() { return document.getMaximum(); }
	
	/**
	 * Returns the minimum value.
	 * 
	 * @return See above.
	 */
	public double getMinimum() { return document.getMinimum(); }
	
	/**
	 * Sets the edited color. 
	 * 
	 * @param editedColor The value to set.
	 */
	public void setEditedColor(Color editedColor)
	{ 
		this.editedColor = editedColor;
	}
	
	/**
	 * Returns the value as a number.
	 * 
	 * @return See above.
	 */
	public Number getValueAsNumber()
	{
		String str = getText();
		if (str == null || str.trim().length() == 0) return null;
		if (Integer.class.equals(numberType))
			return Integer.parseInt(str);
		else if (Double.class.equals(numberType))
			return Double.parseDouble(str);
		else if (Float.class.equals(numberType)) 
			return Float.parseFloat(str);
		return null;
	}
	
	/**
     * Updates the <code>foreground</code> color depending on the text entered.
     * @see DocumentListener#insertUpdate(DocumentEvent)
     */
	public void insertUpdate(DocumentEvent e) { updateForeGround(); }

	/**
     * Updates the <code>foreground</code> color depending on the text entered.
     * @see DocumentListener#removeUpdate(DocumentEvent)
     */
	public void removeUpdate(DocumentEvent e) { updateForeGround(); }
	
	/**
	 * Adds a <code>0</code> if the value of the field ends up with a
	 * <code>.</code>
	 *  @see FocusListener#focusLost(FocusEvent)
	 */
	public void focusLost(FocusEvent e)
	{
		String s = getText();
		if (s != null && s.endsWith(".")) {
			s += "0";
			setText(s);
		}
	}

    /**
     * Required by the {@link DocumentListener} I/F but no-op implementation
     * in our case.
     * @see DocumentListener#changedUpdate(DocumentEvent)
     */
	public void changedUpdate(DocumentEvent e) {}

	/**
     * Required by the {@link FocusListener} I/F but no-op implementation
     * in our case.
     * @see FocusListener#focusGained(FocusEvent)
     */
	public void focusGained(FocusEvent e) {}
	
	/**
	 * Inner class to make sure that we can only enter numerical value.
	 */
	class NumericalPlainDocument 
		extends PlainDocument
	{
		
		/** The minimum value of the text field. */
		private double min;
		
		/** The maximum value of the text field. */
		private double max;
		
		/**
		 * Returns <code>true</code> if the passed string is in the
		 * [min, max] range if a range is specified, <code>false</code> 
		 * otherwise.
		 * 
		 * @param str The string to handle.
		 * @return See above
		 */
		private boolean isInRange(String str)
		{
			try {
				if (Integer.class.equals(numberType)) {
					int val = Integer.parseInt(str);
					int m = (int) min;
					int mx = (int) max;
		            if (m <= val && val <= mx) return true;
				} else if (Double.class.equals(numberType)) {
					double val = Double.parseDouble(str);
		            if (min <= val && val <= max) return true;
				} else if (Float.class.equals(numberType)) {
					float val = Float.parseFloat(str);
		            if (min <= val && val <= max) return true;
				}
	        } catch(NumberFormatException nfe) {}
	       return false;
		}
		
		/**
		 * Creates a new instance.
		 * 
		 * @param min The minimum value.
		 * @param max The maximum value.
		 */
		NumericalPlainDocument(double min, double max)
		{
			this.min = min;
			this.max = max;
		}
		
		/**
		 * Sets the minimum value.
		 * 
		 * @param min The value to set.
		 */
		void setMinimum(double min) { this.min = min; }
		
		/**
		 * Sets the maximum value.
		 * 
		 * @param max The value to set.
		 */
		void setMaximum(double max) { this.max = max; }
		
		/** 
		 * Returns the minimum value.
		 * 
		 * @return See above.
		 */
		double getMinimum() { return min; }
		
		/** 
		 * Returns the maximum value.
		 * 
		 * @return See above.
		 */
		double getMaximum() { return max; }
		
		/**
		 * Overridden to make sure that the value inserted is a numerical
		 * value in the defined range.
		 * @see PlainDocument#insertString(int, String, AttributeSet)
		 */
		public void insertString(int offset, String str, AttributeSet a)
		{
			try {
				if (str == null) return;
				for (int i = 0; i < str.length(); i++) {
		            if (accepted.indexOf(String.valueOf(str.charAt(i))) == -1) {
		            	return;
		            }
		        }
				
				if (accepted.equals(FLOAT) ||
				   (accepted.equals(FLOAT+"-") && negativeAccepted)) {
					if (str.indexOf(".") != -1) {
						if (getText(0, getLength()).indexOf(".") != -1) {
							return;
						}	
					}
				}
				if (negativeAccepted && str.indexOf("-") != -1) {
					if (str.indexOf("-") != 0 || offset != 0 ) {
						return;
					}
				}
				
				if (str.equals(".") && accepted.equals(FLOAT)) {
					super.insertString(offset, str, a);
				} else if (str.equals("-") && negativeAccepted) {
					super.insertString(offset, str, a);
				} else {
					String s = this.getText(0, this.getLength());
					s += str;
					if (isInRange(s))
						super.insertString(offset, str, a);
				}
			} catch (Exception e) {
				Toolkit.getDefaultToolkit().beep();
			}
    	}
	}
	
}
