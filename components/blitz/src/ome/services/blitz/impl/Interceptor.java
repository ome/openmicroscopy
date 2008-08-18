/*
 *   $Id: Server.java 1201 2007-01-18 21:54:35Z jmoore $
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

import ome.api.ServiceInterface;
import ome.services.blitz.util.IceMethodInvoker;
import ome.services.blitz.util.ServantHelper;
import ome.services.throttling.Callback;
import ome.system.OmeroContext;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Ties together the Ice _Tie class with the actual OMERO implementation via
 * reflection.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 */
public class Interceptor implements MethodInterceptor {

    private static Log log = LogFactory.getLog(Interceptor.class);

    Class iface; // TODO removeable?

    ServiceInterface service;

    ServantHelper servantHelper;

    IceMethodInvoker invoker;

    public Interceptor(Class interfaceClass, ServiceInterface service,
            ServantHelper helper, OmeroContext context) {
        this.iface = interfaceClass;
        this.service = service;
        this.servantHelper = helper;
        this.invoker = new IceMethodInvoker(getInterfaceClass(), context);
    }

    public Class getInterfaceClass() {
        return iface;
    }

    public Object invoke(MethodInvocation mi) throws Throwable {

        Object[] args = mi.getArguments();
        Object amdCallback = args[0];
        Ice.Current __current = (Ice.Current) args[args.length - 1];
        Object[] strippedArgs = strip(args);

        Callback cb = new Callback(service, invoker, servantHelper,
                amdCallback, __current, strippedArgs);
        return null;
    }

    protected Object[] strip(Object[] args) {
        Object[] rv = new Object[args.length - 2];
        System.arraycopy(args, 1, rv, 0, args.length - 1);
        return rv;
    }

}
