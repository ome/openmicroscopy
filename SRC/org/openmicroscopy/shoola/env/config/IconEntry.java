/*
 * org.openmicroscopy.shoola.env.config.IconEntry
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 Open Microscopy Environment
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

/*------------------------------------------------------------------------------
 *
* Written by:     Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *                      <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 *                      Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *                      <a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.env.config;

// Java imports 
import java.net.URL;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 */

class IconEntry extends Entry {
    
    private String value;
    IconEntry() {
    }
    
/** Implemented as specified by {@linkEntry}.
 */  
    protected void setContent(Node node) { 
        try {
            //the node is supposed to have tags as children, add control b/c we don't use yet a 
            // XMLSchema config
            if (node.hasChildNodes()) {
                NodeList childList = node.getChildNodes();
                String host = null, port = null;
                for (int i = 0; i<childList.getLength(); i++){
                    Node child = childList.item(i);
                    if (child.getNodeType() == child.ELEMENT_NODE) 
                        value = child.getFirstChild().getNodeValue();
                }
            }  
        } catch (DOMException dex) { throw new RuntimeException(dex); }
    }
/** 
 * Implemented as specified by {@linkEntry}.
 * Builds and return an Icon Object
 * @return  An object implementing {@link javax.swing.Icon Icon} or <code>null</code> if the path 
 *                  was invalid.
 */  
    Object getValue() {
        URL     location = IconEntry.class.getResource(value);
        if ( location != null ) {
            return new ImageIcon(location);
        } else {
            //TODO  errorMsg via logService
            System.err.println("Couldn't find file: "+value);
            return null;
        } 
    }
    
    
}
