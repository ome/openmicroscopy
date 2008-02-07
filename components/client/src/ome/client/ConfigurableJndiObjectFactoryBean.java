/*
 * ome.client.JndiStatefulObjectFactoryBean
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.client;

import java.util.Properties;

import ome.model.IObject;
import ome.system.SessionInitializer;

import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.jndi.JndiObjectTargetSource;
import org.springframework.jndi.JndiTemplate;

/**
 * allows prototype-like lookup of stateful session beans. This is achieved by
 * overriding {@link JndiObjectFactoryBean#isSingleton()} to always return false
 * (i.e. prototype) and by recalling
 * {@link JndiObjectFactoryBean#afterPropertiesSet()} on each
 * {@link JndiObjectFactoryBean#getObject()} call.
 * 
 * This class is fairly sensitive to changes in {@link JndiObjectFactoryBean}.
 * 
 * @author <br>
 *         Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.more@gmx.de"> josh.moore@gmx.de</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision$ $Date$) </small>
 * @since OME3.0
 * @see ome.client.Session#register(IObject)
 */
public class ConfigurableJndiObjectFactoryBean extends JndiObjectFactoryBean {

    protected boolean stateful = false;

    protected MethodInterceptor interceptor;

    protected SessionInitializer init;

    /**
     */
    public void setInit(SessionInitializer init) {
        this.init = init;
    }

    /**
     * setter for {@link Interceptor} which will surround all proxies to, e.g.
     * catch unknown exceptions.
     */
    public void setInterceptor(Interceptor interceptor) {
        this.interceptor = interceptor;
    }

    /**
     */
    @Override
    public boolean isSingleton() {
        return false;
    }

    /**
     * delegates to {@link JndiObjectFactoryBean#getObject()}. If stateful, it
     * also recalls {@link JndiObjectFactoryBean#afterPropertiesSet()} to create
     * a new object. In either case, a {@link TargetSource} is wrapped around
     * the {@link JndiObjectTargetSource} returned from
     * {@link JndiObjectFactoryBean#getObject()} in order to properly handle
     * login.
     */
    @Override
    public Object getObject() {
        try {
            JndiTemplate jt = getJndiTemplate();
            if (init != null) {
                Properties p = getJndiEnvironment();
                p.put("java.naming.security.principal", init.createPrincipal());
                jt = new JndiTemplate(p);
            }
            Object object = super.getObject();
            Advised advised = (Advised) object;
            advised.addAdvice(0, interceptor);
            JndiTargetSource redirector = new JndiTargetSource(jt,
                    (JndiObjectTargetSource) advised.getTargetSource());
            redirector.setInterfaces(advised.getProxiedInterfaces());

            ProxyFactory proxyFactory = new ProxyFactory();
            for (Class klass : advised.getProxiedInterfaces()) {
                proxyFactory.addInterface(klass);
            }
            proxyFactory.setTargetSource(redirector);
            if (interceptor != null) {
                proxyFactory.addAdvice(interceptor);
            }
            return proxyFactory.getProxy();
        } catch (Exception e) {
            if (e instanceof OutOfService) {
                throw (OutOfService) e;
            } else {
                final String msg = "Cannot initialize service proxy";
                logger.error(msg, e);
                throw new OutOfService(msg, e);
            }
        }
    }
}
