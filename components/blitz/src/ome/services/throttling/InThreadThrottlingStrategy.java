/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.throttling;

import ome.api.ServiceInterface;
import ome.services.blitz.util.IceMethodInvoker;
import omero.util.IceMapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import Ice.Current;

/**
 * Throttling implementation which uses the calling server {@link Thread} for
 * execution. This mimics the behavior of the pre-AMD blitz.
 */
public class InThreadThrottlingStrategy implements ThrottlingStrategy {

    private final static Log log = LogFactory
            .getLog(InThreadThrottlingStrategy.class);

    public InThreadThrottlingStrategy() {
    }

    public void callInvokerOnRawArgs(ServiceInterface service,
            IceMethodInvoker invoker, Object __cb, Ice.Current __current,
            Object... args) {
        IceMapper mapper = new IceMapper();
        Callback cb = new Callback(service, invoker, mapper, __cb, __current,
                args);
        cb.run();

    }

    public void callInvokerWithMappedArgs(ServiceInterface service,
            IceMethodInvoker invoker, IceMapper mapper, Object __cb,
            Current __current, Object... args) {
        Callback cb = new Callback(service, invoker, mapper, __cb, __current,
                args);
    }

    public void runnableCall(Current __current, Task task) {
        try {
            task.run();
        } catch (Exception e) {
            log.error("Exception during runnableCall", e);
        }
    }

}
