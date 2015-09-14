/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.basic;

// Java imports
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import ome.api.local.LocalAdmin;
import ome.conditions.ApiUsageException;
import ome.conditions.InternalException;
import ome.conditions.SecurityViolation;
import ome.model.IObject;
import ome.model.enums.EventType;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.model.internal.Permissions.Right;
import ome.model.internal.Permissions.Role;
import ome.model.meta.Event;
import ome.model.meta.EventLog;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.Session;
import ome.security.SecuritySystem;
import ome.services.messages.RegisterServiceCleanupMessage;
import ome.services.sessions.SessionContext;
import ome.services.sessions.state.SessionCache;
import ome.services.sessions.stats.SessionStats;
import ome.services.sharing.ShareStore;
import ome.services.util.ServiceHandler;
import ome.system.EventContext;
import ome.system.Principal;
import ome.system.Roles;
import ome.tools.hibernate.HibernateUtils;

/**
 * Stores information related to the security context of the current thread.
 * Code calling into the server must setup CurrentDetails properly. An existing
 * user must be set (the creation of a new user is only allowed if the current
 * user is set to root; root always exists. QED.) The event must also be set.
 * Umask is optional.
 * 
 * This information is stored in a Details object, but unlike Details which
 * assumes that an empty value signifies increased security levels, empty values
 * here signifiy reduced security levels. E.g.,
 * 
 * Details: user == null ==> object belongs to root CurrentDetails: user == null
 * ==> current user is "nobody" (anonymous)
 */
public class CurrentDetails implements PrincipalHolder {

    private static Logger log = LoggerFactory.getLogger(CurrentDetails.class);

    private final SessionCache cache;

    private final Roles roles;

    private final ThreadLocal<LinkedList<BasicEventContext>> contexts = new ThreadLocal<LinkedList<BasicEventContext>>();

    /**
     * Call context set on the current details before login occurred. If this
     * is the case, then it will get consumed and
     */
    private final ThreadLocal<Map<String, String>> delayedCallContext
        = new ThreadLocal<Map<String, String>>();

    /**
     * Default constructor. Should only be used for testing, since the stats
     * used will not be correct.
     */
    public CurrentDetails() {
        this.cache = null;
        this.roles = new Roles();
    }

    public CurrentDetails(SessionCache cache) {
        this.cache = cache;
        this.roles = new Roles();
    }

    public CurrentDetails(SessionCache cache, Roles roles) {
        this.cache = cache;
        this.roles = roles;
    }

    private LinkedList<BasicEventContext> list() {
        LinkedList<BasicEventContext> list = contexts.get();
        if (list == null) {
            list = new LinkedList<BasicEventContext>();
            contexts.set(list);
        }
        return list;
    }

    // Method call context methods
    // =================================================================

    public Map<String, String> setContext(Map<String, String> ctx) {
        LinkedList<BasicEventContext> list = list();
        if (list.size() == 0) {
            delayedCallContext.set(ctx);
            return null;
        } else {
            return list.getLast().setCallContext(ctx);
        }
    }

    public Map<String, String> getContext() {
        return list().getLast().getCallContext();
    }

    protected void checkDelayedCallContext(BasicEventContext bec) {
        Map<String, String> ctx = delayedCallContext.get();
        delayedCallContext.set(null);
        bec.setCallContext(ctx);
    }

    // PrincipalHolder methods
    // =================================================================

    public int size() {
        return list().size();
    }

    public Principal getLast() {
        return list().getLast().getPrincipal();
    }

    public void login(Principal principal) {
        // Can't use the method in SessionManager since that leads to a
        // circular reference in Spring.
        final String uuid = principal.getName();
        final SessionContext ctx = cache.getSessionContext(uuid);
        final SessionStats stats = ctx.stats();
        final BasicEventContext c = new BasicEventContext(principal, stats);
        login(c);
    }

