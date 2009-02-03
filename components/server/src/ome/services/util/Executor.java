/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.util;

import java.util.ArrayList;
import java.util.List;

import ome.security.SecuritySystem;
import ome.security.basic.PrincipalHolder;
import ome.system.OmeroContext;
import ome.system.Principal;
import ome.system.ServiceFactory;
import ome.tools.spring.InternalServiceFactory;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.springframework.aop.ProxyMethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;

/**
 * Simple execution/work interface which can be used for <em>internal</em> tasks
 * which need to have a full working implementation. The
 * {@link Executor#execute(Principal, ome.services.util.Executor.Work)} method
 * ensures that {@link SecuritySystem#login(Principal)} is called before the
 * task, that a {@link TransactionCallback} and a {@link HibernateCallback}
 * surround the call, and that subsequently {@link SecuritySystem#logout()} is
 * called.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public interface Executor extends ApplicationContextAware {

    /**
     * Provides access to the context for Work-API consumers who need to publish
     * events, etc.
     */
    public OmeroContext getContext();

    /**
     * Executes a {@link Work} instance wrapped in two layers of AOP. The first
     * is intended to acquire the proper arguments for
     * {@link Work#doWork(Session, ServiceFactory)} from the
     * {@link OmeroContext}, and the second performs all the standard service
     * actions for any normal method call.
     * 
     * If the {@link Principal} argument is not null, then additionally, a
     * login/logout sequence will be performed in a try/finally block.
     * 
     * {@link Work} implementation must be annotated with {@link Transactional}
     * in order to properly specify isolation, read-only status, etc.
     * 
     * @param p
     *            Possibly null.
     * @param work
     *            Not null.
     */
    public Object execute(final Principal p, final Work work);

    /**
     * Executes a {@link StatelessWork} wrapped with a transaction. Since
     * {@link StatelessSession} does not return proxies, there is less concern
     * about returned values, but this method <em>completely</em> overrides
     * OMERO security, and should be used <b>very</em> carefully. *
     * 
     * As with {@link #execute(Principal, Work)} the {@link StatelessWork}
     * instance must be properly marked with an {@link Transactional}
     * annotation.
     * 
     * @param work
     *            Non-null.
     * @return
     */
    public Object executeStateless(final StatelessWork work);

    /**
     * Work SPI to perform actions within the server as if they were fully
     * wrapped in our service logic. Note: any results which are coming from
     * Hibernate <em>may <b>not</b></em> be assigned directly to a field, rather
     * must be returned as an {@link Object} so that Hibernate proxies can be
     * properly handled.
     */
    public interface Work {

        /**
         * Returns a description of what this work will be doing for logging
         * purposes.
         */
        String description();

        /**
         * Work method. Must return all results coming from Hibernate via the
         * {@link Object} return method.
         * 
         * @param status
         *            non null.
         * @param session
         *            non null.
         * @param sf
         *            non null.
         * @return Any results which will be used by non-wrapped code.
         */
        Object doWork(Session session, ServiceFactory sf);

    }

    /**
     * Work SPI to perform actions related to
     * {@link org.hibernate.SessionFactory#openStatelessSession() stateless}
     * sessions. This overrides <em>ALL</em> security in the server and should
     * only be used as a last resort. Currently accept locations are:
     * <ul>
     * <li>In the {@link ome.services.sessions.SessionManager} to boot strap a
     * {@link ome.model.meta.Session session}
     * <li>In the {@link ome.security.basic.EventHandler} to save
     * {@link ome.model.meta.EventLog event logs}
     * </ul>
     * 
     * Before the JTA fixes of 4.0, this interface provided a
     * {@link org.hibernate.StatelessSession. However, as mentioned in
     * http://jira.springframework.org/browse/SPR-2495, that interface is not
     * currently supported in Spring's transaction management.
     */
    public interface StatelessWork {

        /**
         * Return a description of what this work will be doing for logging
         * purposes.
         */
        String description();

        Object doWork(SimpleJdbcOperations jdbc);
    }

    /**
     * Simple adapter which takes a String for {@link #description}
     */
    public abstract class SimpleWork implements Work {

        final private String description;

        public SimpleWork(Object o, String method) {
            this.description = o.getClass().getName() + "." + method;
        }

        public String description() {
            return description;
        }

    }

    /**
     * Simple adapter which takes a String for {@link #description}
     */
    public abstract class SimpleStatelessWork implements StatelessWork {

        final private String description;

        public SimpleStatelessWork(Object o, String method) {
            this.description = o.getClass().getName() + "." + method;
        }

        public String description() {
            return description;
        }

    }

    public class Impl implements Executor {

        private final static Log log = LogFactory.getLog(Executor.class);

        protected OmeroContext context;
        final protected List<Advice> advices = new ArrayList<Advice>();
        final protected PrincipalHolder principalHolder;
        final protected String[] proxyNames;
        final protected HibernateTemplate hibTemplate;
        final protected SimpleJdbcOperations jdbcOps;

        public Impl(PrincipalHolder principalHolder, HibernateTemplate ht,
                SimpleJdbcOperations jdbc, String[] proxyNames) {
            this.hibTemplate = ht;
            this.jdbcOps = jdbc;
            this.principalHolder = principalHolder;
            this.proxyNames = proxyNames;
        }

        public void setApplicationContext(ApplicationContext applicationContext)
                throws BeansException {
            this.context = (OmeroContext) applicationContext;
            for (String name : proxyNames) {
                advices.add((Advice) this.context.getBean(name));
            }
        }

        public OmeroContext getContext() {
            return this.context;
        }

        /**
         * Executes a {@link Work} instance wrapped in two layers of AOP. The
         * first is intended to acquire the proper arguments for
         * {@link Work#doWork(TransactionStatus, Session, ServiceFactory)} for
         * the {@link OmeroContext}, and the second performs all the standard
         * service actions for any normal method call.
         * 
         * If the {@link Principal} argument is not null, then additionally, a
         * login/logout sequence will be performed in a try/finally block.
         * 
         * @param p
         * @param work
         */
        public Object execute(final Principal p, final Work work) {
            Interceptor i = new Interceptor(hibTemplate.getSessionFactory());
            ProxyFactory factory = new ProxyFactory();
            factory.setTarget(work);
            factory.setInterfaces(new Class[] { Work.class });

            for (Advice advice : advices) {
                factory.addAdvice(advice);
            }
            factory.addAdvice(i);

            Work wrapper = (Work) factory.getProxy();

            if (p != null) {
                this.principalHolder.login(p);
            }
            try {
                // Arguments will be replaced after hibernate is in effect
                return wrapper.doWork(null, new InternalServiceFactory(
                        this.context));
            } finally {
                if (p != null) {
                    this.principalHolder.logout();
                }
            }
        }

        /**
         * Executes a {@link StatelessWork} in transaction.
         * 
         * @param work
         *            Non-null.
         * @return
         */
        public Object executeStateless(final StatelessWork work) {
            ProxyFactory factory = new ProxyFactory();
            factory.setTarget(work);
            factory.setInterfaces(new Class[] { StatelessWork.class });
            factory.addAdvice(advices.get(2)); // TX FIXME
            StatelessWork wrapper = (StatelessWork) factory.getProxy();
            return wrapper.doWork(this.jdbcOps);
        }

        /**
         * Interceptor class which properly lookups and injects the session
         * objects in the
         * {@link Work#doWork(TransactionStatus, Session, ServiceFactory)}
         * method.
         */
        static class Interceptor implements MethodInterceptor {
            private final SessionFactory factory;

            public Interceptor(SessionFactory sf) {
                this.factory = sf;
            }

            public Object invoke(final MethodInvocation mi) throws Throwable {
                final Object[] args = mi.getArguments();
                args[0] = SessionFactoryUtils.getSession(factory, false);
                return mi.proceed();
            }
        }

    }
}
