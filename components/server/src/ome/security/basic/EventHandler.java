/*
 * ome.security.basic.EventHandler
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.security.basic;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ome.api.StatefulServiceInterface;
import ome.conditions.InternalException;
import ome.model.meta.EventLog;
import ome.system.EventContext;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.springframework.aop.ProxyMethodInvocation;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.util.Assert;

/**
 * method interceptor responsible for login and creation of Events. Calls are
 * made to the {@link BasicSecuritySystem} provided in the
 * {@link EventHandler#EventHandler(BasicSecuritySystem, HibernateTemplate) constructor}.
 * 
 * After the method is
 * {@link MethodInterceptor#invoke(MethodInvocation) invoked} various cleanup
 * actions are performed and finally all credentials all
 * {@link BasicSecuritySystem#invalidateEventContext() cleared} from the
 * {@link Thread}.
 * 
 * 
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @since 3.0
 */
public class EventHandler implements MethodInterceptor {

    private static Log log = LogFactory.getLog(EventHandler.class);

    protected final AnnotationTransactionAttributeSource txSource = new AnnotationTransactionAttributeSource();

    protected final BasicSecuritySystem secSys;

    protected HibernateTemplate ht;

    /**
     * only public constructor, used for dependency injection. Requires an
     * active {@link HibernateTemplate} and {@link BasicSecuritySystem}.
     * 
     * @param securitySystem
     *            Not null.
     * @param template
     *            Not null.
     */
    public EventHandler(BasicSecuritySystem securitySystem,
            HibernateTemplate template) {
        Assert.notNull(securitySystem);
        Assert.notNull(template);
        this.secSys = securitySystem;
        this.ht = template;
    }

    /**
     * invocation interceptor for prepairing this {@link Thread} for execution
     * and subsequently reseting it.
     * 
     * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
     */
    public Object invoke(MethodInvocation arg0) throws Throwable {
        boolean readOnly = checkReadOnly(arg0);
        boolean stateful = StatefulServiceInterface.class.isAssignableFrom(arg0
                .getThis().getClass());

        secSys.loadEventContext(readOnly);
        // now the user can be considered to be logged in.
        EventContext ec = secSys.getEventContext();
        if (log.isInfoEnabled()) {
            log.info(String.format("  Auth:\tuser=%s,group=%s,event=%s(%s)", ec
                    .getCurrentUserId(), ec.getCurrentGroupId(), ec
                    .getCurrentEventId(), ec.getCurrentEventType()));
        }

        boolean failure = false;
        Object retVal = null;
        try {
            ht.execute(new EnableFilterAction(secSys));
            retVal = arg0.proceed();
            saveLogs(readOnly);
            return retVal;
        } catch (Throwable ex) {
            failure = true;
            throw ex;
        } finally {
            try {

                // on failure, we want to make sure that no one attempts
                // any further changes.
                if (failure) {
                    // TODO we should probably do some forced clean up here.
                }

                // stateful services should NOT be flushed, because that's part
                // of the state that should hang around.
                else if (stateful) {
                    // we don't want to do anything, really.
                }

                // read-only sessions should not have anything changed.
                else if (readOnly) {
                    ht.execute(new ClearIfDirtyAction(secSys));
                }

                // stateless services, don't keep their sesssions about.
                else {
                    ht.flush();
                    ht.execute(new CheckDirtyAction(secSys));
                    ht.execute(new DisableFilterAction(secSys));
                    ht.clear();
                }

            } finally {
                secSys.invalidateEventContext();
            }
        }

    }

