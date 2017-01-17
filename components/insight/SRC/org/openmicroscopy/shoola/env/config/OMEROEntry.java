/*
 * org.openmicroscopy.shoola.env.config.OMEROEntry
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2017 University of Dundee. All rights reserved.
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


//Java imports

//Third-party libraries
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//Application-internal dependencies

/** 
 * Handles a <i>structuredEntry</i> of type <i>OMERO</i>.
 * The content of the entry is stored in a {@link OMEROInfo} object, which is
 * then returned by the {@link #getValue() getValue} method.
 *  
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *                  a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class OMEROEntry
    extends Entry
{

    /** 
     * The name of the tag, within this <i>structuredEntry</i>, that specifies
     * the <i>port</i> to connect to <i>OMERO</i>.
     */
    private static final String     PORT_TAG = "port";
    
    /** 
     * The name of the tag, within this <i>structuredEntry</i>, that specifies
     * the <i>portSSL</i> to connect to <i>OMERO</i>.
     */
    private static final String     PORT_SSL_TAG = "portSSL";
    
    /** 
     * The name of the tag, within this <i>structuredEntry</i>, that specifies
     * the <i>hostName</i> to connect to <i>OMERO</i>.
     */
    private static final String     HOST_NAME_TAG = "hostName";
    
    /** 
     * The name of the tag, within this <i>structuredEntry</i>, that indicates
     * if the data transfer is encrypted.
     */
    private static final String     ENCRYPTED_TAG = "encrypted";
    
    /** 
     * The name of the attribute that indicates the user can modify in UI the 
     * value specified in the configuration file.
     */
    private static final String     CONFIGURABLE_ATTRIBUTE = "configurable";
    
    
    /** Holds the contents of the entry. */
    private OMEROInfo value;
    
    /** 
     * Returns the value of the {@link #CONFIGURABLE_ATTRIBUTE}
     * attribute as a boolean.
     * 
     * @param tag The tag to handle.
     * @return See above.
     */
    private static boolean getConfigAttribute(Node tag)
    {
    	NamedNodeMap attrList = tag.getAttributes();
		Node attribute;
		String value = null;
		//Store the values of name and type attributes into ntp.
		for (int i = 0; i < attrList.getLength(); ++i) {
			attribute = attrList.item(i);
			if (CONFIGURABLE_ATTRIBUTE.equals(attribute.getNodeName())) {
				value = attribute.getNodeValue();
			}
		}
		if (value == null) return true;
		value = value.toLowerCase();
		if (AgentInfo.TRUE.equals(value) ||
                AgentInfo.TRUE_SHORT.equals(value))
			return true;
		else if (AgentInfo.FALSE.equals(value) ||
				AgentInfo.FALSE_SHORT.equals(value))
			return false;
    	return true;
    }
    
    /**
     * Helper method to parse the structured entry tag.
     * 
     * @param tag The structured entry tag.
     * @return An object that holds the contents of the tag.
     * @throws ConfigException If the tag is not well formed.
     * @throws DOMException If the specified tag cannot be parsed.
     */
    private static OMEROInfo parseTag(Node tag)
        throws DOMException, ConfigException
    {
        String port = null; 
        String portSSL = null;
        String hostName = null;
        String encrypted = null;
        boolean hostConfigurable = true;
        boolean encryptedConfigurable = true;
        NodeList children = tag.getChildNodes();
        int n = children.getLength();
        Node child, c;
        String tagName, tagValue;
        while (0 < n) {
            child = children.item(--n);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                tagName = child.getNodeName();
                c = child.getFirstChild();
                if (c != null) {
                	 tagValue = c.getNodeValue();
                     if (PORT_TAG.equals(tagName)) port = tagValue;
                     else if (PORT_SSL_TAG.equals(tagName)) portSSL = tagValue;
                     else if (HOST_NAME_TAG.equals(tagName)) {
                    	hostName = tagValue;
                    	hostConfigurable = getConfigAttribute(child);
                     } else if (ENCRYPTED_TAG.equals(tagName)) {
                    	encrypted = tagValue;
                    	encryptedConfigurable = getConfigAttribute(child);
                     } else
                         throw new ConfigException(
                                 "Unrecognized tag within the conf entry: "+
                                 tagName+".");
                }
            }
        }
        if (port == null || port.trim().length() == 0)
            throw new ConfigException("Missing "+PORT_TAG+
                                      " tag within omeds-conf entry.");
        if (portSSL == null || portSSL.trim().length() == 0)
        	portSSL = port;
        OMEROInfo info = new OMEROInfo(port, portSSL, hostName, encrypted);
        info.setHostNameConfigurable(hostConfigurable);
        info.setEncryptedConfigurable(encryptedConfigurable);
        return info;
    }
    
    
    /** Creates a new instance. */
    OMEROEntry() {}
    
    /** 
     * Returns an {@link OMEROInfo} object, which contains the <i>OMERO</i>
     * configuration information.
     * 
     * @return  See above.
     */     
    Object getValue() { return value; }
    
    /** 
     * Implemented as specified by {@link Entry}. 
     * @see Entry#setContent(Node)
     * @throws ConfigException If the configuration entry couldn't be handled.
     */  
    protected void setContent(Node node)
        throws ConfigException
    { 
        try {
            if (node.hasChildNodes()) value = parseTag(node);
        } catch (DOMException dex) { 
            rethrow("Can't parse OMERO entry.", dex);
        }
    }
    
}
