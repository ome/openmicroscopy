/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2014 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.config;


//Java imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Handles the <code>Addon</code> tag.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.0
 */
public class AddOnEntry
    extends Entry
{

    /** 
     * The name of the tag, within this <i>structuredEntry</i>, that is used
     * to specify an agent's configuration.
     */
    private static final String ADD_ON_TAG = "addOn";

    /**
     * The name of the tag, within this <i>structuredEntry</i>, that is used
     * to specify the name.
     */
    private static final String NAME_TAG = "name";

    /**
     * The name of the tag, within this <i>structuredEntry</i>, that is used
     * to specify the description.
     */
    private static final String DESCRIPTION_TAG = "description";

    /**
     * The name of the tag, within this <i>structuredEntry</i>, that is used
     * to specify the download.
     */
    private static final String DOWNLOAD_TAG = "download";

    /**
     * The name of the tag, within this <i>structuredEntry</i>, that is used
     * to specify the script.
     */
    private static final String SCRIPT_TAG = "script";

    /** The object built from the configuration.*/
    private List<AddOnInfo> values;

    /**
     * Adds the given tag name and value to the passed map.
     * 
     * @param tag A tag within the <i>agent</i> tag.
     * @param values The map containing the pairs 
     *               <i>(tag-name, tag-value)</i> relative to the
     *               <i>addOn</i> tag that contains <code>tag</code>.
     * @throws ConfigException If <code>tag</code> is not one of the tags that
     *                          we expect to be within an <i>agent</i> tag.
     */
    private void extractTag(Node tag, Map<String, String> values) 
        throws ConfigException
    {
        String tagName = tag.getNodeName();
        String tagValue = "";
        try {
            tagValue = tag.getFirstChild().getNodeValue();
        } catch (Exception e) {
           //value not required.
        }
        if (NAME_TAG.equals(tagName) ||
            DESCRIPTION_TAG.equals(tagName) ||
            DOWNLOAD_TAG.equals(tagName) ||
            SCRIPT_TAG.equals(tagName)) {
                values.put(tagName, tagValue);
                return;
            }
        throw new ConfigException(
            "Unrecognized tag within the addOn tag: "+tagName);
    }

    /**
     * Extracts the values of the tags within an <i>agent</i> tag and puts them
     * in a map keyed by tag names.
     *
     * @param agent The <i>agent</i> tag.
     * @return The pairs <i>(tag-name, tag-value)</i> built from the tags
     *          contained within the <i>agent</i> tag.
     * @throws DOMException If the entry contents couldn't be retrieved.
     * @throws ConfigException If the <i>agent</i> tag is not structured as
     *                          expected.
     */
    private Map<String, String> extractValues(Node agent)
        throws DOMException, ConfigException
    {
        if (!ADD_ON_TAG.equals(agent.getNodeName()))
            throw new ConfigException(
                "Unrecognized tag within the addOn tag: "+agent.getNodeName());
        Map<String, String> tags = new HashMap<String, String>();
        if (agent.hasChildNodes()) {
            NodeList children = agent.getChildNodes();
            int n = children.getLength();
            Node child;
            while (0 < n) {
                child = children.item(--n);
                if (child.getNodeType() == Node.ELEMENT_NODE)
                    extractTag(child, tags); 
            }
        }
        return tags;
    }

    /** Creates a new instance. */
    AddOnEntry()
    {
        values = new ArrayList<AddOnInfo>();
    }

    /**
     * Returns a {@link AddOnInfo} object, built from the configuration
     * information.
     *
     * @return  See above.
     */  
    Object getValue() { return values; }

    /**
     * Implemented as specified by {@link Entry}.
     * @see Entry#setContent(Node)
     * @throws ConfigException If the configuration entry couldn't be handled.
     */
    protected void setContent(Node node)
            throws ConfigException
   {
        try {
            if (node.hasChildNodes()) {
                NodeList children = node.getChildNodes();
                int n = children.getLength();
                Node child;
                Map<String, String> childTags;
                AddOnInfo info;
                while (0 < n) {
                    child = children.item(--n);
                    if (child.getNodeType() == Node.ELEMENT_NODE) {
                        childTags = extractValues(child);
                        info = new AddOnInfo();
                        info.setName((String) childTags.get(NAME_TAG));
                        info.setDescription(
                            (String) childTags.get(DESCRIPTION_TAG));
                        info.setDownload(
                            (String) childTags.get(DOWNLOAD_TAG));
                        info.setScripts(
                                (String) childTags.get(SCRIPT_TAG));
                        values.add(info);
                    }
                }
            }
        } catch (DOMException dex) {
            rethrow("Can't parse agents entry.", dex);
        }
    }

}
