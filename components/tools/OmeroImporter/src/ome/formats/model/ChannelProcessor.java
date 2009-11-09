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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import loci.formats.FormatTools;
import loci.formats.IFormatReader;

import ome.util.LSID;
import omero.RInt;
import omero.RString;
import omero.model.Channel;
import omero.model.Filter;
import omero.model.Image;
import omero.model.Laser;
import omero.model.LightSource;
import omero.model.LogicalChannel;
import omero.model.Pixels;
import omero.model.TransmittanceRange;

/**
 * Processes the pixels sets of an IObjectContainerStore and ensures
 * that LogicalChannel containers are present in the container cache, and
 * populating channel name and colour where appropriate.
 *   
 * @author Chris Allan <callan at blackcat dot ca>
 * @author Jean-Marie <jburel at dundee dot ac dot uk>
 *
 */
public class ChannelProcessor implements ModelProcessor
{
	/** Logger for this class */
	private Log log = LogFactory.getLog(ChannelProcessor.class);
	
	/** Container store we're currently working with. */
	private IObjectContainerStore store;
	
	/** Bio-Formats reader implementation we're currently working with. */
	private IFormatReader reader;
	
    /**
     * Processes the OMERO client side metadata store.
     * @param store OMERO metadata store to process.
     * @throws ModelException If there is an error during processing.
     */
    public void process(IObjectContainerStore store)
    	throws ModelException
    {
    	this.store = store;
    	reader = this.store.getReader();
    	if (reader == null)
    	{
    		throw new ModelException("Unexpected null reader.");
    	}
    	
    	List<Image> images = store.getSourceObjects(Image.class);
    	String[] domains = reader.getDomains();
    	boolean isGraphicsDomain = false;
    	for (String domain : domains)
    	{
    		if (domain.equals(FormatTools.GRAPHICS_DOMAIN))
    		{
    			log.debug("Images are of the grahpics domain.");
    			isGraphicsDomain = true;
    			break;
    		}
    	}
    	List<Boolean> values;
    	boolean v;
    	Map<ChannelData, Boolean> m;
    	for (int i = 0; i < images.size(); i++)
    	{
    		Pixels pixels = 
    			(Pixels) store.getSourceObject(new LSID(Pixels.class, i));
    		if (pixels == null)
    		{
    			throw new ModelException("Unable to locate Pixels:" + i);
    		}
    		//Require to reset transmitted light
    		values = new ArrayList<Boolean>();
    		m = new HashMap<ChannelData, Boolean>();
    		
    		//Think of strategy for images with high number of channels
    		//i.e. > 6
    		int sizeC = pixels.getSizeC().getValue();
    		for (int c = 0; c < sizeC; c++)
    		{
    			ChannelData channelData = 
    				ChannelData.fromObjectContainerStore(store, i, c);
    			//Color section
    			populateColor(channelData, isGraphicsDomain);

                //only retrieve if not graphcis
                if (!isGraphicsDomain) {
                	//See logical channel retrieval
                	//See the color = ColorsFactory.getColor(Channel channel, LogicalChannel lc) to
                	//determine the color of the channel
                	//Do we have emission data
                	v = ColorsFactory.hasEmissionData(channelData);
                	if (!v)
                	{
                		values.add(v);
                	}
                    m.put(channelData, v);
                }
   			    populateName(channelData, isGraphicsDomain);
    		}
    		
	    	//Need to reset the color of transmitted light
	    	//i.e. images with several "emission channels"
	    	//check if 0 size
	    	if (values.size() != m.size()) {
	    		Iterator<ChannelData> k = m.keySet().iterator();
	    		while (k.hasNext()) {
	    			ChannelData channelData = k.next();
	    			if (!m.get(channelData)) {
	    				int[] defaultColor = ColorsFactory.newWhiteColor();
	    				Channel channel = channelData.getChannel();
	    				channel.setRed(
	    						rint(defaultColor[ColorsFactory.RED_INDEX]));
	    				channel.setGreen(
	    						rint(defaultColor[ColorsFactory.GREEN_INDEX]));
	    				channel.setBlue(
	    						rint(defaultColor[ColorsFactory.BLUE_INDEX]));
	    				channel.setAlpha(
	    						rint(defaultColor[ColorsFactory.ALPHA_INDEX]));
	    			}
	    		}
	    	}
    	}
    }
    
