/*
 * org.openmicroscopy.shoola.env.config.LongEntry
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2013 University of Dundee & Open Microscopy Environment.
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
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

//Third-party libraries

//Application-internal dependencies

/**
 * Handles an <i>entry</i> of type <i>long</i>.
 * The tag's value is stored into a {@link Long} object which is then
 * returned by the {@link #getValue() getValue} method.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 4.4
 */
class LongEntry
	extends Entry
{

	/** The entry value. */
    private Long value;

	/** Creates a new instance. */
    LongEntry() {}
    
    /** 
     * Implemented as specified by {@link Entry}. 
     * @see Entry#setContent(Node)
     * @throws ConfigException If the configuration entry couldn't be handled.
     */  
    protected void setContent(Node node)
		throws ConfigException
    { 
		String cfgVal = null;
		try {
			cfgVal = node.getFirstChild().getNodeValue();
            value = new Long(cfgVal);
		} catch (DOMException dex) { 
			rethrow("Can't parse long entry, name: "+getName()+".", dex);
		} catch (NumberFormatException nfe) {
			rethrow(cfgVal+" is not a valid long, entry name: "+
					getName()+".", nfe);
		}
    }
    
	/**
	 * Returns a {@link Long} object which represents the tag's content.
	 * The double value wrapped by the returned object will be parsed as
	 * specified by the {@link Long} class. 
	 * 
	 * @return	See above.
	 */  
    Object getValue() { return value; }

}
