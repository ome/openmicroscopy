/*
 * org.openmicroscopy.shoola.env.rnd.codomain.ContrastStretchingMap
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
	implements CodomainMap
{
	private ContrastStretchingDef csDef;
	
	/** Implemented as specified in {@link CodomainMap}. */
	public void setContext(CodomainMapContext ctx)
	{
		csDef = (ContrastStretchingDef) ctx;
	}

	/** Implemented as specified in {@link CodomainMap}. */
	public int transform(int x)
	{
		int y = csDef.intervalStart;
		if (x >= csDef.intervalStart && x < csDef.getXStart())
			y = (int) (csDef.getA0()*x+csDef.getB0());
		else if (x >= csDef.getXStart() && x < csDef.getXEnd())
			y = (int) (csDef.getA1()*x+csDef.getB1());
		else if (x >= csDef.getXEnd() && x <= csDef.intervalStart)
			y = (int) (csDef.getA2()*x+csDef.getB2());	
		return y;
	}
	
}

