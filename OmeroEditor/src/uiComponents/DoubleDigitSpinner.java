 /*
 * treeEditingComponents.DoubleDigitSpinner 
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
package uiComponents;

import java.awt.Dimension;

import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class DoubleDigitSpinner 
	extends JSpinner {
	
	private SpinnerModel spinnerModel;
	
	/**
	 * Bound property of this spinner
	 */
	public static final String SPINNER_VALUE = "spinnerValue";
	
	/**
	 * A copy of the current value, used for reporting the "old" value
	 * by the property changed listener. 
	 */
	private int currentValue;
	
	public DoubleDigitSpinner(int value, int min, int max, int stepSize) {
		
		super();
		
		spinnerModel = new SpinnerNumberModel(value, min, max, stepSize);
		setModel(spinnerModel);
		
		currentValue = value;
		
		/*
		 * Set the format of the editor, based on the max value.
		 * eg if max is "23", then 3 will be formatted as "03".
		 * if max is 100, then 3 will be formatted as "003" etc. 
		 */
		int digits = Integer.toString(max).length();
		String zeros = "";
		for (int i=0; i<digits; i++) {
			zeros = zeros + "0";
		}
		JSpinner.NumberEditor doubleDigitEditor = 
			new JSpinner.NumberEditor(this, zeros);
		setEditor(doubleDigitEditor);
		
		/*
		 * Set dimensions based on the ComponentSizesRegistry
		 */
		int w = CompSizesReg.getDimension(CompSizesReg.SPINNER_W);
		int h = CompSizesReg.getDimension(CompSizesReg.SPINNER_H);
		Dimension dim = new Dimension (w, h);
		this.setPreferredSize(dim);
		this.setMaximumSize(dim);
		
		/*
		 * Add an ChangListener
		 */
		this.addChangeListener(new ChangeListener() {
			
			public void stateChanged(ChangeEvent evt) {
				valueChanged();
			}
		});
	}

	public void valueChanged() {
		int newValue = new Integer(spinnerModel.getValue().toString());
		this.firePropertyChange(SPINNER_VALUE, currentValue, newValue);
		
		currentValue = newValue;
	}
}
