/*
 * org.openmicroscopy.shoola.env.config.IntegerEntry
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

/*------------------------------------------------------------------------------
 *
 * Written by:     Jean-Marie Burel     <j.burel@dundee.ac.uk>
 *                      Andrea Falconi          <a.falconi@dundee.ac.uk>
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.env.config;

// Java imports 
import org.w3c.dom.Node;

/**
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 */

class IntegerEntry extends Entry {
    
    private Integer value;
    IntegerEntry() {
    }
/** Implemented as specified by {@linkEntry}.
 */  
    protected void setContent(Node node) { 
        try {
            Node child = node.getFirstChild(); // has only one child
            value = new Integer(child.getNodeValue());
        } catch (Exception ex) { throw new RuntimeException(ex); }
    }
/** Implemented as specified by {@linkEntry}.
 */  
    Object getValue() {
        return value; 
    }
    
    
}
