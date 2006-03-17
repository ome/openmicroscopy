/*
 * omeis.providers.re.codomain.CodomainMapContext
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

package omeis.providers.re.codomain;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Each concrete subclass defines transformation parameters for a 
 * {@link CodomainMap} implementation.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision: 1.1 $ $Date: 2005/06/09 17:23:37 $)
 * </small>
 * @since OME2.2
 */
public abstract class CodomainMapContext
{
	
	/** The lower bound of the codomain interval. */
	protected int intervalStart;
	
	/** The upper bound of the codomain interval. */
	protected int intervalEnd;
	
	/** 
	 * Sets the codomain interval.
	 * No checks are needed as this method is controlled by the
	 * {@link CodomainChain}, which passes in consistent values.
	 * 
	 * @param intervalStart	The lower bound of the codomain interval.
	 * @param intervalEnd	The upper bound of the codomain interval.
	 */
	public void setCodomain(int intervalStart, int intervalEnd)
	{
		this.intervalStart = intervalStart;
		this.intervalEnd = intervalEnd;
	}
	
	/**
	 * This method is overridden so that objects of the same class are 
	 * considered the same.
	 * We need this trick to hanlde nicely <code>CodomainMapContext</code>
	 * objects in collections.
     * @see Object#equals(Object)
	 */
	public final boolean equals(Object o) 
	{
		if (o == null)	return false;
		return (o.getClass() == getClass());
	}

	/**
	 * Computes any parameter that depends on the codomain interval.
	 * The {@link CodomainChain} always calls this method after setting the
	 * interval via {@link #setCodomain(int, int) setCodomain()}.
	 */
	abstract void buildContext();
	
	/**
	 * Returns an instance of the {@link CodomainMap} class that pairs up
	 * with this concrete context class.
	 * 
	 * @return See above.
	 */
	abstract CodomainMap getCodomainMap();
	
	/**
	 * Returns a deep copy of this object.
	 * 
	 * @return See above.
	 */
	public abstract CodomainMapContext copy();
	
}
