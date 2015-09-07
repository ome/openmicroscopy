/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.tools.hibernate;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import ome.util.SqlAction;
import ome.util.TableIdGenerator;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.hibernate.Session;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.impl.SessionFactoryImpl;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

/**
 * Simple source of Thread-aware {@link Session} instances. Wraps a
 * call to {@link SessionFactoryUtils}. Should be safe to call from
 * within any service implementation call or inside of Executor.execute.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.0
 */
public class SessionFactory implements MethodInterceptor {

    private final static Set<String> FORBIDDEN = Collections.unmodifiableSet(
        new HashSet<String>(
            Arrays.asList(
                "createSQLQuery", "getSession", "doWork",
                "connection", "disconnect", "reconnect")));

    static {
        // Check for spelling mistakes
        int found = 0;
        Method[] methods = Session.class.getMethods();
        for (Method m : methods) {
            if (FORBIDDEN.contains(m.getName())) {
                found++;
            }
        }
        if (found < FORBIDDEN.size()) {
            throw new RuntimeException("Method name not found! " + FORBIDDEN);
        }

    }


    private final org.hibernate.SessionFactory factory;

    public SessionFactory(org.hibernate.SessionFactory factory, SqlAction isolatedSqlAction) {
        this.factory = factory;
        for (Object k : this.factory.getAllClassMetadata().keySet()) {
            IdentifierGenerator ig =
                ((SessionFactoryImpl) factory).getIdentifierGenerator((String)k);
            if (ig instanceof TableIdGenerator) {
                ((TableIdGenerator) ig).setSqlAction(isolatedSqlAction);
            }
        }

    }

    /**
     * Returns a session active for the current thread. The returned
     * instance will be wrapped with AOP to prevent certain usage.
     * @see <a href="http://trac.openmicroscopy.org/ome/ticket/73">Trac ticket #73</a>
     * @return a wrapped active Hibernate session
     */
    public Session getSession() {

        Session unwrapped = SessionFactoryUtils.getSession(factory, false);

        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setInterfaces(new Class[]{
            Session.class,
            org.hibernate.classic.Session.class,
            org.hibernate.event.EventSource.class});
        proxyFactory.setTarget(unwrapped);
        proxyFactory.addAdvice(0, this);
        return (Session) proxyFactory.getProxy();

    }

    /**
     * Wraps all invocations to Session to prevent certain usages.
     * Note: {@link QueryBuilder} may unwrap the session in certain
     * cases.
     */
    public Object invoke(MethodInvocation mi) throws Throwable {

        final String name = mi.getMethod().getName();
        if (FORBIDDEN.contains(name)) {
            throw new ome.conditions.InternalException(String.format(
                "Usage of session.%s is forbidden. See ticket #73", name));
        }
        return mi.proceed();

    }

}
