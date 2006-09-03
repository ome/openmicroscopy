/*
 * ome.util.CBlock
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
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

package ome.util;

//Java imports

//Third-party libraries

//Application-internal dependencies
import ome.model.IObject;
import ome.model.core.Image;

/**
 * Block template used to "C"ollect the results of some function called
 * on each {@link ome.model.IObject IObject} in a collection. The {@link CBlock}
 * can be used to "map" {@link IObject} inputs to arbitrary outputs. All collection
 * valued fields on model objects have a method that will scan the collection
 * and apply the block of code. For example, {@link Image#collectPixels(CBlock)}  
 *  
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 3.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 3.0
 */
public interface CBlock<E>
{

	/** invoke this block.
	 * @param object An IObject (possibly null) which should be considered for
	 *   mapping.
	 * @return A possibly null value which is under some interpretation "mapped"
	 *   to the object argument
	 */
    E call( IObject object );

}
