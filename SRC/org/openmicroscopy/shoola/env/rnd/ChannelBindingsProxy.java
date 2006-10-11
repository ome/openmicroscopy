/*
 * org.openmicroscopy.shoola.env.rnd.ChannelBindingsProxy
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

package org.openmicroscopy.shoola.env.rnd;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class ChannelBindingsProxy
{

    /** The lower bound of the pixel intensity interval. */
    private double      inputStart;
    
    /** The upper bound of the pixel intensity interval. */
    private double      inputEnd;
    
    /** Color associated to the wavelength. */
    private int[]       rgba;
    
    /** Flag indicating if the channel is mapped or not. */
    private boolean     active;
    
    /** Identifies a curve in the family. */
    public double       curveCoefficient;
    
    /** The selected family. */
    private String      family;
    
    /** Flag to indicate if the noise reduction is turned on or off. */
    private boolean     noiseReduction;

    /** Creates a new instance. */
    ChannelBindingsProxy()
    {
        rgba = new int[4];
    }
    
    /**
     * Returns <code>true</code> if the channel is mapped, <code>false</code>
     * otherwise.
     * 
     * @return See above. 
     */
    boolean isActive() { return active; }

    /**
     * Sets to <code>true</code> to map the channel, to <code>false</code>
     * otherwise.
     * 
     * @param active The value to set.
     */
    void setActive(boolean active) { this.active = active; }

    /**
     * Returns the selected family.
     * 
     * @return See above. 
     */
    String getFamily() { return family; }
    
    /**
     * Returns the upper bound of the pixels intensity interval.
     * 
     * @return See above. 
     */
    double getInputEnd() { return inputEnd; }
    
    /**
     * Returns the lower bound of the pixels intensity interval.
     * 
     * @return See above. 
     */
    double getInputStart() { return inputStart; }

    /**
     * Sets the bounds of the pixels intensity interval.
     * 
     * @param inputStart The lower bound of the interval.
     * @param inputEnd   The upper bound of the interval.
     */
    void setInterval(double inputStart, double inputEnd)
    { 
        this.inputStart = inputStart;
        this.inputEnd = inputEnd;
    }

    /**
     * Returns the color associated to the channel.
     * 
     * @return See above. 
     */
    int[] getRGBA() { return rgba; }

    /**
     * Sets the color associated to the channel.
     * 
     * @param r The red component.
     * @param g The green component.
     * @param b The blue component.
     * @param a The alpha component.
     */
    void setRGBA(int r, int g, int b, int a)
    { 
        rgba[0] = r;
        rgba[1] = g;
        rgba[2] = b;
        rgba[3] = a;
    }

    /**
     * Sets the color associated to the channel.
     * 
     * @param rgba The rgba array.
     */
    void setRGBA(int[] rgba) {  setRGBA(rgba[0], rgba[1], rgba[2], rgba[3]); }
    
    /**
     * Returns the curve coefficient.
     * 
     * @return See above. 
     */
    double getCurveCoefficient() { return curveCoefficient; }

    /**
     * Returns <code>true</code> if the noise reduction algorithm is turned on,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean isNoiseReduction() { return noiseReduction; }
    
    /**
     * Sets the values needed to perform the quantization process.
     * 
     * @param f     The family.
     * @param coeff The curve coefficient.
     * @param nr    Pass <code>true</code> to urn on the noise reduction 
     *              algorithm, <code>false</code> to turn it off.
     */
    void setQuantization(String f, double coeff, boolean nr)
    {
        family = f;
        curveCoefficient = coeff;
        noiseReduction = nr;
    }
    
}
