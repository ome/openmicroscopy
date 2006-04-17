/*
 * org.openmicroscopy.shoola.env.config.MapEntry
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
import java.util.HashMap;
import java.util.Map;

// Third-party libraries
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//Application-internal dependencies

/**
 * Handles a <i>structuredEntry</i> of type <i>map</i>.
 * Each tag within this entry defines a name-value pair, the name being the
 * tag's name and the value the tag's content.  These name-value pairs are
 * stored into a {@link Map} (keyed by names), which is then returned by the 
 * {@link #getValue() getValue} method.
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
class MapEntry 
    extends Entry
{
    
	/** 
	 * The contents of the entry.
	 * The name of each contained tag is a key in the map and the tag's value
	 * is the value associated to that key in the map.
	 */
    private Map     nameValuePairs;
    
    
	/** Creates a new instance. */
    MapEntry()
    {
		nameValuePairs = new HashMap();
    }
    
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
                NodeList childList = node.getChildNodes();
				Node child;
                for (int i = 0; i < childList.getLength(); i++) {
                    child = childList.item(i);
                    if (child.getNodeType() == Node.ELEMENT_NODE)
                        nameValuePairs.put(child.getNodeName(), 
                                        child.getFirstChild().getNodeValue());
                }
            }  
		} catch (DOMException dex) {
			rethrow("Can't parse map entry, name: "+getName()+".", dex); 
		}
    }
    
	/** 
	 * Returns a map whose keys are the names of each tag within this 
	 * <i>structuredEntry</i> and values are the corresponding tag values.
	 * 
	 * @return	See above.
	 */   
    Object getValue() { return nameValuePairs; }
    
}
