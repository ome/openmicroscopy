/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

// Java imports
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ome.api.ServiceInterface;
import ome.logic.HardWiredInterceptor;
import ome.services.blitz.fire.AopContextInitializer;
import ome.services.blitz.util.BlitzExecutor;
import ome.services.blitz.util.IceMethodInvoker;
import ome.services.blitz.util.ServantHelper;
import ome.services.throttling.ThrottlingStrategy;
import ome.services.util.Executor;
import ome.system.OmeroContext;
import omero.api._ServiceInterfaceOperations;

import org.springframework.aop.framework.ProxyFactory;
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

    protected final BlitzExecutor be;
    protected final ServantHelper helper = new ServantHelper();
    protected ServiceInterface service;
    protected IceMethodInvoker invoker;

    public AbstractAmdServant(ServiceInterface service, BlitzExecutor be) {
        this.be = be;
        this.service = service;
    }

    public final void setApplicationContext(ApplicationContext ctx)
            throws BeansException {
        this.invoker = new IceMethodInvoker(service, (OmeroContext) ctx);
    }

    /**
     * Applies the hard-wired intercepting to this instance. It is not possible
     * to configure hard-wired interceptors in Spring, instead they must be
     * passed in at runtime from a properly compiled class.
     */
    public final void applyHardWiredInterceptors(
            List<HardWiredInterceptor> cptors, AopContextInitializer initializer) {

        ProxyFactory wiredService = new ProxyFactory();
        wiredService.setInterfaces(service.getClass().getInterfaces());
        wiredService.setTarget(service);

        List<HardWiredInterceptor> reversed = new ArrayList<HardWiredInterceptor>(
                cptors);
        Collections.reverse(reversed);
        for (HardWiredInterceptor hwi : reversed) {
            wiredService.addAdvice(0, hwi);
        }
        wiredService.addAdvice(0, initializer);
        service = (ServiceInterface) wiredService.getProxy();
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
