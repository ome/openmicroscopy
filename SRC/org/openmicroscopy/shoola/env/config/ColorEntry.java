/*
 * org.openmicroscopy.shoola.env.config.ColorEntry
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
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//Third-party libraries

//Application-internal dependencies

/** 
 * Hanldes a <i>structuredEntry</i> of type <i>color</i>.
 * The content of the entry is stored in a {@link Color} object, which is
 * then returned by the {@link #getValue() getValue} method.
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
class ColorEntry
    extends Entry
{

    /** 
     * The name of the tag, within this <i>structuredEntry</i>, that is used
     * to specify the red's component.
     */
    private static final String     RED_TAG = "red";
    
    /** 
     * The name of the tag, within this <i>structuredEntry</i>, that is used
     * to specify the green's component.
     */
    private static final String     GREEN_TAG = "green";
    
    /** 
     * The name of the tag, within this <i>structuredEntry</i>, that is used
     * to specify the blue's component.
     */
    private static final String     BLUE_TAG = "blue";
    
    /** 
     * Default value for the red component.
     * This is the value that we use to create a {@link Color} object if the 
     * <i>red</i> tag can't be parsed into an integer or if the parsed value
     * is not between the {@link #MIN_VALUE}, {@link #MAX_VALUE} interval. 
     */
    private static final int        DEFAULT_RED = 255; 
    
    /** 
     * Default value for the green component.
     * This is the value that we use to create a {@link Color} object if the 
     * <i>red</i> tag can't be parsed into an integer or if the parsed value
     * is not between the {@link #MIN_VALUE}, {@link #MAX_VALUE} interval. 
     */
    private static final int        DEFAULT_GREEN = 255; 
    
    /** 
     * Default value for the blue component.
     * This is the value that we use to create a {@link Color} object if the 
     * <i>red</i> tag can't be parsed into an integer or if the parsed value
     * is not between the {@link #MIN_VALUE}, {@link #MAX_VALUE} interval. 
     */
    private static final int        DEFAULT_BLUE = 255; 
    
    /** The minimum allowed value for a color component. */
    private static final int        MIN_VALUE = 0;
    
    /** The maximum allowed value for a color component. */
    private static final int        MAX_VALUE = 255;
    
    /** The color built from the configuration information. */
    private Color value;
    
    /** Creates a new instance. */
    ColorEntry() {}
    
    /** 
     * Returns a {@link Color} object, built from the configuration information.
     * 
     * @return  See above.
     */  
    Object getValue() { return value; }
    
    /** Implemented as specified by {@link Entry}. */  
    protected void setContent(Node tag)
            throws ConfigException
    {
        try {
            Map tags = extractValues(tag);
            value = new Color(getRedComponent(tags), getGreenComponent(tags), 
                        getBlueComponent(tags));
        } catch (DOMException dex) { 
            rethrow("Can't parse font entry.", dex);
        } 
    }
    
    /**
     * Figures out what value for the specified red component to use from 
     * the configuration information.
     * This method tries to parse the value of the corresponding 
     * red tag into an integer.
     * Failing that or if the parsed value is not between the 
     * {@link #MIN_VALUE}, {@link #MAX_SIZE} interval, 
     * then the {@link #DEFAULT_RED} is returned.
     * 
     * @param colorAttributes The pairs <i>(tag-name, tag-value)</i> built from
     *                      the tags contained within the entry tag.
     * @return See above.
     */
    private int getRedComponent(Map colorAttributes)
    {
        String value = (String) colorAttributes.get(RED_TAG);
        return parseValue(value, DEFAULT_RED);
    }
    
    /**
     * Figures out what value for the specified green component to use from 
     * the configuration information.
     * This method tries to parse the value of the corresponding 
     * green tag into an integer.
     * Failing that or if the parsed value is not between the 
     * {@link #MIN_VALUE}, {@link #MAX_SIZE} interval, 
     * then the {@link #DEFAULT_GREEN} is returned.
     * 
     * @param colorAttributes The pairs <i>(tag-name, tag-value)</i> built from
     *                      the tags contained within the entry tag.
     * @return See above.
     */
    private int getGreenComponent(Map colorAttributes)
    {
        String value = (String) colorAttributes.get(GREEN_TAG);
        return parseValue(value, DEFAULT_GREEN);
    }
    
    /**
     * Figures out what value for the specified blue component to use from 
     * the configuration information.
     * This method tries to parse the value of the corresponding 
     * blue tag into an integer.
     * Failing that or if the parsed value is not between the 
     * {@link #MIN_VALUE}, {@link #MAX_SIZE} interval, 
     * then the {@link #DEFAULT_BLUE} is returned.
     * 
     * @param colorAttributes The pairs <i>(tag-name, tag-value)</i> built from
     *                      the tags contained within the entry tag.
     * @return See above.
     */
    private int getBlueComponent(Map colorAttributes)
    {
        String value = (String) colorAttributes.get(BLUE_TAG);
        return parseValue(value, DEFAULT_BLUE);
    }
    
    /** Parse the value. */
    private int parseValue(String value, int defaultValue)
    {
        int v = defaultValue;
        try {
            v = Integer.parseInt(value);
            if (v < MIN_VALUE || MAX_VALUE < v) v = defaultValue;    
        } catch (NumberFormatException nfe) {}
        return v;
    }
    
    /**
     * Extracts the values of the tags within a <i>color</i> tag and puts them
     * in a map keyed by tag names.
     *  
     * @param entry The <i>color</i> entry tag.
     * @return The pairs <i>(tag-name, tag-value)</i> built from the tags
     *          contained within the <i>color</i> tag.
     * @throws DOMException If the entry contents couldn't be retrieved.
     * @throws ConfigException If the <i>font</i> tag is not structured as
     *                          expected.
     */
    private Map extractValues(Node entry)
        throws DOMException, ConfigException
    {
        Map tags = new HashMap();
        if (entry.hasChildNodes()) {
            NodeList children = entry.getChildNodes();
            int n = children.getLength();
            Node child;
            while (0 < n) {
                child = children.item(--n);
                if (child.getNodeType() == Node.ELEMENT_NODE)
                    extractColorTag(child, tags); 
            }
        }
        if (tags.keySet().size() != 3)
            throw new ConfigException("Missing tags within color tag.");
        return tags;
    }
    //TODO: remove the checks (which are not complete anyway) and the
    //ConfigException when we have an XML schema for config files. 
    
    /**
     * Adds the given tag name and value to the passed map.
     * 
     * @param tag   A tag within the <i>color</i> tag.
     * @param values    The map containing the pairs 
     *                  <i>(tag-name, tag-value)</i> relative to the
     *                  <i>color</i> tag that contains <code>tag</code>.
     * @throws DOMException If the entry contents couldn't be retrieved.
     * @throws ConfigException If <code>tag</code> is not one of the tags that
     *                          we expect to be within an <i>font</i> tag.
     */
    private void extractColorTag(Node tag, Map values) 
        throws DOMException, ConfigException
    {
        String tagName = tag.getNodeName(),
                tagValue = tag.getFirstChild().getNodeValue();
        if (RED_TAG.equals(tagName) || GREEN_TAG.equals(tagName) ||
            BLUE_TAG.equals(tagName)) {
                values.put(tagName, tagValue);
                return;
            }
        throw new ConfigException(
            "Unrecognized tag within the color entry: "+tagName);
    }
    //TODO: remove the checks (which are not complete anyway) and the
    //ConfigException when we have an XML schema for config files.

}