    /**
     * Login method which can be used by the security system to replace the
     * existing {@link BasicEventContext}.
     */
    public void login(BasicEventContext bec) {
        if (log.isDebugEnabled()) {
            log.debug("Logging in :" + bec);
        }
        checkDelayedCallContext(bec);
        list().add(bec);
        bec.getStats().methodIn();
    }

    public int logout() {
        LinkedList<BasicEventContext> list = list();
        BasicEventContext bec = list.removeLast();
        bec.getStats().methodOut();
        if (log.isDebugEnabled()) {
            log.debug("Logged out: " + bec);
        }
        return list.size();
    }

    // High-level methods used to fulfill {@link SecuritySystem}
    // =================================================================

    /**
     * Checks if the current {@link Thread} has non-null {@link Experimenter},
     * {@link Event}, and {@link ExperimenterGroup}, required for proper
     * functioning of the security system.
     */
    public boolean isReady() {
        BasicEventContext c = current();
        if (c.getEvent() != null && c.getGroup() != null
                && c.getOwner() != null) {
            return true;
        }
        return false;
    }

    public boolean isGraphCritical(Details details) {
        EventContext ec = getCurrentEventContext();
        long gid = ec.getCurrentGroupId();
        Permissions perms = ec.getCurrentGroupPermissions();
        if (gid < 0) {
            try {
                ExperimenterGroup g = details.getGroup();
                gid  = g.getId();
                perms = g.getDetails().getPermissions();
            } catch (NullPointerException npe) {
                throw new SecurityViolation("isGraphCriticalCheck: not enough context");
            }
            if (gid == roles.getUserGroupId()) {
                throw new SecurityViolation(
                    "isGraphCriticalCheck: Current group < 0 while accessing 'user' group!");
            }
        }

        boolean admin = ec.isCurrentUserAdmin();
        boolean pi = ec.getLeaderOfGroupsList().contains(gid);

        if (perms.isGranted(Role.WORLD, Right.READ)) {
            // Public groups (rwrwrw) are always non-critical
            return false;
        } else if (perms.isGranted(Role.GROUP, Right.READ)) {
            // Since the object will be contained in the group,
            // then it will be readable regardless.
            return false;
        } else {
            // This is a private group. Any form of admin modification is
            // critical.
            return admin || pi;
        }
    }

    public boolean isOwnerOrSupervisor(IObject object) {
        if (object == null) {
            throw new ApiUsageException("Object can't be null");
        }
        final Long o = HibernateUtils.nullSafeOwnerId(object);
        final Long g; // see 2874 and chmod
        if (object instanceof ExperimenterGroup) {
            g = object.getId();
        } else {
            g = HibernateUtils.nullSafeGroupId(object);
        }

        final EventContext ec = getCurrentEventContext();
        final boolean isAdmin = ec.isCurrentUserAdmin();
        final boolean isPI = ec.getLeaderOfGroupsList().contains(g);
        final boolean isOwner = ec.getCurrentUserId().equals(o);

        if (isAdmin || isPI || isOwner) {
            return true;
        }
        return false;
    }

    // State management
    // =================================================================

    /**
     * Replaces all the simple-valued fields in the {@link BasicEventContext}.
     * This method
     */
    void checkAndInitialize(EventContext ec, LocalAdmin admin, ShareStore store) {
        current().checkAndInitialize(ec, admin, store);
    }

    /**
     * Returns the current {@link BasicEventContext instance} throwing an
     * exception if there isn't one.
     */
    BasicEventContext current() {
        BasicEventContext c = list().getLast();
        return c;
    }

    /**
     * @return the current event context
     */
    public EventContext getCurrentEventContext() {
        return current();
    }

    /**
     * It suffices to set the {@link Details} to a new instance to make this
     * context unusable. {@link #isReady()} will return false.
     */
    public void invalidateCurrentEventContext() {
        BasicEventContext c = current();
        c.invalidate();
        if (log.isDebugEnabled()) {
            log.debug("Invalidated login: " + c);
        }
    }

    // ~ Events and Details
    // =================================================================

