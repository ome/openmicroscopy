/*
 * Copyright (C) 2012 Glencoe Software, Inc. All rights reserved.
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

import Ice.Current;

import omero.ServerError;
import omero.cmd.CmdCallbackPrx;
import omero.cmd.CmdCallbackPrxHelper;
import omero.cmd.HandlePrx;
import omero.cmd.Response;
import omero.cmd._CmdCallbackDisp;

/**
 * Callback servant which can be used to wait on a command
 * to finish processing. Standard usage would include passing
 * the handle returned from submit to the instance and then
 * either calling block until its finished or by subclassing
 * and handling whatever action in one of the methods.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.4
 */
public class CmdCallbackI extends _CmdCallbackDisp {

    private static final long serialVersionUID = 1L;

    private final Ice.ObjectAdapter adapter;

    private final Ice.Identity id;

    private final CountDownLatch latch = new CountDownLatch(1);

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
        Response rsp = handle.getResponse();
        if (rsp != null) {
            finished(rsp); // Only time that current should be null.
        }
    }

    //
    // Local invocations
    //

    /**
     * Blocks for the given number of milliseconds unless either {@link #cancelled(Response, Current)}
     * or {@link #finished(Response, Current)} has been called in which case
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
     * Called periodically by the server to signal that processing is
     * moving forward. Default implementation does nothing.
     */
    public void step(int complete, int total, Current __current) {
        // no-op
    }

    /**
     * Called when cancelled successfully, i.e. handle.cancel would return
     * true.
     */
    public final void cancelled(Response rsp, Current __current) {
        latch.countDown();
        onCancelled(rsp, __current);
    }

    /**
     * Method intended to be overridden by subclasses. Default logic does
     * nothing.
     */
    protected void onCancelled(Response rsp, Current __current) {
        // no-op
    }

    /**
     * Called when the command has completed with anything other than
     * a cancellation.
     */
    public final void finished(Response rsp, Current __current) {
        latch.countDown();
        onFinished(rsp, __current);
    }

    /**
     * Method intended to be overridden by subclasses. Default logic does
     * nothing.
     */
    public void onFinished(Response rsp, Current __current) {
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
