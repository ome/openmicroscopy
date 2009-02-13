 /*
 * treeEditingComponents.ComponentSizesRegistry 
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

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * This class is a central place to manage the pixel sizes of UI components.
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class UIUtilities {

	/**
	 * A light blue colour for highlighting fields, etc. 
	 */
	public static final Color BLUE_HIGHLIGHT = new Color(181,213,255);
	
	/**
	 * The height of a JSpinner
	 */
	public static final Integer SPINNER_H = new Integer(1);
	
	/**
	 * The width of a JSpinner
	 */
	public static final Integer SPINNER_W = new Integer(2);
	
	/**
	 * All components that take up a single row (eg Button, TextField, etc)
	 * should try to be this height. 
	 * This should make everything align nicely. 
	 */
	public static final Integer SINGLE_ROW_HEiGHT = new Integer(3);
	
	/**
	 * The minimum width for a text field. Field should be at least
	 * this wide, but could be longer if the text gets longer?
	 */
	public static final Integer TEXT_FIELD_MIN_WIDTH = new Integer(4);;
	
	/**
	 * The minimum width for a text field that displays a number. 
	 */
	public static final Integer NUMB_FIELD_MIN_WIDTH = new Integer(5);
	
	/**
	 * The width of a Formatted text field for displaying 2 digits.
	 * Eg used for showing Hrs:Mins:Secs in the timer. 
	 */
	public static final Integer DOUBLE_DIGIT_FIELD_W = new Integer(6);
	
	/**
	 * The thinkness of a border around eg. buttons. 
	 */
	public static final Integer EMPTY_BORDER_THINKNESS = new Integer(7);
	
	
	/**
	 * A map of the dimensions
	 */
	private Map<Integer, Integer> dimensions;
	
	/**
	 * A reference to an instance of this class.
	 * Saves a new instance being called for each getInstance();
	 */
	private static UIUtilities instance;
	
	/**
	 * Method to get a new instance of this class. 
	 * 
	 * @return	A new instance of this class
	 */
	public static UIUtilities getInstance() {
		if (instance == null) {
			instance = new UIUtilities();
		}
		return instance;
	}
	
	/**
	 * Creates a new instance and populates the dimenions map. 
	 */
	public UIUtilities() {
		
		dimensions = new HashMap<Integer, Integer>();
		
		dimensions.put(SPINNER_H, 25);
		dimensions.put(SPINNER_W, 45);
		dimensions.put(TEXT_FIELD_MIN_WIDTH, 50);
		dimensions.put(NUMB_FIELD_MIN_WIDTH, 75);
		dimensions.put(SINGLE_ROW_HEiGHT, 18);
		dimensions.put(DOUBLE_DIGIT_FIELD_W, 21);
		dimensions.put(EMPTY_BORDER_THINKNESS, 2);
		
	}
	
	/**
	 * Returns a dimension.
	 * 
	 * @param dimension		The ID of the dimension. 
	 * @return		The pixel size of the dimension. 
	 */
	public int getDimension(int dimension) {
		
		return dimensions.get(dimension);

	}
	
	/**
	 * Convenience method to show a confirmation dialog. 
	 * Returns true if the user hits "OK".
	 * 
	 * @param title			The dialog title
	 * @param message		The message to display
	 * @return
	 */
	public static boolean showConfirmDialog(String title, String message) 
	{
		int n = JOptionPane.showConfirmDialog(null, message, title, 
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
		
		return (n == JOptionPane.OK_OPTION);
	}
	
	/**
	 * Convenience method to show a confirmation dialog. 
	 * Returns true if the user hits "OK".
	 * 
	 * @param title			The dialog title
	 * @param message		The message to display
	 * @return
	 */
	public static void showMessageDialog(String title, String message) 
	{
		JOptionPane.showMessageDialog(null, message, title,
				JOptionPane.INFORMATION_MESSAGE);
	}
	
}
