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

import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.border.EtchedBorder;

import tree.DataField;
import ui.components.AttributeEditor;

public class FieldEditorProtocol extends FieldEditor {
	
	private AttributeEditor keywordsFieldEditor;
	
	public FieldEditorProtocol (DataField dataField) {
		
		super(dataField);
		
//		 comma-delimited set of search words
		String keywords = dataField.getAttribute(DataField.KEYWORDS);

		// can't change the protocol field to a different type
		inputTypeSelector.setEnabled(false);
		
		keywordsFieldEditor = new AttributeEditor
			(dataField, "Keywords: ", DataField.KEYWORDS, keywords);
		keywordsFieldEditor.setToolTipText("Add keywords, separated by commas");
		attributeFieldsPanel.add(keywordsFieldEditor);
		
	}
	
	// called when dataField changes attributes
	public void dataFieldUpdated() {
		super.dataFieldUpdated();
		keywordsFieldEditor.setTextFieldText(dataField.getAttribute(DataField.KEYWORDS));
	}

}
