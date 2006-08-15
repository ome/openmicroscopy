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
import ome.model.core.LogicalChannel;

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
    
    public ChannelMetadata(int index, Channel channel, PixelsStatsEntry[] stats)
    {
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
        return 400;//channel.getLogicalChannel().getEmissionWave().intValue();
    }
    
    /** 
     * Returns the global minimum of the channel i.e. the minimum of all minima.
     * 
     * @return See above.
     */
    public int getGlobalMin()
    {
        return 0;//channel.getStatsInfo().getGlobalMin().intValue();
    }
    
    /** 
     * Returns the global maximum of the channel i.e. the minimum of all minima.
     * 
     * @return See above.
     */
    public int getGlobalMax()
    {
        return 700;//channel.getStatsInfo().getGlobalMax().intValue();
    }
    
    /**
     * Returns the array of {@link PixelsStatsEntry} objects, indexed by 
     * timepoint.
     * 
     * @return See above
     */
    public PixelsStatsEntry[] getStatsEntry() { return stats; }
    
}