    /**
     * Populates the default color for the channel if one does not already
     * exist.
     * @param channelData Channel data to use to inform our name decision.
     * @param isGraphicsDomaind Whether or not the image is in the graphics
	 * domain according to Bio-Formats.
     */
    private void populateColor(ChannelData channelData,
    		                   boolean isGraphicsDomain)
    {
    	
    	int[] channelColor = ColorsFactory.getColor(channelData);
    	int channelIndex = channelData.getChannelIndex();
    	Channel channel = channelData.getChannel();
    	if (channelColor != null && !isGraphicsDomain)
    	{
	        channel.setRed(rint(channelColor[0]));
	        channel.setGreen(rint(channelColor[1]));
	        channel.setBlue(rint(channelColor[2]));
	        channel.setAlpha(rint(channelColor[3]));
    	}
    	if (isGraphicsDomain)
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
    
    /**
     * Returns a channel name string from a given filter.
     * @param filter Filter to retrieve a channel name from.
     * @return See above.
     */
    private RString getValueFromFilter(Filter filter)
    {
        if (filter == null)
        {
        	return null;
        }
        TransmittanceRange t = filter.getTransmittanceRange();
        return t == null? null : 
        	rstring(String.valueOf(t.getCutIn().getValue()));
    }
    
    /**
     * Returns the concrete value of an OMERO rtype.
     * @param value OMERO rtype to get the value of.
     * @return Concrete value of <code>value</code> or <code>null</code> if
     * <code>value == null</code>.
     */
    private static Integer getValue(RInt value)
    {
    	return value == null? null : value.getValue();
    }
    
    private RString getChannelName(ChannelData channelData)
    {
    	LogicalChannel lc = channelData.getLogicalChannel();
    	Integer value = getValue(lc.getEmissionWave());
    	RString name;
    	if (value != null)
    	{
    		return rstring(value.toString());
    	}
    	name = getValueFromFilter(channelData.getFilterSetEmissionFilter());
    	if (name != null)
    	{
    		return name;
    	}
    	name = getValueFromFilter(channelData.getSecondaryEmissionFilter());
    	if (name != null)
    	{
    		return name;
    	}
    	//Laser
    	LightSource ls = channelData.getLightSource();
    	if (ls != null)
    	{
    		if (ls instanceof Laser)
    		{
    			Laser laser = (Laser) ls;
    			value = getValue(laser.getWavelength());
    			if (value != null)
    			{
    				return rstring(value.toString());
    			}
    		}
    	}
    	value = getValue(lc.getExcitationWave());
    	if (value != null)
    	{
    		return rstring(value.toString());
    	}
    	name = getValueFromFilter(channelData.getFilterSetExcitationFilter());
    	if (name != null)
    	{
    		return name;
    	}
    	return getValueFromFilter(channelData.getSecondaryExcitationFilter());
    }

	/**
	 * Populates the default channel name for the logical channel if one does 
	 * not already exist.
	 * @param channelData Channel data to use to inform our name decision.
	 * @param isGraphicsDomaind Whether or not the image is in the graphics
	 * domain according to Bio-Formats.
	 */
	private void populateName(ChannelData channelData, boolean isGraphicsDomain)
	{
		LogicalChannel lc = channelData.getLogicalChannel();
		int logicalChannelIndex = channelData.getChannelIndex();
		RString name = getChannelName(channelData);
		if (lc.getName() == null)
		{
			lc.setName(name);
			return;
		}
		if (isGraphicsDomain)
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
