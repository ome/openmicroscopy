/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package omero.cmd;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import omero.LockTimeout;
import omero.ServerError;

import Ice.Current;
import Ice.ObjectNotExistException;
/**
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.4
 */
public class CmdCallbackI {

    private final static Logger logger = Logger.getLogger(CmdCallbackI.class.getName());

    private final omero.client client;

    private final BlockingQueue<Response> q = new LinkedBlockingQueue<Response>();

    /**
     * Proxy passed to this instance on creation. Can be used by subclasses
     * freely. The object will not be nulled, but may be closed server-side.
     */
    protected final HandlePrx handle;

    public CmdCallbackI(omero.client client, HandlePrx handle) {
        this.client = client;
        this.handle = handle;
    }

    /**
     * Calls {@link #block(long)} "loops" number of times with the "ms"
     * argument. This means the total wait time for the delete to occur
     * is: loops X ms. Sensible values might be 10 loops for 500 ms, or
     * 5 seconds.
     *
     * @param loops Number of times to call {@link #block(long)}
     * @param ms Number of milliseconds to pass to {@link #block(long)
     * @throws omero.LockTimeout if {@link #block(long)} does not return
     *  a non-null value after loops calls.
     */
    public Response loop(int loops, long ms) throws LockTimeout {

        int count = 0;
        Response rsp = null;
        while (rsp == null && count < loops) {
            try {
                rsp = block(ms);
            } catch (InterruptedException e) {
                // continue;
            }
            count++;
        }
        if (rsp == null) {
            int waited = (int) (ms / 1000) * loops;
            throw new LockTimeout(null, null,
                    String.format("Handle unfinished after %s seconds",
                            loops, ms), 5000L, waited);
        }
        return rsp;
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
    public Response block(long ms) throws InterruptedException {
        try {
            return handle.getResponse();
        } catch (ObjectNotExistException onee) {
            omero.ClientError ce = new omero.ClientError("Handle is gone!");
            ce.initCause(onee);
            throw ce;
        } catch (Exception e) {
            logger.log(Level.SEVERE,
                    "Error polling HandlePrx:" + handle, e);
            return null;
        } finally {
            TimeUnit.MILLISECONDS.sleep(ms);
        }
    }

    public void close() {
        // adapter.remove(id); // OK ADAPTER USAGE
        try {
            handle.close();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error calling HandlePrx.close:"
                    + handle, e);
        }
    }

}
