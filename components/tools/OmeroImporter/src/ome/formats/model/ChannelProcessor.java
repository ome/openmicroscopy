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

import java.util.LinkedHashMap;
import java.util.List;

import ome.formats.LSID;
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
    /**
     * Processes the OMERO client side metadata store.
     * @param store OMERO metadata store to process.
     * @throws ModelException If there is an error during processing.
     */
    public void process(IObjectContainerStore store)
    	throws ModelException
    {
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
    				store.getIObjectContainer(Channel.class, indexes);
    			}
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
    		}
    	}
    }
}
