/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.procs;

import java.util.HashSet;
import java.util.Set;

import ome.conditions.ApiUsageException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class ProcessSkeleton implements Process {

    private final Logger log;

    private final Set<ProcessCallback> cbs = new HashSet<ProcessCallback>();

    private final Processor processor;

    private boolean cancelled, finished;

    public ProcessSkeleton(Processor p) {
        this.processor = p;
        this.log = LoggerFactory.getLogger(this.getClass());
    }

    public Processor processor() {
        return this.processor;
    }

    public void registerCallback(ProcessCallback cb) {
        synchronized (cbs) {
            checkState();
            boolean added = cbs.add(cb);
            debug("Added", cb, added);
        }
    }

    public void unregisterCallback(ProcessCallback cb) {
        synchronized (cbs) {
            checkState();
            boolean removed = cbs.remove(cb);
            debug("removed", cb, removed);
        }
    }

    public void cancel() {
        synchronized (cbs) {
            checkState();
            for (ProcessCallback cb : cbs) {
                try {
                    cb.processCancelled(this);
                } catch (Exception e) {
                    log
                            .warn(String.format("Exception thrown by %s while "
                                    + "cancelling %s. Removing callback.", cb,
                                    this), e);
                    cbs.remove(cb);
                }
            }
            cbs.clear();
            cancelled = true;
            if (log.isDebugEnabled()) {
                log.debug("Process cancelled: " + this);
            }
        }
    }

    public void finish() {
        synchronized (cbs) {
            checkState();
            for (ProcessCallback cb : cbs) {
                try {
                    cb.processFinished(this);
                } catch (Exception e) {
                    log.warn(String.format("Exception throw by %s while "
                            + "finished %s. Removing callback.", cb, this), e);
                    cbs.remove(cb);
                }
            }
            cbs.clear();
            finished = true;
            if (log.isDebugEnabled()) {
                log.debug("Process finished: " + this);
            }

        }
    }

    public boolean isActive() {
        return !cancelled && !finished;
    }

    protected void checkState() {
        if (!isActive()) {
            String state = cancelled ? "cancelled" : "finished";
            throw new ApiUsageException(String.format("Process already %s: %s",
                    state, this));
        }
    }

    private void debug(String action, ProcessCallback cb, boolean added) {
        if (log.isDebugEnabled()) {
            if (added) {
                log.debug(String.format("%s %s to %s", cb, action, this));
            } else {
                log.debug(String
                        .format("%s already %s to %s", cb, action, this));
            }
        }
    }
}
