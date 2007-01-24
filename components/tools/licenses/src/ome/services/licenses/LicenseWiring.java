/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.licenses;

// Java imports

import java.security.Principal;

import org.aopalliance.intercept.MethodInvocation;

// Application-internal dependencies
import ome.logic.HardWiredInterceptor;

/**
 * Responsible for enforcing a generic licensing policy:
 * <ul>
 * <li>All methods to {@link ILicense} are allowed.</li>
 * <li>For other methods, a non-null {@link LicensedPrincipal} is required.</li>
 * <li>The {@link LicensedPrincipal#getLicenseToken() token} must be valid, as
 * defined by {@link LicenseStore#isValid(byte[])}.</li>
 * </ul> 
 *
 * This {@link HardWiredInterceptor} subclass gets compiled
 * into the server jar via the server/build.xml script.
 *
 * @author Josh Moore, josh.moore at gmx.de
 * @since 3.0-RC1
 * @see HardWiredInterceptor
 * @see ome.logic.AOPAdapter
 * @see ome.logic.AbstractBean
 */
public class LicenseWiring extends HardWiredInterceptor {

    public Object invoke(MethodInvocation mi) throws Throwable {

        LicenseStore store = new LicenseBean();

        Object t = mi.getThis();

        // If this is a call to our license service, then give 'em a break.
        if (t instanceof ILicense) {
            return mi.proceed(); // EARLY EXIT!!
        }

        Principal p = getSessionContext(mi).getCallerPrincipal();
        if (!LicensedPrincipal.class.isAssignableFrom(p.getClass())) {
            throw new LicenseException("Client sent non-licensed Principal.");
        }

        LicensedPrincipal lp = (LicensedPrincipal) p;
        byte[] token = lp.getLicenseToken();

        if (token == null) {
            throw new LicenseException("Method requires a license. Please use " +
                        "ILicense.acquireLicense().");
        }
        
        if (!store.isValid(token)) {
            throw new LicenseException("License not valid.");
        }

        return mi.proceed();
    }
}
