/*
 * org.openmicroscopy.shoola.env.data.util.TransformsParser 
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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.openmicroscopy.shoola.util.file.IOUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
//Third-party libraries

//Application-internal dependencies

/** 
 * Parses the specification file. This class retrieves the style sheets used
 * to downgrade OME-TIFF and OME-XML files.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class TransformsParser
{

	/** The jar to find. */
	public static String SPECIFICATION = "specification";
	
	/** The catalog file to find. */
	private static String CATALOG = "ome-transforms.xml";

	/** The <i>name</i> attribute. */
	private static String CURRENT = "current";

	/** The <i>source</i> node. */
	private static String SOURCE = "source";
	
	/** The configuration file. */
    private Document document;
    
    /** The possible downgrade schema.*/
    private List<Target> targets;
    
    /** The jar to load.*/
    private Map<String, InputStream> values;
    
    /** The current schema.*/
    private String current;
    
    /**
     * Extracts the value of the current schema.
     * 
     * @param schema The current value.
     * @throws Exception Thrown when an error occurred while parsing the file.
     */
    private void extractCurrentSchema(String schema)
		throws Exception
	{
		NodeList list = document.getElementsByTagName(SOURCE);
		Element n;
		Node attribute;
		NamedNodeMap map;
		Target target;
		NodeList t;
		for (int i = 0; i < list.getLength(); ++i) {
			n = (Element) list.item(i);
			map = n.getAttributes();
			for (int j = 0; j < map.getLength(); j++) {
				attribute = map.item(j);
				if (Target.SCHEMA.equals(attribute.getNodeName())) {
					if (schema.equals(attribute.getNodeValue())) {
						t = n.getElementsByTagName(Target.TARGET);
						for (int k = 0; k < t.getLength(); k++) {
							target = new Target((Element) t.item(k));
							target.parse();
							target.formatTransforms(values);
							targets.add(target);
						}
					}
				}
			}
		}
	}
    
	/**
	 * Creates a new instance.
	 */
    public TransformsParser()
	{
		targets = new ArrayList<Target>();
	}
	
    /** Closes the input stream.*/
    public void close()
    {
    	Iterator<Target> i = targets.iterator();
    	while (i.hasNext()) {
			i.next().close();
		}
    }
    
    /**
     * Returns the collection of targets.
     * 
     * @return See above.
     */
    public List<Target> getTargets() { return targets; }
    
    /**
     * Returns the value of the current schema.
     * 
     * @return See above.
     */
    public String getCurrentSchema() { return current; }
    
	/**
	 * Parses the catalog.
	 * 
	 * @param path The relative path.
	 * @throws Exception Thrown when an error occurred while parsing the file.
	 */
	public void parse(String path)
		throws Exception
	{
		if (values == null || values.size() == 0)
			values = IOUtil.extractJarFromPath(SPECIFICATION);
		if (values.size() == 0) {
			//going to extract from libs.
			values = IOUtil.readJar(path);
		}
		
		if (values == null || values.size() == 0)
    		throw new Exception("Unable to load the jar");
		//Extract catalog.
		Iterator<String> i = values.keySet().iterator();
		String key;
		InputStream stream = null;
		while (i.hasNext()) {
			key = i.next();
			if (key.contains(CATALOG)) {
				stream = values.get(key);
				break;
			}
		}
		if (stream == null)
    		throw new Exception("No Catalog found.");
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.parse(stream);
			
			current = document.getDocumentElement().getAttribute(CURRENT);
			if (current == null || current.trim().length() == 0)
				throw new Exception("No schema specified.");
			extractCurrentSchema(current);
		} catch (Exception e) {
			throw new Exception("Unable to parse the catalog.", e);
		}
	}

}
