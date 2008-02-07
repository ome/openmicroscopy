/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.naming.NamingException;

import org.springframework.aop.TargetSource;
import org.springframework.jndi.JndiLookupFailureException;
import org.springframework.jndi.JndiObjectTargetSource;
import org.springframework.jndi.JndiTemplate;
import org.springframework.util.Assert;

/**
 * Responsible for properly handling exceptions thrown on JNDI lookup
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @version 3.0 <small> (<b>Internal version:</b> $Revision$ $Date$) </small>
 * @since 3.0-Beta3
 */
public class JndiTargetSource implements TargetSource {

    protected JndiObjectTargetSource target;

    protected Class[] interfaces;

    /**
     * creates a {@link JndiTargetSource} which performs proper login on every
     * access.
     */
    public JndiTargetSource(JndiTemplate jt, JndiObjectTargetSource target) {
        Assert.notNull(jt, "JndiTemplate is required");
        Assert.notNull(target, "Target is required");
        this.target = target;
        this.target.setJndiTemplate(jt);
    }

    /**
     * Used by {@link ConfigurableJndiObjectFactoryBean} to set the target
     * interfaces.
     */
    public void setInterfaces(Class[] interfaces) {
        this.interfaces = interfaces;
    }

    /** delegates to {@link JndiObjectTargetSource#getTargetClass()} */
    public Class getTargetClass() {
        return this.target.getTargetClass();
    }

    /**
     * delegates to {@link JndiObjectTargetSource#getTarget()}, but returns an
     * {@link InvocationHandler} that will throw an {@link OutOfService} on
     * access.
     */
    public Object getTarget() throws NamingException {
        Object retVal;
        try {
            retVal = this.target.getTarget();
        } catch (final JndiLookupFailureException jlfe) {
            InvocationHandler handler = new InvocationHandler() {
                public Object invoke(Object proxy, Method method, Object[] args)
                        throws Throwable {
                    throw new OutOfService(
                            "Cannot connect to service. Is the server running?",
                            jlfe);
                }
            };
            retVal = Proxy.newProxyInstance(target.getClass().getClassLoader(),
                    interfaces, handler);
        }
        return retVal;
    }

    /** delegates to {@link JndiObjectTargetSource#releaseTarget(Object)} */
    public void releaseTarget(Object target) {
        this.target.releaseTarget(target);
    }

    /** delegates to {@link JndiObjectTargetSource#isStatic()} */
    public boolean isStatic() {
        return this.target.isStatic();
    }

    /**
     * delegates to {@link JndiObjectTargetSource#hashCode()}
     */
    @Override
    public int hashCode() {
        return this.target.hashCode();
    }

    /**
     * Two invoker interceptors are equal if they have the same target or if the
     * targets or the targets are equal.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof JndiTargetSource)) {
            return false;
        }
        JndiTargetSource otherTargetSource = (JndiTargetSource) other;
        return this.target.equals(otherTargetSource.target);
    }

    @Override
    public String toString() {
        return "JndiTargetSource for target: " + this.target;
    }

}
