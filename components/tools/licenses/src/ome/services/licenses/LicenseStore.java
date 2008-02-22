/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.licenses;

import ome.security.SecuritySystem;
import ome.services.sessions.SessionManager;
import ome.system.Principal;

// Java imports

// Third-party libraries

// Application-internal dependencies

/**
 * Provides local extensions of {@link ILicense} for validating license tokens
 * and tracking license timeouts.
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @since 3.0-RC1
 */
public interface LicenseStore extends ILicense {

    /**
     * Increments the number of currently active methods associated with a
     * single license and prevents timeouts during the method call. Required to
     * call {@link #hasLicense(byte[])} within its synchronization boundaries,
     * also responsible for timeouts.
     */
    void enterMethod(byte[] token, Principal p) throws InvalidLicenseException,
            LicenseTimeout;

    /**
     * Decrements the number of active methods associated with a single license
     * and timestamps the last use of the license.
     */
    void exitMethod(byte[] token, Principal p);

    /**
     * Checks for the validity of a token and that the Not responsible for
     * timeouts.
     */
    boolean hasLicense(byte[] token) throws InvalidLicenseException;

    /**
     * Injector which allows the central {@link SecuritySystem} instance to be
     * injected in the {@link LicenseStore store} post-constructor (since the
     * main instance may be constructed statically.
     * 
     * Implementation may want to prevent the setter from being called multiple
     * times, and may want to synchronize access for the same reason.
     * 
     * @DEV.TODO This should not need to be "StaticSecuritySystem" but is so
     *           because of the current inheritance from
     *           {@link ome.logic.AbstractBean} of {@link LicenseBean} which
     *           will hopefully go away in the future.
     */
    void setStaticSecuritySystem(SecuritySystem securitySystem);

    /**
     * Starting with OmeroSessions, the {@link LicenseStore} needs to have
     * access more to the {@link SessionManager} rather than the
     * {@link SecuritySystem} since during session-creation, no user is logged
     * in.
     */
    void setSessionManager(SessionManager sessionManager);
}
