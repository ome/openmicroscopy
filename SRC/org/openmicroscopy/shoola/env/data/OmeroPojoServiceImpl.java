/*
 * org.openmicroscopy.shoola.env.data.OmeroPojoServiceImpl
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
import ome.util.builders.PojoOptions;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.UserDetails;

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
class OmeroPojoServiceImpl
    implements OmeroPojoService
{
    
    /** Uses it to gain access to the container's services. */
    private Registry        context;
    
    /** Reference to the entry point to access the <i>OMERO</i> services. */
    private OMEROGateway    gateway;

    /**
     * Checks if the specified classification algorithm is supported.
     * 
     * @param algorithm The passed index.
     * @return <code>true</code> if the algorithm is supported.
     */
    private boolean checkAlgorithm(int algorithm)
    {
        switch (algorithm) {
            case OmeroPojoService.CLASSIFICATION:
            case OmeroPojoService.DECLASSIFICATION_ME:
            case OmeroPojoService.DECLASSIFICATION_NME:    
                return true;
        }
        return false;
    }
    
    /**
     * Creates a new instance.
     * 
     * @param gateway   Reference to the OMERO entry point.
     *                  Mustn't be <code>null</code>.
     * @param registry Reference to the registry. Mustn't be <code>null</code>.
     */
    OmeroPojoServiceImpl(OMEROGateway gateway, Registry registry)
    {
        if (registry == null)
            throw new IllegalArgumentException("No registry.");
        if (gateway == null)
            throw new IllegalArgumentException("No gateway.");
        context = registry;
        this.gateway = gateway;
    }
    
    /** Implemented as specified by {@link OmeroPojoService}. */
    public Set loadContainerHierarchy(Class rootNodeType, Set rootNodeIDs,
                                    boolean withLeaves)
        throws DSOutOfServiceException, DSAccessException 
    {
        PojoOptions po = new PojoOptions();
        UserDetails ud = getUserDetails();
        Integer id = new Integer(ud.getUserID());
        po.annotationsFor(id);
        po.exp(id);
        if (!withLeaves) po.noLeaves();
        return gateway.loadContainerHierarchy(rootNodeType, rootNodeIDs,
                                            po.map());
    }

    /** Implemented as specified by {@link OmeroPojoService}. */
    public Set findContainerHierarchy(Class rootNodeType, Set leavesIDs)
        throws DSOutOfServiceException, DSAccessException
    {
        PojoOptions po = new PojoOptions();
        UserDetails ud = getUserDetails();
        Integer id = new Integer(ud.getUserID());
        po.annotationsFor(id);
        po.exp(id);
        return gateway.findContainerHierarchy(rootNodeType, leavesIDs,
                        po.map());
    }

    /** Implemented as specified by {@link OmeroPojoService}. */
    public Map findAnnotations(Class nodeType, Set nodeIDs, boolean history)
        throws DSOutOfServiceException, DSAccessException
    {
        PojoOptions po = new PojoOptions();
        po.allAnnotations();
        po.noLeaves();
        return gateway.findAnnotations(nodeType, nodeIDs, po.map());
    }

    /** Implemented as specified by {@link OmeroPojoService}. */
    public Set findCGCPaths(Set imgIDs, int algorithm)
        throws DSOutOfServiceException, DSAccessException
    {
        if (!checkAlgorithm(algorithm)) 
            throw new IllegalArgumentException("Find CGCPaths algorithm not " +
                    "supported.");
        PojoOptions po = new PojoOptions();
        po.noAnnotations();
        po.exp(new Integer(getUserDetails().getUserID()));
        return gateway.findCGCPaths(imgIDs, algorithm, po.map());
    }
    
    /** Implemented as specified by {@link OmeroPojoService}. */
    public Set getImages(Class nodeType, Set nodeIDs)
        throws DSOutOfServiceException, DSAccessException
    {
        PojoOptions po = new PojoOptions();
        Integer id = new Integer(getUserDetails().getUserID());
        po.annotationsFor(id);
        po.exp(id);
        return gateway.getImages(nodeType, nodeIDs, po.map());
    }
    
    /** Implemented as specified by {@link OmeroPojoService}. */
    public Set getUserImages()
        throws DSOutOfServiceException, DSAccessException
    {
        PojoOptions po = new PojoOptions();
        po.noAnnotations();
        Integer id = new Integer(getUserDetails().getUserID());
        po.exp(id);
        return gateway.getUserImages(po.map());
    }
    
    //Need to review this method
    public UserDetails getUserDetails()
    {
        DataManagementService dms = context.getDataManagementService();
        try {
            return dms.getUserDetails();
        } catch (Exception e) {
            // TODO: handle exception
        }
        return null;
        
        
    }
    
}
