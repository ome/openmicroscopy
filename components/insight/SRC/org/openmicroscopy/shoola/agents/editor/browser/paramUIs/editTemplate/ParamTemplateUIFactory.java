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

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.browser.BrowserControl;
import org.openmicroscopy.shoola.agents.editor.browser.paramUIs.AbstractParamEditor;
import org.openmicroscopy.shoola.agents.editor.model.params.AbstractParam;
import org.openmicroscopy.shoola.agents.editor.model.params.BooleanParam;
import org.openmicroscopy.shoola.agents.editor.model.params.DateTimeParam;
import org.openmicroscopy.shoola.agents.editor.model.params.EditorLinkParam;
import org.openmicroscopy.shoola.agents.editor.model.params.EnumParam;
import org.openmicroscopy.shoola.agents.editor.model.params.IParam;
import org.openmicroscopy.shoola.agents.editor.model.params.NumberParam;
import org.openmicroscopy.shoola.agents.editor.model.params.TextBoxParam;
import org.openmicroscopy.shoola.agents.editor.model.params.TextParam;

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
	public static JComponent getEditDefaultComponent(
							IParam paramObject, BrowserControl controller) 
	{
		
		if (paramObject == null) {
			throw new NullPointerException("No parameter.");
		}
		String inputType = paramObject.getAttribute(AbstractParam.PARAM_TYPE);

		AbstractParamEditor pe = null;
		
		if (inputType.equals(TextParam.TEXT_LINE_PARAM)) {
			pe = new AttributeEditLine(paramObject, 
					TextParam.DEFAULT_VALUE, "Default Text");
		} 
		
		else if (inputType.equals(EnumParam.ENUM_PARAM)) {
			pe = new EnumTemplate(paramObject);
		}
		
		else if (inputType.equals(BooleanParam.BOOLEAN_PARAM)) {
			pe = new BooleanTemplate(paramObject);
		}
		
		else if (inputType.equals(DateTimeParam.DATE_TIME_PARAM)) {
			pe = new DateTimeTemplate(paramObject);
		}
		
		else if (inputType.equals(NumberParam.NUMBER_PARAM)) {
			pe = new NumberTemplate(paramObject);
		}
		
		else if (inputType.equals(TextBoxParam.TEXT_BOX_PARAM)) {
			pe = new AttributeEditArea(paramObject,
					TextParam.DEFAULT_VALUE, "Text-box Default");
		}
		
		else if (inputType.equals(EditorLinkParam.EDITOR_LINK_PARAM)) {
			pe =  new EditorLinkPreview(paramObject, controller);
		}
		
		// Some Parameters don't have any "Template" attributes to edit,
		// so, no template UI component...
		if (pe == null) return null;
		
		pe.setController(controller);
		return pe;
	}
}
