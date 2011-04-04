/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.pixeldata;

import ome.model.meta.EventLog;
import ome.services.eventlogs.EventLogLoader;
import ome.services.util.Executor.SimpleWork;
import ome.system.ServiceFactory;
import ome.util.SqlAction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

/**
 * Simple action which can be done in an asynchronous thread in order to process
 * PIXELDATA event logs.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.3
 */
public class PixelDataHandler extends SimpleWork {

    private final static Log log = LogFactory.getLog(PixelDataHandler.class);

    final protected PersistentEventLogLoader loader;

    protected int reps = 5;

    /**
     * Spring injector. Sets the number of indexing runs will be made if there
     * is a substantial backlog.
     */
    public void setRepetitions(int reps) {
        this.reps = reps;
        ;
    }

    public PixelDataHandler(PersistentEventLogLoader ll) {
        super("PixelDataHandler", "process");
        this.loader = ll;
    }

    /**
     * Since these instances are used repeatedly, we need to check for
     * already set SqlAction
     */
    @Override
    public synchronized void setSqlAction(SqlAction sql) {
        if (getSqlAction() == null) {
            super.setSqlAction(sql);
        }
    }

    @Transactional(readOnly = false)
    public Object doWork(Session session, ServiceFactory sf) {
        int count = 1;
        int perbatch = 0;
        long start = System.currentTimeMillis();
        do {
            process();
            count++;
        } while (doMore(count));
        if (perbatch > 0) {
            log.info(String.format("INDEXED %s objects in %s batch(es) [%s ms.]",
                    perbatch, (count - 1), (System.currentTimeMillis() - start)));
        } else {
            log.debug("No objects indexed");
        }
        return null;
    }

    public int process() {

        int count = 0;

        for (EventLog eventLog : loader) {
            if (eventLog != null) {
                // Here we assume that our log loader will only return
                // use the proper types, since we are using the speciifc
                // type defined in this package.
                Long id= eventLog.getEntityId();
                if (id != null) {
                    log.warn("HANDLING " + id);
                    count++;
                }
            }

        }
        return count;
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
        if (count < this.reps && loader.more() > loader.getBatchSize() * 100) {
            log.info(String
                    .format("Suggesting round %s of "
                            + "processing to reduce backlog of %s:", count,
                            loader.more()));
            return true;
        }
        return false;
    }

}