    public Event newEvent(Session session, EventType type,
            TokenHolder tokenHolder) {
        BasicEventContext c = current();
        Event e = new Event();
        e.setType(type);
        e.setTime(new Timestamp(System.currentTimeMillis()));
        tokenHolder.setToken(e.getGraphHolder());
        e.getDetails().setPermissions(Permissions.READ_ONLY);
        // Proxied if necessary
        e.setExperimenter(c.getOwner());
        e.setExperimenterGroup(c.getGroup());
        e.setSession(session);

        c.setEvent(e);
        return e;
    }

    public void addLog(String action, Class klass, Long id) {

        Assert.notNull(action);
        Assert.notNull(klass);
        Assert.notNull(id);

        if (Event.class.isAssignableFrom(klass)
                || EventLog.class.isAssignableFrom(klass)) {
            if (log.isDebugEnabled()) {
                log.debug("Not logging creation of logging type:" + klass);
            }
            return; // EARLY EXIT
        } else {
            if (!isReady()) {
                throw new InternalException("Not ready to add EventLog");
            }
        }

        if (log.isInfoEnabled()) {
            log.info("Adding log:" + action + "," + klass + "," + id);
        }

        BasicEventContext c = current();
        List<EventLog> list = current().getLogs();
        if (list == null) {
            list = new ArrayList<EventLog>();
            c.setLogs(list);
        }

        EventLog l = new EventLog();
        l.setAction(action);
        l.setEntityType(klass.getName()); // TODO could be id to Type entity
        l.setEntityId(id);
        l.setEvent(c.getEvent());
        Details d = Details.create();
        d.setPermissions(Permissions.WORLD_IMMUTABLE);
        l.getDetails().copy(d);
        list.add(l);
    }

    public SessionStats getStats() {
        return current().getStats();
    }

    public List<EventLog> getLogs() { // TODO defensive copy
        List<EventLog> logs = current().getLogs();
        return logs == null ? new ArrayList<EventLog>() : logs;
    }

    public void clearLogs() {
        current().setLogs(null);
    }

    /**
     * Creates a {@link Details} object for the current security context.
     *
     * The {@link Permissions} on the instance are calculated from the current
     * group as well as the user's umask.
     *
     * @return details for the current security context
     * @see <a href="https://trac.openmicroscopy.org.uk/trac/omero/ticket:1434">ticket:1434</a>
     */
    public Details createDetails() {
        final BasicEventContext c = current();
        final Details d = Details.create(new Object[]{c, c.getCallContext()});
        d.setCreationEvent(c.getEvent());
        d.setUpdateEvent(c.getEvent());
        d.setOwner(c.getOwner());
        d.setGroup(c.getGroup());
        // ticket:1434
        final Permissions groupPerms = c.getCurrentGroupPermissions();
        final Permissions p = new Permissions(groupPerms);
        d.setPermissions(p);
        return d;
    }

    public void applyContext(Details details, boolean changePerms) {
        final BasicEventContext c = current();
        details.setContexts(new Object[]{c, c.getCallContext()});
        if (changePerms) {
            // Make the permissions match (#8277)
            final Permissions groupPerms = c.getCurrentGroupPermissions();
            if (groupPerms != Permissions.DUMMY) {
                details.setPermissions(new Permissions(groupPerms));
            } else {
                // In the case of the dummy, we will be required to have
                // the group id already set in the context.
                ExperimenterGroup group = details.getGroup();
                if (group != null) {
                    // Systypes still will have DUMMY values.
                    Long gid = details.getGroup().getId();
                    Permissions p = c.getPermissionsForGroup(gid);
                    if (p != null) {
                        // Ticket:9505. This must be a new copy of the permissions
                        // in order to prevent the restrictions being modified by
                        // later objects!
                        details.setPermissions(new Permissions(p));
                    } else if (gid.equals(Long.valueOf(roles.getUserGroupId()))) {
                        details.setPermissions(new Permissions(Permissions.EMPTY));
                    } else {
                        throw new InternalException("No permissions: " + details);
                    }
                }
            }
        }

    }

