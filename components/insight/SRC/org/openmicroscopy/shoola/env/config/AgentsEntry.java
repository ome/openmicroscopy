/*
 * org.openmicroscopy.shoola.env.config.AgentsEntry
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.config;

//Java Imports 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Third-party libraries
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//Application-internal dependencies

/** 
 * Handles a <i>structuredEntry</i> of type <i>agents</i>.
 * Each <i>agent</i> tag within the entry is stored in an {@link AgentInfo}
 * object. The {@link #getValue() getValue} method returns a list of those
 * objects.  
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *              a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class AgentsEntry 
    extends Entry
{
    
	/** 
	 * The name of the tag, within this <i>structuredEntry</i>, that is used
	 * to specify an agent's configuration.
	 */
	private static final String		AGENT_TAG = "agent";

	/** 
	 * The name of the tag, within the {@link #AGENT_TAG}, that is used
	 * to specify an agent's name.
	 */
	private static final String		AGENT_NAME_TAG = "name";

	/** 
	 * The name of the tag, within the {@link #AGENT_TAG}, that is used
	 * to specify an agent's class.
	 */
	private static final String		AGENT_CLASS_TAG = "class";	
	
	/** 
	 * The name of the tag, within the {@link #AGENT_TAG}, that is used
	 * to specify an agent's configuration file.
	 */
	private static final String		AGENT_CONFIG_TAG = "config";
	
	/** 
	 * The name of the tag, within the {@link #AGENT_TAG}, that is used
	 * to specify an agent's active status.
	 */
	private static final String		AGENT_ACTIVE_TAG = "active";
	
	/** 
	 * The name of the tag, within the {@link #AGENT_TAG}, that is used
	 * to specify an agent's number. Only entry point agent should have a
	 * number.
	 */
	private static final String		AGENT_NUMBER_TAG = "number";
		
	/** The number of tags for an agent. */
	private static final int		TAGS_NUMBER = 3;
	
    /** 
     * The contents of the entry.
     * Each element is an {@link AgentInfo} object that stores the content of
     * an <i>agent</i> tag.
     */
    private List<AgentInfo>    agentsList;
    

	/**
	 * Extracts the values of the tags within an <i>agent</i> tag and puts them
	 * in a map keyed by tag names.
	 *  
	 * @param agent	The <i>agent</i> tag.
	 * @return The pairs <i>(tag-name, tag-value)</i> built from the tags
	 * 			contained within the <i>agent</i> tag.
	 * @throws DOMException If the entry contents couldn't be retrieved.
	 * @throws ConfigException If the <i>agent</i> tag is not structured as
	 * 							expected.
	 */
	private Map<String, String> extractValues(Node agent)
		throws DOMException, ConfigException
	{
		if (!AGENT_TAG.equals(agent.getNodeName()))
			throw new ConfigException(
				"Unrecognized tag within the agents tag: "+agent.getNodeName());
		Map<String, String> tags = new HashMap<String, String>();
		if (agent.hasChildNodes()) {
			NodeList children = agent.getChildNodes();
			int n = children.getLength();
			Node child;
			while (0 < n) {
				child = children.item(--n);
				if (child.getNodeType() == Node.ELEMENT_NODE)
					extractAgentTag(child, tags); 
			}
		}
		if (tags.keySet().size() < TAGS_NUMBER)
			throw new ConfigException("Missing tags within agent tag.");
		return tags;
	}
	//TODO: remove the checks (which are not complete anyway) and the
	//ConfigException when we have an XML schema for config files. 
	
	/**
	 * Adds the given tag name and value to the passed map.
	 * 
	 * @param tag		A tag within the <i>agent</i> tag.
	 * @param values	The map containing the pairs 
	 * 					<i>(tag-name, tag-value)</i> relative to the
	 * 					<i>agent</i> tag that contains <code>tag</code>.
	 * @throws ConfigException If <code>tag</code> is not one of the tags that
	 * 							we expect to be within an <i>agent</i> tag.
	 */
	private void extractAgentTag(Node tag, Map<String, String> values) 
		throws ConfigException
	{
		String tagName = tag.getNodeName(),
				tagValue = tag.getFirstChild().getNodeValue();
		if (AGENT_NAME_TAG.equals(tagName) || 
			AGENT_CLASS_TAG.equals(tagName) ||
			AGENT_CONFIG_TAG.equals(tagName) || 
			AGENT_ACTIVE_TAG.equals(tagName) || 
			AGENT_NUMBER_TAG.equals(tagName)) {
				values.put(tagName, tagValue);
				return;
			}
		throw new ConfigException(
			"Unrecognized tag within the agent tag: "+tagName);
	}
	//TODO: remove the checks (which are not complete anyway) and the
	//ConfigException when we have an XML schema for config files.
    
	
    /** Creates a new instance. */
    AgentsEntry()
    {
		agentsList = new ArrayList<AgentInfo>();
    }
    
	/** 
	 * Returns a list of {@link AgentInfo} objects, each representing the 
	 * contents of an <i>agent</i> tag within this <i>structuredEntry</i>.
	 * 
	 * @return	See above.
	 */  
	Object getValue() { return agentsList; }

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
                AgentInfo info;
				while (0 < n) {
					child = children.item(--n);
					if (child.getNodeType() == Node.ELEMENT_NODE) {
						childTags = extractValues(child);
						info = new AgentInfo();
						info.setName((String) childTags.get(AGENT_NAME_TAG));
						info.setAgentClass(
							(String) childTags.get(AGENT_CLASS_TAG));
						info.setConfigPath(
							(String) childTags.get(AGENT_CONFIG_TAG));
						info.setActive(
								(String) childTags.get(AGENT_ACTIVE_TAG));
						info.setNumber(
								(String) childTags.get(AGENT_NUMBER_TAG));
						agentsList.add(info);
					}		 
				}
            }  
        } catch (DOMException dex) {
        	rethrow("Can't parse agents entry.", dex); 
        }
    }
    
}
