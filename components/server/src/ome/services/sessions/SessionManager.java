/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.sessions;

import ome.conditions.SecurityViolation;
import ome.model.meta.Session;
import ome.system.EventContext;
import ome.system.Principal;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * Responsible for holding onto {@link Session} instances for optimized login. 
 * 
 * Receives notifications as an {@link ApplicationListener}, which should be
 * used to keep the {@link Session} instances up-to-date.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public interface SessionManager extends ApplicationListener {

    Session create(Principal principal);

    /**
     * Provides a copy of the given {@link Session} which is not attached
     * to a Hibernate {@link org.hibernate.Session} but still contains the 
     * critical information needed.
     * 
     * @param session. Can be null, a null will be returned.
     * @return
     */
    Session copy(Session session);
    
    Session update(Session session);
    
    /**
     * @param sessionId
     * @return A current session. Null if the session id is not found.
     */
    Session find(String uuid);
    
    Session find(long id);

    void close(String uuid);

    /**
     * Requires that a
     * @param uuid
     */
    void assertSession(String uuid) throws SecurityViolation;
    
    /**
     * Provides a partial {@link EventContext} for the current {@link Session}.
     *  
     * @param uuid
     * @return
     */
    EventContext getEventContext(Principal principal);
    
    java.util.List<String> getUserRoles(String uuid);
    
    void onApplicationEvent(ApplicationEvent event);

}