/*
 *   $Id: Server.java 1201 2007-01-18 21:54:35Z jmoore $
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.icy.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import ome.api.IAdmin;
import ome.api.IConfig;
import ome.api.IPixels;
import ome.api.IPojos;
import ome.api.IQuery;
import ome.api.ITypes;
import ome.api.IUpdate;
import ome.api.RawFileStore;
import ome.api.RawPixelsStore;
import ome.api.ServiceInterface;
import ome.api.ThumbnailStore;
import ome.logic.HardWiredInterceptor;
import ome.services.icy.fire.AopContextInitializer;
import ome.services.icy.fire.Session;
import ome.services.icy.util.IceMethodInvoker;
import ome.services.icy.util.ServantHelper;
import ome.services.icy.util.UnregisterServantMessage;
import ome.system.OmeroContext;
import ome.system.Principal;
import ome.system.ServiceFactory;
import omeis.providers.re.RenderingEngine;
import omero.api.IAdminPrx;
import omero.api.IAdminPrxHelper;
import omero.api.IConfigPrx;
import omero.api.IConfigPrxHelper;
import omero.api.IPixelsPrx;
import omero.api.IPixelsPrxHelper;
import omero.api.IPojosPrx;
import omero.api.IPojosPrxHelper;
import omero.api.IQueryPrx;
import omero.api.IQueryPrxHelper;
import omero.api.ITypesPrx;
import omero.api.ITypesPrxHelper;
import omero.api.IUpdatePrx;
import omero.api.IUpdatePrxHelper;
import omero.api.RawPixelsStorePrx;
import omero.api.RawPixelsStorePrxHelper;
import omero.api.RenderingEnginePrx;
import omero.api.RenderingEnginePrxHelper;
import omero.api.SimpleCallbackPrx;
import omero.api.ThumbnailStorePrx;
import omero.api.ThumbnailStorePrxHelper;
import omero.api._IAdminOperations;
import omero.api._IAdminTie;
import omero.api._ServiceFactoryDisp;
import omero.util.IceMapper;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * @author josh
 * 
 */
public class Interceptor implements MethodInterceptor {

    Class iface;

    String key;

    ServantHelper servantHelper;
    
    IceMethodInvoker invoker;
    
    public Interceptor(Class interfaceClass, String serviceKey, ServantHelper helper) {
        this.iface = interfaceClass;
        this.key = serviceKey;
        this.servantHelper = helper;
        this.invoker = new IceMethodInvoker(getInterfaceClass());
    }

    public Class getInterfaceClass() {
        return iface;
    }

    public Object invoke(MethodInvocation mi) throws Throwable {

        Object retVal = null;
        try {
            Object[] args = mi.getArguments();
            Ice.Current __current = (Ice.Current) args[args.length-1];
            Object[] strippedArgs = strip(args);
        
            IceMapper mapper = new IceMapper();
            ServiceInterface service = servantHelper.getService(key,__current);
            retVal = invoker.invoke(service, __current, mapper, strippedArgs);
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
        servantHelper.throwIfNecessary(retVal);
        return retVal;
    }
    
    protected Object[] strip(Object[] args) {
        Object[] rv = new Object[args.length-1];
        System.arraycopy(args, 0, rv, 0, args.length-1);
        return rv;
    }
    
}
