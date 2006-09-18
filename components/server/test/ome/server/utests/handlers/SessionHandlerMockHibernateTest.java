/*
 * ome.server.utests.handlers.SessionHandlerMockHibernateTest
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */
package ome.server.utests.handlers;

// Java imports
import java.lang.reflect.Method;
import java.sql.Connection;

import javax.sql.DataSource;

import junit.framework.Assert;

// Third-party libraries
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.classic.Session;
import org.hibernate.FlushMode;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.builder.MatchBuilder;
import org.jmock.core.Invocation;
import org.jmock.core.InvocationMatcher;
import org.jmock.core.Stub;
import org.jmock.core.stub.DefaultResultStub;
import org.springframework.orm.hibernate3.HibernateInterceptor;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.testng.annotations.*;

// Application-internal dependencies
import ome.api.ServiceInterface;
import ome.api.StatefulServiceInterface;
import ome.conditions.InternalException;
import ome.tools.hibernate.SessionHandler;
import omeis.providers.re.RenderingEngine;

/**
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since Omero 2.0
 */
@Test( groups = {"ignore","sessions","hibernate","priority"} )
public class SessionHandlerMockHibernateTest extends MockObjectTestCase
{

    private static Log log = LogFactory
            .getLog(SessionHandlerMockHibernateTest.class);
    
    protected ServiceInterface stateless;
    protected StatefulServiceInterface stateful;
    protected SessionHandler handler;
    protected Session session;
    protected SessionFactory factory;
    protected DataSource dataSource;
    protected Transaction transaction;
    protected Connection connection;
    protected MethodInvocation invocation;
    protected Mock mockSession, mockFactory,  
        mockInvocation, mockStateful, mockStateless, 
        mockDataSource, mockTransaction, mockConnection;

    @Configuration(beforeTestMethod = true)
    protected void setUp() throws Exception
    {
        super.setUp();
        newDataSource();
        // tx and conn are created in beginsTransaction()
        
        newSession();
        newSessionFactory();
        handler = new SessionHandler( dataSource, factory );
        // must call newXInvocation in test

        // these are reused unless otherwise noted
        newStateful();
        newStateless(); 
        
        // Things should always be cleaned up by handler/interceptor
        assertFalse( TransactionSynchronizationManager.hasResource(factory) );
    }

    @Configuration(afterTestMethod = true)
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    // ~ Tests
    // =========================================================================

    @Test
    public void testStatelessInvocation() throws Throwable
    {
        newStatelessInvocation();
        HibernateInterceptor interceptor = new HibernateInterceptor() {
            @Override
            public Object invoke(MethodInvocation methodInvocation) throws Throwable
            {
                return null;
            }
        };
        interceptor.setSessionFactory( factory );
        // for testing stateless, we need control over the interceptor.
        handler = new SessionHandler( dataSource, interceptor );
        handler.invoke( invocation );
        super.verify();
    }

    @Test
    public void testStatefulInvocation() throws Throwable
    {
        newStatefulReadInvocation();
        opensSession();
        beginsTransaction(1);
        checksSessionIsOpen();
        getsFactoryFromSession();
        getsAutoFlushMode();
        checksSessionIsConnected();
        disconnectsSession();
        handler.invoke( invocation );
        super.verify();
    }

    @Test
    public void testTwoStatefulInvocations() throws Throwable
    {
        newStatefulReadInvocation();
        opensSession();
        beginsTransaction(2);
        checksSessionIsOpen();
        getsFactoryFromSession();
        getsAutoFlushMode();
        checksSessionIsConnected();
        disconnectsSession();
        handler.invoke( invocation );
        // And a second call should just work.
        newStatefulReadInvocation();
        handler.invoke( invocation );
        super.verify();
    }
    
    @Test
    @ExpectedExceptions(InternalException.class)
    public void testStatefulInvocationWithExistingSession() throws Throwable
    {
        // setup Session
        prepareThread();
        
        newStatefulReadInvocation();
        checksSessionIsOpen();
        //checksSessionIsConnected();
        getsFactoryFromSession();
        disconnectsSession();
        closesSession();
        handler.invoke( invocation );
        super.verify();
    }
    
    @Test
    public void testStatefulInvocationWithSessionThenClosed() throws Throwable
    {
        newStatefulDestroyInvocation();
        checksSessionIsOpen();
        getsFactoryFromSession();
        getsAutoFlushMode();
        opensSession();
        beginsTransaction(1);
        getsSessionsConnection();
        commitsConnection();
        closesSession();
        handler.invoke( invocation );
        super.verify();
    }
    
    @Test
    public void testSyncResetEvenOnException() throws Throwable
    {
        // setup Session
        prepareThread();

        try {
        newStatefulReadInvocation();
        checksSessionIsOpen();
        //checksSessionIsConnected();
        getsFactoryFromSession();
        disconnectsSession();
        closesSession();
        handler.invoke( invocation );
        fail("Should have thrown.");
        } catch (Exception e)
        {}
    
        newStatefulDestroyInvocation();
        checksSessionIsOpen();
        getsFactoryFromSession();
        getsAutoFlushMode();
        opensSession();
        beginsTransaction(1);
        getsSessionsConnection();
        commitsConnection();
        closesSession();
        handler.invoke( invocation );
        super.verify();
    }
    
    // ~ Once Expectations (creation events)
    // =========================================================================
    
    protected void opensSession()
    {
        mockFactory.expects( once() ).method( "openSession" )
            .will( returnValue( session ));
    }
    
