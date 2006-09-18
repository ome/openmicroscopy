/*
 * ome.tools.hibernate.EventHandler
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
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

// Third-party libraries
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.util.Assert;

// Application-internal dependencies
import ome.api.StatefulServiceInterface;
import ome.conditions.InternalException;
import ome.model.meta.Event;
import ome.model.meta.EventLog;
import ome.security.SecuritySystem;

/**
 * method interceptor responsible for login and creation of Events. Calls are 
 * made to the {@link SecuritySystem} provided in the 
 * {@link EventHandler#EventHandler(SecuritySystem, HibernateTemplate) constructor}.
 * 
 * After the method is {@link MethodInterceptor#invoke(MethodInvocation) invoked}
 * various cleanup actions are performed and finally all credentials all 
 * {@link SecuritySystem#clearCurrentDetails() cleared} from the {@link Thread}.
 * 
 *  
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 3.0
 */
public class EventHandler implements MethodInterceptor
{

    private static Log log 
        = LogFactory.getLog(EventHandler.class);

    protected final SecuritySystem secSys;
    
    protected HibernateTemplate ht;
    
    // for StatefulServices TODO
    private Map<Session, Event> events = Collections
    .synchronizedMap(new WeakHashMap<Session, Event>());

    /** only public constructor, used for dependency injection. Requires an 
     * active {@link HibernateTemplate} and {@link SecuritySystem}.
     * 
     * @param securitySystem Not null.
     * @param template Not null.
     */
    public EventHandler( 
    		SecuritySystem securitySystem, 
    		HibernateTemplate template )
    {
    	Assert.notNull(securitySystem);
    	Assert.notNull(template);
        this.secSys = securitySystem;
        this.ht = template;
    }

    /** invocation interceptor for prepairing this {@link Thread} for execution
     * and subsequently reseting it.
     * 
     * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
     */
    public Object invoke(MethodInvocation arg0) throws Throwable
    {
        secSys.setCurrentDetails();
        boolean readOnly = checkReadOnly(arg0);

        // If read-only, we don't save the new current event, which should 
        // allow us to clear the session.
        if ( readOnly )
        {
        	if (log.isDebugEnabled())
        	{
        		log.debug("Tx readonly. Not saving current event.");
        	}
        // write operations are to be expected, prepare the current details
        // by saving the current event.
        } else {
        	secSys.setCurrentEvent((Event)ht.merge(secSys.getCurrentEvent()));
        }

        // now the user can be considered to be logged in.
        log.info(String.format("  Auth:\tuser=%s,group=%s,event=%s(%s)",
        		secSys.currentUserId(),secSys.currentGroup().getId(),
        		secSys.currentEvent().getId(),secSys.currentEvent().getType()));
        
        boolean failure = false;
        Object retVal = null;
        try {
        	ht.execute(new EnableFilterAction(secSys));
            retVal = arg0.proceed();
            return retVal;
        } catch (Exception ex){
        	failure = true;
        	throw ex;
        } finally {
        	try {
        		
        		boolean stateful = (arg0.getThis() instanceof StatefulServiceInterface);

        		// on failure, we want to make sure that no one attempts 
        		// any further changes.
        		if ( failure )
	        	{
        			// TODO we should probably do some forced clean up here.
	        	}
        		
        		// stateful services should NOT be flushed, because that's part 
        		// of the state that should hang around.
        		else if ( stateful ) 
        		{
        			// we don't want to do anything, really.
        		}
        		
        		// read-only sessions should not have anything changed.
        		else if ( readOnly )
	        	{
	        		ht.execute(new ClearIfDirtyAction(secSys));
	        	}
        		        		
        		// stateless services, don't keep their sesssions about.
        		else
        		{
	        		ht.flush();
	        		ht.execute(new CheckDirtyAction(secSys));
	        		ht.execute(new DisableFilterAction(secSys));
	        		ht.clear();
        			saveLogs();
        		} 
        		
        	} finally {
        		secSys.clearCurrentDetails();
        	}
        }

    }

    /** checks method (and as a fallback the class) for the Spring
     * {@link Transactional} annotation.  
     * 
     * @param mi Non-null method invocation.
     * @return true if the {@link Transactional} annotation lists this method
     * 	as read-only, or if no annotation is found. 
     */
    boolean checkReadOnly(MethodInvocation mi)
    {
    	AnnotationTransactionAttributeSource txSource = new 
    	AnnotationTransactionAttributeSource();
    	
    	TransactionAttribute ta =
    	txSource.getTransactionAttribute(mi.getMethod(), mi.getThis().getClass());
    	
    	return ta == null ? true : ta.isReadOnly();
    	
    }
    
    void saveLogs()
    {
		final SessionFactory sf = this.ht.getSessionFactory();
		this.ht.execute( new HibernateCallback() {
			public Object doInHibernate(Session session) 
			throws HibernateException
			{
				StatelessSession s = sf.openStatelessSession( session.connection() );

				Map<Class,Map<String,EventLog>> logs = secSys.getLogs();
				for (Class k : logs.keySet()) {
					Map<String,EventLog> m = logs.get(k);
					if ( m != null )
					for (EventLog l : m.values()) {
						s.insert( l );
					}
				}
				
				//s.close();
				return null;
			}			
		});
		
    }
    
}

// ~ Actions
// =============================================================================

/**
 * {@link HibernateCallback} which enables our read-security filter.
 */
class EnableFilterAction implements HibernateCallback 
{
	private SecuritySystem secSys;
	public EnableFilterAction( SecuritySystem sec )
	{
		this.secSys = sec;
	}
	public Object doInHibernate(Session session) 
	throws HibernateException, SQLException {
		secSys.enableReadFilter(session);
		return null;
	}
}

/**
 * {@link HibernateCallback} which disables our read-security filter.
 */
class DisableFilterAction implements HibernateCallback 
{
	private SecuritySystem secSys;
	public DisableFilterAction( SecuritySystem sec )
	{
		this.secSys = sec;
	}
	public Object doInHibernate(Session session) 
	throws HibernateException, SQLException {
		secSys.disableReadFilter(session);
		return null;
	}
}

/**
 * {@link HibernateCallback} which checks whether or not the session is dirty.
 * If so, an exception will be thrown.
 */
class ClearIfDirtyAction implements HibernateCallback
{
	private static Log log = LogFactory.getLog(ClearIfDirtyAction.class); 
	
	private SecuritySystem secSys;
	public ClearIfDirtyAction( SecuritySystem sec )
	{
		this.secSys = sec;
	}
	public Object doInHibernate(Session session) 
	throws HibernateException, SQLException {
		if (session.isDirty())
		{
			if (log.isDebugEnabled())
			{
				log.debug("Clearing dirty session.");
			}
			session.clear();
		}
		return null;
	}
}

/**
 * {@link HibernateCallback} which checks whether or not the session is dirty.
 * If so, an exception will be thrown.
 */
class CheckDirtyAction implements HibernateCallback
{
	private SecuritySystem secSys;
	public CheckDirtyAction( SecuritySystem sec )
	{
		this.secSys = sec;
	}
	public Object doInHibernate(Session session) 
	throws HibernateException, SQLException {
		if (session.isDirty())
		{
			throw new InternalException("Session is dirty. Cannot properly " +
					"reset security system. Must rollback.\n Session="+session);
		}
		return null;
	}
}
