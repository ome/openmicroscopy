/*
 * org.openmicroscopy.shoola.env.data.NullOmeroPojoService
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

package org.openmicroscopy.shoola.env.data;



//Java imports
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *                  a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class NullOmeroPojoService
    implements OmeroPojoService
{

    /**
     * No-op implementation
     * @see OmeroPojoService#findAnnotations(Class, Set, boolean)
     */
    public Map findAnnotations(Class nodeType, Set nodeIDs, boolean history)
    	throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    /**
     * No-op implementation
     * @see OmeroPojoService#findCGCPaths(Set, int)
     */
    public Set findCGCPaths(Set imgIDs, int algorithm) 
    	throws DSOutOfServiceException, DSAccessException 
    {
        return null;
    }

    /**
     * No-op implementation
     * @see OmeroPojoService#getUserImages()
     */
    public Set getUserImages()
    	throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    /**
     * No-op implementation
     * @see OmeroPojoService#getCollectionCount(Class, String, Set)
     */
    public Map getCollectionCount(Class rootNodeType, String property,
            						Set rootNodeIDs)
    	throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    /**
     * No-op implementation
     * @see OmeroPojoService#loadContainerHierarchy(Class, Set, boolean, Class, 
     *                                              int)
     */
    public Set loadContainerHierarchy(Class rootNodeType, Set rootNodeIDs, 
            boolean withLeaves, Class rootLevel, int rootLevelID)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * No-op implementation
     * @see OmeroPojoService#findContainerHierarchy(Class, Set, Class, int)
     */
    public Set findContainerHierarchy(Class rootNodeType, Set leavesIDs, 
            Class rootLevel, int rootLevelID)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * No-op implementation
     * @see OmeroPojoService#getImages(Class, Set, Class, int)
     */
    public Set getImages(Class nodeType, Set nodeIDs, Class rootLevel, 
                        int rootLevelID)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }


}
