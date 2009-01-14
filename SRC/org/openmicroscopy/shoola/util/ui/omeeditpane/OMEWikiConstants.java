/*
 * org.openmicroscopy.shoola.util.ui.omeeditpane.OMEWikiConstants 
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
package org.openmicroscopy.shoola.util.ui.omeeditpane;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class OMEWikiConstants 
{

	/** Identifies the <code>id</code> token. */
	static final String	REF_ID = "id";
	
	/** Identifies the <code>name</code> token. */
	static final String	REF_NAME = "name";
	
	/** Regex expression for number. */
	static  final String NUMBERREGEX = "[a-zA-Z0-9]";
	
	/** Regex expression for text. */
	static final String TEXTREGEX = "[-a-zA-Z0-9+&@#/%?~_|!:,.;]*";
	
	/** Regex for a sentence. */
	static final String SENTENCEREGEX = "[-a-zA-Z0-9+&@#/%?~_|!:,. ;]*";
	
	/** Regex for a sequence of characters. */
	static final String CHARACTERREGEX = "[a-zA-Z]+[a-zA-Z0-9]+";
	
	/** Regex for a wiki link. */
	static final String WIKILINKREGEX = "\\[\\["+SENTENCEREGEX+"\\]\\]";
	
	/** Regex expression defining Thumbnail [Thumbnail: id 30]. */
	static final String THUMBNAILREGEX = "\\[(Thumbnail|thumbnail):[ ]*(id|ID|name|Name)[ ]+[a-zA-Z0-9]+[ ]*\\]";

	/** Regex expression defining Dataset [Dataset: id 30]. */
	static final String DATASETREGEX = "\\[(Dataset|dataset):[ ]*(id|ID|name|Name)[ ]+[a-zA-Z0-9]+[ ]*\\]";

	/** Regex expression defining Project [Project: id 30]. */
	static final String PROJECTREGEX = "\\[(Project|project):[ ]*(id|ID|name|Name)[ ]+[a-zA-Z0-9]+[ ]*\\]";
	
	/** Regex expression defining Image [Image: id 30]. */
	static final String IMAGEREGEX = "\\[(Image|image):[ ]*(id|ID|name|Name)[ ]+[a-zA-Z0-9]+[ ]*\\]";
	
	/** Regex expression defining Protocol [Protocol: id 30]. */
	static final String PROTOCOLREGEX = "\\[(Protocol|Protocol):[ ]*(id|ID|name|Name)[ ]+[a-zA-Z0-9]+[ ]*\\]";

	/** Regex expression defining Wiki Heading. */
	static final String HEADINGREGEX = "(^[=]{3}[ ]+"+SENTENCEREGEX+"[ ]+[=]{3}[ ]*$|^[=]{2}[ ]+"+SENTENCEREGEX+"[ ]+[=]{2}[ ]*$|^[=]{1}[ ]+"+SENTENCEREGEX+"[ ]+[=]{1}[ ]*$)";
			
	/** Regex for a bullet list. */
	static final String BULLETREGEX = "^\\*[ ]+"+SENTENCEREGEX;

	/** Regex for bold. */
	static final String BOLDREGEX = "'''"+SENTENCEREGEX+"'''";

	/** Italic regex. */
	static final String ITALICREGEX = "''"+SENTENCEREGEX+"''";

	/** Italic and bold regex. */
	static final String ITALICBOLDREGEX = "'''''"+SENTENCEREGEX+"'''''";

	/** Indent regex. */
	static final String INDENTREGEX = "^[:]+"+SENTENCEREGEX+"$";
		
	/** Regex expression defining url. */
	static final String URLREGEX = "(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
	
	/** Regex for names linked regex. */
	static final String NAMEDLINKREGEX = "\\["+URLREGEX+"[ ]+"+SENTENCEREGEX+"\\]";
	
	/** The tooltip of the hyperlink button. */
	static final String HYPERLINK_TOOLTIP = "Link: http://www.example.com";
	
	/** The tooltip for an image's control. */
	static final String IMAGE_TOOLTIP = "[Image: id 1]";
	
	/** The tooltip for an protocol's control. */
	static final String PROTOCOL_TOOLTIP = "[Protocol: id 1]";
	
	/** The default text for a link. */
	static final String DEFAULT_HYPERLINK = "http://";
	
	/** The default text for a protocol's link. */
	static final String DEFAULT_PROTOCOL = "[Protocol: id 1]";
	
	/** The default text for an image's link. */
	static final String DEFAULT_IMAGE = "[Image: id 1]";
}
