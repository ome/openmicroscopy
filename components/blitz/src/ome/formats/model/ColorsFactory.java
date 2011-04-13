/*
 * ome.formats.model.ColorsFactory
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.formats.model;

// Java imports
import java.util.Iterator;
import java.util.List;
// Third-party libraries

// Application-internal dependencies
import omero.RInt;
import omero.model.Channel;
import omero.model.Filter;
import omero.model.Laser;
import omero.model.LightSource;
import omero.model.LogicalChannel;
import omero.model.TransmittanceRange;


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
     * <code>BLUE</code> color.
     */
    private static final int BLUE_MAX = 500;

    /**
     * Lower bound of the wavelength interval corresponding to a
     * <code>GREEN</code> color.
     */
    private static final int GREEN_MIN = 501;

    /**
     * Upper bound of the wavelength interval corresponding to a
     * <code>GREEN</code> color.
     */
    private static final int GREEN_MAX = 559;//600;

    /**
     * Lower bound of the wavelength interval corresponding to a
     * <code>RED</code> color.
     */
    private static final int RED_MIN = 560;//601;

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
    private static boolean rangeBlue(int wavelength) {
        return wavelength <= BLUE_MAX;// && wavelength >= BLUE_MIN;
    }

    /**
     * Returns <code>true</code> if the wavelength is in the green
     * color band, <code>false</code> otherwise.
     *
     * @param wavelength The wavelength to handle.
     * @return See above.
     */
    private static boolean rangeGreen(int wavelength) {
        return wavelength >= GREEN_MIN && wavelength <= GREEN_MAX;
    }

    /**
     * Returns <code>true</code> if the wavelength is in the red
     * color band, <code>false</code> otherwise.
     *
     * @param wavelength The wavelength to handle.
     * @return See above.
     */
    private static boolean rangeRed(int wavelength) {
        return wavelength >= RED_MIN;//&& wavelength <= RED_MAX;
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
    static Integer getValueFromFilter(Filter filter, boolean emission)
    {
	if (filter == null) return null;
	TransmittanceRange transmittance = filter.getTransmittanceRange();
	if (transmittance == null) return null;
	Integer cutIn = getValue(transmittance.getCutIn());

	if (emission) {
		if (cutIn == null) return null;
		return cutIn+RANGE;
	}
	Integer cutOut = getValue(transmittance.getCutOut());
	if (cutOut == null) return null;
	if (cutIn == null || cutIn == 0) cutIn = cutOut-2*RANGE;
	Integer v = (cutIn+cutOut)/2;
	if (v < 0) return 0;
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
	Integer value = getValue(lc.getEmissionWave());
        //First we check the emission wavelength.
        if (value != null) return determineColor(value);

        //First check the emission filter.
	//First check if filter
        //light path first
        List<Filter> filters = channelData.getLightPathEmissionFilters();
	Iterator<Filter> i;
        if (filters != null) {
		i = filters.iterator();
		while (value == null && i.hasNext()) {
				value = getValueFromFilter(i.next(), true);
			}
        }

	if (value == null)
		value = getValueFromFilter(
				channelData.getFilterSetEmissionFilter(), true);

	//Laser
	if (value == null && channelData.getLightSource() != null) {
		LightSource ls = channelData.getLightSource();
		if (ls instanceof Laser) {
			value = getValue(((Laser) ls).getWavelength());
		}
	}
	if (value != null) return determineColor(value);

	//Excitation
	value = getValue(lc.getExcitationWave());
	if (value != null) return determineColor(value);

	if (value == null) {
		filters = channelData.getLightPathExcitationFilters();
		if (filters != null) {
		i = filters.iterator();
		while (value == null && i.hasNext()) {
				value = getValueFromFilter(i.next(), false);
			}
            }
	}
	if (value == null)
		value = getValueFromFilter(
				channelData.getFilterSetExcitationFilter(), false);

	int[] toReturn = determineColor(value);
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
     * @return
     */
    public static int[] determineColor(Integer value)
    {
	if (value == null) return null;
	if (rangeBlue(value)) return newBlueColor();
	if (rangeGreen(value)) return newGreenColor();
	if (rangeRed(value)) return newRedColor();
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
