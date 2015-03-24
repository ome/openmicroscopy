/*
 * training.DeleteData 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee & Open Microscopy Environment.
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//OMERO dependencies
import omero.cmd.Delete2;
import omero.cmd.Request;
import omero.cmd.Response;
import omero.model.Image;
import omero.model.ImageI;

/** 
 * Sample code showing how to delete data.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.3.2
 */
public class DeleteData
{

	//The value used if the configuration file is not used.*/
	/** The server address.*/
	private String hostName = "serverName";

	/** The username.*/
	private String userName = "userName";
	
	/** The password.*/
	private String password = "password";
	//end edit
	
	/** Reference to the connector.*/
	private Connector connector;
	
	/** 
	 * Delete Image.
	 * 
	 * In the following example, we create an image and delete it.
	 */
	private void deleteImage()
		throws Exception
	{
		//First create an image.
		Image img = new ImageI();
		img.setName(omero.rtypes.rstring("image1"));
		img.setDescription(omero.rtypes.rstring("descriptionImage1"));
		img = (Image) connector.getUpdateService().saveAndReturnObject(img);
		
		Delete2 deleteCmd = new Delete2();
		List<Long> ids = Collections.singletonList(img.getId().getValue());
		deleteCmd.targetObjects = new HashMap<String, List<Long>>();
		deleteCmd.targetObjects.put("Image", ids);
		List<Request> requests = Collections.<Request>singletonList(deleteCmd);
		Response rsp = connector.submit(requests);
		System.err.println(rsp);
	}
	/**
	 * Connects and invokes the various methods.
	 * 
	 * @param info The configuration information.
	 */
	DeleteData(ConfigurationInfo info)
	{
		if (info == null) {
			info = new ConfigurationInfo();
			info.setHostName(hostName);
			info.setPassword(password);
			info.setUserName(userName);
		}
		connector = new Connector(info);
		try {
			connector.connect();
			deleteImage();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				connector.disconnect(); // Be sure to disconnect
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Runs the script without configuration options.
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		new DeleteData(null);
		System.exit(0);
	}

}
