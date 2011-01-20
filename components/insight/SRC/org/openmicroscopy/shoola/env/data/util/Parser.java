/*
 * org.openmicroscopy.shoola.env.data.util.Parser 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.data.util;



//Java imports
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
//Third-party libraries

//Application-internal dependencies

/** 
 * Parses file identifying application on Mac, Windows
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
public class Parser
{

	/** Identifies the path to the executable. */
	public static final String EXECUTABLE_PATH = "executablePath";
	
	/** Identifies the icon associated to the executable. */
	public static final String EXECUTABLE_ICON = "executableIcon";
	
	/** Identifies the name of the executable. */
	public static final String EXECUTABLE_NAME = "executableName";
	
	/** Tag identifying the executable. */
	private static final String EXECUTABLE_TAG_MAC = "CFBundleExecutable";
	
	/** Tag identifying the icon associated to the application. */
	private static final String ICON_TAG_MAC = "CFBundleIconFile";
	
	/** Tag identifying the name of the application. */
	private static final String NAME_TAG_MAC = "CFBundleName";

	/** Path to the resources of the application on MAC. */
	public static final String RESOURCES_MAC = "/Contents/Resources/";
	
	/** Path to the resources of the application on MAC. */
	public static final String EXECUTABLE_MAC = "/Contents/MacOS/";
	
	/** The file to look to retrieve the information. */
	private static final String INFO_FILE_MAC = "/Contents/Info.plist";

	/**
	 * Parses the <code>Info.plist</code> file.
	 * 
	 * @param path The path to the file.
	 * @return Parsed objects.
	 * @throws Exception Thrown if an error occurred while parsing the file.
	 */
	public static Map<String, Object> parseInfoPList(String path)
		throws Exception
	{
		Map<String, Object> map = new HashMap<String, Object>();
		DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = f.newDocumentBuilder();
		Document doc = builder.parse(new File(path+INFO_FILE_MAC));
		//Extract the info
		NodeList list = doc.getElementsByTagName("dict");
		Node node, child, s;
		NodeList nodes;
		String value;
		String r;
		for (int i = 0; i < list.getLength() ; i++){
			node = list.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				nodes = node.getChildNodes();
				for (int j = 0; j < nodes.getLength(); j++) {
					child = nodes.item(j);
					if (child.getNodeType() == Node.ELEMENT_NODE &&
							child.getNodeName().equals("key")) {
						value = child.getTextContent().trim();
						if (EXECUTABLE_TAG_MAC.equals(value)) {
							if (child.getNextSibling() != null) {
								s = child.getNextSibling().getNextSibling();
								r = path+EXECUTABLE_MAC
									+s.getTextContent().trim();
								map.put(EXECUTABLE_PATH, r);	
							}
						} else if (ICON_TAG_MAC.equals(value)) {
							if (child.getNextSibling() != null) {
								s = child.getNextSibling().getNextSibling();
								r = path+RESOURCES_MAC
								+s.getTextContent().trim();
								map.put(EXECUTABLE_ICON, r);	
							}
						} else if (NAME_TAG_MAC.equals(value)) {
							if (child.getNextSibling() != null) {
								s = child.getNextSibling().getNextSibling();
								map.put(EXECUTABLE_NAME, 
										s.getTextContent().trim());	
							}
						}
					}
				}
			}//end of if clause
		}
		return map;
	}
	
}
