/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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

package omeXml;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import util.FileDownload;
import util.XMLMethods;

public class OmeXmlQueryer {

	File xmlFile;
	Document document;
	static OmeXmlQueryer singleInstance;
	
	/* return the single instance of this class*/
	public static OmeXmlQueryer getInstance() {
		if (singleInstance == null)
			singleInstance = new OmeXmlQueryer();
		return singleInstance;
	}
	
	/* private constructor
	 * get an OME-XML file from the cvs web-site
	 * turn it into a DOM Document
	 */
	private OmeXmlQueryer() {
		
		try {
			xmlFile = FileDownload.downloadFile("http://cvs.openmicroscopy.org.uk/svn/specification/Xml/Working/completesample.xml", "temp");
			document = XMLMethods.readXMLtoDOM(xmlFile);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	/* given an element name, returns a list of child names */
	public ArrayList<String> getPossibleChildElementNames(String tagname) {
		
		ArrayList<String> childNames = new ArrayList<String>();
		
		NodeList allNodesWithTagname = document.getElementsByTagName(tagname);
		
		// if more than 1 element match this tagName, want to use the one with most children
		Element element = null;
		int maxChildCount = 0;
		for (int i=0; i < allNodesWithTagname.getLength(); i++) {
			Node node = allNodesWithTagname.item(i);
			int childCount = node.getChildNodes().getLength();
			if (childCount > maxChildCount) {
				element = (Element)allNodesWithTagname.item(i);
				maxChildCount = childCount;
			}	
		}

		NodeList children = element.getChildNodes();
		
		for (int i=0; i < children.getLength(); i++) {
			
			// skip any empty (text) nodes
			Node node = children.item(i);
			
			if (node != null && (node.getNodeType() == Node.ELEMENT_NODE)) {
				childNames.add(node.getNodeName());
			}
		}
		
		return childNames;
	}
}
