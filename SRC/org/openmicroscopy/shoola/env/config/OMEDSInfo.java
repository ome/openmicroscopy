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
	private URL		serverAddress;

	
	/**
	 * Creates a new instance.
	 * This is the only costructor and has package visibility because instances
	 * of this class can only be created (meaningfully) within this package.
	 */
	OMEDSInfo() {}
	
	/**
	 * Parses and sets the <i>URL</i> to connect to <i>OMEDS</i>.
	 * 
	 * @param url	A string representing a valid <i>URL</i>.
	 * @throws MalformedURLException If <code>url</code> specifies a malformed
	 * 									<i>URL</i>.
	 */
	void setServerAddress(String url)
		throws MalformedURLException
	{  
		serverAddress = new URL(url);  
	}
	
	/**
	 * Returns the <i>URL</i> to connect to <i>OMEDS</i>.
	 * 
	 * @return	See above.
	 */
	public URL getServerAddress() { return serverAddress; }

}
