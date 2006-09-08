/*
 * org.openmicroscopy.shoola.env.rnd.metadata.ChannelMetadata
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

package org.openmicroscopy.shoola.env.rnd.metadata;




//Java imports

//Third-party libraries

//Application-internal dependencies
import ome.model.core.Channel;

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
public class ChannelMetadata
{

    /** The OME index of the channel. */
    private final int               index;
    
    private final Channel           channel;
    
    private PixelsStatsEntry[]      stats;
    
    /**
     * Creates a new instance.
     * 
     * @param index     The index of the channel.
     * @param channel
     * @param stats
     */
    public ChannelMetadata(final int index, final Channel channel,
                        PixelsStatsEntry[] stats)
    {
        if (channel == null) 
            throw new IllegalArgumentException("Channel cannot be null.");
        this.index = index;
        this.channel = channel;
        this.stats = stats;
    }
    
    /**
     * Returns the emission wavelength of the channel.
     * 
     * @return See above
     */
    public int getEmissionWavelength()
    {
        Integer wave = channel.getLogicalChannel().getEmissionWave();
        if (wave == null) return index;
        return wave.intValue();
    }
    
    /**
     * Returns the excitation wavelength of the channel.
     * 
     * @return See above
     */
    public int getExcitationWavelength()
    {
        Integer wave = channel.getLogicalChannel().getExcitationWave();
        if (wave == null) return getEmissionWavelength();
        return wave.intValue();
    }
    
    /**
     * Returns the excitation wavelength of the channel.
     * 
     * @return See above
     */
    public int getPinholeSize()
    {
        Integer v = channel.getLogicalChannel().getPinHoleSize();
        if (v == null) return 0;
        return v.intValue();
    }
    
    /**
     * Returns the excitation wavelength of the channel.
     * 
     * @return See above
     */
    public float getNDFilter()
    {
        Float v = channel.getLogicalChannel().getNdFilter();
        if (v == null) return 0;
        return v.floatValue();
    }
    
    /** 
     * Returns the global minimum of the channel i.e. the minimum of all minima.
     * 
     * @return See above.
     */
    public double getGlobalMin()
    {
        return channel.getStatsInfo().getGlobalMin().doubleValue();
    }
    
    /** 
     * Returns the global maximum of the channel i.e. the maximum of all maxima.
     * 
     * @return See above.
     */
    public double getGlobalMax()
    {
        return channel.getStatsInfo().getGlobalMax().doubleValue();
    }
    
    /**
     * Returns the array of {@link PixelsStatsEntry} objects, indexed by 
     * timepoint.
     * 
     * @return See above
     */
    public PixelsStatsEntry[] getStatsEntry() { return stats; }
    
}
