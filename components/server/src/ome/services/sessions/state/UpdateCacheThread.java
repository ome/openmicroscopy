/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.sessions.state;

import ome.services.sessions.SessionManager;
import ome.services.util.ExecutionThread;
import ome.services.util.Executor;
import ome.system.Principal;
import ome.system.ServiceFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.util.Assert;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class UpdateCacheThread extends ExecutionThread {

    private final static Log log = LogFactory.getLog(UpdateCacheThread.class);

    private final static Principal DEFAULT_PRINCIPAL = new Principal("root",
            "system", "Sessions");

    final protected SessionCache cache;

    final static class Work implements Executor.Work {
        private final SessionCache cache;

        Work(SessionCache cache) {
            this.cache = cache;
        }

        public Object doWork(TransactionStatus status,
                org.hibernate.Session session, ServiceFactory sf) {
            cache.doUpdate();
            return null;
        }
    }

    /**
     */
    public UpdateCacheThread(SessionManager manager, Executor executor,
            SessionCache cache) {
        this(manager, executor, cache, DEFAULT_PRINCIPAL);
    }

    /**
     * Main constructor. No arguments can be null.
     */
    public UpdateCacheThread(SessionManager manager, Executor executor,
            SessionCache cache, Principal principal) {
        super(manager, executor, new Work(cache), DEFAULT_PRINCIPAL);
        Assert.notNull(cache);
        this.cache = cache;
    }

    @Override
    public void doRun() {
        this.executor.execute(getPrincipal(), this.work, false);
    }

}