/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
 *	author Will Moore will@lifesci.dundee.ac.uk
 */

package ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import tree.DataFieldConstants;
import tree.IDataFieldObservable;

public class FormFieldMemo extends FormField {
	
	JTextArea textInput;
	
	public FormFieldMemo(IDataFieldObservable dataFieldObs) {
		super(dataFieldObs);
		
		String value = dataField.getAttribute(DataFieldConstants.VALUE);
		
		textInput = new JTextArea(value);
		visibleAttributes.add(textInput);
		textInput.setRows(3);
		textInput.setLineWrap(true);
		textInput.setWrapStyleWord(true);
		JScrollPane textScroller = new JScrollPane(textInput);
		textInput.setMargin(new Insets(3,3,3,3));
		textInput.setPreferredSize(new Dimension(300, 100));
		textInput.addMouseListener(new FormPanelMouseListener());
		textInput.setName(DataFieldConstants.VALUE);
		textInput.addFocusListener(focusChangedListener);
		textInput.addKeyListener(textChangedListener);
		horizontalBox.add(textScroller);
		
		//setExperimentalEditing(false);	// default created as uneditable
	}
	
	
	// overridden by subclasses if they have other attributes to retrieve from dataField
	public void dataFieldUpdated() {
		super.dataFieldUpdated();
		textInput.setText(dataField.getAttribute(DataFieldConstants.VALUE));
	}
	
//	 overridden by subclasses that have input components
	public void setExperimentalEditing(boolean enabled) {
		
		if (enabled) textInput.setForeground(Color.BLACK);
		else textInput.setForeground(textInput.getBackground());
		
		textInput.setEditable(enabled);
	}
}
