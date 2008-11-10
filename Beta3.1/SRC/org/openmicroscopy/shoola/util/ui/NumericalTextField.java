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
import java.awt.Toolkit;
import javax.swing.JTextField;
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
{

	/** Helper reference to the document. */
	private NumericalPlainDocument document;
	
	
	/**
	 * Creates a default instance with {@link Integer#MIN_VALUE} as min value
	 * and {@link Integer#MAX_VALUE} as max value.
	 */
	public NumericalTextField()
	{
		this(Double.MIN_VALUE, Double.MAX_VALUE);	
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
		setDocument(document);
	}
	
	/**
	 * Sets the minimum value.
	 * 
	 * @param min The value to set.
	 */
	public void setMinimum(int min) { document.setMinimum(min); }
	
	/**
	 * Sets the maximum value.
	 * 
	 * @param max The value to set.
	 */
	public void setMaximum(int max) { document.setMaximum(max); }
	
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
		 * Returns <code>true</code> if the passed string is an integer,
		 * <code>false</code> otherwise.
		 * 
		 * @param str The string to handle.
		 * @return See above
		 */
		private boolean isInteger(String str)
		{
			for (int i = 0 ; i < str.length(); i++ )
        		if (!Character.isDigit(str.charAt(i)))
        			return false;
    		return true;
		}
		
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
			boolean valid = false;
			int val = 0;
			try {
	            val = Integer.parseInt(str);
	            if (min <= val && val < max) valid = true;
	        } catch(NumberFormatException nfe) {}
	       return valid;
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
		 * Overridden to make sure that the value inserted is a numerical
		 * value in the defined range.
		 * @see PlainDocument#insertString(int, String, AttributeSet)
		 */
		public void insertString(int offset, String str, AttributeSet a) {
			try {
				if (!Character.isISOControl(str.charAt(0))) {
					int length = getLength();
					String text = getText(0, length);
					if (isInteger(str)) {
						switch (length) {
							case 0:
								//if (isInRange(str)) 
								super.insertString(offset, str, a);
								break;
							default:
								if (isInRange(text)) 
									super.insertString(offset, str, a);
						}
					} else Toolkit.getDefaultToolkit().beep();
		    	} else Toolkit.getDefaultToolkit().beep();
			} catch (Exception e) {
				Toolkit.getDefaultToolkit().beep();
			}
    	}
	}
	
}
