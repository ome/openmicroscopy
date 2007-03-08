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
import ome.model.display.Color;
import ome.model.enums.PhotometricInterpretation;

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
    /** The Default value for the alpha component. */
    static final int DEFAULT_ALPHA = 255;

    /**
     * Lower bound of the emission wavelenght interval corresponding to a
     * <code>BLUE</code> color.
     */
    private static final int BLUE_MIN = 400;

    /**
     * Upper bound of the emission wavelenght interval corresponding to a
     * <code>BLUE</code> color.
     */
    private static final int BLUE_MAX = 500;

    /**
     * Lower bound of the emission wavelenght interval corresponding to a
     * <code>GREEN</code> color.
     */
    private static final int GREEN_MIN = 501;

    /**
     * Upper bound of the emission wavelenght interval corresponding to a
     * <code>GREEN</code> color.
     */
    private static final int GREEN_MAX = 600;

    /**
     * Lower bound of the emission wavelenght interval corresponding to a
     * <code>RED</code> color.
     */
    private static final int RED_MIN = 601;

    /**
     * Upper bound of the emission wavelenght interval corresponding to a
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
     * @return A color.
     */
    private static Color getColor(Channel channel) {
    	LogicalChannel lc = channel.getLogicalChannel();
        Integer emWave = lc.getEmissionWave();
        Color explicitColor = channel.getColorComponent();
        String pi = null;
        if (lc.getPhotometricInterpretation() != null)
        	pi = lc.getPhotometricInterpretation().getValue();
        
        // Handle the pixel data as a set of monochrome (greyscale) channels.
        if (pi != null && pi.equals(Renderer.PHOTOMETRIC_MONOCHROME))
        {
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
        }
        else if (explicitColor != null)
        {
        	// We've got a color image of some type that has explicitly
        	// specified which channel is Red, Green, Blue or some other wacky
        	// color.
        	Color c = new Color();
        	c.setRed(explicitColor.getRed());
        	c.setGreen(explicitColor.getGreen());
        	c.setBlue(explicitColor.getBlue());
        	c.setAlpha(explicitColor.getAlpha());
        	return c;
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
    public static Color getColor(int index, Channel channel) {
        Color c = ColorsFactory.getColor(channel);
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
     * @return a color object.
     */
    public static Color newRedColor() {
        Color c = new Color();
        c.setRed(255);
        c.setGreen(0);
        c.setBlue(0);
        c.setAlpha(DEFAULT_ALPHA);
        return c;
    }

    /**
     * Creates a new <i>Green</i> Color object.
     * 
     * @return a color object.
     */
    public static Color newGreenColor() {
        Color c = new Color();
        c.setRed(0);
        c.setGreen(255);
        c.setBlue(0);
        c.setAlpha(DEFAULT_ALPHA);
        return c;
    }

    /**
     * Creates a new <i>Blue</i> Color object.
     * 
     * @return a color object.
     */
    public static Color newBlueColor() {
        Color c = new Color();
        c.setRed(0);
        c.setGreen(0);
        c.setBlue(255);
        c.setAlpha(DEFAULT_ALPHA);
        return c;
    }
}
