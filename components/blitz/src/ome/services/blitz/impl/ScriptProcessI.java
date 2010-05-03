/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

import java.util.Map;
import java.util.UUID;

import ome.services.procs.scripts.ScriptProcess;
import omero.RInt;
import omero.RType;
import omero.ServerError;
import omero.api.JobHandlePrx;
import omero.constants.categories.PROCESSCALLBACK;
import omero.grid.InteractiveProcessorI;
import omero.grid.InteractiveProcessorPrx;
import omero.grid.ProcessCallbackI;
import omero.grid.ProcessCallbackPrx;
import omero.grid.ProcessPrx;
import omero.grid.ScriptProcessPrx;
import omero.grid.ScriptProcessPrxHelper;
import omero.grid._ScriptProcessDisp;
import omero.model.ScriptJob;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import Ice.Current;

/**
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2
 */
public class ScriptProcessI extends _ScriptProcessDisp {

    private static Log log = LogFactory.getLog(ScriptProcess.class);

    private final InteractiveProcessorPrx processorPrx;

    private final InteractiveProcessorI processor;

    private final ProcessPrx process;

    private final ProcessCallbackI cb;

    private final ServiceFactoryI sf;

    private final ScriptProcessPrx self;

    private final Ice.Identity id;

    private final long jobId;

    public ScriptProcessI(ServiceFactoryI sf, Ice.Current current,
            InteractiveProcessorPrx processorPrx, InteractiveProcessorI processor, ProcessPrx process)
            throws ServerError {
        this.jobId = processor.getJob().getId().getValue();
        this.processorPrx = processorPrx;
        this.processor = processor;
        this.process = process;
        this.sf = sf;
        this.cb = new ProcessCallbackI(sf.getAdapter(), process);
        this.id = new Ice.Identity(UUID.randomUUID().toString(), PROCESSCALLBACK.value);
        this.self = ScriptProcessPrxHelper.uncheckedCast(
                sf.registerServant(this.id, this));
        sf.allow(this.self);
    }

    public ScriptProcessPrx getProxy() {
        return self;
    }

    // Processor delegates
    // =========================================================================

    public void close(boolean detach, Current __current) throws ServerError {
        processor.setDetach(detach, __current);
        processor.stop(__current);
        sf.unregisterServant(processorPrx.ice_getIdentity());
        sf.unregisterServant(self.ice_getIdentity());
        this.cb.close();
    }

    public ScriptJob getJob(Current __current) throws ServerError {
        return (ScriptJob) processor.getJob(__current);
    }

    public Map<String, RType> getResults(int waitSecs, Current __current)
            throws ServerError {

        if (waitSecs > 5 || 0 > waitSecs) {
            throw new omero.ApiUsageException(null, null,
                    "Refusing to wait more than 5 seconds: " + waitSecs);
        }

        try {
            cb.block(waitSecs * 1000);
        } catch (InterruptedException e) {
            // ok
        }
        return processor.getResults(process, __current).getValue();
    }

    public String setMessage(String message, Current __current)
            throws ServerError {

        JobHandlePrx jh = sf.createJobHandle();
        try {
            jh.attach(jobId);
            return jh.setMessage(message);
        } finally {
            jh.close();
        }
    }

    // Process delegates
    // =========================================================================

    public int _wait(Current __current) throws ServerError {
        return process._wait();
    }

    public boolean cancel(Current __current) throws ServerError {
        return process.cancel();
    }

    public boolean kill(Current __current) {
        return process.kill();
    }

    public RInt poll(Current __current) throws ServerError {
        return process.poll();
    }

    public void registerCallback(ProcessCallbackPrx cb, Current __current)
            throws ServerError {
        process.registerCallback(cb);
    }

    public void shutdown(Current __current) {
        process.shutdown();
    }

    public void unregisterCallback(ProcessCallbackPrx cb, Current __current)
            throws ServerError {
        process.unregisterCallback(cb);
    }

}
