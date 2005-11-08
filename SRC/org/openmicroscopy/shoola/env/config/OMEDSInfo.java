/*
 * org.openmicroscopy.shoola.env.config.OMEDSInfo
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
import java.net.MalformedURLException;
import java.net.URL;

//Third-party libraries

//Application-internal dependencies

/** 
 * Holds the configration information for the <i>OMEDS</i> entry in the
 * container's configuration file.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class OMEDSInfo
{
	
    /** The <i>URL</i> to connect to <i>OMEDS</i>. */
    private URL     serverAddress;

    /**
     * Parses and sets the <i>URL</i> to connect to <i>OMEDS</i>.
     * This method should have package visibility because it can only 
     * (meaningfully) used within this package.  However, we made it 
     * public to ease testing.
     * 
     * @param url   A string representing a valid <i>URL</i>.
     * @throws ConfigException If <code>url</code> specifies a malformed
     *                                  <i>URL</i>.
     */
    private URL createServerAddress(String url)
        throws ConfigException
    {  
        try {
             return new URL(url);
        } catch (MalformedURLException e) {
            throw new ConfigException("MalFormed OMEDS url.", e);
        }
    }
    
    /**
     * Creates a new instance.
     * This is the only constructor and should have package visibility because 
     * instances of this class can only be created (meaningfully) within this
     * package.  However, we made it public to ease testing.
     */
    public OMEDSInfo(String url)
        throws ConfigException
    {
        serverAddress = createServerAddress(url);
    }
    
    /**
     * Returns the <i>URL</i> to connect to <i>OMEDS</i>.
     * 
     * @return  See above.
     */
    public URL getServerAddress() { return serverAddress; }

}