    /**
     * checks method (and as a fallback the class) for the Spring
     * {@link Transactional} annotation.
     * 
     * @param mi
     *            Non-null method invocation.
     * @return true if the {@link Transactional} annotation lists this method as
     *         read-only, or if no annotation is found.
     */
    boolean checkReadOnly(MethodInvocation mi) {

        // This user attribute can be set by an interceptor somewhere along the
        // stack up, to prevent this invocation from writing to the database.
        if (mi instanceof ProxyMethodInvocation) {
            ProxyMethodInvocation pmi = (ProxyMethodInvocation) mi;
            Object o = pmi.getUserAttribute("readOnly");
            if (o instanceof Boolean) {
                Boolean b = (Boolean) o;
                return b.booleanValue();
            }
        }
        TransactionAttribute ta = txSource.getTransactionAttribute(mi
                .getMethod(), mi.getThis().getClass());
        return ta == null ? true : ta.isReadOnly();

    }

    /**
     * Calling clearLogs posits that these EventLogs were successfully saved,
     * and so this method may raise an event signalling such. This could
     * eventually be reworked to be fully within the security system.
     */
    void saveLogs(boolean readOnly) {

        // Grabbing a copy to prevent ConcurrentModificationEx
        final List<EventLog> logs = new ArrayList<EventLog>(secSys.getLogs());
        secSys.clearLogs();

        if (logs == null || logs.size() == 0) {
            return; // EARLY EXIT
        }

        if (readOnly) {
            // If we reach here, we have logs when we shouldn't.
            StringBuilder sb = new StringBuilder();
            sb.append("EventLogs in readOnly transaction:\n");
            for (EventLog eventLog : logs) {
                sb.append(eventLog.getAction());
                sb.append(" ");
                sb.append(eventLog);
                sb.append(eventLog.getEntityType());
                sb.append(" ");
                sb.append(eventLog.getEntityId());
                sb.append("\b");
            }
            throw new InternalException(sb.toString());
        }

        final SessionFactory sf = this.ht.getSessionFactory();
        this.ht.execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {
                StatelessSession s = sf.openStatelessSession(session
                        .connection());

                for (EventLog l : logs) {
                    s.insert(l);
                }

                // s.close();
                return null;
            }
        });

        if (secSys.getLogs().size() > 0) {
            throw new InternalException("More logs present after saveLogs()");
        }

    }

}

// ~ Actions
// =============================================================================

/**
 * {@link HibernateCallback} which enables our read-security filter.
 */
class EnableFilterAction implements HibernateCallback {
    private final BasicSecuritySystem secSys;

    public EnableFilterAction(BasicSecuritySystem sec) {
        this.secSys = sec;
    }

    public Object doInHibernate(Session session) throws HibernateException,
            SQLException {
        secSys.enableReadFilter(session);
        return null;
    }
}

/**
 * {@link HibernateCallback} which disables our read-security filter.
 */
class DisableFilterAction implements HibernateCallback {
    private final BasicSecuritySystem secSys;

    public DisableFilterAction(BasicSecuritySystem sec) {
        this.secSys = sec;
    }

    public Object doInHibernate(Session session) throws HibernateException,
            SQLException {
        secSys.disableReadFilter(session);
        return null;
    }
}

/**
 * {@link HibernateCallback} which checks whether or not the session is dirty.
 * If so, an exception will be thrown.
 */
class ClearIfDirtyAction implements HibernateCallback {
    private static Log log = LogFactory.getLog(ClearIfDirtyAction.class);

    private final BasicSecuritySystem secSys;

    public ClearIfDirtyAction(BasicSecuritySystem sec) {
        this.secSys = sec;
    }

    public Object doInHibernate(Session session) throws HibernateException,
            SQLException {
        if (session.isDirty()) {
            if (log.isDebugEnabled()) {
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
class CheckDirtyAction implements HibernateCallback {
    private final BasicSecuritySystem secSys;

    public CheckDirtyAction(BasicSecuritySystem sec) {
        this.secSys = sec;
    }

    public Object doInHibernate(Session session) throws HibernateException,
            SQLException {
        boolean dirty = session.isDirty();
        if (dirty) {
            throw new InternalException("Session is dirty. Cannot properly "
                    + "reset security system. Must rollback.\n Session="
                    + session);
        }
        return null;
    }
}
