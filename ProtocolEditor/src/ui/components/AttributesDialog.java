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

package ui.components;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JDialog;

import tree.DataFieldConstants;
import tree.IAttributeSaver;
import tree.IDataFieldObservable;
import tree.DataFieldObserver;

public class AttributesDialog extends JDialog implements DataFieldObserver{
	
	JComponent parent;
	
	IDataFieldObservable dataFieldObs;
	Box customAttributesBox;
	ArrayList<AttributeEditor> customAttributesFields = new ArrayList<AttributeEditor>();
	
	
	public AttributesDialog(JComponent parent, IDataFieldObservable dataFieldObs) {
		
		this.parent = parent;
		
		this.dataFieldObs = dataFieldObs;
		dataFieldObs.addDataFieldObserver(this);
		
		setModal(false);
		setUndecorated(true);
		
		customAttributesBox = Box.createVerticalBox();
		displayAllAttributes();
		getContentPane().add(customAttributesBox, BorderLayout.CENTER);
		
		pack();
		
	}
	
	public void showAttributesDialog() {
		setLocationRelativeTo(parent);
		setVisible(true);
	}
	
	public void dataFieldUpdated() {
		updateValues();
		// showAttributesDialog();
	}
	

	public void displayAllAttributes() {
		if (dataFieldObs instanceof IAttributeSaver) {
			IAttributeSaver dataField = (IAttributeSaver)dataFieldObs;
			
			LinkedHashMap<String, String> allAttributes = (LinkedHashMap)dataField.getAllAttributes();
			
			Iterator keyIterator = allAttributes.keySet().iterator();
			
			while (keyIterator.hasNext()) {
				String name = (String)keyIterator.next();
				String value = allAttributes.get(name);
				
				// don't display these attributes
				if ((name.equals(DataFieldConstants.ELEMENT_NAME )) || (name.equals(DataFieldConstants.INPUT_TYPE))
						|| (name.equals(DataFieldConstants.SUBSTEPS_COLLAPSED)) || (name.equals(DataFieldConstants.TEXT_NODE_VALUE))) continue;
				
				AttributeEditor attributeEditor = new AttributeEditor(dataField, name, value);
				
				// keep a list of fields
				customAttributesFields.add(attributeEditor);
				customAttributesBox.add(attributeEditor);
			}
		}
	}
		
	public void updateValues() {
		if (dataFieldObs instanceof IAttributeSaver) {
			IAttributeSaver dataField = (IAttributeSaver)dataFieldObs;
			for (AttributeEditor field: customAttributesFields) {
				String attribute = field.getTextField().getName();
				String value = dataField.getAttribute(attribute);
				field.getTextField().setText(value);
			}
		}
	}
}
	

