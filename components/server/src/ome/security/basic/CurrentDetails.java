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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ome.conditions.ApiUsageException;
import ome.model.IObject;
import ome.model.enums.EventType;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.model.meta.Event;
import ome.model.meta.EventLog;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.Session;
import ome.system.EventContext;
import ome.tools.hibernate.HibernateUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
public class CurrentDetails {
    private static Log log = LogFactory.getLog(CurrentDetails.class);

    private final ThreadLocal<BasicEventContext> data = new InheritableThreadLocal<BasicEventContext>() {
        @Override
        protected BasicEventContext initialValue() {
            return new BasicEventContext();
        };
    };

    // High-level methods used to fulfill {@link SecuritySystem}
    // =================================================================

    /**
     * Checks if the current {@link Thread} has non-null {@link Experimenter},
     * {@link Event}, and {@linkExperimenterGroup}, required for proper
     * functioning of the security system.
     */
    public boolean isReady() {
        if (getCreationEvent() != null && getGroup() != null
                && getOwner() != null) {
            return true;
        }
        return false;
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
     * removes all current context. This must stay in sync with the instance
     * fields. If a new {@link ThreadLocal} is added,
     * {@link ThreadLocal#remove()} <em>must</em> be called.
     */
    public void clear() {
        data.remove();
    }

    public EventContext getCurrentEventContext() {
        return data.get();
    }

    void setCurrentEventContext(BasicEventContext bec) {
        data.set(bec);
    }

    private Details getDetails() {
        return data.get().getDetails();
    }

    // ~ Events and Details
    // =================================================================
    // TODO keep up with stack here?
    public void newEvent(long sessionId, EventType type, TokenHolder tokenHolder) {
        Event e = new Event();
        e.setType(type);
        e.setTime(new Timestamp(System.currentTimeMillis()));
        e.setExperimenter(getOwner());
        e.setExperimenterGroup(getGroup());
        tokenHolder.setToken(e.getGraphHolder());
        e.getDetails().setPermissions(Permissions.READ_ONLY);
        e.setSession(new Session(sessionId, false));
        setCreationEvent(e);
    }

    public void addLog(String action, Class klass, Long id) {
        List<EventLog> list = data.get().logs;
        if (list == null) {
            list = new ArrayList<EventLog>();
            data.get().logs = list;
        }

        EventLog l = new EventLog();
        l.setAction(action);
        l.setEntityType(klass.getName()); // TODO could be id to Type entity
        l.setEntityId(id);
        l.setEvent(getCreationEvent());
        Details d = Details.create();
        d.setPermissions(new Permissions());
        l.getDetails().copy(d);
        list.add(l);
    }

    public List<EventLog> getLogs() { // TODO defensive copy
        return data.get().logs == null ? new ArrayList<EventLog>()
                : data.get().logs;
    }

    public void clearLogs() {
        data.get().logs = null;
        // getCreationEvent().clearLogs();
    }

    // TODO move to BSS
    public Details createDetails() {
        Details d = Details.create();
        d.setCreationEvent(getCreationEvent());
        d.setUpdateEvent(getCreationEvent());
        d.setOwner(getOwner());
        d.setGroup(getGroup());
        d.setPermissions(getUmask());
        return d;
    }

    // ~ Umask
    // =========================================================================
    public Permissions getUmask() {
        Permissions umask = data.get().umask;
        if (umask == null) {
            umask = new Permissions();
            setUmask(umask);
        }
        return umask;
        /*
         * FIXME should be configurable see
         * https://trac.openmicroscopy.org.uk/omero/ticket/179
         * getOwner().getProfile().getUmask object.getDetails().getUmask()
         * CurrentDetails.getDetails().getUmask();
         */
    }

    public void setUmask(Permissions umask) {
        data.get().umask = umask;
    }

    // ~ Delegation FIXME possibly remove setters for set(Exp,Grp)
    // =========================================================================

    public void setShareId(Long shareId) {
        this.data.get().shareId = shareId;
    }

    public Event getCreationEvent() {
        return getDetails().getCreationEvent();
    }

    public Experimenter getOwner() {
        return getDetails().getOwner();
    }

    public Permissions getPermissions() {
        return getDetails().getPermissions();
    }

    public Event getUpdateEvent() {
        return getDetails().getUpdateEvent();
    }

    public void setCreationEvent(Event e) {
        getDetails().setCreationEvent(e);
    }

    public void setOwner(Experimenter exp) {
        getDetails().setOwner(exp);
    }

    public void setPermissions(Permissions perms) {
        getDetails().setPermissions(perms);
    }

    // TODO hide these specifics. possibly also Owner->User & CreationEvent ->
    // Event
    public void setUpdateEvent(Event e) {
        getDetails().setUpdateEvent(e);
    }

    public ExperimenterGroup getGroup() {
        return getDetails().getGroup();
    }

    public void setGroup(ExperimenterGroup group) {
        getDetails().setGroup(group);
    }

    // ~ Admin
    // =========================================================================

    public void setAdmin(boolean isAdmin) {
        data.get().isAdmin = isAdmin;
    }

    public boolean isAdmin() {
        return data.get().isAdmin;
    }

    // ~ ReadOnly
    // =========================================================================

    public void setReadOnly(boolean isReadOnly) {
        data.get().isReadOnly = isReadOnly;
    }

    public boolean isReadOnly() {
        return data.get().isReadOnly;
    }

    // ~ Groups
    // =========================================================================

    public void setMemberOfGroups(Collection<Long> groupIds) {
        data.get().memberOfGroups = groupIds;
    }

    public Collection<Long> getMemberOfGroups() {
        Collection<Long> c = data.get().memberOfGroups;
        if (c == null || c.size() == 0) {
            c = Collections.singletonList(Long.MIN_VALUE); // FIXME hack as
            // well.
        }
        return c;
    }

    public void setLeaderOfGroups(Collection<Long> groupIds) {
        data.get().leaderOfGroups = groupIds;
    }

    public Collection<Long> getLeaderOfGroups() {
        Collection<Long> c = data.get().leaderOfGroups;
        if (c == null || c.size() == 0) {
            c = Collections.singletonList(Long.MIN_VALUE); // FIXME hack as
            // well.
        }
        return c;
    }

    // ~ Subsystems
    // =========================================================================

    public boolean addDisabled(String id) {
        Set<String> s = data.get().disabledSubsystems;
        if (s == null) {
            s = new HashSet<String>();
            data.get().disabledSubsystems = s;
            return s.add(id);
        }
        return false;

    }

    public boolean addAllDisabled(String... ids) {
        Set<String> s = data.get().disabledSubsystems;
        if (s == null) {
            s = new HashSet<String>();
            data.get().disabledSubsystems = s;
        }
        if (ids != null) {
            return Collections.addAll(s, ids);
        }
        return false;

    }

    public boolean removeDisabled(String id) {
        Set<String> s = data.get().disabledSubsystems;
        if (s != null && id != null) {
            return s.remove(id);
        }
        return false;
    }

    public boolean removeAllDisabled(String... ids) {
        Set<String> s = data.get().disabledSubsystems;
        if (s != null && ids != null) {
            boolean changed = false;
            for (String string : ids) {
                changed |= s.remove(string);
            }
        }
        return false;
    }

    public void clearDisabled() {
        data.get().disabledSubsystems = null;
    }

    public boolean isDisabled(String id) {
        Set<String> s = data.get().disabledSubsystems;
        if (s == null || id == null || !s.contains(id)) {
            return false;
        }
        return true;

    }

    // ~ Locks
    // =========================================================================

    public Set<IObject> getLockCandidates() {
        Set<IObject> s = data.get().lockCandidates;
        if (s == null) {
            return new HashSet<IObject>();
        }
        return s;
    }

    public void appendLockCandidates(Set<IObject> set) {
        Set<IObject> s = data.get().lockCandidates;
        if (s == null) {
            s = new HashSet<IObject>();
            data.get().lockCandidates = s;
        }
        s.addAll(set);
    }

}
