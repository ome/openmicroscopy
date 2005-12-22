/*
 * omeis.providers.re.metadata.PixelsStats
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

package omeis.providers.re.metadata;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Collects some statistics calculated on a given pixels set.
 *
 * @see PixZStatsEntry
 * @see PixTStatsEntry
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision: 1.2 $ $Date: 2005/06/09 17:04:26 $)
 * </small>
 * @since OME2.2
 */
public class PixelsStats
{
	
    /** A <code>[c, t]</code> array to hold stack statistics. */
	private PixZStatsEntry[][] stats;
	
	/** A <code>[t]</code> array to hold global stack statistics. */
    private PixTStatsEntry[]   globalStats;
    
    
    /**
     * Creates a new instance.
     * 
     * @param sizeC Number of wavelengths (channels) in the pixels set.
     * @param sizeT Number of timepoints in the pixels set.
     */
	PixelsStats(int sizeC, int sizeT)
	{
		stats = new PixZStatsEntry[sizeC][sizeT];
		globalStats = new PixTStatsEntry[sizeC];
	}

    /**
     * Returns the stats relative to the stack at timepoint <code>t</code>
     * and wavelength <code>c</code>.
     * 
     * @param c The wavelength (channel) index.
     * @param t The timepoint index.
     * @return The stats entry.
     */
	public PixZStatsEntry getEntry(int c, int t) { return stats[c][t]; }

    /**
     * Sets the stats entry relative to the stack at timepoint <code>t</code>
     * and wavelength <code>c</code>.
     * 
     * @param c The wavelength (channel) index.
     * @param t The timepoint index.
     * @param min Minimum pixel intensity of the stack.
     * @param max Maximum pixel intensity of the stack.
     */
	void setEntry(int c, int t, double min, double max)
	{
		stats[c][t] = new PixZStatsEntry(min, max);
	}

    /**
     * Sets the stats entry relative to the stack at wavelength <code>c</code>.
     * 
     * @param c The wavelength (channel) index.
     * @param globalMin Minimum pixel intensity of <i>all</i> the stacks at
     *                  wavelength <code>w</code>.
     * @param globalMax Maximum pixel intensity of <i>all</i> the stacks at
     *                  wavelength <code>w</code>.
     */
	void setGlobalEntry(int c, double globalMin, double globalMax)
	{
		globalStats[c] = new PixTStatsEntry(globalMin, globalMax);
	}
		
    /**
     * Returns the stats relative to the all the stacks at wavelength 
     * <code>c</code>.
     * 
     * @param c The wavelength (channel) index.
     * @return The stats entry.
     */
	public PixTStatsEntry getGlobalEntry(int c) 
	{
		return globalStats[c];
	}
	
}
