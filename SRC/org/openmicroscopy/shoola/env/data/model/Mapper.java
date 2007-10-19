/*
 * org.openmicroscopy.shoola.env.data.model.Mapper 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
import ome.model.core.Channel;
import ome.model.core.LogicalChannel;
import ome.model.stats.StatsInfo;

//Third-party libraries

//Application-internal dependencies

/** 
 * Helper class used to map core object to 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class Mapper
{

	/**
	 * Turns the specified {@link Channel} object into {@link ChannelMetadata}
	 * object.
	 * 
	 * @param index		The index of the channel.	
	 * @param channel	The channel to transform.
	 * @return See above.
	 */
	public static ChannelMetadata mapChannel(int index, Channel channel)
	{
		StatsInfo stats = channel.getStatsInfo();
		double min = 0;
		double max = 1;
		if (stats != null) {
			min = stats.getGlobalMin().doubleValue();
			max = stats.getGlobalMax().doubleValue();
		}
		ChannelMetadata cm = new ChannelMetadata(index, min, max);
		LogicalChannel lc = channel.getLogicalChannel();
		Integer w = lc.getEmissionWave();
		if (w != null) cm.setEmissionWavelength(w.intValue());
		w = lc.getExcitationWave();
		if (w != null) cm.setExcitationWavelength(w.intValue());
		w = lc.getPinHoleSize();
		if (w != null) cm.setPinHoleSize(w.intValue());
		Float nd = lc.getNdFilter();
		if (nd != null) cm.setNDFilter(nd.floatValue());
		return cm;
	}
	
}
