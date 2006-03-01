/*
 * org.openmicroscopy.shoola.env.config.BooleanEntry
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

//Third-party libraries
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

//Application-internal dependencies


/**
 * Hanldes an <i>entry</i> of type <i>boolean</i>.
 * The tag's value is stored into a {@link Boolean} object which is then
 * returned by the {@link #getValue() getValue} method.
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
class BooleanEntry
    extends Entry
{
    
    /** The entry value. */
    private Boolean value;
    
    
    /** Creates a new instance. */
    BooleanEntry() {}
    
    /** 
     * Implemented as specified by {@link Entry}. 
     * @see Entry#setContent(Node)
     * @throws ConfigException If the configuration entry couldn't be handled.
     */ 
    protected void setContent(Node node)
    	throws ConfigException
    { 
        try {
            value = new Boolean(node.getFirstChild().getNodeValue());
        } catch (DOMException dex) { 
			rethrow("Can't parse boolean entry, name: "+getName()+".", dex);
        }
    }
    
	/**
	 * Returns a {@link Boolean} object which represents the tag's content.
	 * The boolean value wrapped by the returned object will be
	 * <code>true</code> if, and only if, the tag's content equals the string
	 * <i>"true"</i> (case insensitive match). 
	 * 
	 * @return	See above.
	 */  
    Object getValue() { return value; }
    
    
}
