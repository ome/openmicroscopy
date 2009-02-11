/*
 * omeis.providers.re.ColorsFactory
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omeis.providers.re;

// Java imports

// Third-party libraries

// Application-internal dependencies
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
     * Lower bound of the emission wavelength interval corresponding to a
     * <code>BLUE</code> color.
     */
    private static final int BLUE_MIN = 400;

    /**
     * Upper bound of the emission wavelength interval corresponding to a
     * <code>BLUE</code> color.
     */
    private static final int BLUE_MAX = 500;

    /**
     * Lower bound of the emission wavelength interval corresponding to a
     * <code>GREEN</code> color.
     */
    private static final int GREEN_MIN = 501;

    /**
     * Upper bound of the emission wavelength interval corresponding to a
     * <code>GREEN</code> color.
     */
    private static final int GREEN_MAX = 600;

    /**
     * Lower bound of the emission wavelength interval corresponding to a
     * <code>RED</code> color.
     */
    private static final int RED_MIN = 601;

    /**
     * Upper bound of the emission wavelength interval corresponding to a
     * <code>RED</code> color.
     */
    private static final int RED_MAX = 700;

    /**
     * Returns <code>true</code> if the emission wavelength is in the blue
     * color band, <code>false</code> otherwise.
     * 
     * @param emWavelenght
     *            The value of the emission wavelength.
     * @return See above.
     */
    private static boolean rangeBlue(int emWavelenght) {
        return emWavelenght <= BLUE_MAX && emWavelenght >= BLUE_MIN;
    }

    /**
     * Returns <code>true</code> if the emission wavelength is in the green
     * color band, <code>false</code> otherwise.
     * 
     * @param emWave
     *            The value of the emission wavelength.
     * @return See above.
     */
    private static boolean rangeGreen(int emWave) {
        return emWave >= GREEN_MIN && emWave <= GREEN_MAX;
    }

    /**
     * Returns <code>true</code> if the emission wavelength is in the red
     * color band, <code>false</code> otherwise.
     * 
     * @param emWave
     *            The value of the emission wavelength.
     * @return See above.
     */
    private static boolean rangeRed(int emWave) {
        return emWave >= RED_MIN && emWave <= RED_MAX;
    }

    /**
     * Determines the color usually associated to the specified emission
     * wavelength or explicitly defined for a particular channel.
     * 
     * @param channel The channel to determine the color for.
     * @return An RGBA array representation of the color.
     */
    private static int[] getColor(Channel channel) {
    	LogicalChannel lc = channel.getLogicalChannel();
    	if (lc == null) return null;
        Integer emWave = lc.getEmissionWave();
        
        Integer red = channel.getRed();
        Integer green = channel.getGreen();
        Integer blue = channel.getBlue();
        Integer alpha = channel.getAlpha();
        if (red != null && green != null && blue != null && alpha != null) {
        	// We've got a color image of some type that has explicitly
        	// specified which channel is Red, Green, Blue or some other wacky
        	// color.
            return new int[] { red, green, blue, alpha };
        }

        if (emWave == null)
        {
            return null;
        }
        if (rangeBlue(emWave))
        {
            return newBlueColor();
        }
        if (rangeGreen(emWave))
        {
            return newGreenColor();
        }
        if (rangeRed(emWave))
        {
            return newRedColor();
        }
        return null;
    }

    /**
     * Determines the color usually associated to the specified emission
     * wavelength.
     * 
     * @param index
     *            The channel index.
     * @param channel
     *            The channel to determine the color for.
     * @return A color.
     */
    public static int[] getColor(int index, Channel channel) {
        int[] c = ColorsFactory.getColor(channel);
        if (c != null) {
            return c;
        }
        switch (index) {
            case 0:
            	return newRedColor();
            case 1:
            	return newBlueColor();
            default:
            	return newGreenColor();
        }
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
}
