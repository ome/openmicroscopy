/*
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

import java.util.List;

import ome.api.ServiceInterface;
import ome.services.blitz.util.BlitzExecutor;

import omero.api.AMD_PyramidService_getResolutionDescriptions;
import omero.api.AMD_PyramidService_getResolutionLevel;
import omero.api.AMD_PyramidService_getResolutionLevels;
import omero.api.AMD_PyramidService_getTileSize;
import omero.api.AMD_PyramidService_requiresPixelsPyramid;
import omero.api.AMD_PyramidService_setResolutionLevel;
import omero.api.PyramidService;
import omero.api.ResolutionDescription;
import omero.util.IceMapper;
import omero.util.IceMapper.ReturnMapping;

import Ice.Current;

/**
 * Specialization of {@link AbstractAmdServant} to be used by any services which
 * provide the {@link PyramidService} interface.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.3
 */
public abstract class AbstractPyramidServant extends AbstractCloseableAmdServant {

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

    public void getResolutionDescriptions_async(
            AMD_PyramidService_getResolutionDescriptions __cb, Current __current) {
        IceMapper mapper = new IceMapper(RESOLUTION_DESCRIPTIONS);
        callInvokerOnMappedArgs(mapper, __cb, __current);
    }

    /**
     * This is a fairly brittle mapping from the List<List<Integer>> created by
     * the PixelBuffers to the List<ResolutionDescription> which is remotely
     * provided by Blitz. The assumption is that much of these two levels will
     * be refactored together and therefore that shouldn't be a long-term
     * problem.
     */
    public final static ReturnMapping RESOLUTION_DESCRIPTIONS = new ReturnMapping() {
        public Object mapReturnValue(IceMapper mapper, Object value)
        throws Ice.UserException {

            if (value == null) {
                return null;
            }

            @SuppressWarnings("unchecked")
            List<List<Integer>> sizesArr = (List<List<Integer>>) value;
            ResolutionDescription[] rv = new ResolutionDescription[sizesArr.size()];
            for (int i = 0; i < rv.length; i++) {
                List<Integer> sizes = sizesArr.get(i);
                ResolutionDescription rd = new ResolutionDescription();
                rd.sizeX = sizes.get(0);
                rd.sizeY = sizes.get(1);
                rv[i] = rd;
            }

            return rv;
        }
    };

    //
    // Close logic
    //

    @Override
    protected void preClose(Current current) throws Throwable {
        // no-op
    }

    @Override
    protected void postClose(Current current) {
        // no-op
    }

}
