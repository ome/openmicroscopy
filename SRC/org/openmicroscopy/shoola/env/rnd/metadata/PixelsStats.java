/*
 * org.openmicroscopy.shoola.env.rnd.metadata.ImgStats
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

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class PixelsStats
{
	private PixelsStatsEntry[][] 		stats;
	private PixelsGlobalStatsEntry[] 	globalStats;
    
	PixelsStats(int sizeW, int sizeT)
	{
		stats = new PixelsStatsEntry[sizeW][sizeT];
		globalStats = new PixelsGlobalStatsEntry[sizeW];
	}

	public PixelsStatsEntry getEntry(int w, int t)
	{
		return stats[w][t];       
	}

	void setEntry(int w, int t, int min, int max)
	{
		if (globalStats[w] != null) {
			int globalMin = Math.min(globalStats[w].globalMin, min);
			int globalMax = Math.max(globalStats[w].globalMax, max);
			globalStats[w] = new PixelsGlobalStatsEntry(globalMin, globalMax);
		} else { 
			globalStats[w] = new PixelsGlobalStatsEntry(min, max);
		}
		stats[w][t] = new PixelsStatsEntry(min, max);
	}

	public PixelsGlobalStatsEntry getGlobalEntry(int w)
	{
		return globalStats[w];
	}
	
}
