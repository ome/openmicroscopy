/*
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.sessions;

import java.util.HashMap;
import java.util.Map;

import ome.conditions.InternalException;
import ome.model.meta.Experimenter;
import ome.model.meta.Node;
import ome.model.meta.Session;
import ome.parameters.Parameters;
import ome.services.util.Executor;
import ome.system.ServiceFactory;
import ome.util.SqlAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.transaction.annotation.Transactional;

/**
 * Is for ISession a cache and will be kept there in sync? OR Factors out the
 * logic from ISession and SessionManagerI
 *
 * Therefore either called directly, or via synchronous messages.
 *
 * Uses the name of a Principal as the key to the session. We may need to limit
 * user names to prevent this. (Strictly alphanumeric)
 *
 * Receives notifications as an {@link ApplicationListener}, which should be
 * used to keep the {@link Session} instances up-to-date.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class SessionManagerInDb extends BaseSessionManager {

    private final static Logger log = LoggerFactory.getLogger(SessionManagerInDb.class);

    // Executor methods
    // =========================================================================

    @Override
    protected Session executeUpdate(ServiceFactory sf, Session session,
            long userId, Long sudoerId) {
        Node node = nodeProvider.getManagerByUuid(internal_uuid, sf);
        if (node == null) {
            node = new Node(0L, false); // Using default node.
        }
        session.setNode(node);
        session.setOwner(new Experimenter(userId, false));
        if (sudoerId == null) {
            session.setSudoer(null);
        } else {
            session.setSudoer(new Experimenter(sudoerId, false));
        }
        Session rv = sf.getUpdateService().saveAndReturnObject(session);
        rv.putAt("#2733", session.retrieve("#2733"));
        return rv;
    }

    @Override
    protected Session executeCloseSession(final String uuid) {
        return (Session) executor
                .executeSql(new Executor.SimpleSqlWork(this,
                        "executeCloseSession") {
                    @Transactional(readOnly = false)
                    public Object doWork(SqlAction sql) {
                        try {
                            int count = sql.closeSessions(uuid);
                            if (count == 0) {
                                log.warn("No session updated on closeSession:"
                                        + uuid);
                            } else {
                                log.debug("Session.closed set to now() for "
                                        + uuid);
                            }
                        } catch (Exception e) {
                            log.error("FAILED TO CLOSE SESSION IN DATABASE: "
                                    + uuid, e);
                        }
                        return null;
                    }
                });
    }

    @Override
    protected Session executeInternalSession() {
        final Long sessionId = executeNextSessionId();
        return (Session) executor
                .executeSql(new Executor.SimpleSqlWork(this,
                        "executeInternalSession") {
                    @Transactional(readOnly = false)
                    public Object doWork(SqlAction sql) {

                        // Create a basic session
                        final Session s = new Session();
                        define(s, internal_uuid, "Session Manager internal",
                                System.currentTimeMillis(), Long.MAX_VALUE, 0L,
                                "Sessions", "Internal", null);

                        // Set the owner and node specially for an internal sess
                        final long nodeId = nodeProvider.getManagerIdByUuid(internal_uuid, sql);

                        // SQL defined in data.vm for creating original session
                        // (id,permissions,timetoidle,timetolive,started,closed,defaulteventtype,uuid,owner,node)
                        // select nextval('seq_session'),-35,
                        // 0,0,now(),now(),'rw----','PREVIOUSITEMS','1111',0,0;
                        Map<String, Object> params = new HashMap<String, Object>();
                        params.put("sid", sessionId);
                        params.put("ttl", s.getTimeToLive());
                        params.put("tti", s.getTimeToIdle());
                        params.put("start", s.getStarted());
                        params.put("type", s.getDefaultEventType());
                        params.put("uuid", s.getUuid());
                        params.put("node", nodeId);
                        params.put("owner", roles.getRootId());
                        params.put("agent", s.getUserAgent());
                        params.put("ip", s.getUserIP());
                        int count = sql.insertSession(params);
                        if (count == 0) {
                            throw new InternalException(
                                    "Failed to insert new session: "
                                            + s.getUuid());
                        }
                        Long id = sql.sessionId(s.getUuid());
                        s.setId(id);
                        return s;
                    }
                });
    }

    @Override
    protected Long executeNextSessionId() {
        return (Long) executor
                .executeSql(new Executor.SimpleSqlWork(this,
                        "executeNextSessionId") {
                    @Transactional(readOnly = false)
                    public Object doWork(SqlAction sql) {
                        return sql.nextSessionId();
                    }
                });
    }

    @Override
    protected Session findSessionById(Long id, ServiceFactory sf) {
        return (Session) sf.getQueryService().findByQuery(
                "select s from Session s "
                + "left outer join fetch s.annotationLinks l "
                + "left outer join fetch l.child a where s.id = :id",
                    new Parameters().addId(id));
    }
}
