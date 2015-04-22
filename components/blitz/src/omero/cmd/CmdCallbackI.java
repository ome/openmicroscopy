/*
 * Copyright (C) 2012-2015 Glencoe Software, Inc. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package omero.cmd;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import omero.ServerError;
import Ice.Current;

/**
 *
 * Callback servant used to wait until a HandlePrx would
 * return non-null on getReponse. The server will notify
 * of completion to prevent constantly polling on
 * getResponse. Subclasses can override methods for handling
 * based on the completion status.
 *
 * Example usage:
 * <pre>
 *      cb = new CmdCallbackI(client, handle);
 *      response = null;
 *      while (response == null) {
 *          response = cb.block(500);
 *      }
 *
 *      // or
 *
 *      response = cb.loop(5, 500);
 * </pre>
 *
 * Subclasses which depend on the proper ordering of either initialization
 * or calls to {@link #onFinished(Response, Status, Current)} should make
 * use of the {@link #initializationDone()} and {@link #waitOnInitialization()},
 * or the {@link #onFinishedDone()} and {@link #waitOnFinishedDone()} methods.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.4
 *
 * @see #initializationDone()
 * @see #onFinishedDone()
 */
public class CmdCallbackI extends _CmdCallbackDisp {

    private static class State {
        final Response rsp;
        final Status status;

        State(Response rsp, Status status) {
                this.rsp = rsp;
                this.status = status;
        }
    }

    private static final long serialVersionUID = 1L;

    private final Ice.ObjectAdapter adapter;

    private final Ice.Identity id;

    /**
     * Latch which is released once {@link #finished(Response, Current)} is
     * called. Other methods will block on this value.
     */
    private final CountDownLatch latch = new CountDownLatch(1);

    /**
     * @see #initializationDone()
     * @see #waitOnInitialization()
     */
    private final CountDownLatch isInitialized = new CountDownLatch(1);

    /**
     * @see #onFinishedDone()
     * @see #waitOnFinishedDone()
     */
    private final CountDownLatch isOnFinishedDone = new CountDownLatch(1);

    private final AtomicReference<State> state = new AtomicReference<State>(
            new State(null, null));

    /**
     * Proxy passed to this instance on creation. Can be used by subclasses
     * freely. The object will not be nulled, but may be closed server-side.
     */
    protected final HandlePrx handle;

    public CmdCallbackI(omero.client client, HandlePrx handle)
    throws ServerError {
        this(client.getAdapter(), client.getCategory(), handle);
    }

    public CmdCallbackI(Ice.ObjectAdapter adapter, String category,
            HandlePrx handle)
        throws ServerError {

        this.adapter = adapter;
        this.handle = handle;
        this.id = new Ice.Identity();
        this.id.name = UUID.randomUUID().toString();
        this.id.category = category;
        Ice.ObjectPrx prx = adapter.add(this, id);
        CmdCallbackPrx cb = CmdCallbackPrxHelper.uncheckedCast(prx);
        handle.addCallback(cb);
        // Now check just in case the process exited VERY quickly
        new Thread() {
            public void run() {
                try {
                    poll();
                } catch (Exception e) {
                    // don't throw any exceptions, e. g. if the handle
                    // has already been closed
                    onFinished(null, null, null);
                }
            }
        }.start();
    }

    //
    // Subclass initialization
    //

    /**
     * Subclasses which must perform their own initialization before
     * {@link #onFinished(Response, Status, Current)} is called should
     * call {@link #initializationDone()} once that setup is complete.
     */
    protected void initializationDone() {
        isInitialized.countDown();
    }

    /**
     * Subclasses which must perform their own initialization before
     * {@link #onFinished(Response, Status, Current)} is called should
     * call {@link #waitOnInitialization() before accessing any initialized
     * state.
     */
    protected void waitOnInitialization() {
        try {
            isInitialized.await();
        } catch (InterruptedException ie) {
            // pass
        }
    }

    protected void onFinishedDone() {
        isOnFinishedDone.countDown();
    }

