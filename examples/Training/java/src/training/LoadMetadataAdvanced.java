/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2016 University of Dundee & Open Microscopy Environment.
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

import java.util.List;

import omero.gateway.Gateway;
import omero.gateway.LoginCredentials;
import omero.gateway.SecurityContext;
import omero.gateway.facility.MetadataFacility;
import omero.log.SimpleLogger;
import omero.model.ImageI;
import omero.gateway.model.ChannelData;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.ImageAcquisitionData;

/** 
 * Sample code showing how to load image metadata
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.3.2
 */
public class LoadMetadataAdvanced 
{

	//The value used if the configuration file is not used. To edit*/
	/** The server address.*/
	private static String hostName = "serverName";

	/** The username.*/
	private static String userName = "userName";
	
	/** The password.*/
	private static String password = "password";
	
	private static long imageId = 1;
	//end edit
	
	private Gateway gateway;
    
    private SecurityContext ctx;
	
	/**
	 * Load the image acquisition data.
	 * 
	 * @param info The configuration information.
	 */
	private void loadAcquisitionData(long imageId)
		throws Exception
	{
	    MetadataFacility mdf = gateway.getFacility(MetadataFacility.class);
	    ImageAcquisitionData image = mdf.getImageAcquisitionData(ctx, imageId);
		System.err.println(image.getHumidity());
	}
	
	/**
	 * Load the channel data.
	 * 
	 * @param info The configuration information.
	 */
	private void loadChannelData(long imageId)
		throws Exception
	{
	    MetadataFacility mdf = gateway.getFacility(MetadataFacility.class);
	    
	    List<ChannelData> data = mdf.getChannelData(ctx, imageId);
	    for(ChannelData c : data) {
	        System.out.println(c.getIndex());
	    }
	}

	/**
	 * Connects and invokes the various methods.
	 * 
	 * @param args The login credentials
	 * @param imageId The image id
	 */
	LoadMetadataAdvanced(String[] args, long imageId)
	{
		LoginCredentials cred = new LoginCredentials(args);

        gateway = new Gateway(new SimpleLogger());
        
		try {
		    //First connect.
		    ExperimenterData user = gateway.connect(cred);
            ctx = new SecurityContext(user.getGroupId());
           
			loadAcquisitionData(imageId);
			loadChannelData(imageId);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
			    gateway.disconnect(); // Be sure to disconnect
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
	    if (args == null || args.length == 0)
            args = new String[] { "--omero.host=" + hostName,
                    "--omero.user=" + userName, "--omero.pass=" + password };
	    
		new LoadMetadataAdvanced(args, imageId);
		System.exit(0);
	}
}
