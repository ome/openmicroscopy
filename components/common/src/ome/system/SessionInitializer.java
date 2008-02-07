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
 * during every call to {@link #getSessionId()} and
 * {@link #setSessionId(String)}
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 */
public class SessionInitializer {

    protected Object mutex = new Object();

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
}
