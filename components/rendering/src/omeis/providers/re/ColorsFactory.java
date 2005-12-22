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
import omeis.providers.re.metadata.ChannelBindings;

/** 
 * Utility class to determine the color usually associated to a specified
 * channel w.r.t. its emission wavelength.
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
    
    /** Identifies the red value in the RGBA-array. */
    static final int            RED = 0;
    
    /** Identifies the green value in the RGBA-array. */
    static final int            GREEN = 1;
    
    /** Identifies the blue value in the RGBA-array. */
    static final int            BLUE = 2;
    
    /** Identifies the alpha value in the RGBA-array. */
    static final int            ALPHA = 3;
    
    /** Lower bound of the blue interval. */
    private static final int    BLUE_MIN = 400;
    
    /** Upper bound of the blue interval. */
    private static final int    BLUE_MAX = 500;
    
    /** Lower bound of the green interval. */
    private static final int    GREEN_MIN = 501;
    
    /** Upper bound of the green interval. */
    private static final int    GREEN_MAX = 600;
    
    /** Lower bound of the red interval. */
    private static final int    RED_MIN = 601;
    
    /** Upper bound of the red interval. */
    private static final int    RED_MAX = 700;
    
    /** RGBA-array corresponding to the RED color. */
    private static final int[]  RED_COLOR;
    
    /** RGBA-array corresponding to the GREEN color. */
    private static final int[]  GREEN_COLOR;
    
    /** RGBA-array corresponding to the BLUE color. */
    private static final int[]  BLUE_COLOR;
    
    /** Initializes RGB-arrays. */
    static {
        RED_COLOR = new int[4];
        RED_COLOR[RED] = 255;
        RED_COLOR[GREEN] = 0;
        RED_COLOR[BLUE] = 0;
        RED_COLOR[ALPHA] = ChannelBindings.DEFAULT_ALPHA;
        GREEN_COLOR = new int[4];
        GREEN_COLOR[RED] = 0;
        GREEN_COLOR[GREEN] = 255;
        GREEN_COLOR[BLUE] = 0;
        GREEN_COLOR[ALPHA] = ChannelBindings.DEFAULT_ALPHA;
        BLUE_COLOR = new int[4];
        BLUE_COLOR[RED] = 0;
        BLUE_COLOR[GREEN] = 0;
        BLUE_COLOR[BLUE] = 255;
        BLUE_COLOR[ALPHA] = ChannelBindings.DEFAULT_ALPHA;
    }
    
    /**
     * Returns <code>true</code> if the emission wavelength is in 
     * the blue color band, <code>false</code> otherwise.
     * 
     * @param emWavelenght  The value of the emission wavelength
     * @return See above.
     */
    private static boolean rangeBlue(int emWavelenght)
    {
        if (emWavelenght <= BLUE_MAX && emWavelenght >= BLUE_MIN) return true;
        return false;
    }
    
    /**
     * Returns <code>true</code> if the emission wavelength is in 
     * the green color band, <code>false</code> otherwise.
     * 
     * @param emWavelenght  The value of the emission wavelength
     * @return See above.
     */
    private static boolean rangeGreen(int emWavelenght)
    {
        if (emWavelenght >= GREEN_MIN && emWavelenght <= GREEN_MAX) 
            return true;
        return false;
    }
    
    /**
     * Returns <code>true</code> if the emission wavelength is in 
     * the red color band, <code>false</code> otherwise.
     * 
     * @param emWavelenght  The value of the emission wavelength
     * @return See above.
     */
    private static boolean rangeRed(int emWavelenght)
    {
        if (emWavelenght >= RED_MIN && emWavelenght <= RED_MAX) return true;
        return false;
    }
    
    /**
     * Determines the color usually associated to the specified 
     * emission wavelenght.
     * 
     * @param emWavelenght  The emission wavelength.
     * @return  Returns an rgb array corresponding to the color.
     */
    private static int[] getColor(int emWavelenght)
    {
        if (rangeBlue(emWavelenght)) return BLUE_COLOR;
        if (rangeGreen(emWavelenght)) return GREEN_COLOR;
        if (rangeRed(emWavelenght)) return RED_COLOR;
        return null;
    }
    
    /**
     * Determines the color usually associated to the specified 
     * emission wavelenght.
     * 
     * @param channel The channel index.
     * @param emWavelenght  The emission wavelength.
     * @return  Returns an rgb array corresponding to the color.
     */
    public static int[] getColor(int channel, int emWavelenght)
    {
        int[] c = ColorsFactory.getColor(emWavelenght);
        if (c != null) return c;
        if (channel == 0)  c = BLUE_COLOR;
        else if (channel == 1) c = GREEN_COLOR;
        else if (channel > 1) c = RED_COLOR;
        return c;
    }

}
