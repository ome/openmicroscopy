/*
 * org.openmicroscopy.shoola.env.rnd.QuantumManager
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
import org.openmicroscopy.shoola.env.rnd.defs.QuantumDef;
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsGlobalStatsEntry;
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsStats;
import org.openmicroscopy.shoola.env.rnd.quantum.QuantumFactory;
import org.openmicroscopy.shoola.env.rnd.quantum.QuantumStrategy;

/** 
 * Manages the strategy objects for each wavelength.
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
class QuantumManager
{
    /** Number of wavelengths. */
	private int						sizeW;

	/** 
	 * Contains a strategy object for each wavelength.
	 * Indexed according to the wavelength indeces in the <i>OME</i> 5D pixels
	 * file.
	 */
	private QuantumStrategy[]     	wavesStg;
    
    
    /**
     * Creates a new instance.
     * 
     * @param sizeW		The number of wavelengths.
     */
	QuantumManager(int sizeW)
	{
		this.sizeW = sizeW;
		wavesStg = new QuantumStrategy[sizeW];          
	}
    
    /**
     * Creates and configures an appropriate strategy for each wavelength.
     * The previous window interval settings of each wavelength are retained
     * by the new strategy.
     * 
     * @param qd	The quantum definition which dictates what strategy to use.
     * @param stats	For each wavelength, it contains the global minimum and
     * 				maximum of the wavelength stack across time.	
     */
	void initStrategies(QuantumDef qd, PixelsStats stats)
	{
		QuantumStrategy stg;
		PixelsGlobalStatsEntry wGlobal;
		Comparable gMin, gMax;
		for (int w = 0; w < sizeW; ++w) {
			stg = QuantumFactory.getStrategy(qd);
			wGlobal = stats.getGlobalEntry(w);
			gMin = wGlobal.getGlobalMin();
			gMax = wGlobal.getGlobalMax();
			stg.setExtent(gMin, gMax);
			if (wavesStg[w] == null)	stg.setWindow(gMin, gMax);
			else 
				stg.setWindow(wavesStg[w].getWindowStart(),
									wavesStg[w].getWindowEnd());
			wavesStg[w] = stg;
		}
	}
    
    /** 
     * Retrieves the configured stratgy for the specified wavelength.
     * 
     * @param w		The wavelength index in the <i>OME</i> 5D pixels file.
     */
	QuantumStrategy getStrategyFor(int w) { return wavesStg[w]; }
    
}

