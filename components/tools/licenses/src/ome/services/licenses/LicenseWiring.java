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
 * defined by {@link LicenseStore#hasLicense(byte[])}.</li>
 * </ul>
 * 
 * This {@link HardWiredInterceptor} subclass gets compiled into the server jar
 * via the server/build.xml script.
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @since 3.0-RC1
 * @see HardWiredInterceptor
 * @see ome.tools.spring.AOPAdapter
 * @see ome.logic.AbstractBean
 */
public class LicenseWiring extends HardWiredInterceptor {

    /**
     * Single instance used by this interceptor. {@link LicenseBean} manages
     * synchronization for a static {@link LicenseStore} instance so that
     * synchronization is not necessary here.
     */
    LicenseStore store = new LicenseBean();

    /**
     * Interceptor method which enforces the {@link LicenseWiring} policy.
     */
    public Object invoke(MethodInvocation mi) throws Throwable {

        Object t = mi.getThis();

        // If this is a call to our license service, then give 'em a break.
        if (t instanceof ILicense) {
            return mi.proceed(); // EARLY EXIT!!
        }

        // Since this isn't the license service, they have to use the proper
        // principal
        Principal p = getSessionContext(mi).getCallerPrincipal();
        if (!LicensedPrincipal.class.isAssignableFrom(p.getClass())) {
            throw new LicenseException("Client sent non-licensed Principal.");
        }

        // It is a LicensedPrincipal, but does it have a license?
        LicensedPrincipal lp = (LicensedPrincipal) p;
        byte[] token = lp.getLicenseToken();
        if (token == null) {
            throw new LicenseException("Method requires a license. Please use "
                    + "ILicense.acquireLicense().");
        }

        // Yes, then allow them to continue, but mark their method bounaries.
        // Within enterMethod() the license validity will be checked.
        try {
            store.enterMethod(token, lp);
            return mi.proceed();
        } finally {
            store.exitMethod(token, lp);
        }
    }
}
