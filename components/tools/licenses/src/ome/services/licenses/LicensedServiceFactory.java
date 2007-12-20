/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.licenses;

// Java imports
import java.util.Properties;

// Third-party libraries

import ome.system.Login;
import ome.system.OmeroContext;
import ome.system.Principal;
import ome.system.Server;

/**
 * (CLIENT) Entry point for all licensed client calls. Provides methods to obtain proxies 
 * for all remote facades, including the {@link ILicense} extension. If 
 * licesning is activated in the server, a call to {@link #acquireLicense()}
 * should be performed before calls to any methods (other than {@link ILicense}
 * calls. Before garbage collection, it is the responsibility of the client to
 * call {@link #releaseLicense()} or other clients will have to wait for
 * the license timeout.
 * 
 * Note: the license token can be manually acquired and set via 
 * {@link ILicense#acquireLicense()} and {@link #setLicenseToken(byte[])}.

 * @author Josh Moore, josh.moore at gmx.de
 * @see OmeroContext
 * @since 3.0-RC1
 */
public class LicensedServiceFactory extends ome.system.ServiceFactory {

    public LicensedServiceFactory(Properties prp, Server srv, Login lgn) {
        super((OmeroContext)null); // Does nothing.
        Properties p = prp == null ? new Properties() : prp;
        Properties s = srv == null ? new Properties() : srv.asProperties();
        Properties l = lgn == null ? new Properties() : lgn.asProperties();
        p.putAll(s);
        p.putAll(l);
        this.ctx = OmeroContext.getContext(p,getDefaultContext());
    }

    // ~ Accessors
    // =========================================================================

    /** Acquires a license token and calls {@link #setTokenLicense(byte[])}.
     *  All future calls will be made with this license.
     */
    public boolean acquireLicense() {
        boolean result = true;
        try {
            byte[] token = getLicenseService().acquireLicense();
            setLicenseToken(token);
        } catch (LicenseException le) {
            result = false;
        }
        return result;
    }

    /** Releases the license via {@link ILicense#releaseToken(byte[])} and
     *  sets the current license token to null.
     */
    public boolean releaseLicense() {
        byte[] token = getLicensedPrincipal().getLicenseToken();
        boolean result = getLicenseService().releaseLicense(token);
        setLicenseToken(null);
	return result;
    }

    /**
     * Sets the license token on a {@link LicensedPrincipal} instance which will
     * be passed to the server-side on each invocation.
     */
    public void setLicenseToken(byte[] licenseToken) {
        LicensedPrincipal p = getLicensedPrincipal();
        p.setLicenseToken(licenseToken);
    }

    // ~ Stateless services
    // =========================================================================

    public ILicense getLicenseService() {
        return getServiceByClass(ILicense.class);
    }

    // ~ Helpers
    // =========================================================================

    // TODO move to ServiceFactory
    protected Principal getPrincipal() {
        if (!ctx.containsBean("principal")) {
            throw new UnsupportedOperationException("The context for this "
                    + "ServiceFactory does not contain a Principal on which "
                    + "the umask can be set.");
        }
        Principal p = (Principal) ctx.getBean("principal");
        return p;
    }

    protected LicensedPrincipal getLicensedPrincipal() {
        Principal p = getPrincipal();
        if (!LicensedPrincipal.class.isAssignableFrom(p.getClass())) {
            throw new IllegalStateException("The Principal in this context "
                    + "has not been properly configured for licensing. "
                    + "Please refer to your documentation on how to setup "
                    + "a ome.services.licenses.LicensedPrincipal");

        }
        return (LicensedPrincipal) p;
    }

}
