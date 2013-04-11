/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.throttling;

import java.util.concurrent.Callable;

import ome.api.ServiceInterface;
import ome.security.basic.CurrentDetails;
import ome.services.blitz.util.IceMethodInvoker;
import omero.util.IceMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Ice.Current;

/**
 * Throttling implementation which uses the calling server {@link Thread} for
 * execution. This mimics the behavior of the pre-AMD blitz.
 */
public class InThreadThrottlingStrategy extends AbstractThrottlingStrategy {

    private final static Logger log = LoggerFactory
            .getLogger(InThreadThrottlingStrategy.class);

    private final CurrentDetails cd;

    public InThreadThrottlingStrategy(CurrentDetails cd) {
        this.cd = cd;
    }

    void setup(Ice.Current current) {
        if (current != null) {
            cd.setContext(current.ctx);
        }
    }

    void teardown() {
        cd.setContext(null);
    }

    public void callInvokerOnRawArgs(ServiceInterface service,
            IceMethodInvoker invoker, Object __cb, Ice.Current __current,
            Object... args) {

        setup(__current);
        try {
            IceMapper mapper = new IceMapper();
            Callback cb = new Callback(service, invoker, mapper, __cb,
                    __current, args);
            cb.run(ctx);
        } finally {
            teardown();
        }
    }

    public void callInvokerWithMappedArgs(ServiceInterface service,
            IceMethodInvoker invoker, IceMapper mapper, Object __cb,
            Current __current, Object... args) {

        setup(__current);
        try {
            Callback cb = new Callback(service, invoker, mapper, __cb,
                    __current, args);
            cb.run(ctx);
        } finally {
            teardown();
        }
    }

    public <R> void safeRunnableCall(Current __current, Object __cb, boolean isVoid, Callable<R> callable) {
        setup(__current);
        try {
            Callback2<R> cb = new Callback2<R>(__current, __cb, isVoid, callable);
            cb.run(ctx);
        } finally {
            teardown();
        }
    }

    public void runnableCall(Current __current, Task runnable) {
        setup(__current);
        try {
            runnable.run(ctx);
        } catch (Exception e) {
            log.error("Exception during runnableCall", e);
        } finally {
            teardown();
        }
    }

}
