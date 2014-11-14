/*
 * ome.formats.model.ColorsFactory
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.formats.model;

import static ome.formats.model.UnitsFactory.makeLength;

import java.util.Iterator;
import java.util.List;

import omero.RInt;
import omero.model.Channel;
import omero.model.Filter;
import omero.model.Laser;
import omero.model.LightSource;
import omero.model.LogicalChannel;
import omero.model.TransmittanceRange;
import omero.model.Length;


/**
 * Utility class to determine the color usually associated to a specified
 * channel depending on its emission wavelength. Ported from the server side
 * omeis.providers.re.ColorsFactory.
 *
 * @author Chris Allan <callan at blackcat dot ca>
 */
public class ColorsFactory {

    /** Index of the red component of a color. */
    public static final int RED_INDEX = 0;

    /** Index of the red component of a color. */
    public static final int GREEN_INDEX = 1;

    /** Index of the red component of a color. */
    public static final int BLUE_INDEX = 2;

    /** Index of the red component of a color. */
    public static final int ALPHA_INDEX = 3;

    /** The Default value for the alpha component. */
    static final int DEFAULT_ALPHA = 255;

    /**
     * Lower bound of the wavelength interval corresponding to a
     * <code>BLUE</code> color.
     */
    private static final int BLUE_MIN = 400;

    /**
     * Upper bound of the wavelength interval corresponding to a
     * <code>BLUE</code> color and lower bound of the wavelength
     * interval corresponding to a <code>GREEN</code> color.
     */
    private static final int BLUE_TO_GREEN_MIN = 500;

    /**
     * Upper bound of the wavelength interval corresponding to a
     * <code>GREEN</code> color and lower bound of the wavelength
     * interval corresponding to a <code>RED</code> color.
     */
    private static final int GREEN_TO_RED_MIN = 560;

    /**
     * Upper bound of the wavelength interval corresponding to a
     * <code>RED</code> color.
     */
    private static final int RED_MAX = 700;

    /** The value to add to the cut-in to determine the color. */
    private static final int RANGE = 15;

    /**
     * Returns <code>true</code> if the wavelength is in the blue
     * color band, <code>false</code> otherwise.
     *
     * @param wavelength The wavelength to handle.
     * @return See above.
     */
    private static boolean rangeBlue(double wavelength) {
        return wavelength < BLUE_TO_GREEN_MIN;
    }

    /**
     * Returns <code>true</code> if the wavelength is in the green
     * color band, <code>false</code> otherwise.
     *
     * @param wavelength The wavelength to handle.
     * @return See above.
     */
    private static boolean rangeGreen(double wavelength) {
        return wavelength >= BLUE_TO_GREEN_MIN && wavelength < GREEN_TO_RED_MIN;
    }

