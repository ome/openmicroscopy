/*
 * training.ConnectToOMERO 
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
import java.util.ArrayList;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import omero.client;
import omero.api.IContainerPrx;
import omero.api.ServiceFactoryPrx;
import omero.model.Image;
import omero.sys.ParametersI;
import pojos.ImageData;

/** 
 * Sample code showing how to connect to an OMERO server.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.3.2
 */
public class ConnectToOMERO {

	/** The name space used during the training.*/
	String trainingNameSpace = "imperial.training.demo";
	
	//The value used if the configuration file is not used.*/
	/** The server address.*/
	private String hostName = "howe.openmicroscopy.org.uk";//"serverName";

	/** The username.*/
	private String userName = "user-1";//"userName";
	
	/** The password.*/
	private String password = "ome";//"password";
	
	/** Reference to the clients.*/
	protected client client, unsecureClient;
	
	/** The service factory.*/
	protected ServiceFactoryPrx entryUnencrypted;
	
	/**
	 * Connects to the server.
	 * 
	 * @param info Configuration info or <code>null</code>.
	 */
	protected void connect(ConfigurationInfo info)
		throws Exception
	{
		client = new client(info.getHostName(), info.getPort());
		client.createSession(info.getHostName(), info.getPassword());
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

	/** Load the image.*/
	protected ImageData loadImage(long imageId)
		throws Exception
	{
		IContainerPrx proxy = entryUnencrypted.getContainerService();
		List<Long> ids = new ArrayList<Long>();
		ids.add(imageId);
		List<Image> results = proxy.getImages(Image.class.getName(), ids, 
				new ParametersI());
		//You can directly interact with the IObject or the Pojos object.
		//Follow interaction with the Pojos.
		if (results.size() == 0) return null;
		return new ImageData(results.get(0));
	}
	
	/**
	 * Shows how to connect to omero.
	 * 
	 * @param info Configuration info or <code>null</code>.
	 */
	ConnectToOMERO(ConfigurationInfo info)
	{
		if (info == null) { //run from main
			info = new ConfigurationInfo();
			info.setHostName(hostName);
			info.setPassword(password);
			info.setUserName(userName);
		}
		try {
			connect(info);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Runs the script with configuration options.
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		ConnectToOMERO connector = new ConnectToOMERO(null);
		try {
			connector.disconnect(); // Be sure to disconnect
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

}