    protected void waitOnFinishedDone() {
        try {
            isOnFinishedDone.await();
        } catch (InterruptedException ie) {
            // pass
        }
    }

    //
    // Local invocations
    //

    /**
     * Returns possibly null Response value. If null, then neither has
     * the remote server nor the local poll method called finish with
     * non-null values.
     */
    public Response getResponse() {
        return state.get().rsp;
    }

    /**
     * Returns possibly null Status value. If null, then neither has
     * the remote server nor the local poll method called finish with
     * non-null values.
     */
    public Status getStatus() {
        return state.get().status;
    }

    protected Status getStatusOrThrow() {
        Status s = getStatus();
        if (s == null) {
            throw new omero.ClientError("Status not present!");
        }
        return s;
    }

    /**
     * Returns whether Status::CANCELLED is contained in
     * the flags variable of the Status instance. If no
     * Status is available, a ClientError will be thrown.
     */
    public boolean isCancelled() {
        Status s = getStatusOrThrow();
        return s.flags.contains(omero.cmd.State.CANCELLED);
    }

    /**
     * Returns whether Status::FAILURE is contained in
     * the flags variable of the Status instance. If no
     * Status is available, a ClientError will be thrown.
     */
    public boolean isFailure() {
        Status s = getStatusOrThrow();
        return s.flags.contains(omero.cmd.State.FAILURE);
    }

    /**
     * Calls block(long) "loops" number of times with the "ms"
     * argument. This means the total wait time for the action to occur
     * is: loops X ms. Sensible values might be 10 loops for 500 ms, or
     * 5 seconds.
     *
     * @param loops Number of times to call block(long)
     * @param ms Number of milliseconds to pass to block(long
     * @throws omero.LockTimeout if block(long) does not return
     * a non-null value after loops calls.
     */
    public Response loop(int loops, long ms) throws InterruptedException,
        omero.LockTimeout {

        int count = 0;
        boolean found = false;
        while (count < loops) {
            count++;
            found = block(ms);
            if (found) {
                break;
            }
        }

        if (found) {
            return getResponse();
        } else {
            double waited = (ms/1000.0) * loops;
            throw new omero.LockTimeout(null, null,
                String.format("Command unfinished after %s seconds",
                    waited), 10000, (int) waited);
        }
    }

    /**
     * Blocks for the given number of milliseconds unless
     * {@link #finished(Response, Status, Current)} has been called in which case
     * it returns immediately with true. If false is returned, then the timeout
     * was reached.
     *
     * @param ms Milliseconds which this method should block for.
     * @return
     * @throws InterruptedException
     */
    public boolean block(long ms) throws InterruptedException {
        return latch.await(ms, TimeUnit.MILLISECONDS);
    }

    //
    // Remote invocations
    //

    /**
     * Calls {@link HandlePrx#getResponse} in order to check for a non-null
     * value. If so, {@link Handle#getStatus} is also called, and the two
     * non-null values are passed to
     * {@link #finished(Response, Status, Current)}. This should typically
     * not be used. Instead, favor the use of block and loop.
     *
     */
    public void poll() {
        Response rsp = handle.getResponse();
        if (rsp != null) {
            Status s = handle.getStatus();
            finished(rsp, s, null); // Only time that current should be null.
        }
    }

    /**
     * Called periodically by the server to signal that processing is
     * moving forward. Default implementation does nothing.
     */
    public void step(int complete, int total, Current __current) {
        // no-op
    }

    /**
     * Called when the command has completed.
     */
    public final void finished(Response rsp, Status status, Current __current) {
        state.set(new State(rsp, status));
        latch.countDown();
        onFinished(rsp, status, __current);
    }

    /**
     * Method intended to be overridden by subclasses. Default logic does
     * nothing.
     */
    public void onFinished(Response rsp, Status status, Current __current) {
        // no-op
    }

    /**
     * First removes self from the adapter so as to no longer receive
     * notifications, and the calls close on the remote handle if requested.
     */
    public void close(boolean closeHandle) {
         adapter.remove(id); // OK ADAPTER USAGE
         if (closeHandle) {
             handle.close();
         }
    }
}
