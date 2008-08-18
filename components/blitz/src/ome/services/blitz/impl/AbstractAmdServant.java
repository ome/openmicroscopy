/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

// Java imports
import ome.api.ServiceInterface;
import ome.services.blitz.util.BlitzExecutor;
import ome.services.blitz.util.IceMethodInvoker;
import ome.services.blitz.util.ServantHelper;
import ome.services.throttling.ThrottlingStrategy;
import ome.services.util.Executor;
import ome.system.OmeroContext;
import omero.api._ServiceInterfaceOperations;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * {@link ThrottlingStrategy throttled} implementation base class which can be
 * used by {@link _ServiceInterfaceOperations} implementors and injected into a
 * tie servant.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta4
 */
public class AbstractAmdServant implements ApplicationContextAware {

    protected final ServiceInterface service;

    protected final BlitzExecutor be;

    protected IceMethodInvoker invoker;

    protected final ServantHelper helper = new ServantHelper();

    public AbstractAmdServant(ServiceInterface service, BlitzExecutor be) {
        this.be = be;
        this.service = service;
    }

    public final void setApplicationContext(ApplicationContext ctx)
            throws BeansException {
        this.invoker = new IceMethodInvoker(service, (OmeroContext) ctx);
    }

    public final void serviceInterfaceCall(Object __cb, Ice.Current __current,
            Object... args) {
        if (service == null) {
            throw new ome.conditions.InternalException(
                    "Null service; cannot use serviceInterfaceCall()");
        }
        this.be.serviceInterfaceCall(service, invoker, helper, __cb, __current,
                args);
    }

    public final void runnableCall(Ice.Current __current, BlitzExecutor.Task t) {
        this.be.runnableCall(__current, t);
    }

    public final void executorWorkCall(Executor.Work work) {
        throw new UnsupportedOperationException();
    }

}
