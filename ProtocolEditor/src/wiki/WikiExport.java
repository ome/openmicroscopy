 /*
 * wiki.WikiExport 
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
package wiki;

import java.io.File;
import java.util.List;

import tree.DataFieldNode;
import util.DefaultExport;

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
public class WikiExport extends DefaultExport { 
	
	
	public WikiExport() {
		HEADER = "";
	
		FOOTER = "";

		DIV = "";
		DIV_CLASS_PROTOCOL = "";
		DIV_CLASS_ATTRIBUTE = "";
		DIV_END = "";

		SPAN_CLASS_ELEMENT_NAME = "== ";
		SPAN_END = " ==";

		UNDERLINE = " '' ";
		UNDERLINE_END = " '' ";

		TABLE = "<table cellspacing='1' cellpadding='5' bgcolor='black'>";
		TABLE_END = "</table>";
		TABLE_ROW = "<tr>";
		TABLE_ROW_END = "</tr>";
		TABLE_DATA = "<td bgcolor='#eeeeee'>";
		TABLE_DATA_END = "</td>";
	}
	
	/**
	 * Exports the given dataField nodes as a String. 
	 * Overrides this method to remove br elements. 
	 * 
	 * @see DefaultExport#exportToString(List)
	 * 
	 * @param rootNodes		A list of the nodes to export
	 * @return String		A string representation of the export. 
	 */
	public String exportToString(List<DataFieldNode> rootNodes) 
	{
		String text = super.exportToString(rootNodes);
		return text.replace("<br>", "");
	}

}

