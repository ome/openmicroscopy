/*
 * org.openmicroscopy.shoola.env.config.OMEISEntry
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

// Third-party libraries
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *              a.falconi@dundee.ac.uk</a>
 * <b>Internal version:</b> $Revision$  $Date$
 * @version 2.2
 * @since OME2.2
 */

class OMEISEntry
    extends Entry
{
    
    private HostInfo value;
    OMEISEntry()
    {
    }
/** Implemented as specified by {@link Entry}.
 */  
    protected void setContent(Node node)
    { 
        try {
            //the node is supposed to have tags as children, 
            //add control b/c we don't use a XMLSchema config
            if (node.hasChildNodes()) {
                NodeList childList = node.getChildNodes();
                HostInfo hi = new HostInfo();
                for (int i = 0; i<childList.getLength(); i++){
                    Node child = childList.item(i);
                    if (child.getNodeType()==Node.ELEMENT_NODE)
                        hi.setValue(child.getFirstChild().getNodeValue(),
                                    child.getNodeName());
                }
                value = hi;
            }  
        } catch (DOMException dex) { throw new RuntimeException(dex); }
    }
/** Implemented as specified by {@link Entry}.
 */  
    Object getValue()
    {
        return value; 
    }
    
    
}
