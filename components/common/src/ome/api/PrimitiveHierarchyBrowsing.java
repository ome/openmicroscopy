/*
 * ome.api.PrimitiveHierarchyBrowsing
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

package ome.api;


//Java imports
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies

/** 
 * Provides methods to support browsing of Image hierarchies <b>using only primitive types as inputs</b>.
 * Otherwise as in {@link ome.api.HierarchyBrowsing}
 *
 * @author  <br>Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">
 * 					josh.moore@gmx.de</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 */
public interface PrimitiveHierarchyBrowsing
{

    /**
     * As in {@link HierarchyBrowsing#loadPDIHierarchy(Class, int)} but using only primitive types.
     * @param rootNodeType  The type of the root node.  Can either be
     * 						a string representing
     *                      {@link Project} or {@link Dataset}.
     * @param rootNodeID    The id of the root node.
     * @return The requested node as root and all of its descendants.  The type
     *         of the returned value will be <code>rootNodeType</code>. 
     */
    public OMEModel loadPDIHierarchy(String rootNodeType, int rootNodeID);
    
}
