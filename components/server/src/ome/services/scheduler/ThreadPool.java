/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.scheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quartz.Scheduler;

/**
 * Produces a <a href="http://www.opensymphony.com/quartz/Quartz</a>
 * {@link Scheduler} which automatically loads all the triggers it can find.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2.1
 */
public class ThreadPool {

    private final static Logger log = LoggerFactory.getLogger(ThreadPool.class);

    private final LinkedBlockingQueue<Runnable> queue;

    private final ThreadFactory factory;

    private final ExecutorService executor;

    public ThreadPool(int minThreads, int maxThreads, long msTimeout) {
        queue = new LinkedBlockingQueue<Runnable>();
        factory = null;
        executor = new ThreadPoolExecutor(minThreads, maxThreads, msTimeout,
                TimeUnit.MILLISECONDS, queue); // factory
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public int size() {
        return queue.size();
    }

}
