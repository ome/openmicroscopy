/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.util;

import ome.conditions.SessionException;
import ome.model.meta.Session;
import ome.services.sessions.SessionManager;
import ome.system.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * Thread which can be started and will appropriately acquire a session, then
 * use the {@link Executor} to complete its work.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public abstract class ExecutionThread implements Runnable {

    private final static Logger log = LoggerFactory.getLogger(ExecutionThread.class);

    final protected SessionManager manager;
    final protected Executor executor;
    final protected Executor.Work work;
    final protected Principal principal;
    private Principal sessionPrincipal = null;
    private Session session = null;

    /**
     * Main constructor. No arguments can be null.
     */
    public ExecutionThread(SessionManager manager, Executor executor,
            Executor.Work work, Principal principal) {
        Assert.notNull(manager);
        Assert.notNull(executor);
        Assert.notNull(work);
        Assert.notNull(principal);
        this.manager = manager;
        this.executor = executor;
        this.work = work;
        this.principal = principal;
    }

    /**
     * Initializes the {@link Session} for this {@link Thread} if necessary,
     * then calls {@link #doRun()}.
     */
    public final void run() {
        sessionInit();
        doRun();
    }

    public final Principal getPrincipal() {
        return sessionPrincipal;
    }

    /**
     */
    public abstract void doRun();

    protected final void sessionInit() {

        if (sessionPrincipal != null) {
            try {
                this.manager.getEventContext(sessionPrincipal);
            } catch (SessionException e) {
                sessionPrincipal = null;
            }
        }

        if (sessionPrincipal == null) {
            session = this.manager.createWithAgent(principal, "ExecutionThread", null);
            sessionPrincipal = new Principal(session.getUuid(), principal
                    .getGroup(), principal.getEventType());
        }
    }

}
