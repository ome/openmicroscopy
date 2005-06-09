/*
 * org.openmicroscopy.omero.logic.ContainerDao
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

package org.openmicroscopy.omero.logic;

//Java imports
import java.util.List;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.omero.model.Category;
import org.openmicroscopy.omero.model.CategoryGroup;

/** data access object for various hierarchies of OME objects.
 * 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 1.0
 */
public interface ContainerDao {
    public Object loadHierarchy(final Class arg0, final int arg1);

    public List findPDIHierarchies(final Set arg0);

    public List findCGCIHierarchies(final Set arg0);

    /** load necessary because of the whackyness of CategoryGroup 
     * @DEV.TODO TEMPORARY this must be moved to its own DAO if it can't be abolished all together 
     * @param id
     * @return a CategoryGroup
     */
    public CategoryGroup loadCG(Integer id);

    /** load necessary because of the whackyness of Category 
     * @DEV.TODO TEMPORARY this must be moved to its own DAO if it can't be abolished all together 
     * @param id
     * @return a Category
     */
    public Category loadC(Integer id);
}