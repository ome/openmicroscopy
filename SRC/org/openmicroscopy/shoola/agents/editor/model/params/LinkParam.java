 /*
 * org.openmicroscopy.shoola.agents.editor.model.params.LinkParam 
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
package org.openmicroscopy.shoola.agents.editor.model.params;

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
public class LinkParam 
	extends AbstractParam{

	/**
	 * The value of the parameter type attribute that defines this Parameter. 
	 */
	public static final String LINK_PARAM = "LINK";
	
	/**
	 * Used by the LinkParameter to store an absolute file path to a local File. 
	 * eg Word.doc, PDF etc or Editor file. 
	 * This attribute is mutually exclusive with RELATIVE_FILE_PATH and URL_LINK
	 */
	public static final String ABSOLUTE_FILE_LINK = "absoluteFileLink";
	
	/**
	 * Used by the LinkParam to store a file path to an local file, 
	 * eg Word.doc, PDF etc or Editor file. 
	 * This is a relative file path, FROM the editor file in which this 
	 * parameter appears TO the image.
	 * This attribute is mutually exclusive with ABSOLUTE_FILE_PATH and URL_LINK
	 */
	public static final String RELATIVE_FILE_LINK = "relativeFileLink";
	
	/**
	 * Used by the LinkParam to store a URL. 
	 * This URL is part of the "experimental variables" and is 
	 * specific to the Link Parameter.
	 * It is different from the URL "url" attribute, that exists as 
	 * part of the template of all fields. 
	 * This attribute is mutually exclusive with ABSOLUTE_FILE_PATH
	 * and RELATIVE_FILE_PATH.
	 */
	public static final String URL_LINK = "urlLink";;
	
	/**
	 * Creates an instance. 
	 * 
	 * @param fieldType		The String defining the field type
	 */
	public LinkParam(String fieldType) {
		super(fieldType);
	}
	
	@Override
	public String[] getParamAttributes() {
		return new String[] {ABSOLUTE_FILE_LINK, 
				RELATIVE_FILE_LINK,
				URL_LINK};
	}

	@Override
	public boolean isParamFilled() {
		String[] attributes = getParamAttributes();
		for (int i=0; i<attributes.length; i++) {
			// if any attribute is not null, then this field is filled. 
			if (getAttribute(attributes[i]) != null)
				return true;
		}
		return false;
	}
	
	/**
	 * Returns the absolute link path, or (if null) the relative link path,
	 * or (if still null) the url link.
	 */
	public String toString() {
		String text = "";
		
		String path = getAttribute (ABSOLUTE_FILE_LINK);
		if (path == null) path = getAttribute (RELATIVE_FILE_LINK);
		if (path == null) path = getAttribute (URL_LINK);
		
		if (path != null) {
			text = text + path;
		} else {
			text = text + "no link set";
		}
		
		return text;
	}

}
