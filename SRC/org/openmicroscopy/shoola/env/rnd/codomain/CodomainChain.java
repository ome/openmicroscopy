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

//Java imports
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//Third-party libraries

//Application-internal dependencies

/** 
 * Queues the spatial domain transformations that to be applied to the image.
 * A lookup table is built by composing all transformations (in the same order
 * as they were enqueued) in a map and then by applying the map to each value
 * in the codomain interval [intervalStart, intervalEnd].
 * Note that, in order to compose the transformations in the queue, 
 * this interval has to be both the domain and codomain of each transformations.
 * The LUT is re-built every time the definition of the interval changes.
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
	/** Maximum value for the upper bound of the interval. */
	private static final int	MAX = 255;
	
	/** Minimum value for the lower bound of the interval. */
	private static final int	MIN = 0;
	
	/** Codomain Lookup table .*/
	private int[]				LUT;
	
	/** List of the codomain transformations to be performed. */
	private List				chains;
	
	/** The lower limit of the interval. */
	private int					intervalStart;
	
	/** The upper limit of the interval. */
	private int					intervalEnd;
	
	/** Identity map. */
	private CodomainMap			identity;
		
	public CodomainChain(int start, int end)
	{
		verifyInterval(start, end);
		intervalStart = start;
		intervalEnd = end;
		init();
	}
	
	//TODO: create a copy
	/** 
	 * Add a codomainMap transformation to the queue. 
	 * 
	 * @param cdm	CodomainMap to be added.
	 * 
	 */
	public void add(CodomainMap cdm)
	{
		chains.add(cdm);
	}
	
	/** 
 	* Remove the specified codomainMap transformation from the queue. 
 	* 
 	* @param cdm	CodomainMap to be removed.
 	* 
 	*/
	public void remove(CodomainMap cdm)
	{
		chains.remove(cdm);
	}
	
	/** 
	 * Performs the transformation.
	 * 
	 * @param x		input value.
	 */
	public int transform(int x)
	{
		verifyInput(x);
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

	public void setIntervalEnd(int end)
	{
		verifyInterval(intervalStart, end);
		intervalEnd = end;
		buildLUT();
	}

	public void setIntervalStart(int start)
	{
		verifyInterval(start, intervalEnd);
		intervalStart = start;
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
		Iterator i = chains.iterator();
		for (int x = intervalStart; x <= intervalEnd; ++x) {
			v = x;
			while (i.hasNext()) {
				cdm = (CodomainMap) i.next();
				v = cdm.transform(v);
			}
			LUT[x-intervalStart] = v;	
		}
	}
	
	/** 
	 * Verify the bounds of the input interval. 
	 * 
	 * @param start		lower bound of the interval.
	 * @param end		upper bound of the interval.
	 */
	private void verifyInterval(int start, int end)
	{
		if (start >= end || start < MIN || end > MAX)
			throw new IllegalArgumentException("Interval not consistent.");
	}
	
	/** 
	 * Verify if the input value is in the interval 
	 * [intervalStart, intervalEnd].
	 * 
	 * @param x		input value.
	 */
	private void verifyInput(int x)
	{
		if (x < intervalStart || x > intervalEnd)
			throw new IllegalArgumentException("Value not in the Interval.");
	}
	
}
