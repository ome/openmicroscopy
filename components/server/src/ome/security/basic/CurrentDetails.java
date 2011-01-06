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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ome.conditions.ApiUsageException;
import ome.conditions.InternalException;
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
import ome.services.util.ServiceHandler;
import ome.system.EventContext;
import ome.system.Principal;
import ome.tools.hibernate.HibernateUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

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
 * 
 */
public class CurrentDetails implements PrincipalHolder {

    private static Log log = LogFactory.getLog(CurrentDetails.class);

    private final SessionCache cache;

    private final ThreadLocal<LinkedList<BasicEventContext>> contexts = new ThreadLocal<LinkedList<BasicEventContext>>();

    private final ThreadLocal<Map<String, String>> callContext = new ThreadLocal<Map<String, String>>();

    /**
     * Default constructor. Should only be used for testing, since the stats
     * used will not be correct.
     */
    public CurrentDetails() {
        this.cache = null;
    }
    
    public CurrentDetails(SessionCache cache) {
        this.cache = cache;
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
        Map<String, String> rv = callContext.get();
        callContext.set(ctx);
        return rv;
    }

    public Long getCallGroup() {
        Map<String, String> ctx = callContext.get();
        if (ctx != null && ctx.containsKey("omero.group")) {
            String s = ctx.get("omero.group");
            try {
                return Long.valueOf(ctx.get("omero.group"));
            } catch (Exception e) {
                log.debug("Ignoring invalid omero.group context: " + s);
                return null;
            }
        }
        return null;
    }

    public void setCallGroup(Long id) {
        Map<String, String> ctx = callContext.get();
        if (ctx == null) {
            ctx = new HashMap<String, String>();
            callContext.set(ctx);
        }

        String old = ctx.get("omero.group.old");
        String curr = ctx.get("omero.group");
        if (old != null) {
            throw new RuntimeException("Recursive call! " +
                    String.format("Old: %s Current: %s New: %s",
                            old, curr, id));
        }

        ctx.put("omero.group.old", curr);
        ctx.put("omero.group", "" + id);
    }

    public void resetCallGroup() {
        Map<String, String> ctx = callContext.get();
        if (ctx != null) {
            String old = ctx.get("omero.group.old");
            ctx.remove("omero.group.old");
            ctx.put("omero.group", old);
        }
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
     * {@link Event}, and {@linkExperimenterGroup}, required for proper
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

    /**
     * @see SecuritySystem#isGraphCritical()
     * @return
     */
    public boolean isGraphCritical() {
        EventContext ec = getCurrentEventContext();
        long gid = ec.getCurrentGroupId();
        Permissions perms = ec.getCurrentGroupPermissions();

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
        final Long g = HibernateUtils.nullSafeGroupId(object);

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
     */
    void copy(EventContext ec) {
        current().copyContext(ec);
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
     * Public view on the data contained here. Used to create the
     * 
     * @return
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

    public Event newEvent(long sessionId, EventType type,
            TokenHolder tokenHolder) {
        BasicEventContext c = current();
        Event e = new Event();
        e.setType(type);
        e.setTime(new Timestamp(System.currentTimeMillis()));
        e.setExperimenter(c.getOwner());
        e.setExperimenterGroup(c.getGroup());
        tokenHolder.setToken(e.getGraphHolder());
        e.getDetails().setPermissions(Permissions.READ_ONLY);
        e.setSession(new Session(sessionId, false));
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
        d.setPermissions(new Permissions());
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
     * @return
     * @see <a href="https://trac.openmicroscopy.org.uk/trac/omero/ticket:1434">ticket:1434</a>
     */
    public Details createDetails() {
        BasicEventContext c = current();
        Details d = Details.create();
        d.setCreationEvent(c.getEvent());
        d.setUpdateEvent(c.getEvent());
        d.setOwner(c.getOwner());
        d.setGroup(c.getGroup());
        // ticket:1434
        Permissions groupPerms = c.getGroup().getDetails().getPermissions();
        Permissions userUmask = c.getCurrentUmask();
        Permissions p = new Permissions(groupPerms);
        p.revokeAll(userUmask);
        d.setPermissions(p);
        return d;
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
            boolean isAdmin, boolean isReadOnly, Long shareId) {
        BasicEventContext c = current();
        c.setOwner(owner);
        c.setGroup(group);
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
