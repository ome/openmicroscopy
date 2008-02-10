/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.util;

import ome.conditions.SessionException;
import ome.model.meta.Session;
import ome.services.fulltext.FullTextIndexer;
import ome.services.sessions.SessionManager;
import ome.system.Principal;
import ome.util.DetailsFieldBridge;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.search.bridge.FieldBridge;
import org.springframework.util.Assert;

/**
 * Thread which can be started and will appropriately login itself, complete its
 * work, commit its transaction, and log itself out.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public abstract class ExecutionThread implements Runnable {

    private final static Log log = LogFactory.getLog(ExecutionThread.class);

    final protected SessionManager manager;
    final protected Executor executor;
    final protected Executor.Work work;
    final protected Principal principal;
    protected Principal sessionPrincipal = null;
    protected Session session = null;

    /**
     * Main constructor. No arguments can be null.
     */
    public ExecutionThread(SessionManager manager, Executor executor,
            Executor.Work work, Principal principal) {
        Assert.notNull(manager);
        Assert.notNull(executor);
        Assert.notNull(work);
        Assert.notNull(principal);
        this.manager = manager;
        this.executor = executor;
        this.work = work;
        this.principal = principal;
    }

    /**
     * Passes the {@link FullTextIndexer} instance to
     * {@link Executor.Work#doWork(org.springframework.transaction.TransactionStatus, org.hibernate.Session, ome.system.ServiceFactory)}
     * between calls to {@link DetailsFieldBridge#lock()} and
     * {@link DetailsFieldBridge#unlock()} in order to guarantee that no other
     * {@link FieldBridge} can edit the property. Therefore, only one indexer
     * using this idiom can run at a time.
     */
    public final void run() {
        sessionInit();
        try {
            boolean cont = preWork();
            try {
                if (cont) {
                    this.executor.execute(sessionPrincipal, work);
                }
            } finally {
                postWork();
            }
        } finally {
            // sessionInit() is now trying to create
            // a single session and keep it alive for
            // this thread.
            // this.manager.close(session.getUuid());
        }
    }

    protected final void sessionInit() {

        if (sessionPrincipal != null) {
            try {
                this.manager.getEventContext(sessionPrincipal);
            } catch (SessionException e) {
                sessionPrincipal = null;
            }
        }

        if (sessionPrincipal == null) {
            session = this.manager.create(principal);
            sessionPrincipal = new Principal(session.getUuid(), principal
                    .getGroup(), principal.getEventType());
        }
    }

    /**
     * Can return a veto to prevent execution.
     * 
     * @return true of the execution should continue.
     */
    public abstract boolean preWork();

    public abstract void postWork();
}