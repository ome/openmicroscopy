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

import tree.IDataFieldObservable;

// all dataField attributes are displayed in panel
// used for displaying imported XML elements that may have other attributes

public class FieldEditorCustom extends FieldEditor {

	public FieldEditorCustom(IDataFieldObservable dataFieldObs) {
		
		super(dataFieldObs);
		
		// can't edit custom fields
		nameFieldEditor.getTextArea().setEnabled(false);
		// can't set color attribute (won't be saved in xml)
		colourSelectButton.setEnabled(false);
		
		attributeFieldsPanel.remove(inputTypePanel);
		attributeFieldsPanel.remove(descriptionFieldEditor);
		attributeFieldsPanel.remove(urlFieldEditor);
	}

	
}
