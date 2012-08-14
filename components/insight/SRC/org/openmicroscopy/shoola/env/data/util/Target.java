/*
 * org.openmicroscopy.shoola.env.data.util.Target 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2012 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//Third-party libraries

//Application-internal dependencies

/** 
 * Creates the possible stylesheets.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class Target {

	/** The <i>schema</i> attribute. */
	static String SCHEMA = "schema";
	
	/** The <i>target</i> name. */
	static String TARGET = "target";
	
	/** The <i>transform</i> name. */
	private static String TRANSFORM = "transform";
	
	/** The <i>file</i> attribute. */
	private static String FILE = "file";
	
	/** The <i>info</i> attribute. */
	private static String INFO = "info";
	
	/** The <i>quality</i> attribute. */
	private static String QUALITY = "quality";
	
	/** The name of the schema.*/
	private String schema;
	
	/** The quality of the transformation.*/
	private String quality;
	
	/** The information of the transformation.*/
	private String info;
	
	/** 
	 * The collection of transformations to apply to go to the specified
	 * <code>schema</code>.
	 */
	private List<String> transforms;
	
	/** 
	 * The collection of transformations to apply to go to the specified
	 * <code>schema</code>.
	 */
	private List<InputStream> styleSheets;
	
	/** The node to parse.*/
	private Element node;
	
	/** Populates the collection of transformations.
	 * 
	 * @param node The node to handle.
	 */
	private void populate(Node node)
		throws Exception
	{
		Node attribute;
		NamedNodeMap map = node.getAttributes();
		for (int j = 0; j < map.getLength(); j++) {
			attribute = map.item(j);
			if (FILE.equals(attribute.getNodeName()))
				transforms.add(attribute.getNodeValue());
		}
	}
	
	/**
	 * Creates a target entry.
	 * 
	 * @param node The node to handle.
	 */
	Target(Element node)
	{
		if (node == null) 
			throw new IllegalArgumentException("No node to handle.");
		transforms = new ArrayList<String>();
		styleSheets = new ArrayList<InputStream>();
		this.node = node;
	}
	
	/**
	 * Replaces the string by the corresponding input stream.
	 * 
	 * @param values The values to set.
	 */
	void formatTransforms(Map<String, InputStream> values)
	{
		Iterator<String> i = transforms.iterator();
		String key;
		Entry e;
		Iterator j;
		String value;
		while (i.hasNext()) {
			key = i.next();
			j = values.entrySet().iterator();
			while (j.hasNext()) {
				e = (Entry) j.next();
				value = (String) e.getKey();
				if (value.contains(key)) {
					styleSheets.add((InputStream) e.getValue());
				}
			}
		}
	}
	
	/**
	 * Parses the node.
	 * 
	 * @throws Exception Thrown if an error occurred during the parsing.
	 */
	void parse()
		throws Exception
	{
		Node attribute;
		NamedNodeMap map;
		NodeList transforms;
		map = node.getAttributes();
		for (int j = 0; j < map.getLength(); j++) {
			attribute = map.item(j);
			if (INFO.equals(attribute.getNodeName())) {
				info = attribute.getNodeValue();
			} else if (QUALITY.equals(attribute.getNodeName())) {
				quality = attribute.getNodeValue();
			} else if (SCHEMA.equals(attribute.getNodeName())) {
				schema = attribute.getNodeValue();
			}
		}
		transforms = node.getElementsByTagName(TRANSFORM);
		for (int j = 0; j < transforms.getLength(); j++) {
			populate(transforms.item(j));
		}
	}
	
	/**
	 * Returns the collection of style-sheets to apply.
	 * 
	 * @return See above.
	 */
	public List<InputStream> getTransforms() { return styleSheets; }
	
	/**
	 * Returns the name of the schema.
	 * 
	 * @return See above.
	 */
	public String getSchema() { return schema; }
	
	/**
	 * Returns the quality of the transformation
	 * 
	 * @return See above.
	 */
	public String getQuality() { return quality; }
	
	/**
	 * Returns the information of the transformation
	 * 
	 * @return See above.
	 */
	public String getInformation() { return info; }

	/** Closes the streams.*/
	public void close()
	{
		if (styleSheets == null) return;
		Iterator<InputStream> i = styleSheets.iterator();
		while (i.hasNext()) {
			try {
				i.next().close();
			} catch (Exception e) {}
		}
	}
	
	/**
	 * Overridden to return the name of the schema.
	 */
	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append(schema);
		if (quality != null) buffer.append(" (quality:"+quality+")");
		return buffer.toString();
	}

}
