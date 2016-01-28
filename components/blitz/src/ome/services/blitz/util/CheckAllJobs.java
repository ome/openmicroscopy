/*
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ome.services.blitz.fire.TopicManager;
import ome.services.util.Executor;
import ome.tools.spring.OnContextRefreshedEventListener;
import ome.util.SqlAction;
import omero.constants.categories.PROCESSORCALLBACK;
import omero.constants.topics.PROCESSORACCEPTS;
import omero.grid.ProcessorCallbackPrx;
import omero.grid.ProcessorCallbackPrxHelper;
import omero.grid.ProcessorPrx;
import omero.grid.ProcessorPrxHelper;
import omero.grid._ProcessorCallbackDisp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.transaction.annotation.Transactional;

import Ice.Current;
import Ice.ObjectAdapter;

/**
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2
 */
public class CheckAllJobs extends OnContextRefreshedEventListener {

    private static final Logger log = LoggerFactory.getLogger(CheckAllJobs.class);

    private final Executor ex;

    private final ObjectAdapter oa;

    private final TopicManager tm;

    private final Ice.Identity id;

    private final long waitMs;

    public CheckAllJobs(Executor ex, ObjectAdapter oa, TopicManager tm) {
        this(ex, oa, tm, 10000);
    }

    public CheckAllJobs(Executor ex, ObjectAdapter oa, TopicManager tm,
            long waitMs) {
        this.waitMs = waitMs;
        this.ex = ex;
        this.oa = oa;
        this.tm = tm;
        this.id = new Ice.Identity(UUID.randomUUID().toString(),
                PROCESSORCALLBACK.value);
    }

    @Override
    public void handleContextRefreshedEvent(ContextRefreshedEvent event) {
        run();
    }

    public void run() {
        Callback cb = new Callback();
        Ice.ObjectPrx prx = oa.add(cb, id); // OK ADAPTER USAGE
        ProcessorCallbackPrx cbPrx = ProcessorCallbackPrxHelper.uncheckedCast(prx);
        tm.onApplicationEvent(new TopicManager.TopicMessage(this,
                PROCESSORACCEPTS.value, new ProcessorPrxHelper(),
                "requestRunning", cbPrx));

        new Thread() {
            @Override
            public void run() {
                log.info("Waiting " + waitMs / 1000 + " secs. for callbacks");
                long start = System.currentTimeMillis();
                while (System.currentTimeMillis() < (start + waitMs)) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // ok
                    }
                }
                synchronizeJobs();
            }
        }.start();
    }

    public void synchronizeJobs() {

        final Callback cb = (Callback) oa.find(id);
        final List<Long> ids = new ArrayList<Long>();
        synchronized (cb.openJobs) {
            ids.addAll(cb.openJobs);
        }

        try {
            ex.executeSql(new Executor.SimpleSqlWork(this,
                    "synchronizeJobs") {
                @Transactional(readOnly = false)
                public Object doWork(SqlAction sql) {
                    int count = sql.synchronizeJobs(ids);
                    if (count > 0) {
                        log.warn("Forcibly closed " + count
                                + " abandoned job(s).");
                    }
                    return null;
                }

            });

        } finally {
            oa.remove(id); // OK ADAPTER USAGE
        }
    }

    private class Callback extends _ProcessorCallbackDisp {

        private final List<Long> openJobs = new ArrayList<Long>();

        public void isAccepted(boolean accepted, String sessionUuid,
                String proxyConn, Current __current) {
            log.error("isAccepted should not have been called");
        }

        public void isProxyAccepted(boolean accepted, String sessionUuid,
                ProcessorPrx procProxy, Current __current) {
            log.error("isProxyAccepted should not have been called");
        }

        public void responseRunning(List<Long> jobIds, Current __current) {
            synchronized (openJobs) {
                if (jobIds != null) {
                    log.info("Received " + jobIds.size() + " job(s)");
                    openJobs.addAll(jobIds);
                } else {
                    log.warn("Null jobIds list sent.");
                }
            }
        }

    }

}
