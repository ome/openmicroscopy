/*
 * org.openmicroscopy.shoola.env.rnd.codomain.CodomainChain
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

package org.openmicroscopy.shoola.env.rnd.codomain;

import java.util.ArrayList;
import java.util.List;

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
public class CodomainChain
{
	//	current lookup table
	private int[]		LUT;
	private List		chains;
	private int			intervalStart;
	private int			intervalEnd;
	private CodomainMap	identity;
		
	public CodomainChain(int intervalStart, int intervalEnd)
	{
		this.intervalStart = intervalStart;
		this.intervalEnd = intervalEnd;
		init();
	}
	
	//TODO: create a copy
	public void add(CodomainMap cdm)
	{
		chains.add(cdm);
	}
	
	public void remove(CodomainMap cdm)
	{
		chains.remove(cdm);
	}
	
	public int transform(int x)
	{
		return LUT[x-intervalStart];
	}

	public int getIntervalEnd()
	{
		return intervalEnd;
	}

	public int getIntervalStart()
	{
		return intervalStart;
	}

	public void setIntervalEnd(int intervalEnd)
	{
		this.intervalEnd = intervalEnd;
		buildLUT();
	}

	public void setIntervalStart(int intervalStart)
	{
		this.intervalStart = intervalStart;
		buildLUT();
	}
	
	/** Initializes the list of codomain maps. */
	private void init()
	{ 
		chains = new ArrayList();
		if (identity == null) identity = new IdentityMap();
		chains.add(identity);
	}
	
	/** Build the codomain LUT. */
	private void buildLUT()
	{
		LUT = new int[intervalEnd-intervalStart];
		CodomainMap cdm;
		int v;
		for(int x = intervalStart; x <= intervalEnd; ++x) {
			v = x;
			for (int i = 0; i < chains.size(); i++) {
				cdm = (CodomainMap) chains.get(i);
				v = cdm.transform(v);
			}
			LUT[x-intervalStart] = v;	
		}
	}
	
}
