/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.pixeldata;

import ome.services.sessions.SessionManager;
import ome.services.util.ExecutionThread;
import ome.services.util.Executor;
import ome.system.Principal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.3
 */
public class PixelDataThread extends ExecutionThread {

    private final static Log log = LogFactory.getLog(PixelDataThread.class);

    private final static Principal DEFAULT_PRINCIPAL = new Principal("root",
            "system", "Task");

    /**
     * Uses default {@link Principal} for processing
     */
    public PixelDataThread(SessionManager manager, Executor executor,
            PixelDataHandler handler) {
        this(manager, executor, handler, DEFAULT_PRINCIPAL);
    }

    /**
     * Main constructor. No arguments can be null.
     */
    public PixelDataThread(SessionManager manager, Executor executor,
            PixelDataHandler handler, Principal principal) {
        super(manager, executor, handler, principal);
    }

    /**
     * Called by Spring on creation. Currently a no-op.
     */
    public void start() {
        log.info("Initializing PixelDataThread");
    }

    /**
     */
    @Override
    public void doRun() {
        this.executor.execute(getPrincipal(), work);
    }

    /**
     * Called by Spring on destruction.
     */
    public void stop() {
        log.info("Shutting down PixelDataThread");
    }
}