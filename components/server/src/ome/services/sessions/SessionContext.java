/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.sessions;

import java.util.List;

import ome.model.meta.Session;
import ome.services.sessions.state.SessionCache;
import ome.system.EventContext;

/**
 * Extends {@link EventContext} to hold a {@link Session}. This is used by the
 * {@link SessionManager} to store information in the {@link SessionCache}.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public interface SessionContext extends EventContext {

    Session getSession();
    List<String> getUserRoles();

}