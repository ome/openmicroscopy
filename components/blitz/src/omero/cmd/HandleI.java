/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omero.cmd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import ome.api.local.LocalAdmin;
import ome.conditions.InternalException;
import ome.services.util.Executor;
import ome.system.EventContext;
import ome.system.Principal;
import ome.system.ServiceFactory;
import ome.util.SqlAction;
import omero.LockTimeout;
import omero.ServerError;

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
public class HandleI implements _HandleOperations, IHandle,
       SessionAware {

    private static enum State {
        CREATED, READY, RUNNING, CANCELLING, CANCELLED, FINISHED;
    }

    private static final long serialVersionUID = 15920349984928755L;

    /**
     * Timeout in seconds that cancellation should wait.
     */
    private final int cancelTimeoutMs;

    /**
     * Callbacks that have been added by clients.
     */
    private final ConcurrentHashMap<String, CmdCallbackPrx> callbacks =
        new ConcurrentHashMap<String, CmdCallbackPrx>();

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
     * Current step. should only be incremented during {@link #steps(SqlAction, Session, ServiceFactory)}.
     */
    private final AtomicInteger currentStep = new AtomicInteger();

    /**
     * Status which will be returned by {@link #getStatus()} and as a part of
     * the {@link Response} value.
     */
    private final Status status = new Status();

    /**
     * Context to be passed to
     * {@link Executor#execute(Map, Principal, ome.services.util.Executor.Work)}
     * for properly setting the call context.
     */
    private final Map<String, String> callContext;

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

    private/* final */Helper helper;

    //
    // INTIALIZATION
    //

    /**
     * Calls {@link #HandleI(int, Map)} with a null call context.
     */
    public HandleI(int cancelTimeoutMs) {
        this(cancelTimeoutMs, null);
    }

    /**
     * Create and
     *
     * @param cancelTimeoutMs
     */
    public HandleI(int cancelTimeoutMs, Map<String, String> callContext) {
        this.cancelTimeoutMs = cancelTimeoutMs;
        this.callContext = callContext;
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
        this.helper = new Helper((Request)req, status, null, null, null);
    }

    //
    // CALLBACKS
    //

    public void addCallback(CmdCallbackPrx cb, Current __current) {
        Ice.Identity id = cb.ice_getIdentity();
        String key = Ice.Util.identityToString(id);
        helper.info("Add callback: %s", key);
        cb = CmdCallbackPrxHelper.checkedCast(cb.ice_oneway());
        callbacks.put(key, cb);
    }

    public void removeCallback(CmdCallbackPrx cb, Current __current) {
        Ice.Identity id = cb.ice_getIdentity();
        String key = Ice.Util.identityToString(id);
        helper.info("Remove callback: %s", key);
        cb = CmdCallbackPrxHelper.checkedCast(cb.ice_oneway());
        callbacks.remove(key);
    }

    /**
     * Calls the proper notification on all callbacks based on the current
     * {@link #state}. If the {@link State} is anything other than
     * {@link State#CANCELLED} or {@link State#FINISHED} then
     * {@link CmdCallbackPrx#step(int, int)} is called.
     */
    public void notifyCallbacks() {
        final State state = this.state.get();
        final boolean finished = state.equals(State.FINISHED);
        final boolean cancelled = state.equals(State.CANCELLED);
        for (final CmdCallbackPrx prx : callbacks.values()) {
            try {
                Response rsp = this.rsp.get();
                if (finished || cancelled) {
                    if (cancelled) {
                        helper.info("notify cancelled: %s/%s", rsp, status);
                    } else {
                        helper.info("notify finished: %s/%s", rsp, status);
                    }
                    prx.finished(rsp, status);
                } else {
                    int step = currentStep.get();
                    helper.info("notify step %s of %s", step, status.steps);
                    prx.step(step, status.steps);
                }
            } catch (Exception e) {
                sess.handleCallbackException(e);
            }
        }
    }

    //
    // GETTERS
    //

    public Request getRequest(Current __current) {
        helper.info("getRequest: %s",  req);
        return (Request) req;
    }

    public Response getResponse(Current __current) {
        Response rsp = this.rsp.get();
        helper.info("getResponse: %s", rsp);
        return rsp;
    }

    public Status getStatus(Current __current) {
        helper.info("getStatus: %s", status);
        return status;
    }

    //
    // STATE MGMT
    //

    public boolean cancel(Current __current) throws LockTimeout {
        try {
            boolean cancelled = cancelWithoutNotification();
            if (cancelled) {
                status.flags.add(omero.cmd.State.CANCELLED);
                helper.info("Cancelled.");
            }
            return cancelled;
        } finally {
            rsp.compareAndSet(null, new ERR());
            notifyCallbacks();
        }
    }

    private boolean cancelWithoutNotification() throws LockTimeout {

        helper.info("Cancelling...");

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
                helper.warn("Can't reset to RUNNING. State already changed.\n"
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
        sess.unregisterServant(id); // No exception
        try {
            closeWithoutNotification(current);
        } finally {
            notifyCallbacks();
        }
    }

    private void closeWithoutNotification(Ice.Current current) {
        helper.info("Closing...");
        final State s = state.get();
        if (!State.FINISHED.equals(s) && !State.CANCELLED.equals(s)) {
            helper.info("Handle closed before finished! State=" + state.get());
            try {
                cancel(current);
            }
            catch (LockTimeout e) {
                helper.warn("Cancel failed");
            }
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
            Map<String, String> merged = mergeContexts();

            @SuppressWarnings("unchecked")
            List<Object> rv = (List<Object>) executor.execute(merged, principal,
                    new Executor.SimpleWork(this, "run",
                    Ice.Util.identityToString(id), req) {
                @Transactional(readOnly = false)
                public List<Object> doWork(Session session, ServiceFactory sf) {

                        EventContext ec = ((LocalAdmin) sf.getAdminService())
                            .getEventContextQuiet();
                        // Mimics EventHandler's logging of "Auth:"
                        helper.info(" Auth:\tuid=%s,gid=%s,eid=%s,sess=%s%s",
                            ec.getCurrentUserId(),
                            ec.getCurrentGroupId(),
                            ec.getCurrentEventId(),
                            ec.getCurrentSessionUuid(),
                            (ec.getCurrentShareId() == null) ? "" :
                                    "share:%s" + ec.getCurrentShareId());

                    try {
                        List<Object> rv = doRun(getSqlAction(), session, sf);
                        state.set(State.FINISHED); // Regardless of current
                        return rv;
                    } catch (Cancel c) {
                        // TODO: Perhaps remove local State enum and use solely
                        // the slice defined one.
                        status.flags.add(omero.cmd.State.CANCELLED);
                        state.set(State.CANCELLED);
                        throw c; // Exception intended to rollback transaction
                    }
                }
            });

            // Post-process
            for (int step = 0; step < status.steps; step++) {
                Object obj = rv.get(step);
                req.buildResponse(step, obj);
            }

        } catch (Throwable t) {
            if (t instanceof Cancel) {
                helper.debug("Request cancelled by %s", t.getCause());
            } else {
                helper.debug("Request rolled back by %s", t.getCause());
            }
        } finally {
            rsp.set(req.getResponse());
            sw.stop("omero.request.tx");
            notifyCallbacks();
        }
    }

    private Map<String, String> mergeContexts() {

        final Map<String, String> merged = new HashMap<String, String>();
        final Map<String, String> reqCctx = req.getCallContext();

        if (callContext != null) {
            merged.putAll(callContext);
        }

        if (reqCctx != null) {
            merged.putAll(reqCctx);
        }

        return merged;
    }

    public List<Object> doRun(SqlAction sql, Session session, ServiceFactory sf) throws Cancel {
        StopWatch sw = new CommonsLogStopWatch();
        try {
            return steps(sql, session, sf);
        } finally {
            sw.stop("omero.request");
            status.startTime = sw.getStartTime();
            status.stopTime = sw.getStartTime() + sw.getElapsedTime();
        }
    }

    public List<Object> steps(SqlAction sql, Session session, ServiceFactory sf) throws Cancel {
        try {

            // Initialize. Any exceptions should cancel the process
            List<Object> rv = new ArrayList<Object>();
            StopWatch sw = new CommonsLogStopWatch();
            // Now that we're in the transaction, replace the helper.
            helper = new Helper((Request)req, status, sql, session, sf);
            req.init(helper);

            int j = 0;
            while (j < status.steps) {
                sw = new CommonsLogStopWatch();
                try {
                    if (!state.compareAndSet(State.READY, State.RUNNING)) {
                        throw new Cancel("Not ready");
                    }
                    rv.add(req.step(j));
                } finally {
                    sw.stop("omero.request.step." + j);
                    // If cancel was thrown, then this value will be overwritten
                    // by the try/catch handler
                    state.compareAndSet(State.RUNNING, State.READY);
                }

                j = currentStep.incrementAndGet(); // SOLE INCREMENT

                // The following would probably be better handled by a
                // background thread, or via the heartbeat mechanism. For
                // the moment, though we'll notify callbacks per decile.

                int numOfCallbacks = 10; // TODO: configurable
                int mod = 1;

                // status.steps == 0 can't happen
                if (status.steps > numOfCallbacks) {
                    mod = (status.steps / numOfCallbacks);
                }

                if ((j % mod) == 0) {
                    notifyCallbacks();
                }

            }
            return rv;
        } catch (Cancel cancel) {
            throw cancel;
        } catch (Throwable t) {
            String msg = "Failure during Request.step:";
            helper.error(t, msg);
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
