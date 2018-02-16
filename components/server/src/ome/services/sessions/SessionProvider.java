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

    Session executeCloseSession(String uuid);

    Session executeInternalSession(String uuid, Session session);

    long executeNextSessionId();

    Session findSessionById(Long id, ServiceFactory sf);
}
