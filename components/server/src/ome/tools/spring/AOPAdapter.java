/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.tools.spring;

// Java imports
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

// Third-party imports
import ome.logic.HardWiredInterceptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.AdvisorChainFactory;
import org.springframework.aop.framework.DefaultAdvisorChainFactory;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.framework.ReflectiveMethodInvocation;

// Application-internal dependencies

/**
 * Adapts between Spring AOP and JEE AOP. AOPadapter can be used to share a
 * single service implementation between both Spring and JavaEE. This is
 * achieved by applying the stack of AOP interceptors defined in Spring and
 * having them applied during the {@link AroundInvoke} JavaEE interceptor.
 */
public class AOPAdapter extends ReflectiveMethodInvocation {

    private static Log log = LogFactory.getLog(AOPAdapter.class);

    /**
     * The {@link javax.interceptor.InvocationContext} which is passed into an
     * {@link AroundInvoke}-annotated method.
     */
    private InvocationContext invocation;

    @Override
    /**
     * invokes {@link javax.interceptor.InvocationContext#proceed() proceed} on
     * the {@link InvocationContext} passed into this instance.
     */
    protected Object invokeJoinpoint() throws Throwable {
        return invocation.proceed();
    }

    /**
     * Produces a {@link MethodInvocation} object which delegates to the EJB3
     * {@link InvocationContext} at the end of the {@link MethodInterceptor} 
     * stack. This stack is produced from the {@link HardWiredInterceptor}
     * list and from the {@link MethodInterceptor} instances defined in the
     * Spring configuration files for the current service.
     *
     * Because of hidden constructors, a static factory method is needed to
     * create the AOPAdapter.
     */
    public static AOPAdapter create(ProxyFactoryBean factory,
            InvocationContext context, List<HardWiredInterceptor> wired) {
        return new AOPAdapter(context, proxy(factory), target(context),
                method(context), args(context), targetClass(factory),
                interceptors(factory, context, wired));
    }

    /**
     * simple override of the
     * {@link org.springframework.aop.framework.ReflectiveMethodInvocation}
     * contructor which initializes the {@link AOPAdapter#invocation} field.
     */
    public AOPAdapter(InvocationContext context, Object proxy, Object target,
            Method method, Object[] arguments, Class targetClass,
            List interceptorsAndDynamicMethodMatchers) {
        super(proxy, target, method, arguments, targetClass,
                interceptorsAndDynamicMethodMatchers);
        this.invocation = context;
    }

    // ~ Static helpers for creation.
    // =========================================================================

    protected static Object proxy(ProxyFactoryBean factory) {
        return factory.getObject();
    }

    protected static Object target(InvocationContext context) {
        return context.getTarget();
    }

    protected static Method method(InvocationContext context) {
        return context.getMethod();
    }

    protected static Object[] args(InvocationContext context) {
        return context.getParameters();
    }

    protected static Class targetClass(ProxyFactoryBean factory) {
        return factory.getObjectType();
    }

    protected static final AdvisorChainFactory acf = new DefaultAdvisorChainFactory();
    protected static List interceptors(ProxyFactoryBean factory,
            InvocationContext context, List<HardWiredInterceptor> wired) {
        List first = new ArrayList(wired);
        List append = acf.getInterceptorsAndDynamicInterceptionAdvice(factory, 
                        method(context), targetClass(factory));
        first.addAll(append);
        return first;
    }

}
