/*
 * org.openmicroscopy.shoola.env.data.util.TransformsParser 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee & Open Microscopy Environment.
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


import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.openmicroscopy.shoola.util.CommonsLangUtils;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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

    /** Folder hosting the style sheets.*/
    static String TRANSFORM_FOLDER = "transforms/";

    /** The catalog file to find. */
    private static String CATALOG = "transforms/ome-transforms.xml";

    /** The <i>name</i> attribute. */
    private static String CURRENT = "current";

    /** The <i>source</i> node. */
    private static String SOURCE = "source";

    /** The configuration file. */
    private Document document;

    /** The possible downgrade schema.*/
    private List<Target> targets;

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
     * @throws Exception Thrown when an error occurred while parsing the file.
     */
    public void parse()
            throws Exception
    {
        String name = CATALOG;
        InputStream stream = null;
        if (!UIUtilities.isWindowsOS()) {
            name = "/"+name;
            stream = this.getClass().getResourceAsStream(name);
        } else {
            stream = this.getClass().getClassLoader().getResourceAsStream(name);
        }
        if (stream == null)
            throw new Exception("No Catalog found.");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(stream);

            current = document.getDocumentElement().getAttribute(CURRENT);
            if (CommonsLangUtils.isBlank(current))
                throw new Exception("No schema specified.");
            extractCurrentSchema(current);
        } catch (Exception e) {
            throw new Exception("Unable to parse the catalog.", e);
        } finally {
            stream.close();
        }
    }

}
