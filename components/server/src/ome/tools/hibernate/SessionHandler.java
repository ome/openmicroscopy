/*
 * ome.tools.hibernate.SessionHandler
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
package ome.tools.hibernate;

// Java imports
import java.sql.Connection;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import javax.sql.DataSource;

// Third-party libraries
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.orm.hibernate3.HibernateInterceptor;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

// Application-internal dependencies
import ome.api.StatefulServiceInterface;
import ome.conditions.ApiUsageException;
import ome.conditions.InternalException;

/**
 * holder for Hibernate sessions in stateful servics. A count of calls is kept.
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 3.0
 */
class SessionStatus
{

    int     calls = 0;

    Session session;

    SessionStatus(Session session)
    {
        if (null == session)
            throw new IllegalArgumentException("No null sessions.");

        this.session = session;
    }

}

/**
 * interceptor which delegates to
 * {@link org.springframework.orm.hibernate3.HibernateInterceptor} for stateless
 * services but which keeps a {@link java.util.WeakHashMap} of sessions keyed by
 * the stateful service reference.
 *
 * original idea from: 
 * http://opensource2.atlassian.com/confluence/spring/pages/viewpage.action?pageId=1447
 *
 * See also:
 * http://sourceforge.net/forum/message.php?msg_id=2455707
 * http://forum.springframework.org/archive/index.php/t-10344.html
 * http://opensource2.atlassian.com/projects/spring/browse/SPR-746
 * 
 * and these:
 * http://www.hibernate.org/43.html#A5
 * http://www.carbonfive.com/community/archives/2005/07/ive_been_meanin.html
 * http://www.hibernate.org/377.html
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 3.0
 */
public class SessionHandler implements MethodInterceptor
{

    private final static Log           log      = LogFactory
                                                        .getLog(SessionHandler.class);

    private Map<Object, SessionStatus> sessions = Collections
                                                        .synchronizedMap(new WeakHashMap<Object, SessionStatus>());

    private DataSource                 dataSource;

    private SessionFactory             factory;

    final private static String CTOR_MSG = "Both arguments to the SessionHandler" +
            " constructor should be not null."; 
    
    /** constructor taking a {@link DataSource} and a {@link SessionFactory}.
     * A new {@link HibernateInterceptor} will be created.
     * @param dataSource Not null.
     * @param factory Not null.
     */
    public SessionHandler(DataSource dataSource, SessionFactory factory)
    {
        if ( dataSource == null || factory == null )
        {
            throw new ApiUsageException(CTOR_MSG);
        }

        this.dataSource = dataSource;
        this.factory = factory;
    }
    
    /**
     * delegates to {@link HibernateInterceptor} or manages sessions internally,
     * based on the type of service.
     */
    public Object invoke(final MethodInvocation invocation) throws Throwable
    {
        // Stateless; normal semantics.
        if (!StatefulServiceInterface.class.isAssignableFrom(
                invocation.getThis().getClass()))
        {
        	throw new InternalException( 
        			"Stateless service configured as stateful." );
        }
        
        // Stateful; let's get to work.
        debug("Performing action in stateful session.");
        return doStateful(invocation);
    }

    private Object doStateful(MethodInvocation invocation) throws Throwable
    {
        Object result = null;

        SessionStatus status = null;
        try
        {
        	status = newOrRestoredSession(invocation);
        	status.session.setFlushMode( FlushMode.MANUAL );
            result = invocation.proceed();
            return result;
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw e;
        }
        finally
        {
            try {
                if (isCloseSession(invocation))
                {
                    closeSession();
                }
                else
                {
                	if (status != null )
                	{
                		// Guarantee that no one has changed the FlushMode
                		status.session.setFlushMode( FlushMode.MANUAL );
                		disconnectSession();
                	}
                }
            } catch (Exception e) {
                
                debug("Error while closing/disconnecting session.");
                
            } finally {
                resetThreadSession();

                // Everything successfully turned off. Decrement.
                if (sessions.containsKey(invocation.getThis()))
                {
                    sessions.get(invocation.getThis()).calls--;
                }
            }

        }
    }

