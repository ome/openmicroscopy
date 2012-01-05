/*
 * ome.security.basic.BasicSecuritySystem
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.basic;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.api.local.LocalAdmin;
import ome.api.local.LocalQuery;
import ome.api.local.LocalUpdate;
import ome.conditions.ApiUsageException;
import ome.conditions.InternalException;
import ome.conditions.SecurityViolation;
import ome.conditions.SessionTimeoutException;
import ome.model.IObject;
import ome.model.enums.EventType;
import ome.model.internal.Details;
import ome.model.internal.GraphHolder;
import ome.model.internal.Permissions;
import ome.model.internal.Permissions.Right;
import ome.model.internal.Permissions.Role;
import ome.model.internal.Token;
import ome.model.meta.Event;
import ome.model.meta.EventLog;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.GroupExperimenterMap;
import ome.model.roi.Shape;
import ome.security.AdminAction;
import ome.security.SecureAction;
import ome.security.SecuritySystem;
import ome.security.SystemTypes;
import ome.services.messages.EventLogMessage;
import ome.services.messages.ShapeChangeMessage;
import ome.services.sessions.SessionManager;
import ome.services.sessions.events.UserGroupUpdateEvent;
import ome.services.sessions.stats.PerSessionStats;
import ome.system.EventContext;
import ome.system.OmeroContext;
import ome.system.Principal;
import ome.system.Roles;
import ome.system.ServiceFactory;
import ome.tools.hibernate.ExtendedMetadata;
import ome.tools.hibernate.SecurityFilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Filter;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.util.Assert;

/**
 * simplest implementation of {@link SecuritySystem}. Uses an ctor-injected
 * {@link EventContext} and the {@link ThreadLocal ThreadLocal-}based
 * {@link CurrentDetails} to provide the security infrastructure.
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @version $Revision: 1581 $, $Date: 2007-06-02 12:31:30 +0200 (Sat, 02 Jun
 *          2007) $
 * @see Token
 * @see SecuritySystem
 * @see Details
 * @see Permissions
 * @since 3.0-M3
 */
