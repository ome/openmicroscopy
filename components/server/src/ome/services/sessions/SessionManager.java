/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.sessions;

import java.util.List;

import ome.conditions.SecurityViolation;
import ome.model.internal.Permissions;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
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
 * {@link SessionManager} implementations should strive to be only in-memory
 * representations of the database used as a performance optimization. When possible,
 * all changes should be made to the database as quickly and as synchronously as
 * possible.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public interface SessionManager extends ApplicationListener {

    Session create(Experimenter u, ExperimenterGroup g,
    		List<Long> leaderIds, List<Long> memberIds, List<String> roles,
    		String type, Permissions perms);

    /**
     * Copies the source {@link Session} to the targe instance. This can be useful
     * to disconnect from a Hibernate {@link org.hibernate.Session} while maintaining
     * critical information needed.
     * 
     * @param source Cannot be null.
     * @param source Cannot be null.
     */
    void copy(Session source, Session target);
    
    Session update(Session session);
    
    /**
     * @param sessionId
     * @return A current session. Null if the session id is not found.
     */
    Session find(String uuid);

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