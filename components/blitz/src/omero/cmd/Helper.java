/*
 * Copyright 2012 Glencoe Software, Inc. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package omero.cmd;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import ome.api.local.LocalAdmin;
import ome.conditions.InternalException;
import ome.services.graphs.GraphException;
import ome.system.EventContext;
import ome.system.ServiceFactory;
import ome.util.SqlAction;
import omero.ServerError;
import omero.cmd.HandleI.Cancel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Session;

/**
 * Helper object for all omero.cmd implementations.
 *
 * Since the objects must subclass from an Ice implementation
 * repeated implementations which cannot be inherited are
 * provided here.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.4.0
 */
public class Helper {

    private final Logger log;

    private final AtomicReference<Response> rsp = new AtomicReference<Response>();

    private final Request request;

    private final Status status;

    private final ServiceFactory sf;

    private final Session session;

    private final SqlAction sql;

    private int assertSteps = 0;

    private int assertResponses = 0;

    private boolean stepsSet = false;

    public Helper(Request request, Status status, SqlAction sql,
            Session session, ServiceFactory sf) {
        synchronized (status) {
            if (status.flags == null) {
                status.flags = new ArrayList<State>();
            }
        }
        this.request = request;
        this.status = status;
        this.sql = sql;
        this.session = session;
        this.sf = sf;
        this.log = LoggerFactory.getLogger(
            this.request.toString().replaceAll("@", ".@"));
    }

    /**
     * Run {@link IRequest#init(Helper)} on a sub-command by creating a new
     * Helper instance.
     * @param req the request
     * @param substatus its status for its new helper
     * @return the new helper
     */
    public Helper subhelper(Request req, Status substatus) {
        return new Helper(req, substatus, sql, session, sf);
    }

    private void requireStepsSet() {
        if (!stepsSet) {
            cancel(new ERR(), new InternalException("Steps unset!"), "steps-unset");
        }
    }

    public void setSteps(int steps) {
        if (stepsSet) {
            cancel(new ERR(), new InternalException("Steps set!"), "steps-set");
        }
        status.steps = steps;
        stepsSet = true;
        if (steps == 0) {
            cancel(new ERR(), null, "no-steps");
        }
    }

    public int getSteps() {
        requireStepsSet();
        return status.steps;
    }

    public Status getStatus() {
        return status;
    }

    public Response getResponse() {
        return rsp.get();
    }

    /**
     * Set the response if there is not currently run, in which case true
     * is returned. Otherwise, false.
     * @param rsp the response
     * @return if the setting was successful
     */
    public boolean setResponseIfNull(Response rsp) {
        return this.rsp.compareAndSet(null, rsp);
    }

    //
    // Data access
    // =========================================================================
    //

    public ServiceFactory getServiceFactory() {
        return sf;
    }

    public Session getSession() {
        return session;
    }

    public SqlAction getSql() {
        return sql;
    }

    //
    // Logging
    // =========================================================================
    //

    public void debug(String fmt, Object...args) {
        if (log.isDebugEnabled()) {
            log.debug(String.format(fmt, args));
        }
    }

    public void info(String fmt, Object...args) {
        if (log.isInfoEnabled()) {
            log.info(String.format(fmt, args));
        }
    }

    public void warn(String fmt, Object...args) {
        if (log.isWarnEnabled()) {
            log.warn(String.format(fmt, args));
        }
    }

    public void error(String fmt, Object...args) {
        error(null, fmt, args);
    }

    public void error(Throwable t, String fmt, Object...args) {
        if (log.isErrorEnabled()) {
            if (t != null) {
                log.error(String.format(fmt, args), t);
            } else {
                log.error(String.format(fmt, args));
            }
        }
    }

    //
    // REPORTING
    // =========================================================================
    //

