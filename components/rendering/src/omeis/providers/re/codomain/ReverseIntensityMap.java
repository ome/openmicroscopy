/*
 * omeis.providers.re.codomain.ReverseIntensityMap
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
 * Reverses the intensity levels of an image. 
 * It produces the equivalent of a photographic negative.
 * This type of transformation is suited fo enhancing white or gray 
 * details embedded in dark regions of an image, especially when the black areas
 * are dominant in size.
 *
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
class ReverseIntensityMap 
	implements CodomainMap
{
	
    /** The mapping context of this map. */
	private CodomainMapContext	ctx;
	
	/** 
     * Implemented as specified in {@link CodomainMap}. 
     * @see CodomainMap#setContext(CodomainMapContext)
     */
	public void setContext(CodomainMapContext ctx) { this.ctx = ctx; }

	/** 
     * Implemented as specified in {@link CodomainMap}. 
     * @see CodomainMap#transform(int)
     */
	public int transform(int x) { return ctx.intervalEnd-x; }
    
    /** 
     * Overriden to return the name of this map. 
     * @see Object#toString()
     */
    public String toString() { return "ReverseIntensityMap"; }
    
}

