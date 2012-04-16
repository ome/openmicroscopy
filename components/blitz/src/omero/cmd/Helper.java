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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

import ome.system.ServiceFactory;
import ome.util.SqlAction;

import omero.cmd.HandleI.Cancel;

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

    private static final Log log = LogFactory.getLog(Helper.class);

    private final AtomicReference<Response> rsp = new AtomicReference<Response>();

    private final Request request;

    private final Status status;

    private final ServiceFactory sf;

    private final Session session;

    private final SqlAction sql;
    
    private int assertSteps = 0;
    
    private int assertResponses = 0;

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
    }

    public int getSteps() {
        return status.steps;
    }

    public Response getResponse() {
        return rsp.get();
    }

    /**
     * Set the response if there is not currently run, in which case true
     * is returned. Otherwise, false.
     *
     * @param rsp
     *            Can be null.
     * @return
     */
    public boolean setResponse(Response rsp) {
        return this.rsp.compareAndSet(null, rsp);
    }

    //
    // REPORTING
    // =========================================================================
    //

    public ServiceFactory getServiceFactory() {
        return sf;
    }

    public Session getSession() {
        return session;
    }

    //
    // REPORTING
    // =========================================================================
    //

    /**
     * Converts pairs of values from the varargs list into a map. Any leftover
     * value will be ignored.
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
     * Calls {@link #fail(ERR, String, Map)} with the output of
     * {@link #params(String...)}.
     *
     * @param err
     * @param name
     * @param paramList
     */
    protected void fail(ERR err, final String name, final String... paramList) {
        fail(err, name, params(paramList));
    }

    /**
     * Sets the status.flags and the ERR properties appropriately.
     *
     * @param err
     * @param name
     * @param params
     */
    protected void fail(ERR err, final String name,
            final Map<String, String> params) {
        status.flags.add(State.FAILURE);
        err.category = request.ice_id();
        err.name = name;
        err.parameters = params;
        rsp.set(err);
    }

    /**
     * Calls {@link #cancel(ERR, Throwable, String, Map<String, String>)} with
     * the output of {@link #params(String...)}.
     *
     * See the statement about the return value.
     *
     * @param err
     * @param t
     * @param name
     * @param paramList
     */
    public Cancel cancel(ERR err, Throwable t, final String name,
            final String... paramList) {
        return cancel(err, t, name, params(paramList));
    }

    /**
     * Like {@link #fail(ERR, String, String...)} but also stores the message
     * and stacktrace of the throwable in the parameters and throws a
     * {@link Cancel} exception.
     *
     * A {@link Cancel} is thrown, even though one is also specificed as the
     * return value. This permits:
     * <pre>
     * } catch (Throwable t) {
     *     throw helper.cancel(new ERR(), t);
     * }
     * </pre>
     * since omitting the "throw" within the catch clauses forces one to enter
     * a number of "return null; // Never reached" lines.
     *
     * @param err
     * @param t
     * @param name
     * @param params
     */
    public Cancel cancel(ERR err, Throwable t, final String name,
            final Map<String, String> params) {
        fail(err, name, params);

        Cancel cancel = new Cancel(name);
        if (t != null) {

            String msg = t.getMessage();
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            String st = pw.toString();
            err.parameters.put("message", msg);
            err.parameters.put("stacktrace", sw.toString());

            cancel.initCause(t);

        }
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
     * 
     * @param i
     * @param step
     * @return
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
     */
    public void assertStep(int step) {
        assertStep(this.assertSteps, step);
        this.assertSteps++;
    }
    
    /**
     * Checks the given steps against the number of times that this method
     * has been called on this instance and then increments the call count.
     */
    public void assertResponse(int step) {
        assertStep(this.assertResponses, step);
        this.assertResponses++;
    }

    /**
     * Returns true if this is the last step for the given request.
     */
    public boolean isLast(int step) {
        return step == (status.steps - 1);
    }
}
