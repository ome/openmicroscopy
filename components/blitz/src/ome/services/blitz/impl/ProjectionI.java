/*
 * ome.api.ProjectionI
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

import java.util.List;

import Ice.Current;
import ome.api.ServiceInterface;
import ome.services.blitz.util.BlitzExecutor;
import ome.services.blitz.util.BlitzOnly;
import omero.ServerError;
import omero.api.AMD_IProjection_projectPixels;
import omero.api.AMD_IProjection_projectStack;
import omero.api._IProjectionOperations;
import omero.constants.projection.ProjectionType;
import omero.model.PixelsType;

/**
 * Implementation of the IProjection service.
 * 
 * @author Chris Allan <callan at blackcat.ca>
 * @since 3.0-Beta4
 * @see ome.api.IProjection
 */
public class ProjectionI
    extends AbstractAmdServant implements _IProjectionOperations, BlitzOnly
{

    public ProjectionI(ServiceInterface service, BlitzExecutor be)
    {
        super(service, be);
    }
    
    public void projectPixels_async(AMD_IProjection_projectPixels __cb,
            long pixelsId, PixelsType pixelsType, ProjectionType algorithm, 
            int tStart, int tEnd, List<Integer> channelList, int stepping, 
            int zStart, int zEnd, String name, Current __current)
        throws ServerError
    {
        callInvokerOnRawArgs(__cb, __current, pixelsId, pixelsType, 
                             algorithm.ordinal(), tStart, tEnd, channelList, 
                             stepping, zStart, zEnd, name);
    }

    public void projectStack_async(AMD_IProjection_projectStack __cb,
            long pixelsId, PixelsType pixelsType, ProjectionType algorithm, 
            int timepoint, int channelIndex, int stepping, int start, int end,
            Current __current) throws ServerError
    {
        callInvokerOnRawArgs(__cb, __current, pixelsId, pixelsType, 
                             algorithm.ordinal(), timepoint, channelIndex, 
                             stepping, start, end);
    }
}
