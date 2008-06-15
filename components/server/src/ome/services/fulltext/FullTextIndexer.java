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

    protected int reps = 5;

    /**
     * Spring injector. Sets the number of indexing runs will be made if there
     * is a substantal backlog.
     */
    public void setRepetitions(int reps) {
        this.reps = reps;
        ;
    }

    public FullTextIndexer(EventLogLoader ll) {
        this.loader = ll;
    }

    /**
     * Runes {@link #doIndexing(FullTextSession)} within a Lucene transaction.
     * {@link #doIndexing(FullTextSession)} will also be called
     */
    public Object doWork(TransactionStatus status, Session session,
            ServiceFactory sf) {
        int count = 1;
        do {
            FullTextSession fullTextSession = Search
                    .createFullTextSession(session);
            fullTextSession.setFlushMode(FlushMode.MANUAL);
            fullTextSession.setCacheMode(CacheMode.IGNORE);
            Transaction transaction = fullTextSession.beginTransaction();
            doIndexing(fullTextSession);
            transaction.commit();
            session.clear();
            count++;
        } while (doMore(count));
        return null;
    }

    public void doIndexing(FullTextSession session) {

        int count = 0;

        for (EventLog eventLog : loader) {
            if (eventLog != null) {
                String act = eventLog.getAction();
                Class type = asClassOrNull(eventLog.getEntityType());
                if (type != null) {
                    long id = eventLog.getEntityId();

                    Action action = null;
                    if ("DELETE".equals(act)) {
                        action = new Purge(type, id);
                    } else if ("UPDATE".equals(act) || "INSERT".equals(act)) {
                        action = new Index(get(session, type, id));
                    } else {
                        log.error("Unknown action type: " + act);
                    }

                    if (action != null) {
                        try {
                            action.go(session);
                        } catch (Exception e) {
                            loader.rollback(eventLog);
                            throw new InternalException(
                                    "FullTextIndexer stuck! "
                                            + "Failed to index EventLog: "
                                            + eventLog);
                        }
                        action.log(log);
                    }
                }
            }

        }

    }

    /**
     * Default implementation suggests doing more if fewer than {@link #reps}
     * runs have been made and if there are still more than
     * {@link EventLogLoader#batchSize} x 100 backlog entries.
     * 
     * This is based on the assumption that indexing runs roughly 120 times an
     * hour, so if there are more than an hours worth of batches, do extra work
     * to catch up.
     */
    public boolean doMore(int count) {
        if (count < this.reps && loader.more() > loader.batchSize * 100) {
            log.info(String
                    .format("Suggesting round %s of "
                            + "indexing to reduce backlog of %s:", count,
                            loader.more()));
            return true;
        }
        return false;
    }

    protected Class asClassOrNull(String str) {
        try {
            return Class.forName(str);
        } catch (ClassNotFoundException e) {
            log.warn("Unknown entity type found in database: " + str);
            return null;
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