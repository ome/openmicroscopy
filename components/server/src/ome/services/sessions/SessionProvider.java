/*
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.sessions;

import ome.model.meta.Session;
import ome.system.ServiceFactory;

/**
 * Provides {@link SessionManagerImpl} with wrapper around session storage backends.
 * @author Josh Moore, josh at glencoesoftware.com
 * @author m.t.b.carroll@dundee.ac.uk
 */
public interface SessionProvider {

    Session executeUpdate(ServiceFactory sf, Session session, String uuid, long userId, Long sudoerId);

    /**
     * Loads a session directly, sets its "closed" value and immediately
     * saves it. This method is not called
     * directly from the {@link SessionManager#close(String)} and {@link SessionManager#closeAll()} methods
     * since there are other non-explicit ways for a session to be destroyed, such
     * as a timeout within {@link ome.services.sessions.state.SessionCache} and so this is called from
     * {@link SessionManagerImpl#onApplicationEvent(org.springframework.context.ApplicationEvent)} when a
     * {@link ome.services.messages.DestroySessionMessage} is received.
     */
    void executeCloseSession(String uuid);

    Session executeInternalSession(String uuid, Session session);

    /**
     * Added as an attempt to cure ticket:1176
     */
    long executeNextSessionId();

    /**
     * Retrieves a session by ID.
     * @param id session ID to lookup
     * @param sf active service factory
     * @return See above.
     */
    Session findSessionById(long id, ServiceFactory sf);
}
