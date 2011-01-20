/*
 * org.openmicroscopy.shoola.env.config.OMEROInfo
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


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Holds the configration information for the <i>OMERO</i> entry in the
 * container's configuration file.
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
public class OMEROInfo
{

    /** The value of the <code>port</code> sub-tag. */ 
    private int        port;
    
    /**
     * Parses the specified string into an integer.
     * 
     * @param value The string holding the value to parse.
     * @return See above.
     * @throws ConfigException If <code>value</code> is not a well-formed 
     *                         integer.
     */
    private int parseInt(String value)
        throws ConfigException
    {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            throw new ConfigException("Malformed integer value: "+value+".");
        }
    }
    
    /**
     * Creates a new instance.
     * This is the only constructor and should have package visibility because 
     * instances of this class can only be created (meaningfully) within this
     * package. However, we made it public to ease testing.
     *     
     * @param port The value of the <code>port</code> sub-tag.
     * @throws ConfigException If <code>port</code> can't be parsed into an 
     *                          integer.
     */
    public OMEROInfo(String port)
        throws ConfigException
    {
        this.port = parseInt(port);
    }
    
    /**
     * Returns the value of the <code>port</code> sub-tag.
     * 
     * @return See above.
     */
    public int getPort() { return port; }
    
}