    /**
     * Converts pairs of values from the varargs list into a map. Any leftover
     * value will be ignored.
     * @param paramList a list of parameters
     * @return a map with entries constructed from consecutive pairs of the parameters
     */
    public Map<String, String> params(final String... paramList) {
        Map<String, String> params = new HashMap<String, String>();
        for (int i = 0; i < paramList.length-1; i = i + 2) {
            if ((i+1)==paramList.length) {
                break; // Handle the odd remaining item
            }
            params.put(paramList[i], paramList[i + 1]);
        }
        return params;
    }

    /**
     * Calls {@link #fail(ERR, Throwable, String, Map)} with the output of
     * {@link #params(String...)}.
     */
    public void fail(ERR err, Throwable t, final String name, final String... paramList) {
        fail(err, t, name, params(paramList));
    }

    /**
     * Sets the status.flags and the ERR properties appropriately and also
     * stores the message and stacktrace of the throwable in the parameters.
     */
    public void fail(ERR err, Throwable t, final String name,
            final Map<String, String> params) {
        status.flags.add(State.FAILURE);
        err.category = request.ice_id();
        err.name = name;
        if (err.parameters == null) {
            err.parameters = params;
        } else {
            err.parameters.putAll(params);
        }
        if (t != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            String st = sw.toString();
            if (t instanceof ServerError) {
                String msg = ((ServerError) t).message;
                err.parameters.put("message", msg);
            } else {
                String msg = t.getMessage();
                err.parameters.put("message", msg);
            }
            err.parameters.put("stacktrace", st);
        }
        rsp.set(err);
    }

    /**
     * Calls {@link #cancel(ERR, Throwable, String, Map)} with
     * the output of {@link #params(String...)}.
     *
     * See the statement about the return value.
     */
    public Cancel cancel(ERR err, Throwable t, final String name,
            final String... paramList) {
        return cancel(err, t, name, params(paramList));
    }

    /**
     * Like {@link #fail(ERR, Throwable, String, String...)} throws a
     * {@link Cancel} exception.
     *
     * A {@link Cancel} is thrown, even though one is also specified as the
     * return value. This permits:
     * <pre>
     * } catch (Throwable t) {
     *     throw helper.cancel(new ERR(), t);
     * }
     * </pre>
     * since omitting the "throw" within the catch clauses forces one to enter
     * a number of "return null; // Never reached" lines.
     */
    public Cancel cancel(ERR err, Throwable t, final String name,
            final Map<String, String> params) {
        fail(err, t, name, params);
        status.flags.add(State.CANCELLED);
        Cancel cancel = new Cancel(name);
        if (t != null) {
            cancel.initCause(t);
        }
        info("Cancelled");
        throw cancel;
    }

    /**
     * Throws an exception if the current step is not the expected value. The
     * boolean returns value has been added so it can be used as:
     * <pre>
     * public boolean step(int i) {
     *     if (helper.assertStep(0, i)) {
     *         return null;
     *     }
     * </pre>
     * @param i the expected step number
     * @param step the actual step number
     */
    public void assertStep(int i, int step) {
        if (step != i) {
            cancel(new ERR(), null, "bad step",
                    "actual_step", "" + step,
                    "expected_step", "" + i);
        }
    }

    /**
     * Checks the given steps against the number of times that this method
     * has been called on this instance and then increments the call count.
     * @param step the new step number
     */
    public void assertStep(int step) {
        assertStep(this.assertSteps, step);
        this.assertSteps++;
    }

    /**
     * Checks the given steps against the number of times that this method
     * has been called on this instance and then increments the call count.
     * @param step the step number for the new response
     */
    public void assertResponse(int step) {
        assertStep(this.assertResponses, step);
        this.assertResponses++;
    }

    /**
     * Returns true if this is the last step for the given request.
     * @param step the step number
     * @return if the given step is the last
     */
    public boolean isLast(int step) {
        return step == (status.steps - 1);
    }

    /**
     * Provides an {@link EventContext} instance without reloading the session,
     * via {@link LocalAdmin#getEventContextQuiet()}.
     * @return the event context
     */
    public EventContext getEventContext() {
        final LocalAdmin admin = (LocalAdmin) sf.getAdminService();
        final EventContext ec = admin.getEventContextQuiet();
        return ec;
    }
}
