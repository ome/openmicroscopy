 /*
 * org.openmicroscopy.shoola.agents.editor.model.params.EditorLinkParam 
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
package org.openmicroscopy.shoola.agents.editor.model.params;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * This parameter stores a link to another Editor file, either a local file
 * or a link to Editor file on server, using ID. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class EditorLinkParam 
	extends AbstractParam {
	
	/**
	 * This is the param-type string for this parameter. 
	 */
	public static final String 		EDITOR_LINK_PARAM = "editorLinkParam";
	
	/**
	 * An attribute in which to store the name of the server at the time that
	 * editor links to server (in the form of an ID) are created. 
	 * If the file is saved as a local XML file, need to know which server the
	 * linked Editor files are on. 
	 */
	public static final String 		SERVER_NAME = "serverName";

	/**
	 * Creates an instance. 
	 */
	public EditorLinkParam() {
		super(EDITOR_LINK_PARAM);
	}
	
	/**
	 * Simple test for whether a link is a valid ID on the server.
	 * Current implementation simply tests whether it is an integer. 
	 * 
	 * @param link
	 * @return
	 */
	public static boolean isLinkValidId(String link) {
		
		try {
			int integer = Integer.valueOf(link);
			return true;
		} catch (NumberFormatException ex) {
			
			return false;
		}
	}
	
	/**
	 * Returns the value of the parameter. 
	 * 
	 * @see Object#toString()
	 */
	public String toString() {
		
		String text = super.toString();
		
		String value = getParamValue();
		if (value != null) {
			if (isLinkValidId(value)) {
				text = "File ID: " + value;
			} else
			text = value;
		}
		
		return text;
	}

}
