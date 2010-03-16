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

    private final omero.client client;

    private final ProcessPrx proc;

    private final Ice.Identity id;

    private final BlockingQueue<Action> q = new LinkedBlockingQueue<Action>();

    public ProcessCallbackI(omero.client client, ProcessPrx process)
            throws ServerError {
        this.client = client;
        this.proc = process;
        this.id = new Ice.Identity(UUID.randomUUID().toString(),
                "ProcessCallback");
        Ice.ObjectPrx prx = this.client.getAdapter().add(this, id);
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
         this.client.getAdapter().remove(id);
     }
}
