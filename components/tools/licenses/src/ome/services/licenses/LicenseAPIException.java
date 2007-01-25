/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.licenses;

// Java imports
import javax.ejb.ApplicationException;

// Third-party libraries

// Application-internal dependencies

/**
 * (CLIENT) Thrown by a properly functioning server only when the 
 * client has improperly used the licensing infrastructure (non-null 
 * {@link LicensedPrincipal} requirement).
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @since 3.0-RC1
 */
@ApplicationException
public class LicenseAPIException extends LicenseException {

    private static final long serialVersionUID = -6265930201780653560L;

    public LicenseAPIException(String msg) {
        super(msg);
    }

}
