/*
 * ome.client.JndiStatefulObjectFactoryBean
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.client;

// Java imports
import javax.naming.NamingException;

import ome.conditions.InternalException;
import ome.model.IObject;
import ome.system.Principal;

import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.jndi.JndiObjectTargetSource;

// Application-internal dependencies

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

    protected Principal principal;

    protected String credentials;

    protected MethodInterceptor interceptor;

    /**
     * changes the behavior of the {@link JndiObjectFactoryBean} by
     */
    public void setStateful(boolean isStatless) {
        this.stateful = isStatless;
    }

    /**
     * setter for the {@link Principal} which will be passed to
     * {@link JBossTargetSource#JBossTargetSource(JndiObjectTargetSource, java.security.Principal, String)}
     */
    public void setPrincipal(Principal securityPrincipal) {
        this.principal = securityPrincipal;
    }

    /**
     * setter for the credentials which will be passed to
     * {@link JBossTargetSource#JBossTargetSource(JndiObjectTargetSource, java.security.Principal, String)}
     */
    public void setCredentials(String securityCredentials) {
        this.credentials = securityCredentials;
    }

    /**
     * setter for the interceptor, which will be used to wrap all proxies.
     */
    public void setInterceptor(MethodInterceptor interceptor) {
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
                (JndiObjectTargetSource) advised.getTargetSource(),
                this.principal, this.credentials);

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
