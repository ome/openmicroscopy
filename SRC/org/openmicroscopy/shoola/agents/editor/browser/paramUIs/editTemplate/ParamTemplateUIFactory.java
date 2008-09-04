 /*
 * org.openmicroscopy.shoola.agents.editor.browser.paramUIs.editTemplate.
 * ParamTemplateUIFactory 
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
package org.openmicroscopy.shoola.agents.editor.browser.paramUIs.editTemplate;

//Java imports

import javax.swing.JComponent;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.browser.paramUIs.DateTimeField;
import org.openmicroscopy.shoola.agents.editor.browser.paramUIs.TextBoxEditor;
import org.openmicroscopy.shoola.agents.editor.model.DataFieldConstants;
import org.openmicroscopy.shoola.agents.editor.model.params.AbstractParam;
import org.openmicroscopy.shoola.agents.editor.model.params.IParam;
import org.openmicroscopy.shoola.agents.editor.model.params.SingleParam;

/** 
 * A Factory for creating the UI components for editing the Template part
 * of Parameter objects, e.g. default values, etc.
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ParamTemplateUIFactory {

	/**
	 * Creates UI components for editing the Template part of 
	 * a Parameter object.
	 * 
	 * @param paramObject		The Parameter you want to edit
	 * @return					A UI component for editing the template.
	 */
	public static JComponent getEditDefaultComponent(IParam paramObject) 
	{
		
		if (paramObject == null) {
			return null;
		}
		String inputType = paramObject.getAttribute(AbstractParam.PARAM_TYPE);
		
		if (inputType == null) {
			return null;
		}
		
		if (inputType.equals(SingleParam.TEXT_LINE_PARAM)) {
			return new AttributeEditLine(paramObject, 
					SingleParam.DEFAULT_VALUE, "Default Text");
		} 
		
		if (inputType.equals(SingleParam.ENUM_PARAM)) {
			return new EnumTemplate(paramObject);
		}
		
		if (inputType.equals(DataFieldConstants.DATE_TIME_FIELD)) {
			return null;
		} 
		
		if (inputType.equals(SingleParam.TEXT_BOX_PARAM)) {
			return new AttributeEditArea(paramObject,
					SingleParam.DEFAULT_VALUE, "Text-box Default");
		}
		
		return null;
	}
}
