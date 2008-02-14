/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.fulltext;

import ome.services.sessions.SessionManager;
import ome.services.util.ExecutionThread;
import ome.services.util.Executor;
import ome.system.Principal;
import ome.util.DetailsFieldBridge;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

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
        super(manager, executor, indexer, DEFAULT_PRINCIPAL);
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
        super(manager, executor, indexer, DEFAULT_PRINCIPAL);
        Assert.notNull(bridge);
        this.indexer = indexer;
        this.bridge = bridge;
        this.waitForLock = waitForLock;
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
        if (waitForLock) {
            DetailsFieldBridge.lock();
            try {
                DetailsFieldBridge.setFieldBridge(this.bridge);
                this.executor.execute(getPrincipal(), work, true);
            } finally {
                DetailsFieldBridge.unlock();
            }
        } else {
            if (DetailsFieldBridge.tryLock()) {
                try {
                    DetailsFieldBridge.setFieldBridge(this.bridge);
                    this.executor.execute(getPrincipal(), work, true);
                } finally {
                    DetailsFieldBridge.unlock();
                }
            }
        }
    }
}