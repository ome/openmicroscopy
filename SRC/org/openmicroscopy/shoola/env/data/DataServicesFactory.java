/*
 * org.openmicroscopy.shoola.env.data.DataServicesFactory
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

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.ds.DataFactory;
import org.openmicroscopy.ds.DataServer;
import org.openmicroscopy.ds.RemoteCaller;
import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.OMEDSInfo;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.ui.UserCredentials;

/** 
 * A factory for the {@link DataManagementService} and the
 * {@link SemanticTypeService}.
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
public class DataServicesFactory
{
	
	private static DataServicesFactory		singleton;
	
	//NB: this can't be called outside of container b/c agents have no refs
	//to the singleton container. So we can be sure this method is going to
	//create services just once.
	public static DataServicesFactory getInstance(Container c)
	{
		if (c == null)
			throw new NullPointerException();  //An agent called this method?
		if (singleton == null)	singleton = new DataServicesFactory(c);
		return singleton;
	}
	
	private Registry		registry;
	private RemoteCaller	proxy;
	private DMSAdapter		dms;
	private STSAdapter		sts;
	
	
	private DataServicesFactory(Container c)
	{
		registry = c.getRegistry();
		
		//Retrieve the connection URL and create the proxy.
		OMEDSInfo info = (OMEDSInfo) registry.lookup(LookupNames.OMEDS);
		if (info == null)  //TODO: get rid of this when we have an XML schema.
			throw new NullPointerException("No data server host provided!");
		proxy = DataServer.getDefaultCaller(info.getServerAddress());
		
		//Create the DMS adapter.
		DataFactory omeds = new DataFactory(proxy);
		dms = new DMSAdapter(omeds, registry); 
		
		//Create the STS adapter.
		//TODO: implement when SemanticTypeManager is ready.
	}
	
	public DataManagementService getDMS()
	{
		return dms;
	}

	public SemanticTypesService getSTS()
	{
		return sts;
	}
	
	/**
	 * Try to connect to OMEDS.
	 * 
	* @throws DSOutOfServiceException If the connection is broken, or logged in
	* @throws DSAccessException If an error occured while trying to 
	* 			retrieve data from OMEDS service.
	*/
	public void connect()
		throws DSOutOfServiceException, DSAccessException
	{
		UserCredentials uc = (UserCredentials)
								registry.lookup(LookupNames.USER_CREDENTIALS);
		//uc can't be null b/c there's no way to call this method b/f init.
		proxy.login(uc.getUserName(), uc.getPassword());
		//retrieve the user's ID and store it in the UserCredentials.
		uc.setUserID(dms.getUserID());
	}
	
	public void shutdown()
	{
		try {
			proxy.logout();
			//proxy.dispose();	
		} catch (Throwable e) {
			//Ignore, we're quitting the app.
		}
	}
	
}
