/*
 * org.openmicroscopy.shoola.env.config.OMEROInfo
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
    
    /** The value of the <code>hostName</code> sub-tag. */ 
    private String      hostName;
    
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
     * @param hostName The value of the <code>hostName</code> sub-tag.          
     * @param port The value of the <code>port</code> sub-tag.
     * @throws ConfigException If <code>port</code> can't be parsed into an 
     *                          integer.
     */
    public OMEROInfo(String hostName, String port)
        throws ConfigException
    {
        this.hostName = hostName;
        this.port = parseInt(port);
    }
    
    /**
     * Returns the value of the <code>port</code> sub-tag.
     * 
     * @return See above.
     */
    public int getPort() { return port; }
    
    /**
     * Returns the value of the <code>hostName</code> sub-tag.
     * 
     * @return See above.
     */
    public String getHostName() { return hostName; }

}
