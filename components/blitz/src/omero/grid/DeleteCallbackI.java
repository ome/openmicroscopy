/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omero.grid;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import omero.ServerError;
import omero.api.delete.DeleteHandlePrx;

/**
 * Callback used for waiting until {@link DeleteHandlePrx} will return true on
 * {@link DeleteHandlePrx#finished()}. The {@link #block(long)} method will wait
 * the given number of milliseconds and then return the number of errors if any
 * or null if the delete is not yet complete.
 *
 * Example usage:
 *
 * <pre>
 * DeleteCallbackI cb = new DeleteCallbackI(client, handle);
 * Integer errors = null;
 * while (errors == null) {
 *     errors = cb.block(500);
 * }
 * </pre>
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2.1
 */
public class DeleteCallbackI {

    @SuppressWarnings("unused")
    private final Ice.ObjectAdapter adapter;

    @SuppressWarnings("unused")
    private final Ice.Identity id;

    @SuppressWarnings("unused")
    private final BlockingQueue<Integer> q = new LinkedBlockingQueue<Integer>();

    private final boolean poll;

    /**
     * Proxy passed to this instance on creation. Can be used by subclasses
     * freely. The object will not be nulled, but may be closed server-side.
     */
    protected final DeleteHandlePrx handle;

    public DeleteCallbackI(omero.client client, DeleteHandlePrx handle)
            throws ServerError {
        this(client.getAdapter(), handle, true);
    }

    // Marked private; will be used in case a future implementation wants to
    // pass itself in as a callback
    @SuppressWarnings("unused")
    private DeleteCallbackI(omero.client client, DeleteHandlePrx handle,
            boolean poll) throws ServerError {
        this(client.getAdapter(), handle, poll);
    }

    // Marked private; will be used in case a future implementation wants to
    // pass itself in as a callback
    @SuppressWarnings("unused")
    private DeleteCallbackI(Ice.ObjectAdapter adapter, DeleteHandlePrx handle)
            throws ServerError {
        this(adapter, handle, true);
    }

    // Marked private; will be used in case a future implementation wants to
    // pass itself in as a callback
    @SuppressWarnings("unused")
    private DeleteCallbackI(Ice.ObjectAdapter adapter, DeleteHandlePrx handle,
            boolean poll) throws ServerError {
        this.poll = poll;
        this.handle = handle;
        this.adapter = adapter;
        this.id = new Ice.Identity(UUID.randomUUID().toString(),
                "DeleteHandleCallback");
        // Ice.ObjectPrx prx = adapter.add(this, id);
        // ProcessCallbackPrx cb = ProcessCallbackPrxHelper.uncheckedCast(prx);
        // process.registerCallback(cb);
    }

    /**
     *
     * Should only be used if the default logic of the process methods is kept
     * in place. If "q.put" does not get called, this method will always block
     * for the given milliseconds.
     *
     * @param ms
     * @return
     * @throws InterruptedException
     */
    public Integer block(long ms) throws InterruptedException {
        if (poll) {
            try {
                if (handle.finished()) {
                    return handle.errors();
                }
            } catch (Exception e) {
                System.err.println("Error polling DeleteHandle:" + handle);
                e.printStackTrace();
            }
        }
        return null; // q.poll(ms, TimeUnit.MILLISECONDS);
    }

    public void close() {
        // adapter.remove(id); // OK ADAPTER USAGE
    }
}
