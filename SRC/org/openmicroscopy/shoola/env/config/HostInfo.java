/*
 * org.openmicroscopy.shoola.env.config.HostInfo
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
 * Creates an object containing the Host's informations.
 *
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *              a.falconi@dundee.ac.uk</a>
 * <br><b>Internal version:</b> $Revision$  $Date$
 * @version 2.2
 * @since OME2.2
 */

class HostInfo
{
	static final String PORT = "port", HOST = "host";
    String              host;
    Integer             port;
    
	/** 
	* Set the pair (name, value).
	* 
	* @param value		tag's value.
	* @param tag		tag's name.
	*/    
    void setValue(String value, String tag)
     {
        try {
            if (tag.equals(PORT)) port = new Integer(value);  
            else if (tag.equals(HOST)) host = value;
        } catch (Exception ex) { throw new RuntimeException(ex); }
    }
    
	/** 
	 * Return the value of the <code>host</code> tag.
	 *
	 * @return the above mentioned.
	 */
    String getHost()
    {
        return host;
    }
    
	/** 
	 * Return the value of the <code>port</code> tag.
	 *
	 * @return the above mentioned.
	 */
    Integer getPort()
    {
        return port;
    }
   
    
}
