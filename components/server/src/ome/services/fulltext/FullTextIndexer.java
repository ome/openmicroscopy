/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.fulltext;

import ome.conditions.InternalException;
import ome.model.IAnnotated;
import ome.model.IGlobal;
import ome.model.IMutable;
import ome.model.IObject;
import ome.model.meta.EventLog;
import ome.services.util.Executor.Work;
import ome.system.ServiceFactory;
import ome.tools.hibernate.QueryBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.springframework.transaction.TransactionStatus;

/**
 * Simple action which can be done in an asynchronous thread in order to index
 * Hibernate entities. Attempts to index each {@link EventLog} passed from the
 * {@link EventLogLoader} multiple times on failure. Eventually
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class FullTextIndexer implements Work {

    private final static Log log = LogFactory.getLog(FullTextIndexer.class);

    abstract class Action {
        Class type;
        long id;
        IObject obj;

        abstract void go(FullTextSession session);

        abstract void log(Log log);
    }

    class Purge extends Action {
        Purge(Class type, long id) {
            this.type = type;
            this.id = id;
        }

        @Override
        void go(FullTextSession session) {
            session.purge(type, id);
        }

        @Override
        void log(Log log) {
            log.info(String.format("Purged: %s:Id_%d", type, id));
        }
    }

    class Index extends Action {

        Index(IObject obj) {
            this.obj = obj;
        }

        @Override
        void go(FullTextSession session) {
            session.index(obj);
        }

        @Override
        void log(Log log) {
            log.info(String.format("Indexed: %s", obj));
        }
    }

    final protected EventLogLoader loader;

    public FullTextIndexer(EventLogLoader ll) {
        this.loader = ll;
    }

    public Object doWork(TransactionStatus status, Session session,
            ServiceFactory sf) {
        FullTextSession fullTextSession = Search.createFullTextSession(session);
        fullTextSession.setFlushMode(FlushMode.MANUAL);
        fullTextSession.setCacheMode(CacheMode.IGNORE);
        Transaction transaction = fullTextSession.beginTransaction();
        doIndexing(fullTextSession);
        transaction.commit();
        session.clear();
        return null;
    }

    public void doIndexing(FullTextSession session) {

        int count = 0;

        for (EventLog eventLog : loader) {
            // Three retries
            while (count < 3 && eventLog != null) {
                try {
                    String act = eventLog.getAction();
                    Class type = asClassOrThrow(eventLog.getEntityType());
                    long id = eventLog.getEntityId();

                    Action action;
                    if ("DELETE".equals(act)) {
                        action = new Purge(type, id);
                    } else if ("UPDATE".equals(act) || "INSERT".equals(act)) {
                        action = new Index(get(session, type, id));
                    } else {
                        throw new InternalException("Unknown action type: "
                                + act);
                    }

                    action.go(session);
                    action.log(log);
                    eventLog = null;
                    count = 0;
                } catch (Exception e) {
                    log.error(String.format("Failed to index %s %d times",
                            eventLog, count), e);
                }
            }

            // Failed; Giving up
            if (count > 0) {
                loader.rollback(eventLog);
                throw new InternalException("Failed to index entry. Giving up.");
            }
        }

    }

    protected Class asClassOrThrow(String str) {
        try {
            return Class.forName(str);
        } catch (ClassNotFoundException e) {
            throw new InternalException("Unknown entity type in database: "
                    + str);
        }
    }

    protected IObject get(Session session, Class type, long id) {
        QueryBuilder qb = new QueryBuilder();
        qb.select("this").from(type.getName(), "this");
        if (IAnnotated.class.isAssignableFrom(type)) {
            qb.join("this.annotationLinks", "l1", true, true);
            qb.join("l1.child", "a1", true, true);
            qb.join("a1.annotationLinks", "l2", true, true);
            qb.join("l2.child", "a2", true, true);
        }
        if (!IGlobal.class.isAssignableFrom(type)) {
            if (IMutable.class.isAssignableFrom(type)) {
                qb.join("this.details.updateEvent", "update", false, true);
            }
            qb.join("this.details.creationEvent", "create", false, true);
            qb.join("this.details.owner", "owner", false, true);
            qb.join("this.details.group", "group", false, true);
        }
        qb.where().and("this.id = :id");
        qb.param("id", id);

        return (IObject) qb.query(session).uniqueResult();
    }
}