 /*
 * treeEditingComponents.NumberEditor 
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
package treeEditingComponents;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;

import treeModel.fields.IParam;
import treeModel.fields.SingleParam;
import uiComponents.UIsizes;
import uiComponents.CustomLabel;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * This is the UI component for displaying the Number Field (with units label)
 * and editing the number. 
 * It delegates the editing to the TextFieldEditor. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class NumberEditor 
	extends Box 
	implements ITreeEditComp,
	PropertyChangeListener {
	
	private IParam param;
	
	public NumberEditor(IParam param) {
		super(BoxLayout.X_AXIS);
		
		this.param = param;
		
		TextFieldEditor numberField = new TextFieldEditor(param, 
				SingleParam.PARAM_VALUE);
		int minW = UIsizes.getInstance().
			getDimension(UIsizes.NUMB_FIELD_MIN_WIDTH);
		//numberField.setMinWidth(minW);
		numberField.addPropertyChangeListener(ITreeEditComp.VALUE_CHANGED_PROPERTY, 
				this);
		
		add(numberField);
		
		add(Box.createHorizontalStrut(10));
		
		String units = param.getAttribute(SingleParam.PARAM_UNITS);
		add(new CustomLabel(units));
	}

	/**
	 * This doesn't need to do anything, since the propertyChangeEvent 
	 * will come from the number field itself
	 */
	public void attributeEdited(String attributeName, String newValue) {
		
	}

	/**
	 * This is the only attribute that you can modify from this class. 
	 */
	public String getAttributeName() {
		return SingleParam.PARAM_VALUE;
	}

	public IParam getParameter() {
		return param;
	}

	/**
	 * Simply pass on the property change event. 
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		this.firePropertyChange(evt.getPropertyName(), 
				evt.getOldValue(), evt.getNewValue());
	}

}
