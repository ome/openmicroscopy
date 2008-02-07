/*
 * ome.client.JndiStatefulObjectFactoryBean
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.client;

import javax.naming.NamingException;

import ome.conditions.InternalException;
import ome.model.IObject;
import ome.system.SessionInitializer;

import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.jndi.JndiObjectTargetSource;

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

    protected SessionInitializer init;

    protected MethodInterceptor interceptor;

    /**
     * changes the behavior of the {@link JndiObjectFactoryBean} by
     */
    public void setStateful(boolean isStatless) {
        this.stateful = isStatless;
    }

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
     * delegates to {@link JndiObjectFactoryBean#isSingleton()} if not
     * {@link #stateful}. Else returns false.
     */
    @Override
    public boolean isSingleton() {
        return stateful ? false : super.isSingleton();
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

        ome.model.meta.Session sess;
        if (init == null) {
            throw new InternalException("Service factory error:"
                    + this.getJndiName() + " is missing it's initializer");
        } else {
            sess = init.getSession();
        }

        if (stateful) {
            try {
                afterPropertiesSet();
            } catch (NamingException ne) {
                InternalException ie = new InternalException(ne.getMessage());
                ie.setStackTrace(ne.getStackTrace());
                throw ie;
            }
        }

        Object object = super.getObject();
        Advised advised = (Advised) object;
        JBossTargetSource redirector = new JBossTargetSource(
                (JndiObjectTargetSource) advised.getTargetSource(), init
                        .createPrincipal(), sess.getUuid());

        ProxyFactory proxyFactory = new ProxyFactory();
        for (Class klass : advised.getProxiedInterfaces()) {
            proxyFactory.addInterface(klass);
        }
        proxyFactory.setTargetSource(redirector);
        if (interceptor != null) {
            proxyFactory.addAdvice(interceptor);
        }
        return proxyFactory.getProxy();
    }

}
