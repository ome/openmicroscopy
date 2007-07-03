/*
 * org.openmicroscopy.shoola.env.rnd.metadata.ChannelMetadata
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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

package org.openmicroscopy.shoola.env.data.model;




//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Data Object holding information about the channel.
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
public class ChannelMetadata
{

    /** The OME index of the channel. */
    private final int	index;
    
    /** The emission wavelength of the channel. */
    private int			emissionWavelength;			
    
    /** The excitation wavelength of the channel. */
    private int			excitationWavelength;
    
    /** The pinhole size. */
    private int			pinHoleSize;
    
    /** The ND filter. */
    private float		ndFilter;
    
    /** The global minimum of the channel i.e. the minimum of all minima. */
    private double		globalMin;
    
    /** The global maximum of the channel i.e. the minimum of all minima. */
    private double		globalMax;
    
    /** 
     * Sets the emission wavelength.
     * 
     * @param emissionWavelength The value to set.
     */
    void setEmissionWavelength(int emissionWavelength)
    {
    	this.emissionWavelength = emissionWavelength;
    }
    
    /** 
     * Sets the excitation wavelength.
     * 
     * @param excitationWavelength The value to set.
     */
    void setExcitationWavelength(int excitationWavelength)
    {
    	this.excitationWavelength = excitationWavelength;
    }
    
    /** 
     * Sets the pin hole size.
     * 
     * @param pinHoleSize The value to set.
     */
    void setPinHoleSize(int pinHoleSize) { this.pinHoleSize = pinHoleSize; }
    
    /** 
     * Sets the filter.
     * 
     * @param ndFilter The value to set.
     */
    void setNDFilter(float ndFilter) { this.ndFilter = ndFilter; }
    
    /**
     * Creates a new instance.
     * 
     * @param index     The index of the channel.
     * @param globalMin	The minimum of all minima.
     * @param globalMax	The maximum of all maxima.
     */
    public ChannelMetadata(final int index, double globalMin, double globalMax)
    {
        if (index < 0)
        	throw new IllegalArgumentException("Channel index cannot < 0.");
        if (globalMin > globalMax)
        	throw new IllegalArgumentException("Min cannot less than Max");
        this.index = index;
        this.globalMin = globalMin;
        this.globalMax = globalMax;
        emissionWavelength = index;
        excitationWavelength = -1;
    }
    
    /**
     * Returns the emission wavelength of the channel.
     * 
     * @return See above
     */
    public int getEmissionWavelength() { return emissionWavelength; }
    
    /**
     * Returns the excitation wavelength of the channel.
     * 
     * @return See above
     */
    public int getExcitationWavelength()
    { 
    	if (excitationWavelength < 0) return getEmissionWavelength();
    	return excitationWavelength; 
    }
    
    /**
     * Returns the excitation wavelength of the channel.
     * 
     * @return See above
     */
    public int getPinholeSize() { return pinHoleSize; }
    
    /**
     * Returns the excitation wavelength of the channel.
     * 
     * @return See above
     */
    public float getNDFilter() { return ndFilter; }
    
    /** 
     * Returns the global minimum of the channel i.e. the minimum of all minima.
     * 
     * @return See above.
     */
    public double getGlobalMin() { return globalMin; }
    
    /** 
     * Returns the global maximum of the channel i.e. the maximum of all maxima.
     * 
     * @return See above.
     */
    public double getGlobalMax() { return globalMax; }
    
    /**
     * Returns the channel's index.
     * 
     * @return See above.
     */
    public int getIndex() { return index; }
}
