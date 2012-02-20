/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omero.cmd;

import java.util.concurrent.atomic.AtomicReference;

import ome.conditions.InternalException;
import ome.services.util.Executor;
import ome.system.Principal;
import ome.system.ServiceFactory;
import ome.util.SqlAction;
import omero.LockTimeout;
import omero.ServerError;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.perf4j.StopWatch;
import org.perf4j.commonslog.CommonsLogStopWatch;
import org.springframework.transaction.annotation.Transactional;

import Ice.Current;
import Ice.Identity;

/**
 * Servant for the handle proxy from the Command API. This is also a
 * {@link Runnable} and is passed to a ThreadPool instance
 *
 * <pre>
 * Transitions:
 *
 *      +------------------o [FINISHED]
 *      |                        o
 *      |                        |
 * (CREATED) ---o READY o===o RUNNING o===o CANCELLING ---o [CANCELLED]
 *      |           |                           o                o
 *      |           |---------------------------|                |
 *      +--------------------------------------------------------+
 *
 * </pre>
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.3.2
 */
public class HandleI extends _HandleDisp implements IHandle,
        SessionAware {

    private static enum State {
        CREATED, READY, RUNNING, CANCELLING, CANCELLED, FINISHED;
    }

    private static final long serialVersionUID = 15920349984928755L;

    private static final Log log = LogFactory.getLog(HandleI.class);

    /**
     * Timeout in seconds that cancellation should wait.
     */
    private final int cancelTimeoutMs;

    /**
     * State-diagram location. This instance is also used as a lock around
     * certain critical sections.
     */
    private final AtomicReference<State> state = new AtomicReference<State>();

    /**
     * Final response. If this value is non-null, then clients will assume that
     * processing is finished.
     */
    private final AtomicReference<Response> rsp = new AtomicReference<Response>();

    /**
     * Status which will be returned by {@link #getStatus()} and as a part of
     * the {@link Response} value.
     */
    private final Status status = new Status();

    /**
     * The principal, i.e. the session information, about the current users
     * logins. This will be passed to the {@link #executor} instance for logging
     * in.
     */
    private/* final */Principal principal;

    /**
     * Executor which will be used to provide access to the Hibernate
     * {@link Session} in a background thread.
     */
    private/* final */Executor executor;

    /**
     * The identity of this servant, used during logging and similar operations.
     */
    private/* final */Ice.Identity id;

    private/* final */SessionI sess;

    private/* final */IRequest req;

    //
    // INTIALIZATION
    //

    /**
     * Create and
     *
     * @param cancelTimeoutMs
     */
    public HandleI(int cancelTimeoutMs) {
        this.cancelTimeoutMs = cancelTimeoutMs;
        this.state.set(State.CREATED);
    }

    public void setSession(SessionI session) throws ServerError {
        this.sess = session;
        this.principal = sess.getPrincipal();
        this.executor = sess.getExecutor();
    }

    public void initialize(Identity id, IRequest req) {
        this.id = id;
        this.req = req;
    }

    //
    // GETTERS
    //

    public Request getRequest(Current __current) {
        return (Request) req;
    }

    public Response getResponse(Current __current) {
        return rsp.get();
    }

    public Status getStatus(Current __current) {
        return status;
    }

    //
    // STATE MGMT
    //

    public boolean cancel(Current __current) throws LockTimeout {

        // If we can successfully catch the CREATED or READY state,
        // then there's no reason to wait for anything else.
        if (state.compareAndSet(State.CREATED, State.CANCELLED)
                || state.compareAndSet(State.READY, State.CANCELLED)) {
            return true;
        }

        long start = System.currentTimeMillis();
        while (cancelTimeoutMs >= (System.currentTimeMillis() - start)) {

            // This is the most important case. If things are running, then
            // we want to set "CANCELLING" as quickly as possible.
            if (state.compareAndSet(State.RUNNING, State.CANCELLING)
                    || state.compareAndSet(State.READY, State.CANCELLING)
                    || state.compareAndSet(State.CANCELLING, State.CANCELLING)) {

                try {
                    Thread.sleep(cancelTimeoutMs / 10);
                } catch (InterruptedException e) {
                    // Igoring the interruption since the while block
                    // will properly handle another iteration.
                }

            }

            // These are end states, so we'll just return.
            // See the transition states in the class javadoc
            if (state.compareAndSet(State.CANCELLED, State.CANCELLED)) {
                return true;

            } else if (state.compareAndSet(State.FINISHED, State.FINISHED)) {
                return false;
            }

        }

        // The only time that state gets set to CANCELLING is in the try
        // block above. If we've exited the while without switching to CANCELLED
        // then we've failed, and the value should be rolled back.
        //
        // If #run() noticed this before hand, then it would already have
        // moved from RUNNING to CANCELLED, and so the following statement would
        // return false, in which case we print out a warning so that in a later
        // version we can be more careful about retrying.
        if (!state.compareAndSet(State.CANCELLING, State.RUNNING)) {
            log.warn("Can't reset to RUNNING. State already changed.\n"
                    + "This could be caused either by another thread having\n"
                    + "already set the state back to RUNNING, or by the state\n"
                    + "having changed to CANCELLED. In either case, it is safe\n"
                    + "to throw the exception, and have the user recall cancel.");
        }

        LockTimeout lt = new LockTimeout();
        lt.backOff = 5000;
        lt.message = "timed out while waiting on CANCELLED state";
        lt.seconds = cancelTimeoutMs / 1000;
        throw lt;

    }

    //
    // CloseableServant. See documentation in interface.
    //

    public void close(Ice.Current current) {
        sess.unregisterServant(id);
        final State s = state.get();
        if (!State.FINISHED.equals(s) && !State.CANCELLED.equals(s)) {
            log.warn("Handle closed before finished! State=" + state.get());
        }
    }

    //
    // Runnable
    //

    /**
     *
     * NB: only executes if {@link #state} is {@link State#CREATED}.
     */
    public void run() {

        // If we're not in the created state, then do nothing
        // since something has gone wrong.
        if (!state.compareAndSet(State.CREATED, State.READY)) {
            return; // EARLY EXIT!
        }

        StopWatch sw = new CommonsLogStopWatch();
        try {
            executor.execute(principal, new Executor.SimpleWork(this, "run",
                    Ice.Util.identityToString(id), req) {
                @Transactional(readOnly = false)
                public Object doWork(Session session, ServiceFactory sf) {
                    try {
                        doRun(getSqlAction(), session, sf);
                        state.compareAndSet(State.READY, State.FINISHED);
                    } catch (Cancel c) {
                        state.set(State.CANCELLED);
                        throw c; // Exception intended to rollback transaction
                    }
                    return null;
                }
            });

        } catch (Throwable t) {
            if (t instanceof Cancel) {
                log.debug("Request cancelled by " + t.getCause());
            } else {
                log.debug("Request rolled back by " + t.getCause());
            }
        } finally {
            rsp.set(req.getResponse());
            sw.stop("omero.request.tx");
        }
    }

    public void doRun(SqlAction sql, Session session, ServiceFactory sf) throws Cancel {
        log.info("Running " + req);
        StopWatch sw = new CommonsLogStopWatch();
        try {
            steps(sql, session, sf);
            req.finish();
        } finally {
            sw.stop("omero.request");
            status.startTime = sw.getStartTime();
            status.stopTime = sw.getStartTime() + sw.getElapsedTime();
        }
    }

    public void steps(SqlAction sql, Session session, ServiceFactory sf) throws Cancel {
        try {

            // Initialize. Any exceptions should cancel the process
            StopWatch sw = new CommonsLogStopWatch();
            req.init(status, sql, session, sf);

            for (int j = 0; j < status.steps; j++) {
                sw = new CommonsLogStopWatch();
                try {
                    if (!state.compareAndSet(State.READY, State.RUNNING)) {
                        throw new Cancel("Not ready");
                    }
                    req.step(j);
                } finally {
                    sw.stop("omero.request.step." + j);
                    // If cancel was thrown, then this value will be overwritten
                    // by the try/catch handler
                    state.compareAndSet(State.RUNNING, State.READY);
                }
            }
        } catch (Cancel cancel) {
            throw cancel;
        } catch (Throwable t) {
            String msg = "Failure during Request.step:";
            log.error(msg, t);
            Cancel cancel = new Cancel("Cancelled by " + t.getClass().getName());
            cancel.initCause(t);
            throw cancel;
        }

    }

    /**
     * Signals that {@link HandleI#run()} has noticed that {@link HandleI#state}
     * wants a cancellation or that the {@link Request} implementation wishes to
     * stop execution.
     */
    public static class Cancel extends InternalException {

        private static final long serialVersionUID = 1L;

        public Cancel(String message) {
            super(message);
        }

    }

}
