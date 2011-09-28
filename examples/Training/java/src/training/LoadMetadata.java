/*
 * training.LoadMetadata 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee & Open Microscopy Environment.
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

//Third-party libraries

//Application-internal dependencies
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import omero.api.IContainerPrx;
import omero.model.Channel;
import omero.model.Image;
import omero.model.Pixels;
import omero.sys.ParametersI;
import pojos.ChannelData;
import pojos.ImageAcquisitionData;
import pojos.ImageData;

/** 
 * Sample code showing how to load image metadata
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.3.2
 */
public class LoadMetadata 
	extends ConnectToOMERO
{

	/** The id of an image.*/
	private long imageId = 551;
	
	/** Load the image acquisition data.*/
	private void loadAcquisitionData()
		throws Exception
	{
		IContainerPrx proxy = entryUnencrypted.getContainerService();
		List<Long> ids = new ArrayList<Long>();
		ids.add(imageId);
		ParametersI po = new ParametersI();
		po.acquisitionData(); // load the acquisition data.
		List<Image> results = proxy.getImages(Image.class.getName(), ids, po);
		ImageAcquisitionData image = new ImageAcquisitionData(results.get(0));
		//Display information about the image
		//e.g. humidity
		System.err.println(image.getHumidity());
	}
	
	
	/** Load the channel data.*/
	private void loadChannelData()
		throws Exception
	{
		ImageData image = loadImage(imageId);
		long pixelsId = image.getDefaultPixels().getId();
		Pixels pixels = 
			entryUnencrypted.getPixelsService().retrievePixDescription(pixelsId);
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
	 */
	LoadMetadata()
	{
		try {
			connect(); //First connect.
			loadAcquisitionData();
			loadChannelData();
			client.closeSession();
		} catch (Exception e) {
			e.printStackTrace();
			if (client != null) client.closeSession();
		}
	}
	
	public static void main(String[] args) 
	{
		new LoadMetadata();
	}
}
