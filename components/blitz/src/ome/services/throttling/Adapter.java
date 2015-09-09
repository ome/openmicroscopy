/*
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

package ome.services.throttling;

import ome.services.blitz.util.BlitzExecutor;
import ome.services.util.Executor;
import ome.system.OmeroContext;
import ome.system.Principal;
import omero.util.IceMapper;

import org.springframework.util.Assert;

/**
 * Simple adapter which takes a {@link Executor#Work} instance and executes it
 * as a {@link BlitzExecutor} task. All exceptions are caught and routed to via
 * the {@link #exception(Exception, OmeroContext)} method to the provided callback.
 */
public class Adapter extends Task {

    private final Executor ex;
    private final Executor.Work work;
    private final Principal p;
    private final IceMapper mapper;

    public Adapter(Object callback, Ice.Current current, IceMapper mapper,
            Executor ex, Principal p, Executor.Work work) {
        super(callback, current, mapper.isVoid());
        this.p = p;
        this.ex = ex;
        this.work = work;
        this.mapper = mapper;
        Assert.notNull(callback, "Callback null");
        Assert.notNull(work, "Work null");
        Assert.notNull(ex, "Executor null");
        Assert.notNull(p, "Principal null");
    }

    public void run(OmeroContext ctx) {
        try {
            Object retVal = null;

            // If the work throw an exception, we have to handle it as
            // IceMethodInvoker would.
            try {
                retVal = this.ex.execute(this.p, this.work);
            } catch (Throwable t) {
                Ice.UserException ue = mapper.handleException(t, ex
                        .getContext());
                exception(ue, ex.getContext());
                return; // EARLY EXIT
            }

            // Any exception thrown now will be thrown as is.
            if (mapper != null && mapper.canMapReturnValue()) {
                retVal = mapper.mapReturnValue(retVal);
            }
            response(retVal, ctx);

        } catch (Exception e) {
            exception(e, ctx);
        }

    }

}