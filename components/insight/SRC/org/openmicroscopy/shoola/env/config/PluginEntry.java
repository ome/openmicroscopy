/*
 * org.openmicroscopy.shoola.env.config.PluginEntry
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
package org.openmicroscopy.shoola.env.config;



//Java imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//Third-party libraries

//Application-internal dependencies

/**
 * Handles a <i>structuredEntry</i> of type <i>plugins</i>.
 * Each <i>plugin</i> tag within the entry is stored in a {@link PluginInfo}
 * object. The {@link #getValue() getValue} method returns a list of those
 * objects.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 4.4
 */
public class PluginEntry
	extends Entry
{

	/** 
	 * The name of the tag, within this <i>structuredEntry</i>, that is used
	 * to specify a plugin's information.
	 */
	private static final String		PLUGIN_TAG = "plugin";
	
	/** 
     * The name of the tag, within this <i>structuredEntry</i>, that specifies
     * the <i>onelineInformation</i>.
     */
    private static final String     INFO_TAG = "info";
    
    /** 
     * The name of the tag, within this <i>structuredEntry</i>, that specifies
     * the dependencies when the client is ran as a plugin.
     */
    private static final String     DEPENDENCIES_TAG = "dependencies";
    
    /** 
     * The name of the tag, within this <i>structuredEntry</i>, that specifies
     * where to look for the dependencies.
     */
    private static final String     DIRECTORY_TAG = "directory";
    
    /** 
     * The name of the tag, within this <i>structuredEntry</i>, that specifies
     * the name of the application the client will be used as a plugin.
     */
    private static final String     NAME_TAG = "name";
    
    /** 
     * The name of the tag, within this <i>structuredEntry</i>, that specifies
     * the identifier of the plugin.
     */
    private static final String     ID_TAG = "id";
    
    /** 
     * The name of the tag, within this <i>structuredEntry</i>, that specifies
     * the conjunction to use when several dependencies are specified.
     */
    private static final String     CONJUNCTION_TAG = "conjunction";
    
    /** Holds the contents of the entry. */
    private List<PluginInfo> values;
    
    /**
	 * Adds the given tag name and value to the passed map.
	 * 
	 * @param tag A tag within the <i>plugin</i> tag.
	 * @param values The map containing the pairs 
	 * 				<i>(tag-name, tag-value)</i> relative to the
	 * 					<i>agent</i> tag that contains <code>tag</code>.
	 * @throws ConfigException If <code>tag</code> is not one of the tags that
	 * 							we expect to be within an <i>agent</i> tag.
	 */
	private void extractPluginTag(Node tag, Map<String, String> values) 
		throws ConfigException
	{
		String tagName = tag.getNodeName();
		if (!tag.hasChildNodes()) return;
		String tagValue = tag.getFirstChild().getNodeValue();
		if (DEPENDENCIES_TAG.equals(tagName) || 
			ID_TAG.equals(tagName) || INFO_TAG.equals(tagName) || 
			NAME_TAG.equals(tagName) || DIRECTORY_TAG.equals(tagName) ||
			CONJUNCTION_TAG.equals(tagName)) {
				values.put(tagName, tagValue);
				return;
			}
		throw new ConfigException(
			"Unrecognized tag within the plugin tag: "+tagName);
	}
    
    /**
	 * Extracts the values of the tags within an <i>agent</i> tag and puts them
	 * in a map keyed by tag names.
	 *  
	 * @param plugin The <i>plugin</i> tag.
	 * @return The pairs <i>(tag-name, tag-value)</i> built from the tags
	 * 			contained within the <i>agent</i> tag.
	 * @throws DOMException If the entry contents couldn't be retrieved.
	 * @throws ConfigException If the <i>agent</i> tag is not structured as
	 * 							expected.
	 */
	private Map<String, String> extractValues(Node plugin)
		throws DOMException, ConfigException
	{
		if (!PLUGIN_TAG.equals(plugin.getNodeName()))
			throw new ConfigException(
				"Unrecognized tag within the plugins tag: "+
			plugin.getNodeName());
		Map<String, String> tags = new HashMap<String, String>();
		if (plugin.hasChildNodes()) {
			NodeList children = plugin.getChildNodes();
			int n = children.getLength();
			Node child;
			while (0 < n) {
				child = children.item(--n);
				if (child.getNodeType() == Node.ELEMENT_NODE)
					extractPluginTag(child, tags); 
			}
		}
		return tags;
	}
    /** Creates a new instance. */
    PluginEntry()
    {
    	values = new ArrayList<PluginInfo>();
    }
    
    /** 
     * Returns an {@link PluginInfo} object, which contains the <i>Plugin</i>
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
                PluginInfo info;
				while (0 < n) {
					child = children.item(--n);
					if (child.getNodeType() == Node.ELEMENT_NODE) {
						childTags = extractValues(child);
						info = new PluginInfo((String) childTags.get(ID_TAG), 
								(String) childTags.get(DEPENDENCIES_TAG),
								(String) childTags.get(DIRECTORY_TAG));
						info.setName((String) childTags.get(NAME_TAG));
						info.setInfo((String) childTags.get(INFO_TAG));
						info.setConjunction((String) childTags.get(
								CONJUNCTION_TAG));
						values.add(info);
					}
				}
            }  
        } catch (DOMException dex) { 
            rethrow("Can't parse Plugin entry.", dex);
        }
    }
}
