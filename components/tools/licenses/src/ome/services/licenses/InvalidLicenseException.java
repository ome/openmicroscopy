/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.licenses;

/**
 * (CLIENT) Exception thrown on a regular method call when the provided 
 * license token is rejected by the {@link LicenseStore}. This does
 * <em>not</em> include calls to {@link ILicense#releaseLicense()}.
 * 
 * @author Josh Moore, josh.moore @ gmx.de
 * @since  3.0-RC1
 */
public class InvalidLicenseException extends LicenseException {

    private static final long serialVersionUID = -1260656423146821938L;

    public InvalidLicenseException(String msg) {
        super(msg);
    }

}
