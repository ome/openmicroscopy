/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.fulltext;

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
public class FullTextThread implements Runnable {

    private final static Log log = LogFactory.getLog(FullTextThread.class);

    private final static Principal DEFAULT_PRINCIPAL = new Principal("root",
            "system", "FullText");

    final protected Executor executor;
    final protected FullTextIndexer indexer;
    final protected FullTextBridge bridge;
    final protected Principal principal;

    /**
     * Uses default {@link Principal} for indexing
     */
    public FullTextThread(Executor executor, FullTextIndexer indexer,
            FullTextBridge bridge) {
        this(executor, indexer, bridge, DEFAULT_PRINCIPAL);
    }

    /**
     * Main constructor. No arguments can be null.
     */
    public FullTextThread(Executor executor, FullTextIndexer indexer,
            FullTextBridge bridge, Principal principal) {
        Assert.notNull(executor);
        Assert.notNull(indexer);
        Assert.notNull(bridge);
        Assert.notNull(principal);
        this.executor = executor;
        this.indexer = indexer;
        this.bridge = bridge;
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
    public void run() {
        DetailsFieldBridge.lock();
        try {
            DetailsFieldBridge.setFieldBridge(this.bridge);
            this.executor.execute(this.principal, this.indexer);
        } finally {
            DetailsFieldBridge.unlock();
        }
    }

}