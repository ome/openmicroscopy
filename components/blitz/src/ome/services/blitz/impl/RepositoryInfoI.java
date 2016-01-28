/*
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

import ome.api.IRepositoryInfo;
import ome.services.blitz.util.BlitzExecutor;
import omero.ServerError;
import omero.api.AMD_IRepositoryInfo_getFreeSpaceInKilobytes;
import omero.api.AMD_IRepositoryInfo_getUsageFraction;
import omero.api.AMD_IRepositoryInfo_getUsedSpaceInKilobytes;
import omero.api.AMD_IRepositoryInfo_removeUnusedFiles;
import omero.api.AMD_IRepositoryInfo_sanityCheckRepository;
import omero.api._IRepositoryInfoOperations;

import Ice.Current;

/**
 * Implementation of the IRepositoryInfo service.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta4
 * @see ome.api.IRepositoryInfo
 */
public class RepositoryInfoI extends AbstractAmdServant implements
        _IRepositoryInfoOperations {

    public RepositoryInfoI(IRepositoryInfo service, BlitzExecutor be) {
        super(service, be);
    }

    // Interface methods
    // =========================================================================

    public void getFreeSpaceInKilobytes_async(
            AMD_IRepositoryInfo_getFreeSpaceInKilobytes __cb, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current);

    }

    public void getUsageFraction_async(
            AMD_IRepositoryInfo_getUsageFraction __cb, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current);

    }

    public void getUsedSpaceInKilobytes_async(
            AMD_IRepositoryInfo_getUsedSpaceInKilobytes __cb, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current);

    }

    public void removeUnusedFiles_async(
            AMD_IRepositoryInfo_removeUnusedFiles __cb, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current);

    }

    public void sanityCheckRepository_async(
            AMD_IRepositoryInfo_sanityCheckRepository __cb, Current __current)
            throws ServerError {
        callInvokerOnRawArgs(__cb, __current);

    }

}
