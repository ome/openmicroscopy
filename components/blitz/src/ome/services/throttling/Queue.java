/**
 *   Copyright 2008-2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.throttling;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import ome.system.OmeroContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** * Manages AMD-based method dispatches from blitz. * */
public class Queue {

    private final static Logger log = LoggerFactory.getLogger(Queue.class);

    static class CancelledException extends Exception {
    }

    private final OmeroContext ctx;
    private final BlockingQueue<Callback> q = new LinkedBlockingQueue<Callback>();
    private final AtomicBoolean done = new AtomicBoolean();

    public Queue(OmeroContext ctx) {
        done.set(false);
        this.ctx = ctx;
    }

    public void put(Callback callback) {
        boolean cont = !done.get();
        if (cont) {
            while (true) {
                try {
                    q.put(callback);
                    break;
                } catch (InterruptedException e) {
                    log.warn("Queue interrupted during put");
                }
            }
        } else {
            callback.exception(new CancelledException(), ctx);
        }
    }

    public Callback take() {
        Callback cb = null;
        while (true) {
            try {
                cb = q.take();
            } catch (InterruptedException e) {
                log.warn("Queue interrupted during take");
            }
            break;
        }
        return cb;
    }

    public void destroy() {
        boolean wasDone = done.getAndSet(true);
        if (!wasDone) {
            for (Callback cb : q) {
                cb.exception(new CancelledException(), ctx);
            }
        }
    }
}
