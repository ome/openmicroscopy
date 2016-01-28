/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

package ome.services.blitz.util;

import java.util.concurrent.Callable;

import ome.api.ServiceInterface;
import ome.services.throttling.Task;
import omero.util.IceMapper;

/**
 * Single-point of execution for all AMD blitz calls.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta4
 */
public interface BlitzExecutor {

    /**
     * Uses the given {@link IceMethodInvoker} to make the method call. All
     * arguments are passed directly into the invoker, and the return value from
     * the invoker is passed to the user.
     */
    void callInvokerOnRawArgs(ServiceInterface service,
            IceMethodInvoker invoker, Object __cb, Ice.Current __current,
            Object... args);

    /**
     * Passes the given arguments to {@link IceMethodInvoker} with the
     * assumption that all conversion from omero.* to ome.* has taken place.
     * Similarly, the {@link IceMapper} instance will be used to map the
     * return value from ome.* to omero.*.
     */
    void callInvokerWithMappedArgs(ServiceInterface service,
            IceMethodInvoker invoker, IceMapper mapper, Object __cb,
            Ice.Current __current, Object... args);

    void runnableCall(Ice.Current __current, Task runnable);

    <R> void safeRunnableCall(Ice.Current __current, Object cb, boolean isVoid,
            Callable<R> callable);
}
