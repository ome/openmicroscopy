/*
 * org.openmicroscopy.shoola.env.data.util.Target 
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
import java.util.Iterator;
import java.util.List;

import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


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

    /** The node to parse.*/
    private Element node;

    /**
     * Populates the collection of transformations.
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
        this.node = node;
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
    public List<InputStream> getTransforms()
    {
        List<InputStream> styleSheets = new ArrayList<InputStream>();
        Iterator<String> j = transforms.iterator();
        String name;
        InputStream stream;
        while (j.hasNext()) {
            name = j.next();
            if (!UIUtilities.isWindowsOS()) {
                stream = this.getClass().getResourceAsStream(
                        "/"+TransformsParser.TRANSFORM_FOLDER+name);
            } else {
                stream = this.getClass().getClassLoader().getResourceAsStream(
                        TransformsParser.TRANSFORM_FOLDER+name);
            }
            if (stream != null) {
                styleSheets.add(stream);
            }
        }
        return styleSheets;
    }

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
