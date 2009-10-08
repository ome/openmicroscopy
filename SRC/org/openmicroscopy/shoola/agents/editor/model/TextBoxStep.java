 /*
 * org.openmicroscopy.shoola.agents.editor.model.TextBoxStep 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.editor.model;

import org.openmicroscopy.shoola.agents.editor.model.params.AbstractParam;
import org.openmicroscopy.shoola.agents.editor.model.params.FieldParamsFactory;
import org.openmicroscopy.shoola.agents.editor.model.params.IParam;
import org.openmicroscopy.shoola.agents.editor.model.params.TextBoxParam;
import org.openmicroscopy.shoola.agents.editor.model.params.TextParam;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * This is a Field/Step that has a single Text-Box parameter.
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class TextBoxStep 
	extends Field {
	
	/**
	 * Creates an instance of this field/step without adding the text-box
	 * parameter that this step should have. 
	 * In this case, the Text-Box parameter should be added immediately 
	 * with the {@link #addContent(IFieldContent)} method. 
	 */
	public TextBoxStep()
	{
		super();
	}
	
	/**
	 * Creates an instance of this class and adds the single text-box 
	 * parameter that this class expects.
	 * 
	 * @param textValue			The text value of the text box. If null, not set
	 */
	public TextBoxStep(String textValue)
	{
		this();
		
		IParam p = FieldParamsFactory.getFieldParam(TextBoxParam.TEXT_BOX_PARAM);
		if (textValue != null) {
			p.setAttribute(TextParam.PARAM_VALUE, textValue);
		}
		super.addContent(p);
	}
	
	/**
	 * This returns the single Text-Box parameter of this Step. 
	 * Should be only 1. But it returns the first one, or null if not found. 
	 * 
	 * @return	see above. 
	 */
	public IParam getTextBoxParam()
	{
		int contentCount = getContentCount();
		
		IFieldContent fc;
		for (int i = 0; i < contentCount; i++) {
			fc = getContentAt(i);
			if (fc instanceof TextBoxParam) {
				return (IParam)fc;
			}
		}
		return null;
	}
	
	/**
	 * Implemented as specified by the {@link IField} interface.
	 * Don't allow adding of parameters, except TextBoxParam
	 */
	public void addContent(IFieldContent param) {
		if ((param != null) && (! (param instanceof AbstractParam)))
			super.addContent(param);
		
		
		if ((param != null) && (param instanceof TextBoxParam)) {
			super.addContent(param);
		}
	}
	
	/**
	 * Implemented as specified by the {@link IField} interface.
	 * Don't allow adding of parameters
	 */
	public void addContent(int index, IFieldContent param) 
	{
		if ((param != null) && (! (param instanceof AbstractParam)))
			super.addContent(index, param);
	}
	
	/**
	 * Implemented as specified by the {@link IField} interface.
	 * Won't remove TextBoxParam
	 */
	public int removeContent(IFieldContent param) 
	{
		if (param instanceof TextBoxParam) {
			return 0;
		}
		return super.removeContent(param);
	}
	
	/**
	 * Implemented as specified by the {@link IField} interface.
	 * Won't remove TextBoxParam 
	 */
	public void removeContent(int index) 
	{
		if (! (getContentAt(index) instanceof TextBoxParam)) {
			super.removeContent(index);
		}
	}
	

	/**
	 * Overridden to return "Comment Step";
	 * 
	 * @see Field#getToolTipText()
	 */
	public String getToolTipText() {
		return "Comment Step";
	}

}
