/*
 * ome.formats.model.ChannelProcessor
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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

package ome.formats.model;

import static omero.rtypes.rint;
import static omero.rtypes.rstring;

import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import loci.formats.IFormatReader;

import ome.formats.LSID;
import omero.metadatastore.IObjectContainer;
import omero.model.Channel;
import omero.model.IObject;
import omero.model.Image;
import omero.model.LogicalChannel;
import omero.model.Pixels;

/**
 * Processes the pixels sets of an IObjectContainerStore and ensures
 * that LogicalChannel containers are present in the container cache, adding
 * them if they are missing.
 *   
 * @author Chris Allan <callan at blackcat dot ca>
 *
 */
public class ChannelProcessor implements ModelProcessor
{
	/** Logger for this class */
	private Log log = LogFactory.getLog(ChannelProcessor.class);
	
	/** Container store we're currently working with. */
	private IObjectContainerStore store;
	
    /**
     * Processes the OMERO client side metadata store.
     * @param store OMERO metadata store to process.
     * @throws ModelException If there is an error during processing.
     */
    public void process(IObjectContainerStore store)
    	throws ModelException
    {
    	this.store = store;
    	
    	List<Image> images = store.getSourceObjects(Image.class);
    	for (int i = 0; i < images.size(); i++)
    	{
    		Pixels pixels = 
    			(Pixels) store.getSourceObject(new LSID(Pixels.class, i));
    		for (int c = 0; c < pixels.getSizeC().getValue(); c++)
    		{
    			IObject sourceObject =
    				store.getSourceObject(new LSID(Channel.class, i, c));
    			if (sourceObject == null)
    			{
    				LinkedHashMap<String, Integer> indexes = 
    					new LinkedHashMap<String, Integer>();
    				indexes.put("imageIndex", i);
    				indexes.put("logicalChannelIndex", c);
    				IObjectContainer container =
    					store.getIObjectContainer(Channel.class, indexes);
    				sourceObject = container.sourceObject;
    			}
   				populateColor((Channel) sourceObject, i, c);
    			sourceObject =
    				store.getSourceObject(new LSID(LogicalChannel.class, i, c));
    			if (sourceObject == null)
    			{
    				LinkedHashMap<String, Integer> indexes = 
    					new LinkedHashMap<String, Integer>();
    				indexes.put("imageIndex", i);
    				indexes.put("logicalChannelIndex", c);
    				store.getIObjectContainer(LogicalChannel.class, indexes);
    			}
    			populateName((LogicalChannel) sourceObject, i, c);
    		}
    	}
    }
    
    /**
     * Populates the default color for the channel if one does not already
     * exist and the image is RGB(A) or indexed color.
     * @param channel Channel object.
     * @param imageIndex Image/series index.
     * @param channelIndex Channel index.
     */
    private void populateColor(Channel channel, int imageIndex,
    		                   int channelIndex)
    {
    	IFormatReader reader = store.getReader();
    	if (reader.isRGB() || reader.isIndexed())
		{
			int channelCount = 
				store.countCachedContainers(Channel.class, channelIndex); 
			if (channelCount == 3 || channelCount == 4)
			{
				log.debug("Setting color channel to RGB.");
				// red
				if (channelIndex == 0)
				{
					channel.setRed(rint(255));
					channel.setGreen(rint(0));
					channel.setBlue(rint(0));
					channel.setAlpha(rint(255));
				}
				
				// green
				if (channelIndex == 1)
				{
					channel.setRed(rint(0));
					channel.setGreen(rint(255));
					channel.setBlue(rint(0));
					channel.setAlpha(rint(255));
				}
				
				// blue
				if (channelIndex == 2)
				{
					channel.setRed(rint(0));
					channel.setGreen(rint(0));
					channel.setBlue(rint(255));
					channel.setAlpha(rint(255));
				}
				
				// alpha
				if (channelIndex == 3)
				{
					channel.setRed(rint(0));
					channel.setGreen(rint(0));
					channel.setBlue(rint(0));
					channel.setAlpha(rint(0));  // Transparent
				}
			}
		}
    }

	/**
	 * Populates the default channel name for the logical channel if one does 
	 * not already exist and the image is RGB(A) or indexed-color.
	 * @param channel Channel object.
	 * @param imageIndex Image/series index.
	 * @param channelIndex Channel index.
	 */
	private void populateName(LogicalChannel lc, int imageIndex,
			                  int logicalChannelIndex)
	{
		IFormatReader reader = store.getReader();
		if (reader.isRGB() || reader.isIndexed())
		{
			int channelCount = 
				store.countCachedContainers(Channel.class, imageIndex); 
			if (channelCount == 3 || channelCount == 4)
			{
				log.debug("Setting channels name to Red, Green, Blue or Alpha.");
				// red
				if (lc.getName() == null && logicalChannelIndex == 0)
				{
					lc.setName(rstring("Red"));
				}
	
				// green
				if (lc.getName() == null && logicalChannelIndex == 1)
				{
					lc.setName(rstring("Green"));
				}
	
				// blue
				if (lc.getName() == null && logicalChannelIndex == 2)
				{
					lc.setName(rstring("Blue"));
				}
				
				// alpha
				if (lc.getName() == null && logicalChannelIndex == 3)
				{
					lc.setName(rstring("Alpha"));
				}
			}
		}
	}
}