    private SessionStatus newOrRestoredSession(MethodInvocation invocation)
            throws HibernateException
    {

        SessionStatus status = sessions.get(invocation.getThis());
    	Session previousSession = ! isSessionBoundToThread() ? null : 
    		sessionBoundToThread();

        // a session is currently running.
        // something has gone wrong (e.g. with cleanup) abort!
        if ( previousSession != null )
        {
        		String msg = "Dirty Hibernate Session "
        			+ sessionBoundToThread() + " found in Thread "
        			+ Thread.currentThread();

		        sessionBoundToThread().close();
		        resetThreadSession();
		        throw new InternalException(msg);
        } 
    	
    	// we may or may not be in a session, but if we haven't yet bound 
    	// it to This, then we need to.
        else if (status == null || !status.session.isOpen())
        {
            Session currentSession = acquireAndBindSession();
            status = new SessionStatus( currentSession );
            sessions.put(invocation.getThis(), status);
        } 

        // the session bound to This is already currently being called. abort!
        else if (status.calls > 1)
        {
            throw new InternalException(
                    "Hibernate session is not re-entrant.\n" +
                    "Either you have two threads operating on the same " +
                    "stateful object (don't do this)\n or you have a " +
                    "recursive call (recurse on the unwrapped object). ");
        }
        
        // all is fine.
        else {
            debug("Binding and reconnecting session.");
            bindSession(status.session);
            reconnectSession(status.session);
        }

        // It's ready to be used. Increment.
        status.calls++;
        return status;

    }

    // ~ SESSIONS
    // =========================================================================

    private boolean isCloseSession(MethodInvocation invocation)
    {
        return "destroy".equals(invocation.getMethod().getName());
    }

    private Session acquireAndBindSession() throws HibernateException
    {
        debug("Opening and binding session.");
        Session session = factory.openSession();
        bindSession(session);
        return session;
    }

    private void bindSession(Session session) 
    {
        debug("Binding session to thread.");
        SessionHolder sessionHolder = new SessionHolder(session);
        sessionHolder.setTransaction(sessionHolder.getSession()
                .beginTransaction());
        TransactionSynchronizationManager.bindResource(factory, sessionHolder);
        if ( ! TransactionSynchronizationManager.isSynchronizationActive())
        	throw new InternalException( "Synchronization not active for " +
        			"TransactionSynchronizationManager");
    }
    
    private Session nullOrSessionBoundToThread()
    {
    	return isSessionBoundToThread() ? sessionBoundToThread() : null;
    }
    
    private Session sessionBoundToThread()
    {
        return SessionFactoryUtils.getSession(factory, false);
    }

    private boolean isSessionBoundToThread()
    {
        return TransactionSynchronizationManager.hasResource(factory)
                && sessionBoundToThread() != null;
    }

    private void resetThreadSession()
    {
        if (isSessionBoundToThread())
        {
            debug("Session bound to thread. Reseting.");
            TransactionSynchronizationManager.unbindResource(factory);
        } else {
            debug("Session not bound to thread. No need to reset.");
        }
    }

    private void reconnectSession(Session session) throws HibernateException
    {
        if (!session.isConnected())
        {
            debug("Session not connected. Connecting.");
            Connection connection = DataSourceUtils.getConnection(dataSource);
            session.reconnect(connection);
        } else {
            debug("Session already connected. Not reconnecting.");
        }
    }

    private void disconnectSession() throws HibernateException
    {
    	Session session = nullOrSessionBoundToThread();
        if ( session != null && session.isConnected())
        {
            debug("Session bound to thread. Disconnecting.");
            session.disconnect();
        } else {
            debug("No session bound to thread. Can't disconnect.");
        }
    }
    
    private void closeSession() throws Exception
    {
    	Session session = nullOrSessionBoundToThread();
        if (session != null)
        {
            debug("Session bound to thread. Closing.");
            try
            {
                session.connection().commit();
                session.close();
            } 
            
            finally
            {
                resetThreadSession();
            }

        } else {
            debug("No session bound to thread. Can't close.");
        }

    }
    
    private void debug(String message)
    {
        if ( log.isDebugEnabled())
            log.debug(message);
    }

}