    protected void beginsTransaction(int count)
    {
     
        newConnection();
        newTransaction();
        mockSession.expects( exactly(count) ).method("beginTransaction");
//        MatchBuilder builder = 
//            mockSession.expects( once() ).method( "beginTransaction" );
//        builder.will( returnValue( transaction ) );
//        builder.will( printStackTrace() );
//        if ( id != null )
//        {
//            builder.id(id);
//        }
//        if ( after != null )
//        {
//            builder.after(after);
//        }
    }

    // ~ More-than-once Expectations (somewhat idempotent)
    // =========================================================================
    
    protected void checksSessionIsOpen()
    {
        mockSession.expects( atLeastOnce() ).method( "isOpen" )
            .will( returnValue( true ));
    }

    protected void checksSessionIsConnected()
    {
    	log.warn("No longer called. WHY?");
//        mockSession.expects( atLeastOnce() ).method( "isConnected" )
//            .will( returnValue( true ));
    }

    protected void getsSessionsConnection()
    {
        mockSession.expects( atLeastOnce() ).method( "connection" )
            .will( returnValue( connection ));
    }
    
    protected void getsAutoFlushMode()
    {
        mockSession.expects( atLeastOnce() ).method( "getFlushMode" )
            .will( returnValue( FlushMode.AUTO ));
    }
    
    protected void getsFactoryFromSession()
    {
        mockSession.expects( atLeastOnce() ).method( "getSessionFactory" )
        .will( returnValue( factory ));
    }
    
    protected void disconnectsSession()
    {
        mockSession.expects( atLeastOnce() ).method( "disconnect" );
    }
    
    protected void closesSession()
    {
        mockSession.expects( atLeastOnce() ).method( "close" );
    }
    
    protected void commitsConnection()
    {
        mockConnection.expects( atLeastOnce() ).method( "commit" );
    }
    
    
    
    // ~ Helpers
    // =========================================================================

    protected void newDataSource(){
        mockDataSource = mock(DataSource.class);
        dataSource = (DataSource) mockDataSource.proxy();
    }
    
    protected void newConnection(){
        mockConnection = mock(Connection.class);
        connection = (Connection) mockConnection.proxy();
    }
    
    protected void newTransaction(){
        mockTransaction = mock(Transaction.class);
        mockTransaction.setDefaultStub( new DefaultResultStub() );
        transaction = (Transaction)mockTransaction.proxy();
    }
    protected void newSession(){
        mockSession = mock(Session.class);
        session = (Session) mockSession.proxy();
    }

    protected void newSessionFactory(){
        mockFactory = mock(SessionFactory.class);
        factory = (SessionFactory) mockFactory.proxy();
    }

    protected void newStateful( )
    {
        mockStateful = mock(StatefulServiceInterface.class);
        stateful = (StatefulServiceInterface) mockStateful.proxy();
    }
    
    protected void newStateless( )
    {
        mockStateless = mock(ServiceInterface.class);
        stateless = (ServiceInterface) mockStateless.proxy();
    }
    
    protected void newStatelessInvocation(){
        mockInvocation = mock(MethodInvocation.class);
        invocation = (MethodInvocation) mockInvocation.proxy();
        mockInvocation.expects( once() ).method("getThis")
            .will( returnValue(stateless));
    }
    
    protected void newStatefulReadInvocation() throws Exception    {
        Method method = RenderingEngine.class.getMethod("load");
        newStatefulInvocation( method );
    }

    protected void newStatefulDestroyInvocation() throws Exception    {
        Method method = RenderingEngine.class.getMethod("destroy");
        newStatefulInvocation( method );
    }

    protected void newStatefulInvocation( Method method )
    {
        mockInvocation = mock(MethodInvocation.class);
        invocation = (MethodInvocation) mockInvocation.proxy();
        mockInvocation.expects( atLeastOnce() ).method("getThis")
            .will( returnValue(stateful));
        mockInvocation.expects( atLeastOnce() ).method("getMethod")
            .will( returnValue(method));
        mockInvocation.expects( once() ).method( "proceed" );

    }
    
    protected void prepareThread()
    {
        mockSession.expects( once() ).method( "beginTransaction" ).id("prep");
        SessionHolder sessionHolder = new SessionHolder(session);
        sessionHolder.setTransaction(sessionHolder.getSession()
                .beginTransaction());
        TransactionSynchronizationManager.bindResource(factory, sessionHolder);
        TransactionSynchronizationManager.initSynchronization();
    }

    protected Stub printStackTrace()
    {
        return new StackTraceStub();
    }
    
    private class StackTraceStub implements Stub
    {
        public StringBuffer describeTo( StringBuffer buffer ) {
            return buffer.append("prints stack trace");
        }

        public Object invoke( Invocation invocation ) throws Throwable {
            new Throwable().printStackTrace();
            return null;
        }
    }

    protected InvokedRecorder exactly( int count )
    {
        return new InvokedRecorder( count );
    }
    
    private class InvokedRecorder implements InvocationMatcher
    {
        private int actual = 0;
        private int expected = 0;
        
        public InvokedRecorder( int expected )
        {
            this.expected = expected;
        }
        
        public boolean matches( Invocation invocation ) {
            return true;
        }

        public void invoked( Invocation invocation ) {
            actual++;
        }

        public void verify() {
            Assert.assertTrue(
                    "expected method was not called "+
                    expected+" rather "+actual+" times.", actual == expected);
        }

        public boolean hasDescription() {
            return true;
        }

        public StringBuffer describeTo( StringBuffer buffer ) {
            buffer.append("expected "+expected+" times");
            buffer.append(" and has been invoked "+actual+" times");
            return buffer;
        }
    }
    
}
