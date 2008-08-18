/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.licenses;

import ome.services.blitz.impl.AbstractAmdServant;
import ome.services.blitz.util.BlitzExecutor;
import omero.ServerError;
import omero.licenses.AMD_ILicense_acquireLicense;
import omero.licenses.AMD_ILicense_getAvailableLicenseCount;
import omero.licenses.AMD_ILicense_getLicenseTimeout;
import omero.licenses.AMD_ILicense_getTotalLicenseCount;
import omero.licenses.AMD_ILicense_releaseLicense;
import omero.licenses.AMD_ILicense_resetLicenses;
import omero.licenses.NoAvailableLicenseException;
import omero.licenses._ILicenseOperations;
import Ice.Current;

/**
 * Implementation of the ILicense service.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta4
 * @see ome.services.licenses.ILicense
 */
public class LicenseI extends AbstractAmdServant implements _ILicenseOperations {

    public LicenseI(ILicense service, BlitzExecutor be) {
        super(service, be);
    }

    // Interface methods
    // =========================================================================

    public void acquireLicense_async(AMD_ILicense_acquireLicense __cb,
            Current __current) throws ServerError, NoAvailableLicenseException {
        serviceInterfaceCall(__cb, __current);

    }

    public void getAvailableLicenseCount_async(
            AMD_ILicense_getAvailableLicenseCount __cb, Current __current)
            throws ServerError {
        serviceInterfaceCall(__cb, __current);

    }

    public void getLicenseTimeout_async(AMD_ILicense_getLicenseTimeout __cb,
            Current __current) throws ServerError {
        serviceInterfaceCall(__cb, __current);

    }

    public void getTotalLicenseCount_async(
            AMD_ILicense_getTotalLicenseCount __cb, Current __current)
            throws ServerError {
        serviceInterfaceCall(__cb, __current);

    }

    public void releaseLicense_async(AMD_ILicense_releaseLicense __cb,
            byte[] token, Current __current) throws ServerError {
        serviceInterfaceCall(__cb, __current, token);

    }

    public void resetLicenses_async(AMD_ILicense_resetLicenses __cb,
            Current __current) throws ServerError {
        serviceInterfaceCall(__cb, __current);

    }
}
