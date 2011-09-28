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

import java.util.ArrayList;
import java.util.List;

import omero.api.IContainerPrx;
import omero.model.Image;
import omero.sys.ParametersI;
import pojos.ImageData;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Sample code showing how to load image metadata
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class LoadMetadata 
	extends ConnectToOMERO
{

	/** The image.*/
	private ImageData image;
	
	/** The id of an image.*/
	private long imageId = 551;
	
	/** Load the image.*/
	private void loadImage()
		throws Exception
	{
		image = loadImage(imageId);
	}
	
	/** Load channel metadata.*/
	private void loadChannelMetadata()
		throws Exception
	{
		image = loadImage(imageId);
	}
	
	/**
	 * Connects and invokes the various methods.
	 */
	LoadMetadata()
	{
		try {
			connect(); //First connect.
			loadImage();
			client.closeSession();
		} catch (Exception e) {
			if (client != null) client.closeSession();
		}
		
		
	}
	
	public static void main(String[] args) 
	{
		new LoadMetadata();
	}
}
