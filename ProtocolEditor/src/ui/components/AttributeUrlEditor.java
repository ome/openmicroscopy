package ui.components;

/*
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
 *	author Will Moore will@lifesci.dundee.ac.uk
 */

import java.net.MalformedURLException;
import java.net.URL;

import tree.IAttributeSaver;

public class AttributeUrlEditor extends AttributeEditor {

	public AttributeUrlEditor(IAttributeSaver dataField, String label,
			String attribute, String value) {
		super(dataField, label, attribute, value);
	}
	
	public AttributeUrlEditor(IAttributeSaver dataField, String attribute,
			String value) {
		super(dataField, attribute, value);
	}
	

	// called to update dataField with attribute
	protected void setDataFieldAttribute(String attributeName, String value, boolean notifyUndoRedo) {
		
		/*
		 * If the user entered a value, check that it is a URL. If not, add "http://"
		 */
		if ((value != null) && (value.length() > 0)) {
			try {
				URL url = new URL(value);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				value = "http://" + value;
			}
		}
		
		// Now set attribute as normal. 
		
		dataField.setAttribute(attributeName, value, notifyUndoRedo);
	}

}
