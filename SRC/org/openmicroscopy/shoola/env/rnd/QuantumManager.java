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
import org.openmicroscopy.shoola.env.rnd.quantum.QuantumFactory;
import org.openmicroscopy.shoola.env.rnd.quantum.QuantumStrategy;

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
class QuantumManager
{
    /** Number of wavelengths. */
	private int						sizeW;

	private QuantumStrategy[]     	wavesStg;
    
	QuantumManager()
	{
		//sizeW = d.sizeW;
		wavesStg = new QuantumStrategy[sizeW];          
	}
    
    /**
     * Set a strategy.
     * @param qd	
     */
	void setStrategy(QuantumDef qd)
	{
		if (qd == null) return;
		QuantumStrategy     stg;
		//ImgAgent imgAgent = (ImgAgent)AgentsManager.getAgent(ImgAgent.class);
		//ImgGlobalStatsEntry globalStatsEntry;
		for (int w = 0; w < sizeW; ++w) {
				stg = QuantumFactory.getStrategy(qd);
				//globalStatsEntry = imgAgent.getGlobalStats(w);
				//stg.setExtent(new Integer(globalStatsEntry.globalMin), 
				//					new Integer(globalStatsEntry.globalMax));
				if (wavesStg[w] != null)
					stg.setWindow(wavesStg[w].getWindowStart(),
								wavesStg[w].getWindowEnd());
					wavesStg[w] = stg;
		}
	}
    
    /** 
     * Retrieves a the stratgy of the specified wavelength.
     * 
     * @param w		wavelength index.
     */
	QuantumStrategy getStrategyFor(int w)
	{
		return wavesStg[w];
	}
    
}

