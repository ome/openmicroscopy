/*
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import ome.api.ServiceInterface;
import ome.api.StatefulServiceInterface;
import ome.logic.HardWiredInterceptor;
import ome.services.blitz.fire.AopContextInitializer;
import ome.services.blitz.util.BlitzExecutor;
import ome.services.blitz.util.IceMethodInvoker;
import ome.services.throttling.Task;
import ome.services.throttling.ThrottlingStrategy;
import ome.services.util.Executor;
import ome.system.OmeroContext;
import omero.ServerError;
import omero.api.AMD_StatefulServiceInterface_activate;
import omero.api.AMD_StatefulServiceInterface_close;
import omero.api.AMD_StatefulServiceInterface_getCurrentEventContext;
import omero.api.AMD_StatefulServiceInterface_passivate;
import omero.api._ServiceInterfaceOperations;
import omero.util.IceMapper;
import omero.util.ServantHolder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import Ice.Current;

/**
 * {@link ThrottlingStrategy throttled} implementation base class which can be
 * used by {@link _ServiceInterfaceOperations} implementors and injected into a
 * tie servant.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta4
 */
public abstract class AbstractAmdServant implements ApplicationContextAware {

    final protected Logger log = LoggerFactory.getLogger(getClass());

    final protected BlitzExecutor be;

    /**
     * If there is no undering ome.* service, then this value can be null.
     */
    protected ServiceInterface service;

    /**
     * If a service is provided, then an invoker will be created to cache all of
     * its methods.
     */
    protected IceMethodInvoker invoker;

    protected OmeroContext ctx;

    protected ServantHolder holder;

    public AbstractAmdServant(ServiceInterface service, BlitzExecutor be) {
        this.be = be;
        this.service = service;
    }

    /**
     * Sets the {@link ServantHolder} for the current session so that on
     * {@link #close_async(AMD_StatefulServiceInterface_close, Current)}
     * it will be possible to cleanup the resources.
     * @param holder
     */
    public void setHolder(ServantHolder holder) {
        this.holder = holder;
    }

    /**
     * Creates an {@link IceMethodInvoker} for this instance if {@link #service}
     * is non-null. Otherwise gives subclasses a chance to use the {@link OmeroContext}
     * via {@link #onSetContext(OmeroContext)}
     */
    public final void setApplicationContext(ApplicationContext ctx)
            throws BeansException {
        this.ctx = (OmeroContext) ctx;
        if (service != null) {
            this.invoker = new IceMethodInvoker(service, this.ctx);
        }
        try {
            onSetOmeroContext(this.ctx);
        } catch (Exception e) {
            throw new FatalBeanException("Error on setOmeroContext", e);
        }
    }
    
    /**
     * To be overridden by subclasses.
     */
    public void onSetOmeroContext(OmeroContext context) throws Exception {
        //no-op
    }

    /**
     * Applies the hard-wired intercepting to this instance. It is not possible
     * to configure hard-wired interceptors in Spring, instead they must be
     * passed in at runtime from a properly compiled class.
     */
    public final void applyHardWiredInterceptors(
            List<HardWiredInterceptor> cptors, AopContextInitializer initializer) {

        if (service != null) {
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
    }

    public final void callInvokerOnRawArgs(Object __cb, Ice.Current __current,
            Object... args) {
        if (service == null) {
            throw new ome.conditions.InternalException(
                    "Null service; cannot use callInvoker()");
        }
        this.be.callInvokerOnRawArgs(service, invoker, __cb, __current, args);
    }

    public final void callInvokerOnMappedArgs(IceMapper mapper, Object __cb,
            Ice.Current __current, Object... args) {
        if (service == null) {
            throw new ome.conditions.InternalException(
                    "Null service; cannot use callInvoker()");
        }
        this.be.callInvokerWithMappedArgs(service, invoker, mapper, __cb,
                __current, args);
    }

    public final void runnableCall(Ice.Current __current, Task r) {
        this.be.runnableCall(__current, r);
    }

    public final <R> void safeRunnableCall(Ice.Current __current, Object cb,
            boolean isVoid, Callable<R> callable) {
        this.be.safeRunnableCall(__current, cb, isVoid, callable);
    }

    public final void executorWorkCall(Executor.Work work) {
        throw new UnsupportedOperationException();
    }

    //
    // StatefulServiceInterface
    //

    public final void activate_async(AMD_StatefulServiceInterface_activate __cb,
            Current __current) {
        // Do nothing for the moment
    }

    public final void passivate_async(AMD_StatefulServiceInterface_passivate __cb,
            Current __current) {
        // Do nothing for the moment
    }

    public final void getCurrentEventContext_async(
            AMD_StatefulServiceInterface_getCurrentEventContext __cb,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current);

    }

}
