/*
 * ome.security.basic.BasicSecuritySystem
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.basic;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.api.local.LocalAdmin;
import ome.api.local.LocalQuery;
import ome.api.local.LocalUpdate;
import ome.conditions.ApiUsageException;
import ome.conditions.InternalException;
import ome.conditions.SecurityViolation;
import ome.model.IObject;
import ome.model.enums.EventType;
import ome.model.internal.Details;
import ome.model.internal.GraphHolder;
import ome.model.internal.Permissions;
import ome.model.internal.Token;
import ome.model.meta.Event;
import ome.model.meta.EventLog;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.GroupExperimenterMap;
import ome.security.ACLVoter;
import ome.security.AdminAction;
import ome.security.SecureAction;
import ome.security.SecuritySystem;
import ome.security.SystemTypes;
import ome.services.sessions.SessionManager;
import ome.services.sessions.events.UserGroupUpdateEvent;
import ome.system.EventContext;
import ome.system.Principal;
import ome.system.Roles;
import ome.system.ServiceFactory;
import ome.tools.hibernate.ExtendedMetadata;
import ome.tools.hibernate.SecurityFilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
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
public class BasicSecuritySystem implements SecuritySystem {
    private final static Log log = LogFactory.getLog(BasicSecuritySystem.class);

    /**
     * private token for identifying loose "ownership" of certain objects.
     * 
     * @see IObject#getGraphHolder()
     * @see GraphHolder#hasToken()
     */
    private final Token token = new Token();

    /** active principal stack */

    protected final OmeroInterceptor interceptor;

    protected final SystemTypes sysTypes;

    protected final CurrentDetails cd;

    protected final TokenHolder tokenHolder;

    protected final PrincipalHolder principalHolder;

    protected final Roles roles;

    protected final ACLVoter acl;

    protected final SessionManager sessionManager;

    protected final ServiceFactory sf;

    /**
     * Simpilifed factory method which generates all the security primitives
     * internally. Primarily useful for generated testing instances.
     */
    public static BasicSecuritySystem selfConfigure(SessionManager sm,
            ServiceFactory sf) {
        CurrentDetails cd = new CurrentDetails();
        SystemTypes st = new SystemTypes();
        TokenHolder th = new TokenHolder();
        PrincipalHolder ph = new PrincipalHolder();
        OmeroInterceptor oi = new OmeroInterceptor(st, new ExtendedMetadata(),
                cd, th, ph);
        BasicSecuritySystem sec = new BasicSecuritySystem(oi, st, cd, sm,
                new Roles(), sf, new BasicACLVoter(cd, st, th),
                new TokenHolder(), ph);
        return sec;
    }

    /**
     * Main public constructor for this {@link SecuritySystem} implementation.
     */
    public BasicSecuritySystem(OmeroInterceptor interceptor,
            SystemTypes sysTypes, CurrentDetails cd,
            SessionManager sessionManager, Roles roles, ServiceFactory sf,
            ACLVoter acl, TokenHolder tokenHolder,
            PrincipalHolder principalHolder) {
        this.principalHolder = principalHolder;
        this.sessionManager = sessionManager;
        this.tokenHolder = tokenHolder;
        this.interceptor = interceptor;
        this.sysTypes = sysTypes;
        this.roles = roles;
        this.acl = acl;
        this.cd = cd;
        this.sf = sf;
    }

    // ~ Login/logout
    // =========================================================================

    public void login(Principal principal) {
        principalHolder.login(principal);
    }

    public int logout() {
        return principalHolder.logout();
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
        Session sess = (Session) session;
        sess.enableFilter(SecurityFilter.filterName).setParameter(
                SecurityFilter.is_admin, currentUserIsAdmin()).setParameter(
                SecurityFilter.current_user, currentUserId()).setParameterList(
                SecurityFilter.current_groups, memberOfGroups())
                .setParameterList(SecurityFilter.leader_of_groups,
                        leaderOfGroups());
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

    public void loadEventContext(boolean isReadOnly) {

        final LocalAdmin admin = (LocalAdmin) sf.getAdminService();
        final LocalUpdate update = (LocalUpdate) sf.getUpdateService();

        // Call to session manager throws an exception on failure
        final Principal p = clearAndCheckPrincipal();
        final EventContext ec = sessionManager.getEventContext(p);

        // start refilling current details
        cd.setReadOnly(isReadOnly);

        cd.setMemberOfGroups(ec.getMemberOfGroupsList());
        cd.setLeaderOfGroups(ec.getLeaderOfGroupsList());

        // Experimenter
        Experimenter exp;
        if (isReadOnly) {
            exp = new Experimenter(ec.getCurrentUserId(), false);
        } else {
            exp = admin.userProxy(ec.getCurrentUserId());
        }
        exp.getGraphHolder().setToken(token, token);
        cd.setOwner(exp);

        // Active group
        ExperimenterGroup grp;
        if (isReadOnly) {
            grp = new ExperimenterGroup(ec.getCurrentGroupId(), false);
        } else {
            grp = admin.groupProxy(ec.getCurrentGroupId());
        }

        if (!ec.getMemberOfGroupsList().contains(grp.getId())) {
            throw new SecurityViolation(String.format(
                    "User %s is not a member of group %s", p.getName(), p
                            .getGroup()));
        }
        grp.getGraphHolder().setToken(token, token);
        cd.setGroup(grp);

        // isAdmin
        if (roles.isSystemGroup(grp)) {
            cd.setAdmin(true);
        }

        // Event
        String t = p.getEventType();
        if (t == null) {
            t = ec.getCurrentEventType();
        }
        EventType type = new EventType(t);
        type.getGraphHolder().setToken(token, token);
        cd.newEvent(ec.getCurrentSessionId().longValue(), type, token);

        Event event = getCurrentEvent();
        event.getGraphHolder().setToken(token, token);

        // If this event is not read only, then lets save this event to prevent
        // flushing issues later.
        if (!isReadOnly) {
            setCurrentEvent(update.saveAndReturnObject(event));
        }
    }

    /**
     * Used by {@link EventHandler} to set the current {@link EventContext} so
     * it is not necessarily to have a valid context here like in
     * {@link #loadEventContext(boolean)}
     * 
     * @see SecuritySystem#setEventContext(EventContext)
     */
    public void setEventContext(EventContext context) {
        final Principal p = clearAndCheckPrincipal();

        if (!(context instanceof BasicEventContext)) {
            throw new ApiUsageException("BasicSecuritySystem can only accept "
                    + "BasicEventContext instances.");
        }

        final BasicEventContext bec = (BasicEventContext) context;
        final String u_name = bec.getCurrentUserName();
        final String g_name = bec.getCurrentGroupName();
        final String t_name = bec.getCurrentEventType();

        if (p.getName().equals(u_name) && p.getGroup().equals(g_name)
                && p.getEventType().equals(t_name)) {
            cd.setCurrentEventContext(bec);
        }

        else {
            throw new InternalException(String.format(
                    "Principal:%s/%s/%s does not match Context:%s/%s/%s", p
                            .getName(), p.getGroup(), p.getEventType(), u_name,
                    g_name, t_name));
        }
    }

    private Principal clearAndCheckPrincipal() {
        // clear even if this fails. (make SecuritySystem unusable)
        clearEventContext();

        if (principalHolder.size() == 0) {
            throw new SecurityViolation(
                    "Principal is null. Not logged in to SecuritySystem.");
        }

        final Principal p = principalHolder.getLast();

        if (p.getName() == null) {
            throw new InternalException(
                    "Principal.name is null. Security system failure.");
        }

        return p;
    }

    // TODO should possible set all or nothing.

    public Details createDetails() {
        return cd.createDetails();
    }

    public void newEvent(long sessionId, EventType type) {
        cd.newEvent(sessionId, type, token);
    }

    public void setCurrentEvent(Event event) {
        cd.setCreationEvent(event);
    }

    public void addLog(String action, Class klass, Long id) {

        Assert.notNull(action);
        Assert.notNull(klass);
        Assert.notNull(id);

        if (Event.class.isAssignableFrom(klass)
                || EventLog.class.isAssignableFrom(klass)) {
            log.debug("Not logging creation of logging type:" + klass);
        }

        else {
            checkReady("addLog");

            log.info("Adding log:" + action + "," + klass + "," + id);

            // CurrentDetails.getCreationEvent().addEventLog(l);
            cd.addLog(action, klass, id);
        }
    }

    public List<EventLog> getLogs() {
        return cd.getLogs();
    }

    public void clearLogs() {
        if (log.isDebugEnabled()) {
            log.debug("Clearing EventLogs.");
        }

        boolean foundAdminType = false;
        for (EventLog log : getLogs()) {
            String t = log.getEntityType();
            if (Experimenter.class.getName().equals(t)
                    || ExperimenterGroup.class.getName().equals(t)
                    || GroupExperimenterMap.class.getName().equals(t)) {
                foundAdminType = true;
            }
        }
        if (foundAdminType) {
            this.sessionManager.onApplicationEvent(new UserGroupUpdateEvent(
                    this));
        }
        cd.clearLogs();
    }

    public void clearEventContext() {
        if (log.isDebugEnabled()) {
            log.debug("Clearing EventContext.");
        }

        cd.clear();
    }

    // read-only ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * @see SecuritySystem#isEmptyEventContext()
     */
    public boolean isEmptyEventContext() {
        EventContext ctx = cd.getCurrentEventContext();
        // These are the only values which can be null checked in
        // EventContext. Others (like leaderOfGroups) are never null.
        return ctx.getCurrentEventId() == null
                && ctx.getCurrentGroupId() == null
                && ctx.getCurrentUserId() == null;
    }

    public Long currentUserId() {
        checkReady("currentUserId");
        return cd.getOwner().getId();
    }

    public Long currentGroupId() {
        checkReady("currentGroupId");
        return cd.getGroup().getId();
    }

    public Collection<Long> leaderOfGroups() {
        checkReady("leaderOfGroups");
        return cd.getLeaderOfGroups();
    }

    public Collection<Long> memberOfGroups() {
        checkReady("memberOfGroups");
        return cd.getMemberOfGroups();
    }

    public Experimenter currentUser() {
        checkReady("currentUser");
        return cd.getOwner();
    }

    public ExperimenterGroup currentGroup() {
        checkReady("currentGroup");
        return cd.getGroup();
    }

    public Event currentEvent() {
        checkReady("currentEvent");
        return cd.getCreationEvent();
    }

    public Event getCurrentEvent() {
        checkReady("getCurrentEvent");
        return cd.getCreationEvent();
    }

    public boolean currentUserIsAdmin() {
        checkReady("currentUserIsAdmin");
        return cd.isAdmin();
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

            // FIXME
            // Token oneTimeToken = new Token();
            // oneTimeTokens.put(oneTimeToken);
            ghs.add(obj.getGraphHolder());

        }

        // Holding onto the graph holders since they protect the access
        // to their tokens
        for (GraphHolder graphHolder : ghs) {
            graphHolder.setToken(token, token); // oneTimeToken
        }

        T retVal;
        try {
            retVal = action.updateObject(objs);
        } finally {
            for (GraphHolder graphHolder : ghs) {
                graphHolder.setToken(token, null);
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

        final LocalQuery query = (LocalQuery) sf.getQueryService();
        query.execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException, SQLException {

                boolean wasAdmin = cd.isAdmin();

                try {
                    cd.setAdmin(true);
                    disable(MergeEventListener.MERGE_EVENT);
                    enableReadFilter(session);
                    action.runAsAdmin();
                } finally {
                    cd.setAdmin(wasAdmin);
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

    public EventContext getEventContext() {
        return cd.getCurrentEventContext();
    }

    public ACLVoter getACLVoter() {
        return acl;
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

}
