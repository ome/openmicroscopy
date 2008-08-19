/*
 * ome.client.JndiStatefulObjectFactoryBean
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.client;

import java.util.Properties;

import ome.conditions.RootException;
import ome.model.IObject;
import ome.system.Principal;
import ome.system.SessionInitializer;

import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.jndi.JndiLookupFailureException;
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

    /**
     */
    public void setInit(SessionInitializer init) {
        this.init = init;
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
            Principal principal = null;
            Properties p = new Properties();
            p.putAll(getJndiEnvironment());
            if (init != null) {
                principal = init.createPrincipal();
                p.put("java.naming.security.principal", principal);
                p.put("java.naming.security.credentials", "hidden");
            } else {
                principal = (Principal) p.get("java.naming.security.principal");
            }

            // Now set the template to be used to our workaround JndiTemplate
            setJndiTemplate(new ome.client.JndiTemplate(p));

            Object object;
            try {
                object = lookup();
            } catch (JndiLookupFailureException jlfe) {
                throw new OutOfService(
                        "Cannot connect to service. Is the server running?",
                        jlfe);
            }

            Interceptor i = new Interceptor(principal);

            ProxyFactoryBean factory = new ProxyFactoryBean();
            factory.setTarget(object);
            factory.addAdvice(i);
            factory.setInterfaces(new Class[] { getObjectType() });
            object = factory.getObject();
            return object;
        } catch (Exception e) {
            if (e instanceof OutOfService) {
                throw (OutOfService) e;
            } else if (e instanceof RootException) {
                throw (RootException) e;
            } else {
                final String msg = "Cannot initialize service proxy";
                logger.error(msg, e);
                throw new OutOfService(msg, e);
            }
        }
    }
}