    /**
     * Returns <code>true</code> if the wavelength is in the red
     * color band, <code>false</code> otherwise.
     *
     * @param wavelength The wavelength to handle.
     * @return See above.
     */
    private static boolean rangeRed(double wavelength) {
        return wavelength >= GREEN_TO_RED_MIN;
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

    /**
     * Returns <code>true</code> if the channel has emission metadata,
     * <code>false</code> otherwise.
     *
     * @param f The filter to handle.
     * @return See above.
     */
    private static boolean isFilterHasEmissionData(Filter f)
    {
	if (f == null) return false;
	TransmittanceRange transmittance = f.getTransmittanceRange();
	if (transmittance == null) return false;
	return transmittance.getCutIn() != null;
    }

    /**
     * Returns <code>true</code> if the channel has emission and/or excitation
     * information, <code>false</code> otherwise.
     *
     * @param channelData Channel data to use to determine a color for.
     * @param full Pass <code>true</code> to check emission and excitation,
     * 			   <code>false</code> to only check emission.
     * @return See above.
     */
    private static boolean hasEmissionExcitationData(ChannelData channelData,
		boolean full)
    {
	LogicalChannel lc = channelData.getLogicalChannel();
	if (lc == null) return false;
	if (lc.getEmissionWave() != null) return true;

	List<Filter> filters = channelData.getLightPathEmissionFilters();
	Iterator<Filter> i;
	if (filters != null) {
		i = filters.iterator();
		while (i.hasNext()) {
			if (isFilterHasEmissionData(i.next()))
			return true;
			}
	}
	if (channelData.getFilterSet() != null) {
		Filter f = channelData.getFilterSetEmissionFilter();
		if (isFilterHasEmissionData(f)) return true;
	}

	if (!full) return false;
	//Excitation
	//Laser
	if (channelData.getLightSource() != null) {
		LightSource src = channelData.getLightSource();
		if (src instanceof Laser) {
			Laser laser = (Laser) src;
			if (laser.getWavelength() != null) return true;
		}
	}
	if (lc.getExcitationWave() != null) return true;
	filters = channelData.getLightPathExcitationFilters();
	if (filters != null) {
		i = filters.iterator();
		while (i.hasNext()) {
			if (isFilterHasEmissionData(i.next()))
			return true;
			}
	}
	if (channelData.getFilterSet() != null) {
		Filter f = channelData.getFilterSetExcitationFilter();
		if (isFilterHasEmissionData(f)) return true;
	}

	return false;
    }

    /**
     * Returns the range of the wavelength or <code>null</code>.
     *
     * @param filter   The filter to handle.
     * @param emission Passed <code>true</code> to indicate that the filter is
     * 				   an emission filter, <code>false</code> otherwise.
     * @return See above.
     */
    static Length getValueFromFilter(Filter filter, boolean emission)
    {
	if (filter == null) return null;
	TransmittanceRange transmittance = filter.getTransmittanceRange();
	if (transmittance == null) return null;
	Length cutIn = transmittance.getCutIn();

	if (emission) {
		if (cutIn == null) return null;
		return makeLength(cutIn.getValue()+RANGE, cutIn.getUnit());
	}
	Length cutOut = transmittance.getCutOut();
	if (cutOut == null) return null;
	if (cutIn == null || cutIn.getValue() == 0)
	    cutIn = makeLength(cutOut.getValue()-2*RANGE, cutOut.getUnit());
	// FIXME: are these in the same unit?
	Length v = makeLength((cutIn.getValue()+cutOut.getValue())/2, cutIn.getUnit());
	if (v.getValue() < 0) return makeLength(0.0, ome.formats.model.UnitsFactory.TransmittanceRange_CutIn);
	return v;
    }

    /**
     * Determines the color usually associated to the specified
     * wavelength or explicitly defined for a particular channel.
     *
     * @param channelData Channel data to use to determine a color for.
     */
    public static int[] getColor(ChannelData channelData) {
	LogicalChannel lc = channelData.getLogicalChannel();
	Channel channel = channelData.getChannel();
	if (lc == null) return null;
	if (!hasEmissionExcitationData(channelData, true)) {
		Integer red = getValue(channel.getRed());
            Integer green = getValue(channel.getGreen());
            Integer blue = getValue(channel.getBlue());
            Integer alpha = getValue(channel.getAlpha());
            if (red != null && green != null && blue != null && alpha != null) {
		// We've got a color image of some type that has explicitly
		// specified which channel is Red, Green, Blue or some other wacky
		// color.
		//if (red == 0 && green == 0 && blue == 0 && alpha == 0)
		//	alpha = DEFAULT_ALPHA;
                return new int[] { red, green, blue, alpha };
            }
            // XXX: Is commenting this out right?
            //return null;
      }

      Length valueWavelength = lc.getEmissionWave();
      //First we check the emission wavelength.
        if (valueWavelength != null) return determineColor(valueWavelength);

      Length valueFilter = null;
      //First check the emission filter.
      //First check if filter
      //light path first
      List<Filter> filters = channelData.getLightPathEmissionFilters();
      Iterator<Filter> i;
      if (filters != null) {
		i = filters.iterator();
		while (valueFilter == null && i.hasNext()) {
				valueFilter = getValueFromFilter(i.next(), true);
			}
      }

	if (valueFilter == null)
		valueFilter = getValueFromFilter(
				channelData.getFilterSetEmissionFilter(), true);

	//Laser
	if (valueWavelength == null && valueFilter == null && channelData.getLightSource() != null) {
		LightSource ls = channelData.getLightSource();
		if (ls instanceof Laser) {
			valueWavelength = ((Laser) ls).getWavelength();
		}
	}
	if (valueWavelength != null) return determineColor(valueWavelength);

	//Excitation
	valueWavelength = lc.getExcitationWave();
	if (valueWavelength != null) return determineColor(valueWavelength);

	if (valueFilter == null) {
		filters = channelData.getLightPathExcitationFilters();
		if (filters != null) {
		i = filters.iterator();
		while (valueFilter == null && i.hasNext()) {
				valueFilter = getValueFromFilter(i.next(), false);
			}
            }
	}
	if (valueFilter == null)
		valueFilter = getValueFromFilter(
				channelData.getFilterSetExcitationFilter(), false);

	int[] toReturn = determineColor(valueFilter);
	if (toReturn != null)
	{
		return toReturn;
	}
	switch (channelData.getChannelIndex()) {
		case 0: return newRedColor();
		case 1: return newGreenColor();
		default: return newBlueColor();
		/*
		case 1: return newBlueColor();
		default: return newGreenColor();
		*/
	}

    }

    /**
     * Determines the color corresponding to the passed value.
     *
     * @param value The value to handle.
     */
    public static int[] determineColor(Length value)
    {
	if (value == null) return null;
	if (rangeBlue(value.getValue())) return newBlueColor();
	if (rangeGreen(value.getValue())) return newGreenColor();
	if (rangeRed(value.getValue())) return newRedColor();
	return null;
    }

    /**
     * Returns <code>true</code> if the channel has emission metadata,
     * <code>false</code> otherwise.
     *
     * @param channelData Channel data to use to determine a color for.
     * @return See above.
     */
    public static boolean hasEmissionData(ChannelData channelData)
    {
	return hasEmissionExcitationData(channelData, false);
    }

    /**
     * Creates a new <i>Red</i> Color object.
     *
     * @return An RGBA array representation of the color Red.
     */
    public static int[] newRedColor() {
        return new int[] { 255, 0, 0, DEFAULT_ALPHA };
    }

    /**
     * Creates a new <i>Green</i> Color object.
     *
     * @return An RGBA array representation of the color Green.
     */
    public static int[] newGreenColor() {
        return new int[] { 0, 255, 0, DEFAULT_ALPHA };
    }

    /**
     * Creates a new <i>Blue</i> Color object.
     *
     * @return An RGBA array representation of the color Blue.
     */
    public static int[] newBlueColor() {
        return new int[] { 0, 0, 255, DEFAULT_ALPHA };
    }

    /**
     * Creates a new <i>Grey</i> Color object.
     *
     * @return An RGBA array representation of the color Blue.
     */
    public static int[] newGreyColor() {
        return new int[] { 128, 128, 128, DEFAULT_ALPHA };
    }

    /**
     * Creates a new <i>White</i> Color object.
     *
     * @return An RGBA array representation of the color Blue.
     */
    public static int[] newWhiteColor() {
        return new int[] { 255, 255, 255, DEFAULT_ALPHA };
    }

}
