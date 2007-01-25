/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.licenses;

// Java imports

// Third-party libraries

import ome.system.Principal;

/**
 * (CLIENT) Extension of {@link ome.system.Principal} which carries a byte-array
 * license token to the server.
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @see ome.system.Principal
 * @since 3.0-RC1
 */
public class LicensedPrincipal extends ome.system.Principal {

    private static final long serialVersionUID = 4441954018296933085L;

    protected byte[] licenseToken = null;

    /** See {@link Principal#Principal(String, String, String)} */
    public LicensedPrincipal(String userName, String userGroup, String eventType) {
        super(userName, userGroup, eventType);
    }

    // MUTABLE

    public boolean hasLicenseToken() {
        return this.licenseToken != null;
    }

    public byte[] getLicenseToken() {
        return this.licenseToken;
    }

    /**
     * Sets the byte-array license token.
     * @param token May be null.
     */
    public void setLicenseToken(byte[] token) {
        licenseToken = token;
    }

}
