/*
 * org.openmicroscopy.shoola.env.rnd.codomain.CodomainMapFactory
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
import java.util.Iterator;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.rnd.defs.CodomainMapDef;

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
public class CodomainFactory
{
	public static CodomainChain makeNew(int cdStart, int cdEnd, 
										List codomainMapDefs) 
	{
		CodomainChain chain = new CodomainChain(cdStart, cdEnd);
		Iterator i = codomainMapDefs.iterator();
		CodomainMapDef cmd;
		CodomainMap cMap;
		while (i.hasNext()) {
			cmd = (CodomainMapDef) i.next();
			cMap = makeNewMap(cdStart, cdEnd, cmd);
			chain.add(cMap);
		}
		return chain;	
	}
	
	/** 
	 * Make a new codomainMap and a new CodomainCtx, link them. 
	 * 
	 * @param cdStart	lower bound of the codomain interval.
	 * @param cdEnd		upper bound of the codomain interval.
	 * @param cmd		Reference to a codomainMapDef object used to build
	 * 					map and its context.
	 * @return codomainMap object.
	 */
	private static CodomainMap makeNewMap(int cdStart, int cdEnd, 
										CodomainMapDef cmd)
	{
		CodomainMap map = null;
		CodomainMapContext ctx = null;
		switch (cmd.getType()) {
			case CodomainMapDef.CONTRAST_STRETCHING:
				map = new ContrastStretchingMap();
				ctx = new ContrastStretchingContext();
				break;
			case CodomainMapDef.PLANE_SLICING:
				map = new PlaneSlicingMap();
				ctx = new PlaneSlicingContext();
				break;
			case CodomainMapDef.REVERSE_INTENSITY:
				map = new ReverseIntensityMap();
				ctx = new PlaneSlicingContext();		
		}
		ctx.setCodomain(cdStart, cdEnd);
		ctx.updateFields(cmd.getParams());
		map.setContext(ctx);
		
		return map;
	}
	
}
