 /*
 * treeEditingComponents.EditingComponentFactory 
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

import javax.swing.JComponent;
import javax.swing.JPanel;

import tree.DataFieldConstants;

import fields.AbstractParam;
import fields.IParam;
import fields.SingleParam;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * This is a factory that creates UI editing components for IFieldValue objects.
 * Each field in the OMERO.editor may contain one (or more) "value" objects,
 * that model the experimental variables in that field. 
 * These variables, are made editable within appropriate UI components created
 * by this Factory, depending on the String getFieldType().
 * Eg, a text value will be edited by a TextField, a checkBox value will be
 * edited by a checkBox etc. 
 * 
 * These components do not observe changes to the data in the Value objects, as
 * they have done in earlier versions of OMERO.editor. 
 * This is because the JTree in which they are displayed is refreshed on
 * each update, and these editing components are re-built with the new
 * data values. 
 * This occurs very frequently, even when changing the selection path of the 
 * tree. Adding these components as observers of Value objects each time they
 * are created would results in a large number of observers, with no easy 
 * way of de-referencing them? 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class EditingComponentFactory {
	
	public static JComponent getEditingComponent(IParam paramObject) {
		
		String inputType = paramObject.getAttribute(AbstractParam.PARAM_TYPE);
		
		if (inputType == null) {
			return new JPanel();
		}
		
		if (inputType.equals(SingleParam.TEXT_LINE_PARAM)) {
			return new TextFieldEditor(paramObject);
		} 
		
		if (inputType.equals(DataFieldConstants.DATE_TIME_FIELD)) {
			return new DateTimeField(paramObject);
		} 
		
		if (inputType.equals(SingleParam.TEXT_BOX_PARAM)) {
			return new TextBoxEditor(paramObject);
		}
		
		if (inputType.equals(DataFieldConstants.FIXED_PROTOCOL_STEP)) {
			return new JPanel();
		}
		
		return null;
	}
	
	

}
