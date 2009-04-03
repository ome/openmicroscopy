package util;

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

public class HtmlExport extends DefaultExport { 
	
	
	public HtmlExport() {
		HEADER = "<html><head> \n" +
		"<style type='text/css'> \n" +
		"div {padding: 2px 0px 2px 30px; margin: 0px; font-family: Arial;} \n" +
		".protocol {background: #dddddd; padding: 5px; font-size: 120%; border: 1px #390d61 solid;} \n" +
		".elementName {font-size: 110%;} \n" +
		".attribute {font-size: 80%;} \n" +
		".title {background: #dddddd; padding: 5px; font-size: 110%; border-bottom: 1px #390d61 solid;} \n" +
		"h3 {padding: 0px; margin:0px; font-size: 110%;} \n" +
		"</style> \n" +
		"</head><body>";
	
		FOOTER = "</body></html>";

		DIV = "<div>";
		DIV_CLASS_PROTOCOL = "<div class='protocol'>";
		DIV_CLASS_ATTRIBUTE = "<div class='attribute'>";
		DIV_END = "</div>";

		SPAN_CLASS_ELEMENT_NAME = "<span class='elementName'>";
		SPAN_END = "</span>";

		UNDERLINE = "<u>";
		UNDERLINE_END = "</u>";

		String BROWSER_TOOL_TIP = "title='Please return to Protocol Editor to expand or collapse, then print again'";
		RIGHT_ARROW = "<img src='http://morstonmud.com/omero/arrow_right.gif' width='15' height='13'" + BROWSER_TOOL_TIP + ">";
		DOWN_ARROW = "<img src='http://morstonmud.com/omero/arrow_down.gif' width='13' height='15'" + BROWSER_TOOL_TIP + ">";

		TABLE = "<table cellspacing='1' cellpadding='5' bgcolor='black'>";
		TABLE_END = "</table>";
		TABLE_ROW = "<tr>";
		TABLE_ROW_END = "</tr>";
		TABLE_DATA = "<td bgcolor='#eeeeee'>";
		TABLE_DATA_END = "</td>";
	}

}
