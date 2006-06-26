/*
 * omeis.providers.re.ColorsFactory
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package omeis.providers.re;


//Java imports

//Third-party libraries

//Application-internal dependencies
import ome.model.core.Channel;
import ome.model.display.Color;

/** 
 * Utility class to determine the color usually associated to a specified
 * channel depending on its emission wavelength.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: 1.6 $ $Date: 2005/07/05 16:13:52 $)
 * </small>
 * @since OME2.2
 */
public class ColorsFactory
{
    /** The Default value for the alpha component. */
    static final int            DEFAULT_ALPHA = 255;
    
    /**
     * Lower bound of the emission wavelenght interval corresponding to a
     * <code>BLUE</code> color. 
     */
    private static final int    BLUE_MIN = 400;
    
    /**
     * Upper bound of the emission wavelenght interval corresponding to a
     * <code>BLUE</code> color. 
     */
    private static final int    BLUE_MAX = 500;
    
    /**
     * Lower bound of the emission wavelenght interval corresponding to a
     * <code>GREEN</code> color. 
     */
    private static final int    GREEN_MIN = 501;
    
    /**
     * Upper bound of the emission wavelenght interval corresponding to a
     * <code>GREEN</code> color. 
     */
    private static final int    GREEN_MAX = 600;
    
    /**
     * Lower bound of the emission wavelenght interval corresponding to a
     * <code>RED</code> color. 
     */
    private static final int    RED_MIN = 601;
    
    /**
     * Upper bound of the emission wavelenght interval corresponding to a
     * <code>RED</code> color. 
     */
    private static final int    RED_MAX = 700;
    
    /** The RGBA-array corresponding to the <code>RED</code> color. */
    private static final Color  RED_COLOR = new Color();
    
    /** The RGBA-array corresponding to the <code>GREEN</code> color. */
    private static final Color  GREEN_COLOR = new Color();
    
    /** The RGBA-array corresponding to the <code>BLUE</code> color. */
    private static final Color  BLUE_COLOR = new Color();
    
    /** Initializes the RGB-arrays. */
    static {
        RED_COLOR.setRed(255);
        RED_COLOR.setGreen(0);
        RED_COLOR.setBlue(0);
        RED_COLOR.setAlpha(DEFAULT_ALPHA);
        GREEN_COLOR.setRed(0);
        GREEN_COLOR.setGreen(255);
        GREEN_COLOR.setBlue(0);
        GREEN_COLOR.setAlpha(DEFAULT_ALPHA);
        BLUE_COLOR.setRed(0);
        BLUE_COLOR.setGreen(0);
        BLUE_COLOR.setBlue(255);
        BLUE_COLOR.setAlpha(DEFAULT_ALPHA);
    }
    
    /**
     * Returns <code>true</code> if the emission wavelength is in 
     * the blue color band, <code>false</code> otherwise.
     * 
     * @param emWavelenght  The value of the emission wavelength.
     * @return See above.
     */
    private static boolean rangeBlue(int emWavelenght)
    {
        return (emWavelenght <= BLUE_MAX && emWavelenght >= BLUE_MIN);
    }
    
    /**
     * Returns <code>true</code> if the emission wavelength is in 
     * the green color band, <code>false</code> otherwise.
     * 
     * @param emWave  The value of the emission wavelength.
     * @return See above.
     */
    private static boolean rangeGreen(int emWave)
    {
        return(emWave >= GREEN_MIN && emWave <= GREEN_MAX);
    }
    
    /**
     * Returns <code>true</code> if the emission wavelength is in 
     * the red color band, <code>false</code> otherwise.
     * 
     * @param emWave  The value of the emission wavelength.
     * @return See above.
     */
    private static boolean rangeRed(int emWave)
    {
        return (emWave >= RED_MIN && emWave <= RED_MAX);
    }
    
    /**
     * Determines the color usually associated to the specified 
     * emission wavelength.
     * 
     * @param channel The channel to determine the color for.
     * @return A color.
     */
    private static Color getColor(Channel channel)
    {
    	int emWave = channel.getLogicalChannel().getEmissionWave().intValue();
        if (rangeBlue(emWave)) return BLUE_COLOR;
        if (rangeGreen(emWave)) return GREEN_COLOR;
        if (rangeRed(emWave)) return RED_COLOR;
        return null;
    }
    
    /**
     * Determines the color usually associated to the specified 
     * emission wavelength.
     * 
     * @param index     The channel index.
     * @param channel   The channel to determine the color for.
     * @return  A color.
     */
    public static Color getColor(int index, Channel channel)
    {
        Color c = ColorsFactory.getColor(channel);
        if (c != null) return c;
        switch (index) {
        	case  0: return BLUE_COLOR;
        	case  1: return GREEN_COLOR;
        	default: return RED_COLOR;
        }
    }
    
}
