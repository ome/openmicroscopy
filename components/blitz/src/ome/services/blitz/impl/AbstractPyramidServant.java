/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

import ome.api.ServiceInterface;
import ome.services.blitz.util.BlitzExecutor;
import omero.api.AMD_PyramidService_getResolutionLevel;
import omero.api.AMD_PyramidService_getResolutionLevels;
import omero.api.AMD_PyramidService_getTileSize;
import omero.api.AMD_PyramidService_requiresPixelsPyramid;
import omero.api.AMD_PyramidService_setResolutionLevel;
import omero.api.PyramidService;
import Ice.Current;

/**
 * Specialization of {@link AbstractAmdServant} to be used by any services which
 * provide the {@link PyramidService} interface.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.3
 */
public abstract class AbstractPyramidServant extends AbstractAmdServant {

    public AbstractPyramidServant(ServiceInterface service, BlitzExecutor be) {
        super(service, be);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * omero.api._PyramidServiceOperations#getResolutionLevels_async(omero.api
     * .AMD_PyramidService_getResolutionLevels, Ice.Current)
     */
    public void getResolutionLevels_async(
            AMD_PyramidService_getResolutionLevels __cb, Current __current) {
        callInvokerOnRawArgs(__cb, __current);
    }

    /*
     * (non-Javadoc)
     *
     * @see omero.api._PyramidServiceOperations#getTileSize_async(omero.api.
     * AMD_PyramidService_getTileSize, Ice.Current)
     */
    public void getTileSize_async(AMD_PyramidService_getTileSize __cb,
            Current __current) {
        callInvokerOnRawArgs(__cb, __current);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * omero.api._PyramidServiceOperations#requiresPixelsPyramid_async(omero.api.
     * AMD_PyramidService_requiresPixelsPyramid, Ice.Current)
     */
    public void requiresPixelsPyramid_async(
            AMD_PyramidService_requiresPixelsPyramid __cb, Current __current) {
        callInvokerOnRawArgs(__cb, __current);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * omero.api._PyramidServiceOperations#setResolutionLevel_async(omero.api
     * .AMD_PyramidService_setResolutionLevel, int, Ice.Current)
     */
    public void setResolutionLevel_async(
            AMD_PyramidService_setResolutionLevel __cb, int resolutionLevel,
            Current __current) {
        callInvokerOnRawArgs(__cb, __current, resolutionLevel);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * omero.api._PyramidServiceOperations#getResolutionLevel_async(omero.api
     * .AMD_PyramidService_getResolutionLevel, Ice.Current)
     */
    public void getResolutionLevel_async(
            AMD_PyramidService_getResolutionLevel __cb, Current __current) {
        callInvokerOnRawArgs(__cb, __current);
    }

}