    /**
     * Checks the "groupPermissions" map in {@link BasicEventContext} which has
     * been filled up by calls to {@link BasicEventContext#setPermissionsForGroup(Long, Permissions)}
     * during {@link BasicACLVoter#allowLoad(org.hibernate.Session, Class, Details, long)}.
     * @param session the Hibernate session
     */
    public void loadPermissions(org.hibernate.Session session) {
        current().loadPermissions(session);
    }

    public Experimenter getOwner() {
        return current().getOwner();
    }

    public ExperimenterGroup getGroup() {
        return current().getGroup();
    }

    public Event getEvent() {
        return current().getEvent();
    }

    /**
     * Take all values during loadEventContext() in one shot. Event is set
     * during {@link #newEvent(long, EventType, TokenHolder)} and possibly
     * updated via {@link #updateEvent(Event)}.
     */
    void setValues(Experimenter owner, ExperimenterGroup group,
            Permissions perms,
            boolean isAdmin, boolean isReadOnly, Long shareId) {
        BasicEventContext c = current();
        c.setOwner(owner);
        c.setGroup(group, perms);
        c.setAdmin(isAdmin);
        c.setReadOnly(isReadOnly);
        c.setShareId(shareId);
    }

    /**
     * Allows for updating the {@link Event} if the session is not readOnly.
     */
    void updateEvent(Event event) {
        current().setEvent(event);
    }

    // ~ Cleanups
    // =========================================================================

    /**
     * Add a {@link RegisterServiceCleanupMessage} to the current thread for
     * cleanup by the {@link ServiceHandler} on exit.
     */
    public void addCleanup(RegisterServiceCleanupMessage cleanup) {
        Set<RegisterServiceCleanupMessage> cleanups = current()
                .getServiceCleanups();
        if (cleanups == null) {
            cleanups = new HashSet<RegisterServiceCleanupMessage>();
            current().setServiceCleanups(cleanups);
        }
        cleanups.add(cleanup);
    }

    /**
     * Returns the current cleanups and resets the {@link Set}. Instances can
     * most likely only be closed once, so it doesn't make sense to keep them
     * around. The first caller of this method is responsible for closing all of
     * them.
     * @return a new copy of the current cleanups
     */
    public Set<RegisterServiceCleanupMessage> emptyCleanups() {
        Set<RegisterServiceCleanupMessage> set = current().getServiceCleanups();
        if (current().getServiceCleanups() == null) {
            return Collections.emptySet();
        } else {
            Set<RegisterServiceCleanupMessage> copy = new HashSet<RegisterServiceCleanupMessage>(
                    set);
            set.clear();
            return copy;
        }
    }

    // ~ Subsystems
    // =========================================================================

    public boolean addDisabled(String id) {
        Set<String> s = current().getDisabledSubsystems();
        if (s == null) {
            s = new HashSet<String>();
            current().setDisabledSubsystems(s);
        }
        return s.add(id);
    }

    public boolean addAllDisabled(String... ids) {
        Set<String> s = current().getDisabledSubsystems();
        if (s == null) {
            s = new HashSet<String>();
            current().setDisabledSubsystems(s);
        }
        if (ids != null) {
            return Collections.addAll(s, ids);
        }
        return false;

    }

    public boolean removeDisabled(String id) {
        Set<String> s = current().getDisabledSubsystems();
        if (s != null && id != null) {
            return s.remove(id);
        }
        return false;
    }

    public boolean removeAllDisabled(String... ids) {
        Set<String> s = current().getDisabledSubsystems();
        if (s != null && ids != null) {
            boolean changed = false;
            for (String string : ids) {
                changed |= s.remove(string);
            }
        }
        return false;
    }

    public void clearDisabled() {
        current().setDisabledSubsystems(null);
    }

    public boolean isDisabled(String id) {
        if (size() == 0) {
            // The security system is not active, so nothing can have
            // been "disabled"
            return false;
        } else {
            Set<String> s = current().getDisabledSubsystems();
            if (s == null || id == null || !s.contains(id)) {
                return false;
            }
            return true;
        }

    }

}
