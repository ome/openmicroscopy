/*
 * org.openmicroscopy.shoola.env.config.OMEROEntry
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
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 * Hanldes a <i>structuredEntry</i> of type <i>OMERO</i>.
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
     * the <i>HostName</i> to connect to <i>OMERO</i>.
     */
    private static final String     HOSTNAME_TAG = "hostName";
    
    /** 
     * The name of the tag, within this <i>structuredEntry</i>, that specifies
     * the <i>port</i> to connect to <i>OMERO</i>.
     */
    private static final String     PORT_TAG = "port";
    
    /** Holds the contents of the entry. */
    private OMEROInfo value;
    
    /**
     * Helper method to parse the structured entry tag.
     * 
     * @param tag The structured entry tag.
     * @return An object that holds the contents of the tag.
     * @throws ConfigException If the tag is malformed.
     */
    private static OMEROInfo parseTag(Node tag)
        throws DOMException, ConfigException
    {
        String hostName = null, port = null; 
        NodeList children = tag.getChildNodes();
        int n = children.getLength();
        Node child;
        String tagName, tagValue;
        while (0 < n) {
            child = children.item(--n);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                tagName = child.getNodeName();
                tagValue = child.getFirstChild().getNodeValue();
                if (HOSTNAME_TAG.equals(tagName))
                    hostName = tagValue;
                else if (PORT_TAG.equals(tagName))
                    port = tagValue;
                else
                    throw new ConfigException(
                            "Unrecognized tag within the ice-conf entry: "+
                            tagName+".");
            }
        }
        if (hostName == null)
            throw new ConfigException("Missing "+HOSTNAME_TAG+
                                      " tag within omeds-conf entry.");
        if (port == null)
            throw new ConfigException("Missing "+PORT_TAG+
                                      " tag within omeds-conf entry.");
        return new OMEROInfo(hostName, port);
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
    
    /** Implemented as specified by {@link Entry}. */  
    protected void setContent(Node node)
        throws ConfigException
    { 
        try {
            if (node.hasChildNodes()) value = parseTag(node);
        } catch (DOMException dex) { 
            rethrow("Can't parse OMERODS entry.", dex);
        }
    }
    
}
