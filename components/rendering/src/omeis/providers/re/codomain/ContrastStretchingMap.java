/*
 * omeis.providers.re.codomain.ContrastStretchingMap
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
 * Basic piecewise linear functions. 
 * The idea is to increase the dynamic range of levels in the image being 
 * processed.
 * The locations of the points pStart and pEnd 
 * (cf. {@link ContrastStretchingContext}) determine the equation of the linear
 * functions.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision: 1.2 $ $Date: 2005/06/20 10:59:54 $)
 * </small>
 * @since OME2.2
 */
class ContrastStretchingMap
	implements CodomainMap
{
	
    /** The mapping context. */
	private ContrastStretchingContext csCtx;
	
	/** Implemented as specified in {@link CodomainMap}. */
	public void setContext(CodomainMapContext ctx)
	{
		csCtx = (ContrastStretchingContext) ctx;
	}

	/** Implemented as specified in {@link CodomainMap}. */
	public int transform(int x)
	{
		int y = csCtx.intervalStart;
		if (x >= csCtx.intervalStart && x < csCtx.getXStart())
			y = (int) (csCtx.getA0()*x+csCtx.getB0());
		else if (x >= csCtx.getXStart() && x < csCtx.getXEnd())
			y = (int) (csCtx.getA1()*x+csCtx.getB1());
		else if (x >= csCtx.getXEnd() && x <= csCtx.intervalStart)
			y = (int) (csCtx.getA2()*x+csCtx.getB2());	
		return y;
	}
	
    /** Overrides the toString method. */
    public String toString() { return "ContrastStretchingMap"; }
    
}

