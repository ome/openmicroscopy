/*
 * org.openmicroscopy.shoola.env.config.OMEDSEntry
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

// Java imports 

// Third-party libraries
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//Application-internal dependencies

/**
 * Hanldes a <i>structuredEntry</i> of type <i>OMEDS</i>.
 * The content of the entry is stored in a {@link OMEDSInfo} object, which is
 * then returned by the {@link #getValue() getValue} method.
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
class OMEDSEntry
    extends Entry
{
    
    /** 
     * The name of the tag, within this <i>structuredEntry</i>, that specifies
     * the <i>URL</i> to connect to <i>OMEDS</i>.
     */
    private static final String     URL_TAG = "url";
    
    
    /** Holds the contents of the entry. */
    private OMEDSInfo value;
    
    /**
     * Helper method to parse the structured entry tag.
     * 
     * @param tag The structured entry tag.
     * @return An object that holds the contents of the tag.
     * @throws ConfigException If the tag is malformed.
     */
    private static OMEDSInfo parseTag(Node tag)
        throws DOMException, ConfigException
    {
        String url = null; 
        NodeList children = tag.getChildNodes();
        int n = children.getLength();
        Node child;
        String tagName, tagValue;
        while (0 < n) {
            child = children.item(--n);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                tagName = child.getNodeName();
                tagValue = child.getFirstChild().getNodeValue();
                if (URL_TAG.equals(tagName))
                    url = tagValue;
                else
                    throw new ConfigException(
                            "Unrecognized tag within the ice-conf entry: "+
                            tagName+".");
            }
        }
        if (url == null)
            throw new ConfigException("Missing "+URL_TAG+
                                      " tag within omeds-conf entry.");
        return new OMEDSInfo(url);
    }
    
    /** Creates a new instance. */
    OMEDSEntry() {}
    
    /** 
     * Returns a {@link OMEDSInfo} object, which contains the <i>OMEDS</i>
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
            if (node.hasChildNodes())   value = parseTag(node);
        } catch (DOMException dex) { 
            rethrow("Can't parse OMEDS entry.", dex);
        }
    }
    
}
