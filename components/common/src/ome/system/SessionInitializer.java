/*
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.system;

import ome.api.ISession;
import ome.model.meta.Session;

/**
 * 
 * Manages the creation of a single {@link Session} created via the injected
 * {@link ISession} service. This is used for by the client-side
 * {@link ServiceFactory}. Each instance synchronizes on an internal mutex
 * during every call to {@link #getSession()} and
 * {@link #setSession(Session)}
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 */
public class SessionInitializer {

    protected Object mutex = new Object();

    /** Principal given by the user */
    protected Principal principal;

    protected String credentials;

    protected ome.model.meta.Session session;

    protected ISession sessions;

    public void setSessionService(ISession service) {
        this.sessions = service;
    }

    public void setPrincipal(Principal principal) {
        this.principal = principal;
    }

    public void setCredentials(String securityCredentials) {
        this.credentials = securityCredentials;
    }

    public boolean hasSession() {
        synchronized (mutex) {
            return session != null;
        }
    }

    public ome.model.meta.Session getSession() {
        synchronized (mutex) {
            if (session == null) {
                session = sessions.createSession(principal, credentials);
            }
        }
        return this.session;
    }

    public void setSession(Session s) {
        synchronized (mutex) {
            this.session = s;
        }
    }

    public Principal createPrincipal() {
        getSession();
        Principal sessionPrincipal = new Principal(this.session.getUuid(),
                this.principal.getGroup(), this.principal.getEventType());
        return sessionPrincipal;
    }

}
