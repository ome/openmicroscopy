/*
 * org.openmicroscopy.shoola.env.rnd.codomain.PlaneSlicingMap
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
 * We consider that the image is composed of eight 1-bit planes.
 * Two types of plane Slicing transformations {@link #transformConstant}
 * and {@link #transformNonConstant} are possible.
 * Let l denote the level of the <code>planeSelected</code>.
 * 1-	Map all levels &lt; l to the constant <code>lowerLimit</code> and 
 * 		the levels &gt; l to the constant <code>upperLimit</code>.
 * 		This transformation highlights the range l and reduces all others to 
 * 		a constant level.
 * 2-	This transformation highlights the rang l 
 * 		but preserves all other levels.	
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
class PlaneSlicingMap
	implements CodomainMap
{
	
	private PlaneSlicingContext	psCtx;

	/** 
	 * Implemented as specified in {@link CodomainMap}. 
	 * 
	 * @param params	SlicingDef object.
	 */
	public void setContext(CodomainMapContext ctx)
	{
		psCtx = (PlaneSlicingContext) ctx;
	}

	/** Implemented as specified in {@link CodomainMap}. */
	public int transform(int x)
	{
		int r;
		if (psCtx.IsConstant()) r = transformConstant(x);
		else r = transformNonConstant(x);
		return r;
	}
	
	/** 
	* Highlights the level of the <code>planeSelected</code> and 
	* reduces all others to a constant level.
	*/
	private int transformConstant(int x) 
	{
		int r;
		if (x < psCtx.getPlaneSelected()) r = psCtx.getLowerLimit();
		else if (x > psCtx.getPlaneSelected()+1) r = psCtx.getUpperLimit();
		else r = psCtx.getPlaneSelected();
		return r;
	}
	
	/** 
	* Highlights the level of the <code>planeSelected</code> but
	* preserves all other levels.
	*/
	private int transformNonConstant(int x) 
	{
		int r = x;
		if (x > psCtx.getPlanePrevious() && x <= psCtx.getPlaneSelected()) 
			r = psCtx.getPlaneSelected();
		return r;
	}
	
}

