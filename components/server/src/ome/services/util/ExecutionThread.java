/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.util;

import ome.model.meta.Session;
import ome.services.sessions.SessionManager;
import ome.services.util.Executor;
import ome.system.Principal;
import ome.util.DetailsFieldBridge;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
        Session s = this.manager.create(principal);
        Principal p = new Principal(s.getUuid(), principal.getGroup(),
                principal.getEventType());
        try {
            preWork();
            try {
                this.executor.execute(p, work);
            } finally {
                postWork();
            }
        } finally {
            this.manager.close(s.getUuid());
        }
    }

    public abstract void preWork();

    public abstract void postWork();
}