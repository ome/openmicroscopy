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
import java.util.concurrent.TimeUnit;

import omero.ServerError;
import Ice.Current;

/**
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2
 */
public class ProcessCallbackI extends _ProcessCallbackDisp {

    public enum Action {
        FINISHED,
        CANCELLED,
        KILLED;
    }

    private final Ice.ObjectAdapter adapter;

    private final Ice.Identity id;

    private final BlockingQueue<Action> q = new LinkedBlockingQueue<Action>();

    private final boolean poll;

    /**
     * Proxy passed to this instance on creation. Can be used by subclasses
     * freely. The object will not be nulled, but may be closed server-side.
     */
    protected final ProcessPrx process;

    public ProcessCallbackI(ProcessCallbackI pcb) throws ServerError {
        this(pcb.adapter, pcb.id.category, pcb.process);
    }
    
    public ProcessCallbackI(omero.client client, ProcessPrx process)
    throws ServerError {
        this(client, process, true);
    }

    public ProcessCallbackI(omero.client client, ProcessPrx process, boolean poll)
    throws ServerError {
        this(client.getAdapter(), client.getCategory(), process, poll);
    }

    public ProcessCallbackI(Ice.ObjectAdapter adapter, String category,
            ProcessPrx process) throws ServerError {
        this(adapter, category, process, true);
    }

    public ProcessCallbackI(Ice.ObjectAdapter adapter, String category,
            ProcessPrx process, boolean poll)
        throws ServerError {

        this.adapter = adapter;
        this.poll = poll;
        this.process = process;
        this.id = new Ice.Identity();
        this.id.name = UUID.randomUUID().toString();
        this.id.category = category;
        Ice.ObjectPrx prx = adapter.add(this, id);
        ProcessCallbackPrx cb = ProcessCallbackPrxHelper.uncheckedCast(prx);
        process.registerCallback(cb);
    }

    /**
     *
     * Should only be used if the default logic of the process methods is kept
     * in place. If "q.put" does not get called, this method will always
     * block for the given milliseconds.
     *
     * @param ms
     * @return
     * @throws InterruptedException
     */
    public Action block(long ms) throws InterruptedException {
        if (poll) {
            try {
                omero.RInt rc = process.poll();
                if (rc != null) {
                    processFinished(rc.getValue(), null);
                }
            } catch (Exception e) {
                System.err.println("Error calling poll:" + e);
            }
        }
        return q.poll(ms, TimeUnit.MILLISECONDS);
    }

    public void processCancelled(boolean success, Current __current) {
        try {
            q.put(Action.CANCELLED);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void processFinished(int returncode, Current __current) {
        try {
            q.put(Action.FINISHED);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void processKilled(boolean success, Current __current) {
        try {
            q.put(Action.KILLED);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void close() {
         adapter.remove(id); // OK ADAPTER USAGE
     }
}
