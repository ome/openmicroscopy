/*
 * org.openmicroscopy.shoola.agents.rnd.ContrastStretchingMap
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

package org.openmicroscopy.shoola.agents.rnd;

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
class ContrastStretchingMap
	extends CodomainMap
{
	/** Codomain interval.*/
	private int cdStart;
	private int cdEnd;
	
	ContrastStretchingMap(int cdStart, int cdEnd)
	{
		this.cdStart = cdStart;
		this.cdEnd = cdEnd;
	}

	/** Implemented as specified in {@link CodomainMap}. 
	 * 
	 * @param params	ContrastStretchingDef object with data used to write
	 * 					the equation of the three linear mappings: y = a*x+b.
	 */
	void setContext(Object params)
	{
		
	}

	/** Implemented as specified in {@link CodomainMap}. */
	int transform(int x)
	{
		return x;
	}

	/** Implemented as specified in {@link CodomainMap}. */
	void setCodomain(int cdStart, int cdEnd)
	{
		this.cdStart = cdStart;
		this.cdEnd = cdEnd;
	}
	
}
