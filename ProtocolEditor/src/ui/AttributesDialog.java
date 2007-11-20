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

import java.awt.BorderLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;

import tree.DataField;
import tree.DataFieldObserver;

public class AttributesDialog extends JDialog implements DataFieldObserver{
	
	JComponent parent;
	AttributesPanel attributesPanel;
	
	
	public AttributesDialog(JComponent parent, DataField dataField) {
		
		this.parent = parent;
		dataField.addDataFieldObserver(this);
		
		setModal(false);
		setUndecorated(true);
		
		attributesPanel = new AttributesPanel(dataField);
		
		getContentPane().add(attributesPanel, BorderLayout.CENTER);
		
		pack();
		
	}
	
	public void showAttributesDialog() {
		setLocationRelativeTo(parent);
		setVisible(true);
	}
	
	public void dataFieldUpdated() {
		attributesPanel.updateValues();
		showAttributesDialog();
	}
	
	
	public class AttributesPanel extends AbstractDataFieldPanel {
		
		Box customAttributesBox;
		ArrayList<AttributeEditor> customAttributesFields = new ArrayList<AttributeEditor>();
		
		
		public AttributesPanel(DataField dataField) {
			this.dataField = dataField;
			customAttributesBox = Box.createVerticalBox();
			displayAllAttributes();
			this.add(customAttributesBox);
		}
	
		public void displayAllAttributes() {
			LinkedHashMap<String, String> allAttributes = dataField.getAllAttributes();
		
			Iterator keyIterator = allAttributes.keySet().iterator();
		
			while (keyIterator.hasNext()) {
				String name = (String)keyIterator.next();
				String value = allAttributes.get(name);
			
				// don't display these attributes
				if ((name.equals(DataField.ELEMENT_NAME )) || (name.equals(DataField.INPUT_TYPE))
						|| (name.equals(DataField.SUBSTEPS_COLLAPSED)) || (name.equals(DataField.TEXT_NODE_VALUE))) continue;
			
				AttributeEditor attributeEditor = new AttributeEditor(name, value);
			
				// keep a list of fields
				customAttributesFields.add(attributeEditor);
				customAttributesBox.add(attributeEditor);
			}
		}
		
		public void updateValues() {
			for (AttributeEditor field: customAttributesFields) {
				String attribute = field.getTextField().getName();
				String value = dataField.getAttribute(attribute);
				field.getTextField().setText(value);
			}
		}
	}
	
}
