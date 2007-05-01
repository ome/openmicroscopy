/*
 * measurement.ImageModel 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.util.ui.measurement.model;


//Java imports
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.ChannelMetadata;
import org.openmicroscopy.shoola.util.ui.roi.model.util.Coord3D;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ImageModel 
{
	public ChannelInfo		channelInfo;
	public double			micronsPixelX;
	public double			micronsPixelY;
	public double			micronsPixelZ;
	public double			width, height;
	public int			    numZSections;
	public int			    numTSections;
	
	public Coord3D			coord;
	
	public ImageModel()
	{
		coord = new Coord3D(0,0);
	}
	
//	public void setCurrentChannel(int channel)
//	{
//		coord.c = channel;
//	}
	
	public void setCurrentZ(int z)
	{
		coord.z = z;
	}
	
	public void setCurrentT(int t)
	{
		coord.t = t;
	}
	
	public Coord3D getCoord3D()
	{
		return new Coord3D(coord.t, coord.z);
	}
	
	public void setCoord3D(Coord3D coord)
	{
		this.coord = coord.clone();
	}
	
	public void setMicronsPixelX(double x)
	{
		micronsPixelX = x;
	}

	public void setMicronsPixelY(double y)
	{
		micronsPixelY = y;
	}

	public void setMicronsPixelZ(double z)
	{
		micronsPixelZ = z;
	}
	
	public void setImageDimensions(double w, double h)
	{
		width = w;
		height = h;
	}
		
	public void setMaxT(int maxT)
	{
		numTSections = maxT;
	}
	
	public void setMaxZ(int maxZ)
	{
		numZSections = maxZ;
	}
	
	public int getNumChannels()
	{
		return channelInfo.numChannels;
	}
	
	public void setChannelData(ChannelMetadata[] channelset, ArrayList<Color> colours)
	{
		channelInfo = new ChannelInfo();
		channelInfo.numChannels = channelset.length; 
	
		
		for(int i = 0 ; i < channelset.length; i++)
		{
			ChannelField field = new ChannelField(
									channelset[i].getEmissionWavelength()+"",
									channelset[i].getEmissionWavelength()+"",
									colours.get(i));
			channelInfo.channels.add(field);
		}
	}
	
	
}


