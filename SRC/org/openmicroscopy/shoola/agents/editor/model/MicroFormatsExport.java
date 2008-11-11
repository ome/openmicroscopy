 /*
 * org.openmicroscopy.shoola.agents.editor.model.MicroFormatsExport 
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
package org.openmicroscopy.shoola.agents.editor.model;

import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.XMLElement;

import org.openmicroscopy.shoola.agents.editor.model.params.AbstractParam;
import org.openmicroscopy.shoola.agents.editor.model.params.DateTimeParam;
import org.openmicroscopy.shoola.agents.editor.model.params.IParam;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class MicroFormatsExport 
	extends XMLexport {

	/**
	 * Handles the creation of an XHTML element for a parameter.
	 * 
	 * @param param
	 * @return
	 */
	IXMLElement createParamElement(IParam param) 
	{
		if (param instanceof DateTimeParam) {
			DateTimeParam dtParam = (DateTimeParam)param;
			
			IXMLElement vEventElement = new XMLElement(paramTag);
			addClassAttribute(vEventElement, "vevent OMERO");
			
			IXMLElement summary = new XMLElement("abbr");
			addClassAttribute(summary, "summary");
			summary.setAttribute("title", "OMERO.editor date");
			// content is string representation of parameter
			summary.setContent(param.toString());
			vEventElement.addChild(summary);
			
			String date = dtParam.getYYYYMMDD();
			
			IXMLElement startDate = new XMLElement("span");
			addClassAttribute(startDate, "dtstart");
			startDate.setContent(date);
			vEventElement.addChild(startDate);
			
			IXMLElement endDate = new XMLElement("span");
			addClassAttribute(endDate, "dtend");
			endDate.setContent(date);
			vEventElement.addChild(endDate);
			
			return vEventElement;
		}
		else {
			return super.createParamElement(param);
		}
	}
}
