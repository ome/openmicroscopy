/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.licenses;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ome.api.ISession;
import ome.logic.HardWiredInterceptor;
import ome.system.Principal;

import org.aopalliance.intercept.MethodInvocation;

/**
 * Responsible for enforcing a generic licensing policy:
 * <ul>
 * <li>All methods to {@link ILicense} are allowed.</li>
 * <li>In an application server:</li>
 * <ul>
 * <li>For other methods, a non-null {@link LicensedPrincipal} is required.</li>
 * <li>The {@link LicensedPrincipal#getLicenseToken() token} must be valid, as
 * defined by {@link LicenseStore#hasLicense(byte[])}.</li>
 * </ul>
 * <li>In OMERO.blitz:</li>
 * <ul>
 * <li>All licensing is handled transparently.</li>
 * </ul>
 * </ul>
 * 
 * This {@link HardWiredInterceptor} subclass gets compiled in via the build
 * system.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 * @see HardWiredInterceptor
 * @see ome.tools.spring.AOPAdapter
 */
public class LicenseWiring extends HardWiredInterceptor {

    /**
     * Single instance used by this interceptor. {@link LicenseBean} manages
     * synchronization for a static {@link LicenseStore} instance so that
     * synchronization is not necessary here.
     */
    LicenseStore store = new LicenseBean();

    private static Map<String, byte[]> tokensBySession = Collections
            .synchronizedMap(new HashMap<String, byte[]>());

    // ~ For use by LicenseSessionListener
    // =========================================================================

    byte[] getToken(String sessionName) {
        return tokensBySession.get(sessionName);
    }

    void setToken(String sessionName, byte[] token) {
        tokensBySession.put(sessionName, token);
    }

    @Override
    public String getName() {
        return "licenseWiring";
    }

    private final static Method acquire;
    private final static Method release;
    static {
        try {
            acquire = ILicense.class.getMethod("acquireLicense");
            release = ILicense.class.getMethod("releaseLicense", byte[].class);
        } catch (Exception e) {
            throw new RuntimeException("Error configuring LicenseWiring:", e);
        }
    }

    /**
     * This method implements special handling for calls to ILicense.
     * Originally, it was required to use ILicense directly. That is now largely
     * deprecated in favor of letting the session code handle it.
     * 
     * Calls may however, still be made to ILicense, and we should handle those
     * properly.
     */
    public Object handleILicense(MethodInvocation mi, Principal p)
            throws Throwable {

        String mthd = mi.getMethod().getName();

        if (acquire.getName().equals(mthd)) {
            byte[] token = (byte[]) mi.proceed();
            tokensBySession.put(p.getName(), token);
            return token;
        } else if (release.getName().equals(mthd)) {
            byte[] token = tokensBySession.get(p.getName());
            mi.getArguments()[0] = token;
            Object retVal = mi.proceed();
            tokensBySession.put(p.getName(), null);
            return retVal;
        } else {
            return mi.proceed();
        }
    }

    /**
     * Interceptor method which enforces the {@link LicenseWiring} policy.
     */
    public Object invoke(MethodInvocation mi) throws Throwable {

        Object t = mi.getThis();
        Principal p = getPrincipal(mi);

        // ISession cannot be licensed otherwise things won't work
        if (t instanceof ISession) {
            return mi.proceed();
        }

        // If this is a call to our license service, then give 'em a break.
        if (t instanceof ILicense) {
            return handleILicense(mi, p); // EARLY EXIT!!
        }

        // Since this isn't a privileged service, then a token must exist
        byte[] token;

        String session = p.getName();
        token = tokensBySession.get(session);

        // Was there really a token?
        if (token == null) {
            throw new LicenseException("Method requires a license. Please use "
                    + "ILicense.acquireLicense() or create a new session.");
        }

        // Yes, then allow them to continue, but mark their method boundaries.
        // Within enterMethod() the license validity will be checked.
        try {
            store.enterMethod(token, p);
            return mi.proceed();
        } finally {
            store.exitMethod(token, p);
        }
    }

}
