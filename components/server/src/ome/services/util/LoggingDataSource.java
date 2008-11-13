/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.util;

import javax.sql.DataSource;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;

/**
 * Produces a wrapper around a {@link DataSource} which logs all connection failures.
 * This was intended to be an implementation, but due to breaking changes in JDBC4
 * was instead chagned to a proxy {@link FactoryBean}
 * 
 * @author Josh Moore, josh at glencoesoftwarecom
 * @since Beta4
 */
public class LoggingDataSource implements FactoryBean {

    public final static Log log = LogFactory.getLog(LoggingDataSource.class);

    private final DataSource proxy;
    
    public LoggingDataSource(final DataSource dataSource) {
        ProxyFactory factory = new ProxyFactory(dataSource);
        factory.setInterfaces(new Class[]{DataSource.class});
        factory.addAdvice(new MethodInterceptor(){

            public Object invoke(MethodInvocation arg0) throws Throwable {
                try {
                    return arg0.proceed();
                } catch (Throwable t) {
                    if (arg0.getMethod().getName().equals("getConnection")) {
                        log.error(t);
                    }
                    throw t;
                }
            }});
        proxy = (DataSource) factory.getProxy();
    }

    public Object getObject() throws Exception {
        return proxy;
    }

    public Class getObjectType() {
        return DataSource.class;
    }

    public boolean isSingleton() {
        return true;
    }
    
}
