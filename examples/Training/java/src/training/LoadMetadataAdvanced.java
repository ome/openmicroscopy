/*
 * training.LoadMetadataAdvanced
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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

//Third-party libraries





//Application-internal dependencies
import omero.api.IContainerPrx;
import omero.gateway.Gateway;
import omero.gateway.LoginCredentials;
import omero.gateway.SecurityContext;
import omero.log.SimpleLogger;
import omero.model.Channel;
import omero.model.Image;
import omero.model.Pixels;
import omero.sys.ParametersI;
import pojos.ChannelData;
import pojos.ExperimenterData;
import pojos.ImageAcquisitionData;
import pojos.ImageData;

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
	private String hostName = "serverName";

	/** The username.*/
	private String userName = "userName";
	
	/** The password.*/
	private String password = "password";
	
	private long imageId = 1;
	//end edit
	
	private Gateway gateway;
    
    private SecurityContext ctx;
	
    //TODO: Implement and use a MetadataFacility for doing this stuff!
    
	/**
	 * Load the image acquisition data.
	 * 
	 * @param info The configuration information.
	 */
	private void loadAcquisitionData(ConfigurationInfo info)
		throws Exception
	{
		IContainerPrx proxy = gateway.getPojosService(ctx);
		ParametersI po = new ParametersI();
		po.acquisitionData(); // load the acquisition data.
		List<Image> results = proxy.getImages(Image.class.getName(), 
				Arrays.asList(info.getImageId()), po);
		if (results.size() == 0)
			throw new Exception("Image does not exist. Check ID.");
		ImageAcquisitionData image = new ImageAcquisitionData(results.get(0));
		//Display information about the image
		//e.g. humidity
		System.err.println(image.getHumidity());
	}
	
	/**
	 * Loads the image.
	 * 
	 * @param imageID The id of the image to load.
	 * @return See above.
	 */
	private ImageData loadImage(long imageID)
		throws Exception
	{
		IContainerPrx proxy = gateway.getPojosService(ctx);
		List<Image> results = proxy.getImages(Image.class.getName(),
				Arrays.asList(imageID), new ParametersI());
		//You can directly interact with the IObject or the Pojos object.
		//Follow interaction with the Pojos.
		if (results.size() == 0)
			throw new Exception("Image does not exist. Check ID.");
		return new ImageData(results.get(0));
	}
	
	/**
	 * Load the channel data.
	 * 
	 * @param info The configuration information.
	 */
	private void loadChannelData(ConfigurationInfo info)
		throws Exception
	{
		ImageData image = loadImage(info.getImageId());
		if (image == null)
			throw new Exception("Image does not exist. Check ID.");
		long pixelsId = image.getDefaultPixels().getId();
		Pixels pixels =
			gateway.getPixelsService(ctx).retrievePixDescription(pixelsId);
		List<Channel> l = pixels.copyChannels();
		Iterator<Channel> i = l.iterator();
		int index = 0;
		//Easier to use Pojo to access data.
		ChannelData channel;
		while (i.hasNext()) {
			channel = new ChannelData(index, i.next());
			index++;
		}
	}

	/**
	 * Connects and invokes the various methods.
	 * 
	 * @param info The configuration information.
	 */
	LoadMetadataAdvanced(ConfigurationInfo info)
	{
		if (info == null) {
			info = new ConfigurationInfo();
			info.setHostName(hostName);
			info.setPassword(password);
			info.setUserName(userName);
			info.setImageId(imageId);
		}
		
		LoginCredentials cred = new LoginCredentials();
        cred.getServer().setHostname(info.getHostName());
        cred.getServer().setPort(info.getPort());
        cred.getUser().setUsername(info.getUserName());
        cred.getUser().setPassword(info.getPassword());

        gateway = new Gateway(new SimpleLogger());
        
		try {
		    //First connect.
		    ExperimenterData user = gateway.connect(cred);
            ctx = new SecurityContext(user.getGroupId());
           
			loadAcquisitionData(info);
			loadChannelData(info);
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
		new LoadMetadataAdvanced(null);
		System.exit(0);
	}
}