@RevisionDate("$Date: 2007-06-02 12:31:30 +0200 (Sat, 02 Jun 2007) $")
@RevisionNumber("$Revision: 1581 $")
public class BasicSecuritySystem implements SecuritySystem,
        ApplicationContextAware, ApplicationListener<EventLogMessage> {

    private final static Log log = LogFactory.getLog(BasicSecuritySystem.class);

    protected final OmeroInterceptor interceptor;

    protected final SystemTypes sysTypes;

    protected final CurrentDetails cd;

    protected final TokenHolder tokenHolder;

    protected final Roles roles;

    protected final SessionManager sessionManager;

    protected final ServiceFactory sf;

    protected/* final */OmeroContext ctx;

    /**
     * Simpilifed factory method which generates all the security primitives
     * internally. Primarily useful for generated testing instances.
     */
    public static BasicSecuritySystem selfConfigure(SessionManager sm,
            ServiceFactory sf) {
        CurrentDetails cd = new CurrentDetails();
        SystemTypes st = new SystemTypes();
        TokenHolder th = new TokenHolder();
        OmeroInterceptor oi = new OmeroInterceptor(new Roles(),
                st, new ExtendedMetadata.Impl(),
                cd, th, new PerSessionStats(cd));
        BasicSecuritySystem sec = new BasicSecuritySystem(oi, st, cd, sm,
                new Roles(), sf, new TokenHolder());
        return sec;
    }

    /**
     * Main public constructor for this {@link SecuritySystem} implementation.
     */
    public BasicSecuritySystem(OmeroInterceptor interceptor,
            SystemTypes sysTypes, CurrentDetails cd,
            SessionManager sessionManager, Roles roles, ServiceFactory sf,
            TokenHolder tokenHolder) {
        this.sessionManager = sessionManager;
        this.tokenHolder = tokenHolder;
        this.interceptor = interceptor;
        this.sysTypes = sysTypes;
        this.roles = roles;
        this.cd = cd;
        this.sf = sf;
    }

    public void setApplicationContext(ApplicationContext arg0)
            throws BeansException {
        this.ctx = (OmeroContext) arg0;
    }

    // ~ Login/logout
    // =========================================================================

    public void login(Principal principal) {
        cd.login(principal);
    }

    public int logout() {
        return cd.logout();
    }

    // ~ Checks
    // =========================================================================
    /**
     * implements {@link SecuritySystem#isReady()}. Simply checks for null
     * values in all the relevant fields of {@link CurrentDetails}
     */
    public boolean isReady() {
        return cd.isReady();
    }

    /**
     * classes which cannot be created by regular users.
     * 
     * @see <a
     *      href="https://trac.openmicroscopy.org.uk/omero/ticket/156">ticket156</a>
     */
    public boolean isSystemType(Class<? extends IObject> klass) {
        return sysTypes.isSystemType(klass);
    }

    /**
     * tests whether or not the current user is either the owner of this entity,
     * or the superivsor of this entity, for example as root or as group owner.
     * 
     * @param iObject
     *            Non-null managed entity.
     * @return true if the current user is owner or supervisor of this entity
     */
    public boolean isOwnerOrSupervisor(IObject iObject) {
        return cd.isOwnerOrSupervisor(iObject);
    }

    // ~ Read security
    // =========================================================================
    /**
     * enables the read filter such that graph queries will have non-visible
     * entities silently removed from the return value. This filter does <em>
     * not</em>
     * apply to single value loads from the database. See
     * {@link #allowLoad(Class, Details)} for more.
     * 
     * Note: this filter must be disabled on logout, otherwise the necessary
     * parameters (current user, current group, etc.) for building the filters
     * will not be available. Similarly, while enabling this filter, no calls
     * should be made on the given session object.
     * 
     * @param session
     *            a generic session object which can be used to enable this
     *            filter. Each {@link SecuritySystem} implementation will
     *            require a specific session type.
     * @see EventHandler#invoke(org.aopalliance.intercept.MethodInvocation)
     */
    public void enableReadFilter(Object session) {
        if (session == null || !(session instanceof Session)) {
            throw new ApiUsageException(
                    "The Object argument to enableReadFilter"
                            + " in the BasicSystemSecurity implementation must be a "
                            + " non-null org.hibernate.Session.");
        }

        checkReady("enableReadFilter");
        // beware
        // http://opensource.atlassian.com/projects/hibernate/browse/HHH-1932
        EventContext ec = getEventContext();
        Session sess = (Session) session;
        Filter filter = sess.enableFilter(SecurityFilter.filterName);

        Long shareId = ec.getCurrentShareId();
        int share01 = shareId != null ? 1 : 0;

        int admin01 = (ec.isCurrentUserAdmin() ||
                ec.getLeaderOfGroupsList().contains(ec.getCurrentGroupId()))
                ? 1 : 0;

        int nonpriv01 = (ec.getCurrentGroupPermissions().isGranted(Role.GROUP, Right.READ)
                || ec.getCurrentGroupPermissions().isGranted(Role.WORLD, Right.READ))
                ? 1 : 0;

        filter.setParameter(SecurityFilter.is_share, share01); // ticket:2219, not checking -1 here.
        filter.setParameter(SecurityFilter.is_adminorpi, admin01);
        filter.setParameter(SecurityFilter.is_nonprivate, nonpriv01);
        filter.setParameter(SecurityFilter.current_group, ec.getCurrentGroupId());
        filter.setParameter(SecurityFilter.current_user, ec.getCurrentUserId());
    }

    public void  updateReadFilter(Session session) {
        session.disableFilter(SecurityFilter.filterName);
        enableReadFilter(session);
    }

    /**
     * disable this filer. All future queries will have no security context
     * associated with them and all items will be visible.
     * 
     * @param session
     *            a generic session object which can be used to disable this
     *            filter. Each {@link SecuritySystem} implementation will
     *            require a specifc session type.
     * @see EventHandler#invoke(org.aopalliance.intercept.MethodInvocation)
     */
    public void disableReadFilter(Object session) {
        // Session system doesn't seem to provide this
        // i.e. isReady() is false here. Disabling but need to review
        // checkReady("disableReadFilter");

        Session sess = (Session) session;
        sess.disableFilter(SecurityFilter.filterName);
    }

    // ~ Subsystem disabling
    // =========================================================================

    public void disable(String... ids) {
        if (ids == null || ids.length == 0) {
            throw new ApiUsageException("Ids should not be empty.");
        }
        cd.addAllDisabled(ids);
    }

    public void enable(String... ids) {
        if (ids == null || ids.length == 0) {
            cd.clearDisabled();
        }
        cd.removeAllDisabled(ids);
    }

    public boolean isDisabled(String id) {
        if (id == null) {
            throw new ApiUsageException("Id should not be null.");
        }
        return cd.isDisabled(id);
    }

    // OmeroInterceptor delegation
    // =========================================================================

    public Details newTransientDetails(IObject object)
            throws ApiUsageException, SecurityViolation {
        checkReady("transientDetails");
        return interceptor.newTransientDetails(object);
    }

    public Details checkManagedDetails(IObject object, Details trustedDetails)
            throws ApiUsageException, SecurityViolation {
        checkReady("managedDetails");
        return interceptor.checkManagedDetails(object, trustedDetails);
    }

    // ~ CurrentDetails delegation (ensures proper settings of Tokens)
    // =========================================================================


    public boolean isGraphCritical() {
        checkReady("isGraphCritical");
        return cd.isGraphCritical();
    }

    public void loadEventContext(boolean isReadOnly) {
        loadEventContext(isReadOnly, false);
    }

    public void loadEventContext(boolean isReadOnly, boolean isClose) {

        final LocalAdmin admin = (LocalAdmin) sf.getAdminService();
        final LocalUpdate update = (LocalUpdate) sf.getUpdateService();

        // Call to session manager throws an exception on failure
        final Principal p = clearAndCheckPrincipal();

        // ticket:6639 - Rather than catch the RemoveSessionException
        // we are going to check the type of the context and if it
        // matches, then we know we should do no more loading.
        EventContext ec = cd.getCurrentEventContext();
        if (ec instanceof BasicSecurityWiring.CloseOnNoSessionContext) {
            throw new SessionTimeoutException("closing", ec);
        }

        // ticket:1855 - Catching SessionTimeoutException in order to permit
        // the close of a stateful service.
        try {
            ec = sessionManager.getEventContext(p);
        } catch (SessionTimeoutException ste) {
            if (!isClose) {
                throw ste;
            }
            ec = (EventContext) ste.sessionContext;
        }

        // Refill current details
        cd.copy(ec);

        // Experimenter
        Experimenter exp;
        if (isReadOnly) {
            exp = new Experimenter(ec.getCurrentUserId(), false);
        } else {
            exp = admin.userProxy(ec.getCurrentUserId());
        }
        tokenHolder.setToken(exp.getGraphHolder());

        // isAdmin
        boolean isAdmin = false;
        for (long gid : ec.getMemberOfGroupsList()) {
            if (roles.getSystemGroupId() == gid) {
                isAdmin = true;
                break;
            }
        }

        // Active group
        ExperimenterGroup grp;
        Long groupId = cd.getCallGroup();
        Long shareId = ec.getCurrentShareId();
        if (groupId == null) {
            groupId = ec.getCurrentGroupId();
        } else {
            if (groupId >= 0) {
                log.debug("Using call-requested group: " + groupId);
            } else {
                // ticket:2950
                if (!isAdmin) {
                    throw new SecurityViolation("Only administrators can use negative groups!");
                }
                log.info("Setting share id to -1");
                shareId = -1L;
                groupId = ec.getCurrentGroupId();
            }
        }

        if (isReadOnly) {
            grp = new ExperimenterGroup(groupId, false);
        } else {
            grp = admin.groupProxy(groupId);
        }

        long sessionId = ec.getCurrentSessionId().longValue();
        ome.model.meta.Session sess = null;
        if (isReadOnly) {
            sess = new ome.model.meta.Session(sessionId, false);
        } else {
            sess = sf.getQueryService().get(ome.model.meta.Session.class, sessionId);
        }

        // public groups (ticket:1940)
        if (!isAdmin && !ec.getMemberOfGroupsList().contains(grp.getId())) {
            // Only force loading the group if we would otherwise throw an exception.
            // The extra performance hit on READ is just the price of browsing
            // public data
            ExperimenterGroup publicGroup = admin.groupProxy(groupId);
            if (!publicGroup.getDetails().getPermissions().isGranted(Role.WORLD, Right.READ)) {
                throw new SecurityViolation(String.format(
                    "User %s is not a member of group %s and cannot login",
                            ec.getCurrentUserId(), grp.getId()));
            }
        }
        tokenHolder.setToken(grp.getGraphHolder());

        // In order to less frequently access the ThreadLocal in CurrentDetails
        // All properities are now set in one shot, except for Event.
        cd.setValues(exp, grp, isAdmin, isReadOnly, shareId);

        // Event
        String t = p.getEventType();
        if (t == null) {
            t = ec.getCurrentEventType();
        }
        EventType type = new EventType(t);
        tokenHolder.setToken(type.getGraphHolder());
        Event event = cd.newEvent(sess, type, tokenHolder);
        tokenHolder.setToken(event.getGraphHolder());

        // If this event is not read only, then lets save this event to prevent
        // flushing issues later.
        if (!isReadOnly) {
            cd.updateEvent(update.saveAndReturnObject(event)); // TODO use merge
        }

    }

    private Principal clearAndCheckPrincipal() {

        // clear even if this fails. (make SecuritySystem unusable)
        invalidateEventContext();

        if (cd.size() == 0) {
            throw new SecurityViolation(
                    "Principal is null. Not logged in to SecuritySystem.");
        }

        final Principal p = cd.getLast();

        if (p.getName() == null) {
            throw new InternalException(
                    "Principal.name is null. Security system failure.");
        }

        return p;
    }

    public void addLog(String action, Class klass, Long id) {
        cd.addLog(action, klass, id);
    }

    public List<EventLog> getLogs() {
        return cd.getLogs();
    }

    public void clearLogs() {
        if (log.isDebugEnabled()) {
            log.debug("Clearing EventLogs.");
        }

        boolean foundAdminType = false;
        List<EventLog> foundShapes = new ArrayList<EventLog>();
        for (EventLog log : getLogs()) {
            String t = log.getEntityType();
            String a = log.getAction();
            if (Experimenter.class.getName().equals(t)
                    || ExperimenterGroup.class.getName().equals(t)
                    || GroupExperimenterMap.class.getName().equals(t)) {
                foundAdminType = true;
            }
            try {
                if (Shape.class.isAssignableFrom(Class.forName(t))) {
                    if ("INSERT".equals(a) || "UPDATE".equals(a)) {
                        foundShapes.add(log);
                    }
                }
            } catch (ClassNotFoundException e) {
                throw new InternalException("Shape != Class.forName: " + t);
            }
        }
        // publish message if administrative type is modified
        if (foundAdminType) {
            if (ctx == null) {
                log.error("No context found for publishing");
            } else {
                this.ctx.publishEvent(new UserGroupUpdateEvent(this));
            }
        }
        // publish message if shape is created or updated
        if (foundShapes.size() > 0) {
            this.ctx.publishEvent(new ShapeChangeMessage(this, foundShapes));
        }
        
        cd.clearLogs();
    }

    public void invalidateEventContext() {
        if (log.isDebugEnabled()) {
            log.debug("Invalidating current EventContext.");
        }
        cd.invalidateCurrentEventContext();
    }

    // ~ Tokens & Actions
    // =========================================================================

    /**
     * 
     * It would be better to catch the
     * {@link SecureAction#updateObject(IObject)} method in a try/finally block,
     * but since flush can be so poorly controlled that's not possible. instead,
     * we use the one time token which is removed this Object is checked for
     * {@link #hasPrivilegedToken(IObject) privileges}.
     * 
     * @param obj
     *            A managed (non-detached) entity. Not null.
     * @param action
     *            A code-block that will be given the entity argument with a
     *            {@link #hasPrivilegedToken(IObject)} privileged token}.
     */
    public <T extends IObject> T doAction(SecureAction action, T... objs) {
        Assert.notNull(objs);
        Assert.notEmpty(objs);
        Assert.notNull(action);

        final LocalQuery query = (LocalQuery) sf.getQueryService();
        final List<GraphHolder> ghs = new ArrayList<GraphHolder>();

        for (T obj : objs) {

            // TODO inject
            if (obj.getId() != null && !query.contains(obj)) {
                throw new SecurityViolation("Services are not allowed to call "
                        + "doAction() on non-Session-managed entities.");
            }

            // ticket:1794 - use of IQuery.get along with doAction() creates
            // two objects (outer proxy and inner target) and only the outer
            // proxy has its graph holder modified without this block, leading
            // to security violations on flush since no token is present.
            if (obj instanceof HibernateProxy) {
                HibernateProxy hp = (HibernateProxy) obj;
                IObject obj2 = (IObject) hp.getHibernateLazyInitializer().getImplementation();
                ghs.add(obj2.getGraphHolder());
            }

            // FIXME
            // Token oneTimeToken = new Token();
            // oneTimeTokens.put(oneTimeToken);
            ghs.add(obj.getGraphHolder());

        }

        // Holding onto the graph holders since they protect the access
        // to their tokens
        for (GraphHolder graphHolder : ghs) {
            tokenHolder.setToken(graphHolder); // oneTimeToken
        }

        T retVal;
        try {
            retVal = action.updateObject(objs);
        } finally {
            for (GraphHolder graphHolder : ghs) {
                tokenHolder.clearToken(graphHolder);
            }
        }
        return retVal;
    }

    /**
     * merge event is disabled for {@link #runAsAdmin(AdminAction)} because
     * passing detached (client-side) entities to this method is particularly
     * dangerous.
     */
    public void runAsAdmin(final AdminAction action) {
        Assert.notNull(action);

        // Need to check here so that no exception is thrown
        // during the try block below
        checkReady("runAsAdmin");

        final LocalQuery query = (LocalQuery) sf.getQueryService();
        query.execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException, SQLException {

                BasicEventContext c = cd.current();
                boolean wasAdmin = c.isCurrentUserAdmin();

                try {
                    c.setAdmin(true);
                    disable(MergeEventListener.MERGE_EVENT);
                    enableReadFilter(session);
                    action.runAsAdmin();
                } finally {
                    c.setAdmin(wasAdmin);
                    enable(MergeEventListener.MERGE_EVENT);
                    enableReadFilter(session); // Now as non-admin
                }
                return null;
            }
        });
    }

    /**
     * See {@link TokenHolder#copyToken(IObject, IObject)

     */
    public void copyToken(IObject source, IObject copy) {
        tokenHolder.copyToken(source, copy);
    }

    /**
     * See {@link TokenHolder#hasPrivilegedToken(IObject)

     */
    public boolean hasPrivilegedToken(IObject obj) {
        return tokenHolder.hasPrivilegedToken(obj);
    }

    // ~ Configured Elements
    // =========================================================================

    public Roles getSecurityRoles() {
        return roles;
    }

    public EventContext getEventContext(boolean refresh) {
        EventContext ec = cd.getCurrentEventContext();
        if (refresh) {
            String uuid = ec.getCurrentSessionUuid();
            ec = sessionManager.reload(uuid);
        }
        return ec;
    }

    public EventContext getEventContext() {
        return getEventContext(false);
    }

    // ~ Helpers
    // =========================================================================

    /**
     * calls {@link #isReady()} and if not throws an {@link ApiUsageException}.
     * The {@link SecuritySystem} must be in a valid state to perform several
     * functions.
     */
    protected void checkReady(String method) {
        if (!isReady()) {
            throw new ApiUsageException("The security system is not ready.\n"
                    + "Cannot execute: " + method);
        }

    }

    public void onApplicationEvent(EventLogMessage elm) {
        if (elm != null) {
            for (Long id : elm.entityIds) {
                addLog(elm.action, elm.entityType, id);
            }
        }
    }

}
