/*
 * omeis.providers.re.ColorsFactory
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omeis.providers.re;

// Java imports
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

// Third-party libraries

// Application-internal dependencies
import ome.model.acquisition.Filter;
import ome.model.acquisition.FilterSet;
import ome.model.acquisition.Laser;
import ome.model.acquisition.LightPath;
import ome.model.acquisition.LightSource;
import ome.model.acquisition.TransmittanceRange;
import ome.model.core.Channel;
import ome.model.core.LogicalChannel;


/**
 * Utility class to determine the color usually associated to a specified
 * channel depending on its emission wavelength.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @version 2.2 <small> (<b>Internal version:</b> $Revision$ $Date:
 *          2005/07/05 16:13:52 $) </small>
 * @since OME2.2
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
     * Returns <code>true</code> if the channel has emission and/or excitation
     * information, <code>false</code> otherwise.
     * 
     * @param lc   The channel to handle.
     * @param full Pass <code>true</code> to check emission and excitation,
     * 			   <code>false</code> to only check emission.
     * @return See above.
     */
    private static boolean hasEmissionExcitationData(LogicalChannel lc, 
    		boolean full)
    {
    	if (lc == null) return false;
    	if (lc.getEmissionWavelength() != null) return true;
    	//Need to check the light path.
    	List<Filter> filters;
    	Iterator<Filter> j;
    	FilterSet f = null;
    	LightPath lp = null;
    	//light path first
    	if (lc.getLightPath() != null) {
    		lp = (LightPath) lc.getLightPath();
    		if (lp.sizeOfEmissionFilterLink() > 0) {
    			filters = new ArrayList<Filter>();
    			j = lp.linkedEmissionFilterIterator();
        		while (j.hasNext()) {
        			filters.add(j.next());
        		}
    			sortFilters(filters);
    			j = filters.iterator();
    			while (j.hasNext()) {
					if (isFilterHasEmissionData(j.next())) return true;
				}
    		}
    	}
    	
    	if (lc.getFilterSet() != null) {
    		f = (FilterSet) lc.getFilterSet();
    		if (f.sizeOfEmissionFilterLink() > 0) {
    			filters = new ArrayList<Filter>();
    			j = f.linkedEmissionFilterIterator();
        		while (j.hasNext()) {
        			filters.add(j.next());
        		}
    			sortFilters(filters);
    			j = filters.iterator();
    			while (j.hasNext()) {
					if (isFilterHasEmissionData(j.next())) return true;
				}
    		}
    	}

    	if (!full) return false;
    	//Excitation
    	//Laser
    	if (lc.getLightSourceSettings() != null) {
    		LightSource src = lc.getLightSourceSettings().getLightSource();
    		if (src instanceof Laser) {
    			Laser laser = (Laser) src;
    			if (laser.getWavelength() != null) return true;
    		}
    	}
    	if (lc.getExcitationWavelength() != null) return true;
    	//ligth path
    	if (lp != null) {
    		if (lp.sizeOfExcitationFilterLink() > 0) {
    			filters = new ArrayList<Filter>();
    			j = lp.linkedExcitationFilterIterator();
        		while (j.hasNext()) {
        			filters.add(j.next());
        		}
    			sortFilters(filters);
    			j = filters.iterator();
    			while (j.hasNext()) {
					if (isFilterHasEmissionData(j.next())) return true;
				}
    		}
    	}
    	//filter set
    	if (f != null) {
    		if (f.sizeOfExcitationFilterLink() > 0) {
    			filters = new ArrayList<Filter>();
    			j = f.linkedExcitationFilterIterator();
        		while (j.hasNext()) {
        			filters.add(j.next());
        		}
    			sortFilters(filters);
    			j = filters.iterator();
    			while (j.hasNext()) {
					if (isFilterHasEmissionData(j.next())) return true;
				}
    		}
    	}

    	return false;
    }
    
    /**
     * Determines the color usually associated to the specified
     * wavelength or explicitly defined for a particular channel.
     * 
     * @param channel The channel to determine the color for.
     * @param lc	  The logical channel associated to that channel.
     * @return An RGBA array representation of the color.
     */
    private static int[] getColor(Channel channel, LogicalChannel lc) {
    	if (lc == null) return null;
    	if (!hasEmissionExcitationData(lc, true)) {
    		Integer red = channel.getRed();
    		Integer green = channel.getGreen();
    		Integer blue = channel.getBlue();
    		Integer alpha = channel.getAlpha();
    		if (red != null && green != null && blue != null && alpha != null) {
    			// We've got a color image of some type that has explicitly
    			// specified which channel is Red, Green, Blue or some other wacky
    			// color.
    			//if (red == 0 && green == 0 && blue == 0 && alpha == 0)
    			//	alpha = DEFAULT_ALPHA;
    			return new int[] { red, green, blue, alpha };
    		}
    		return null;
    	}
    	Integer value = lc.getEmissionWavelength();
    	//First we check the emission wavelength.
    	if (value != null) return determineColor(value);

    	//First check the emission filter.
    	//First check if filter

    	List<Filter> filters;
    	Iterator<Filter> j;
    	FilterSet f = null;
    	LightPath lp = null;
    	//LightPath
    	if (value == null && lc.getLightPath() != null) {
    		filters = new ArrayList<Filter>();
    		lp = lc.getLightPath();
    		j = lp.linkedEmissionFilterIterator();
    		while (j.hasNext()) {
				filters.add(j.next());
			}
    		sortFilters(filters);
    		while (value == null && j.hasNext()) {
    			value = getValueFromFilter(j.next(), true);
    		}
    	}
    	
    	if (value == null && lc.getFilterSet() != null) {
    		filters = new ArrayList<Filter>();
    		f = lc.getFilterSet();
    		j = f.linkedEmissionFilterIterator();
    		while (j.hasNext()) {
				filters.add(j.next());
			}
    		sortFilters(filters);
    		while (value == null && j.hasNext()) {
    			value = getValueFromFilter(j.next(), true);
    		}
    	}
    	
    	
    	//Laser
    	if (value == null && lc.getLightSourceSettings() != null) {
    		LightSource ls = lc.getLightSourceSettings().getLightSource();
    		if (ls instanceof Laser) value = ((Laser) ls).getWavelength();
    	}
    	if (value != null) return determineColor(value);

    	//Excitation
    	value = lc.getExcitationWavelength();
    	if (value != null) return determineColor(value);

    	//light path first
    	if (value == null && lp != null) {
    		filters = new ArrayList<Filter>();
    		j = lp.linkedExcitationFilterIterator();
    		while (j.hasNext()) {
				filters.add(j.next());
			}
    		sortFilters(filters);
    		while (value == null && j.hasNext()) {
    			value = getValueFromFilter(j.next(), false);
    		}
    	}
    	
    	if (value == null && f != null) {
    		filters = new ArrayList<Filter>();
    		j = f.linkedExcitationFilterIterator();
    		while (j.hasNext()) {
				filters.add(j.next());
			}
    		sortFilters(filters);
    		while (value == null && j.hasNext()) {
    			value = getValueFromFilter(j.next(), false);
    		}
    	}
    	return determineColor(value);
    }
 
    /**
     * Determines the color corresponding to the passed value.
     * 
     * @param value The value to handle.
     * @return
     */
    private static int[] determineColor(Integer value)
    {
    	if (value == null) return null;
    	if (rangeBlue(value)) return newBlueColor();
    	if (rangeGreen(value)) return newGreenColor();
    	if (rangeRed(value)) return newRedColor();
    	return null;
    }
    
    /**
     * Returns the range of the wavelength or <code>null</code>.
     * 
     * @param filter   The filter to handle.
     * @param emission Passed <code>true</code> to indicate that the filter is 
     * 				   an emission filter, <code>false</code> otherwise.
     * @return See above.
     */
    private static Integer getValueFromFilter(Filter filter, boolean emission)
    {
    	if (filter == null) return null;
    	TransmittanceRange transmittance = filter.getTransmittanceRange();
    	if (transmittance == null) return null;
    	Integer cutIn = transmittance.getCutIn();
    	
    	if (emission) {
    		if (cutIn == null) return null;
        	return cutIn+RANGE;
    	}
    	Integer cutOut = transmittance.getCutOut();
    	if (cutOut == null) return null;
    	if (cutIn == null || cutIn == 0) cutIn = cutOut-2*RANGE;
    	Integer v = (cutIn+cutOut)/2;
    	if (v < 0) return 0;
    	return v;
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
     * Sorts the filters by ID to make sure that the filters with the 
     * highest ID is first picked.
     * 
     * @param filters the filters to handle.
     */
    private static void sortFilters(List<Filter> filters)
    {
    	if (filters == null || filters.size() == 0) return;
        Comparator c = new Comparator() {
            public int compare(Object o1, Object o2)
            {
                long id1 = ((Filter) o1).getId(),
                id2 = ((Filter) o2).getId();
                int v = 0;
                if (id1 < id2) v = -1;
                else if (id1 > id2) v = 1;
                return v;
            }
        };
        Collections.sort(filters, c);
    }
    
    /**
     * Determines the color usually associated to the specified wavelength.
     * 
     * @param index The channel index.
     * @param channel The channel to determine the color for.
     * @return A color.
     */
    public static int[] getColor(int index, Channel channel) {
    	return getColor(index, channel, channel.getLogicalChannel());
    }

    /**
     * Determines the color usually associated to the specified wavelength.
     * 
     * @param index The channel index.
     * @param channel The channel to determine the color for.
     * @param lc The entity hosting information about the emission etc.
     * @return A color.
     */
    public static int[] getColor(int index, Channel channel, LogicalChannel
    		lc) {
    	if (lc == null) lc = channel.getLogicalChannel();
        int[] c = ColorsFactory.getColor(channel, lc);
        if (c != null) return c;
        switch (index) {
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
     * Returns <code>true</code> if the channel has emission metadata,
     * <code>false</code> otherwise.
     * 
     * @param lc The channel to handle.
     * @return See above.
     */
    public static boolean hasEmissionData(LogicalChannel lc)
    {
    	return hasEmissionExcitationData(lc, false);
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
