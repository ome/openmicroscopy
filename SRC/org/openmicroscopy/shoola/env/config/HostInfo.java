/*
 * org.openmicroscopy.shoola.env.config.HostInfo
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

/** Creates an object containing the Host's informations
 *
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 */

class HostInfo {
    
    String  host;
    Integer port;
    static final String PORT = "port", HOST = "host";
    
    void setValue(String value, String tag) {
        try {
            if (tag.equals(PORT)) port = new Integer(value); // catch 
            else if (tag.equals(HOST)) host = value;
        } catch (Exception ex) { throw new RuntimeException(ex); }
    }
    String getHost() {
        return host;
    }
    Integer getPort() {
        return port;
    }
   
    
}
