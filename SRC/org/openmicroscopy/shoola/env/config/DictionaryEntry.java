/*
 * org.openmicroscopy.shoola.env.config.DictionaryEntry
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.env.config;

//Java imports
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

//Third-party libraries
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//Application-internal dependencies

/** 
 * Tempo class, should be modified if this approach is really useful for the 
 * user. Eventually an approach similar to the one selected for agent should
 * be implemented.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class DictionaryEntry
	extends Entry
{

	/** The dictionary file. */
	private Document        	document;
	
	/** Collects all tags that we have to handle from the dictionary file. */
	private List	        	entriesTags;
	
	private List	elements;
	
	public DictionaryEntry()
	{
		elements = new ArrayList();		
	}

	/** Implemented as specified by {@link Entry}. */  
	protected void setContent(Node node)
	{
		try {
			String path = node.getFirstChild().getNodeValue();
			//String absPathName = container.resolveConfigFile(configFile);
			parse(node.getFirstChild().getNodeValue());
	   } catch (Exception ex) { throw new RuntimeException(ex); }
		
	}

	/** Implemented as specified by {@link Entry}. */  
	Object getValue() { return elements; }


	/** 
	 * Parses the dictionary file, extracts its entries (only
	 * <code>structuredEntry</code> tags are taken into
	 * account).
	 * 
	 * @throws ConfigException	If an error occurs and the registry can't be
	 * 							filled up.
	 */
	private void parse(String xmlFile)
		throws ConfigException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			System.out.println(xmlFile);
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.parse(new File(xmlFile));
			readConfigEntries();
			Iterator i = entriesTags.iterator();
			while (i.hasNext())
			   retrieveData((Node) i.next());
		} catch (Exception e) { 
			throw new ConfigException("An error occurred while attempting"+
										"to process: "+xmlFile, e); 
		}   
	}

	/** 
	 * Retrieves the content of the tags that we handle.
	 * Stores their DOM representation (DOM node) into a list.
	 */
	private void readConfigEntries()
	{
		NodeList list;
		Node n;
		list = document.getElementsByTagName(Entry.STRUCT_ENTRY);
		for (int i = 0; i < list.getLength(); ++i) {
			System.out.println(i);
			n = list.item(i);
			if (n.hasChildNodes()) entriesTags.add(n);
		}
	}
	
	/** Implemented as specified by {@link Entry}. */    
	private void retrieveData(Node node)
	{ 
		HashMap tagsValues = new HashMap();
		try {
			if (node.hasChildNodes()) {
				NodeList childList = node.getChildNodes();
				Node child;
				for (int i = 0; i < childList.getLength(); i++) {
					child = childList.item(i);
					
					if (child.getNodeType() == Node.ELEMENT_NODE) {
						System.out.println(child.getNodeName());
						tagsValues.put(child.getNodeName(), 
						child.getFirstChild().getNodeValue());
					}
						
				}
				elements.add(tagsValues);
			}  
		} catch (Exception ex) { throw new RuntimeException(ex); }
	}
    
}
