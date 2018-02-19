/*
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.sessions;

import java.util.HashMap;
import java.util.Map;

import ome.api.local.LocalQuery;
import ome.conditions.InternalException;
import ome.model.meta.Experimenter;
import ome.model.meta.Node;
import ome.model.meta.Session;
import ome.model.meta.Share;
import ome.parameters.Parameters;
import ome.security.NodeProvider;
import ome.services.util.Executor;
import ome.services.util.ReadOnlyStatus;
import ome.system.Roles;
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
public class SessionProviderInDb implements SessionProvider, ReadOnlyStatus.IsAware {

    private final static Logger log = LoggerFactory.getLogger(SessionProviderInDb.class);

    private final Roles roles;

    private final NodeProvider nodeProvider;

    private final Executor executor;

    public SessionProviderInDb(Roles roles, NodeProvider nodeProvider, Executor executor) {
        this.roles = roles;
        this.nodeProvider = nodeProvider;
        this.executor = executor;
    }

    // Executor methods
    // =========================================================================

    @Override
    public Session executeUpdate(ServiceFactory sf, Session session, String uuid,
            long userId, Long sudoerId) {
        Node node = nodeProvider.getManagerByUuid(uuid, sf);
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
    public void executeCloseSession(final String uuid) {
        executor.executeSql(new Executor.SimpleSqlWork(this,
                        "executeCloseSession") {
            @Transactional(readOnly = false)
            public Object doWork(SqlAction sql) {
                try {
                    final int count = sql.closeSessions(uuid);
                    if (count == 0) {
                        log.info("No session updated on closeSession: {}", uuid);
                    } else {
                        log.debug("Session.closed set to now() for {}", uuid);
                    }
                } catch (Exception e) {
                    log.error("FAILED TO CLOSE SESSION IN DATABASE: {}", uuid, e);
                }
                return null;
            }
        });
    }

    @Override
    public Session executeInternalSession(final String uuid, final Session session) {
        final Long sessionId = executeNextSessionId();
        return (Session) executor
                .executeSql(new Executor.SimpleSqlWork(this,
                        "executeInternalSession") {
                    @Transactional(readOnly = false)
                    public Object doWork(SqlAction sql) {

                        // Set the owner and node specially for an internal sess
                        final long nodeId = nodeProvider.getManagerIdByUuid(uuid, sql);

                        // SQL defined in data.vm for creating original session
                        // (id,permissions,timetoidle,timetolive,started,closed,defaulteventtype,uuid,owner,node)
                        // select nextval('seq_session'),-35,
                        // 0,0,now(),now(),'rw----','PREVIOUSITEMS','1111',0,0;
                        Map<String, Object> params = new HashMap<String, Object>();
                        params.put("sid", sessionId);
                        params.put("ttl", session.getTimeToLive());
                        params.put("tti", session.getTimeToIdle());
                        params.put("start", session.getStarted());
                        params.put("type", session.getDefaultEventType());
                        params.put("uuid", session.getUuid());
                        params.put("node", nodeId);
                        params.put("owner", roles.getRootId());
                        params.put("agent", session.getUserAgent());
                        params.put("ip", session.getUserIP());
                        int count = sql.insertSession(params);
                        if (count == 0) {
                            throw new InternalException(
                                    "Failed to insert new session: "
                                            + session.getUuid());
                        }
                        Long id = sql.sessionId(session.getUuid());
                        session.setNode(new Node(nodeId, false));
                        session.setOwner(new Experimenter(roles.getRootId(), false));
                        session.setId(id);
                        return session;
                    }
                });
    }

    @Override
    public long executeNextSessionId() {
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
    public Session findSessionById(Long id, ServiceFactory sf) {
        final LocalQuery iQuery = (LocalQuery) sf.getQueryService();
        final String sessionClass = iQuery.find(Share.class, id) == null ? "Session" : "Share";
        return (Session) iQuery.findByQuery(
                        "select s from " + sessionClass + " s "
                        + "left outer join fetch s.sudoer "
                        + "left outer join fetch s.annotationLinks l "
                        + "left outer join fetch l.child a where s.id = :id",
                        new Parameters().addId(id).cache());
    }

    @Override
    public boolean isReadOnly(ReadOnlyStatus readOnly) {
        return readOnly.isReadOnlyDb();
    }
}
