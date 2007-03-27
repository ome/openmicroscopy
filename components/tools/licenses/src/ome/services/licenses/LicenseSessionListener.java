/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.licenses;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ome.security.SecuritySystem;
import ome.services.icy.util.AbstractSessionMessage;
import ome.services.icy.util.CreateSessionMessage;
import ome.services.icy.util.DestroySessionMessage;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * Listens for {@link AbstractSessionMessages} sent by
 * {@link ome.services.icy.fire.SesssionManagerI} and 
 * {@link ome.services.icy.impl.ServiceFactoryI} and creates licenses
 * when necessary for the user. The listener decouples the session 
 * creation from the licensing logic.
 *
 * This is notably different from the process needed to acquire a
 * license in the application server case, since there is no 
 * central session.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
public class LicenseSessionListener implements ApplicationListener {

    private ILicense lic;

    private LicenseWiring wiring;

    private SecuritySystem sec;

    public void setSecuritySystem(SecuritySystem secSys) {
        this.sec = secSys;
    }

    public void setLicenseWiring(LicenseWiring licenseWiring) {
        this.wiring = licenseWiring;
    }

    public void setService(ILicense service) {
        this.lic = service;
    }

    public void onApplicationEvent(ApplicationEvent event) {
            if (event instanceof CreateSessionMessage) {
                CreateSessionMessage create = (CreateSessionMessage) event;
                login(create);
                try {
                    byte[] token = lic.acquireLicense();
                } finally {
                    logout();
                }
            } else if (event instanceof DestroySessionMessage) {
                DestroySessionMessage destroy = (DestroySessionMessage) event;
                try {
                    login(destroy);
                    byte[] token = wiring.getToken(destroy.getSessionId());
                    lic.releaseLicense(token);
                } finally {
                    logout();
            }
        }
    }

    protected void login(AbstractSessionMessage event) {
        this.sec.login(event.getPrincipal());
    }

    protected void logout() {
        this.sec.logout();
    }

}
