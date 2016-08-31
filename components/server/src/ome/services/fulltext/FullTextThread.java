/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.fulltext;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.util.Assert;

import ome.services.eventlogs.EventLogLoader;
import ome.services.sessions.SessionManager;
import ome.services.util.ExecutionThread;
import ome.services.util.Executor;
import ome.system.Principal;
import ome.util.DetailsFieldBridge;

/**
 * Library entry-point for indexing. Once the {@link FullTextThread} is properly
 * initialized calling {@link run()} repeatedly and from multiple
 * {@link Thread threads} should be safe.
 * 
 * For more control, use the {@link EventLogLoader#more()} method to test how
 * often calls to {@link #run} should be made. See {@link Main} for examples.
 * 
 * By default, the indexing will take place as "root".
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class FullTextThread extends ExecutionThread {

    private final static Log log = LogFactory.getLog(FullTextThread.class);

    private final static Principal DEFAULT_PRINCIPAL = new Principal("root",
            "system", "FullText");

    final protected boolean waitForLock;
    
    final protected FullTextIndexer indexer;

    final protected FullTextBridge bridge;

    private boolean isactive = true;
    
    private final Lock activeLock = new ReentrantLock(true);

    /**
     * Uses default {@link Principal} for indexing
     */
    public FullTextThread(SessionManager manager, Executor executor,
            FullTextIndexer indexer, FullTextBridge bridge) {
        this(manager, executor, indexer, bridge, DEFAULT_PRINCIPAL);
    }

    /**
     * Uses default {@link Principal} for indexing
     */
    public FullTextThread(SessionManager manager, Executor executor,
            FullTextIndexer indexer, FullTextBridge bridge, boolean waitForLock) {
        this(manager, executor, indexer, bridge, DEFAULT_PRINCIPAL, waitForLock);
    }

    /**
     * Main constructor. No arguments can be null.
     */
    public FullTextThread(SessionManager manager, Executor executor,
            FullTextIndexer indexer, FullTextBridge bridge, Principal principal) {
        super(manager, executor, indexer, principal);
        Assert.notNull(bridge);
        this.indexer = indexer;
        this.bridge = bridge;
        this.waitForLock = false;
    }

    /**
     * Main constructor. No arguments can be null.
     */
    public FullTextThread(SessionManager manager, Executor executor,
            FullTextIndexer indexer, FullTextBridge bridge,
            Principal principal, boolean waitForLock) {
        super(manager, executor, indexer, principal);
        Assert.notNull(bridge);
        this.indexer = indexer;
        this.bridge = bridge;
        this.waitForLock = waitForLock;
    }

    /**
     * Called by Spring on creation. Currently a no-op.
     */
    public void start() {
        log.info("Initializing Full-Text Indexer");
    }

    /**
     * Retry the work unit in case {@link ome.util.SqlAction#setCurrentEventLog(long, String)} used by
     * {@link ome.services.eventlogs.PersistentEventLogLoader} throws an unexpected constraint violation
     * as reported in Trac ticket #10181.
     * @param callContext the call context to use in work unit execution
     */
    private void retriableExecute(Map<String, String> callContext) {
        try {
            this.executor.execute(callContext, getPrincipal(), this.work);
        } catch (DataIntegrityViolationException e) {
            this.executor.execute(callContext, getPrincipal(), this.work);
            log.warn("work unit threw exception, succeeded on second try", e);
        }
    }

    /**
     * Passes the {@link FullTextIndexer} instance to
     * {@link Executor.Work#doWork(org.springframework.transaction.TransactionStatus, org.hibernate.Session, ome.system.ServiceFactory)}
     * between calls to {@link DetailsFieldBridge#lock()} and
     * {@link DetailsFieldBridge#unlock()} in order to guarantee that no other
     * {@link org.hibernate.search.bridge.FieldBridge} can edit the property.
     * Therefore, only one indexer using this idiom can run at a time.
     */
    @Override
    public void doRun() {

        activeLock.lock();
        try {
            if (!isactive) {
                log.info("Inactive; skipping");
                return;
            }
        } finally {
            activeLock.unlock();
        }

        final Map<String, String> callContext = new HashMap<String, String>();
        callContext.put("omero.group", "-1");

        if (waitForLock) {
            DetailsFieldBridge.lock();
            try {
                DetailsFieldBridge.setFieldBridge(this.bridge);
                retriableExecute(callContext);
            } finally {
                DetailsFieldBridge.unlock();
            }
        } else {
            if (DetailsFieldBridge.tryLock()) {
                try {
                    DetailsFieldBridge.setFieldBridge(this.bridge);
                    retriableExecute(callContext);
                } finally {
                    DetailsFieldBridge.unlock();
                }
            } else {
                log.info("Currently running; skipping");
            }
        }
    }

    /**
     * Called by Spring on destruction. Waits for the global lock on
     * {@link DetailsFieldBridge} then marks this thread as inactive.
     */
    public void stop() {
        log.info("Shutting down Full-Text Indexer");
        boolean acquiredLock = false;
        try {
            acquiredLock = activeLock.tryLock(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.warn("active lock acquisition interrupted.");
        }

        if (!acquiredLock) {
            log.error("Could not acquire active lock "
                    + "for indexer within 60 seconds. Overriding.");
        }

        acquiredLock = DetailsFieldBridge.tryLock();
        if (!acquiredLock) {
            log.error("Cound not acquire bridge lock. "
                    + "Waiting 60 seconds and aborting.");
            try {
                Thread.sleep(60 * 1000L);
            } catch (InterruptedException e) {
                log.warn("bridge lock acquisition interrupted.");
            }
        }

        isactive = false;
        if (acquiredLock) {
            DetailsFieldBridge.unlock();
        }
    }
}