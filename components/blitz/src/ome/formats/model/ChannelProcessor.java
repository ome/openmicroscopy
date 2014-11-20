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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import loci.formats.FormatTools;
import loci.formats.IFormatReader;
import ome.formats.model.ChannelData;
import ome.formats.model.ColorsFactory;
import ome.formats.model.IObjectContainerStore;
import ome.formats.model.ModelException;
import ome.formats.model.ModelProcessor;
import ome.util.LSID;
import omero.RInt;
import omero.RString;
import omero.model.Channel;
import omero.model.Filter;
import omero.model.Image;
import omero.model.Laser;
import omero.model.Length;
import omero.model.LightSource;
import omero.model.LogicalChannel;
import omero.model.Pixels;
import omero.model.TransmittanceRange;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.math.DoubleMath;

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

	/** Name of the <code>red</code> component when it is a graphics image. */
	public static final String RED_TEXT = "Red";

	/** Name of the <code>green</code> component when it is a graphics image. */
	public static final String GREEN_TEXT = "Green";

	/** Name of the <code>blue</code> component when it is a graphics image. */
	public static final String BLUE_TEXT = "Blue";

	/** Name of the <code>blue</code> component when it is a graphics image. */
	public static final String ALPHA_TEXT = "Alpha";

	/** Logger for this class */
	private Logger log = LoggerFactory.getLogger(ChannelProcessor.class);

	/** Container store we're currently working with. */
	private IObjectContainerStore store;

	/** Bio-Formats reader implementation we're currently working with. */
	private IFormatReader reader;

	/**
	 * Returns the name from the wavelength.
	 *
	 * @param value The value to handle.
	 * @return See above.
	 */
	private String getNameFromWavelength(Length value)
	{
	    if (value == null) return null;
	    //Check that the value is an int
        if (DoubleMath.isMathematicalInteger(value.getValue())) {
            return ""+value.getValue();
        }
        return value.toString();
	}

	/**
	 * Sets the default color if it is a single channel image.
	 *
	 * @param channelData Channel data to use to set the color.
	 */
	private void setSingleChannel(ChannelData channelData)
	{
	    int channelIndex = channelData.getChannelIndex();
	    Channel channel = channelData.getChannel();
	    Integer red = getValue(channel.getRed());
        Integer green = getValue(channel.getGreen());
        Integer blue = getValue(channel.getBlue());
        Integer alpha = getValue(channel.getAlpha());
        RString name;
        //color already set by Bio-formats
        if (red != null && green != null && blue != null && alpha != null) {
            return;
        }
        int[] defaultColor = ColorsFactory.newGreyColor();
        channel.setRed(
                rint(defaultColor[ColorsFactory.RED_INDEX]));
        channel.setGreen(
                rint(defaultColor[ColorsFactory.GREEN_INDEX]));
        channel.setBlue(
                rint(defaultColor[ColorsFactory.BLUE_INDEX]));
        channel.setAlpha(
                rint(defaultColor[ColorsFactory.ALPHA_INDEX]));
	}

	/**
     * Populates the default color for the channel if one does not already
     * exist.
     *
     * @param channelData Channel data to use to set the color.
     * @param isGraphicsDomaind Whether or not the image is in the graphics
	 * domain according to Bio-Formats.
     */
    private void populateDefault(ChannelData channelData,
		                   boolean isGraphicsDomain)
    {
	int channelIndex = channelData.getChannelIndex();
	Channel channel = channelData.getChannel();
	LogicalChannel lc = channelData.getLogicalChannel();
	int[] defaultColor;
	if (isGraphicsDomain)
		{
	    log.debug("Setting color channel to RGB.");
	    setDefaultChannelColor(channel, channelIndex);
	    switch (channelIndex) {
				case 0: //red
		        if (lc.getName() == null)
				lc.setName(rstring(RED_TEXT));
		        break;
				case 1: //green
		        if (lc.getName() == null)
				lc.setName(rstring(GREEN_TEXT));
		        break;
				case 2: //blue
		        if (lc.getName() == null)
				lc.setName(rstring(BLUE_TEXT));
		        break;
				case 3: //alpha, reset transparent
					channel.setRed(rint(0));
		        channel.setGreen(rint(0));
		        channel.setBlue(rint(0));
		        channel.setAlpha(rint(0)); //transparent
		        if (lc.getName() == null)
				lc.setName(rstring(ALPHA_TEXT));
			}
	    return;
		}

        Integer red = getValue(channel.getRed());
        Integer green = getValue(channel.getGreen());
        Integer blue = getValue(channel.getBlue());
        Integer alpha = getValue(channel.getAlpha());
        RString name;
        //color already set by Bio-formats
        if (red != null && green != null && blue != null && alpha != null) {
            //Try to set the name.
            log.debug("Already set in BF.");
            if (lc.getName() == null) {
                name = getChannelName(channelData);
                if (name != null) lc.setName(name);
            }
            return;
        }

        //not set by
        //First we check the emission wavelength.

        Length valueWavelength = lc.getEmissionWave();
        if (valueWavelength != null) {
            setChannelColor(channel, channelIndex,
                ColorsFactory.determineColor(valueWavelength));
            if (lc.getName() == null) {
                lc.setName(rstring(getNameFromWavelength(valueWavelength)));
            }
            return;
        }

        Length valueFilter = null;

	//First check the emission filter.
	//First check if filter
	Filter f = getValidFilter(channelData.getLightPathEmissionFilters(),
			true);
	if (f != null)
		valueFilter = ColorsFactory.getValueFromFilter(f, true);
	if (valueFilter != null) {
		setChannelColor(channel, channelIndex,
				ColorsFactory.determineColor(valueFilter));
		if (lc.getName() == null) {
			name = getNameFromFilter(f);
			if (name != null) lc.setName(name);
		}
		return;
	}
	f = channelData.getFilterSetEmissionFilter();
	valueFilter = ColorsFactory.getValueFromFilter(f, true);

	if (valueFilter != null) {
		setChannelColor(channel, channelIndex,
				ColorsFactory.determineColor(valueFilter));
		if (lc.getName() == null) {
			name = getNameFromFilter(f);
			if (name != null) lc.setName(name);
		}
		return;
	}
	//Laser
	if (channelData.getLightSource() != null) {
		LightSource ls = channelData.getLightSource();
		if (ls instanceof Laser) {
			valueWavelength = ((Laser) ls).getWavelength();
			if (valueWavelength != null) {
			setChannelColor(channel, channelIndex,
					ColorsFactory.determineColor(valueWavelength));
			if (lc.getName() == null) {
			    lc.setName(rstring(getNameFromWavelength(valueWavelength)));
			}
			return;
		}
		}
	}
	//Excitation
	valueWavelength = lc.getExcitationWave();
	if (valueWavelength != null) {
		setChannelColor(channel, channelIndex,
				ColorsFactory.determineColor(valueWavelength));
		if (lc.getName() == null) {
		    lc.setName(rstring(getNameFromWavelength(valueWavelength)));
		}
		return;
	}
	f = getValidFilter(channelData.getLightPathExcitationFilters(), false);
	if (f != null)
		valueFilter = ColorsFactory.getValueFromFilter(f, false);

	if (valueFilter != null) {
		setChannelColor(channel, channelIndex,
				ColorsFactory.determineColor(valueFilter));
		if (lc.getName() == null) {
			name = getNameFromFilter(f);
			if (name != null) lc.setName(name);
		}
		return;
	}
	f = channelData.getFilterSetExcitationFilter();
	valueFilter = ColorsFactory.getValueFromFilter(f, false);

	if (valueFilter != null) {
		setChannelColor(channel, channelIndex,
				ColorsFactory.determineColor(valueFilter));
		if (lc.getName() == null) {
			name = getNameFromFilter(f);
			if (name != null) lc.setName(name);
		}
		return;
	}

	//not been able to set the color

	setDefaultChannelColor(channel, channelIndex);
    }

    /**
     * Returns the first filter from the list with a <code>not null</code>
     * value.
     *
     * @param filters The collection to handle.
     * @param emission Passed <code>true</code> to indicate that the filter
     * is an emission filter, <code>false</code> otherwise.
     * @return See above.
     */
    private Filter getValidFilter(List<Filter> filters, boolean emission)
    {
	if (filters == null) return null;
	Iterator<Filter> i = filters.iterator();
	Length value = null;
	Filter f;
	while (i.hasNext()) {
		f = i.next();
		value = ColorsFactory.getValueFromFilter(f, emission);
			if (value != null) return f;
		}
	return null;
    }

    /**
     * Sets the default color of the channel.
     *
     * @param channel The channel to handle.
     * @param index   The index of the channel.
     */
    private void setDefaultChannelColor(Channel channel, int index)
    {
	//not been able to set the color
	int[] defaultColor;
	switch (index) {
		case 0: //red
			defaultColor = ColorsFactory.newRedColor();
	        break;
		case 1: //green
			defaultColor = ColorsFactory.newGreenColor();
	        break;
		default: //blue
			defaultColor = ColorsFactory.newBlueColor();
	}
	channel.setRed(
				rint(defaultColor[ColorsFactory.RED_INDEX]));
		channel.setGreen(
				rint(defaultColor[ColorsFactory.GREEN_INDEX]));
		channel.setBlue(
				rint(defaultColor[ColorsFactory.BLUE_INDEX]));
		channel.setAlpha(
				rint(defaultColor[ColorsFactory.ALPHA_INDEX]));
    }

    /**
     * Sets the color of the channel.
     *
     * @param channel The channel to handle.
     * @param index   The index of the channel.
     * @param rgba    The color to set.
     */
    private void setChannelColor(Channel channel, int index, int[] rgba)
    {
	if (rgba == null) {
		setDefaultChannelColor(channel, index);
		return;
	}
	channel.setRed(rint(rgba[0]));
	channel.setGreen(rint(rgba[1]));
	channel.setBlue(rint(rgba[2]));
	channel.setAlpha(rint(rgba[3]));
    }

    /**
     * Returns a channel name string from a given filter.
     *
     * @param filter Filter to retrieve a channel name from.
     * @return See above.
     */
    private RString getNameFromFilter(Filter filter)
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
     *
     * @param value OMERO rtype to get the value of.
     * @return Concrete value of <code>value</code> or <code>null</code> if
     * <code>value == null</code>.
     */
    private Integer getValue(RInt value)
    {
	return value == null? null : value.getValue();
    }

    /**
     * Determines the name of the channel.
     * This method should only be invoked when a color was assigned by
     * Bio-formats.
     *
     * @param channelData The channel to handle.
     * @return See above.
     */
    private RString getChannelName(ChannelData channelData)
    {
	LogicalChannel lc = channelData.getLogicalChannel();
	Length value = lc.getEmissionWave();
	RString name;
	if (value != null)
	{
		return rstring(getNameFromWavelength(value));
	}
	Iterator<Filter> i;
	List<Filter> filters = channelData.getLightPathEmissionFilters();
	if (filters != null) {
		i = filters.iterator();
		while (i.hasNext()) {
			name = getNameFromFilter(i.next());
			if (name != null) return name;
		}
	}

	name = getNameFromFilter(channelData.getFilterSetEmissionFilter());
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
			value = laser.getWavelength();
			if (value != null)
			{
			    return rstring(getNameFromWavelength(value));
			}
		}
	}
	value = lc.getExcitationWave();
	if (value != null)
	{
	    return rstring(getNameFromWavelength(value));
	}
	filters = channelData.getLightPathExcitationFilters();
	if (filters != null) {
		i = filters.iterator();
		while (i.hasNext()) {
			name = getNameFromFilter(i.next());
			if (name != null) return name;
			}
	}
	return getNameFromFilter(channelData.getFilterSetExcitationFilter());
    }

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
		log.warn("Unexpected null reader.");
		return;
	}

	List<Image> images = store.getSourceObjects(Image.class);
	String[] domains = reader.getDomains();
	boolean isGraphicsDomain = false;
	for (String domain : domains)
	{
		if (FormatTools.GRAPHICS_DOMAIN.equals(domain))
		{
			log.debug("Images are of the graphics domain.");
			isGraphicsDomain = true;
			break;
		}
	}
	log.debug("isGraphicsDomain: "+isGraphicsDomain);
	int count;
	boolean v;
	Map<ChannelData, Boolean> m;
	Pixels pixels;
	int sizeC;
	ChannelData channelData;
	Iterator<ChannelData> k;
	for (int i = 0; i < images.size(); i++)
	{
		pixels = (Pixels) store.getSourceObject(new LSID(Pixels.class, i));
		if (pixels == null)
		{
			throw new ModelException("Unable to locate Pixels:" + i);
		}
		//Require to reset transmitted light
		count = 0;
		m = new HashMap<ChannelData, Boolean>();

		//Think of strategy for images with high number of channels
		//i.e. > 6
		sizeC = pixels.getSizeC().getValue();
		if (sizeC == 1) {
		    channelData = ChannelData.fromObjectContainerStore(store, i, 0);
		    setSingleChannel(channelData);
		} else {
		    for (int c = 0; c < sizeC; c++)
	        {
	            channelData = ChannelData.fromObjectContainerStore(store, i, c);
	            //Color section
	            populateDefault(channelData, isGraphicsDomain);

	                //only retrieve if not graphics
	                if (!isGraphicsDomain) {
	            //Determine if the channel same emission wavelength.
	            v = ColorsFactory.hasEmissionData(channelData);
	            if (!v)
	            {
	                count++;
	            }
	                    m.put(channelData, v);
	                }
	        }
		}

		//Need to reset the color of transmitted light
		//i.e. images with several "emission channels"
		//check if 0 size

		if (count > 0 && count != m.size()) {
			k = m.keySet().iterator();
			while (k.hasNext()) {
				channelData = k.next();
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

}
