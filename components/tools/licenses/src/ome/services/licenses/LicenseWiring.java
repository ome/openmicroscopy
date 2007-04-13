/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.licenses;

// Java imports

import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.aopalliance.intercept.MethodInvocation;

// Application-internal dependencies
import ome.logic.HardWiredInterceptor;
import ome.services.icy.fire.SessionPrincipal;
import ome.services.icy.util.CreateSessionMessage;
import ome.services.icy.util.DestroySessionMessage;

/**
 * Responsible for enforcing a generic licensing policy:
 * <ul>
 * <li>All methods to {@link ILicense} are allowed.</li>
 * <li>In an application server:</li>
 *  <ul>
 *   <li>For other methods, a non-null {@link LicensedPrincipal} is required.</li>
 *   <li>The {@link LicensedPrincipal#getLicenseToken() token} must be valid, as
 * defined by {@link LicenseStore#hasLicense(byte[])}.</li>
 *  </ul>
 * <li>In OMERO.blitz:</li>
 *  <ul>
 *   <li>All licensing is handled transparently.</li>
 *  </ul>
 * </ul>
 *
 * This {@link HardWiredInterceptor} subclass gets compiled in via the build system.
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
            release = ILicense.class.getMethod("releaseLicense",byte[].class);
        } catch (Exception e) {
            throw new RuntimeException("Error configuring LicenseWiring:",e);
        }
    }

    /**
     * This method implements special handling for calls to ILicense via
     * blitz.
     * @param mi
     * @return
     * @throws Throwable
     * @DEV.TODO This should most likely be handled by having a separate
     *          interface for blitz, e.g. "BlitzLicense" but for the moment
     *          we'll use this. Note: this is also complicated by recursion,
     *          one has to be careful not to let LicenseWiring be called twice.
     */
    public Object handleILicense(MethodInvocation mi) throws Throwable {

        Principal p = getPrincipal(mi);
        boolean blitz = SessionPrincipal.class.isAssignableFrom(p.getClass());
        String mthd = mi.getMethod().getName();

        if (!blitz) {
            return mi.proceed();
        }

        SessionPrincipal sp = (SessionPrincipal) p;
        if (acquire.getName().equals(mthd)) {
            byte[] token = (byte[]) mi.proceed();
            tokensBySession.put(sp.getSession(), token);
            return token;
        } else if (release.getName().equals(mthd)) {
            byte[] token = tokensBySession.get(sp.getSession());
            mi.getArguments()[0] = token;
            Object retVal = mi.proceed();
            tokensBySession.put(sp.getSession(), null);
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

        // If this is a call to our license service, then give 'em a break.
        if (t instanceof ILicense) {
            return handleILicense(mi); // EARLY EXIT!!
        }

        // Since this isn't the license service, they have to use the proper
        // principal
        Principal p = getPrincipal(mi);
        LicensedPrincipal lp;
        byte[] token;
        if (LicensedPrincipal.class.isAssignableFrom(p.getClass())) {

            // It is a LicensedPrincipal, but does it have a license?
            lp = (LicensedPrincipal) p;
            token = lp.getLicenseToken();

        } else if (SessionPrincipal.class.isAssignableFrom(p.getClass())) {

            // It is a SessionPrincipal from blitz, let's see if there's a
            // current session.
            SessionPrincipal sp = (SessionPrincipal) p;
            String session = sp.getSession();
            token = tokensBySession.get(session);
            lp = new LicensedPrincipal(sp.getName(),sp.getGroup(),sp.getEventType());
            lp.setLicenseToken(token);
        } else {

            throw new LicenseException("No valid principal found:"+p);

        }

        // Was there really a token?
        if (token == null) {
            throw new LicenseException("Method requires a license. Please use "
                    + "ILicense.acquireLicense().");
        }

        // Yes, then allow them to continue, but mark their method boundaries.
        // Within enterMethod() the license validity will be checked.
        try {
            store.enterMethod(token, lp);
            return mi.proceed();
        } finally {
            store.exitMethod(token, lp);
        }
    }

}
