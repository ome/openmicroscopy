/*
 * training.Connector
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee & Open Microscopy Environment.
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
package training;


//Java imports
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Third-party libraries

import omero.ServerError;
//Application-internal dependencies
import omero.client;
import omero.api.IAdminPrx;
import omero.api.IContainerPrx;
import omero.api.IMetadataPrx;
import omero.api.IPixelsPrx;
import omero.api.IQueryPrx;
import omero.api.IRoiPrx;
import omero.api.IUpdatePrx;
import omero.api.RawFileStorePrx;
import omero.api.RawPixelsStorePrx;
import omero.api.RenderingEnginePrx;
import omero.api.ServiceFactoryPrx;
import omero.api.ThumbnailStorePrx;
import omero.cmd.CmdCallbackI;
import omero.cmd.DoAll;
import omero.cmd.HandlePrx;
import omero.cmd.Request;
import omero.cmd.Response;
import omero.grid.SharedResourcesPrx;

/** 
 * Sample code showing how to connect to an OMERO server.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.3.2
 */
public class Connector {
	
	//The value used if the configuration file is not used. To edit*/
	/** The server address.*/
	private String hostName = "serverName";

	/** The username.*/
	private String userName = "userName";
	
	/** The password.*/
	private String password = "password";
	//end edit
	
	/** Reference to the clients.*/
	protected client client, unsecureClient;
	
	/** The service factory.*/
	protected ServiceFactoryPrx entryUnencrypted;
	
	/** The configuration information.*/
	private ConfigurationInfo info;
	
	/** Connects to the server.*/
	protected void connect()
		throws Exception
	{
		client = new client(info.getHostName(), info.getPort());
		client.createSession(info.getUserName(), info.getPassword());
		// if you want to have the data transfer encrypted then you can 
		// use the entry variable otherwise use the following 
		unsecureClient = client.createClient(false);
		entryUnencrypted = unsecureClient.getSession();
	}

	/** Disconnects.*/
	protected void disconnect()
		throws Exception
	{
		if (client != null) client.__del__(); // No exception
		if (unsecureClient != null) unsecureClient.__del__(); // No exception
	}

	/**
	 * Shows how to connect to omero.
	 * 
	 * @param info Configuration info or <code>null</code>.
	 */
	Connector(ConfigurationInfo info)
	{
		if (info == null) { //run from main
			info = new ConfigurationInfo();
			info.setHostName(hostName);
			info.setPassword(password);
			info.setUserName(userName);
		}
		this.info = info;
		try {
			connect();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				disconnect(); // Be sure to disconnect
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Returns the pixels service.
	 * 
	 * @return See above.
	 * @throws ServerError
	 */
	IPixelsPrx getPixelsService()
		throws ServerError
	{
		return entryUnencrypted.getPixelsService();
	}
	
	/**
	 * Returns the update service.
	 * 
	 * @return See above.
	 * @throws ServerError
	 */
	IUpdatePrx getUpdateService()
		throws ServerError
	{
		return entryUnencrypted.getUpdateService();
	}
	
	/**
	 * Returns the raw pixels store.
	 * 
	 * @return See above.
	 * @throws ServerError
	 */
	RawPixelsStorePrx getRawPixelsStore()
		throws ServerError
	{
		return entryUnencrypted.createRawPixelsStore();
	}

	/**
	 * Returns the container service.
	 * 
	 * @return See above.
	 * @throws ServerError
	 */
	IContainerPrx getContainerService()
		throws ServerError
	{
		return entryUnencrypted.getContainerService();
	}
	
	/**
	 * Returns the shared resources.
	 * 
	 * @return See above.
	 * @throws ServerError
	 */
	SharedResourcesPrx getSharedResources()
		throws ServerError
	{
		return entryUnencrypted.sharedResources();
	}
	
	/**
	 * Returns the thumbnail store.
	 * 
	 * @return See above.
	 * @throws ServerError
	 */
	ThumbnailStorePrx getThumbnailStore()
		throws ServerError
	{
		return entryUnencrypted.createThumbnailStore();
	}
	
	/**
	 * Returns the rendering engine.
	 * 
	 * @return See above.
	 * @throws ServerError
	 */
	RenderingEnginePrx getRenderingEngine()
		throws ServerError
	{
		return entryUnencrypted.createRenderingEngine();
	}
	
	/**
	 * Returns the admin service.
	 * 
	 * @return See above.
	 * @throws ServerError
	 */
	IAdminPrx getAdminService()
		throws ServerError
	{
		return entryUnencrypted.getAdminService();
	}
	
	/**
	 * Returns the query service.
	 * 
	 * @return See above.
	 * @throws ServerError
	 */
	IQueryPrx getQueryService()
		throws ServerError
	{
		return entryUnencrypted.getQueryService();
	}

	/**
	 * Returns the ROI service.
	 * 
	 * @return See above.
	 * @throws ServerError
	 */
	IRoiPrx getRoiService()
		throws ServerError
	{
		return entryUnencrypted.getRoiService();
	}
	
	/**
	 * Returns the Raw File store.
	 * 
	 * @return See above.
	 * @throws ServerError
	 */
	RawFileStorePrx getRawFileStore()
		throws ServerError
	{
		return entryUnencrypted.createRawFileStore();
	}
	
	/**
	 * Returns the Metadata service.
	 * 
	 * @return See above.
	 * @throws ServerError
	 */
	IMetadataPrx getMetadataService()
		throws ServerError
	{
		return entryUnencrypted.getMetadataService();
	}

	/**
	 * Submits the specified commands
	 * 
	 * @param commands The commands to submit.
	 * @return See above.
	 * @throws ServerError
	 * @throws InterruptedException
	 */
	Response submit(List<Request> commands)
		throws ServerError, InterruptedException
	{
		DoAll all = new DoAll();
		all.requests = commands;
		final Map<String, String> callContext = new HashMap<String, String>();
		final HandlePrx prx = entryUnencrypted.submit(all, callContext);
		CmdCallbackI cb = new CmdCallbackI(unsecureClient, prx);
		cb.loop(20, 500);
		return cb.getResponse();
	}
	/**
	 * Runs the script without configuration options.
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		new Connector(null);
		System.exit(0);
	}

}
