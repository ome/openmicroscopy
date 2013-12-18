/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.utests;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import ome.services.util.Executor;
import ome.system.OmeroContext;
import ome.system.Principal;
import ome.system.ServiceFactory;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

/**
 * Simple Executor implementation which simply delegates to the
 * {@link Executor.Work#doWork(org.hibernate.Session, ServiceFactory)} and
 * similar methods.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
public class DummyExecutor implements Executor {

    org.hibernate.Session session;
    ServiceFactory sf;
    ExecutorService service;

    public DummyExecutor(org.hibernate.Session session, ServiceFactory sf) {
        this(session, sf, null);
    }

    public DummyExecutor(org.hibernate.Session session, ServiceFactory sf,
        ExecutorService service) {
        this.session = session;
        this.sf = sf;
        this.service = service;
    }

    public Object execute(Principal p, Work work) {
        return execute(null, p, work);
    }

    public Object execute(Map<String, String> callContext, Principal p, Work work) {
        return work.doWork(session, sf);
    }

    public <T> Future<T> submit(Callable<T> callable) {
        throw new UnsupportedOperationException();
    }

    public <T> Future<T> submit(Map<String, String> callContext, Callable<T> callable) {
        throw new UnsupportedOperationException();
    }

    public <T> Future<T> submit(Priority prio, Callable<T> callable) {
        throw new UnsupportedOperationException();
    }

    public <T> Future<T> submit(Priority prio, Map<String, String> callContext,
            Callable<T> callable) {
        throw new UnsupportedOperationException();
    }

    public <T> T get(Future<T> future) {
        throw new UnsupportedOperationException();
    }

    public ExecutorService getService() {
        return service;
    }

    public Object executeSql(SqlWork work) {
        throw new UnsupportedOperationException();
    }

    public void setApplicationContext(ApplicationContext arg0)
            throws BeansException {
        throw new UnsupportedOperationException();
    }

    public OmeroContext getContext() {
        throw new UnsupportedOperationException();
    }

    public Principal principal() {
        throw new UnsupportedOperationException();
    }

}
