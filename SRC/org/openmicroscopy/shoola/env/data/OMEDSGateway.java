/*
 * org.openmicroscopy.shoola.env.data.OMEDSGateway
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

package org.openmicroscopy.shoola.env.data;

//Java imports
import java.net.URL;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.ds.DataFactory;
import org.openmicroscopy.ds.DataServer;
import org.openmicroscopy.ds.RemoteCaller;
import org.openmicroscopy.ds.RemoteServices;

/** 
 * Unified access point to the various <i>OMEDS</i> services.
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
class OMEDSGateway 
{

	/**
	 * The factory provided by the connection library to access the various
	 * <i>OMEDS</i> services.
	 */
	private RemoteServices	proxiesFactory;
	
	OMEDSGateway(URL omedsAddress) 
	{
		proxiesFactory = DataServer.getDefaultServices(omedsAddress);
	}
	
	/**
	 * Try to connect to <i>OMEDS</i>.
	 * 
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * 			retrieve data from OMEDS service.
	 */
	void login(String userName, String password)
		throws DSOutOfServiceException, DSAccessException
	{
		RemoteCaller proxy = proxiesFactory.getRemoteCaller();
		proxy.login(userName, password);
		
		//TODO: The proxy should throw a checked exception if login fails!
		//Catch that exception when the connection lib will be modified.
	}
	
	void logout()
	{
		RemoteCaller proxy = proxiesFactory.getRemoteCaller();
		proxy.logout();
		
		//TODO: The proxy should throw a checked exception on failure!
		//Catch that exception when the connection lib will be modified.
		
		//TODO: The proxy should have a dispose method to release resources
		//like sockets.  Add this call when connection lib will be modified.
		//proxy.dispose();	
		
	}
	
	DataFactory getDataFactory()
	{
		return (DataFactory) proxiesFactory.getService(DataFactory.class);
	}

}
